package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "coord_on_map")
public class CoordOnMapSDB {
    @PrimaryKey
    @ColumnInfo(name = "ID")
    @SerializedName("ID")
    public long id;
    @NonNull
    @SerializedName("nm")
    public String nm = "";
    @ColumnInfo(name = "parent_id", defaultValue = "0")
    @SerializedName("parent_id")
    public long parentId;
    @ColumnInfo(name = "supervisor_id", defaultValue = "0")
    @SerializedName("supervisor_id")
    public long supervisorId;
    @ColumnInfo(name = "city_id", defaultValue = "0")
    @SerializedName("city_id")
    public long cityId;
    @ColumnInfo(name = "operator_id", defaultValue = "0")
    @SerializedName("operator_id")
    public long operatorId;
    @ColumnInfo(name = "author_id", defaultValue = "0")
    @SerializedName("author_id")
    public long authorId;
    @ColumnInfo(name = "dt_update", defaultValue = "0")
    @SerializedName("dt_update")
    public long dtUpdate;
}
