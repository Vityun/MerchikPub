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
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.SyncResponsePolicy;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.ChatExchange;
import ua.com.merchik.merchik.data.synchronization.ChatSyncPolicy;
import ua.com.merchik.merchik.data.synchronization.TableName;

@Keep
public final class ChatSyncWorker extends Worker {
    private static final String USER = "user_id";
    private static final String TABLE = "table";
    private final ChatExchange exchange = new ChatExchange();

    public ChatSyncWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    public static void onFcmChat(Context context) {
        for (TableName table : ChatSyncPolicy.notificationTables(false)) enqueue(context, table, true);
    }

    public static void onWebSocketChat(Context context) {
        for (TableName table : ChatSyncPolicy.notificationTables(true)) enqueue(context, table, true);
    }

    public static void enqueue(Context context, TableName table, boolean notification) {
        ChatSyncPolicy.action(table);
        int userId = Globals.getCurrentUserId();
        if (context == null || userId <= 0) return;
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ChatSyncWorker.class)
                .setInputData(new Data.Builder().putInt(USER, userId).putString(TABLE, table.getCode()).build())
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build();
        // A notification arriving during an HTTP request needs a subsequent fetch, not KEEP.
        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(
                "chat_sync_" + userId + "_" + table.getCode(),
                notification ? ExistingWorkPolicy.APPEND_OR_REPLACE : ExistingWorkPolicy.KEEP, request);
        Globals.writeToMLOG("INFO", "ChatSync/enqueue", "table=" + table.getCode()
                + ", userId=" + userId + ", notification=" + notification);
    }

    @NonNull
    @Override
    public Result doWork() {
        int userId = getInputData().getInt(USER, 0);
        if (userId <= 0 || userId != Globals.getCurrentUserId()) return Result.success();
        TableName table = TableName.fromCode(getInputData().getString(TABLE));
        if (table != TableName.CHAT && table != TableName.CHAT_GROUP) return Result.failure();
        try {
            if (isStopped()) return Result.retry();
            exchange.download(table, userId);
            return Result.success();
        } catch (Exception error) {
            boolean retry = !isStopped() && SyncResponsePolicy.shouldRetry(error, getRunAttemptCount());
            Globals.writeToMLOG("ERROR", "ChatSync/failed", "table=" + table.getCode()
                    + ", attempt=" + getRunAttemptCount() + ", retry=" + retry
                    + ", VPI unchanged: " + SyncResponsePolicy.shortError(error.toString()));
            return retry ? Result.retry() : Result.failure();
        }
    }

    @Override
    public void onStopped() {
        exchange.cancel();
    }
}
