package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//@PrimaryKey(autoGenerate = true)
@Entity(tableName = "achievements", indices = {@Index(value = {"serverId"}, unique = true)})
public class AchievementsSDB {

    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public Integer id;

    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "serverId")
    @NonNull
    public Integer serverId;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public String dt;       // Формат YYYY-MM-dd HH:mm

    @SerializedName("dt_ut")
    @Expose
    @ColumnInfo(name = "dt_ut")
    public Long dt_ut;      // Дата dt, но в формате юникст тайма

    @SerializedName("img_before_id")
    @Expose
    @ColumnInfo(name = "img_before_id")
    public Integer imgBeforeId;

    @SerializedName("img_before")
    @Expose
    @ColumnInfo(name = "img_before")
    public String imgBefore;

    @SerializedName("img_before_big")
    @Expose
    @ColumnInfo(name = "img_before_big")
    public String imgBeforeBig;

    @SerializedName("img_after_id")
    @Expose
    @ColumnInfo(name = "img_after_id")
    public Integer imgAfterId;

    @SerializedName("img_after")
    @Expose
    @ColumnInfo(name = "img_after")
    public String imgAfter;

    @SerializedName("img_after_big")
    @Expose
    @ColumnInfo(name = "img_after_big")
    public String imgAfterBig;

    @SerializedName("score")
    @Expose
    @ColumnInfo(name = "score")
    public String score;

    @SerializedName("score_who_nm")
    @Expose
    @ColumnInfo(name = "score_who_nm")
    public String scoreWhoNm;

    @SerializedName("score_dt")
    @Expose
    @ColumnInfo(name = "score_dt")
    public String scoreDt;

    @SerializedName("adresa_nm")
    @Expose
    @ColumnInfo(name = "adresa_nm")
    public String adresaNm;

    @SerializedName("adresa_addr")
    @Expose
    @ColumnInfo(name = "adresa_addr")
    public String adresaAddr;

    @SerializedName("adresa_tp")
    @Expose
    @ColumnInfo(name = "adresa_tp")
    public String adresaTp;

    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public Integer addrId;

    @SerializedName("spiskli_nm")
    @Expose
    @ColumnInfo(name = "spiskli_nm")
    public String spiskliNm;

    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public String clientId;

    @SerializedName("code_dad2")
    @Expose
    @ColumnInfo(name = "code_dad2")
    public Long codeDad2;

    @SerializedName("user_id")
    @Expose
    @ColumnInfo(name = "user_id")
    public Integer userId;

    @SerializedName("sotr_fio")
    @Expose
    @ColumnInfo(name = "sotr_fio")
    public String sotrFio;

    @SerializedName("comment_dt")
    @Expose
    @ColumnInfo(name = "comment_dt")
    public String commentDt;

    @SerializedName("comment_txt")
    @Expose
    @ColumnInfo(name = "comment_txt")
    public String commentTxt;

    @SerializedName("comment_user")
    @Expose
    @ColumnInfo(name = "comment_user")
    public String commentUser;

    @SerializedName("prem_reason")
    @Expose
    @ColumnInfo(name = "prem_reason")
    public String premReason;

    @SerializedName("prem_amount")
    @Expose
    @ColumnInfo(name = "prem_amount")
    public String premAmount;

    @SerializedName("prem_amount_dt")
    @Expose
    @ColumnInfo(name = "prem_amount_dt")
    public String premAmountDt;

    @SerializedName("prem_sotr")
    @Expose
    @ColumnInfo(name = "prem_sotr")
    public String premSotr;

    @SerializedName("dvi")
    @Expose
    @ColumnInfo(name = "dvi")
    public Integer dvi;

    @SerializedName("confirm_state")
    @Expose
    @ColumnInfo(name = "confirm_state")
    public Integer confirmState;

    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "theme_id")
    public Integer themeId;

    // ---------- ДЛЯ ОПЦИИ КОНТРОЛЯ НАЧАЛО---------------

    @Ignore
    public Integer error;

    @Ignore
    public StringBuilder note;

    @Ignore
    public Integer currentVisit;

    // ---------- ДЛЯ ОПЦИИ КОНТРОЛЯ КОНЕЦ---------------
}
