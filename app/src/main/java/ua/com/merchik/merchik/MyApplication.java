package ua.com.merchik.merchik;

import android.app.Application;
import android.content.Context;

import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.room.RoomManager;

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        RealmManager.init(this);
        RoomManager.init(this);
        Clock.initTime();

        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

}


