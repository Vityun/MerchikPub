package ua.com.merchik.merchik.data.Database.Room.Planogram;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "planogramm_type")
public class PlanogrammTypeSDB {

    @PrimaryKey(autoGenerate = false) // Указываем, что ключ не автоинкрементный
    @ColumnInfo(name = "id")
    @NonNull
    @SerializedName("ID")
    private Integer ID;

    @ColumnInfo(name = "planogram_id")
    @SerializedName("planogram_id")
    private String planogram_id;

    @ColumnInfo(name = "tt_id")
    @SerializedName("tt_id")
    private String tt_id;

    @ColumnInfo(name = "author_id")
    @SerializedName("author_id")
    private String author_id;

    @ColumnInfo(name = "dt_update_ut")
    @SerializedName("dt_update_ut")
    private String dt_update_ut;

    public PlanogrammTypeSDB(
            @NonNull Integer ID,
            String planogram_id,
            String tt_id,
            String author_id,
            String dt_update_ut
    ) {
        this.ID = ID;
        this.planogram_id = planogram_id;
        this.tt_id = tt_id;
        this.author_id = author_id;
        this.dt_update_ut = dt_update_ut;
    }

    // Геттеры
    @NonNull
    public Integer getID() {
        return ID;
    }

    public String getPlanogram_id() {
        return planogram_id;
    }

    public String getTt_id() {
        return tt_id;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public String getDt_update_ut() {
        return dt_update_ut;
    }

    // Сеттеры (необходимы для Room)
    public void setID(@NonNull Integer ID) {
        this.ID = ID;
    }

    public void setPlanogram_id(String planogram_id) {
        this.planogram_id = planogram_id;
    }

    public void setTt_id(String tt_id) {
        this.tt_id = tt_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public void setDt_update_ut(String dt_update_ut) {
        this.dt_update_ut = dt_update_ut;
    }
}