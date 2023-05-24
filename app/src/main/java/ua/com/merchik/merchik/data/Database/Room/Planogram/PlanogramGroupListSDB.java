package ua.com.merchik.merchik.data.Database.Room.Planogram;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "planogram_group_list")
public class PlanogramGroupListSDB {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    private Integer id;

    @SerializedName("planogram_id")
    @Expose
    @ColumnInfo(name = "planogram_id")
    private Integer planogramId;

    @SerializedName("group_id")
    @Expose
    @ColumnInfo(name = "group_id")
    private Integer groupId;

    @SerializedName("group_txt")
    @Expose
    @ColumnInfo(name = "group_txt")
    private String groupTxt;

    @SerializedName("author_txt")
    @Expose
    @ColumnInfo(name = "author_txt")
    private String authorTxt;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    private Integer authorId;

    @SerializedName("dt_update_ut")
    @Expose
    @ColumnInfo(name = "dt_update_ut")
    private Long dtUpdateUt;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    private String dtUpdate;
}
