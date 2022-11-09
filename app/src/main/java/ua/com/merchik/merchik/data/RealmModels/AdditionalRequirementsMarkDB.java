package ua.com.merchik.merchik.data.RealmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class AdditionalRequirementsMarkDB extends RealmObject {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    private String id;
    @SerializedName("dt")
    @Expose
    private Long dt;    // секунды
    @SerializedName("item_id")
    @Expose
    private Integer itemId;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("score")
    @Expose
    private String score;
    @SerializedName("tp")
    @Expose
    private String tp;

    private String uploadStatus;

    public String comment;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getDt() {
        return dt;
    }

    public void setDt(Long dt) {
        this.dt = dt;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getTp() {
        return tp;
    }

    public void setTp(String tp) {
        this.tp = tp;
    }

    public String getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
}
