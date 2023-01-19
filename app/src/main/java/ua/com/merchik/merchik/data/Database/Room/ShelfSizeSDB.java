package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.sql.Date;

@Entity(tableName = "shelf_size")
public class ShelfSizeSDB {

    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public String clientId;

    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public Integer addrId;

    @SerializedName("gruadr_id")
    @Expose
    @ColumnInfo(name = "gruadr_id")
    public Integer gruadrId;

    @SerializedName("grp_id")
    @Expose
    @ColumnInfo(name = "grp_id")
    public Integer grpId;

    @SerializedName("width")
    @Expose
    @ColumnInfo(name = "width")
    public Float width;

    @SerializedName("planzn")
    @Expose
    @ColumnInfo(name = "planzn")
    public Float planzn;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Integer authorId;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public Long dt;

    @SerializedName("dt_day")
    @Expose
    @ColumnInfo(name = "dt_day")
    public Date dtDay;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public Long dtChange;
}
