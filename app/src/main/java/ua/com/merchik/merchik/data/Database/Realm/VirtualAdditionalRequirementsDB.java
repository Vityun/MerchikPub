package ua.com.merchik.merchik.data.Database.Realm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class VirtualAdditionalRequirementsDB{
    @SerializedName("ID")
    @Expose
    public Integer id;

    @SerializedName("site_id")
    @Expose
    public Integer siteId;

    @SerializedName("nm")
    @Expose
    public String nm;

    @SerializedName("notes")
    @Expose
    public String notes;

    @SerializedName("client_id")
    @Expose
    public String clientId;

    @SerializedName("grp_id")
    @Expose
    public String grpId;

    @SerializedName("addr_id")
    @Expose
    public String addrId;

    @SerializedName("theme_id")
    @Expose
    public String themeId;

    @SerializedName("tovar_id")
    @Expose
    public String tovarId;

    @SerializedName("exam_id")
    @Expose
    public String examId;

    @SerializedName("option_id")
    @Expose
    public String optionId;

    @SerializedName("hide_client")
    @Expose
    public String hideClient;

    @SerializedName("hide_user")
    @Expose
    public String hideUser;

    @SerializedName("not_approve")
    @Expose
    public String not_approve;

    @SerializedName("dt_start")
    @Expose
    public String dtStart;

    @SerializedName("dt_end")
    @Expose
    public String dtEnd;

    @SerializedName("author_id")
    @Expose
    public String authorId;

    @SerializedName("dt_change")
    @Expose
    public String dtChange;

    // -----

    // Тзн.НоваяКолонка("Оценка","Число",7,0);
	// Тзн.НоваяКолонка("ОтклОтСред","Число",7,1); //отклонение данной оценки от СРЕДНЕЙ (для того, чтобы ребята не ставили ОДНУ и ту-же оценку по всем ДТ)
	// Тзн.НоваяКолонка("ДатаОценки","Дата");
	// Тзн.НоваяКолонка("Недоч","Число",5,0);
	// Тзн.НоваяКолонка("Зачет","Число",5,0);
	// Тзн.НоваяКолонка("Примечание","Строка",300);



    public Integer mark;                // Оценка
    public Double deviationFromTheMean; // ОтклОтСред
    public Date markDate;               // ДатаОценки
    public Integer nedotoch;            // Недоч
    public Integer offset;              // Зачет
    public String note;                 // Примечание

}
