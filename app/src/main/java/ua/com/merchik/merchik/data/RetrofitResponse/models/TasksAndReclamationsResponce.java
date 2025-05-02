package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.TasksAndReclamationsDB;

public class TasksAndReclamationsResponce {

    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("list")
    @Expose
    private List<TasksAndReclamationsDB> list = null;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<TasksAndReclamationsDB> getList() {
        return list;
    }

    public void setList(List<TasksAndReclamationsDB> list) {
        this.list = list;
    }

}
