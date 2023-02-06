package ua.com.merchik.merchik.data.RealmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TARCommentsDB extends RealmObject {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    public String id;
    @SerializedName("tp")
    @Expose
    public String tp;
    @SerializedName("dt")
    @Expose
    public String dt;
    @SerializedName("who")
    @Expose
    public String who;
    @SerializedName("comment")
    @Expose
    public String comment;

    @SerializedName("photo")
    @Expose
    public String photo;

    @SerializedName("photo_hash")
    @Expose
    public String photo_hash;

    @SerializedName("r_id")
    @Expose
    public String rId;
    @SerializedName("dvi")
    @Expose
    public String dvi;
    @SerializedName("from_1c")
    @Expose
    public String from1c;
    @SerializedName("report_id")
    @Expose
    public String reportId;
    @SerializedName("responce_id")
    @Expose
    public String responceId;

    public boolean startUpdate;

    public String getID() {
        return id;
    }

    public void setID(String iD) {
        this.id = iD;
    }

    public String getTp() {
        return tp;
    }

    public void setTp(String tp) {
        this.tp = tp;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getRId() {
        return rId;
    }

    public void setRId(String rId) {
        this.rId = rId;
    }

    public String getDvi() {
        return dvi;
    }

    public void setDvi(String dvi) {
        this.dvi = dvi;
    }

    public String getFrom1c() {
        return from1c;
    }

    public void setFrom1c(String from1c) {
        this.from1c = from1c;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getResponceId() {
        return responceId;
    }

    public void setResponceId(String responceId) {
        this.responceId = responceId;
    }
}
