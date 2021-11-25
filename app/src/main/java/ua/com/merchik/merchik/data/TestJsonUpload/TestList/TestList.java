package ua.com.merchik.merchik.data.TestJsonUpload.TestList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TestList {
    @SerializedName("mod")
    @Expose
    private String mod;
    @SerializedName("act")
    @Expose
    private String act;
    @SerializedName("name")
    @Expose
    private List<String> name = null;

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

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }
}
