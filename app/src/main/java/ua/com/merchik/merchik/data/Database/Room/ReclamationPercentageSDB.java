package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.sql.Date;

@Entity(tableName = "reclamation_percentage")
public class ReclamationPercentageSDB {

    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName("tp")
    @Expose
    @ColumnInfo(name = "tp")
    public Integer tp;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public Date dt;

    @SerializedName("percent")
    @Expose
    @ColumnInfo(name = "percent")
    public Float percent;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;
}
