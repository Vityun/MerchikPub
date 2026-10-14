package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import com.google.gson.JsonElement;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ReportPrepareSync;
import ua.com.merchik.merchik.ServerExchange.ReportPrepareUploadPolicy;
import ua.com.merchik.merchik.data.UploadToServ.ReportPrepareServ;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class ReportPrepareRealm {

    public static ReportPrepareDB getByIdCopy(Long id) {
        if (id == null) return null;
        ReportPrepareDB row = INSTANCE.where(ReportPrepareDB.class).equalTo("iD", id).findFirst();
        return row == null ? null : INSTANCE.copyFromRealm(row);
    }

    // Setter-only callbacks edit the current row, then mirror those fields to the detached UI snapshot.
    public static boolean updateFields(ReportPrepareDB snapshot, Consumer<ReportPrepareDB> update) {
        return updateFields(snapshot, current -> true, update);
    }

    public static boolean updateFields(ReportPrepareDB snapshot, Predicate<ReportPrepareDB> canUpdate,
                                       Consumer<ReportPrepareDB> update) {
        boolean[] rejected = {false};
        boolean saved = updateFields(INSTANCE, snapshot, current -> {
            rejected[0] = !canUpdate.test(current);
            return !rejected[0];
        }, update);
        if (!saved && !rejected[0]) {
            Long id = snapshot != null && RealmObject.isValid(snapshot) ? snapshot.getID() : null;
            Globals.writeToMLOG("ERROR", "ReportPrepare/updateFields", "Row not found or invalid, id=" + id);
        }
        if (saved) ReportPrepareSync.localEdit(snapshot.getID());
        return saved;
    }

    static boolean updateFields(Realm realm, ReportPrepareDB snapshot, Consumer<ReportPrepareDB> update) {
        return updateFields(realm, snapshot, current -> true, update);
    }

    static boolean updateFields(Realm realm, ReportPrepareDB snapshot, Predicate<ReportPrepareDB> canUpdate,
                                Consumer<ReportPrepareDB> update) {
        if (snapshot == null || !RealmObject.isValid(snapshot) || snapshot.getID() == null) return false;
        Long id = snapshot.getID();
        boolean[] saved = {false};
        realm.executeTransaction(transactionRealm -> {
            ReportPrepareDB current = transactionRealm.where(ReportPrepareDB.class)
                    .equalTo("iD", id).findFirst();
            if (current == null || !canUpdate.test(current)) return;
            update.accept(current);
            saved[0] = true;
        });
        if (saved[0] && !RealmObject.isManaged(snapshot)) {
            update.accept(snapshot);
        }
        return saved[0];
    }

    public static void setAll(List<ReportPrepareDB> data) {
        mergeServerRows(data);
    }

    // Call on the Realm owner's thread. Pending local values always win over a download.
    public static boolean mergeServerRows(List<ReportPrepareDB> data) {
        return mergeServerRows(INSTANCE, data, ReportPrepareSync::deferredDownload,
                ReportPrepareRealm::logIdentityConflict);
    }

    static boolean mergeServerRows(Realm owner, List<ReportPrepareDB> data, Consumer<String> deferred,
                                   Consumer<String> log) {
        boolean[] complete = {true};
        owner.executeTransaction(realm -> {
            ReportPrepareIdentityIndex identities = new ReportPrepareIdentityIndex();
            Map<Long, String> pending = new HashMap<>();
            for (ReportPrepareDB row : realm.where(ReportPrepareDB.class).findAll()) {
                identities.remember(row.getID(), row.getCodeDad2(), row.getTovarId());
                if (row.getUploadStatus() == 1) pending.put(row.getID(), row.getCodeDad2());
            }
            List<ReportPrepareDB> incomingRows = new ArrayList<>();
            for (ReportPrepareDB incoming : data) {
                if (incoming == null || !RealmObject.isValid(incoming)) {
                    complete[0] = false;
                    log.accept("source=DOWNLOAD, reason=INVALID_ROW, action=SKIP");
                    continue;
                }
                String conflict = identities.conflict(incoming.getID(), incoming.getCodeDad2(), incoming.getTovarId());
                if (conflict != null) {
                    log.accept("source=DOWNLOAD, " + conflict);
                    // A rejected duplicate is handled; only malformed/deferred data holds back VPI.
                    if (!ReportPrepareIdentityIndex.hasIdentity(incoming.getID(), incoming.getCodeDad2(), incoming.getTovarId())) {
                        complete[0] = false;
                    }
                    continue;
                }
                if (pending.containsKey(incoming.getID())) {
                    complete[0] = false;
                    deferred.accept(pending.get(incoming.getID()));
                    continue;
                }
                ReportPrepareDB accepted = RealmObject.isManaged(incoming) ? realm.copyFromRealm(incoming) : incoming;
                accepted.setUploadStatus(0);
                incomingRows.add(accepted);
                identities.remember(accepted.getID(), accepted.getCodeDad2(), accepted.getTovarId());
            }
            realm.copyToRealmOrUpdate(incomingRows);
        });
        return complete[0];
    }

    private static void logIdentityConflict(String message) {
        Globals.writeToMLOG("WARNING", "ReportPrepareDuplicate", message);
    }

    public static ReportPrepareDB getOrCreateLocal(ReportPrepareDB defaults) {
        return getOrCreateLocal(INSTANCE, defaults, ReportPrepareRealm::logIdentityConflict);
    }

    static ReportPrepareDB getOrCreateLocal(Realm owner, ReportPrepareDB defaults, Consumer<String> log) {
        if (defaults == null || !RealmObject.isValid(defaults)
                || !ReportPrepareIdentityIndex.hasProduct(defaults.getCodeDad2(), defaults.getTovarId())) {
            throw new IllegalArgumentException("ReportPrepare requires codeDad2 and tovarId");
        }
        ReportPrepareDB[] result = {null};
        owner.executeTransaction(realm -> {
            ReportPrepareDB existing = realm.where(ReportPrepareDB.class)
                    .equalTo("codeDad2", defaults.getCodeDad2())
                    .equalTo("tovarId", defaults.getTovarId()).findFirst();
            if (existing != null) {
                log.accept("source=LOCAL_CREATE, dad2=" + existing.getCodeDad2()
                        + ", tovarId=" + existing.getTovarId() + ", keptId=" + existing.getID()
                        + ", action=REUSE_EXISTING");
                result[0] = realm.copyFromRealm(existing);
                return;
            }
            Number lastId = realm.where(ReportPrepareDB.class).max("iD");
            ReportPrepareDB candidate = RealmObject.isManaged(defaults) ? realm.copyFromRealm(defaults) : defaults;
            candidate.setID(lastId == null ? 1L : Math.addExact(lastId.longValue(), 1L));
            result[0] = realm.copyFromRealm(realm.copyToRealm(candidate));
        });
        return result[0];
    }

    // Legacy callers already own the transaction. New editors should use updateFields.
    public static void saveRowInTransaction(ReportPrepareDB row) {
        saveRowInTransaction(INSTANCE, row, ReportPrepareRealm::logIdentityConflict);
    }

    static void saveRowInTransaction(Realm realm, ReportPrepareDB row, Consumer<String> log) {
        if (!realm.isInTransaction()) throw new IllegalStateException("ReportPrepare write requires a transaction");
        if (row == null || !RealmObject.isValid(row)) {
            log.accept("source=SINGLE_ROW, reason=INVALID_ROW, action=SKIP");
            return;
        }
        ReportPrepareIdentityIndex identities = new ReportPrepareIdentityIndex();
        if (row.getID() != null) {
            ReportPrepareDB byId = realm.where(ReportPrepareDB.class).equalTo("iD", row.getID()).findFirst();
            if (byId != null) identities.remember(byId.getID(), byId.getCodeDad2(), byId.getTovarId());
        }
        if (ReportPrepareIdentityIndex.hasProduct(row.getCodeDad2(), row.getTovarId())) {
            ReportPrepareDB byProduct = realm.where(ReportPrepareDB.class)
                    .equalTo("codeDad2", row.getCodeDad2()).equalTo("tovarId", row.getTovarId()).findFirst();
            if (byProduct != null) identities.remember(byProduct.getID(), byProduct.getCodeDad2(), byProduct.getTovarId());
        }
        String conflict = identities.conflict(row.getID(), row.getCodeDad2(), row.getTovarId());
        if (conflict != null) log.accept("source=SINGLE_ROW, " + conflict);
        else realm.copyToRealmOrUpdate(row);
    }

    public static List<Long> acknowledgeUpload(Map<String, JsonElement> sent, Set<String> acceptedIds) {
        return acknowledgeUpload(INSTANCE, sent, acceptedIds);
    }

    static List<Long> acknowledgeUpload(Realm owner, Map<String, JsonElement> sent, Set<String> acceptedIds) {
        List<Long> cleared = new ArrayList<>();
        owner.executeTransaction(realm -> {
            for (String id : acceptedIds) {
                if (!sent.containsKey(id)) continue;
                ReportPrepareDB current = realm.where(ReportPrepareDB.class)
                        .equalTo("iD", Long.parseLong(id)).findFirst();
                if (ReportPrepareUploadPolicy.canAcknowledge(true, true, sent.get(id),
                        current == null ? null : uploadSnapshot(current))) {
                    current.setUploadStatus(0);
                    cleared.add(current.getID());
                }
            }
        });
        return cleared;
    }

    public static ReportPrepareServ uploadSnapshot(ReportPrepareDB row) {
        return new ReportPrepareServ(String.valueOf(row.getID()), row.getDtChange(),
                row.getDtReport(), row.getKli(), row.getTovarId(), row.getAddrId(),
                row.getPrice(), row.getFace(), row.getAmount(), row.getDtExpire(),
                row.getExpireLeft(), row.getNotes(), row.getUp(), row.getAkciya(),
                row.getAkciyaId(), row.getOborotvedNum(), row.getErrorId(),
                row.getErrorComment(), row.getCodeDad2(), String.valueOf(row.buyerOrderId),
                row.getPriceMin(), row.getPriceMax());
    }

    public static RealmResults<ReportPrepareDB> getAll() {
        return INSTANCE.where(ReportPrepareDB.class)
                .findAll();
    }

    public static RealmResults<ReportPrepareDB> getByIds(Long[] id) {
        return INSTANCE.where(ReportPrepareDB.class)
                .in("iD", id)
                .findAll();
    }

    public static RealmResults<ReportPrepareDB> getReportPrepareByDad2(long dad2) {
        return INSTANCE.where(ReportPrepareDB.class)
                .equalTo("codeDad2", String.valueOf(dad2))
                .findAll();
    }

    public static List<ReportPrepareDB> getReportPrepareByDad2_LIST(long dad2) {
        RealmResults<ReportPrepareDB> reportPrepareDBS = INSTANCE.where(ReportPrepareDB.class)
                .equalTo("codeDad2", String.valueOf(dad2))
                .findAll();

        List<ReportPrepareDB> result = new ArrayList<>();
        if (reportPrepareDBS != null) result = INSTANCE.copyFromRealm(reportPrepareDBS);

        return result;
    }

    public static List<ReportPrepareDB> getRPLastChange(
            String clientId,
            String addrId,
            long dtChange
    ) {
        long tenDaysMillis = 10L * 24 * 60 * 60;
        long dateFrom = dtChange - tenDaysMillis;

        RealmResults<ReportPrepareDB> reportPrepareDBS = INSTANCE.where(ReportPrepareDB.class)
                .equalTo("kli", clientId)
                .equalTo("addrId", addrId)
                .greaterThanOrEqualTo("dtChange", dateFrom)
                .lessThanOrEqualTo("dtChange", dtChange)
                .findAll();

        return INSTANCE.copyFromRealm(reportPrepareDBS);
    }
    public static List<ReportPrepareDB> getRPLastChangeOld(String clientId, String addrId, long dtChange) {
        RealmResults<ReportPrepareDB> reportPrepareDBS = INSTANCE.where(ReportPrepareDB.class)
                .equalTo("kli", clientId)
                .equalTo("addrId", addrId)
                .greaterThan("dtChange", dtChange)
                .findAll();

        List<ReportPrepareDB> res = new ArrayList<>();
        if (reportPrepareDBS != null) res = INSTANCE.copyFromRealm(reportPrepareDBS);
        return res;
    }

    public static ReportPrepareDB getReportPrepareByTov(String dad2, String tovarId) {
        ReportPrepareDB reportPrepareDB = INSTANCE.where(ReportPrepareDB.class)
                .equalTo("tovarId", tovarId)
                .and()
                .equalTo("codeDad2", dad2)
                .findFirst();
        if (reportPrepareDB != null) reportPrepareDB = INSTANCE.copyFromRealm(reportPrepareDB);
        return reportPrepareDB;
    }


    public static List<ReportPrepareDB> joinWithTovarsAndTovGroups(List<ReportPrepareDB> reportPrepareDBList) {
        List<String> tovarIdsList = new ArrayList<>();
        for (ReportPrepareDB item : reportPrepareDBList) {
            tovarIdsList.add(item.tovarId); // Собираем для JOIN-а идентификаторы Товаров

            item.colSKU = Integer.parseInt(item.face) > 0 ? 1 : 0;  // Расчитываем заодно и СКЮ
        }

        String[] tovIds = tovarIdsList.toArray(new String[0]);  // Получаю идентификаторы Товаров для запроса
        List<TovarDB> tovarDBList = RealmManager.INSTANCE.copyFromRealm(TovarRealm.getByIds(tovIds));

        List<Integer> tovarGrpIdsList = new ArrayList<>();
        for (ReportPrepareDB item : reportPrepareDBList) {
            for (TovarDB tovItem : tovarDBList) {
                int id1 = Integer.parseInt(item.tovarId);
                int id2 = Integer.parseInt(tovItem.getiD());
                if (id1 == id2) {
                    item.tovarDB = tovItem;
                    item.shelfSpaceLength = (tovItem.width * Integer.parseInt(item.face) / 1000);
                    tovarGrpIdsList.add(Integer.valueOf(tovItem.getGroupId()));
                    break;
                }
            }
        }

        List<TovarGroupSDB> tovarGroupSDBList = SQL_DB.tovarGroupDao().getAllByIds(tovarGrpIdsList);
        for (ReportPrepareDB item : reportPrepareDBList) {
            for (TovarGroupSDB tovGrpItem : tovarGroupSDBList) {
                if (item.tovarDB != null) {
                    int id1 = Integer.parseInt(item.tovarDB.getGroupId());
                    int id2 = tovGrpItem.id;
                    if (id1 == id2){
                        item.tovarGroupSDB = tovGrpItem;
                        break;
                    }
                }
            }
        }

        return reportPrepareDBList;
    }


    public static List<ReportPrepareDB> getReportPrepareUpMoreThanZeroByDad2List_LIST(List<Long> dad2List) {
        if (dad2List == null || dad2List.isEmpty()) {
            return new ArrayList<>();
        }

        String[] dad2Array = new String[dad2List.size()];
        for (int i = 0; i < dad2List.size(); i++) {
            dad2Array[i] = String.valueOf(dad2List.get(i));
        }

        RealmResults<ReportPrepareDB> reportPrepareDBS = INSTANCE.where(ReportPrepareDB.class)
                .in("codeDad2", dad2Array)
                .isNotNull("up")
                .notEqualTo("up", "")
                .notEqualTo("up", "0")
                .findAll();

        List<ReportPrepareDB> result = new ArrayList<>();
        if (reportPrepareDBS != null) {
            result = INSTANCE.copyFromRealm(reportPrepareDBS);
        }

        return result;
    }

}
