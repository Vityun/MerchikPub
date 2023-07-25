package ua.com.merchik.merchik.data.ServerLogin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserInfo {

    @SerializedName("fio")
    @Expose
    private String fio;
    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("tel")
    @Expose
    private String tel;

    @SerializedName("token")
    @Expose
    public String token;

    /**
     * Статус мерчика. Типо нашей компаннии или чужой
     * our  - свой
     * foreign - чужой
     * */
    @SerializedName("user_work_plan_status")
    @Expose
    public String user_work_plan_status;

    @SerializedName("report_count")
    @Expose
    private String reportCount;
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

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getReportCount() {
        return reportCount;
    }

    public void setReportCount(String reportCount) {
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
