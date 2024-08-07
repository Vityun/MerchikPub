package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class TovarRealm {

    public static TovarDB getById(String tov){
        TovarDB tovarDB = INSTANCE.where(TovarDB.class)
                .equalTo("iD", tov)
                .findFirst();
        if (tovarDB != null) tovarDB = INSTANCE.copyFromRealm(tovarDB);
        return tovarDB;
    }

    public static List<TovarDB> getByIds(String[] tov){
        return INSTANCE.where(TovarDB.class)
                .in("iD", tov)
                .findAll();
    }

    public static List<TovarDB> getByCliIds(String[] tov){
        return RealmManager.INSTANCE.copyFromRealm(INSTANCE.where(TovarDB.class)
                .in("clientId", tov)
                .findAll());
    }

    public static List<TovarDB> getTov(){
        return INSTANCE.where(TovarDB.class)
                .notEqualTo("photoId", "0")
                .findAll();
    }

    public static List<TovarDB> getAllTov(){
        return INSTANCE.copyFromRealm(INSTANCE.where(TovarDB.class)
                .findAll());
    }

}
