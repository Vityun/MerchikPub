package ua.com.merchik.merchik.data.UploadToServ;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ua.com.merchik.merchik.data.QuestionAnswerDB;
import ua.com.merchik.merchik.data.QuestionAnswerPaymentAmount;

public class QuestionAnswerUpload {

    @SerializedName("question")
    @Expose
    public String question;

    @SerializedName("answer")
    @Expose
    public String answer;

    @SerializedName("dt")
    @Expose
    public Long dt;

    @SerializedName("avg_answer")
    @Expose
    public String avgAnswer;

    @SerializedName("type_user")
    @Expose
    public Integer typeUser;

    @SerializedName("id_quest")
    @Expose
    public Integer idQuest;

    @SerializedName("comment")
    @Expose
    public String comment;

    @SerializedName("id_quest_com")
    @Expose
    public Integer idQuestCom;

    @SerializedName("object_id")
    @Expose
    public String objectId;

    @SerializedName("object_str")
    @Expose
    public String objectStr;

    @SerializedName("adr_id")
    @Expose
    public String adrId;

    @SerializedName("kli_id")
    @Expose
    public String kliId;

    @SerializedName("object_date")
    @Expose
    public String objectDate;

    @SerializedName("element_id")
    @Expose
    public String elementId;

    @SerializedName("option_id")
    @Expose
    public String optionId;

    @SerializedName("mnenie_id")
    @Expose
    public String mnenieId;

    public static QuestionAnswerUpload fromDb(QuestionAnswerDB item) {
        QuestionAnswerUpload data = new QuestionAnswerUpload();

        data.question = item.getQuestion();
        data.answer = QuestionAnswerPaymentAmount.forUpload(item.getIdQuest(), item.getAnswer());
        data.dt = item.getDt();
        data.avgAnswer = item.getAvgAnswer();
        data.typeUser = item.getTypeUser();
        data.idQuest = item.getIdQuest();
        data.comment = item.getComment();
        data.idQuestCom = item.getIdQuestCom();
        data.objectId = item.getObjectId();
        data.objectStr = item.getObjectStr();
        data.adrId = item.getAdrId();
        data.kliId = item.getKliId();
        data.objectDate = formatObjectDate(item.getObjectDate());
        data.elementId = String.valueOf(item.getId());
        data.optionId = item.getOptionId();
        data.mnenieId = item.getMnenieId();

        return data;
    }

    static String formatObjectDate(Long timestamp) {
        if (timestamp == null || timestamp <= 0) return "";
        // Local answers store Unix seconds; tolerate older millisecond values too.
        long millis = timestamp > 100_000_000_000L ? timestamp : timestamp * 1000L;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));
        return sdf.format(new Date(millis));
    }
}
