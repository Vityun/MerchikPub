package ua.com.merchik.merchik.data.RealmModels;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import ua.com.merchik.merchik.data.Database.Room.AddressSDBOverride;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;
import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

public class ReportPrepareDB extends RealmObject implements DataObjectUI {

    @SerializedName("ID")
    @Expose
    @PrimaryKey
    public Long iD;
    @SerializedName("dt")
    @Expose
    public String dt;
    @SerializedName("dt_report")
    @Expose
    public String dtReport;
    @SerializedName("time_from")
    @Expose
    public String timeFrom;
    @SerializedName("time_to")
    @Expose
    public String timeTo;
    @SerializedName("isp")
    @Expose
    public String isp;
    @SerializedName("kli")
    @Expose
    public String kli;
    @SerializedName("tovar_id")
    @Expose
    public String tovarId;
    @SerializedName("addr_id")
    @Expose
    public String addrId;
    @SerializedName("price")
    @Expose
    public String price;
    @SerializedName("price_min")
    @Expose
    public String priceMin;
    @SerializedName("price_max")
    @Expose
    public String priceMax;
    @SerializedName("face")
    @Expose
    public String face;
    @SerializedName("amount")
    @Expose
    public int amount;
    @SerializedName("up")
    @Expose
    public String up;
    @SerializedName("dt_expire")
    @Expose
    public String dtExpire;
    @SerializedName("expire_left")
    @Expose
    public String expireLeft;
    @SerializedName("notes")
    @Expose
    public String notes;
    @SerializedName("otchet_num")
    @Expose
    public String otchetNum;
    @SerializedName("otchet_unique")
    @Expose
    public String otchetUnique;
    @SerializedName("code_dad2")
    @Expose
    public String codeDad2;
    @SerializedName("otchet_tp")
    @Expose
    public String otchetTp;
    @SerializedName("price_copy")
    @Expose
    public String priceCopy;
    @SerializedName("merchik_id")
    @Expose
    public String merchikId;
    @SerializedName("author_id")
    @Expose
    public String authorId;
    @SerializedName("autofill_state")
    @Expose
    public String autofillState;
    @SerializedName("client_report")
    @Expose
    public String clientReport;

    @SerializedName("dt_change")
    @Expose
    public long dtChange;

    @SerializedName("akciya")
    @Expose
    public String akciya;
    @SerializedName("akciya_id")
    @Expose
    public String akciyaId;
    @SerializedName("tovar_error")
    @Expose
    public String tovarError;

    @SerializedName("oborotved_num")
    @Expose
    public String oborotvedNum;

    @SerializedName("oborotved_num_date")
    @Expose
    public String oborotved_num_date;

    @SerializedName("state")
    @Expose
    public String state;
    @SerializedName("error_id")
    @Expose
    public String errorId;
    @SerializedName("error_comment")
    @Expose
    public String errorComment;

    @SerializedName("buyer_order_id")
    @Expose
    public Integer buyerOrderId;

    @SerializedName("faces_plan")
    @Expose
    public Integer facesPlan;

    private int uploadStatus;       // Необходимость выгрузки записи
    private String serverResponce;      // Ответ от сервера (в основном тут будет ответ почему запись не принята сервером)

    /*
     * 04.01.2023.
     * Используется в опции контроля 157352
     * Если установить 1 -- значит есть какое-то нарушение по Товару.
     * */
    @Ignore
    public int errorExist;

    /*
     * 04.01.2023.
     * Используется в опции контроля 157352, 1455
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
     * Используется в опции контроля 157352, 1455
     * Кол. СКЮ. Заполняется единичкой, если заполнен фейс не равній нулю
     * */
    @Ignore
    public int colSKU;

    /*
     * 09.01.2023.
     * Используется в опции контроля 138644
     * Количество Товаров.
     * тут отметим те товары, у которых НЕ нулевой фейс - стоят на витрине
     * */
    @Ignore
    public int numberOfTovar;

    /*
     * 09.01.2023.
     * Используется в опции контроля 138644
     * Зачёт.
     * а тут отметим те товары которые стоят на витрине и у которых указан или подъем или "ошибка" - (причина НЕ подъема)
     * */
    @Ignore
    public int offset;

    /*
     * 19.01.23.
     * Используется в опции контроля 1455
     * Это у меня что-то типа JOIN. Потому что в Опции нужны данные Товаров
     * */
    @Ignore
    public TovarDB tovarDB;

    /*
     * 19.01.23.
     * Используется в опции контроля 1455
     * Это у меня что-то типа JOIN. Потому что в Опции нужны данные Групп Товаров из Товаров
     * */
    @Ignore
    public TovarGroupSDB tovarGroupSDB;

    /*
     * 19.01.23.
     * Используется в опции контроля 1455
     * Длина полочного пространства. Должна расчитываться: ( Товар.ширина * Фейс / 1000 )
     * */
    @Ignore
    public Double shelfSpaceLength;

    /*
     * 19.01.23.
     * Используется в опции контроля 1455
     * ДлинаППО. Общая ДЛИНА ПП всей категории в ТТ (включая конкурентов) (м)
     * */
    @Ignore
    public Double widthPPO;

    /*
     * 19.01.23.
     * Используется в опции контроля 1455
     * ДоляПлан. Плановая ДОЛЯ ПП которую ДОЛЖЕН занимать товар КЛИЕНТА в ТТ (%)
     * */
    @Ignore
    public Double plannedShare;

    /*
     * 19.01.23.
     * Используется в опции контроля 1455
     * ДоляФакт. Фактическая ДОЛЯ ПП которую ЗАНИМАЕТ товар КЛИЕНТА в ТТ (%)
     * */
    @Ignore
    public Double shareActual;

    /*
     * 19.01.23.
     * Используется в опции контроля 1455
     * Отклонение. Отклонение ФАКТИЧЕСКОЙ ДОЛИ ПП от ПЛАНОВОЙ (%)
     * */
    @Ignore
    public Double deflection;

    /*
     * 19.01.23.
     * Используется в опции контроля 1455
     * Недочёт.
     * */
    @Ignore
    public Integer deficit;

    /*
     * 01.05.23.
     * Используется в опции контроля 575
     * Реквизит для подсчета количества СКЮ по УЧЕТУ
     * */
    @Ignore
    public Integer numberSKUForAccounting;

    /*
     * 01.05.23.
     * Используется в опции контроля 575
     * Реквизит для подсчета количества СКЮ по ФАКТУ
     * */
    @Ignore
    public Integer numberSKUForFact;

    /*
     * 01.05.23.
     * Используется в опции контроля 575
     * Разница
     * */
    @Ignore
    public Integer difference;

    /*
     * 01.05.23.
     * Используется в опции контроля 575, 159707, 135591, 84005, 84967, 164985
     * Наруш. //признак ошибки
     * */
    @Ignore
    public Integer error;

    /*
     * 01.05.23.
     * Используется в опции контроля 575, 159707, 135591
     * Прим. //описание ошибки
     * */
    @Ignore
    public String errorNote;

    /*
    * 10.03.24.
    * Используется в опции контроля 84005, 84967, 164985
    * признак того, що ДОСГ зазначений
    * */
    @Ignore
    public Integer colTov;

    /*
     * 10.03.24.
     * Используется в опции контроля 84005, 84967, 164985
     * признак того, що ДОСГ зазначений
     * */
    @Ignore
    public Integer colDOSG;

    /*
     * 10.03.24.
     * Используется в опции контроля 84005, 84967, 164985
     * Особое Внимание
     * */
    @Ignore
    public int OSV;

    /*
     * 05.07.24.
     * Используется в опции контроля 80977
     * Что-то нашел
     * */
    @Ignore
    public int find;

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

    public long getDtChange() {
        return dtChange;
    }

    public void setDtChange(long dtChange) {
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

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return DataObjectUI.DefaultImpls.getHidedFieldsOnUI(this);
    }

    @Nullable
    @Override
    public Long getFieldTranslateId(@NonNull String key) {
        return AddressSDBOverride.INSTANCE.getTranslateId(key);
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
}
