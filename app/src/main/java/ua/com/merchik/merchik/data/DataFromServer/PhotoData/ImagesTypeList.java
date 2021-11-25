package ua.com.merchik.merchik.data.DataFromServer.PhotoData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ImagesTypeList {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("selected")
    @Expose
    private Integer selected;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public Integer getSelected() {
        return selected;
    }

    public void setSelected(Integer selected) {
        this.selected = selected;
    }
}
