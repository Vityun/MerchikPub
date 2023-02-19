package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import android.util.Log;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;

public class TARCommentsRealm {

    public static void saveOrUpdate(List<TARCommentsDB> data) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    /**
     * 23.03.2021
     * Сохранение данных в таблицу "Комментарии к рекламациям" / "Переписка"
     * <p>
     * 21.12.2022
     * Добавлен этот лютый костыль из-за:
     * Если удалять и перезаписывать таблицу - выясняется что на стороне приложения ЕСТЬ фотография
     * у комментария, а на стороне сервера ЕЩЁ нет. Таким образом сервер, при синхронизации затирал
     * данные о фотографии в приложении и по итогу люди переставали видеть фото к комментам ЗИР.
     * <p>
     * Сделанное решение делает невозможным загрузку данных изменённых на сервере.
     */
    public static void setTARCommentsDB(List<TARCommentsDB> data) {
        try {
            if (data != null) {
                Log.e("setTARCommentsDB", "Start data: " + new Gson().toJson(data));
                String[] ids = new String[data.size()];
                int i = 0;
                for (TARCommentsDB item : data) {
                    ids[i++] = item.getID();
                }

                Log.e("setTARCommentsDB", "ids: " + Arrays.toString(ids));
                List<TARCommentsDB> notSaveComments = INSTANCE.copyFromRealm(TARCommentsRealm.getTARCommentByIds(ids));
                Log.e("setTARCommentsDB", "notSaveComments: " + new Gson().toJson(notSaveComments));
                if (notSaveComments != null && notSaveComments.size() > 0) {
                    for (TARCommentsDB servList : data) {
                        for (TARCommentsDB currentList : notSaveComments) {
                            if (servList.getID().equals(currentList.getID())) {
                                data.remove(servList);
                            }
                        }
                    }
                }
                Log.e("setTARCommentsDB", "End data: " + new Gson().toJson(data));

                INSTANCE.beginTransaction();
                INSTANCE.copyToRealm(data);
                INSTANCE.commitTransaction();
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "setTARCommentsDB/Exception", "Exception e: " + e);
        }
    }

    public static TARCommentsDB getTARCommentById(String id) {
        return INSTANCE.where(TARCommentsDB.class)
                .equalTo("id", id)
                .findFirst();
    }

    public static RealmResults<TARCommentsDB> getTARCommentByIds(String[] ids) {
        return INSTANCE.where(TARCommentsDB.class)
                .in("id", ids)
                .findAll();
    }


    /**
     * 23.03.2021
     * Получение комментариев (переписки) по ID ЗИР-а
     *
     * @return
     */
    public static List<TARCommentsDB> getTARCommentByTarId(String id) {
        return INSTANCE.where(TARCommentsDB.class)
                .equalTo("rId", id)
                .sort("dt", Sort.DESCENDING)
                .findAll();
    }


    /**
     * Для опции контроля 135329
     *
     * @param id
     * @param userId
     * @param mode   - Если установлен в 1: отбираю ВСЕ комментарии по данной задачи.
     *               - Если НЕ указан: Отображаю комментарии только те, к которым есть фотографии
     */
    public static List<TARCommentsDB> getTARCommentsToOptionControl(Integer id, Integer userId, Integer mode) {

        RealmResults<TARCommentsDB> res = INSTANCE.where(TARCommentsDB.class).findAll();

        res = res.where()
                .equalTo("rId", String.valueOf(id))
                .equalTo("who", String.valueOf(userId)).findAll();

        if (mode != null && mode == 1) {
            res = res.where()
                    .beginGroup()
                    .isNotNull("photo")
                    .notEqualTo("photo", "0")
                    .notEqualTo("photo", "")
                    .endGroup()
                    .or()
                    .beginGroup()
                    .isNotNull("photo_hash")
                    .notEqualTo("photo_hash", "0")
                    .notEqualTo("photo_hash", "")
                    .endGroup()
                    .findAll();
        }

        if (res != null) {
            return INSTANCE.copyFromRealm(res);
        } else {
            return null;
        }


/*        return INSTANCE.where(TARCommentsDB.class)
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
                .findAll();*/
    }


    /**
     * 26.03.2021
     * Получение комментов созданных на моей стороне для выгрузки на сервер.
     * <p>
     * Будет работать так: Получаю данные без ID, выгружаю их на сервер, обновляю с серверными
     * ID-шками, записываю обратно в базу данных.
     */
    public static List<TARCommentsDB> getTARCommentToUpload() {
        return INSTANCE.where(TARCommentsDB.class)
//                 .isNull("id")
                .equalTo("startUpdate", true)
                .findAll();
    }

}
