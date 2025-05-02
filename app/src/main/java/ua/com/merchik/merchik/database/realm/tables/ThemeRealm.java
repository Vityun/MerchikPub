package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.ThemeDB;

public class ThemeRealm {

    /**
     * 25.03.2021
     * Сохранение Тем в БД
     * */
    public static void setThemeDBTable(List<ThemeDB> data) {
        INSTANCE.beginTransaction();
//        INSTANCE.delete(ThemeDB.class);
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    public static List<ThemeDB> getAll(){
        return INSTANCE.copyFromRealm(INSTANCE.where(ThemeDB.class).findAll());
    }


//    public static ThemeDB getByID(String id){
//        return INSTANCE.where(ThemeDB.class)
//                .equalTo("id", id)
//                .findFirst();
//    }


    public static ThemeDB getThemeById(String id){
        ThemeDB themeDB = INSTANCE.where(ThemeDB.class)
                .endsWith("id", id)
                .findFirst();
        if (themeDB != null) themeDB = INSTANCE.copyFromRealm(themeDB);
        return themeDB;
    }

    public static List<ThemeDB> getThemeByIds(String[] ids){
        List<ThemeDB> themeDB = INSTANCE.where(ThemeDB.class)
                .in("id", ids)
                .findAll();
        if (themeDB != null) themeDB = INSTANCE.copyFromRealm(themeDB);
        return themeDB;
    }

    public static List<ThemeDB> getTARTheme(){
        List<ThemeDB> themeDB =  INSTANCE.where(ThemeDB.class)
//                .equalTo("tp", "2")
                .findAll();
        if (themeDB != null) themeDB = INSTANCE.copyFromRealm(themeDB);
        return themeDB;
    }

}
