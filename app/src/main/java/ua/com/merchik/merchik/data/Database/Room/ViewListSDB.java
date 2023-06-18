package ua.com.merchik.merchik.data.Database.Room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "view_list")
public class ViewListSDB {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName("lesson_id")
    @Expose
    @ColumnInfo(name = "lessonId")
    public Integer lessonId;

    @SerializedName("merchik_id")
    @Expose
    @ColumnInfo(name = "merchikId")
    public Integer merchikId;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public Long dt;
}
