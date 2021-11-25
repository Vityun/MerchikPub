package ua.com.merchik.merchik.data.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LogDB extends RealmObject {

    @PrimaryKey
    private Integer id;
    private Long dt_action;
    private String comments;
    private Integer tp;
    private String client_id;
    private Integer addr_id;
    private Long obj_id;
    private Integer author;
    private Long dt;
    private String session;
    private String obj_date;

    public LogDB() {
    }

    public LogDB(Integer id, Long dt_action, String comments, Integer tp, String client_id, Integer addr_id, Long obj_id, Integer author, Long dt, String session, String obj_date) {
        this.id = id;
        this.dt_action = dt_action;
        this.comments = comments;
        this.tp = tp;
        this.client_id = client_id;
        this.addr_id = addr_id;
        this.obj_id = obj_id;
        this.author = author;
        this.dt = dt;
        this.session = session;
        this.obj_date = obj_date;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getDt_action() {
        return dt_action;
    }

    public void setDt_action(Long dt_action) {
        this.dt_action = dt_action;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getTp() {
        return tp;
    }

    public void setTp(Integer tp) {
        this.tp = tp;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public Integer getAddr_id() {
        return addr_id;
    }

    public void setAddr_id(Integer addr_id) {
        this.addr_id = addr_id;
    }

    public Long getObj_id() {
        return obj_id;
    }

    public void setObj_id(Long obj_id) {
        this.obj_id = obj_id;
    }

    public Integer getAuthor() {
        return author;
    }

    public void setAuthor(Integer author) {
        this.author = author;
    }

    public Long getDt() {
        return dt;
    }

    public void setDt(Long dr) {
        this.dt = dr;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getObj_date() {
        return obj_date;
    }

    public void setObj_date(String obj_date) {
        this.obj_date = obj_date;
    }
}
