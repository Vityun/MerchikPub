package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsJOIN.AdditionalMaterialsJOINAdditionalMaterialsAddressSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class AdditionalRequirementsMarkRealm {

    public static void setDataToDB(List<AdditionalRequirementsMarkDB> data) {
        INSTANCE.beginTransaction();
//        INSTANCE.delete(AdditionalRequirementsMarkDB.class);
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    public static void setNewMark(AdditionalRequirementsMarkDB data) {
        INSTANCE.beginTransaction();
        INSTANCE.insertOrUpdate(data);
        INSTANCE.commitTransaction();
    }


    public static AdditionalRequirementsMarkDB getMark(long dt, int id, String userId) {
//        AdditionalRequirementsMarkDB res = null;
//        RealmQuery query = INSTANCE.where(AdditionalRequirementsMarkDB.class);
//
//        if (dt == 0) {
//            query.greaterThan("dt", dt);
//        }
//
//        query.equalTo("itemId", id);
//        query.equalTo("userId", userId);
//        query.sort("dt", Sort.DESCENDING);
//        res = (AdditionalRequirementsMarkDB) query.findFirst();
//
//        return res;
        AdditionalRequirementsMarkDB data = INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .greaterThan("dt", dt)
                .equalTo("itemId", id)
                .equalTo("userId", userId)
                .sort("dt", Sort.DESCENDING)
                .findFirst();
        return  data;
    }

    public static List<AdditionalRequirementsMarkDB> getToUpload() {
        return INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .isNotNull("uploadStatus")
                .equalTo("uploadStatus", "0")
                .findAll();
    }


    /**
     * 09.08.2021
     * Получение оценок по данному списку доп. требований
     * <p>
     * tp = 1 - Доп. Требования / tp = 0 - Доп. Материалы
     */
    public static RealmResults<AdditionalRequirementsMarkDB> getAdditionalRequirementsMarks(long dateFrom, long dateTo, int userId, String tp, List<AdditionalRequirementsDB> data) {
        Integer[] ids = new Integer[data.size()];
        int i = 0;
        for (AdditionalRequirementsDB item : data) {
            ids[i++] = item.getId();
        }

        return INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .between("dt", dateFrom, dateTo)
                .equalTo("userId", String.valueOf(userId))
                .in("itemId", ids)
                .equalTo("tp", tp)
                .sort("dt", Sort.DESCENDING)
                .findAll();
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
