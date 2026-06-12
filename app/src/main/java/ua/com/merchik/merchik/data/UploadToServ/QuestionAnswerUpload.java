package ua.com.merchik.merchik.data.UploadToServ;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ua.com.merchik.merchik.data.QuestionAnswerDB;

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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


        data.question = item.getQuestion();
        data.answer = item.getAnswer();
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
//        data.objectDate = item.getObjectDate();
        data.objectDate = item.getObjectDate() > 0
                ? sdf.format(new Date(item.getObjectDate()))
                : "";
        data.elementId = String.valueOf(item.getId());
        data.optionId = item.getOptionId();
        data.mnenieId = item.getMnenieId();

        return data;
    }
}