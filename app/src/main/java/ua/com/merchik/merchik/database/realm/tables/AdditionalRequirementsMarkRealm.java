package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsJOIN.AdditionalMaterialsJOINAdditionalMaterialsAddressSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;

public class AdditionalRequirementsMarkRealm {

    public static void setDataToDB(List<AdditionalRequirementsMarkDB> data) {
        INSTANCE.beginTransaction();
        INSTANCE.delete(AdditionalRequirementsMarkDB.class);
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    public static void setNewMark(AdditionalRequirementsMarkDB data) {
        INSTANCE.beginTransaction();
        INSTANCE.insertOrUpdate(data);
        INSTANCE.commitTransaction();
    }


    public static AdditionalRequirementsMarkDB getMark(long dt, int id, String userId) {
        AdditionalRequirementsMarkDB data = INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .greaterThan("dt", dt)
                .equalTo("itemId", id)
                .equalTo("userId", userId)
                .sort("dt", Sort.DESCENDING)
                .findFirst();
        if (data != null) data = INSTANCE.copyFromRealm(data);
        return data;
    }

    public static List<AdditionalRequirementsMarkDB> getToUpload() {
        List<AdditionalRequirementsMarkDB> res = INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .isNotNull("uploadStatus")
                .equalTo("uploadStatus", "0")
                .findAll();
        if (res != null) res = INSTANCE.copyFromRealm(res);
        return res;
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

        return INSTANCE.copyFromRealm(INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .between("dt", dateFrom, dateTo)
                .equalTo("userId", String.valueOf(userId))
                .in("itemId", ids)
                .equalTo("tp", tp)
                .sort("dt", Sort.DESCENDING)
                .findAll());
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
