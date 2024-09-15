package ua.com.merchik.merchik.Activities;

import android.app.Application;
import android.content.Context;

//import com.google.firebase.FirebaseApp;

import dagger.hilt.android.HiltAndroidApp;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.room.RoomManager;

@HiltAndroidApp
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
//        FirebaseApp.initializeApp(this);
        RealmManager.init(this);
        RoomManager.init(this);
        Clock.initTime();

        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

}


