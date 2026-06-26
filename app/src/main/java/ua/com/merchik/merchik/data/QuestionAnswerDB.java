package ua.com.merchik.merchik.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;

import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;
import ua.com.merchik.merchik.features.main.QuestionAnswerDBOverride;


@Entity(tableName = "question_answers")
public class QuestionAnswerDB implements DataObjectUI {

    @PrimaryKey
    @ColumnInfo(name = "ID")
    @SerializedName("ID")
    @Expose
    private long id;

    @ColumnInfo(name = "user_id")
    @SerializedName("user_id")
    @Expose
    private Long userId;

    @ColumnInfo(name = "question")
    @SerializedName("question")
    @Expose
    private String question;

    @ColumnInfo(name = "answer")
    @SerializedName("answer")
    @Expose
    private String answer;

    @ColumnInfo(name = "dt")
    @SerializedName("dt")
    @Expose
    private Long dt;

    @ColumnInfo(name = "avg_answer")
    @SerializedName("avg_answer")
    @Expose
    private String avgAnswer;

    @ColumnInfo(name = "type_user")
    @SerializedName("type_user")
    @Expose
    private Integer typeUser;

    @ColumnInfo(name = "id_quest")
    @SerializedName("id_quest")
    @Expose
    private Integer idQuest;

    @ColumnInfo(name = "comment")
    @SerializedName("comment")
    @Expose
    private String comment;

    @ColumnInfo(name = "id_quest_com")
    @SerializedName("id_quest_com")
    @Expose
    private Integer idQuestCom;

    @ColumnInfo(name = "object_id")
    @SerializedName("object_id")
    @Expose
    private String objectId;

    @ColumnInfo(name = "object_str")
    @SerializedName("object_str")
    @Expose
    private String objectStr;

    @ColumnInfo(name = "adr_id")
    @SerializedName("adr_id")
    @Expose
    private String adrId;

    @ColumnInfo(name = "kli_id")
    @SerializedName("kli_id")
    @Expose
    private String kliId;

    @ColumnInfo(name = "object_date")
    @SerializedName("object_date")
    @Expose
    private Long objectDate;

    @ColumnInfo(name = "element_id")
    @SerializedName("element_id")
    @Expose
    private String elementId;

    @ColumnInfo(name = "option_id")
    @SerializedName("option_id")
    @Expose
    private String optionId;

    @ColumnInfo(name = "mnenie_id")
    @SerializedName("mnenie_id")
    @Expose
    private String mnenieId;

    @Ignore
    private String timeColor;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Long getDt() {
        return dt;
    }

    public void setDt(Long dt) {
        this.dt = dt;
    }

    public String getAvgAnswer() {
        return avgAnswer;
    }

    public void setAvgAnswer(String avgAnswer) {
        this.avgAnswer = avgAnswer;
    }

    public Integer getTypeUser() {
        return typeUser;
    }

    public void setTypeUser(Integer typeUser) {
        this.typeUser = typeUser;
    }

    public Integer getIdQuest() {
        return idQuest;
    }

    public void setIdQuest(Integer idQuest) {
        this.idQuest = idQuest;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getIdQuestCom() {
        return idQuestCom;
    }

    public void setIdQuestCom(Integer idQuestCom) {
        this.idQuestCom = idQuestCom;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectStr() {
        return objectStr;
    }

    public void setObjectStr(String objectStr) {
        this.objectStr = objectStr;
    }

    public String getAdrId() {
        return adrId;
    }

    public void setAdrId(String adrId) {
        this.adrId = adrId;
    }

    public String getKliId() {
        return kliId;
    }

    public void setKliId(String kliId) {
        this.kliId = kliId;
    }

    public Long getObjectDate() {
        return objectDate;
    }

    public void setObjectDate(Long objectDate) {
        this.objectDate = objectDate;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getMnenieId() {
        return mnenieId;
    }

    public void setMnenieId(String mnenieId) {
        this.mnenieId = mnenieId;
    }



    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return QuestionAnswerDBOverride.INSTANCE.getHidedFieldsOnUI();
    }

    @Nullable
    @Override
    public Long getFieldTranslateId(@NonNull String key) {
        return QuestionAnswerDBOverride.INSTANCE.getTranslateId(key);
    }

    @NonNull
    @Override
    public String getValueUI(@NonNull String key, @NonNull Object value) {
        return QuestionAnswerDBOverride.INSTANCE.getValueUI(key, value);
    }

    @Nullable
    @Override
    public MerchModifier getFieldModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getFieldModifier(this, key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getValueModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getValueModifier(this, key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getContainerModifier(@NonNull JSONObject jsonObject) {
        return QuestionAnswerDBOverride.INSTANCE.getContainerModifier(jsonObject);
    }

    @Nullable
    @Override
    public Integer getIdResImage() {
        return DataObjectUI.DefaultImpls.getIdResImage(this);
    }

    @NonNull
    @Override
    public String getFieldsImageOnUI() {
        return DataObjectUI.DefaultImpls.getFieldsImageOnUI(this);
    }

    @Nullable
    @Override
    public List<String> getFieldsForOrderOnUI() {
        return DataObjectUI.DefaultImpls.getFieldsForOrderOnUI(this);
    }

    @NonNull
    @Override
    public List<String> getPreferredFieldOrder() {
        return DataObjectUI.DefaultImpls.getPreferredFieldOrder(this);
    }

    @Override
    public @NotNull String getCommentsForImage() {
        return DataObjectUI.DefaultImpls.getCommentsForImage(this);
    }


    @Override
    public @Nullable String getCommentForImageValue(@NotNull String key, @NotNull JSONObject jsonObject) {
        return DataObjectUI.DefaultImpls.getCommentForImageValue(this, key, jsonObject);
    }

    public String getTimeColor() {
        return timeColor;
    }

    public void setTimeColor(String timeColor) {
        this.timeColor = timeColor;
    }
}
