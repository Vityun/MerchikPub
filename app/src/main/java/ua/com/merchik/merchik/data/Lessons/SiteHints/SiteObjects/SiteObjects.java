package ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SiteObjects {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("object_list")
    @Expose
    private List<SiteObjectsDB> objectList = null;

//    @SerializedName("object_list")
//    @Expose
//    private List<SiteObjectsSDB> objectSQLList = null;

    @SerializedName("error")
    @Expose
    private String error;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<SiteObjectsDB> getObjectList() {
        return objectList;
    }

    public void setObjectList(List<SiteObjectsDB> objectList) {
        this.objectList = objectList;
    }

//    public List<SiteObjectsSDB> getObjectSQLList() {
//        return objectSQLList;
//    }
//
//    public void setObjectSQLList(List<SiteObjectsSDB> objectSQLList) {
//        this.objectSQLList = objectSQLList;
//    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
