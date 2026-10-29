package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import ua.com.merchik.merchik.ServerExchange.ExchangeWork;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.SyncExchangeTrace;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.ArticleSDB;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsGroupsSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammAddressSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammGroupSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammImagesSDB;
import ua.com.merchik.merchik.data.Database.Room.StandartSDB;
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SamplePhotoResponse;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHints;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.QuestionAnswerDB;
import ua.com.merchik.merchik.data.RealmModels.ArticleDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.QuestionAnswerResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.AdditionalMaterialsGroupsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ArticleResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.StandartResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammAddressResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammGroupResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammImagesResponse;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

import static ua.com.merchik.merchik.ServerExchange.TablesExchange.ReferenceSyncPolicy.Table.*;
import static ua.com.merchik.merchik.ServerExchange.TablesExchange.ReferenceSyncPolicy.rows;

/** Shared download lifecycle; existing exchange entry points delegate here. */
public final class ReferenceDataExchange {
    private static final Map<RequestContext, Single<?>> IN_FLIGHT = new HashMap<>();

    private ReferenceDataExchange() { }

    public static void samplePhotos(Clicks.clickObjectAndStatus callback) {
        start(new Dataset<SamplePhotoSDB>(SAMPLE_PHOTOS, false) {
            List<SamplePhotoSDB> load(JsonObject request, SyncExchangeTrace trace) throws Exception {
                SamplePhotoResponse body = body(RetrofitBuilder.getRetrofitInterface()
                        .GET_SAMPLE_PHOTO(RetrofitBuilder.contentType, request));
                trace.response(body.state, null, body.list);
                return rows(body.state, null, body.list, row -> row.id);
            }
            long count(AppDatabase db, Realm realm) { return db.samplePhotoDao().count(); }
            long periodSeconds() { return 7L * 24 * 60 * 60; }
            void saveRoom(AppDatabase db, List<SamplePhotoSDB> rows) { db.samplePhotoDao().upsertAll(rows); }
        }, callback(callback));
    }

    public static void planogramAddresses(Clicks.clickObjectAndStatus callback) {
        start(new Dataset<PlanogrammAddressSDB>(PLANOGRAM_ADDRESSES, false) {
            List<PlanogrammAddressSDB> load(JsonObject request, SyncExchangeTrace trace) throws Exception {
                PlanogrammAddressResponse body = body(RetrofitBuilder.getRetrofitInterface()
                        .Planogramm_ADDRESS_RESPONSE(RetrofitBuilder.contentType, request));
                trace.response(body.state, body.error, body.list);
                return rows(body.state, body.error, body.list, row -> row.id);
            }
            long count(AppDatabase db, Realm realm) { return db.planogrammAddressDao().count(); }
            void saveRoom(AppDatabase db, List<PlanogrammAddressSDB> rows) { db.planogrammAddressDao().upsertAll(rows); }
        }, callback(callback));
    }

    public static void planogramGroups(Clicks.clickObjectAndStatus callback) {
        start(new Dataset<PlanogrammGroupSDB>(PLANOGRAM_GROUPS, false) {
            List<PlanogrammGroupSDB> load(JsonObject request, SyncExchangeTrace trace) throws Exception {
                PlanogrammGroupResponse body = body(RetrofitBuilder.getRetrofitInterface()
                        .Planogramm_GROUP_RESPONSE(RetrofitBuilder.contentType, request));
                trace.response(body.state, body.error, body.list);
                return rows(body.state, body.error, body.list, row -> row.id);
            }
            long count(AppDatabase db, Realm realm) { return db.planogrammGroupDao().count(); }
            void saveRoom(AppDatabase db, List<PlanogrammGroupSDB> rows) { db.planogrammGroupDao().upsertAll(rows); }
        }, callback(callback));
    }

    public static void planogramImages(Clicks.clickObjectAndStatus callback) {
        start(new Dataset<PlanogrammImagesSDB>(PLANOGRAM_IMAGES, false) {
            List<PlanogrammImagesSDB> load(JsonObject request, SyncExchangeTrace trace) throws Exception {
                PlanogrammImagesResponse body = body(RetrofitBuilder.getRetrofitInterface()
                        .Planogramm_IMAGES_RESPONSE(RetrofitBuilder.contentType, request));
                trace.response(body.state, body.error, body.list);
                return rows(body.state, body.error, body.list, row -> row.id);
            }
            long count(AppDatabase db, Realm realm) { return db.planogrammImagesDao().count(); }
            void saveRoom(AppDatabase db, List<PlanogrammImagesSDB> rows) { db.planogrammImagesDao().upsertAll(rows); }
        }, callback(callback));
    }

    public static void standards(ExchangeInterface.ExchangeResponseInterface callback) {
        start(new Dataset<StandartSDB>(STANDART, false) {
            List<StandartSDB> load(JsonObject request, SyncExchangeTrace trace) throws Exception {
                StandartResponse body = body(RetrofitBuilder.getRetrofitInterface()
                        .GET_TABLE_STANDART(RetrofitBuilder.contentType, request));
                trace.response(body.state, body.error, body.list);
                return rows(body.state, body.error, body.list, row -> row.id);
            }
            long count(AppDatabase db, Realm realm) { return db.standartDao().count(); }
            void saveRoom(AppDatabase db, List<StandartSDB> rows) { db.standartDao().insertAll(rows); }
        }, callback == null ? null : new Result<StandartSDB>() {
            public void success(List<StandartSDB> rows) { callback.onSuccess(rows); }
            public void failure(String error) { callback.onFailure(error); }
        });
    }

    public static void articles() {
        start(new Dataset<ArticleSDB>(ARTICLES, true) {
            List<ArticleSDB> load(JsonObject request, SyncExchangeTrace trace) throws Exception {
                ArticleResponse body = body(RetrofitBuilder.getRetrofitInterface()
                        .ARTICLE_DOWNLOAD(RetrofitBuilder.contentType, request));
                trace.response(body.state, body.error, body.list);
                return rows(body.state, body.error, body.list, row -> row.id);
            }
            long count(AppDatabase db, Realm realm) {
                // Legacy stores may contain different historical rows. Track both without
                // requiring their contents to become identical after an incremental upsert.
                return db.articleDao().count() + realm.where(ArticleDB.class).count();
            }
            void saveRoom(AppDatabase db, List<ArticleSDB> rows) { db.articleDao().insertAll(rows); }
            void saveRealm(Realm realm, List<ArticleSDB> rows) {
                List<ArticleDB> copies = new ArrayList<>(rows.size());
                for (ArticleSDB row : rows) copies.add(ArticleDB.fromRoom(row));
                realm.copyToRealmOrUpdate(copies);
            }
        }, null);
    }

    public static void lessons() {
        start(new Dataset<SiteHintsDB>(LESSONS, true) {
            List<SiteHintsDB> load(JsonObject request, SyncExchangeTrace trace) throws Exception {
                SiteHints body = body(RetrofitBuilder.getRetrofitInterface()
                        .DOWNLOAD_VIDEO_LESSONS(RetrofitBuilder.contentType, request));
                trace.response(body.getState(), body.error, body.getList());
                return rows(body.getState(), body.error, body.getList(), SiteHintsDB::getID);
            }
            long count(AppDatabase db, Realm realm) { return realm.where(SiteHintsDB.class).count(); }
            void saveRoom(AppDatabase db, List<SiteHintsDB> rows) { }
            void saveRealm(Realm realm, List<SiteHintsDB> rows) { realm.copyToRealmOrUpdate(rows); }
        }, null);
    }

    public static void questions() {
        start(new Dataset<QuestionAnswerDB>(QUESTIONS, false) {
            List<QuestionAnswerDB> load(JsonObject request, SyncExchangeTrace trace) throws Exception {
                QuestionAnswerResponse body = body(RetrofitBuilder.getRetrofitInterface()
                        .GET_QUESTION_LIST(RetrofitBuilder.contentType, request));
                trace.response(body.getState(), body.getError(), body.getList());
                return rows(body.getState(), body.getError(), body.getList(), QuestionAnswerDB::getId);
            }
            long count(AppDatabase db, Realm realm) { return db.questionAnswerDao().count(); }
            void saveRoom(AppDatabase db, List<QuestionAnswerDB> rows) { db.questionAnswerDao().insertAll(rows); }
        }, null);
    }

    public static void additionalMaterialGroups() {
        start(new Dataset<AdditionalMaterialsGroupsSDB>(ADDITIONAL_MATERIAL_GROUPS, false) {
            List<AdditionalMaterialsGroupsSDB> load(JsonObject request, SyncExchangeTrace trace) throws Exception {
                AdditionalMaterialsGroupsResponse body = body(RetrofitBuilder.getRetrofitInterface()
                        .AdditionalMaterialsGroupsResponse_JSON_UPLOAD(RetrofitBuilder.contentType, request));
                trace.response(body.state, body.error, body.list);
                return rows(body.state, body.error, body.list, row -> row.id);
            }
            long count(AppDatabase db, Realm realm) { return db.additionalMaterialsGroupsDao().count(); }
            void saveRoom(AppDatabase db, List<AdditionalMaterialsGroupsSDB> rows) {
                db.additionalMaterialsGroupsDao().insertAll(rows);
            }
        }, null);
    }

    @SuppressWarnings("unchecked")
    private static <T> void start(Dataset<T> dataset, Result<T> callback) {
        RequestContext context = new RequestContext(dataset.table);
        Single<List<T>> request;
        synchronized (IN_FLIGHT) {
            request = (Single<List<T>>) IN_FLIGHT.get(context);
            if (request == null) {
                request = Single.fromCallable(() -> download(context, dataset))
                        .subscribeOn(Schedulers.io())
                        .doOnError(error -> log("ERROR", context, "VPI unchanged: " + error))
                        .doFinally(() -> {
                            synchronized (IN_FLIGHT) { IN_FLIGHT.remove(context); }
                        }).cache();
                IN_FLIGHT.put(context, request);
            } else {
                log("INFO", context, "join in-flight download");
            }
        }
        request.observeOn(AndroidSchedulers.mainThread()).compose(ExchangeWork.track()).subscribe(
                rows -> notify(context, callback, rows, null),
                error -> notify(context, callback, null, error));
    }

    private static <T> List<T> download(RequestContext context, Dataset<T> dataset) throws Exception {
        context.requireCurrent();
        AppDatabase db = context.db;
        String scope = FilteredSyncPolicy.accountScope(context.userId, "all");
        String resource = dataset.table.resource();
        SynchronizationCursorSDB cursor = db.synchronizationCursorDao().get(resource, scope);
        SyncExchangeTrace trace = null;
        try (Realm realm = dataset.usesRealm ? Realm.getDefaultInstance() : null) {
            Long started = TrustedTime.nowServerSecOrLocalSec();
            long count = dataset.count(db, realm);
            if (dataset.periodSeconds() > 0 && cursor != null && cursor.itemCount == count
                    && cursor.vpi > 0 && started != null && started >= cursor.vpi
                    && started - cursor.vpi < dataset.periodSeconds()) {
                return new ArrayList<>();
            }
            long from = FilteredSyncPolicy.requestWatermark(cursor, started,
                    cursor != null && count >= 0 && cursor.itemCount == count);
            JsonObject request = ReferenceSyncPolicy.request(dataset.table, from);
            log("INFO", context, "start: " + request);
            trace = new SyncExchangeTrace(resource, from);
            List<T> rows = dataset.load(request, trace);
            context.requireCurrent();
            // Room and Realm cannot share a transaction. Commit Realm first; on a Room failure
            // the old cursor remains, so the next exchange replays the same server changes.
            if (realm != null) {
                realm.executeTransaction(transaction -> {
                    context.requireCurrent();
                    dataset.saveRealm(transaction, rows);
                    context.requireCurrent();
                });
            }
            SynchronizationCursorSDB saved = FilteredSyncPolicy.savedCursor(resource, scope, from, started, 0);
            db.runInTransaction(() -> {
                context.requireCurrent();
                dataset.saveRoom(db, rows);
                saved.itemCount = dataset.count(db, realm);
                db.synchronizationCursorDao().save(saved);
                context.requireCurrent();
            });
            log("INFO", context, "saved: rows=" + rows.size() + ", vpi=" + saved.vpi);
            trace.saved(saved.vpi);
            return rows;
        } catch (Exception error) {
            if (trace != null) trace.failed(error);
            throw error;
        }
    }

    private static <B> B body(Call<B> call) throws Exception {
        Response<B> response = call.execute();
        if (!response.isSuccessful() || response.body() == null) {
            if (response.errorBody() != null) response.errorBody().close();
            throw new IllegalStateException("HTTP " + response.code() + ", emptyBody=" + (response.body() == null));
        }
        return response.body();
    }

    private interface Result<T> {
        void success(List<T> rows);
        void failure(String error);
    }

    private static <T> Result<T> callback(Clicks.clickObjectAndStatus callback) {
        return callback == null ? null : new Result<T>() {
            public void success(List<T> rows) { callback.onSuccess(rows); }
            public void failure(String error) { callback.onFailure(error); }
        };
    }

    private static <T> void notify(RequestContext context, Result<T> callback, List<T> rows, Throwable error) {
        if (callback == null || !context.isCurrent()) return;
        try {
            if (error == null) callback.success(rows);
            else callback.failure(error.toString());
        } catch (Exception failure) {
            log("ERROR", context, "callback failed: " + failure);
        }
    }

    private static void log(String level, RequestContext context, String text) {
        Globals.writeToMLOG(level, "ReferenceDataExchange/" + context.table.resource(), text);
    }

    private abstract static class Dataset<T> {
        final ReferenceSyncPolicy.Table table;
        final boolean usesRealm;

        Dataset(ReferenceSyncPolicy.Table table, boolean usesRealm) {
            this.table = table;
            this.usesRealm = usesRealm;
        }
        abstract List<T> load(JsonObject request, SyncExchangeTrace trace) throws Exception;
        abstract long count(AppDatabase db, Realm realm);
        abstract void saveRoom(AppDatabase db, List<T> rows);
        long periodSeconds() { return 0; }
        void saveRealm(Realm realm, List<T> rows) { }
    }

    private static final class RequestContext {
        final AppDatabase db = RoomManager.SQL_DB;
        final int userId = Globals.getCurrentUserId();
        final String sessionId = Globals.getCurrentSessionId();
        final ReferenceSyncPolicy.Table table;

        RequestContext(ReferenceSyncPolicy.Table table) { this.table = table; }

        boolean isCurrent() {
            return db != null && db == RoomManager.SQL_DB && userId == Globals.getCurrentUserId()
                    && Objects.equals(sessionId, Globals.getCurrentSessionId());
        }

        void requireCurrent() {
            if (!isCurrent()) throw new IllegalStateException("Account/session/database changed");
        }

        @Override public boolean equals(Object other) {
            if (!(other instanceof RequestContext)) return false;
            RequestContext that = (RequestContext) other;
            return db == that.db && userId == that.userId && table == that.table
                    && Objects.equals(sessionId, that.sessionId);
        }

        @Override public int hashCode() {
            return Objects.hash(System.identityHashCode(db), userId, table, sessionId);
        }
    }
}
