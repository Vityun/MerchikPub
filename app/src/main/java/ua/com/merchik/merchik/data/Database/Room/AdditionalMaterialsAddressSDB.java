package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "additional_materials_address")
public class AdditionalMaterialsAddressSDB {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("file_id")
    @Expose
    @ColumnInfo(name = "file_id")
    public Integer fileId;

    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public Integer addrId;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Integer authorId;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;
}
