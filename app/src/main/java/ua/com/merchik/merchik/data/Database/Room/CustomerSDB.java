package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "client")
public class CustomerSDB {
    @SerializedName("client_id")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String id;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("edrpou")
    @Expose
    @ColumnInfo(name = "edrpou")
    public String edrpou;

    @SerializedName("main_tov_grp")
    @Expose
    @ColumnInfo(name = "main_tov_grp")
    public Integer mainTovGrp;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;

    @SerializedName("recl_reply_mode")
    @Expose
    @ColumnInfo(name = "recl_reply_mode")
    public Integer reclReplyMode;   // РежОтветаНаЗиР

    @SerializedName("ppa_auto")
    @Expose
    @ColumnInfo(name = "ppa_auto")
    public Integer ppaAuto;   
}
