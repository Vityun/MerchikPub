package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "votes", indices = {@Index(value = {"serverId"}, unique = true)})
public class VoteSDB {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    public Integer id;      // ВНИМАНИЕ. Это мой внутренний идентификатор

    @SerializedName("ID")
    @Expose
    @NonNull
    @ColumnInfo(name = "serverId")
    public Integer serverId;        // Это идентификатор сервера

    @SerializedName("isp")
    @Expose
    @ColumnInfo(name = "isp")
    public String isp;

    @SerializedName("kli")
    @Expose
    @ColumnInfo(name = "kli")
    public String kli;

    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public Integer addrId;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public Long dt;

    @SerializedName("df")
    @Expose
    @ColumnInfo(name = "df")
    public Long df;

    @SerializedName("photo_id")
    @Expose
    @ColumnInfo(name = "photo_id")
    public Long photoId;

    @SerializedName("code_dad2")
    @Expose
    @ColumnInfo(name = "code_dad2")
    public Long codeDad2;

    @SerializedName("code_iza")
    @Expose
    @ColumnInfo(name = "code_iza")
    public Long codeIza;

    @SerializedName("voter_id")
    @Expose
    @ColumnInfo(name = "voter_id")
    public Integer voterId;

    @SerializedName("score")
    @Expose
    @ColumnInfo(name = "score")
    public Integer score;

    @SerializedName("merchik")
    @Expose
    @ColumnInfo(name = "merchik")
    public Integer merchik;

    @SerializedName("ip")
    @Expose
    @ColumnInfo(name = "ip")
    public String ip;

    @SerializedName("vote_type")
    @Expose
    @ColumnInfo(name = "vote_type")
    public Integer voteType;

    @SerializedName("vote_class")
    @Expose
    @ColumnInfo(name = "vote_class")
    public Integer voteClass;

    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "theme_id")
    public Integer themeId;

    @SerializedName("dt_day")
    @Expose
    @ColumnInfo(name = "dt_day")
    public Integer dtDay;

    @SerializedName("dt_month")
    @Expose
    @ColumnInfo(name = "dt_month")
    public Integer dtMonth;

    @SerializedName("dt_year")
    @Expose
    @ColumnInfo(name = "dt_year")
    public Integer dtYear;

    @SerializedName("cntrl_doc")
    @Expose
    @ColumnInfo(name = "cntrl_doc")
    public Integer cntrlDoc;

    @SerializedName("flag")
    @Expose
    @ColumnInfo(name = "flag")
    public Integer flag;

    @SerializedName("comments")
    @Expose
    @ColumnInfo(name = "comments")
    public String comments;
}
