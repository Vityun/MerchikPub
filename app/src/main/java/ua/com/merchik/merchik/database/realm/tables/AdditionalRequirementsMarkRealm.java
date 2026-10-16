package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.AdditionalRequirementsMarkUploadPolicy;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsJOIN.AdditionalMaterialsJOINAdditionalMaterialsAddressSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;

public class AdditionalRequirementsMarkRealm {

    public static void setDataToDB(List<AdditionalRequirementsMarkDB> data) {
        INSTANCE.executeTransaction(realm -> {
            List<AdditionalRequirementsMarkDB> pending = realm.copyFromRealm(
                    realm.where(AdditionalRequirementsMarkDB.class).equalTo("uploadStatus", "0").findAll());
            realm.delete(AdditionalRequirementsMarkDB.class);
            realm.copyToRealmOrUpdate(data);
            // A download may finish while the user is editing or an upload is in flight.
            realm.copyToRealmOrUpdate(pending);
        });
    }
    public static void setDataToDBWithOutDel(List<AdditionalRequirementsMarkDB> data) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    public static void setNewMark(AdditionalRequirementsMarkDB data) {
        INSTANCE.beginTransaction();
        INSTANCE.insertOrUpdate(data);
        INSTANCE.commitTransaction();
    }


    public static AdditionalRequirementsMarkDB getMark(long dt, int id, String userId) {
        List<AdditionalRequirementsMarkDB> data = INSTANCE.copyFromRealm(INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .greaterThan("dt", dt)
                .equalTo("itemId", id)
                .equalTo("userId", userId)
                .findAll());
        return data.isEmpty() ? null : Collections.min(data, AdditionalRequirementsMarkUploadPolicy::compareNewestFirst);
    }

    public static List<AdditionalRequirementsMarkDB> getToUpload(int userId) {
        if (userId <= 0) return Collections.emptyList();
        RealmResults<AdditionalRequirementsMarkDB> pendingRows = INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .equalTo("userId", String.valueOf(userId)).equalTo("uploadStatus", "0").findAll();
        if (pendingRows.isEmpty()) return Collections.emptyList();
        List<AdditionalRequirementsMarkDB> result = new ArrayList<>();
        INSTANCE.executeTransaction(realm -> {
            List<AdditionalRequirementsMarkDB> pending = realm.copyFromRealm(pendingRows);
            List<AdditionalRequirementsMarkDB> latest = AdditionalRequirementsMarkUploadPolicy.latestPending(pending);
            Set<String> latestIds = new HashSet<>();
            for (AdditionalRequirementsMarkDB row : latest) latestIds.add(row.getId());
            for (AdditionalRequirementsMarkDB row : pending) {
                if (!AdditionalRequirementsMarkUploadPolicy.hasIdentity(row)) {
                    Globals.writeToMLOG("ERROR", "ARMarkUpload/queue", "Invalid pending row: localId=" + row.getId());
                } else if (!latestIds.contains(row.getId())) {
                    AdditionalRequirementsMarkDB current = realm.where(AdditionalRequirementsMarkDB.class)
                            .equalTo("id", row.getId()).findFirst();
                    if (AdditionalRequirementsMarkUploadPolicy.canAcknowledge(row, current)) {
                        current.setUploadStatus(AdditionalRequirementsMarkUploadPolicy.SUPERSEDED);
                        Globals.writeToMLOG("INFO", "ARMarkUpload/queue", "Superseded: localId=" + row.getId()
                                + ", userId=" + userId + ", type=" + row.getTp() + ", itemId=" + row.getItemId()
                                + ", score=" + row.getScore());
                    }
                }
            }
            result.addAll(AdditionalRequirementsMarkUploadPolicy.nextBatch(latest, userId));
        });
        return result;
    }

    public static List<String> acknowledgeUpload(List<AdditionalRequirementsMarkDB> sent, Set<Integer> accepted) {
        if (sent.isEmpty() || accepted.isEmpty()) return Collections.emptyList();
        List<String> cleared = new ArrayList<>();
        String uploadedAt = String.valueOf(System.currentTimeMillis());
        INSTANCE.executeTransaction(realm -> {
            for (AdditionalRequirementsMarkDB row : sent) {
                if (!accepted.contains(row.getItemId())) continue;
                AdditionalRequirementsMarkDB current = realm.where(AdditionalRequirementsMarkDB.class)
                        .equalTo("id", row.getId()).findFirst();
                if (AdditionalRequirementsMarkUploadPolicy.canAcknowledge(row, current)) {
                    current.setUploadStatus(uploadedAt);
                    cleared.add(row.getId());
                }
            }
        });
        return cleared;
    }

    /**
     * 09.08.2021
     * Получение оценок по данному списку доп. требований
     * <p>
     * tp = 1 - Доп. Требования / tp = 0 - Доп. Материалы
     */
    public static List<AdditionalRequirementsMarkDB> getAdditionalRequirementsMarks(long dateFrom, long dateTo, int userId, String tp, List<AdditionalRequirementsDB> data) {
        Integer[] ids = new Integer[data.size()];
        int i = 0;
        for (AdditionalRequirementsDB item : data) {
            ids[i++] = item.getId();
        }

        List<AdditionalRequirementsMarkDB> result = INSTANCE.copyFromRealm(INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .between("dt", dateFrom, dateTo)
                .equalTo("userId", String.valueOf(userId))
                .in("itemId", ids)
                .equalTo("tp", tp)
                .sort("dt", Sort.DESCENDING)
                .findAll());
        Collections.sort(result, AdditionalRequirementsMarkUploadPolicy::compareNewestFirst);
        return result;
    }

    public static RealmResults<AdditionalRequirementsMarkDB> getAdditionalRequirementsMarksAM(long dateFrom, long dateTo, int userId, String tp, List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> data) {
        Integer[] ids = new Integer[data.size()];
        int i = 0;
        for (AdditionalMaterialsJOINAdditionalMaterialsAddressSDB item : data) {
            ids[i++] = item.id;
        }

        return INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .between("dt", dateFrom, dateTo)
                .equalTo("userId", String.valueOf(userId))
                .in("itemId", ids)
                .equalTo("tp", tp)
                .sort("dt", Sort.DESCENDING)
                .findAll();
    }


}
