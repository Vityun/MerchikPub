package ua.com.merchik.merchik.Activities;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import java.io.File;

import com.google.android.gms.maps.MapsInitializer;
import dagger.hilt.android.HiltAndroidApp;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.R;
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
        WorkManagerHelper.INSTANCE.schedulePhotoDownloadTaskSecond(this);

//        WorkManagerHelper.INSTANCE.scheduleWpDataSync(this);

//        WorkManagerHelper.INSTANCE.startSyncWorker(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels();
        }

        MapsInitializer.initialize(
                getApplicationContext(),
                MapsInitializer.Renderer.LATEST,
                renderer -> {
                    // optional callback, можно оставить пустым
                }
        );

    }

    public static Context getAppContext() {
        return MyApplication.context;
    }


    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Канал по умолчанию
            NotificationChannel channel = new NotificationChannel(
                    getString(R.string.default_channel),
                    "Повідомлення",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Уведомления по умолчанию");
            channel.setShowBadge(true);

            // Канал для чатов
            NotificationChannel chatChannel = new NotificationChannel(
                    getString(R.string.channel_chat),
                    "Повідомлення чату",
                    NotificationManager.IMPORTANCE_HIGH
            );
            chatChannel.setDescription("Уведомления о новых сообщениях в чатах");
            chatChannel.setShowBadge(true);

            // Канал для подтверждения работы
            NotificationChannel workConfirmChannel = new NotificationChannel(
                    getString(R.string.channel_work_confirm),
                    "Роботу підтвердили",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            workConfirmChannel.setDescription("Уведомления о подтверждении работы");
            workConfirmChannel.setShowBadge(true);

            // Канал для изменений в работе
            NotificationChannel workUpdateChanel = new NotificationChannel(
                    getString(R.string.channel_work_update),
                    "Изменения в работе",
                    NotificationManager.IMPORTANCE_LOW
            );
            workUpdateChanel.setDescription("Уведомления о изменениях в данной работе");
            workUpdateChanel.setShowBadge(true);

            // Канал для задач
            NotificationChannel taskChannel = new NotificationChannel(
                    getString(R.string.channel_task),
                    "Нові завдання",
                    NotificationManager.IMPORTANCE_HIGH
            );
            taskChannel.setDescription("Уведомления о новых заданиях");

            // Канал для рекламаций
            NotificationChannel reclamationChannel = new NotificationChannel(
                    getString(R.string.channel_reclamation),
                    "Обновление в рекламациях",
                    NotificationManager.IMPORTANCE_HIGH
            );
            reclamationChannel.setDescription("Изменениния в рекламациях");

            // Канал для системных уведомлений
            NotificationChannel systemChannel = new NotificationChannel(
                    getString(R.string.channel_system),
                    "Системные",
                    NotificationManager.IMPORTANCE_HIGH
            );
            systemChannel.setDescription("Важные системные уведомления");

            // Канал для прочих обновлений
            NotificationChannel updateChannel = new NotificationChannel(
                    getString(R.string.channel_update),
                    "Получены новые изменения",
                    NotificationManager.IMPORTANCE_HIGH
            );
            updateChannel.setDescription("Прочие уведомления об обновлениях");


            // Создаем все каналы
            notificationManager.createNotificationChannel(chatChannel);
            notificationManager.createNotificationChannel(taskChannel);
            notificationManager.createNotificationChannel(reclamationChannel);
            notificationManager.createNotificationChannel(systemChannel);
            notificationManager.createNotificationChannel(updateChannel);
            notificationManager.createNotificationChannel(channel);
            notificationManager.createNotificationChannel(workConfirmChannel);
            notificationManager.createNotificationChannel(workUpdateChanel);
        }
    }
}




