package ua.com.merchik.merchik.database.realm.tables;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.RealmModels.ErrorDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class ErrorRealm {

    public static RealmResults<ErrorDB> getAllErrorDb() {
        return INSTANCE.where(ErrorDB.class)
                .findAll();
    }

    public static ErrorDB getErrorDbByNm(String nm) {
        ErrorDB result = INSTANCE.where(ErrorDB.class)
                .equalTo("nm", nm)
                .findFirst();
        if (result != null )
            result = INSTANCE.copyFromRealm(result);
        return result;
    }

    public static ErrorDB getErrorDbById(String id) {
        return INSTANCE.where(ErrorDB.class)
                .equalTo("ID", id)
                .findFirst();
    }
}
