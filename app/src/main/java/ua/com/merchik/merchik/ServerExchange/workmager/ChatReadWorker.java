package ua.com.merchik.merchik.ServerExchange.workmager;

import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.JsonObject;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.synchronization.ChatReadPolicy;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

@Keep
public final class ChatReadWorker extends Worker {
    private static final String USER = "user_id";
    private static final String MESSAGE = "message_id";
    private volatile Call<JsonObject> activeCall;

    public ChatReadWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    public static Operation enqueue(Context context, int userId, int messageId) {
        if (userId <= 0 || messageId <= 0) throw new IllegalArgumentException("Invalid chat read receipt");
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ChatReadWorker.class)
                .setInputData(new Data.Builder().putInt(USER, userId).putInt(MESSAGE, messageId).build())
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build();
        return WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(
                "chat_read_" + userId + "_" + messageId, ExistingWorkPolicy.KEEP, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        int userId = getInputData().getInt(USER, 0);
        int messageId = getInputData().getInt(MESSAGE, 0);
        if (userId <= 0 || messageId <= 0) return Result.failure();
        if (userId != Globals.getCurrentUserId()) return Result.retry();
        AppDatabase db = RoomManager.SQL_DB;
        String session = Globals.getCurrentSessionId();
        if (db == null || session == null || session.isEmpty()) return Result.retry();
        try {
            if (isStopped()) return Result.retry();
            Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(
                    RetrofitBuilder.contentType, ChatReadPolicy.request(messageId));
            activeCall = call;
            if (isStopped()) call.cancel();
            if (userId != Globals.getCurrentUserId() || db != RoomManager.SQL_DB
                    || !Objects.equals(session, Globals.getCurrentSessionId())) return Result.retry();
            Response<JsonObject> response = call.execute();
            boolean accepted = response.isSuccessful() && ChatReadPolicy.acknowledged(response.body());
            if (response.errorBody() != null) response.errorBody().close();
            if (userId != Globals.getCurrentUserId() || db != RoomManager.SQL_DB
                    || !Objects.equals(session, Globals.getCurrentSessionId())) return Result.retry();
            Globals.writeToMLOG(accepted ? "INFO" : "ERROR", "ChatRead/receipt",
                    "messageId=" + messageId + ", http=" + response.code() + ", accepted=" + accepted);
            return accepted ? Result.success() : Result.retry();
        } catch (Exception error) {
            Globals.writeToMLOG("ERROR", "ChatRead/retry", "messageId=" + messageId
                    + ", attempt=" + getRunAttemptCount() + ", error=" + error.getClass().getSimpleName());
            return Result.retry();
        } finally {
            activeCall = null;
        }
    }

    @Override
    public void onStopped() {
        Call<JsonObject> call = activeCall;
        if (call != null) call.cancel();
    }
}
