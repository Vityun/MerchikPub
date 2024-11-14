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

import ua.com.merchik.merchik.data.RetrofitResponse.VacancyItemResponse;
import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

@Entity(tableName = "vacancy")
public class VacancySDB implements DataObjectUI {

    @SerializedName("id")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Long id;

    @SerializedName("city_id")
    @Expose
    @ColumnInfo(name = "city_id")
    public Long cityId;

    @SerializedName("district_id")
    @Expose
    @ColumnInfo(name = "district_id")
    public Long districtId;

    @SerializedName("doljnost_id")
    @Expose
    @ColumnInfo(name = "doljnost_id")
    public Long doljnostId;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public Long dtChange;

    @SerializedName("dt_create")
    @Expose
    @ColumnInfo(name = "dt_create")
    public String dtCreate;

    @SerializedName("occupancy_id")
    @Expose
    @ColumnInfo(name = "occupancy_id")
    public Long occupancyId;

    @SerializedName("premium_start")
    @Expose
    @ColumnInfo(name = "premium_start")
    public Integer premiumStart;

    @SerializedName("route_id")
    @Expose
    @ColumnInfo(name = "route_id")
    public Long routeId;

    @SerializedName("salary")
    @Expose
    @ColumnInfo(name = "salary")
    public Integer salary;

    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "theme_id")
    public Integer themeId;

    @SerializedName("work_time")
    @Expose
    @ColumnInfo(name = "work_time")
    public String work_time;

    public VacancySDB() {}

    public VacancySDB(VacancyItemResponse from) {
        id = from.id;
        cityId = from.cityId;
        districtId = from.districtId;
        doljnostId = from.doljnostId;
        dtChange = from.dtChange;
        dtCreate = from.dtCreate;
        occupancyId = from.occupancyId;
        premiumStart = from.premiumStart;
        routeId = from.routeId;
        salary = from.salary;
        themeId = from.themeId;
        work_time = from.work_time;
    }

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return DataObjectUI.DefaultImpls.getHidedFieldsOnUI(this);
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
