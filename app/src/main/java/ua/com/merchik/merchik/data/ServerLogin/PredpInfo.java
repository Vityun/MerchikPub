package ua.com.merchik.merchik.data.ServerLogin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PredpInfo {

    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("uid")
    @Expose
    private String uid;

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
