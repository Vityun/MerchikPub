package ua.com.merchik.merchik.data.Database.Room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(
        tableName = "wp_data_pause",
        primaryKeys = {"code_dad2", "dt_start"}
)
public class WPDataPauseSDB {

    @SerializedName("code_dad2")
    @Expose
    @ColumnInfo(name = "code_dad2")
    public long codeDad2;

    @SerializedName("dt_start")
    @Expose
    @ColumnInfo(name = "dt_start")
    public long dtStart;

    @SerializedName("dt_end")
    @Expose
    @ColumnInfo(name = "dt_end")
    public long dtEnd;

    @SerializedName("dt_update_client")
    @Expose
    @ColumnInfo(name = "dt_update_client")
    public long dtUpdateClient;

    @ColumnInfo(name = "uploadStatus")
    public int uploadStatus;
}
