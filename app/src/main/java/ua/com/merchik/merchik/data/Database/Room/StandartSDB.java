package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "standart")
public class StandartSDB {

    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("content_id")
    @Expose
    @ColumnInfo(name = "content_id")
    public Integer contentId;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public Long dtChange;

    @SerializedName("ispolnitel_id")
    @Expose
    @ColumnInfo(name = "ispolnitel_id")
    public Integer ispolnitelId;

    @SerializedName("contacter_id")
    @Expose
    @ColumnInfo(name = "contacter_id")
    public Integer contacterId;

    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "theme_id")
    public Integer themeId;

    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public Integer addrId;

    @SerializedName("score")
    @Expose
    @ColumnInfo(name = "score")
    public Integer score;

    @SerializedName("score_user_id")
    @Expose
    @ColumnInfo(name = "score_user_id")
    public Integer scoreUserId;

    @SerializedName("score_dt")
    @Expose
    @ColumnInfo(name = "score_dt")
    public Long scoreDt;

//    @SerializedName("comment")
//    @Expose
//    @ColumnInfo(name = "comment")
//    public String comment;

    @SerializedName("doc_type")
    @Expose
    @ColumnInfo(name = "doc_type")
    public Integer docType;

    @SerializedName("doc_num")
    @Expose
    @ColumnInfo(name = "doc_num")
    public String docNum;

    @SerializedName("doc_id")
    @Expose
    @ColumnInfo(name = "doc_id")
    public Long docId;

    @SerializedName("code_dad2")
    @Expose
    @ColumnInfo(name = "code_dad2")
    public Long codeDad2;

    @SerializedName("complete")
    @Expose
    @ColumnInfo(name = "complete")
    public Integer complete;

    @SerializedName("read_user_id")
    @Expose
    @ColumnInfo(name = "read_user_id")
    public Integer readUserId;

    @SerializedName("read_dt")
    @Expose
    @ColumnInfo(name = "read_dt")
    public Long readDt;

    @SerializedName("so")
    @Expose
    @ColumnInfo(name = "so")
    public Integer so;

    @SerializedName("duration")
    @Expose
    @ColumnInfo(name = "duration")
    public Integer duration;

    @SerializedName("premium")
    @Expose
    @ColumnInfo(name = "premium")
    public Integer premium;
}
