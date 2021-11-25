package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

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
                INSTANCE.delete(TARCommentsDB.class);
                INSTANCE.copyToRealmOrUpdate(data);
                INSTANCE.commitTransaction();
            }else {
                // TODO Set to LOG info about error
            }
        }catch (Exception e){
            // TODO Set to LOG info about error
        }
    }


    /**
     * 23.03.2021
     * Получение комментариев (переписки) по ID ЗИР-а
     *
     * @return*/
    public static List<TARCommentsDB> getTARCommentByTarId(String id){
        return INSTANCE.where(TARCommentsDB.class)
                .equalTo("rId", id)
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
                 .isNull("id")
                 .findAll();
    }

}
