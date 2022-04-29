package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "address")
public class AddressSDB {

    @SerializedName("addr_id")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("city_id")
    @Expose
    @ColumnInfo(name = "city_id")
    public Integer cityId;

    @SerializedName("tp_id")
    @Expose
    @ColumnInfo(name = "tp_id")
    public Integer tpId;

    @SerializedName("obl_id")
    @Expose
    @ColumnInfo(name = "obl_id")
    public Integer oblId;

    @SerializedName("tt_id")
    @Expose
    @ColumnInfo(name = "tt_id")
    public Integer ttId;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;

    @SerializedName("location_xd")
    @Expose
    @ColumnInfo(name = "location_xd")
    public Float locationXd;

    @SerializedName("location_yd")
    @Expose
    @ColumnInfo(name = "location_yd")
    public Float locationYd;

}
