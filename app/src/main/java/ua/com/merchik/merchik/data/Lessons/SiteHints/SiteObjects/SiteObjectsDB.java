package ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

//import androidx.room.PrimaryKey;

public class SiteObjectsDB extends RealmObject implements DataObjectUI {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    private int id;
    @SerializedName("ID_1c")
    @Expose
    private String iD1c;
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("comments")
    @Expose
    private String comments;
    @SerializedName("script_mod")
    @Expose
    private String scriptMod;
    @SerializedName("script_act")
    @Expose
    private String scriptAct;
    @SerializedName("lesson_id")
    @Expose
    private String lessonId;
    @SerializedName("platform_id")
    @Expose
    private String platformId;
    @SerializedName("object_type")
    @Expose
    private String objectType;
    @SerializedName("dt_change")
    @Expose
    private String dtChange;
    @SerializedName("author_id")
    @Expose
    private String authorId;
    @SerializedName("nm_translation")
    @Expose
    private String nmTranslation;
    @SerializedName("comments_translation")
    @Expose
    private String commentsTranslation;
    @SerializedName("lang_id")
    @Expose
    private String langId;




    public int getID() {
        return id;
    }

    public void setID(int iD) {
        this.id = iD;
    }

    public String getID1c() {
        return iD1c;
    }

    public void setID1c(String iD1c) {
        this.iD1c = iD1c;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getScriptMod() {
        return scriptMod;
    }

    public void setScriptMod(String scriptMod) {
        this.scriptMod = scriptMod;
    }

    public String getScriptAct() {
        return scriptAct;
    }

    public void setScriptAct(String scriptAct) {
        this.scriptAct = scriptAct;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getDtChange() {
        return dtChange;
    }

    public void setDtChange(String dtChange) {
        this.dtChange = dtChange;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getNmTranslation() {
        return nmTranslation;
    }

    public void setNmTranslation(String nmTranslation) {
        this.nmTranslation = nmTranslation;
    }

    public String getCommentsTranslation() {
        return commentsTranslation;
    }

    public void setCommentsTranslation(String commentsTranslation) {
        this.commentsTranslation = commentsTranslation;
    }

    public String getLangId() {
        return langId;
    }

    public void setLangId(String langId) {
        this.langId = langId;
    }

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return DataObjectUI.DefaultImpls.getHidedFieldsOnUI(this);
    }

    @Nullable
    @Override
    public Long getFieldTranslateId(@NonNull String key) {
        return DataObjectUI.DefaultImpls.getFieldTranslateId(this, key);
    }

    @NonNull
    @Override
    public String getValueUI(@NonNull String key, @NonNull Object value) {
        return DataObjectUI.DefaultImpls.getValueUI(this, key, value);
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
        return DataObjectUI.DefaultImpls.getContainerModifier(this, jsonObject);
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

}
