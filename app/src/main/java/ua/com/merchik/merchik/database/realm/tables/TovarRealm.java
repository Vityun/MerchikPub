package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class TovarRealm {

    public static TovarDB getById(String tov) {
        TovarDB tovarDB = INSTANCE.where(TovarDB.class)
                .equalTo("iD", tov)
                .findFirst();
        if (tovarDB != null) tovarDB = INSTANCE.copyFromRealm(tovarDB);
        return tovarDB;
    }

    public static List<TovarDB> getByIds(String[] tov) {
        return INSTANCE.where(TovarDB.class)
                .in("iD", tov)
                .findAll();
    }

    public static List<TovarDB> getByCliIds(String[] tov) {
        return RealmManager.INSTANCE.copyFromRealm(INSTANCE.where(TovarDB.class)
                .in("clientId", tov)
                .findAll());
    }

    public static List<TovarDB> getTov() {
        return INSTANCE.where(TovarDB.class)
                .notEqualTo("photoId", "0")
                .findAll();
    }

    public static List<TovarDB> getAllTov() {
        return INSTANCE.copyFromRealm(INSTANCE.where(TovarDB.class)
                .findAll());
    }

    private static List<Integer> getTovarIdsForBatches(int batchSize) {
        List<Integer> tovarIds = new ArrayList<>();
        int offset = 0;

        while (true) {
            RealmResults<TovarDB> results = INSTANCE.where(TovarDB.class).findAll();

            // Рассчитываем границы для текущего батча
            int start = offset;
            int end = Math.min(offset + batchSize, results.size());

            if (start >= end) break; // Выходим из цикла, если достигнут конец

            // Копируем только текущий батч
            List<TovarDB> batch = INSTANCE.copyFromRealm(results.subList(start, end));

            // Извлекаем ID
            for (TovarDB tovar : batch) {
                tovarIds.add(Integer.valueOf(tovar.getiD()));
            }

            offset += batchSize;

        }

        return tovarIds;
    }

    public static List<Integer> getTovarIdsInBatches(int batchSize) {
        List<Integer> tovarIds = new ArrayList<>();
        int offset = 0;

        while (true) {
            Realm realm = Realm.getDefaultInstance();
            try {
                RealmResults<TovarDB> results = realm.where(TovarDB.class).findAll();

                // Рассчитываем границы для текущего батча
                int start = offset;
                int end = Math.min(offset + batchSize, results.size());

                if (start >= end) break; // Выходим из цикла, если достигнут конец

                // Копируем только текущий батч
                List<TovarDB> batch = realm.copyFromRealm(results.subList(start, end));

                // Извлекаем ID
                for (TovarDB tovar : batch) {
                    tovarIds.add(Integer.valueOf(tovar.getiD()));
                }

                offset += batchSize;
            } finally {
                realm.close();
            }
        }

        return tovarIds;
    }


    // Перегруженный метод с дефолтным значением batchSize
    public static List<Integer> getTovarIdsInBatches() {
        return getTovarIdsInBatches(1000);
    }
    // Перегруженный метод с дефолтным значением batchSize
}
