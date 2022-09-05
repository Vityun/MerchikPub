package ua.com.merchik.merchik.database.realm.tables;

import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class AppUserRealm {
    public static AppUsersDB getAppUserById(int id){
        return INSTANCE.where(AppUsersDB.class)
                .equalTo("userId", id)
                .findFirst();
    }
}
