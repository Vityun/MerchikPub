package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;

import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;
import ua.com.merchik.merchik.features.main.AddressSDBOverride;

/**
 * Таблица Адресов
 * */
@Entity(tableName = "address")
public class AddressSDB implements DataObjectUI {

    @SerializedName("addr_id")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("city_id")
    @Expose
    @ColumnInfo(name = "city_id")
    public Integer cityId;

//    группа юрлиц (торговая марка)
    @SerializedName("tp_id")
    @Expose
    @ColumnInfo(name = "tp_id")
    public Integer tpId;

    @SerializedName("obl_id")
    @Expose
    @ColumnInfo(name = "obl_id")
    public Integer oblId;

    @SerializedName("tt_id")
    @Expose
    @ColumnInfo(name = "tt_id")
    public Integer ttId;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;

    @SerializedName("location_xd")
    @Expose
    @ColumnInfo(name = "location_xd")
    public Float locationXd;

    @SerializedName("location_yd")
    @Expose
    @ColumnInfo(name = "location_yd")
    public Float locationYd;

    @SerializedName("kol_kass")
    @Expose
    @ColumnInfo(name = "kol_kass")
    public Integer kolKass;

    @SerializedName("nomer_tt")
    @Expose
    @ColumnInfo(name = "nomer_tt")
    public Integer nomerTT;

    @SerializedName("kps")
    @Expose
    @ColumnInfo(name = "kps")
    public Integer kps;


    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return DataObjectUI.DefaultImpls.getHidedFieldsOnUI(this);
    }

    @Nullable
    @Override
    public Long getFieldTranslateId(@NonNull String key) {
        return AddressSDBOverride.INSTANCE.getTranslateId(key);
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

    @Nullable
    @Override
    public List<String> getFieldsForOrderOnUI() {
        return DataObjectUI.DefaultImpls.getFieldsForOrderOnUI(this);
    }

    @NonNull
    @Override
    public List<String> getPreferredFieldOrder() {
        return DataObjectUI.DefaultImpls.getPreferredFieldOrder(this);
    }
}
