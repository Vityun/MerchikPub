package ua.com.merchik.merchik.data.RetrofitResponse;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VacancyItemResponse {

    @SerializedName("ID")
    @Expose
    public Long id;

    @SerializedName("city_id")
    @Expose
    public Long cityId;

    @SerializedName("district_id")
    @Expose
    public Long districtId;

    @SerializedName("doljnost_id")
    @Expose
    public Long doljnostId;

    @SerializedName("dt_change")
    @Expose
    public Long dtChange;

    @SerializedName("dt_create")
    @Expose
    public String dtCreate;

    @SerializedName("occupancy_id")
    @Expose
    public Long occupancyId;

    @SerializedName("premium_start")
    @Expose
    public Integer premiumStart;

    @SerializedName("route_id")
    @Expose
    public Long routeId;

    @SerializedName("salary")
    @Expose
    public Integer salary;

    @SerializedName("theme_id")
    @Expose
    public Integer themeId;

    @SerializedName("work_time")
    @Expose
    public String work_time;
}
