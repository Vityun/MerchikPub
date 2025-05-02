package ua.com.merchik.merchik.data.RetrofitResponse.models;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ua.com.merchik.merchik.data.Database.Room.DossierSotrSDB;

public class DossierSotrItemResponse {

    @SerializedName("ID")
    @Expose
    public Long id;

    @SerializedName("doc_num")
    @Expose
    public String docNum;

    @SerializedName("theme_id")
    @Expose
    public Long themeId;

    @SerializedName("controller_id")
    @Expose
    public Long controllerId;

    @SerializedName("exam_id")
    @Expose
    public String examId;

    @SerializedName("addr_id")
    @Expose
    public Long addrId;

    @SerializedName("addr_tp_id")
    @Expose
    public Long addrTpId;

    @SerializedName("client_id")
    @Expose
    public String clientId;

    @SerializedName("staj_duration")
    @Expose
    public Long stajDuration;

    @SerializedName("notes")
    @Expose
    public String notes;

    @SerializedName("status")
    @Expose
    public Long status;

    @SerializedName("dt")
    @Expose
    public String dt;

    @SerializedName("coord_id")
    @Expose
    public Long coordId;

    @SerializedName("priznak")
    @Expose
    public Long priznak;

    @SerializedName("dt_change")
    @Expose
    public Long dtChange;

    @SerializedName("doljnost")
    @Expose
    public Long doljnost;

    @SerializedName("option_id")
    @Expose
    public Long optionId;

    @SerializedName("stajirovka_id")
    @Expose
    public Long stajirovkaId;

    @SerializedName("lesson_id")
    @Expose
    public Long lessonId;

    @SerializedName("license")
    @Expose
    public Long license;

    @SerializedName("menu_template_id")
    @Expose
    public Long menuTemplateId;

    @SerializedName("opinion_id")
    @Expose
    public Long opinionId;
}
