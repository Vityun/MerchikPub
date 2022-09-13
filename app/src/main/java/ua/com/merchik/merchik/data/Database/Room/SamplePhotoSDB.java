package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "sample_photo")
public class SamplePhotoSDB {
    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName("photo_id")
    @Expose
    @ColumnInfo(name = "photoId")
    public Integer photoId;

    @SerializedName("about")
    @Expose
    @ColumnInfo(name = "about")
    public String about;

    @SerializedName("id_1c")
    @Expose
    @ColumnInfo(name = "id1c")
    public Integer id1c;

    @SerializedName("photo_tp")
    @Expose
    @ColumnInfo(name = "photoTp")
    public Integer photoTp;

    @SerializedName("active")
    @Expose
    @ColumnInfo(name = "active")
    public Integer active;

    @SerializedName("grp_id")
    @Expose
    @ColumnInfo(name = "grpId")
    public Integer grpId;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("abbr")
    @Expose
    @ColumnInfo(name = "abbr")
    public String abbr;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dtUpdate")
    public Long dtUpdate;
}
