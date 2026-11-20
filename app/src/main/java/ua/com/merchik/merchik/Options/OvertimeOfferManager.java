package ua.com.merchik.merchik.Options;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Activities.MyApplication;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.WorkTimeRules;
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.features.main.DBViewModels.AchievementsSDBViewModel;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/** Optional post-completion flow, independent of the report-conduct outbox. Main thread only. */
public final class OvertimeOfferManager {
    public static final String EXTRA_OFFER = "overtime_offer";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final Gson GSON = new Gson();
    private static final Set<String> REQUESTS = new HashSet<>();

    public static final class Offer {
        public int userId;
        public long dad2;
        public String clientId;
        public int addrId;
        public int themeId;
        public long start;
        public long end;
        public BigDecimal planSeconds;
        public BigDecimal baselineSeconds;
        public String status = "pending";

        public boolean isPlanned() { return OvertimeOfferPolicy.VITMARK.equals(clientId); }
        public double baselineMinutes() { return baselineSeconds.doubleValue() / 60.0; }
        public String baselineText() { return OvertimeOfferPolicy.minutes(baselineSeconds); }
    }

    private OvertimeOfferManager() { }

    private static SharedPreferences prefs() {
        return MyApplication.getAppContext().getSharedPreferences("overtime_offers", Context.MODE_PRIVATE);
    }

    private static String key(int userId, long dad2) { return userId + ":" + dad2; }

    private static Offer load(long dad2) {
        return GSON.fromJson(prefs().getString(key(Globals.getCurrentUserId(), dad2), null), Offer.class);
    }

    private static void save(Offer offer) {
        prefs().edit().putString(key(offer.userId, offer.dad2), GSON.toJson(offer)).apply();
    }

    private static void log(long dad2, String message) {
        Globals.writeToMLOG("INFO", "OvertimeOffer", "dad2=" + dad2 + ", " + message);
    }

    private static void skip(Offer offer, String reason) {
        offer.status = "done";
        save(offer);
        log(offer.dad2, "skip=" + reason);
    }

    /** Called only after the local work-end transaction succeeded. */
    public static void workEndSaved(long dad2) {
        try {
            Offer previous = load(dad2);
            if (previous != null) return;
            WpDataDB visit = RealmManager.getWorkPlanRowByCodeDad2(dad2);
            if (visit == null || Globals.getCurrentUserId() <= 0) return;
            WorkTimeRules.Times times = times(visit);
            if (times.end <= times.start || times.start <= 0) return;
            Offer offer = new Offer();
            offer.userId = Globals.getCurrentUserId();
            offer.dad2 = dad2;
            offer.clientId = visit.getClient_id();
            offer.addrId = visit.getAddr_id();
            offer.themeId = visit.getTheme_id();
            offer.start = times.start;
            offer.end = times.end;
            save(offer);
            MAIN.post(() -> resume(dad2));
        } catch (Exception e) { log(dad2, "enqueue failed: " + e); }
    }

    private static WorkTimeRules.Times times(WpDataDB visit) {
        return WorkTimeRules.normalize(visit.getVisit_start_dt(), visit.getClient_start_dt(),
                visit.getVisit_end_dt(), visit.getClient_end_dt());
    }

    public static boolean matches(Offer offer, WpDataDB visit) {
        if (offer == null || visit == null || offer.userId != Globals.getCurrentUserId()
                || visit.getCode_dad2() != offer.dad2 || !Objects.equals(offer.clientId, visit.getClient_id())
                || offer.addrId != visit.getAddr_id() || offer.themeId != visit.getTheme_id()) return false;
        WorkTimeRules.Times times = times(visit);
        return times.start == offer.start && times.end == offer.end && times.end > times.start;
    }

    private static boolean hasAchievement(Offer offer) {
        for (AchievementsSDB achievement : RoomManager.SQL_DB.achievementsDao().getByDad2(offer.dad2)) {
            // Unsynced local achievements do not yet have userId; dad2 already scopes the visit.
            if (Integer.valueOf(OvertimeOfferPolicy.ACHIEVEMENT_THEME).equals(achievement.themeId)) return true;
        }
        return false;
    }

    public static void resume(long dad2) {
        try {
            Offer offer = load(dad2);
            if (offer == null || "done".equals(offer.status)) return;
            WpDataDB visit = RealmManager.getWorkPlanRowByCodeDad2(dad2);
            if (!matches(offer, visit)) { skip(offer, "visit_changed"); return; }
            if (offer.themeId == OvertimeOfferPolicy.SUPPR_THEME) { skip(offer, "suppr"); return; }
            if (hasAchievement(offer)) { skip(offer, "achievement_exists"); return; }
            BigDecimal plan = eligiblePlan(offer, visit);
            if (plan == null) return;
            if ("ready".equals(offer.status) && matchesEvaluatedPlan(offer, plan)) {
                if (OvertimeOfferPolicy.exceedsPlan(offer.end - offer.start, plan,
                        overtimePercent(offer, offer.baselineSeconds))) {
                    ReportConductUi.showOvertimeOffer(dad2);
                } else skip(offer, "plan_threshold_not_exceeded");
                return;
            }
            if (offer.isPlanned()) {
                evaluated(offer, plan, plan);
                return;
            }
            if (offer.clientId == null || offer.clientId.isEmpty() || offer.addrId <= 0 || offer.themeId <= 0) {
                skip(offer, "missing_request_parameters"); return;
            }
            if ("ready".equals(offer.status)) {
                // A saved offer without a matching plan may still contain the old duration units.
                offer.status = "pending";
                offer.planSeconds = null;
                offer.baselineSeconds = null;
                save(offer);
            }
            String requestKey = key(offer.userId, dad2);
            if (!REQUESTS.add(requestKey)) return;
            try {
                RetrofitBuilder.getSilentRetrofitInterface().GET_AVERAGE_WORK_TIME(
                        OvertimeOfferPolicy.averageRequest(offer.clientId, offer.addrId, offer.themeId, dad2))
                        .enqueue(new Callback<JsonObject>() {
                            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                MAIN.post(() -> {
                                    REQUESTS.remove(requestKey);
                                    complete(offer, response.isSuccessful()
                                            ? OvertimeOfferPolicy.averageSeconds(response.body()) : null);
                                });
                            }
                            @Override public void onFailure(Call<JsonObject> call, Throwable error) {
                                MAIN.post(() -> {
                                    REQUESTS.remove(requestKey);
                                    log(dad2, "avg_time failed: " + error);
                                    complete(offer, null);
                                });
                            }
                        });
            } catch (Exception e) { REQUESTS.remove(requestKey); throw e; }
        } catch (Exception e) { log(dad2, "evaluation failed: " + e); }
    }

    private static BigDecimal eligiblePlan(Offer offer, WpDataDB visit) {
        BigDecimal plan = OvertimeOfferPolicy.plannedSeconds(visit.getDuration_plan());
        if (plan == null) {
            skip(offer, "invalid_plan");
            return null;
        }
        long actual = offer.end - offer.start;
        // The server response selects the exact threshold; do not filter out a possible 10% case.
        int precheckPercent = offer.isPlanned() ? OvertimeOfferPolicy.DEFAULT_OVERTIME_PERCENT
                : OvertimeOfferPolicy.MIN_OVERTIME_PERCENT;
        log(offer.dad2, "actualSeconds=" + actual + ", planSeconds=" + plan + ", precheckPercent=" + precheckPercent);
        if (!OvertimeOfferPolicy.exceedsPlan(actual, plan, precheckPercent)) {
            skip(offer, "plan_threshold_not_exceeded");
            return null;
        }
        return plan;
    }

    private static void complete(Offer offer, BigDecimal average) {
        try {
            // Login/visit changes while the request is in flight must not affect another visit.
            if (offer.userId != Globals.getCurrentUserId()) return;
            Offer current = load(offer.dad2);
            if (current == null || !"pending".equals(current.status)) return;
            WpDataDB visit = RealmManager.getWorkPlanRowByCodeDad2(offer.dad2);
            if (!matches(offer, visit)) {
                skip(offer, "visit_changed_during_request"); return;
            }
            if (hasAchievement(offer)) { skip(offer, "achievement_exists"); return; }
            BigDecimal plan = eligiblePlan(offer, visit);
            if (plan != null) evaluated(offer, plan, average);
        } catch (Exception e) { log(offer.dad2, "response failed: " + e); }
    }

    private static void evaluated(Offer offer, BigDecimal plan, BigDecimal baseline) {
        if (baseline == null) { skip(offer, "average_unavailable"); return; }
        int overtimePercent = overtimePercent(offer, baseline);
        log(offer.dad2, "source=" + (offer.isPlanned() ? "plan" : "server_average")
                + ", planSeconds=" + plan + ", baselineSeconds=" + baseline + ", overtimePercent=" + overtimePercent);
        if (!offer.isPlanned() && !OvertimeOfferPolicy.acceptsAverage(plan, baseline)) {
            skip(offer, "average_too_short_for_plan"); return;
        }
        if (!OvertimeOfferPolicy.exceedsPlan(offer.end - offer.start, plan, overtimePercent)) {
            skip(offer, "plan_threshold_not_exceeded"); return;
        }
        offer.planSeconds = plan;
        offer.baselineSeconds = baseline;
        offer.status = "ready";
        save(offer);
        ReportConductUi.showOvertimeOffer(offer.dad2);
    }

    private static int overtimePercent(Offer offer, BigDecimal baseline) {
        return offer.isPlanned() ? OvertimeOfferPolicy.DEFAULT_OVERTIME_PERCENT
                : OvertimeOfferPolicy.overtimePercentForAverage(baseline);
    }

    private static boolean matchesEvaluatedPlan(Offer offer, BigDecimal plan) {
        return plan != null && offer.planSeconds != null && plan.compareTo(offer.planSeconds) == 0
                && (offer.isPlanned() ? offer.baselineSeconds != null && plan.compareTo(offer.baselineSeconds) == 0
                : OvertimeOfferPolicy.acceptsAverage(plan, offer.baselineSeconds));
    }

    public static boolean isApplicable(Offer offer, WpDataDB visit) {
        if (!matches(offer, visit)) return false;
        BigDecimal plan = OvertimeOfferPolicy.plannedSeconds(visit.getDuration_plan());
        return matchesEvaluatedPlan(offer, plan) && OvertimeOfferPolicy.exceedsPlan(
                offer.end - offer.start, plan, overtimePercent(offer, offer.baselineSeconds));
    }

    public static Offer ready(long dad2) {
        try {
            Offer offer = load(dad2);
            return offer != null && "ready".equals(offer.status)
                    && isApplicable(offer, RealmManager.getWorkPlanRowByCodeDad2(dad2))
                    && !hasAchievement(offer) ? offer : null;
        } catch (Exception e) { log(dad2, "ready failed: " + e); return null; }
    }

    public static String message(Offer offer) {
        BigDecimal actualSeconds = BigDecimal.valueOf(offer.end - offer.start);
        return "Фактическая продолжительность визита: " + OvertimeOfferPolicy.minutes(actualSeconds) + " мин."
                + "<br>Плановая продолжительность: " + OvertimeOfferPolicy.minutes(offer.planSeconds) + " мин."
                + (offer.isPlanned() ? "" : "<br>Средняя продолжительность предыдущих визитов: " + offer.baselineText() + " мин.")
                + "<br>Текущая работа заняла на "
                + OvertimeOfferPolicy.minutes(actualSeconds.subtract(offer.planSeconds)) + " мин. больше плана."
                + "<br>У вас есть возможность получить дополнительную оплату от клиента за эту переработку. " +
                        "Для этого необходимо оформить заявку которая будет передана клиенту на рассмотрение."
                + "<br>Бажаєте оформити заявку на разове підвищення оплати за перепрацювання?";
    }

    public static void dismiss(Offer offer) {
        try { offer.status = "done"; save(offer); log(offer.dad2, "offer_closed"); }
        catch (Exception e) { log(offer.dad2, "dismiss failed: " + e); }
    }

    public static void open(Activity activity, Offer offer) {
        try {
            Offer current = ready(offer.dad2);
            if (current == null) return;
            Intent intent = new Intent(activity, FeaturesActivity.class);
            intent.putExtra("viewModel", AchievementsSDBViewModel.class.getCanonicalName());
            intent.putExtra("contextUI", "ACHIEVEMENT");
            intent.putExtra("modeUI", "DEFAULT");
            intent.putExtra("dataJson", GSON.toJson(offer.dad2));
            intent.putExtra("title", "Досягнення");
            intent.putExtra("one_time_payment_increase", true);
            intent.putExtra(EXTRA_OFFER, GSON.toJson(current));
            // Keep the visit activity underneath the achievement and its two dialogs.
            activity.startActivity(intent);
            dismiss(current);
        } catch (Exception e) { log(offer.dad2, "open failed: " + e); }
    }
}
