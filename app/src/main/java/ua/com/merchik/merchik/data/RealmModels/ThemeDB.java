package ua.com.merchik.merchik.data.RealmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class ThemeDB extends RealmObject {

    @SerializedName("ID")
    @Expose
    private String id;
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("comment")
    @Expose
    private String comment;
    @SerializedName("grp_id")
    @Expose
    private String grpId;
    @SerializedName("tp")
    @Expose
    private String tp;

    @SerializedName("need_photo")
    @Expose
    public Integer need_photo;

    @SerializedName("need_report")
    @Expose
    public Integer need_report;

    @SerializedName("dt_update")
    @Expose
    private String dtUpdate;

    public String getID() {
        return id;
    }

    public void setID(String iD) {
        this.id = iD;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getGrpId() {
        return grpId;
    }

    public void setGrpId(String grpId) {
        this.grpId = grpId;
    }

    public String getTp() {
        return tp;
    }

    public void setTp(String tp) {
        this.tp = tp;
    }

    public String getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(String dtUpdate) {
        this.dtUpdate = dtUpdate;
    }

    @Override
    public String toString() {
        return nm;
    }

}
