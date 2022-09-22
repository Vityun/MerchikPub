package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "tovar_grp_client")
public class TovarGroupClientSDB {
    @SerializedName("ID")
    @Expose
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public Integer clientId;

    @SerializedName("tovar_grp_id")
    @Expose
    @ColumnInfo(name = "tovar_grp_id")
    public Integer tovarGrpId;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Integer authorId;

    @SerializedName("addr_tp_id")
    @Expose
    @ColumnInfo(name = "addr_tp_id")
    public Integer addrTpId;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public Long dtChange;
}
