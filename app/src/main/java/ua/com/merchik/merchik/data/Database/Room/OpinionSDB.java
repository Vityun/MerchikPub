package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "opinions")
public class OpinionSDB {
    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("grp_id")
    @Expose
    @ColumnInfo(name = "grp_id")
    public String grp_id;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public String dtChange;

    @Override
    public String toString() {
        return nm;
    }
}
