package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.ReportConductManager;
import ua.com.merchik.merchik.ServerExchange.QuestionAnswerAckPolicy;
import ua.com.merchik.merchik.data.QuestionAnswerDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.data.UploadToServ.QuestionAnswerUpload;
import ua.com.merchik.merchik.data.UploadToServ.QuestionAnswerUploadResponse;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class QuestionExchange {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final long RETRY_MS = 60_000L;
    private static AppDatabase sessionDb;
    private static int sessionUser;
    private static long generation;
    private static boolean uploading;
    private static long retryAfter;

    public void downloadQuestionAnswer() {
        ReferenceDataExchange.questions();
    }

    public static long uploadedThrough() {
        SynchronizationTimetableDB table = RealmManager.getSynchronizationTimetableRowByTable("question_answer");
        if (table == null) throw new IllegalStateException("Missing question_answer upload checkpoint");
        return QuestionAnswerAckPolicy.uploadedThrough(table.getVpi_app(), table.getVpo_app());
    }

    public void uploadQuestionAnswers() {
        int user = Globals.getCurrentUserId();
        AppDatabase db = SQL_DB;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            MAIN.post(() -> {
                if (user == Globals.getCurrentUserId() && db == SQL_DB) uploadQuestionAnswers();
            });
            return;
        }
        if (user <= 0 || db == null) return;
        if (sessionUser != user || sessionDb != db) {
            sessionUser = user;
            sessionDb = db;
            generation++;
            uploading = false;
            retryAfter = 0;
        }
        if (uploading || SystemClock.elapsedRealtime() < retryAfter) return;
        uploading = true;
        long token = ++generation;
        try {
            List<QuestionAnswerDB> rows = db.questionAnswerDao().getPendingForUpload(
                    Math.max(uploadedThrough(), QuestionAnswerAckPolicy.LOCAL_ID_MIN - 1), user);
            if (rows.isEmpty()) {
                uploading = false;
                return;
            }
            List<QuestionAnswerUpload> sent = new ArrayList<>();
            for (QuestionAnswerDB row : rows) sent.add(QuestionAnswerUpload.fromDb(row));
            StandartData data = new StandartData();
            data.mod = "quest_data";
            data.act = "add_row";
            data.data = sent;
            JsonObject request = new Gson().toJsonTree(data).getAsJsonObject();
            Globals.writeToMLOG("INFO", "uploadQuestionAnswersTest", "request: " + request);
            RetrofitBuilder.getRetrofitInterface().QUESTION_ANSWER_UPLOAD(RetrofitBuilder.contentType, request)
                    .enqueue(new Callback<QuestionAnswerUploadResponse>() {
                        @Override public void onResponse(Call<QuestionAnswerUploadResponse> call,
                                                         Response<QuestionAnswerUploadResponse> response) {
                            MAIN.post(() -> acknowledge(user, db, token, sent, response));
                        }

                        @Override public void onFailure(Call<QuestionAnswerUploadResponse> call, Throwable t) {
                            MAIN.post(() -> failed(user, db, token, t.toString()));
                        }
                    });
        } catch (Exception e) {
            failed(user, db, token, e.toString());
        }
    }

    private static boolean owns(int user, AppDatabase db, long token) {
        return token == generation && user == Globals.getCurrentUserId() && db == SQL_DB;
    }

    private static void acknowledge(int user, AppDatabase db, long token,
                                    List<QuestionAnswerUpload> sent,
                                    Response<QuestionAnswerUploadResponse> response) {
        if (!owns(user, db, token)) return;
        try {
            QuestionAnswerUploadResponse body = response.body();
            Globals.writeToMLOG("INFO", "uploadQuestionAnswersTest/onResponse",
                    "response.code(): " + response.code() + ", response.body(): " + new Gson().toJson(body));
            if (!response.isSuccessful() || body == null || !Boolean.TRUE.equals(body.state)) {
                failed(user, db, token, body != null ? body.error : "HTTP " + response.code());
                return;
            }
            SynchronizationTimetableDB table = RealmManager.getSynchronizationTimetableRowByTable("question_answer");
            if (table == null) throw new IllegalStateException("Missing question_answer upload checkpoint");
            long previous = QuestionAnswerAckPolicy.uploadedThrough(table.getVpi_app(), table.getVpo_app());
            long through = QuestionAnswerAckPolicy.acknowledgedThrough(previous, sent, body);
            if (through > previous) {
                table.setVpo_app(through);
                RealmManager.setToSynchronizationTimetableDB(table);
            }
            boolean complete = through >= Long.parseLong(sent.get(sent.size() - 1).elementId);
            uploading = false;
            retryAfter = complete ? 0 : SystemClock.elapsedRealtime() + RETRY_MS;
            Globals.writeToMLOG(complete ? "INFO" : "WARNING", "QuestionAnswerSync/ack",
                    "user=" + user + ", sent=" + sent.size() + ", previous=" + previous
                            + ", uploadedThrough=" + through + ", complete=" + complete);
            // The queued commands re-read the persisted acknowledgement, not the request snapshot.
            if (through > previous) MAIN.post(ReportConductManager::flushPending);
        } catch (Exception e) {
            failed(user, db, token, e.toString());
        }
    }

    private static void failed(int user, AppDatabase db, long token, String error) {
        if (!owns(user, db, token)) return;
        uploading = false;
        retryAfter = SystemClock.elapsedRealtime() + RETRY_MS;
        Globals.writeToMLOG("ERROR", "QuestionAnswerSync/upload",
                "user=" + user + ", error=" + error + ", keptPending=true, retryMs=" + RETRY_MS);
    }
}
