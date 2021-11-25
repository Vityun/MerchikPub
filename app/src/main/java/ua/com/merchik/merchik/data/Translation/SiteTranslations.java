package ua.com.merchik.merchik.data.Translation;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SiteTranslations {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("list")
    @Expose
    private List<SiteTranslationsList> list = null;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<SiteTranslationsList> getList() {
        return list;
    }

    public void setList(List<SiteTranslationsList> list) {
        this.list = list;
    }

}
