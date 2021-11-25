package ua.com.merchik.merchik.data.URLData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class URLData {

    @SerializedName("act")
    @Expose
    private String act;
    @SerializedName("params")
    @Expose
    private Params params;
    @SerializedName("sess_id")
    @Expose
    private String sessId;

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public String getSessId() {
        return sessId;
    }

    public void setSessId(String sessId) {
        this.sessId = sessId;
    }

}
