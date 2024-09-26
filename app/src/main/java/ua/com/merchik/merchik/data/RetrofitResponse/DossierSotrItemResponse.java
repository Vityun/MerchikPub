package ua.com.merchik.merchik.data.RetrofitResponse;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ua.com.merchik.merchik.data.Database.Room.DossierSotrSDB;

public class DossierSotrItemResponse {

    @SerializedName("ID")
    @Expose
    @ColumnInfo(name = "ID")
    public Long id;

    @SerializedName("doc_num")
    @Expose
    @ColumnInfo(name = "doc_num")
    public String docNum;

    @SerializedName("theme_id")
    @Expose
    @ColumnInfo(name = "theme_id")
    public Long themeId;

    @SerializedName("controller_id")
    @Expose
    @ColumnInfo(name = "controller_id")
    public Long controllerId;

    @SerializedName("exam_id")
    @Expose
    @ColumnInfo(name = "exam_id")
    public String examId;

    @SerializedName("addr_id")
    @Expose
    @ColumnInfo(name = "addr_id")
    public Long addrId;

    @SerializedName("addr_tp_id")
    @Expose
    @ColumnInfo(name = "addr_tp_id")
    public Long addrTpId;

    @SerializedName("client_id")
    @Expose
    @ColumnInfo(name = "client_id")
    public String clientId;

    @SerializedName("staj_duration")
    @Expose
    @ColumnInfo(name = "staj_duration")
    public Long stajDuration;

    @SerializedName("notes")
    @Expose
    @ColumnInfo(name = "notes")
    public String notes;

    @SerializedName("status")
    @Expose
    @ColumnInfo(name = "status")
    public Long status;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public String dt;

    @SerializedName("coord_id")
    @Expose
    @ColumnInfo(name = "coord_id")
    public Long coordId;

    @SerializedName("priznak")
    @Expose
    @ColumnInfo(name = "priznak")
    public Long priznak;

    @SerializedName("dt_change")
    @Expose
    @ColumnInfo(name = "dt_change")
    public Long dtChange;

    @SerializedName("doljnost")
    @Expose
    @ColumnInfo(name = "doljnost")
    public Long doljnost;

    @SerializedName("option_id")
    @Expose
    @ColumnInfo(name = "option_id")
    public Long optionId;

    @SerializedName("stajirovka_id")
    @Expose
    @ColumnInfo(name = "stajirovka_id")
    public Long stajirovkaId;

    @SerializedName("lesson_id")
    @Expose
    @ColumnInfo(name = "lesson_id")
    public Long lessonId;

    @SerializedName("license")
    @Expose
    @ColumnInfo(name = "license")
    public Long license;

    @SerializedName("menu_template_id")
    @Expose
    @ColumnInfo(name = "menu_template_id")
    public Long menuTemplateId;

    @SerializedName("opinion_id")
    @Expose
    @ColumnInfo(name = "opinion_id")
    public Long opinionId;
}
