package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.OptionsDB;

public class OptionsServer {

    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("list")
    @Expose
    private List<OptionsDB> list = null;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<OptionsDB> getList() {
        return list;
    }

    public void setList(List<OptionsDB> list) {
        this.list = list;
    }

}