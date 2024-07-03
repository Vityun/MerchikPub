package ua.com.merchik.merchik.data.RealmModels;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import io.realm.RealmObject;
import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

public class ThemeDB extends RealmObject implements DataObjectUI {

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

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return DataObjectUI.DefaultImpls.getHidedFieldsOnUI(this);
    }

    @Nullable
    @Override
    public Long getTranslateId(@NonNull String key) {
        return DataObjectUI.DefaultImpls.getTranslateId(this, key);
    }

    @NonNull
    @Override
    public String getValueUI(@NonNull String key, @NonNull Object value) {
        return DataObjectUI.DefaultImpls.getValueUI(this, key, value);
    }

    @Nullable
    @Override
    public MerchModifier getFieldModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getFieldModifier(this, key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getValueModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getValueModifier(this, key, jsonObject);
    }
}
