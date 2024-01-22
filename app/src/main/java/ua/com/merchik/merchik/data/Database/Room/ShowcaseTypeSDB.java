package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "showcase_type")
public class ShowcaseTypeSDB {
    @PrimaryKey
    @NonNull
    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "ID")
    public Integer id;

    @SerializedName("parent_id")
    @Expose
    @ColumnInfo(name = "parent_id")
    public Integer parentId;

    @SerializedName("is_grp")
    @Expose
    @ColumnInfo(name = "is_grp")
    public Integer isGrp;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("comments")
    @Expose
    @ColumnInfo(name = "comments")
    public String comments;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Integer authorId;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public Long dtUpdate;
}
