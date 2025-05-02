package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.ThemeDB;

public class ThemeTableRespose {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("list")
    @Expose
    private List<ThemeDB> list = null;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<ThemeDB> getList() {
        return list;
    }

    public void setList(List<ThemeDB> list) {
        this.list = list;
    }
}
