package ua.com.merchik.merchik.data.TestJsonUpload.TestLite;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TestLite {
    @SerializedName("mod")
    @Expose
    private String mod;
    @SerializedName("act")
    @Expose
    private String act;

    public String getMod() {
        return mod;
    }

    public void setMod(String mod) {
        this.mod = mod;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }
}
