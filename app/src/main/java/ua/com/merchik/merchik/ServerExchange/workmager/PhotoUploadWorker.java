package ua.com.merchik.merchik.ServerExchange.workmager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.BuildConfig;
import ua.com.merchik.merchik.PhotoReports;

@Keep
public final class PhotoUploadWorker extends Worker {
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile PhotoReports reports;
    private volatile boolean cancelled;

    public PhotoUploadWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    public static void schedule(Context context) {
        WorkManager manager = WorkManager.getInstance(context.getApplicationContext());
        // Retire the old 0/7.5-minute schedule and temporary load-test jobs.
        manager.cancelUniqueWork("PhotoUploadA");
        manager.cancelUniqueWork("PhotoUploadB");
        manager.cancelAllWorkByTag(PhotoUploadExperiment.TAG);
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();
        for (int slot = 0; slot < 5; slot++) {
            PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                    PhotoUploadWorker.class, 15, TimeUnit.MINUTES)
                    .setInitialDelay(slot * 3L, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                    .build();
            manager.enqueueUniquePeriodicWork(
                    "PhotoUpload5_" + slot, ExistingPeriodicWorkPolicy.KEEP, request);
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        boolean experiment = getTags().contains(PhotoUploadExperiment.TAG);
        if (experiment && PhotoUploadExperiment.mustStop(BuildConfig.DEBUG,
                getInputData().getLong(PhotoUploadExperiment.EXPIRES_AT, 0), System.currentTimeMillis())) {
            WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(PhotoUploadExperiment.TAG);
            Globals.writeToMLOG("INFO", "PhotoUploadWorker/experimentFinished", "work=" + getId());
            return Result.success();
        }
        int userId = Globals.getCurrentUserId();
        if (userId <= 0 || isStopped()) return Result.success();
        long started = SystemClock.elapsedRealtime();
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean retry = new AtomicBoolean();
        Globals.writeToMLOG("INFO", "PhotoUploadWorker/start", "work=" + getId() + ", userId=" + userId
                + ", experiment=" + experiment);
        // The existing uploader and its Realm instance are main-thread confined.
        main.post(() -> {
            if (cancelled || isStopped() || userId != Globals.getCurrentUserId()) {
                done.countDown();
                return;
            }
            try {
                reports = new PhotoReports(getApplicationContext());
                reports.uploadInBackground(failed -> {
                    retry.set(failed);
                    done.countDown();
                });
            } catch (Exception error) {
                retry.set(true);
                Globals.writeToMLOG("ERROR", "PhotoUploadWorker/start", error.toString());
                done.countDown();
            }
        });
        try {
            if (!done.await(8, TimeUnit.MINUTES)) {
                cancelUpload();
                Globals.writeToMLOG("WARNING", "PhotoUploadWorker/timeout", "work=" + getId());
                return Result.retry();
            }
            Globals.writeToMLOG("INFO", "PhotoUploadWorker/finish", "work=" + getId()
                    + ", retry=" + retry.get() + ", stopped=" + isStopped()
                    + ", elapsedMs=" + (SystemClock.elapsedRealtime() - started));
            return retry.get() ? Result.retry() : Result.success();
        } catch (InterruptedException error) {
            cancelUpload();
            Thread.currentThread().interrupt();
            return Result.retry();
        }
    }

    private void cancelUpload() {
        cancelled = true;
        main.post(() -> {
            if (reports != null) reports.cancelBackgroundUpload();
        });
    }

    @Override
    public void onStopped() {
        cancelUpload();
    }
}
