package ua.com.merchik.merchik.data.Database.Room.Planogram;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "planogram")
public class PlanogramSDB {

    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("isp_id")
    @Expose
    @ColumnInfo(name = "isp_id")
    public String ispId;

    @SerializedName("isp_txt")
    @Expose
    @ColumnInfo(name = "isp_txt")
    public String ispTxt;

    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public String clientId;

    @SerializedName("client_txt")
    @Expose
    @ColumnInfo(name = "client_txt")
    public String clientTxt;

    @SerializedName("img_id")
    @Expose
    @ColumnInfo(name = "img_id")
    public Integer imgId;

    @SerializedName("photo")
    @Expose
    @ColumnInfo(name = "photo")
    public String photo;

    @SerializedName("photo_id")
    @Expose
    @ColumnInfo(name = "photo_id")
    public Integer photoId;

    @SerializedName("photo_big")
    @Expose
    @ColumnInfo(name = "photo_big")
    public String photoBig;

    @SerializedName("nm")
    @Expose
    @ColumnInfo(name = "nm")
    public String nm;

    @SerializedName("comments")
    @Expose
    @ColumnInfo(name = "comments")
    public String comments;

    @SerializedName("dt_start")
    @Expose
    @ColumnInfo(name = "dt_start")
    public String dtStart;

    @SerializedName("dt_end")
    @Expose
    @ColumnInfo(name = "dt_end")
    public String dtEnd;

    @SerializedName("author_id")
    @Expose
    @ColumnInfo(name = "author_id")
    public Integer authorId;

    @SerializedName("author_txt")
    @Expose
    @ColumnInfo(name = "author_txt")
    public String authorTxt;

    @SerializedName("dt_update")
    @Expose
    @ColumnInfo(name = "dt_update")
    public String dtUpdate;
}
