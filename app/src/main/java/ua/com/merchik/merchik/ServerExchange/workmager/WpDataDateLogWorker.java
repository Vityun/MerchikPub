package ua.com.merchik.merchik.ServerExchange.workmager;

import android.content.Context;
import android.content.SharedPreferences;

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

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.realm.RealmObject;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.FileCompressor;
import ua.com.merchik.merchik.Utils.WpDataDateMismatch;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

@Keep
public final class WpDataDateLogWorker extends Worker {
    private static final String TAG = "WpDataDateLog";
    private static final String PREFS = "wp_data_date_diagnostics";
    private static final String KEY_DAD2 = "dad2";
    private static final int MAX_ATTEMPTS = 3;
    private static final ExecutorService PREPARE = Executors.newSingleThreadExecutor();
    private volatile Call<Void> uploadCall;

    public WpDataDateLogWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    public static void onVisitOpened(Context context, WpDataDB wp) {
        try {
            if (wp == null || !RealmObject.isValid(wp)) return;
            ZoneId zone = ZoneId.systemDefault();
            WpDataDateMismatch mismatch = WpDataDateMismatch.find(wp.getCode_dad2(), wp.getDt(), zone);
            if (mismatch == null) return;

            Context app = context.getApplicationContext();
            SharedPreferences prefs = preferences(app);
            if (prefs.getBoolean("finished_" + mismatch.codeDad2, false)) return;

            // Detach and serialize on the Realm owner's thread, never inside the worker.
            WpDataDB snapshot = RealmObject.isManaged(wp) ? wp.getRealm().copyFromRealm(wp) : wp;
            String diagnostic = mismatch.diagnostic(new Gson().toJson(snapshot), Globals.getCurrentUserId());
            String fileName = mismatch.archiveName(LocalDate.now(zone));
            PREPARE.execute(() -> prepareAndEnqueue(app, mismatch.codeDad2, fileName, diagnostic));
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", TAG + "/onVisitOpened", e.toString());
        }
    }

    private static void prepareAndEnqueue(Context context, long dad2, String fileName, String diagnostic) {
        File snapshot = null;
        try {
            SharedPreferences prefs = preferences(context);
            if (prefs.getBoolean("finished_" + dad2, false)) return;

            File directory = directory(context, dad2);
            String pendingName = prefs.getString("file_" + dad2, null);
            File archive = pendingName == null ? null : new File(directory, pendingName);
            if (archive == null || !archive.isFile() || archive.length() == 0) {
                if (!directory.isDirectory() && !directory.mkdirs()) {
                    throw new IOException("Cannot create diagnostic directory");
                }
                Globals.writeToMLOG("ERROR", TAG + "/mismatch", diagnostic);
                snapshot = new File(directory, "M_LOG_COPIED.txt");
                WpDataDateMismatch.writeSnapshot(new File(context.getCacheDir(), "M_LOG.txt"), snapshot, diagnostic);
                archive = FileCompressor.compressFile(snapshot.getPath(), new File(directory, fileName).getPath());
                if (archive == null || !archive.isFile() || archive.length() == 0) {
                    throw new IOException("Cannot compress diagnostic log");
                }
                if (!prefs.edit().putString("file_" + dad2, archive.getName()).commit()) {
                    throw new IOException("Cannot persist diagnostic log state");
                }
            }

            OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(WpDataDateLogWorker.class)
                    .setInputData(new Data.Builder().putLong(KEY_DAD2, dad2).build())
                    .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                    .build();
            WorkManager.getInstance(context).enqueueUniqueWork(TAG + "_" + dad2, ExistingWorkPolicy.KEEP, work)
                    .getResult().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Globals.writeToMLOG("ERROR", TAG + "/prepare", e.toString());
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", TAG + "/prepare", "dad2=" + dad2 + ", " + e);
        } finally {
            deleteFile(snapshot);
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        long dad2 = getInputData().getLong(KEY_DAD2, 0);
        if (dad2 <= 0) return Result.failure();
        SharedPreferences prefs = preferences(getApplicationContext());
        if (prefs.getBoolean("finished_" + dad2, false)) return Result.success();

        String name = prefs.getString("file_" + dad2, null);
        if (name == null) return Result.failure();
        File archive = new File(directory(getApplicationContext(), dad2), name);
        if (!archive.isFile() || archive.length() == 0) {
            Globals.writeToMLOG("ERROR", TAG + "/upload", "Missing archive for dad2=" + dad2);
            return Result.failure();
        }

        try {
            RequestBody body = RequestBody.create(MediaType.parse("application/gzip"), archive);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", archive.getName(), body);
            uploadCall = RetrofitBuilder.getSilentRetrofitInterface().UPLOAD_ZIP_FILE("debug_log_file", "send", part);
            if (isStopped()) return Result.retry();
            Response<Void> response = uploadCall.execute();
            boolean successful = response.isSuccessful();
            int statusCode = response.code();
            if (response.errorBody() != null) response.errorBody().close();
            if (successful) {
                finish(prefs, dad2, "sent");
                deleteFile(archive);
                Globals.writeToMLOG("INFO", TAG + "/upload", "Sent dad2=" + dad2 + ", file=" + name);
                return Result.success();
            }
            Globals.writeToMLOG("ERROR", TAG + "/upload", "dad2=" + dad2 + ", HTTP=" + statusCode);
            return retryOrFinish(prefs, dad2, archive);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", TAG + "/upload", "dad2=" + dad2 + ", " + e);
            return retryOrFinish(prefs, dad2, archive);
        }
    }

    private Result retryOrFinish(SharedPreferences prefs, long dad2, File archive) {
        if (getRunAttemptCount() + 1 < MAX_ATTEMPTS) return Result.retry();
        finish(prefs, dad2, "failed");
        deleteFile(archive);
        return Result.failure();
    }

    private static void finish(SharedPreferences prefs, long dad2, String status) {
        if (!prefs.edit().putBoolean("finished_" + dad2, true).putString("status_" + dad2, status)
                .remove("file_" + dad2).commit()) {
            Globals.writeToMLOG("ERROR", TAG + "/finish", "Cannot persist status for dad2=" + dad2);
        }
    }

    @Override
    public void onStopped() {
        Call<Void> call = uploadCall;
        if (call != null) call.cancel();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static File directory(Context context, long dad2) {
        return new File(context.getFilesDir(), "wp_data_date_logs/" + dad2);
    }

    private static void deleteFile(File file) {
        if (file != null && file.exists() && !file.delete()) {
            Globals.writeToMLOG("INFO", TAG + "/cleanup", "Cannot delete " + file.getName());
        }
    }
}
