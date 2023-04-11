package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "fragment")
public class FragmentSDB {

    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("img_id")
    @Expose
    @ColumnInfo(name = "img_id")
    public Integer imgId;

    @SerializedName("region_num")
    @Expose
    @ColumnInfo(name = "region_num")
    public Integer regionNum;

    @SerializedName("x1")
    @Expose
    @ColumnInfo(name = "x1")
    public Integer x1;

    @SerializedName("y1")
    @Expose
    @ColumnInfo(name = "y1")
    public Integer y1;

    @SerializedName("x2")
    @Expose
    @ColumnInfo(name = "x2")
    public Integer x2;

    @SerializedName("y2")
    @Expose
    @ColumnInfo(name = "y2")
    public Integer y2;

    @SerializedName("comment")
    @Expose
    @ColumnInfo(name = "comment")
    public String comment;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Integer authorId;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;
}
