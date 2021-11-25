package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class AdditionalRequirementsMarkRealm {

    public static void setDataToDB(List<AdditionalRequirementsMarkDB> data) {
        INSTANCE.beginTransaction();
        INSTANCE.delete(AdditionalRequirementsMarkDB.class);
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    public static void setNewMark(AdditionalRequirementsMarkDB data){
        INSTANCE.beginTransaction();
        INSTANCE.insertOrUpdate(data);
        INSTANCE.commitTransaction();
    }



    public static AdditionalRequirementsMarkDB getMark(int id){
        AdditionalRequirementsMarkDB data = INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .equalTo("itemId", id)
                .equalTo("userId", String.valueOf(Globals.userId))
                .sort("dt", Sort.DESCENDING)
                .findFirst();
        return  data;
    }

    public static List<AdditionalRequirementsMarkDB> getToUpload(){
        return INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .isNotNull("uploadStatus")
                .equalTo("uploadStatus", "0")
                .findAll();
    }


    /**
     * 09.08.2021
     * Получение оценок по данному списку доп. требований
     * */
    public static RealmResults<AdditionalRequirementsMarkDB> getAdditionalRequirementsMarks(long dateFrom, long dateTo, int userId, List<AdditionalRequirementsDB> data){

        Integer[] ids = new Integer[data.size()];
        int i = 0;
        for (AdditionalRequirementsDB item : data){
            ids[i++] = item.getId();
        }


        return INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .between("dt", dateFrom, dateTo)
                .equalTo("userId", String.valueOf(userId))
                .in("itemId", ids)
                .sort("dt", Sort.DESCENDING)
                .findAll();
    }


}
