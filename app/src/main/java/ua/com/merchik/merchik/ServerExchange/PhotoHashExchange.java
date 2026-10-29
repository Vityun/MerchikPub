package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ModImagesView;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ModImagesViewList;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/** Both legacy cron entry points share one in-flight hash reconciliation per session. */
public final class PhotoHashExchange {
    private static final Set<Context> RUNNING = new HashSet<>();
    private PhotoHashExchange() { }

    public static void refresh() {
        Context context = new Context();
        synchronized (RUNNING) { if (!RUNNING.add(context)) return; }
        Single.fromCallable(() -> refreshSync(context)).subscribeOn(Schedulers.io())
                .doFinally(() -> { synchronized (RUNNING) { RUNNING.remove(context); } })
                .compose(ExchangeWork.track())
                .subscribe(count -> log("INFO", "resolved=" + count), error -> log("ERROR", error.toString()));
    }

    private static int refreshSync(Context context) throws Exception {
        context.check();
        List<StackPhotoDB> snapshots;
        try (Realm realm = Realm.getDefaultInstance()) {
            snapshots = realm.copyFromRealm(realm.where(StackPhotoDB.class).equalTo("get_on_server", 0)
                    .isNotNull("photo_hash").isNotNull("time_event")
                    .isNotNull("client_id").isNotNull("addr_id").findAll());
        }
        TreeSet<String> hashes = new TreeSet<>();
        for (StackPhotoDB photo : snapshots) {
            if (!photo.photo_hash.trim().isEmpty()) hashes.add(photo.photo_hash);
        }
        List<String> requested = new ArrayList<>(hashes);
        int[] resolved = {0};
        boolean[] comments = {false};
        for (int offset = 0; offset < requested.size(); offset += 200) {
            context.check();
            List<String> chunk = requested.subList(offset, Math.min(offset + 200, requested.size()));
            Response<ModImagesView> response = RetrofitBuilder.getRetrofitInterface().PHOTO_METADATA_BY_HASH(
                    "images_view", "list_image", "1", "0", chunk).execute();
            if (!response.isSuccessful()) {
                if (response.errorBody() != null) response.errorBody().close();
                throw new IllegalStateException("Photo hash HTTP " + response.code());
            }
            List<ModImagesViewList> rows = PhotoMetadataPolicy.rows(response.body(), new JsonObject());
            try (Realm realm = Realm.getDefaultInstance()) {
                realm.executeTransaction(transaction -> {
                    context.check();
                    for (ModImagesViewList row : rows) {
                        if (!chunk.contains(row.imgHash)) throw new IllegalStateException("Unexpected photo hash");
                        List<StackPhotoDB> matches = new ArrayList<>();
                        for (StackPhotoDB snapshot : snapshots) {
                            if (PhotoMetadataPolicy.matchesHashResponse(snapshot, row)) matches.add(snapshot);
                        }
                        if (matches.size() != 1) {
                            log("WARN", "hash requires unambiguous visit/product match: serverId=" + row.getID());
                            continue;
                        }
                        StackPhotoDB snapshot = matches.get(0);
                        int serverMatches = 0;
                        for (ModImagesViewList candidate : rows) {
                            if (PhotoMetadataPolicy.matchesHashResponse(snapshot, candidate)) serverMatches++;
                        }
                        if (serverMatches != 1) {
                            log("WARN", "multiple server photos match localId=" + snapshot.getId());
                            continue;
                        }
                        StackPhotoDB current = transaction.where(StackPhotoDB.class)
                                .equalTo("id", snapshot.getId()).findFirst();
                        StackPhotoDB assigned = transaction.where(StackPhotoDB.class)
                                .equalTo("photoServerId", row.getID()).findFirst();
                        if (assigned != null && assigned.getId() != snapshot.getId()) {
                            log("WARN", "server ID already belongs to localId=" + assigned.getId());
                            continue;
                        }
                        long uploaded = row.getDtUpload() == null || row.getDtUpload().isEmpty()
                                ? System.currentTimeMillis() : Math.multiplyExact(Long.parseLong(row.getDtUpload()), 1000L);
                        if (StackPhotoSyncPolicy.receivedServerDetails(current, snapshot, row.getID(), row.getPhotoUrl(), uploaded)) {
                            resolved[0]++;
                            comments[0] |= current.isCommentUpload();
                        }
                    }
                    context.check();
                });
            }
        }
        if (comments[0]) PhotoCommentSync.changed();
        return resolved[0];
    }

    private static void log(String level, String text) { Globals.writeToMLOG(level, "PhotoHashExchange", text); }

    private static final class Context {
        final AppDatabase db = RoomManager.SQL_DB;
        final int user = Globals.getCurrentUserId();
        final String session = Globals.getCurrentSessionId();
        void check() {
            if (db == null || db != RoomManager.SQL_DB || user != Globals.getCurrentUserId()
                    || !Objects.equals(session, Globals.getCurrentSessionId())) {
                throw new IllegalStateException("Photo hash session changed");
            }
        }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Context)) return false;
            Context that = (Context) other;
            return db == that.db && user == that.user && Objects.equals(session, that.session);
        }
        @Override public int hashCode() { return Objects.hash(System.identityHashCode(db), user, session); }
    }
}
