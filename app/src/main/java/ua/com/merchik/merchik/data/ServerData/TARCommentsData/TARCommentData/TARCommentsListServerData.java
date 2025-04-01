package ua.com.merchik.merchik.data.ServerData.TARCommentsData.TARCommentData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TARCommentsListServerData {


    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("note")
    @Expose
    private String note;

    @SerializedName("state")
    @Expose
    private Boolean state;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    /*
    31.03.2025
    Весь код ниже deprecated, для поддержки старых версий оставляю, но TODO надо будет его убирать в новых сборках
    */

    @SerializedName("info")
    @Expose
    private TARCommentsInfoServerData info;

    @SerializedName("error")
    @Expose
    public String error;


    public TARCommentsInfoServerData getInfo() {
        return info;
    }

    public void setInfo(TARCommentsInfoServerData info) {
        this.info = info;
    }

}
