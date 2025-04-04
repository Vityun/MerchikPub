package ua.com.merchik.merchik.data.Database.Room;

import android.util.Log;

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
import ua.com.merchik.merchik.features.main.AdditionalRequirementsDBOverride;
import ua.com.merchik.merchik.features.main.UsersSDBOverride;

/**
 * Таблица в которой хронятся ВСЕ сотрудники ДОСТУПНЫЕ данному пользователю (который залогинен)
 * */
@Entity(tableName = "sotr")
public class UsersSDB implements DataObjectUI{
    @SerializedName("user_id")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("fio")
    @Expose
    @ColumnInfo(name = "fio")
    public String fio;

    @SerializedName("tel")
    @Expose
    @ColumnInfo(name = "tel")
    public String tel;

    @SerializedName("tel2")
    @Expose
    @ColumnInfo(name = "tel2")
    public String tel2;

    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public String clientId;

    @SerializedName("department")
    @Expose
    @ColumnInfo(name = "department")
    public Integer department;  // Подразделение

    @SerializedName("otdel_id")
    @Expose
    @ColumnInfo(name = "otdel_id")
    public Integer otdelId;  // Отдел

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Integer authorId;

    @SerializedName("city_id")
    @Expose
    @ColumnInfo(name = "city_id")
    public Integer cityId;

    @SerializedName("work_addr_id")
    @Expose
    @ColumnInfo(name = "work_addr_id")
    public Integer workAddrId;

    @SerializedName("inn")
    @Expose
    @ColumnInfo(name = "inn")
    public String inn;

    @SerializedName("img_personal_photo_thumb")
    @Expose
    @ColumnInfo(name = "img_personal_photo_thumb")
    public String img_personal_photo_thumb;

    @SerializedName("img_personal_photo")
    @Expose
    @ColumnInfo(name = "img_personal_photo")
    public String img_personal_photo;

    @SerializedName("img_personal_photo_path")
    @Expose
    @ColumnInfo(name = "img_personal_photo_path")
    public Integer img_personal_photo_stackId;

    @SerializedName("send_sms")
    @Expose
    @ColumnInfo(name = "send_sms")
    public Integer sendSms;

    @SerializedName("fired")
    @Expose
    @ColumnInfo(name = "fired")
    public Integer fired;

    @SerializedName("fired_reason")
    @Expose
    @ColumnInfo(name = "fired_reason")
    public String firedReason;

    @SerializedName("fired_dt")
    @Expose
    @ColumnInfo(name = "fired_dt")
    public Long firedDt;

    @SerializedName("flag")
    @Expose
    @ColumnInfo(name = "flag")
    public String flag;

    @SerializedName("report_count")
    @Expose
    @ColumnInfo(name = "report_count")
    public Integer reportCount;

    @SerializedName("report_date_01")
    @Expose
    @ColumnInfo(name = "report_date_01")
    public Date reportDate01;

    @SerializedName("report_date_05")
    @Expose
    @ColumnInfo(name = "report_date_05")
    public Date reportDate05;

    @SerializedName("report_date_20")
    @Expose
    @ColumnInfo(name = "report_date_20")
    public Date reportDate20;

    @SerializedName("report_date_40")
    @Expose
    @ColumnInfo(name = "report_date_40")
    public Date reportDate40;


    @SerializedName("report_date_200")
    @Expose
    @ColumnInfo(name = "report_date_200")
    public Date reportDate200;

    @SerializedName("tel_corp")
    @Expose
    @ColumnInfo(name = "tel_corp")
    public Integer telCorp;

    @SerializedName("tel2_corp")
    @Expose
    @ColumnInfo(name = "tel2_corp")
    public Integer tel2Corp;

    // 07.03.25 добавил поле получение последнего экл
    @SerializedName("last_ekl_date")
    @Expose
    @ColumnInfo(name = "last_ekl_date")
    public String last_ekl_date;



    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return UsersSDBOverride.INSTANCE.getHidedFieldsOnUI();
    }

    @Nullable
    @Override
    public Long getFieldTranslateId(@NonNull String key) {
        return UsersSDBOverride.INSTANCE.getTranslateId(key);
    }

    @NonNull
    @Override
    public String getValueUI(@NonNull String key, @NonNull Object value) {
        return UsersSDBOverride.INSTANCE.getValueUI(key, value);
    }

    @Nullable
    @Override
    public MerchModifier getFieldModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getFieldModifier(this, key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getValueModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return UsersSDBOverride.INSTANCE.getValueModifier(key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getContainerModifier(@NonNull JSONObject jsonObject) {
        return UsersSDBOverride.INSTANCE.getContainerModifier(jsonObject);
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

//    @Override
//    public String toString() {
//        return fio;
//    }
}
