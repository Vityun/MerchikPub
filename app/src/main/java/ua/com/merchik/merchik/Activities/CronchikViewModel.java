package ua.com.merchik.merchik.Activities;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.compose.runtime.snapshots.SnapshotStateList;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;

import ua.com.merchik.merchik.ServerExchange.workmager.WorkManagerHelper;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class CronchikViewModel extends ViewModel {

    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private final WorkManager workManager;
    private final LiveData<List<WorkInfo>> workInfoLiveData;

    //    private TimerCallback callback;
    private final Handler handler = new Handler(Looper.getMainLooper()); // Handler для UI-потока
//    private static final long DELAY_MS = 10_000; // 10 секунд в миллисекундах
//    private final Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            if (callback != null) {
//                callback.onTimerCronchik(); // Вызываем метод Activity
//            }
//            handler.postDelayed(this, 10_000); // Повтор каждые 10 сек
//        }
//    };

    public CronchikViewModel() {
        badgeCounts.add(null); // Для первой вкладки
        badgeCounts.add(null); // Для второй вкладки

        workManager = WorkManager.getInstance(MyApplication.getAppContext());
        workInfoLiveData = workManager.getWorkInfosForUniqueWorkLiveData("PhotoDownloadWork");
    }

    public LiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    public LiveData<List<WorkInfo>> getWorkInfo() {
        return workInfoLiveData;
    }

    // Добавляем методы для изменения состояния
    public void setLoading(boolean isLoading) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            loadingState.setValue(isLoading);
        } else {
            loadingState.postValue(isLoading);
        }
    }

    public void scheduleDownload() {
        WorkManagerHelper.INSTANCE.schedulePhotoDownloadTask(MyApplication.getAppContext());
    }

    private final SnapshotStateList<Integer> badgeCounts = new SnapshotStateList<>();

    public SnapshotStateList<Integer> getBadgeCounts() {
        return badgeCounts;
    }

    public void updateBadge(int index, @Nullable Integer count) {
        if (index >= 0 && index < badgeCounts.size()) {
            badgeCounts.set(index, count);
        }
    }

    public void updateBadgeAdditionalIncome() {
        badgeCounts.set(1, RealmManager.getAllWorkPlanForRNO().size());
    }

    public void clearBadge(int index) {
        updateBadge(index, null);
    }

}