package ua.com.merchik.merchik.data.RetrofitResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

public class WpDataServer {

    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("list")
    @Expose
    private java.util.List<WpDataDB> wp_data = null;

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

    public java.util.List<WpDataDB> getList() {
        return wp_data;
    }

    public void setList(java.util.List<WpDataDB> list) {
        this.wp_data = list;
    }

}
