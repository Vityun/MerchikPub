package ua.com.merchik.merchik.data.UploadToServ;

public class LogUploadToServ {

    private String element_id;
    private String dt_action;       //
    private String comments;        //
    private String tp;              //
    private String client_id;       //
    private String addr_id;         //
    private String obj_id;          //
    private String upload_date;     //

    public LogUploadToServ() {
    }

    public LogUploadToServ(String element_id, String dt_action, String comments, String tp, String client_id, String addr_id, String obj_id, String upload_date) {
        this.element_id = element_id;
        this.dt_action = dt_action;
        this.comments = comments;
        this.tp = tp;
        this.client_id = client_id;
        this.addr_id = addr_id;
        this.obj_id = obj_id;
        this.upload_date = upload_date;
    }

    public String getElement_id() {
        return element_id;
    }

    public void setElement_id(String element_id) {
        this.element_id = element_id;
    }

    public String getDt_action() {
        return dt_action;
    }

    public void setDt_action(String dt_action) {
        this.dt_action = dt_action;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTp() {
        return tp;
    }

    public void setTp(String tp) {
        this.tp = tp;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getAddr_id() {
        return addr_id;
    }

    public void setAddr_id(String addr_id) {
        this.addr_id = addr_id;
    }

    public String getObj_id() {
        return obj_id;
    }

    public void setObj_id(String obj_id) {
        this.obj_id = obj_id;
    }

    public String getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(String upload_date) {
        this.upload_date = upload_date;
    }
}
