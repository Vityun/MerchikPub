package ua.com.merchik.merchik.data.RetrofitResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SotrTableList {

    @SerializedName("user_id")
    @Expose
    private Integer user_id;
    @SerializedName("fio")
    @Expose
    private String fio;
    @SerializedName("client_id")
    @Expose
    private String clientId;
    @SerializedName("dt_update")
    @Expose
    private String dtUpdate;
    @SerializedName("author_id")
    @Expose
    private String authorId;
    @SerializedName("city_id")
    @Expose
    private String cityId;
    @SerializedName("work_addr_id")
    @Expose
    private String workAddrId;
    @SerializedName("inn")
    @Expose
    private String inn;
    @SerializedName("report_count")
    @Expose
    private Integer reportCount;
    @SerializedName("report_date_01")
    @Expose
    private String reportDate01;
    @SerializedName("report_date_05")
    @Expose
    private String reportDate05;
    @SerializedName("report_date_20")
    @Expose
    private String reportDate20;
    @SerializedName("report_date_40")
    @Expose
    private String reportDate40;


    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(String dtUpdate) {
        this.dtUpdate = dtUpdate;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getWorkAddrId() {
        return workAddrId;
    }

    public void setWorkAddrId(String workAddrId) {
        this.workAddrId = workAddrId;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public String getReportDate01() {
        return reportDate01;
    }

    public void setReportDate01(String reportDate01) {
        this.reportDate01 = reportDate01;
    }

    public String getReportDate05() {
        return reportDate05;
    }

    public void setReportDate05(String reportDate05) {
        this.reportDate05 = reportDate05;
    }

    public String getReportDate20() {
        return reportDate20;
    }

    public void setReportDate20(String reportDate20) {
        this.reportDate20 = reportDate20;
    }

    public String getReportDate40() {
        return reportDate40;
    }

    public void setReportDate40(String reportDate40) {
        this.reportDate40 = reportDate40;
    }
}
