package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

//@Entity(tableName = "planogramm")
public class PlanogrammSDB {

    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @ColumnInfo(name = "ispId")
    public String ispId;

    @ColumnInfo(name = "ispTxt")
    public String ispTxt;

    @ColumnInfo(name = "clientId")
    public String clientId;

    @ColumnInfo(name = "clientTxt")
    public String clientTxt;

    @ColumnInfo(name = "imgId")
    public Integer imgId;

    @ColumnInfo(name = "photo")
    public String photo;

    @ColumnInfo(name = "photoId")
    public Long photoId;

    @ColumnInfo(name = "photoBig")
    public String photoBig;

    @ColumnInfo(name = "nm")
    public String nm;

    @ColumnInfo(name = "comments")
    public String comments;

    @ColumnInfo(name = "dtStart")
    public String dtStart;

    @ColumnInfo(name = "dtEnd")
    public String dtEnd;

    @ColumnInfo(name = "authorId")
    public String authorId;

    @ColumnInfo(name = "authorTxt")
    public String authorTxt;

    @ColumnInfo(name = "dtUpdate")
    public String dtUpdate;
}
