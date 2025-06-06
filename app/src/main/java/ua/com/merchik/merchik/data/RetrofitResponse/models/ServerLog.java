package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.RetrofitResponse.Log;

public class ServerLog {

    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("log")
    @Expose
    private List<Log> log = null;

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

    public List<Log> getLog() {
        return log;
    }

    public void setLog(List<Log> log) {
        this.log = log;
    }

}