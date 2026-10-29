package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import ua.com.merchik.merchik.ServerExchange.ExchangeWork;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.SyncExchangeTrace;
import ua.com.merchik.merchik.ServerExchange.SyncResponsePolicy;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.data.Database.Room.LocationDevices;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.LocationDevicesResponse;
import ua.com.merchik.merchik.data.SynchronizationTimeTable;
import ua.com.merchik.merchik.data.synchronization.DownloadStatus;
import ua.com.merchik.merchik.data.synchronization.SynchronizationTimetableRepository;
import ua.com.merchik.merchik.data.synchronization.TableName;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public final class LocationDevicesExchange {
    private static final Map<Context, Completable> IN_FLIGHT = new HashMap<>();

    public static void download() {
        Context context = new Context();
        Completable pending;
        synchronized (IN_FLIGHT) {
            pending = IN_FLIGHT.get(context);
            if (pending == null) {
                pending = Completable.fromAction(() -> download(context))
                        .subscribeOn(Schedulers.io())
                        .doFinally(() -> { synchronized (IN_FLIGHT) { IN_FLIGHT.remove(context); } })
                        .cache();
                IN_FLIGHT.put(context, pending);
            }
        }
        pending.compose(ExchangeWork.track()).subscribe(() -> { }, error -> Globals.writeToMLOG("ERROR", "LocationDevicesExchange",
                "VPI unchanged: " + SyncResponsePolicy.shortError(error.toString())));
    }

    private static void download(Context context) {
        context.requireCurrent();
        SynchronizationTimetableRepository.ensureDefaults();
        AppDatabase db = context.db;
        SynchronizationTimeTable previous = db.synchronizationTimetableDao().getByTableName(TableName.WIFI_MAC_LOCATION);
        if (previous == null) throw new IllegalStateException("Missing Wi-Fi sync settings");
        Long started = TrustedTime.nowServerSecOrLocalSec();
        long from = LocationDevicesSyncPolicy.requestWatermark(previous.getLastDownloadTime(), started);
        JsonObject request = new JsonObject();
        request.addProperty("mod", "location");
        request.addProperty("act", "device_list");
        request.addProperty("dt_change_from", Long.toString(from));
        SyncExchangeTrace trace = new SyncExchangeTrace("location/device_list", from);
        try {
            LocationDevicesResponse response = RetrofitBuilder.getRetrofitInterface()
                    .GET_LOCATION_DEVICES_RX(RetrofitBuilder.contentType, request).blockingGet();
            trace.response(response == null ? null : response.state, response == null ? null : response.error,
                    response == null ? null : response.list);
            List<LocationDevices> rows = LocationDevicesSyncPolicy.rows(response);
            long watermark = LocationDevicesSyncPolicy.savedWatermark(from, started);
            // Data and cursor commit together. An account change or failed batch rolls both back.
            db.runInTransaction(() -> {
                context.requireCurrent();
                for (int offset = 0; offset < rows.size(); offset += 1000) {
                    context.requireCurrent();
                    db.locationDevicesDao().upsertAll(rows.subList(offset, Math.min(offset + 1000, rows.size())));
                }
                SynchronizationTimeTable current = db.synchronizationTimetableDao().getByTableName(TableName.WIFI_MAC_LOCATION);
                if (current == null) throw new IllegalStateException("Missing Wi-Fi sync settings");
                db.synchronizationTimetableDao().insert(new SynchronizationTimeTable(
                        current.getId(), current.getTableName(), current.getSyncPeriodSeconds(), watermark,
                        current.getLastUploadTime(), rows.size(), current.getUploadedItems(), current.getDescription(),
                        current.isUserGenerated(), DownloadStatus.SUCCESS, current.getLastUploadStatus()));
                context.requireCurrent();
            });
            trace.saved(watermark);
        } catch (RuntimeException error) {
            trace.failed(error);
            throw error;
        }
    }

    private static final class Context {
        final AppDatabase db = RoomManager.SQL_DB;
        final int user = Globals.getCurrentUserId();
        final String session = Globals.getCurrentSessionId();
        void requireCurrent() {
            if (db == null || db != RoomManager.SQL_DB || user <= 0 || user != Globals.getCurrentUserId()
                    || !Objects.equals(session, Globals.getCurrentSessionId())) {
                throw new IllegalStateException("Wi-Fi account/session/database changed");
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
