package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReportHintList {
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("field")
    @Expose
    private String field;
    @SerializedName("important")
    @Expose
    private Boolean important;
    @SerializedName("recent_items")
    @Expose
    private List<RecentItem> recentItems = null;
    @SerializedName("value")
    @Expose
    private String value;

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Boolean getImportant() {
        return important;
    }

    public void setImportant(Boolean important) {
        this.important = important;
    }

    public List<RecentItem> getRecentItems() {
        return recentItems;
    }

    public void setRecentItems(List<RecentItem> recentItems) {
        this.recentItems = recentItems;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
