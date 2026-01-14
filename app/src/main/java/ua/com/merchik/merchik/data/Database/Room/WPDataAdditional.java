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
    public int confirmDecision;     // состояние 1 обработали, 0 - в процессе

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

    @SerializedName("action")
    @Expose
    @ColumnInfo(name = "action")
    public int action;      // начиная с 10.09.2025 значения это поле имеет такую логику: 0 - не обработана | 1 - заявка подтверждена | 2 - заявка отклонена

    @ColumnInfo(name = "uploadStatus")
    public int uploadStatus;

    @SerializedName("date_from")
    @Expose
    @ColumnInfo(name = "date_from")
    public long dateFrom;

    @SerializedName("date_to")
    @Expose
    @ColumnInfo(name = "date_to")
    public long dateTo;

    @SerializedName("route_id")
    @Expose
    @ColumnInfo(name = "route_id")
    public int routeId;         // маршрут
}
