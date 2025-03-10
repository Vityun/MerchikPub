package ua.com.merchik.merchik.Activities.TaskAndReclamations;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;


public class TARViewModel extends ViewModel {
    private final MutableLiveData<TasksAndReclamationsSDB> tasksAndReclamations = new MutableLiveData<>();

    public void setTasksAndReclamations(TasksAndReclamationsSDB data) {
        tasksAndReclamations.setValue(data);
    }

    public LiveData<TasksAndReclamationsSDB> getTasksAndReclamations() {
        return tasksAndReclamations;
    }
}
