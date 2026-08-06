package ua.com.merchik.merchik.data.Lessons.SiteHints;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

public class SiteHintsDB extends RealmObject implements DataObjectUI {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    private int id;
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("w_all")
    @Expose
    private String wAll;
    @SerializedName("w_kli")
    @Expose
    private String wKli;
    @SerializedName("w_our")
    @Expose
    private String wOur;
    @SerializedName("doljnosti")
    @Expose
    private String doljnosti;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("html")
    @Expose
    private String html;
    @SerializedName("platform_id")
    @Expose
    private String platformId;
    @SerializedName("dt")
    @Expose
    private String dt;

    public int getID() {
        return id;
    }

    public void setID(int iD) {
        this.id = iD;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getWAll() {
        return wAll;
    }

    public void setWAll(String wAll) {
        this.wAll = wAll;
    }

    public String getWKli() {
        return wKli;
    }

    public void setWKli(String wKli) {
        this.wKli = wKli;
    }

    public String getWOur() {
        return wOur;
    }

    public void setWOur(String wOur) {
        this.wOur = wOur;
    }

    public String getDoljnosti() {
        return doljnosti;
    }

    public void setDoljnosti(String doljnosti) {
        this.doljnosti = doljnosti;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
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
        return R.drawable.youtube;
    }

    @NonNull
    @Override
    public String getFieldsImageOnUI() {
        return "url";
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
}
