package ua.com.merchik.merchik.Activities;

import android.app.Application;
import android.content.Context;

//import com.google.firebase.FirebaseApp;

//import com.google.firebase.FirebaseApp;

import java.io.File;

import dagger.hilt.android.HiltAndroidApp;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.ServerExchange.workmager.WorkManagerHelper;
import ua.com.merchik.merchik.Utils.LogCleaner;
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

//        throw new RuntimeException("Test Crash"); // Force a crash
        File cacheDir = getCacheDir();

        BuildersKt.launch(GlobalScope.INSTANCE, Dispatchers.getIO(), CoroutineStart.DEFAULT, (coroutineScope, continuation) -> LogCleaner.INSTANCE.cleanOldLogs(cacheDir, continuation));

        WorkManagerHelper.INSTANCE.schedulePhotoDownloadTask(this);
//        WorkManagerHelper.INSTANCE.scheduleWpDataSync(this);

//        WorkManagerHelper.INSTANCE.startSyncWorker(this);

    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

}


