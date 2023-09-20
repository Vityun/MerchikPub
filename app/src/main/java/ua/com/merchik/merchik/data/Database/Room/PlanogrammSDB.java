package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "planogramm")
public class PlanogrammSDB {

    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    @SerializedName("ID")
    public Integer id;

    @ColumnInfo(name = "ispId")
    @SerializedName("ispId")
    public String ispId;

    @ColumnInfo(name = "ispTxt")
    @SerializedName("ispTxt")
    public String ispTxt;

    @ColumnInfo(name = "clientId")
    @SerializedName("clientId")
    public String clientId;

    @ColumnInfo(name = "clientTxt")
    @SerializedName("clientTxt")
    public String clientTxt;

    @ColumnInfo(name = "imgId")
    @SerializedName("imgId")
    public Integer imgId;

    @ColumnInfo(name = "photo")
    @SerializedName("photo")
    public String photo;

    @ColumnInfo(name = "photoId")
    @SerializedName("photoId")
    public Long photoId;

    @ColumnInfo(name = "photoBig")
    @SerializedName("photoBig")
    public String photoBig;

    @ColumnInfo(name = "nm")
    @SerializedName("nm")
    public String nm;

    @ColumnInfo(name = "comments")
    @SerializedName("comments")
    public String comments;

    @ColumnInfo(name = "dtStart")
    @SerializedName("dtStart")
    public String dtStart;

    @ColumnInfo(name = "dtEnd")
    @SerializedName("dtEnd")
    public String dtEnd;

    @ColumnInfo(name = "authorId")
    @SerializedName("authorId")
    public String authorId;

    @ColumnInfo(name = "authorTxt")
    @SerializedName("authorTxt")
    public String authorTxt;

    @ColumnInfo(name = "dtUpdate")
    @SerializedName("dtUpdate")
    public String dtUpdate;
}
