package ua.com.merchik.merchik.data.RealmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class CustomerGroupsListDB  extends RealmObject {

    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("parent")
    @Expose
    private String parent;
    @SerializedName("client_id")
    @Expose
    private String clientId;

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

}