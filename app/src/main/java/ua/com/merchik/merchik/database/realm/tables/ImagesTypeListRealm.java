package ua.com.merchik.merchik.database.realm.tables;

import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class ImagesTypeListRealm {

    public static ImagesTypeListDB getByID(int id){
        return INSTANCE.where(ImagesTypeListDB.class)
                .equalTo("id", id)
                .findFirst();
    }
}
