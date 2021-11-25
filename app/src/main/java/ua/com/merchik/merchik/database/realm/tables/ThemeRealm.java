package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.ThemeDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class ThemeRealm {

    /**
     * 25.03.2021
     * Сохранение Тем в БД
     * */
    public static void setThemeDBTable(List<ThemeDB> data) {
        INSTANCE.beginTransaction();
        INSTANCE.delete(ThemeDB.class);
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }


    public static ThemeDB getByID(String id){
        return INSTANCE.where(ThemeDB.class)
                .equalTo("id", id)
                .findFirst();
    }


    public static ThemeDB getThemeById(String id){
        return INSTANCE.where(ThemeDB.class)
                .endsWith("id", id)
                .findFirst();
    }

    public static List<ThemeDB> getTARTheme(){
        return INSTANCE.where(ThemeDB.class)
                .equalTo("tp", "2")
                .findAll();
    }

}
