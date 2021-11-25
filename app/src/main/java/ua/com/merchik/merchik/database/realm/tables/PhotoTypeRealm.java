package ua.com.merchik.merchik.database.realm.tables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class PhotoTypeRealm {

    public static RealmResults<ImagesTypeListDB> getPhotoType(){
        return INSTANCE.where(ImagesTypeListDB.class)
                .findAll();
    }


    public static Map<String, String> getPhotoTypeMap(){
        Map<String, String> result = new HashMap<>();

        List<ImagesTypeListDB> list = getPhotoType();
        for (ImagesTypeListDB item : list){
            if (item.getNm() != null){
                result.put(String.valueOf(item.getId()), item.getNm());
            }
        }

        return result;
    }
}
