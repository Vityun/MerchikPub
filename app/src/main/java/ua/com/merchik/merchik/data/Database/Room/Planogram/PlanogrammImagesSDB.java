package ua.com.merchik.merchik.data.Database.Room.Planogram;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "planogramm_img")
public class PlanogrammImagesSDB {

    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("planogram_id")
    @Expose
    @ColumnInfo(name = "planogram_id")
    public Integer planogramId;

    @SerializedName("img_id")
    @Expose
    @ColumnInfo(name = "img_id")
    public String imgId;

    @SerializedName("photo_id")
    @Expose
    @ColumnInfo(name = "photo_id")
    public Integer photoId;

    @SerializedName("photo_big")
    @Expose
    @ColumnInfo(name = "photo_big")
    public String photoBig;

    @SerializedName("author_txt")
    @Expose
    @ColumnInfo(name = "author_txt")
    public String authorTxt;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Integer authorId;

    @SerializedName("dt_update_ut")
    @Expose
    @ColumnInfo(name = "dt_update_ut")
    public Long dtUpdateUt;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public String dtUpdate;
}
