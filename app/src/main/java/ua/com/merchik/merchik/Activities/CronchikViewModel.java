package ua.com.merchik.merchik.Activities;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;

import ua.com.merchik.merchik.ServerExchange.workmager.WorkManagerHelper;
import ua.com.merchik.merchik.Utils.TimerCallback;

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

//    // Устанавливаем колбэк (вызывается из Activity)
//    public void setTimerCallback(TimerCallback callback) {
//        this.callback = callback;
//    }
//
//    // Запускаем таймер
//    public void startTimer() {
//        handler.postDelayed(runnable, 10_000);
//    }
//
//    // Останавливаем таймер
//    public void stopTimer() {
//        handler.removeCallbacks(runnable);
//    }
//
//    @Override
//    protected void onCleared() {
//        super.onCleared();
//        stopTimer(); // Важно: очищаем при уничтожении ViewModel
//        callback = null; // Убираем ссылку на Activity
//    }

}