package ua.com.merchik.merchik.data.RealmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PPADB  extends RealmObject {

    @SerializedName("ID")
    @Expose
    @PrimaryKey
    private String id;
    @SerializedName("isp")
    @Expose
    private String isp;
    @SerializedName("client")
    @Expose
    private String client;
    @SerializedName("addr_id")
    @Expose
    private String addrId;
    @SerializedName("tovar_id")
    @Expose
    private String tovarId;
    @SerializedName("face_starsh")
    @Expose
    private String faceStarsh;
    @SerializedName("face_num")
    @Expose
    private String faceNum;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("state_marketing")
    @Expose
    private String stateMarketing;
    @SerializedName("ostatok")
    @Expose
    private String ostatok;
    @SerializedName("ostatok_act")
    @Expose
    private String ostatokAct;
    @SerializedName("dt_update")
    @Expose
    private String dtUpdate;
    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("ignore_pos")
    @Expose
    private String ignorePos;
    @SerializedName("code_iza")
    @Expose
    private String codeIza;

    public String getID() {
        return id;
    }

    public void setID(String iD) {
        this.id = iD;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getAddrId() {
        return addrId;
    }

    public void setAddrId(String addrId) {
        this.addrId = addrId;
    }

    public String getTovarId() {
        return tovarId;
    }

    public void setTovarId(String tovarId) {
        this.tovarId = tovarId;
    }

    public String getFaceStarsh() {
        return faceStarsh;
    }

    public void setFaceStarsh(String faceStarsh) {
        this.faceStarsh = faceStarsh;
    }

    public String getFaceNum() {
        return faceNum;
    }

    public void setFaceNum(String faceNum) {
        this.faceNum = faceNum;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateMarketing() {
        return stateMarketing;
    }

    public void setStateMarketing(String stateMarketing) {
        this.stateMarketing = stateMarketing;
    }

    public String getOstatok() {
        return ostatok;
    }

    public void setOstatok(String ostatok) {
        this.ostatok = ostatok;
    }

    public String getOstatokAct() {
        return ostatokAct;
    }

    public void setOstatokAct(String ostatokAct) {
        this.ostatokAct = ostatokAct;
    }

    public String getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(String dtUpdate) {
        this.dtUpdate = dtUpdate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIgnorePos() {
        return ignorePos;
    }

    public void setIgnorePos(String ignorePos) {
        this.ignorePos = ignorePos;
    }

    public String getCodeIza() {
        return codeIza;
    }

    public void setCodeIza(String codeIza) {
        this.codeIza = codeIza;
    }
}
