package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "article")
public class ArticleSDB {

    @PrimaryKey
    @SerializedName("ID")
    @Expose
    @NonNull
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName("vendor_code")
    @Expose
    @ColumnInfo(name = "vendor_code")
    public String vendorCode;

    @SerializedName("tovar_id")
    @Expose
    @ColumnInfo(name = "tovar_id")
    public Integer tovarId;

    @SerializedName("addr_tp_id")
    @Expose
    @ColumnInfo(name = "addr_tp_id")
    public Integer addrTpId;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;
}
