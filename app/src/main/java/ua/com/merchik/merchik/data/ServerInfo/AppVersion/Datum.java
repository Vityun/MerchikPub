package ua.com.merchik.merchik.data.ServerInfo.AppVersion;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum {
    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("val")
    @Expose
    private String val;

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

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}
