package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

public class StackPhotoRealm {

    public static void setAll(List<StackPhotoDB> data) {
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

    public static RealmResults<StackPhotoDB> getAllRealm() {
        return INSTANCE.where(StackPhotoDB.class)
                .findAll();
    }


    public static StackPhotoDB getById(int id) {
        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("id", id)
                .findFirst();
    }

    public static RealmResults<StackPhotoDB> getById(Integer[] id) {
        return INSTANCE.where(StackPhotoDB.class)
                .in("id", id)
                .findAll();
    }

    public static RealmResults<StackPhotoDB> getByIds(Integer[] id) {
        return INSTANCE.where(StackPhotoDB.class)
                .in("photoServerId", id)
                .findAll();
    }

    public static RealmResults<StackPhotoDB> getByIds2(String[] id) {
        return INSTANCE.where(StackPhotoDB.class)
                .in("photoServerId", id)
                .findAll();
    }

    public static RealmResults<StackPhotoDB> getByServerIds(List<Integer> ids) {
        String[] id = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            id[i] = String.valueOf(ids.get(i));
        }

        return INSTANCE.where(StackPhotoDB.class)
                .in("photoServerId", id)
                .findAll();
    }

    public static StackPhotoDB getByHash(String hash) {
        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("photo_hash", hash)
                .findFirst();
    }

    public static StackPhotoDB getByPhotoNum(String photoNum) {
        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("photo_num", photoNum)
                .findFirst();
    }

    public static void deleteByPhotoNum(String photoNum) {
        StackPhotoDB data = INSTANCE.where(StackPhotoDB.class)
                .equalTo("photo_num", photoNum)
                .findFirst();

        if (data != null) {
            if (!INSTANCE.isInTransaction()) {
                INSTANCE.beginTransaction();
            }
            data.deleteFromRealm();
            INSTANCE.commitTransaction();
        }
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
                    .notEqualTo("photo_type", 35)
//                    .isNull("showcase_id")  // Показываем мерчу в ЖФ фото НЕ "ВИТРИН"
                    .findAll();
        } else if (addr != 0 && customer.equals("")) {
            return INSTANCE.where(StackPhotoDB.class)
                    .isNotNull("photoServerId")
                    .equalTo("addr_id", addr)
                    .notEqualTo("photo_type", 18)
                    .notEqualTo("photo_type", 29)
                    .notEqualTo("photo_type", 35)
//                    .isNull("showcase_id")  // Показываем мерчу в ЖФ фото НЕ "ВИТРИН"
                    .findAll();
        } else if (addr == 0 && !customer.equals("")) {
            return INSTANCE.where(StackPhotoDB.class)
                    .isNotNull("photoServerId")
                    .equalTo("client_id", customer)
                    .notEqualTo("photo_type", 18)
                    .notEqualTo("photo_type", 29)
                    .notEqualTo("photo_type", 35)
//                    .isNull("showcase_id")  // Показываем мерчу в ЖФ фото НЕ "ВИТРИН"
                    .findAll();
        } else {
            return INSTANCE.where(StackPhotoDB.class)
                    .isNotNull("photoServerId")
                    .notEqualTo("photo_type", 18)
                    .notEqualTo("photo_type", 29)
                    .notEqualTo("photo_type", 35)
//                    .isNull("showcase_id")  // Показываем мерчу в ЖФ фото НЕ "ВИТРИН"
                    .findAll();
        }
    }


    public static RealmResults<StackPhotoDB> getPlanogramPhoto(int addr, String customer) {
        if (addr != 0 && !customer.equals("")) {
            RealmResults<StackPhotoDB> query = INSTANCE.where(StackPhotoDB.class)
                    .isNotNull("photoServerId")
                    .equalTo("addr_id", addr)
                    .equalTo("client_id", customer)
                    .equalTo("photo_type", 5)
                    .sort("dt", Sort.DESCENDING)
//                    .isNull("showcase_id")  // Показываем мерчу в ЖФ фото НЕ "ВИТРИН"
                    .limit(25)
                    .findAll();
            if (query != null && query.size() > 0) {
                return query;
            }
        }

        if (!customer.equals("")) {
            RealmResults<StackPhotoDB> query = INSTANCE.where(StackPhotoDB.class)
                    .isNotNull("photoServerId")
                    .equalTo("client_id", customer)
                    .equalTo("photo_type", 5)
//                    .isNull("showcase_id")  // Показываем мерчу в ЖФ фото НЕ "ВИТРИН"
                    .findAll();
            if (query != null && query.size() > 0) {
                return query;
            }
        }

//        return INSTANCE.where(StackPhotoDB.class)
//                .isNotNull("photoServerId")
//                .equalTo("photo_type", 5)
//                .findAll();

        return null;
    }


    /*Проверка есть планограмма или нет*/
    public static boolean checkByType5() {
        StackPhotoDB res = INSTANCE.where(StackPhotoDB.class)
                .equalTo("photo_type", 5)
                .findFirst();

        if (res != null) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 12.02.2022
     *
     * @return
     */
    public static List<Integer> findTovarIds(List<Integer> ids) {
        ArrayList<Integer> result = new ArrayList<>(); // id-шники которых нет в БД

        Log.e("MY_TIME", "START TIME");

        for (Integer tovarId : ids) {
            StackPhotoDB stackPhotoDB = INSTANCE.where(StackPhotoDB.class)
                    .equalTo("object_id", tovarId)
                    .findFirst();

            if (stackPhotoDB == null || stackPhotoDB.getPhoto_num().equals("")) {
                result.add(tovarId);
            }
        }

        Log.e("MY_TIME", "END TIME. После проверки всех Товаров");

        return result;
    }

    public static RealmResults<StackPhotoDB> getPhoto(long dtFrom, long dtTo, long dad2, int photoType) {
        return INSTANCE.where(StackPhotoDB.class)
                .between("create_time", dtFrom, dtTo)
                .equalTo("code_dad2", dad2)
                .equalTo("photo_type", photoType)
//                .isNull("showcase_id")  // Показываем мерчу в ЖФ фото НЕ "ВИТРИН"
                .findAll();
    }

    public static RealmResults<StackPhotoDB> getPhotosByDAD2(long dad2, int photoType) {
        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("code_dad2", dad2)
                .equalTo("photo_type", photoType)
//                .isNull("showcase_id")  // Показываем мерчу в ЖФ фото НЕ "ВИТРИН"
                .findAll();
    }


    /**
     * 09.01.2023.
     * Универсальная функция для получения Жернала фото
     */
    public static RealmResults<StackPhotoDB> getPhoto(Long dtFrom, Long dtTo, Integer userId, Integer addrId, String clientId, Long dad2, Integer photoType, String[] tovIds) {
        RealmResults<StackPhotoDB> res = INSTANCE.where(StackPhotoDB.class).findAll();

//        res = res.where().isNull("showcase_id").findAll();  // Показываем мерчу в ЖФ фото НЕ "ВИТРИН"

        if (res != null) {
            Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.res", "res: " + res.size());
        }

        Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.Long dtFrom", "Long dtFrom: " + dtFrom);
        Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.Long dtTo", "Long dtTo: " + dtTo);
        Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.Integer userId", "Integer userId: " + userId);
        Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.Integer addrId", "Integer addrId: " + addrId);
        Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.String clientId", "String clientId: " + clientId);
        Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.Long dad2", "Long dad2: " + dad2);
        Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.Integer photoType", "Integer photoType: " + photoType);


        if (dtFrom != null && dtTo != null) {
            res = res.where().between("create_time", dtFrom, dtTo).findAll();
            if (res != null) {
                Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.dtFromdtTo", "res: " + res.size());
            }
        }

        if (userId != null) {
            res = res.where().equalTo("user_id", userId).findAll();
            if (res != null) {
                Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.userId", "res: " + res.size());
            }
        }

        if (addrId != null) {
            res = res.where().equalTo("addr_id", addrId).findAll();
            if (res != null) {
                Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.addrId", "res: " + res.size());
            }
        }

        if (clientId != null) {
            res = res.where().equalTo("client_id", clientId).findAll();
            if (res != null) {
                Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.clientId", "res: " + res.size());
            }
        }

        if (dad2 != null) {
            res = res.where().equalTo("code_dad2", dad2).findAll();
            if (res != null) {
                Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.dad2", "res: " + res.size());
            }
        }

        if (photoType != null) {
            res = res.where().equalTo("photo_type", photoType).findAll();
            if (res != null) {
                Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.photoType", "res: " + res.size());
            }
        }

        if (tovIds != null) {
            res = res.where().in("tovar_id", tovIds).findAll();
            if (res != null) {
                Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.photoType", "res: " + res.size());
            }
        }

        if (res != null) {
            Globals.writeToMLOG("INFO", "StackPhotoRealm.getPhoto.all", "res: " + res.size());
        }

        return res;
    }


    /**
     * 18.10.23.
     * Получаем список фото по отчёту которые выгрузились уже на сайт
     */
    public static List<StackPhotoDB> getUploadedStackPhotoByDAD2(Long codeDad2) {
        RealmResults<StackPhotoDB> res = INSTANCE.where(StackPhotoDB.class).findAll();

        if (codeDad2 != null) {
            res = res.where().equalTo("code_dad2", codeDad2).findAll();
        }

        res = res.where().greaterThan("get_on_server", 1).findAll();

        return INSTANCE.copyFromRealm(res);
    }


}
