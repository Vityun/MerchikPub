package ua.com.merchik.merchik.data.Lessons.SiteHints;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SiteHints {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("list")
    @Expose
    private List<SiteHintsDB> list = null;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<SiteHintsDB> getList() {
        return list;
    }

    public void setList(List<SiteHintsDB> list) {
        this.list = list;
    }
}
