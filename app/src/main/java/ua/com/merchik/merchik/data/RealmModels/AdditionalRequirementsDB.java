package ua.com.merchik.merchik.data.RealmModels;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Date;

import io.realm.RealmObject;
import ua.com.merchik.merchik.data.Database.Room.AdditionalRequirementsDBOverride;
import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

public class AdditionalRequirementsDB extends RealmObject implements DataObjectUI {
    @SerializedName("ID")
    @Expose
    private Integer id;
    @SerializedName("site_id")
    @Expose
    private Integer siteId;
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("notes")
    @Expose
    private String notes;
    @SerializedName("client_id")
    @Expose
    private String clientId;
    @SerializedName("grp_id")
    @Expose
    private String grpId;
    @SerializedName("addr_id")
    @Expose
    private String addrId;
    @SerializedName("theme_id")
    @Expose
    private String themeId;
    @SerializedName("tovar_id")
    @Expose
    private String tovarId;
    @SerializedName("exam_id")
    @Expose
    private String examId;
    @SerializedName("option_id")
    @Expose
    private String optionId;
    @SerializedName("hide_client")
    @Expose
    private String hideClient;
    @SerializedName("hide_user")
    @Expose
    private String hideUser;

    @SerializedName("disable_score")
    @Expose
    public String disableScore;

    @SerializedName("not_approve")
    @Expose
    private String not_approve;

    @SerializedName("dt_start")
    @Expose
    public Date dtStart;

    @SerializedName("dt_end")
    @Expose
    public Date dtEnd;

    @SerializedName("author_id")
    @Expose
    private String authorId;
    @SerializedName("dt_change")
    @Expose
    private String dtChange;

    @SerializedName("user_id")
    @Expose
    public String userId;

    @SerializedName("addr_tt_id")
    @Expose
    public Integer addrTTId;

    @SerializedName("color")
    @Expose
    public String color;

    // Pika сумма премии по доп.треб.
    @SerializedName("summ")
    @Expose
    private String summ;

    @SerializedName("showcase_tp_id")
    @Expose
    public Integer showcaseTpId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getGrpId() {
        return grpId;
    }

    public void setGrpId(String grpId) {
        this.grpId = grpId;
    }

    public String getAddrId() {
        return addrId;
    }

    public void setAddrId(String addrId) {
        this.addrId = addrId;
    }

    public String getThemeId() {
        return themeId;
    }

    // Pika получение суммы премии по доп.треб.
    public String getSumm() { return summ; }
    public void setSumm(String sum) { this.summ=sum; }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public String getTovarId() {
        return tovarId;
    }

    public void setTovarId(String tovarId) {
        this.tovarId = tovarId;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getHideClient() {
        return hideClient;
    }

    public void setHideClient(String hideClient) {
        this.hideClient = hideClient;
    }

    public String getHideUser() {
        return hideUser;
    }

    public void setHideUser(String hideUser) {
        this.hideUser = hideUser;
    }

    public String getNot_approve() {
        return not_approve;
    }

    public void setNot_approve(String not_approve) {
        this.not_approve = not_approve;
    }

//    public String getDtStart() {
//        return dtStart;
//    }
//
//    public void setDtStart(String dtStart) {
//        this.dtStart = dtStart;
//    }
//
//    public String getDtEnd() {
//        return dtEnd;
//    }
//
//    public void setDtEnd(String dtEnd) {
//        this.dtEnd = dtEnd;
//    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getDtChange() {
        return dtChange;
    }

    public void setDtChange(String dtChange) {
        this.dtChange = dtChange;
    }

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return "addr_id, author_id, disable_score, dt_change, exam_id, grp_id, hide_client, " +
                "hide_user, ID, not_approve, options_id, showcase_tp_id, site_id, summ, theme_id," +
                "tovar_id, user_id, option_id, dt_end, dt_start, client_id, color";
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
        return AdditionalRequirementsDBOverride.INSTANCE.getValueModifier(key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getContainerModifier(@NonNull JSONObject jsonObject) {
        return AdditionalRequirementsDBOverride.INSTANCE.getContainerModifier(jsonObject);
    }

    @Nullable
    @Override
    public Integer getIdResImage() {
        return DataObjectUI.DefaultImpls.getIdResImage(this);
    }
}
