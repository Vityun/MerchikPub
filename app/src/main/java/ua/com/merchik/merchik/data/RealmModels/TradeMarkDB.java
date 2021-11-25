package ua.com.merchik.merchik.data.RealmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TradeMarkDB extends RealmObject {

    @SerializedName("ID")
    @Expose
    @PrimaryKey
    private String iD;
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("dt_update")
    @Expose
    private String dtUpdate;
    @SerializedName("sort_type")
    @Expose
    private String sortType;

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

    public String getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(String dtUpdate) {
        this.dtUpdate = dtUpdate;
    }

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

}
