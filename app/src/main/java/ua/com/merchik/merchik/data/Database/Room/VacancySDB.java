package ua.com.merchik.merchik.data.Database.Room;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ua.com.merchik.merchik.data.RetrofitResponse.VacancyItemResponse;

@Entity(tableName = "vacancy")
public class VacancySDB {

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
}
