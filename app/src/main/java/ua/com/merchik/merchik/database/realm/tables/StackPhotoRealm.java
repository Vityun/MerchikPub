package ua.com.merchik.merchik.database.realm.tables;

import android.util.Log;

import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class StackPhotoRealm {

    public static void setAll(List<StackPhotoDB> data){
        INSTANCE.beginTransaction();
        INSTANCE.insertOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    public static StackPhotoDB stackPhotoDBGetPhotoBySiteId(String id) {
        if (id == null || id.equals("")) {
            return null;
        }

        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("photoServerId", id)
                .findFirst();
    }

    public static List<StackPhotoDB> getAll() {
        return INSTANCE.where(StackPhotoDB.class)
                .findAll();
    }


    public static StackPhotoDB getById(int id) {
        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("id", id)
                .findFirst();
    }

    public static RealmResults<StackPhotoDB> getByIds(Integer[] id) {
        return INSTANCE.where(StackPhotoDB.class)
                .in("id", id)
                .findAll();
    }

    public static StackPhotoDB getByHash(String hash) {
        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("photo_hash", hash)
                .findFirst();
    }

    public static RealmResults<StackPhotoDB> getTARFilterPhoto(int addr, String customer) {

        Log.d("test", "" + addr + customer);

        if (addr != 0 && !customer.equals("")) {
            return INSTANCE.where(StackPhotoDB.class)
                    .isNotNull("photoServerId")
                    .equalTo("addr_id", addr)
                    .equalTo("client_id", customer)
                    .notEqualTo("photo_type", 18)
                    .notEqualTo("photo_type", 29)
                    .findAll();
        } else if (addr != 0 && customer.equals("")) {
            return INSTANCE.where(StackPhotoDB.class)
                    .isNotNull("photoServerId")
                    .equalTo("addr_id", addr)
                    .notEqualTo("photo_type", 18)
                    .notEqualTo("photo_type", 29)
                    .findAll();
        } else if (addr == 0 && !customer.equals("")) {
            return INSTANCE.where(StackPhotoDB.class)
                    .isNotNull("photoServerId")
                    .equalTo("client_id", customer)
                    .notEqualTo("photo_type", 18)
                    .notEqualTo("photo_type", 29)
                    .findAll();
        } else {
            return INSTANCE.where(StackPhotoDB.class)
                    .isNotNull("photoServerId")
                    .notEqualTo("photo_type", 18)
                    .notEqualTo("photo_type", 29)
                    .findAll();
        }
    }


    public static RealmResults<StackPhotoDB> getPlanogramPhoto(int addr, String customer) {
        if (addr != 0 && !customer.equals("")){
            RealmResults<StackPhotoDB> query = INSTANCE.where(StackPhotoDB.class)
                    .isNotNull("photoServerId")
                    .equalTo("addr_id", addr)
                    .equalTo("client_id", customer)
                    .equalTo("photo_type", 5)
                    .findAll();
            if (query != null && query.size() > 0){return query;}
        }

        if (!customer.equals("")){
            RealmResults<StackPhotoDB> query = INSTANCE.where(StackPhotoDB.class)
                    .isNotNull("photoServerId")
                    .equalTo("client_id", customer)
                    .equalTo("photo_type", 5)
                    .findAll();
            if (query != null && query.size() > 0) {
                return query;
            }
        }

//        if (addr != 0){
//            RealmResults<StackPhotoDB> query = INSTANCE.where(StackPhotoDB.class)
//                    .isNotNull("photoServerId")
//                    .equalTo("addr_id", addr)
//                    .equalTo("photo_type", 5)
//                    .findAll();
//            if (query != null && query.size() > 0){return query;}
//        }



        return INSTANCE.where(StackPhotoDB.class)
                .isNotNull("photoServerId")
                .equalTo("photo_type", 5)
                .findAll();
    }



    /*Проверка есть планограмма или нет*/
    public static boolean checkByType5(){
        StackPhotoDB res = INSTANCE.where(StackPhotoDB.class)
                .equalTo("photo_type", 5)
                .findFirst();

        if (res!=null){
            return true;
        }else {
            return false;
        }
    }

}
