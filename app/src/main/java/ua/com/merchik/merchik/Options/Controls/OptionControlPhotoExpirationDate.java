package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.data.RealmModels.StackPhotoDB.PHOTO_EXPIRATION_DATE;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;

/*
Контроль наличия фото товаров СИСГ (с истекающим/истекшим сроком годности).
 */
public class OptionControlPhotoExpirationDate<T> extends OptionControl {
    public static final int OPTION_CONTROL_EXPIRATION_DATE_ID = 174877;

    public static final int DEFAULT_EXPIRATION_DAYS = 30;
    private static final String LOG_TAG = "ExpirationDate";

    private WpDataDB wp;
    private String documentDate, clientId, optionId;
    private int addressId, userId;
    private long dad2;

    public boolean signal = false;

    public OptionControlPhotoExpirationDate(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        // 1.0.
        try {
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            this.unlockCodeResultListener = unlockCodeResultListener;

            getDocumentVar();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    executeOption();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "OptionControlPhotoExpirationDate/executeOption", "Exception e: " + e);
                }
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPhotoExpirationDate", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wpDataDB = (WpDataDB) document;
            this.wpDataDB = wpDataDB;

            wp = wpDataDB;

            documentDate = Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000);

            clientId = wpDataDB.getClient_id();
            addressId = wpDataDB.getAddr_id();
            userId = wpDataDB.getUser_id();
            dad2 = wpDataDB.getCode_dad2();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        long dad2ForGetStackPhotoDB = dad2;
        int colMax = getExpirationDaysLimit();
        Date planDay = startOfDay(wp.getDt());
        Date expirationLimitDate = addDays(planDay, colMax);

        // values
        int signalInt = 0;         // Сигнал заблокированно или нет
        int err = 0;
        int totalTovSISG = 0;
        int totalPhoto = 0;

        List<String> spisTovOSV = new ArrayList<>();

        // 2.0. получим данные о товарах в отчете
        List<ReportPrepareDB> reportPrepareRaw = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(dad2));
        if (reportPrepareRaw == null) {
            reportPrepareRaw = new ArrayList<>();
        }

        // 3.0 получаем товары ОСВ из доп требований
        optionId = String.valueOf(resolveOptionForAdditionalRequirements());
        List<AdditionalRequirementsDB> additionalRequirements = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(document, true, Integer.valueOf(optionId), null, wp.getDt(), wp.getDt(), null, null, null, null);

        if (additionalRequirements != null) {
            for (AdditionalRequirementsDB item : additionalRequirements) {
                if (item != null) {
                    addUniqueTovarId(spisTovOSV, item.getTovarId());
                }
            }
            spisTovOSV.sort(null);
        }

        SpannableStringBuilder errMsgType1 = new SpannableStringBuilder();
        int errType1Cnt = 0;

        errMsgType1.append("Для наступних товарів потрібно зробити Фото СИСГ:").append("\n\n");

        // 4.0
        List<ReportPrepareDB> reportPrepare = selectReportPrepareByTovarAndExpire(reportPrepareRaw);

        // 5.0
        List<StackPhotoDB> stackPhotoDBS = RealmManager.stackPhotoByDad2AndType(dad2ForGetStackPhotoDB, PHOTO_EXPIRATION_DATE);
        if (stackPhotoDBS == null) {
            stackPhotoDBS = new ArrayList<>();
        }

        logSummary(
                "start",
                "dad2=" + dad2
                        + ", optionId=" + safeOptionValue(optionDB != null ? optionDB.getOptionId() : null)
                        + ", optionControlId=" + safeOptionValue(optionDB != null ? optionDB.getOptionControlId() : null)
                        + ", wpDate=" + formatDate(planDay)
                        + ", expirationLimit=" + formatDate(expirationLimitDate)
                        + ", days=" + colMax
                        + ", reportRaw=" + reportPrepareRaw.size()
                        + ", reportSelected=" + reportPrepare.size()
                        + ", osvCount=" + spisTovOSV.size()
                        + ", osvPreview=" + previewIds(spisTovOSV)
                        + ", photoType50=" + stackPhotoDBS.size()
        );

        // 6.0
        // Тут должна формироваться более подробная информация о том с какими Товарами есть проблема
        boolean showTovList = false;
        for (ReportPrepareDB item : reportPrepare) {
            if (item == null) continue;

            // 6.1
            ExpirationRequirementCheck requirementCheck = checkExpirationDatePhotoRequired(item, spisTovOSV, expirationLimitDate);
            logItemDecision(item, requirementCheck, planDay, expirationLimitDate);
            if (!requirementCheck.required) continue;

            totalTovSISG++;

            // 6.3
            Date dtExpire = requirementCheck.dtExpire;
            StackPhotoDB currentTovPhoto = findPhotoByTovar(stackPhotoDBS, item.getTovarId());
            if (currentTovPhoto != null) {
                totalPhoto++;
            } else {
                item.error = 1;
                item.errorNote = "Нема світлини товару у котрого "
                        + (dtExpire.after(planDay) ? "спливає" : "сплив")
                        + " термін придатності.";

                showTovList = true;
                errType1Cnt++;
                err++;
                errMsgType1.append(createLinkedString(item, null)).append("\n");
            }
        }

        // Формирование сообщения
        if (errType1Cnt > 0 || showTovList) {
            errMsgType1.append("потрібно зробити фото.").append("\n");
            spannableStringBuilder.append(errMsgType1);
        }
        if (err > 0) {
            notCloseSpannableStringBuilderDialog = true;
        }

        // 7.0
        // Тут формируются более короткие сообщения касательно наличия Фото СИСГ у Товаров
        if (reportPrepare.isEmpty()) {
            spannableStringBuilder.append("Товарів, по котрим треба перевіряти наявність Фото СИСГ, не знайдено.");
            signalInt = 1;
            signal = true;
        } else if (totalTovSISG == 0) {
            spannableStringBuilder.append("Товарів, по котрим треба виконати світлини 'СИСГ', не знайдено. Зауважень нема.");
            signalInt = 2;
            signal = false;
        } else if (totalTovSISG > 0 && stackPhotoDBS.isEmpty()) {
            spannableStringBuilder.append("Фото ").append(String.valueOf(totalTovSISG)).append(" товарів, у котрих спливає (сплив) термін придатності, НЕ знайдено!");
            signalInt = 1;
            signal = true;
        } else if (err > 0) {
            spannableStringBuilder.append("Не виконані світлини по ").append(String.valueOf(errType1Cnt)).append(" товарів, у котрих спливає (сплив) термін придатності. Див. список.");
            signalInt = 1;
            signal = true;
        } else {
            spannableStringBuilder.append("Зауважень по виготовленню світлин товарів, у котрих спливає (сплив) термін придатності СИСГ нема. Виготовлено ")
                    .append(String.valueOf(totalPhoto))
                    .append(" світлин по ")
                    .append(String.valueOf(totalTovSISG))
                    .append(" товарам.");
            signalInt = 2;
            signal = false;
        }

        spannableStringBuilder.append("\n\n").append(massageToUser != null ? massageToUser : "");

        // 7.0 сохраним сигнал
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                if (signal) {
                    double penalty = calculatePenalty(wpDataDB);
                    optionDB.setIsSignal("1");
                    optionDB.setSumPenalty(String.valueOf(penalty));
                } else {
                    optionDB.setIsSignal("2");
                    optionDB.setSumPenalty("0.00");
                }
                realm.insertOrUpdate(optionDB);
            }
        });

        // 8.0 Блокировка проведения
        if (signalInt == 1) {
            setIsBlockOption(true);
        }
        logSummary(
                "result",
                "dad2=" + dad2
                        + ", totalRequired=" + totalTovSISG
                        + ", totalPhoto=" + totalPhoto
                        + ", missing=" + err
                        + ", signal=" + signal
                        + ", signalInt=" + signalInt
        );
        checkUnlockCode(optionDB);
    }

    public static int getRequiredExpirationDatePhotoCount(WpDataDB wpDataDB, OptionsDB optionDB) {
        try {
            if (wpDataDB == null || wpDataDB.getDt() == null) return 0;

            int colMax = getExpirationDaysLimit(optionDB);
            Date expirationLimitDate = addDays(startOfDay(wpDataDB.getDt()), colMax);
            List<String> spisTovOSV = getAdditionalRequirementTovarIds(wpDataDB, optionDB);

            List<ReportPrepareDB> reportPrepareRaw = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(wpDataDB.getCode_dad2()));
            if (reportPrepareRaw == null) {
                reportPrepareRaw = new ArrayList<>();
            }

            int count = 0;
            for (ReportPrepareDB item : selectReportPrepareByTovarAndExpire(reportPrepareRaw)) {
                if (isExpirationDatePhotoRequired(item, spisTovOSV, expirationLimitDate)) {
                    count++;
                }
            }

            return count;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPhotoExpirationDate/getRequiredExpirationDatePhotoCount", "Exception e: " + e);
            return 0;
        }
    }

    private int resolveOptionForAdditionalRequirements() {
        return resolveOptionForAdditionalRequirements(optionDB);
    }

    private static int resolveOptionForAdditionalRequirements(OptionsDB optionDB) {
        int option = parseIntSafe(optionDB != null ? optionDB.getOptionId() : null);
        if (option > 0) {
            return option;
        }

        int optionControl = parseIntSafe(optionDB != null ? optionDB.getOptionControlId() : null);
        return optionControl > 0 ? optionControl : OPTION_CONTROL_EXPIRATION_DATE_ID;
    }

    private static List<String> getAdditionalRequirementTovarIds(WpDataDB wpDataDB, OptionsDB optionDB) {
        List<String> result = new ArrayList<>();
        if (wpDataDB == null || wpDataDB.getDt() == null) return result;

        int optionId = resolveOptionForAdditionalRequirements(optionDB);
        List<AdditionalRequirementsDB> additionalRequirements = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(
                wpDataDB,
                true,
                optionId,
                null,
                wpDataDB.getDt(),
                wpDataDB.getDt(),
                null,
                null,
                null,
                null
        );

        if (additionalRequirements != null) {
            for (AdditionalRequirementsDB item : additionalRequirements) {
                if (item != null) {
                    addUniqueTovarId(result, item.getTovarId());
                }
            }
            result.sort(null);
        }

        return result;
    }

    private static boolean isExpirationDatePhotoRequired(ReportPrepareDB item, List<String> spisTovOSV, Date expirationLimitDate) {
        return checkExpirationDatePhotoRequired(item, spisTovOSV, expirationLimitDate).required;
    }

    private static ExpirationRequirementCheck checkExpirationDatePhotoRequired(ReportPrepareDB item, List<String> spisTovOSV, Date expirationLimitDate) {
        ExpirationRequirementCheck result = new ExpirationRequirementCheck();
        if (item == null) {
            result.reason = "skip_item_null";
            return result;
        }

        result.dtExpire = parseExpireDate(item.getDtExpire());
        result.face = parseIntSafe(item.face);
        result.hasOsvFilter = spisTovOSV != null && !spisTovOSV.isEmpty();
        result.inOsv = !result.hasOsvFilter || spisTovOSV.contains(item.getTovarId());

        if (result.dtExpire == null) {
            result.reason = "skip_empty_or_bad_expire_date";
        } else if (result.face <= 0) {
            result.reason = "skip_face_zero";
        } else if (!result.dtExpire.before(expirationLimitDate)) {
            result.reason = "skip_out_of_period";
        } else if (!result.inOsv) {
            result.reason = "skip_not_in_osv";
        } else {
            result.required = true;
            result.reason = "required";
        }

        return result;
    }

    private static List<ReportPrepareDB> selectReportPrepareByTovarAndExpire(List<ReportPrepareDB> reportPrepare) {
        Map<String, ReportPrepareDB> map = new LinkedHashMap<>();

        for (ReportPrepareDB item : reportPrepare) {
            if (item == null || isEmpty(item.getTovarId())) continue;

            String key = item.getTovarId() + "|" + normalizeDateKey(item.getDtExpire());
            ReportPrepareDB current = map.get(key);
            if (current == null || shouldReplaceReportPrepare(current, item)) {
                map.put(key, item);
            }
        }

        return new ArrayList<>(map.values());
    }

    private static boolean shouldReplaceReportPrepare(ReportPrepareDB current, ReportPrepareDB candidate) {
        int currentScore = getReportPrepareScore(current);
        int candidateScore = getReportPrepareScore(candidate);

        if (candidateScore != currentScore) {
            return candidateScore > currentScore;
        }

        long currentDtChange = current != null ? current.getDtChange() : 0;
        long candidateDtChange = candidate != null ? candidate.getDtChange() : 0;
        if (candidateDtChange != currentDtChange) {
            return candidateDtChange > currentDtChange;
        }

        long currentId = current != null && current.getID() != null ? current.getID() : 0;
        long candidateId = candidate != null && candidate.getID() != null ? candidate.getID() : 0;
        return candidateId > currentId;
    }

    private static int getReportPrepareScore(ReportPrepareDB item) {
        if (item == null) return 0;

        int score = 0;
        if (parseIntSafe(item.face) > 0) score += 1000;
        if (!isEmpty(item.dtExpire) && !"0000-00-00".equals(item.dtExpire)) score += 100;
        if (parseIntSafe(item.oborotvedNum) > 0) score += 10;
        if (item.amount > 0) score += 5;
        return score;
    }

    private static void addUniqueTovarId(List<String> tovIds, String tovarId) {
        if (tovIds == null || isEmpty(tovarId) || "0".equals(tovarId) || tovIds.contains(tovarId)) return;
        tovIds.add(tovarId);
    }

    private StackPhotoDB findPhotoByTovar(List<StackPhotoDB> stackPhotoDBS, String tovarId) {
        if (stackPhotoDBS == null || isEmpty(tovarId)) return null;

        for (StackPhotoDB item : stackPhotoDBS) {
            if (item != null && tovarId.equals(item.tovar_id)) {
                return item;
            }
        }
        return null;
    }

    private int getExpirationDaysLimit() {
        return getExpirationDaysLimit(optionDB);
    }

    private static int getExpirationDaysLimit(OptionsDB optionDB) {
        int amountMax = parseIntSafe(optionDB != null ? optionDB.getAmountMax() : null);
        return amountMax > 0 ? amountMax : DEFAULT_EXPIRATION_DAYS;
    }

    private static int parseIntSafe(String value) {
        if (isEmpty(value)) return 0;
        String normalizedValue = value.trim().replace(',', '.');

        try {
            return Integer.parseInt(normalizedValue);
        } catch (Exception e) {
            try {
                double result = Double.parseDouble(normalizedValue);
                if (result > 0 && result < 1) return 1;
                return (int) result;
            } catch (Exception ignored) {
                return 0;
            }
        }
    }

    private static Date parseExpireDate(String value) {
        if (isEmpty(value) || "0000-00-00".equals(value.trim())) {
            return null;
        }

        String normalizedValue = value.trim();
        String[] patterns = new String[]{"yyyy-MM-dd", "dd.MM.yyyy", "dd-MM-yyyy", "yyyyMMdd"};

        for (String pattern : patterns) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
                format.setLenient(false);
                Date date = format.parse(normalizedValue);
                if (date != null) {
                    return startOfDay(date);
                }
            } catch (ParseException ignored) {
            }
        }

        return null;
    }

    private static Date startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    private static String normalizeDateKey(String rawValue) {
        Date expireDate = parseExpireDate(rawValue);
        if (expireDate == null) {
            return isEmpty(rawValue) ? "" : rawValue.trim();
        }

        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(expireDate);
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void logSummary(String stage, String message) {
        String fullMessage = stage + ": " + message;
        Log.e(LOG_TAG, fullMessage);
        Globals.writeToMLOG("INFO", "OptionControlPhotoExpirationDate/" + stage, message);
    }

    private static void logItemDecision(ReportPrepareDB item, ExpirationRequirementCheck check, Date planDay, Date expirationLimitDate) {
        if (item == null || check == null) return;

        Log.e(LOG_TAG, "item: tovarId=" + item.getTovarId()
                + ", faceRaw=" + safeOptionValue(item.face)
                + ", face=" + check.face
                + ", dtExpireRaw=" + safeOptionValue(item.getDtExpire())
                + ", dtExpire=" + formatDate(check.dtExpire)
                + ", wpDate=" + formatDate(planDay)
                + ", expirationLimit=" + formatDate(expirationLimitDate)
                + ", hasOsvFilter=" + check.hasOsvFilter
                + ", inOsv=" + check.inOsv
                + ", required=" + check.required
                + ", reason=" + check.reason);
    }

    private static String formatDate(Date date) {
        if (date == null) return "null";
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
    }

    private static String previewIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return "[]";

        int limit = Math.min(ids.size(), 10);
        List<String> preview = ids.subList(0, limit);
        return preview + (ids.size() > limit ? "...+" + (ids.size() - limit) : "");
    }

    private static String safeOptionValue(String value) {
        return value == null ? "null" : value;
    }

    private static class ExpirationRequirementCheck {
        boolean required = false;
        Date dtExpire = null;
        int face = 0;
        boolean hasOsvFilter = false;
        boolean inOsv = false;
        String reason = "unknown";
    }

    /**
     * 05.03.23.
     * Делает кликабельным Товар, что б при клике можно было б сделать фото.
     *
     * @param stackPhotoDB: Фото из БД. Нужно для того что б правильно отрисовывать цвета товарам.
     */
    private SpannableString createLinkedString(ReportPrepareDB item, StackPhotoDB stackPhotoDB) {
        TovarDB tov = TovarRealm.getById(item.getTovarId());
        String tovName = tov != null && tov.getNm() != null ? tov.getNm().replace("&quot;", "\"") : "товар";
        String tovBarcode = tov != null && tov.getBarcode() != null ? tov.getBarcode() : item.getTovarId();
        String tovWeight = tov != null && tov.getWeight() != null ? tov.getWeight() : "";
//        String errorNote = item.errorNote != null ? " - " + item.errorNote : "";
        String msg = String.format("(%s) %s (%s)", tovBarcode, tovName, tovWeight);
        SpannableString res = new SpannableString(msg);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Activity activity = findActivity(textView.getContext());
                if (activity == null) {
                    Toast.makeText(textView.getContext(), "Не вдалося відкрити камеру для Фото СИСГ.", Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(textView.getContext(), "Виготовлення світлини по товару(" + tovBarcode + "): " + tovName, Toast.LENGTH_LONG).show();

                resetPhotoContext();

                Globals.writeToMLOG("INFO", "OptionControlPhotoExpirationDate/createLinkedString", "wp_dad2: " + wp.getCode_dad2());
                Globals.writeToMLOG("INFO", "OptionControlPhotoExpirationDate/createLinkedString", "tov.id: " + item.getTovarId());

                new MakePhoto().pressedMakePhoto(activity, wp, optionDB, String.valueOf(PHOTO_EXPIRATION_DATE), item.getTovarId(), () -> {
                });
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                if (stackPhotoDB != null) {
                    if (stackPhotoDB.get_on_server != 0) {
                        ds.setColor(Color.GREEN);
                    } else if (stackPhotoDB.create_time != 0 && stackPhotoDB.upload_to_server != 0) {
                        ds.setColor(Color.YELLOW);
                    } else {
                        ds.setColor(Color.RED);
                    }
                } else {
                    ds.setColor(Color.BLUE);
                }
                ds.setUnderlineText(true);
            }
        };

        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return res;
    }

    private void resetPhotoContext() {
        MakePhoto.img_src_id = "";
        MakePhoto.showcase_id = "";
        MakePhoto.planogram_id = "";
        MakePhoto.planogram_img_id = "";
        MakePhoto.example_id = "";
        MakePhoto.example_img_id = "";
    }

    private Activity findActivity(Context context) {
        Context currentContext = context;
        while (currentContext instanceof ContextWrapper) {
            if (currentContext instanceof Activity) {
                return (Activity) currentContext;
            }
            currentContext = ((ContextWrapper) currentContext).getBaseContext();
        }
        return null;
    }
}
