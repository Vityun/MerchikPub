package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class TARCommentsRealm {
    /**
     * 23.03.2021
     * Сохранение данных в таблицу "Комментарии к рекламациям" / "Переписка"
     */
    public static void setTARCommentsDB(List<TARCommentsDB> data) {
        try {
            if (data != null){
                INSTANCE.beginTransaction();
//                INSTANCE.delete(TARCommentsDB.class);
                INSTANCE.copyToRealm(data);
                INSTANCE.commitTransaction();
            }else {
                // TODO Set to LOG info about error
            }
        }catch (Exception e){
            // TODO Set to LOG info about error
        }
    }

    public static TARCommentsDB getTARCommentById(String id){
        return INSTANCE.where(TARCommentsDB.class)
                .equalTo("id", id)
                .findFirst();
    }

    public static RealmResults<TARCommentsDB> getTARCommentByIds(String[] ids){
        return INSTANCE.where(TARCommentsDB.class)
                .in("id", ids)
                .findAll();
    }


    /**
     * 23.03.2021
     * Получение комментариев (переписки) по ID ЗИР-а
     *
     * @return*/
    public static List<TARCommentsDB> getTARCommentByTarId(String id){
        return INSTANCE.where(TARCommentsDB.class)
                .equalTo("rId", id)
                .sort("dt", Sort.DESCENDING)
                .findAll();
    }


    /**
     * Для опции контроля 135329
     *
     * @param id
     * @param userId*/
    public static List<TARCommentsDB> getTARCommentsToOptionControl(Integer id, Integer userId){
        return INSTANCE.where(TARCommentsDB.class)
                .equalTo("rId", String.valueOf(id))
                .equalTo("who", String.valueOf(userId))
                .and()
                .beginGroup()
                .notEqualTo("photo", "0")
                .notEqualTo("photo", "")
                .endGroup()
                .or()
                .beginGroup()
                .notEqualTo("photo_hash", "0")
                .notEqualTo("photo_hash", "")
                .endGroup()
                .findAll();
    }


    /**
     * 26.03.2021
     * Получение комментов созданных на моей стороне для выгрузки на сервер.
     *
     * Будет работать так: Получаю данные без ID, выгружаю их на сервер, обновляю с серверными
     * ID-шками, записываю обратно в базу данных.
     * */
    public static List<TARCommentsDB> getTARCommentToUpload(){
         return INSTANCE.where(TARCommentsDB.class)
//                 .isNull("id")
                 .equalTo("startUpdate", true)
                 .findAll();
    }

}
