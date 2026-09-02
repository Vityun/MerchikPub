package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.DynamicPhotoSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest;

public class DynamicPhotoStackPhotoExchange {

    private static final String TAG = "DynamicPhotoStackPhotoExchange";
    private static final int REQUEST_CHUNK_SIZE = 200;
    private static final int IDS_PREVIEW_LIMIT = 25;
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    public boolean downloadMissingFromDynamicPhotoTableAsync() {
        if (SQL_DB == null) {
            logError("start", "skip: Room database is not initialized");
            return false;
        }

        if (!RUNNING.compareAndSet(false, true)) {
            logInfo("start", "skip: previous dynamic_photo StackPhoto download is still running");
            return false;
        }

        Single.fromCallable(this::collectMissingDynamicPhotoStackPhotoIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    logInfo("check", result.toLogMessage(previewIds(result.missingPhotoIds)));

                    if (result.missingPhotoIds.isEmpty()) {
                        RUNNING.set(false);
                        return;
                    }

                    downloadMissingPhotoIds(result.missingPhotoIds);
                }, throwable -> {
                    logError("check", "error=" + throwable);
                    RUNNING.set(false);
                });

        return true;
    }

    private DynamicPhotoStackPhotoCheckResult collectMissingDynamicPhotoStackPhotoIds() {
        List<DynamicPhotoSDB> rows = SQL_DB.dynamicPhotoDao().getAll();
        LinkedHashSet<String> uniquePhotoIds = new LinkedHashSet<>();

        if (rows != null) {
            for (DynamicPhotoSDB row : rows) {
                String photoId = normalizeDynamicPhotoId(row != null ? row.photoId : null);
                if (photoId != null) {
                    uniquePhotoIds.add(photoId);
                }
            }
        }

        List<String> missingPhotoIds = new ArrayList<>();
        int existingCount = 0;

        if (uniquePhotoIds.isEmpty()) {
            return new DynamicPhotoStackPhotoCheckResult(
                    rows != null ? rows.size() : 0,
                    0,
                    0,
                    missingPhotoIds
            );
        }

        Set<String> existingPhotoIds = new HashSet<>();
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<StackPhotoDB> existingPhotos = realm.where(StackPhotoDB.class)
                    .in("photoServerId", uniquePhotoIds.toArray(new String[0]))
                    .findAll();

            for (StackPhotoDB photo : existingPhotos) {
                if (photo.photoServerId != null) {
                    existingPhotoIds.add(photo.photoServerId);
                }
            }
        } finally {
            realm.close();
        }

        for (String photoId : uniquePhotoIds) {
            if (existingPhotoIds.contains(photoId)) {
                existingCount++;
            } else {
                missingPhotoIds.add(photoId);
            }
        }

        return new DynamicPhotoStackPhotoCheckResult(
                rows != null ? rows.size() : 0,
                uniquePhotoIds.size(),
                existingCount,
                missingPhotoIds
        );
    }

    private void downloadMissingPhotoIds(List<String> missingPhotoIds) {
        List<List<String>> chunks = splitIntoChunks(missingPhotoIds);
        logInfo(
                "request.start",
                "missingCount=" + missingPhotoIds.size()
                        + ", chunkCount=" + chunks.size()
                        + ", missingIds=" + previewIds(missingPhotoIds)
        );

        downloadChunk(chunks, 0, missingPhotoIds.size());
    }

    private void downloadChunk(List<List<String>> chunks, int chunkIndex, int totalMissingCount) {
        if (chunkIndex >= chunks.size()) {
            logInfo(
                    "request.complete",
                    "totalMissingCount=" + totalMissingCount + ", chunkCount=" + chunks.size()
            );
            RUNNING.set(false);
            return;
        }

        List<String> chunk = chunks.get(chunkIndex);
        PhotoTableRequest request = new PhotoTableRequest();
        request.mod = "images_view";
        request.act = "list_image";
        request.nolimit = "1";
        request.id_list = String.join(",", chunk);

        logInfo(
                "request.chunk",
                "chunk=" + (chunkIndex + 1)
                        + "/" + chunks.size()
                        + ", count=" + chunk.size()
                        + ", ids=" + previewIds(chunk)
        );

        try {
            new PhotoDownload().getPhotoInfoAndSaveItToDB(
                    request,
                    new Clicks.clickObjectAndStatus<StackPhotoDB>() {
                        @Override
                        public void onSuccess(StackPhotoDB data) {
                            String photoServerId = data != null ? data.photoServerId : null;
                            logInfo(
                                    "response.chunk",
                                    "chunk=" + (chunkIndex + 1)
                                            + "/" + chunks.size()
                                            + ", status=success"
                                            + ", firstSavedPhotoServerId=" + photoServerId
                            );
                            downloadChunk(chunks, chunkIndex + 1, totalMissingCount);
                        }

                        @Override
                        public void onFailure(String error) {
                            logError(
                                    "response.chunk",
                                    "chunk=" + (chunkIndex + 1)
                                            + "/" + chunks.size()
                                            + ", status=failure"
                                            + ", ids=" + previewIds(chunk)
                                            + ", error=" + error
                            );
                            downloadChunk(chunks, chunkIndex + 1, totalMissingCount);
                        }
                    }
            );
        } catch (Exception e) {
            logError(
                    "request.chunk",
                    "chunk=" + (chunkIndex + 1)
                            + "/" + chunks.size()
                            + ", ids=" + previewIds(chunk)
                            + ", error=" + e
            );
            downloadChunk(chunks, chunkIndex + 1, totalMissingCount);
        }
    }

    private List<List<String>> splitIntoChunks(List<String> source) {
        List<List<String>> chunks = new ArrayList<>();
        for (int start = 0; start < source.size(); start += REQUEST_CHUNK_SIZE) {
            int end = Math.min(start + REQUEST_CHUNK_SIZE, source.size());
            chunks.add(new ArrayList<>(source.subList(start, end)));
        }
        return chunks;
    }

    private String normalizeDynamicPhotoId(String rawPhotoId) {
        if (rawPhotoId == null) {
            return null;
        }

        String photoId = rawPhotoId.trim();
        if (photoId.isEmpty() || photoId.equals("0")) {
            return null;
        }

        return photoId;
    }

    private String previewIds(List<String> ids) {
        int limit = Math.min(ids.size(), IDS_PREVIEW_LIMIT);
        List<String> preview = ids.subList(0, limit);
        String suffix = ids.size() > limit ? "... +" + (ids.size() - limit) : "";
        return preview + suffix;
    }

    private void logInfo(String place, String message) {
        Log.e(TAG, place + ": " + message);
        Globals.writeToMLOG("INFO", TAG + "/" + place, message);
    }

    private void logError(String place, String message) {
        Log.e(TAG, place + ": " + message);
        Globals.writeToMLOG("ERROR", TAG + "/" + place, message);
    }

    private static class DynamicPhotoStackPhotoCheckResult {
        final int dynamicPhotoRowsCount;
        final int uniquePhotoIdsCount;
        final int existingPhotoIdsCount;
        final List<String> missingPhotoIds;

        DynamicPhotoStackPhotoCheckResult(
                int dynamicPhotoRowsCount,
                int uniquePhotoIdsCount,
                int existingPhotoIdsCount,
                List<String> missingPhotoIds
        ) {
            this.dynamicPhotoRowsCount = dynamicPhotoRowsCount;
            this.uniquePhotoIdsCount = uniquePhotoIdsCount;
            this.existingPhotoIdsCount = existingPhotoIdsCount;
            this.missingPhotoIds = missingPhotoIds;
        }

        String toLogMessage(String missingIdsPreview) {
            return "dynamicPhotoRows=" + dynamicPhotoRowsCount
                    + ", uniquePhotoIds=" + uniquePhotoIdsCount
                    + ", existingStackPhotos=" + existingPhotoIdsCount
                    + ", missingStackPhotos=" + missingPhotoIds.size()
                    + ", missingIds=" + missingIdsPreview;
        }
    }
}
