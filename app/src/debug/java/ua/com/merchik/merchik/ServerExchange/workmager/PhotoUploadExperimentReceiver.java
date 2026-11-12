package ua.com.merchik.merchik.ServerExchange.workmager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Shell-only entry point, absent from release manifests and sources. */
public final class PhotoUploadExperimentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int count = intent.getIntExtra("count", 5);
        int minutes = intent.getIntExtra("minutes", 45);
        final List<Long> offsets;
        try {
            offsets = PhotoUploadExperiment.additionalOffsetsSeconds(count);
            PhotoUploadExperiment.expiresAt(System.currentTimeMillis(), minutes);
        } catch (IllegalArgumentException error) {
            setResultCode(1);
            setResultData(error.getMessage());
            return;
        }
        Context app = context.getApplicationContext();
        PendingResult pending = goAsync();
        new Thread(() -> {
            synchronized (PhotoUploadExperimentReceiver.class) {
                try {
                    WorkManager manager = WorkManager.getInstance(app);
                    manager.cancelAllWorkByTag(PhotoUploadExperiment.TAG).getResult().get(5, TimeUnit.SECONDS);
                    PhotoUploadWorker.schedule(app);
                    long expires = PhotoUploadExperiment.expiresAt(System.currentTimeMillis(), minutes);
                    List<PeriodicWorkRequest> requests = new ArrayList<>();
                    Constraints constraints = new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED).build();
                    for (long delay : offsets) {
                        requests.add(new PeriodicWorkRequest.Builder(PhotoUploadWorker.class, 15, TimeUnit.MINUTES)
                                .setInitialDelay(delay, TimeUnit.SECONDS)
                                .setConstraints(constraints)
                                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                                .setInputData(new Data.Builder().putLong(PhotoUploadExperiment.EXPIRES_AT, expires).build())
                                .addTag(PhotoUploadExperiment.TAG)
                                .build());
                    }
                    if (!requests.isEmpty()) manager.enqueue(requests).getResult().get(5, TimeUnit.SECONDS);
                    String message = "total=" + count + ", extra=" + requests.size()
                            + ", expiresAt=" + (requests.isEmpty() ? 0 : expires);
                    Log.i("PhotoUploadExperiment", message);
                    pending.setResultCode(0);
                    pending.setResultData(message);
                } catch (Exception error) {
                    WorkManager.getInstance(app).cancelAllWorkByTag(PhotoUploadExperiment.TAG);
                    Log.e("PhotoUploadExperiment", "Cannot configure experiment", error);
                    pending.setResultCode(1);
                    pending.setResultData(error.toString());
                } finally {
                    pending.finish();
                }
            }
        }, "PhotoUploadExperiment").start();
    }
}
