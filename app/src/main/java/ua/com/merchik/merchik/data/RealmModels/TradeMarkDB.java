package ua.com.merchik.merchik.data.RealmModels;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import ua.com.merchik.merchik.data.Database.Room.AdditionalRequirementsDBOverride;
import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

public class TradeMarkDB extends RealmObject implements DataObjectUI {

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

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return "dt_update, ID, sort_type";
    }

    @Nullable
    @Override
    public Long getFieldTranslateId(@NonNull String key) {
        return DataObjectUI.DefaultImpls.getFieldTranslateId(this, key);
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
        return AdditionalRequirementsDBOverride.INSTANCE.getValueModifier(key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getContainerModifier(@NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getContainerModifier(this, jsonObject);
    }

    @Nullable
    @Override
    public Integer getIdResImage() {
        return DataObjectUI.DefaultImpls.getIdResImage(this);
    }

    @NonNull
    @Override
    public String getFieldsImageOnUI() {
        return DataObjectUI.DefaultImpls.getFieldsImageOnUI(this);
    }
}
