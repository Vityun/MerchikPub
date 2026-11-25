package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import retrofit2.Response;
import ua.com.merchik.merchik.Activities.MyApplication;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirements.AdditionalRequirementsServerData;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public final class AdditionalRequirementsExchange {
    private static final String TAG = "AdditionalRequirements/list";
    // Serialize the wide replacement and the daily merge, including their network requests.
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();

    private AdditionalRequirementsExchange() {}

    public static void refreshDaily() {
        enqueue(AdditionalRequirementsSnapshot.DAILY);
    }

    public static void downloadFull() {
        enqueue(AdditionalRequirementsSnapshot.FULL);
    }

    private static void enqueue(AdditionalRequirementsSnapshot mode) {
        int user = Globals.getCurrentUserId();
        String session = Globals.getCurrentSessionId();
        if (user <= 0 || session == null || session.isEmpty()) return;
        LocalDate day = AdditionalRequirementsSnapshot.dayAt(TrustedTime.nowServerSecOrLocalSec());
        String key = user + ":" + session + ":" + mode + ":" + day;
        if (!PENDING.add(key)) return;
        WORKER.execute(() -> {
            try {
                if (!isCurrent(user, session)) return;
                SharedPreferences schedule = MyApplication.getAppContext().getSharedPreferences(
                        "additional_requirements_daily", Context.MODE_PRIVATE);
                String receiptKey = String.valueOf(user);
                boolean daily = mode == AdditionalRequirementsSnapshot.DAILY;
                if (mode.alreadyDownloaded(day, schedule.getString(receiptKey, null))) return;

                JsonObject request = mode.request(day);
                Globals.writeToMLOG("INFO", TAG, mode + " user=" + user + " " + request);
                Response<AdditionalRequirementsServerData> response = RetrofitBuilder.getRetrofitInterface()
                        .GET_TABLE_AdditionalRequirementsDB(RetrofitBuilder.contentType, request).execute();
                if (!response.isSuccessful()) throw new IllegalStateException("HTTP " + response.code());
                List<AdditionalRequirementsDB> rows = mode.rows(response.body());
                if (!isCurrent(user, session)) return;
                if (!rows.isEmpty()) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.executeTransaction(r -> {
                            if (!isCurrent(user, session)) throw new IllegalStateException("Session changed");
                            AdditionalRequirementsRealm.replaceDownloadedRows(r, rows, !daily);
                        });
                    }
                }
                // A valid empty list completes today's check, but never clears data or advances VPI.
                if (daily && isCurrent(user, session))
                    schedule.edit().putString(receiptKey, day.toString()).apply();
                Globals.writeToMLOG("INFO", TAG, mode + " user=" + user + " rows=" + rows.size());
            } catch (Exception e) {
                Globals.writeToMLOG("ERR", TAG, mode + " user=" + user + " " + e);
            } finally {
                PENDING.remove(key);
            }
        });
    }

    private static boolean isCurrent(int user, String session) {
        return user == Globals.getCurrentUserId() && Objects.equals(session, Globals.getCurrentSessionId());
    }
}
