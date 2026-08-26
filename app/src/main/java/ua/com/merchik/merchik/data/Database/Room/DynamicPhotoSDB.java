package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(
        tableName = "dynamic_photo",
        indices = {@Index(value = {"achieve_dynamics_id"})}
)
public class DynamicPhotoSDB {

    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "ID")
    public String id = "";

    @SerializedName("achieve_dynamics_id")
    @Expose
    @ColumnInfo(name = "achieve_dynamics_id")
    public String achieveDynamicsId;

    @SerializedName("photo_id")
    @Expose
    @ColumnInfo(name = "photo_id")
    public String photoId;

    @SerializedName("active")
    @Expose
    @ColumnInfo(name = "active")
    public String active;

    @SerializedName("dvi")
    @Expose
    @ColumnInfo(name = "dvi")
    public String dvi;

    @SerializedName("about")
    @Expose
    @ColumnInfo(name = "about")
    public String about;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public String authorId;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public String dtUpdate;
}
