package ua.com.merchik.merchik.Activities;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;

import ua.com.merchik.merchik.ServerExchange.workmager.DownloadImagesWorker;
import ua.com.merchik.merchik.ServerExchange.workmager.WorkManagerHelper;

public class PhotoDownloaderViewModel extends ViewModel {
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private final WorkManager workManager;
    private final LiveData<List<WorkInfo>> workInfoLiveData;

    public PhotoDownloaderViewModel() {
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
}