package ua.com.merchik.merchik.data.UploadToServ;

public class ReportPrepareServ {

    private String element_id       ; // id from RP
    private String dt               ;// время создания записи
    private String dt_report        ; // дата отчёта (YYYY-MM-DD)
    private String client_id        ; // код клиента
    private String tovar_id         ; // код товара
    private String addr_id          ; // код адреса
    private String price            ; // цена
    private String face             ; // кол фейсов
    private int amount              ; // количество товара
    private String dt_expire        ; // дата окончания срока годности (YYYY-MM-DD)
    private String expire_left      ; // кол товара с окончанием срока годности
    private String notes            ; // примечание
    private String up               ; // количество поднятого товара
    private String akciya           ; // признак наличия акции
    private String akciya_id        ; // код акции
    private String oborotved_num    ; // Остаток по оборотной ведомости
    private String error_id         ; // код ошибки
    private String error_comment    ; // комментарий к ошибке
    private String code_dad2        ; // код дад2
    private String buyer_order_id   ; // Номер заказа

    public ReportPrepareServ() {
    }

    public ReportPrepareServ(String element_id, String dt, String dt_report, String client_id, String tovar_id, String addr_id, String price, String face, int amount, String dt_expire, String expire_left, String notes, String up, String akciya, String akciya_id, String oborotved_num, String error_id, String error_comment, String code_dad2, String buyer_order_id) {
        this.element_id = element_id;
        this.dt = dt;
        this.dt_report = dt_report;
        this.client_id = client_id;
        this.tovar_id = tovar_id;
        this.addr_id = addr_id;
        this.price = price;
        this.face = face;
        this.amount = amount;
        this.dt_expire = dt_expire;
        this.expire_left = expire_left;
        this.notes = notes;
        this.up = up;
        this.akciya = akciya;
        this.akciya_id = akciya_id;
        this.oborotved_num = oborotved_num;
        this.error_id = error_id;
        this.error_comment = error_comment;
        this.code_dad2 = code_dad2;
        this.buyer_order_id = buyer_order_id;
    }

    public String getElement_id() {
        return element_id;
    }

    public void setElement_id(String element_id) {
        this.element_id = element_id;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getDt_report() {
        return dt_report;
    }

    public void setDt_report(String dt_report) {
        this.dt_report = dt_report;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getTovar_id() {
        return tovar_id;
    }

    public void setTovar_id(String tovar_id) {
        this.tovar_id = tovar_id;
    }

    public String getAddr_id() {
        return addr_id;
    }

    public void setAddr_id(String addr_id) {
        this.addr_id = addr_id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
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

    public String getDt_expire() {
        return dt_expire;
    }

    public void setDt_expire(String dt_expire) {
        this.dt_expire = dt_expire;
    }

    public String getExpire_left() {
        return expire_left;
    }

    public void setExpire_left(String expire_left) {
        this.expire_left = expire_left;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getUp() {
        return up;
    }

    public void setUp(String up) {
        this.up = up;
    }

    public String getAkciya() {
        return akciya;
    }

    public void setAkciya(String akciya) {
        this.akciya = akciya;
    }

    public String getAkciya_id() {
        return akciya_id;
    }

    public void setAkciya_id(String akciya_id) {
        this.akciya_id = akciya_id;
    }

    public String getOborotved_num() {
        return oborotved_num;
    }

    public void setOborotved_num(String oborotved_num) {
        this.oborotved_num = oborotved_num;
    }

    public String getError_id() {
        return error_id;
    }

    public void setError_id(String error_id) {
        this.error_id = error_id;
    }

    public String getError_comment() {
        return error_comment;
    }

    public void setError_comment(String error_comment) {
        this.error_comment = error_comment;
    }

    public String getCode_dad2() {
        return code_dad2;
    }

    public void setCode_dad2(String code_dad2) {
        this.code_dad2 = code_dad2;
    }
}
