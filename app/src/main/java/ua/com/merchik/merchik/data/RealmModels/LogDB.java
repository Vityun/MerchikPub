package ua.com.merchik.merchik.data.RealmModels;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import ua.com.merchik.merchik.features.main.LogDBOverride;
import ua.com.merchik.merchik.dataLayer.DataObjectUI;
import ua.com.merchik.merchik.dataLayer.model.MerchModifier;

public class LogDB extends RealmObject implements DataObjectUI{

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

    @NonNull
    @Override
    public String getHidedFieldsOnUI() {
        return LogDBOverride.INSTANCE.getHidedFieldsOnUI();
    }

    @Nullable
    @Override
    public Long getFieldTranslateId(@NonNull String key) {
        return LogDBOverride.INSTANCE.getTranslateId(key);
    }

    @NonNull
    @Override
    public String getValueUI(@NonNull String key, @NonNull Object value) {
        return LogDBOverride.INSTANCE.getValueUI(key, value);
    }

    @Nullable
    @Override
    public MerchModifier getFieldModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return LogDBOverride.INSTANCE.getFieldModifier(key, jsonObject);
    }

    @Nullable
    @Override
    public MerchModifier getValueModifier(@NonNull String key, @NonNull JSONObject jsonObject) {
        return LogDBOverride.INSTANCE.getValueModifier(key, jsonObject);
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

    @Nullable
    @Override
    public List<String> getFieldsForOrderOnUI() {
        return DataObjectUI.DefaultImpls.getFieldsForOrderOnUI(this);
    }
}
