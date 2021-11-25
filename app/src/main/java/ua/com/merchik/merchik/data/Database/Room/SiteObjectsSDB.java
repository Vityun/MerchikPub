package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "site_objects")
public class SiteObjectsSDB {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public int id;

    @SerializedName("additional_id")
    @Expose
    @ColumnInfo(name = "additional_id")
    public Integer additionalId;

    @SerializedName("ID_1c")
    @Expose
    @ColumnInfo(name = "ID_1c")
    public String iD1c;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("comments")
    @Expose
    @ColumnInfo(name = "comments")
    public String comments;

    @SerializedName("script_mod")
    @Expose
    @ColumnInfo(name = "script_mod")
    public String scriptMod;

    @SerializedName("script_act")
    @Expose
    @ColumnInfo(name = "script_act")
    public String scriptAct;

    @SerializedName("lesson_id")
    @Expose
    @ColumnInfo(name = "lesson_id")
    public String lessonId;

    @SerializedName("platform_id")
    @Expose
    @ColumnInfo(name = "platform_id")
    public String platformId;

    @SerializedName("object_type")
    @Expose
    @ColumnInfo(name = "object_type")
    public String objectType;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public String dtChange;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public String authorId;

    @SerializedName("nm_translation")
    @Expose
    @ColumnInfo(name = "nm_translation")
    public String nmTranslation;

    @SerializedName("comments_translation")
    @Expose
    @ColumnInfo(name = "comments_translation")
    public String commentsTranslation;

    @SerializedName("lang_id")
    @Expose
    @ColumnInfo(name = "lang_id")
    public String langId;
}
