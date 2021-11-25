package ua.com.merchik.merchik.data.DataFromServer.PhotoData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ClientId {
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("selected")
    @Expose
    private Integer selected;

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getSelected() {
        return selected;
    }

    public void setSelected(Integer selected) {
        this.selected = selected;
    }
}
