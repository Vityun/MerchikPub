package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "content")
public class ContentSDB {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("training_id")
    @Expose
    @ColumnInfo(name = "training_id")
    public Integer trainingId;

    @SerializedName("group_id")
    @Expose
    @ColumnInfo(name = "group_id")
    public Integer groupId;

    @SerializedName("place_id")
    @Expose
    @ColumnInfo(name = "place_id")
    public Integer placeId;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("photo_id")
    @Expose
    @ColumnInfo(name = "photo_id")
    public Integer photoId;

    @SerializedName("exam_q_id")
    @Expose
    @ColumnInfo(name = "exam_q_id")
    public Integer examQId;

    @SerializedName("dop_treb_id")
    @Expose
    @ColumnInfo(name = "dop_treb_id")
    public Integer dopTrebId;

    @SerializedName("dop_material_id")
    @Expose
    @ColumnInfo(name = "dop_material_id")
    public Integer dopMaterialId;

    @SerializedName("url_video")
    @Expose
    @ColumnInfo(name = "url_video")
    public String urlVideo;

    @SerializedName("about")
    @Expose
    @ColumnInfo(name = "about")
    public String about;

    @SerializedName("img_example")
    @Expose
    @ColumnInfo(name = "img_example")
    public String imgExample;

    @SerializedName("qa_id")
    @Expose
    @ColumnInfo(name = "qa_id")
    public Integer qaId;

    @SerializedName("term_id")
    @Expose
    @ColumnInfo(name = "term_id")
    public Integer termId;

    @SerializedName("reglament_id")
    @Expose
    @ColumnInfo(name = "reglament_id")
    public Integer reglamentId;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Integer authorId;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public Integer dt;

    @SerializedName("so")
    @Expose
    @ColumnInfo(name = "so")
    public Integer so;

    @SerializedName("so_grp")
    @Expose
    @ColumnInfo(name = "so_grp")
    public Integer soGrp;

    @SerializedName("is_grp")
    @Expose
    @ColumnInfo(name = "is_grp")
    public Integer isGrp;

    @SerializedName("inc_lvl")
    @Expose
    @ColumnInfo(name = "inc_lvl")
    public Integer incLvl;

    @SerializedName("active")
    @Expose
    @ColumnInfo(name = "active")
    public Integer active;

    @SerializedName("timeout")
    @Expose
    @ColumnInfo(name = "timeout")
    public Integer timeout;

    @SerializedName("duration_plan")
    @Expose
    @ColumnInfo(name = "duration_plan")
    public Integer durationPlan;
}
