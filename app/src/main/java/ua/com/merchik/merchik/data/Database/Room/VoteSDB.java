package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "votes", indices = {@Index(value = {"serverId"}, unique = true)})
public class VoteSDB {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public Integer id;      // ВНИМАНИЕ. Это мой внутренний идентификатор

    @SerializedName("ID")
    @Expose
    @NonNull
    @ColumnInfo(name = "serverId")
    public Integer serverId;        // Это идентификатор сервера

    @ColumnInfo(name = "dt_upload")
    public Long dtUpload;        // Типо время выгрузки

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

    // ВыбКласс - фильтр по классу голоса (число):
// 0 - оценка по фото,
// 1 - оценка по дет. отчету,
// 2 - оценка по аудиофайлу
// 3 - оценка по достижению
// 4 - оценка по витрине (насправді це оцінка Ідентифікатора вітрини)
// 5 - оценка по планограмме (насправді це оцінка Ідентифікатора планограми) при цьіому можуть перевизначатись ДАД2 + Адр ... і т.і.
// 6 - оценка по планограмме (насправді це оцінка Ідентифікатора планограми) при цьіому унікальність (ФотоИД+ДАД2+Автора+Тема). Призначена для зберігання переліку Планограм, яка існувала на момент
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

    // Поля для опций
    @Ignore
    public int error;

    @Ignore
    public String note;

    @Ignore
    public String authorVote;


    public VoteSDB() {
    }

    public VoteSDB(@NonNull Integer id, @NonNull Integer serverId, Long dtUpload, String isp, String kli, Integer addrId, Long dt, Long df, Long photoId, Long codeDad2, Long codeIza, Integer voterId, Integer score, Integer merchik, String ip, Integer voteType, Integer voteClass, Integer themeId, Integer dtDay, Integer dtMonth, Integer dtYear, Integer cntrlDoc, Integer flag, String comments) {
        this.id = id;
        this.serverId = serverId;
        this.dtUpload = dtUpload != null ? dtUpload : 1; // Default value
        this.isp = isp;
        this.kli = kli;
        this.addrId = addrId;
        this.dt = dt;
        this.df = df;
        this.photoId = photoId;
        this.codeDad2 = codeDad2;
        this.codeIza = codeIza;
        this.voterId = voterId;
        this.score = score;
        this.merchik = merchik;
        this.ip = ip;
        this.voteType = voteType;
        this.voteClass = voteClass;
        this.themeId = themeId;
        this.dtDay = dtDay;
        this.dtMonth = dtMonth;
        this.dtYear = dtYear;
        this.cntrlDoc = cntrlDoc;
        this.flag = flag;
        this.comments = comments;
    }
}
