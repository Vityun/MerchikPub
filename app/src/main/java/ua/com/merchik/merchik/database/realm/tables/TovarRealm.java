package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.TovarDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class TovarRealm {

    public static TovarDB getById(String tov){
        return INSTANCE.where(TovarDB.class)
                .equalTo("iD", tov)
                .findFirst();
    }

    public static List<TovarDB> getByIds(String[] tov){
        return INSTANCE.where(TovarDB.class)
                .in("iD", tov)
                .findAll();
    }

}
