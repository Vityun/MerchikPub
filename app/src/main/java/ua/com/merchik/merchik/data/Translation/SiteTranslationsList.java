package ua.com.merchik.merchik.data.Translation;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SiteTranslationsList  extends RealmObject {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    private String iD;
    @SerializedName("num_1c")
    @Expose
    private String num1c;
    @SerializedName("internal_name")
    @Expose
    private String internalName;
    @SerializedName("lang_id")
    @Expose
    private String langId;
    @SerializedName("default_value")
    @Expose
    private String defaultValue;
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("script_mod")
    @Expose
    private String scriptMod;
    @SerializedName("script_act")
    @Expose
    private String scriptAct;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("platform_id")
    @Expose
    private String platformId;
    @SerializedName("dt_update")
    @Expose
    private String dtUpdate;

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public String getNum1c() {
        return num1c;
    }

    public void setNum1c(String num1c) {
        this.num1c = num1c;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getLangId() {
        return langId;
    }

    public void setLangId(String langId) {
        this.langId = langId;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(String dtUpdate) {
        this.dtUpdate = dtUpdate;
    }
}
