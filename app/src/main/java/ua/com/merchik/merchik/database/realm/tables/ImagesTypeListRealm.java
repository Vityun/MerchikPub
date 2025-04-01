package ua.com.merchik.merchik.database.realm.tables;

import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;
import ua.com.merchik.merchik.data.RealmModels.LogDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class ImagesTypeListRealm {

    public static ImagesTypeListDB getByID(int id){
        ImagesTypeListDB result =  INSTANCE.where(ImagesTypeListDB.class)
                .equalTo("id", id)
                .findFirst();
        if (result != null )
            result = INSTANCE.copyFromRealm(result);
        return result;

    }
}
