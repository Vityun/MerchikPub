package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;

public class AppUserRealm {

    public static AppUsersDB getAppUser(){
        AppUsersDB appUsersDB = INSTANCE.where(AppUsersDB.class)
                .findFirst();
        if (appUsersDB != null) appUsersDB = INSTANCE.copyFromRealm(appUsersDB);
        return appUsersDB;
    }

    public static AppUsersDB getAppUserById(int id){
        AppUsersDB appUsersDB = INSTANCE.where(AppUsersDB.class)
                .equalTo("userId", id)
                .findFirst();
        if (appUsersDB != null) appUsersDB = INSTANCE.copyFromRealm(appUsersDB);
        return appUsersDB;
    }
}
