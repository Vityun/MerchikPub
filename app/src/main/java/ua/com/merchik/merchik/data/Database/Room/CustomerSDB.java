package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.sql.Date;
import java.util.List;

import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;
import ua.com.merchik.merchik.features.main.CustomerSDBOverride;
/**
 * Таблица КЛИЕНТОВ доступных данному пользователю
 * */
@Entity(tableName = "client")
public class CustomerSDB implements DataObjectUI {
    @SerializedName("client_id")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String id;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("edrpou")
    @Expose
    @ColumnInfo(name = "edrpou")
    public String edrpou;

    @SerializedName("main_tov_grp")
    @Expose
    @ColumnInfo(name = "main_tov_grp")
    public Integer mainTovGrp;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;

    @SerializedName("recl_reply_mode")
    @Expose
    @ColumnInfo(name = "recl_reply_mode")
    public Integer reclReplyMode;   // РежОтветаНаЗиР

    @SerializedName("ppa_auto")
    @Expose
    @ColumnInfo(name = "ppa_auto")
    public Integer ppaAuto;

    @SerializedName("work_start_date")
    @Expose
    @ColumnInfo(name = "work_start_date")
    public Date workStartDate;

    @SerializedName("work_restart_date")
    @Expose
    @ColumnInfo(name = "work_restart_date")
    public Date workRestartDate;

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return CustomerSDBOverride.INSTANCE.getHidedFieldsOnUI();
    }

    @Nullable
    @Override
    public Long getFieldTranslateId(@NonNull String key) {
        return CustomerSDBOverride.INSTANCE.getTranslateId(key);
    }

    @NonNull
    @Override
    public String getValueUI(@NonNull String key, @NonNull Object value) {
        return DataObjectUI.DefaultImpls.getValueUI(this, key, value);
    }

    @Nullable
    @Override
    public MerchModifier getFieldModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
       return CustomerSDBOverride.INSTANCE.getFieldModifier(key, jsonObject);
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
}
