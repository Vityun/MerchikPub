package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "potential_client")
public class PotentialClientSDB {
    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "id")
    public Integer id;


    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public Long dt;


    @SerializedName("dt_process")
    @Expose
    @ColumnInfo(name = "dtProcess")
    public Long dtProcess;


    @SerializedName("user_id")
    @Expose
    @ColumnInfo(name = "userId")
    public Integer userId;


    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addrId")
    public Integer addrId;


    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "themeId")
    public Integer themeId;


    @SerializedName("comments")
    @Expose
    @ColumnInfo(name = "comments")
    public String comments;
}
