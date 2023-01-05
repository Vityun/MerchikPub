package ua.com.merchik.merchik.data.RealmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class ReportPrepareDB extends RealmObject {

    @SerializedName("ID")
    @Expose
    @PrimaryKey
    private Long iD;
    @SerializedName("dt")
    @Expose
    private String dt;
    @SerializedName("dt_report")
    @Expose
    private String dtReport;
    @SerializedName("time_from")
    @Expose
    private String timeFrom;
    @SerializedName("time_to")
    @Expose
    private String timeTo;
    @SerializedName("isp")
    @Expose
    private String isp;
    @SerializedName("kli")
    @Expose
    private String kli;
    @SerializedName("tovar_id")
    @Expose
    private String tovarId;
    @SerializedName("addr_id")
    @Expose
    private String addrId;
    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("price_min")
    @Expose
    private String priceMin;
    @SerializedName("price_max")
    @Expose
    private String priceMax;
    @SerializedName("face")
    @Expose
    private String face;
    @SerializedName("amount")
    @Expose
    private int amount;
    @SerializedName("up")
    @Expose
    private String up;
    @SerializedName("dt_expire")
    @Expose
    private String dtExpire;
    @SerializedName("expire_left")
    @Expose
    private String expireLeft;
    @SerializedName("notes")
    @Expose
    private String notes;
    @SerializedName("otchet_num")
    @Expose
    private String otchetNum;
    @SerializedName("otchet_unique")
    @Expose
    private String otchetUnique;
    @SerializedName("code_dad2")
    @Expose
    private String codeDad2;
    @SerializedName("otchet_tp")
    @Expose
    private String otchetTp;
    @SerializedName("price_copy")
    @Expose
    private String priceCopy;
    @SerializedName("merchik_id")
    @Expose
    private String merchikId;
    @SerializedName("author_id")
    @Expose
    private String authorId;
    @SerializedName("autofill_state")
    @Expose
    private String autofillState;
    @SerializedName("client_report")
    @Expose
    private String clientReport;
    @SerializedName("dt_change")
    @Expose
    private String dtChange;
    @SerializedName("akciya")
    @Expose
    private String akciya;
    @SerializedName("akciya_id")
    @Expose
    private String akciyaId;
    @SerializedName("tovar_error")
    @Expose
    private String tovarError;
    @SerializedName("oborotved_num")
    @Expose
    private String oborotvedNum;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("error_id")
    @Expose
    private String errorId;
    @SerializedName("error_comment")
    @Expose
    private String errorComment;

    @SerializedName("buyer_order_id")
    @Expose
    public Integer buyerOrderId;

    @SerializedName("faces_plan")
    @Expose
    public Integer facesPlan;

    /*
    * 04.01.2023.
    * Используется в опции контроля 157352
    * Если установить 1 -- значит есть какое-то нарушение по Товару.
    * */
    @Ignore
    public int errorExist;

    /*
    * 04.01.2023.
    * Используется в опции контроля 157352
    * Примечание к Товару. Например: записывается что именно не понравилось.
    * */
    @Ignore
    public String note;

    /*
     * 04.01.2023.
     * Используется в опции контроля 157352
     * Количество исправлений
     * */
    @Ignore
    public int fixesNum;

    /*
     * 04.01.2023.
     * Используется в опции контроля 157352
     * Кол. СКЮ. Заполняется единичкой, если заполнен фейс не равній нулю
     * */
    @Ignore
    public int colSKU;

    private int uploadStatus;       // Необходимость выгрузки записи
    private String serverResponce;      // Ответ от сервера (в основном тут будет ответ почему запись не принята сервером)


    public ReportPrepareDB() {
    }


    public Long getID() {
        return iD;
    }

    public void setID(Long iD) {
        this.iD = iD;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getDtReport() {
        return dtReport;
    }

    public void setDtReport(String dtReport) {
        this.dtReport = dtReport;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getKli() {
        return kli;
    }

    public void setKli(String kli) {
        this.kli = kli;
    }

    public String getTovarId() {
        return tovarId;
    }

    public void setTovarId(String tovarId) {
        this.tovarId = tovarId;
    }

    public String getAddrId() {
        return addrId;
    }

    public void setAddrId(String addrId) {
        this.addrId = addrId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPriceMin() {
        return priceMin;
    }

    public void setPriceMin(String priceMin) {
        this.priceMin = priceMin;
    }

    public String getPriceMax() {
        return priceMax;
    }

    public void setPriceMax(String priceMax) {
        this.priceMax = priceMax;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getUp() {
        return up;
    }

    public void setUp(String up) {
        this.up = up;
    }

    public String getDtExpire() {
        return dtExpire;
    }

    public void setDtExpire(String dtExpire) {
        this.dtExpire = dtExpire;
    }

    public String getExpireLeft() {
        return expireLeft;
    }

    public void setExpireLeft(String expireLeft) {
        this.expireLeft = expireLeft;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getOtchetNum() {
        return otchetNum;
    }

    public void setOtchetNum(String otchetNum) {
        this.otchetNum = otchetNum;
    }

    public String getOtchetUnique() {
        return otchetUnique;
    }

    public void setOtchetUnique(String otchetUnique) {
        this.otchetUnique = otchetUnique;
    }

    public String getCodeDad2() {
        return codeDad2;
    }

    public void setCodeDad2(String codeDad2) {
        this.codeDad2 = codeDad2;
    }

    public String getOtchetTp() {
        return otchetTp;
    }

    public void setOtchetTp(String otchetTp) {
        this.otchetTp = otchetTp;
    }

    public String getPriceCopy() {
        return priceCopy;
    }

    public void setPriceCopy(String priceCopy) {
        this.priceCopy = priceCopy;
    }

    public String getMerchikId() {
        return merchikId;
    }

    public void setMerchikId(String merchikId) {
        this.merchikId = merchikId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAutofillState() {
        return autofillState;
    }

    public void setAutofillState(String autofillState) {
        this.autofillState = autofillState;
    }

    public String getClientReport() {
        return clientReport;
    }

    public void setClientReport(String clientReport) {
        this.clientReport = clientReport;
    }

    public String getDtChange() {
        return dtChange;
    }

    public void setDtChange(String dtChange) {
        this.dtChange = dtChange;
    }

    public String getAkciya() {
        return akciya;
    }

    public void setAkciya(String akciya) {
        this.akciya = akciya;
    }

    public String getAkciyaId() {
        return akciyaId;
    }

    public void setAkciyaId(String akciyaId) {
        this.akciyaId = akciyaId;
    }

    public String getTovarError() {
        return tovarError;
    }

    public void setTovarError(String tovarError) {
        this.tovarError = tovarError;
    }

    public String getOborotvedNum() {
        return oborotvedNum;
    }

    public void setOborotvedNum(String oborotvedNum) {
        this.oborotvedNum = oborotvedNum;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public String getErrorComment() {
        return errorComment;
    }

    public void setErrorComment(String errorComment) {
        this.errorComment = errorComment;
    }


    public int getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(int uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public String getServerResponce() {
        return serverResponce;
    }

    public void setServerResponce(String serverResponce) {
        this.serverResponce = serverResponce;
    }
}
