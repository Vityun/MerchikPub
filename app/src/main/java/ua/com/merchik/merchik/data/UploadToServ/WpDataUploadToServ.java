package ua.com.merchik.merchik.data.UploadToServ;

public class WpDataUploadToServ {

    private String element_id;
    private String code_dad2;       //
    private String user_id;         //
    private String client_id;       //
    private String isp;             //

    private String visit_start_dt;  //
    private String visit_end_dt;    //
    private String client_start_dt; //
    private String client_end_dt;   //

    private String status_set;   //

    public WpDataUploadToServ() {
    }

    public WpDataUploadToServ(String element_id, String code_dad2, String user_id, String client_id, String isp, String visit_start_dt, String visit_end_dt, String client_start_dt, String client_end_dt, String status_set) {
        this.element_id = element_id;
        this.code_dad2 = code_dad2;
        this.user_id = user_id;
        this.client_id = client_id;
        this.isp = isp;
        this.visit_start_dt = visit_start_dt;
        this.visit_end_dt = visit_end_dt;
        this.client_start_dt = client_start_dt;
        this.client_end_dt = client_end_dt;
        this.status_set = status_set;
    }

    public String getElement_id() {
        return element_id;
    }

    public void setElement_id(String element_id) {
        this.element_id = element_id;
    }

    public String getCode_dad2() {
        return code_dad2;
    }

    public void setCode_dad2(String code_dad2) {
        this.code_dad2 = code_dad2;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getVisit_start_dt() {
        return visit_start_dt;
    }

    public void setVisit_start_dt(String visit_start_dt) {
        this.visit_start_dt = visit_start_dt;
    }

    public String getVisit_end_dt() {
        return visit_end_dt;
    }

    public void setVisit_end_dt(String visit_end_dt) {
        this.visit_end_dt = visit_end_dt;
    }

    public String getClient_start_dt() {
        return client_start_dt;
    }

    public void setClient_start_dt(String client_start_dt) {
        this.client_start_dt = client_start_dt;
    }

    public String getClient_end_dt() {
        return client_end_dt;
    }

    public void setClient_end_dt(String client_end_dt) {
        this.client_end_dt = client_end_dt;
    }
}
