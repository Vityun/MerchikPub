package ua.com.merchik.merchik.data.Database.Room;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "wp_data_additional")
public class WPDataAdditional {


    @PrimaryKey
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "ID")
    public long ID;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public long dt;

    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public int clientId;

    @SerializedName("isp")
    @Expose
    @ColumnInfo(name = "isp")
    public String isp;

    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public int addrId;

    @SerializedName("code_dad2")
    @Expose
    @ColumnInfo(name = "code_dad2")
    public long codeDad2;

    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "theme_id")
    public int themeId;

    @SerializedName("user_decision")
    @Expose
    @ColumnInfo(name = "user_decision")
    public int userDecision;

    @SerializedName("confirm_dt")
    @Expose
    @ColumnInfo(name = "confirm_dt")
    public long confirmDt;

    @SerializedName("confirm_decision")
    @Expose
    @ColumnInfo(name = "confirm_decision")
    public int confirmDecision;

    @SerializedName("confirm_auto")
    @Expose
    @ColumnInfo(name = "confirm_auto")
    public int confirmAuto;

    @SerializedName("comment")
    @Expose
    @ColumnInfo(name = "comment")
    public String comment;

    @SerializedName("kps")
    @Expose
    @ColumnInfo(name = "kps")
    public int kps;

    @ColumnInfo(name = "uploadStatus")
    public int uploadStatus;
}
