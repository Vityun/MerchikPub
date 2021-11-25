package ua.com.merchik.merchik.data.ServerLogin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginSearch {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("list")
    @Expose
    private List<LoginSearchList> list = null;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<LoginSearchList> getList() {
        return list;
    }

    public void setList(List<LoginSearchList> list) {
        this.list = list;
    }
}
