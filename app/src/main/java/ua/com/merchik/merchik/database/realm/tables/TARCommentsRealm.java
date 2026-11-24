package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;

public class TARCommentsRealm {

    public static void saveOrUpdate(List<TARCommentsDB> data) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    // Download is insert-only: a known ID may have a local photo or a reply pending upload.
    public static void setTARCommentsDB(List<TARCommentsDB> data) {
        if (data == null || data.isEmpty()) return;
        INSTANCE.executeTransaction(realm -> {
            List<TARCommentsDB> missing = missingComments(data,
                    id -> realm.where(TARCommentsDB.class).equalTo("id", id).findFirst() != null);
            if (!missing.isEmpty()) realm.insert(missing);
        });
    }

    static List<TARCommentsDB> missingComments(List<TARCommentsDB> downloaded, Predicate<String> stored) {
        Set<String> seen = new HashSet<>();
        List<TARCommentsDB> missing = new ArrayList<>();
        for (TARCommentsDB row : downloaded) {
            String id = row == null ? null : row.getID();
            if (id != null && !id.trim().isEmpty() && seen.add(id) && !stored.test(id)) {
                missing.add(row);
            }
        }
        return missing;
    }

    public static TARCommentsDB getTARCommentById(String id) {
        return INSTANCE.where(TARCommentsDB.class)
                .equalTo("id", id)
                .findFirst();
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
