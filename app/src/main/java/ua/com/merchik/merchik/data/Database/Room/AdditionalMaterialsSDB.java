package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "additional_materials")
public class AdditionalMaterialsSDB {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("approve")
    @Expose
    @ColumnInfo(name = "approve")
    public String approve;

    @SerializedName("client")
    @Expose
    @ColumnInfo(name = "client")
    public String client;

    @SerializedName("user_id")
    @Expose
    @ColumnInfo(name = "user_id")
    public Integer user_id;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public String dt;

    @SerializedName("expire")
    @Expose
    @ColumnInfo(name = "expire")
    public String expire;

    @SerializedName("file_archive")
    @Expose
    @ColumnInfo(name = "file_archive")
    public String fileArchive;

    @SerializedName("txt")
    @Expose
    @ColumnInfo(name = "txt")
    public String txt;

    @SerializedName("file_ext")
    @Expose
    @ColumnInfo(name = "file_ext")
    public String fileExt;

    @SerializedName("file_size")
    @Expose
    @ColumnInfo(name = "file_size")
    public String fileSize;

    @SerializedName("score")
    @Expose
    @ColumnInfo(name = "score")
    public String score;

    @SerializedName("score_cnt")
    @Expose
    @ColumnInfo(name = "score_cnt")
    public String scoreCnt;

    @SerializedName("score_sum")
    @Expose
    @ColumnInfo(name = "score_sum")
    public String scoreSum;

    @SerializedName("state")
    @Expose
    @ColumnInfo(name = "state")
    public String state;

}
