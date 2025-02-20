package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ua.com.merchik.merchik.data.RealmModels.WpDataDB;


public class DetailedReportViewModel extends ViewModel {
    private final MutableLiveData<WpDataDB> wpDataDB = new MutableLiveData<>();

    public void setWpDataDB(WpDataDB data) {
        wpDataDB.setValue(data);
    }

    public LiveData<WpDataDB> getWpDataDB() {
        return wpDataDB;
    }
}
