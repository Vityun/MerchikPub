package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "additional_materials_groups")
public class AdditionalMaterialsGroupsSDB {
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

    @SerializedName("group_id")
    @Expose
    @ColumnInfo(name = "group_id")
    public Integer groupId;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Integer authorId;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;
}
