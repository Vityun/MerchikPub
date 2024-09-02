package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.Database.Room.AddressSDBOverride;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;
import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

public class PhotoTypeRealm {

    public static RealmResults<ImagesTypeListDB> getPhotoType(){
        return INSTANCE.where(ImagesTypeListDB.class)
                .findAll();
    }

    public static ImagesTypeListDB getPhotoTypeById(int id){
        return INSTANCE.where(ImagesTypeListDB.class)
                .equalTo("id", id)
                .findFirst();
    }


    public static Map<Integer, String> getPhotoTypeMap(){
        Map<Integer, String> result = new HashMap<>();

        List<ImagesTypeListDB> list = getPhotoType();
        for (ImagesTypeListDB item : list){
            if (item.getNm() != null){
                result.put(Integer.valueOf(String.valueOf(item.getId())), item.getNm());
            }
        }

        return result;
    }
}
