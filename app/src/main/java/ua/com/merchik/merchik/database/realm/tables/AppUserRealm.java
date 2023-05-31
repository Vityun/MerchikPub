package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;

public class AppUserRealm {

    public static AppUsersDB getAppUser(){
        return INSTANCE.where(AppUsersDB.class)
                .findFirst();
    }

    public static AppUsersDB getAppUserById(int id){
        return INSTANCE.where(AppUsersDB.class)
                .equalTo("userId", id)
                .findFirst();
    }
}
