package ua.com.merchik.merchik.data.RealmModels;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class WpDataDB extends RealmObject implements Serializable {

    @PrimaryKey
    private long ID;
    private Date dt;
    private String client_id;
    private String isp;
    private String isp_fact;
    private int tech_sup_active;
    private int addr_id;
    private int user_id;
    private long dt_start;
    private long dt_stop;
    private int action;
    private int action_type;
    private String stajirovka_stage;
    private int one_time_work;
    private int theme_grp;
    private int theme_id;
    private long code_dda;
    private String code_ddas;
    private long codedad;
    private long code_dad2;
    private String smeta;
    private String smeta_1c;
    private String doc_num;
    private int doc_num_grp;
    private int doc_type;
    private String doc_num_1c;
    private long doc_num_1c_id;
    private String doc_num_otchet;
    private int signal_cnt;
    private long doc_num_otchet_id;
    private String smeta_active;
    private int super_id;
    private int territorial_id;
    private int regional_id;
    private int nop_id;
    private int starsh_tt_id;
    private int contacter_id;
    private int fot_user_id;
    private int dot_user_id;
    private long visit_start_dt;
    private Long visit_start_dt_receive;
    private int visit_start_geo_distance;
    private int visit_start_geo_accuracy;
    private int visit_start_geo_id;
    private long visit_end_dt;
    private Long visit_end_dt_receive;
    private int visit_end_geo_distance;
    private int visit_end_geo_accuracy;
    private int visit_end_geo_id;
    private int visit_arrive_dt;
    private int visit_arrive_geo_distance;
    private int visit_arrive_geo_accuracy;
    private int visit_arrive_geo_id;
    private int visit_report_starsh;
    private int visit_report_starsh_quality;
    private long client_start_dt;
    private Long client_start_dt_receive;
    private int client_start_geo_distance;
    private int client_start_geo_accuracy;
    private int client_start_geo_id;
    private int client_start_anybody;
    private long client_end_dt;
    private Long client_end_dt_receive;
    private int client_end_geo_distance;
    private int client_end_geo_accuracy;
    private int client_end_geo_id;
    private int client_end_anybody;
    private int client_report_starsh;
    public Long client_work_duration;  // продолжительность работ по клиенту
    private int priority;
    private int import_type;
    private long dt_update;
    private String code_aadd;
    private String work_stop_reason;
    private int simple_report;
    private int copy_price_days;
    private double cash_zakaz;
    private double cash_sum_30;
    private double cash_sum_addr_30;
    private double cash_ispolnitel;
    private int visit_per_week;
    private int sku;
    private long duration;
    private int mon;
    private int tue;
    private int wed;
    private int thu;
    private int fri;
    private int sat;
    private int sun;
    private String source_change;
    private int status;
    private int set_status;
    private String premiya_total;
    private String addr_location_xd;
    private String addr_location_yd;
    private String addr_txt;
    private String client_txt;
    private String user_txt;
    private String action_txt;
    private String action_short_txt;
    private String code_iza;

    // 12.07.22. Добавлены поля для отписания комментария.
    public String user_comment;
    public int user_comment_author_id;
    public long user_comment_dt_update;

    // 19.09.22
    public int ptt_user_id; // Поле ПТТ-шника
    public double sku_plan;
    public double sku_fact;
    public double oos;

    public boolean kp;  // КП Команда на Проведение
    public boolean startUpdate;

    public double cash_fact;
    public double cash_penalty;

    public WpDataDB() {
    }

    public WpDataDB(long ID, Date dt, String client_id, String isp, String isp_fact, int tech_sup_active, int addr_id, int user_id, long dt_start, long dt_stop, int action, int action_type, String stajirovka_stage, int one_time_work, int theme_grp, int theme_id, long code_dda, String code_ddas, long codedad, long code_dad2, String smeta, String smeta_1c, String doc_num, int doc_num_grp, int doc_type, String doc_num_1c, long doc_num_1c_id, String doc_num_otchet, int signal_cnt, long doc_num_otchet_id, String smeta_active, int super_id, int territorial_id, int regional_id, int nop_id, int starsh_tt_id, int contacter_id, int fot_user_id, int dot_user_id, long visit_start_dt, int visit_start_geo_distance, int visit_start_geo_accuracy, int visit_start_geo_id, long visit_end_dt, int visit_end_geo_distance, int visit_end_geo_accuracy, int visit_end_geo_id, int visit_arrive_dt, int visit_arrive_geo_distance, int visit_arrive_geo_accuracy, int visit_arrive_geo_id, int visit_report_starsh, int visit_report_starsh_quality, long client_start_dt, int client_start_geo_distance, int client_start_geo_accuracy, int client_start_geo_id, int client_start_anybody, long client_end_dt, int client_end_geo_distance, int client_end_geo_accuracy, int client_end_geo_id, int client_end_anybody, int client_report_starsh, int priority, int import_type, long dt_update, String code_aadd, String work_stop_reason, int simple_report, int copy_price_days, double cash_zakaz, double cash_sum_30, double cash_sum_addr_30, double cash_ispolnitel, int visit_per_week, int sku, int duration, int mon, int tue, int wed, int thu, int fri, int sat, int sun, String source_change, int status, String premiya_total, String addr_location_xd, String addr_location_yd, String addr_txt, String client_txt, String user_txt, String action_txt, String action_short_txt) {
        this.ID = ID;
        this.dt = dt;
        this.client_id = client_id;
        this.isp = isp;
        this.isp_fact = isp_fact;
        this.tech_sup_active = tech_sup_active;
        this.addr_id = addr_id;
        this.user_id = user_id;
        this.dt_start = dt_start;
        this.dt_stop = dt_stop;
        this.action = action;
        this.action_type = action_type;
        this.stajirovka_stage = stajirovka_stage;
        this.one_time_work = one_time_work;
        this.theme_grp = theme_grp;
        this.theme_id = theme_id;
        this.code_dda = code_dda;
        this.code_ddas = code_ddas;
        this.codedad = codedad;
        this.code_dad2 = code_dad2;
        this.smeta = smeta;
        this.smeta_1c = smeta_1c;
        this.doc_num = doc_num;
        this.doc_num_grp = doc_num_grp;
        this.doc_type = doc_type;
        this.doc_num_1c = doc_num_1c;
        this.doc_num_1c_id = doc_num_1c_id;
        this.doc_num_otchet = doc_num_otchet;
        this.signal_cnt = signal_cnt;
        this.doc_num_otchet_id = doc_num_otchet_id;
        this.smeta_active = smeta_active;
        this.super_id = super_id;
        this.territorial_id = territorial_id;
        this.regional_id = regional_id;
        this.nop_id = nop_id;
        this.starsh_tt_id = starsh_tt_id;
        this.contacter_id = contacter_id;
        this.fot_user_id = fot_user_id;
        this.dot_user_id = dot_user_id;
        this.visit_start_dt = visit_start_dt;
        this.visit_start_geo_distance = visit_start_geo_distance;
        this.visit_start_geo_accuracy = visit_start_geo_accuracy;
        this.visit_start_geo_id = visit_start_geo_id;
        this.visit_end_dt = visit_end_dt;
        this.visit_end_geo_distance = visit_end_geo_distance;
        this.visit_end_geo_accuracy = visit_end_geo_accuracy;
        this.visit_end_geo_id = visit_end_geo_id;
        this.visit_arrive_dt = visit_arrive_dt;
        this.visit_arrive_geo_distance = visit_arrive_geo_distance;
        this.visit_arrive_geo_accuracy = visit_arrive_geo_accuracy;
        this.visit_arrive_geo_id = visit_arrive_geo_id;
        this.visit_report_starsh = visit_report_starsh;
        this.visit_report_starsh_quality = visit_report_starsh_quality;
        this.client_start_dt = client_start_dt;
        this.client_start_geo_distance = client_start_geo_distance;
        this.client_start_geo_accuracy = client_start_geo_accuracy;
        this.client_start_geo_id = client_start_geo_id;
        this.client_start_anybody = client_start_anybody;
        this.client_end_dt = client_end_dt;
        this.client_end_geo_distance = client_end_geo_distance;
        this.client_end_geo_accuracy = client_end_geo_accuracy;
        this.client_end_geo_id = client_end_geo_id;
        this.client_end_anybody = client_end_anybody;
        this.client_report_starsh = client_report_starsh;
        this.priority = priority;
        this.import_type = import_type;
        this.dt_update = dt_update;
        this.code_aadd = code_aadd;
        this.work_stop_reason = work_stop_reason;
        this.simple_report = simple_report;
        this.copy_price_days = copy_price_days;
        this.cash_zakaz = cash_zakaz;
        this.cash_sum_30 = cash_sum_30;
        this.cash_sum_addr_30 = cash_sum_addr_30;
        this.cash_ispolnitel = cash_ispolnitel;
        this.visit_per_week = visit_per_week;
        this.sku = sku;
        this.duration = duration;
        this.mon = mon;
        this.tue = tue;
        this.wed = wed;
        this.thu = thu;
        this.fri = fri;
        this.sat = sat;
        this.sun = sun;
        this.source_change = source_change;
        this.status = status;
        this.premiya_total = premiya_total;
        this.addr_location_xd = addr_location_xd;
        this.addr_location_yd = addr_location_yd;
        this.addr_txt = addr_txt;
        this.client_txt = client_txt;
        this.user_txt = user_txt;
        this.action_txt = action_txt;
        this.action_short_txt = action_short_txt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // !!! Удалил класс потому что один считался реалмовым чисто, а второй обычным. В принципе надо разобраться с этим
//        if (o == null || getClass() != o.getClass()) return false;
        if (o == null) return false;
        WpDataDB wpDataDB = (WpDataDB) o;
        return  isp == wpDataDB.isp &&
                user_id == wpDataDB.user_id &&
                code_dad2 == wpDataDB.code_dad2 &&
                client_id.equals(wpDataDB.client_id);
    }

/*    @Override
    public int hashCode() {
        return Objects.hash(client_id, isp, user_id, code_dad2);
    }*/

    public long getId() {
        return ID;
    }

    public void setId(int id) {
        this.ID = id;
    }

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
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

    public String getIsp_fact() {
        return isp_fact;
    }

    public void setIsp_fact(String isp_fact) {
        this.isp_fact = isp_fact;
    }

    public int getTech_sup_active() {
        return tech_sup_active;
    }

    public void setTech_sup_active(int tech_sup_active) {
        this.tech_sup_active = tech_sup_active;
    }

    public int getAddr_id() {
        return addr_id;
    }

    public void setAddr_id(int addr_id) {
        this.addr_id = addr_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public long getDt_start() {
        return dt_start;
    }

    public void setDt_start(long dt_start) {
        this.dt_start = dt_start;
    }

    public long getDt_stop() {
        return dt_stop;
    }

    public void setDt_stop(long dt_stop) {
        this.dt_stop = dt_stop;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getAction_type() {
        return action_type;
    }

    public void setAction_type(int action_type) {
        this.action_type = action_type;
    }

    public String getStajirovka_stage() {
        return stajirovka_stage;
    }

    public void setStajirovka_stage(String stajirovka_stage) {
        this.stajirovka_stage = stajirovka_stage;
    }

    public int getOne_time_work() {
        return one_time_work;
    }

    public void setOne_time_work(int one_time_work) {
        this.one_time_work = one_time_work;
    }

    public int getTheme_grp() {
        return theme_grp;
    }

    public void setTheme_grp(int theme_grp) {
        this.theme_grp = theme_grp;
    }

    public int getTheme_id() {
        return theme_id;
    }

    public void setTheme_id(int theme_id) {
        this.theme_id = theme_id;
    }

    public long getCode_dda() {
        return code_dda;
    }

    public void setCode_dda(long code_dda) {
        this.code_dda = code_dda;
    }

    public String getCode_ddas() {
        return code_ddas;
    }

    public void setCode_ddas(String code_ddas) {
        this.code_ddas = code_ddas;
    }

    public long getCodedad() {
        return codedad;
    }

    public void setCodedad(long codedad) {
        this.codedad = codedad;
    }

    public long getCode_dad2() {
        return code_dad2;
    }

    public void setCode_dad2(long code_dad2) {
        this.code_dad2 = code_dad2;
    }

    public String getSmeta() {
        return smeta;
    }

    public void setSmeta(String smeta) {
        this.smeta = smeta;
    }

    public String getSmeta_1c() {
        return smeta_1c;
    }

    public void setSmeta_1c(String smeta_1c) {
        this.smeta_1c = smeta_1c;
    }

    public String getDoc_num() {
        return doc_num;
    }

    public void setDoc_num(String doc_num) {
        this.doc_num = doc_num;
    }

    public int getDoc_num_grp() {
        return doc_num_grp;
    }

    public void setDoc_num_grp(int doc_num_grp) {
        this.doc_num_grp = doc_num_grp;
    }

    public int getDoc_type() {
        return doc_type;
    }

    public void setDoc_type(int doc_type) {
        this.doc_type = doc_type;
    }

    public String getDoc_num_1c() {
        return doc_num_1c;
    }

    public void setDoc_num_1c(String doc_num_1c) {
        this.doc_num_1c = doc_num_1c;
    }

    public long getDoc_num_1c_id() {
        return doc_num_1c_id;
    }

    public void setDoc_num_1c_id(long doc_num_1c_id) {
        this.doc_num_1c_id = doc_num_1c_id;
    }

    public String getDoc_num_otchet() {
        return doc_num_otchet;
    }

    public void setDoc_num_otchet(String doc_num_otchet) {
        this.doc_num_otchet = doc_num_otchet;
    }

    public int getSignal_cnt() {
        return signal_cnt;
    }

    public void setSignal_cnt(int signal_cnt) {
        this.signal_cnt = signal_cnt;
    }

    public long getDoc_num_otchet_id() {
        return doc_num_otchet_id;
    }

    public void setDoc_num_otchet_id(long doc_num_otchet_id) {
        this.doc_num_otchet_id = doc_num_otchet_id;
    }

    public String getSmeta_active() {
        return smeta_active;
    }

    public void setSmeta_active(String smeta_active) {
        this.smeta_active = smeta_active;
    }

    public int getSuper_id() {
        return super_id;
    }

    public void setSuper_id(int super_id) {
        this.super_id = super_id;
    }

    public int getTerritorial_id() {
        return territorial_id;
    }

    public void setTerritorial_id(int territorial_id) {
        this.territorial_id = territorial_id;
    }

    public int getRegional_id() {
        return regional_id;
    }

    public void setRegional_id(int regional_id) {
        this.regional_id = regional_id;
    }

    public int getNop_id() {
        return nop_id;
    }

    public void setNop_id(int nop_id) {
        this.nop_id = nop_id;
    }

    public int getStarsh_tt_id() {
        return starsh_tt_id;
    }

    public void setStarsh_tt_id(int starsh_tt_id) {
        this.starsh_tt_id = starsh_tt_id;
    }

    public int getContacter_id() {
        return contacter_id;
    }

    public void setContacter_id(int contacter_id) {
        this.contacter_id = contacter_id;
    }

    public int getFot_user_id() {
        return fot_user_id;
    }

    public void setFot_user_id(int fot_user_id) {
        this.fot_user_id = fot_user_id;
    }

    public int getDot_user_id() {
        return dot_user_id;
    }

    public void setDot_user_id(int dot_user_id) {
        this.dot_user_id = dot_user_id;
    }

    public long getVisit_start_dt() {
        return visit_start_dt;
    }

    public void setVisit_start_dt(long visit_start_dt) {
        this.visit_start_dt = visit_start_dt;
    }

    public int getVisit_start_geo_distance() {
        return visit_start_geo_distance;
    }

    public void setVisit_start_geo_distance(int visit_start_geo_distance) {
        this.visit_start_geo_distance = visit_start_geo_distance;
    }

    public int getVisit_start_geo_accuracy() {
        return visit_start_geo_accuracy;
    }

    public void setVisit_start_geo_accuracy(int visit_start_geo_accuracy) {
        this.visit_start_geo_accuracy = visit_start_geo_accuracy;
    }

    public int getVisit_start_geo_id() {
        return visit_start_geo_id;
    }

    public void setVisit_start_geo_id(int visit_start_geo_id) {
        this.visit_start_geo_id = visit_start_geo_id;
    }

    public long getVisit_end_dt() {
        return visit_end_dt;
    }

    public void setVisit_end_dt(long visit_end_dt) {
        this.visit_end_dt = visit_end_dt;
    }

    public int getVisit_end_geo_distance() {
        return visit_end_geo_distance;
    }

    public void setVisit_end_geo_distance(int visit_end_geo_distance) {
        this.visit_end_geo_distance = visit_end_geo_distance;
    }

    public int getVisit_end_geo_accuracy() {
        return visit_end_geo_accuracy;
    }

    public void setVisit_end_geo_accuracy(int visit_end_geo_accuracy) {
        this.visit_end_geo_accuracy = visit_end_geo_accuracy;
    }

    public int getVisit_end_geo_id() {
        return visit_end_geo_id;
    }

    public void setVisit_end_geo_id(int visit_end_geo_id) {
        this.visit_end_geo_id = visit_end_geo_id;
    }

    public int getVisit_arrive_dt() {
        return visit_arrive_dt;
    }

    public void setVisit_arrive_dt(int visit_arrive_dt) {
        this.visit_arrive_dt = visit_arrive_dt;
    }

    public int getVisit_arrive_geo_distance() {
        return visit_arrive_geo_distance;
    }

    public void setVisit_arrive_geo_distance(int visit_arrive_geo_distance) {
        this.visit_arrive_geo_distance = visit_arrive_geo_distance;
    }

    public int getVisit_arrive_geo_accuracy() {
        return visit_arrive_geo_accuracy;
    }

    public void setVisit_arrive_geo_accuracy(int visit_arrive_geo_accuracy) {
        this.visit_arrive_geo_accuracy = visit_arrive_geo_accuracy;
    }

    public int getVisit_arrive_geo_id() {
        return visit_arrive_geo_id;
    }

    public void setVisit_arrive_geo_id(int visit_arrive_geo_id) {
        this.visit_arrive_geo_id = visit_arrive_geo_id;
    }

    public int getVisit_report_starsh() {
        return visit_report_starsh;
    }

    public void setVisit_report_starsh(int visit_report_starsh) {
        this.visit_report_starsh = visit_report_starsh;
    }

    public int getVisit_report_starsh_quality() {
        return visit_report_starsh_quality;
    }

    public void setVisit_report_starsh_quality(int visit_report_starsh_quality) {
        this.visit_report_starsh_quality = visit_report_starsh_quality;
    }

    public long getClient_start_dt() {
        return client_start_dt;
    }

    public void setClient_start_dt(long client_start_dt) {
        this.client_start_dt = client_start_dt;
    }

    public int getClient_start_geo_distance() {
        return client_start_geo_distance;
    }

    public void setClient_start_geo_distance(int client_start_geo_distance) {
        this.client_start_geo_distance = client_start_geo_distance;
    }

    public int getClient_start_geo_accuracy() {
        return client_start_geo_accuracy;
    }

    public void setClient_start_geo_accuracy(int client_start_geo_accuracy) {
        this.client_start_geo_accuracy = client_start_geo_accuracy;
    }

    public int getClient_start_geo_id() {
        return client_start_geo_id;
    }

    public void setClient_start_geo_id(int client_start_geo_id) {
        this.client_start_geo_id = client_start_geo_id;
    }

    public int getClient_start_anybody() {
        return client_start_anybody;
    }

    public void setClient_start_anybody(int client_start_anybody) {
        this.client_start_anybody = client_start_anybody;
    }

    public long getClient_end_dt() {
        return client_end_dt;
    }

    public void setClient_end_dt(long client_end_dt) {
        this.client_end_dt = client_end_dt;
    }

    public int getClient_end_geo_distance() {
        return client_end_geo_distance;
    }

    public void setClient_end_geo_distance(int client_end_geo_distance) {
        this.client_end_geo_distance = client_end_geo_distance;
    }

    public int getClient_end_geo_accuracy() {
        return client_end_geo_accuracy;
    }

    public void setClient_end_geo_accuracy(int client_end_geo_accuracy) {
        this.client_end_geo_accuracy = client_end_geo_accuracy;
    }

    public int getClient_end_geo_id() {
        return client_end_geo_id;
    }

    public void setClient_end_geo_id(int client_end_geo_id) {
        this.client_end_geo_id = client_end_geo_id;
    }

    public int getClient_end_anybody() {
        return client_end_anybody;
    }

    public void setClient_end_anybody(int client_end_anybody) {
        this.client_end_anybody = client_end_anybody;
    }

    public int getClient_report_starsh() {
        return client_report_starsh;
    }

    public void setClient_report_starsh(int client_report_starsh) {
        this.client_report_starsh = client_report_starsh;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getImport_type() {
        return import_type;
    }

    public void setImport_type(int import_type) {
        this.import_type = import_type;
    }

    public long getDt_update() {
        return dt_update;
    }

    public void setDt_update(long dt_update) {
        this.dt_update = dt_update;
    }

    public String getCode_aadd() {
        return code_aadd;
    }

    public void setCode_aadd(String code_aadd) {
        this.code_aadd = code_aadd;
    }

    public String getWork_stop_reason() {
        return work_stop_reason;
    }

    public void setWork_stop_reason(String work_stop_reason) {
        this.work_stop_reason = work_stop_reason;
    }

    public int getSimple_report() {
        return simple_report;
    }

    public void setSimple_report(int simple_report) {
        this.simple_report = simple_report;
    }

    public int getCopy_price_days() {
        return copy_price_days;
    }

    public void setCopy_price_days(int copy_price_days) {
        this.copy_price_days = copy_price_days;
    }

    public double getCash_zakaz() {
        return cash_zakaz;
    }

    public void setCash_zakaz(double cash_zakaz) {
        this.cash_zakaz = cash_zakaz;
    }

    public double getCash_sum_30() {
        return cash_sum_30;
    }

    public void setCash_sum_30(double cash_sum_30) {
        this.cash_sum_30 = cash_sum_30;
    }

    public double getCash_sum_addr_30() {
        return cash_sum_addr_30;
    }

    public void setCash_sum_addr_30(double cash_sum_addr_30) {
        this.cash_sum_addr_30 = cash_sum_addr_30;
    }

    public double getCash_ispolnitel() {
        return cash_ispolnitel;
    }

    public void setCash_ispolnitel(double cash_ispolnitel) {
        this.cash_ispolnitel = cash_ispolnitel;
    }

    public int getVisit_per_week() {
        return visit_per_week;
    }

    public void setVisit_per_week(int visit_per_week) {
        this.visit_per_week = visit_per_week;
    }

    public int getSku() {
        return sku;
    }

    public void setSku(int sku) {
        this.sku = sku;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getMon() {
        return mon;
    }

    public void setMon(int mon) {
        this.mon = mon;
    }

    public int getTue() {
        return tue;
    }

    public void setTue(int tue) {
        this.tue = tue;
    }

    public int getWed() {
        return wed;
    }

    public void setWed(int wed) {
        this.wed = wed;
    }

    public int getThu() {
        return thu;
    }

    public void setThu(int thu) {
        this.thu = thu;
    }

    public int getFri() {
        return fri;
    }

    public void setFri(int fri) {
        this.fri = fri;
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
    }

    public int getSun() {
        return sun;
    }

    public void setSun(int sun) {
        this.sun = sun;
    }

    public String getSource_change() {
        return source_change;
    }

    public void setSource_change(String source_change) {
        this.source_change = source_change;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSetStatus() {
        return set_status;
    }

    public void setSetStatus(int setStatus) {
        this.set_status = setStatus;
    }

    public String getPremiya_total() {
        return premiya_total;
    }

    public void setPremiya_total(String premiya_total) {
        this.premiya_total = premiya_total;
    }

    public String getAddr_location_xd() {
        return addr_location_xd;
    }

    public void setAddr_location_xd(String addr_location_xd) {
        this.addr_location_xd = addr_location_xd;
    }

    public String getAddr_location_yd() {
        return addr_location_yd;
    }

    public void setAddr_location_yd(String addr_location_yd) {
        this.addr_location_yd = addr_location_yd;
    }

    public String getAddr_txt() {
        return addr_txt;
    }

    public void setAddr_txt(String addr_txt) {
        this.addr_txt = addr_txt;
    }

    public String getClient_txt() {
        return client_txt;
    }

    public void setClient_txt(String client_txt) {
        this.client_txt = client_txt;
    }

    public String getUser_txt() {
        return user_txt;
    }

    public void setUser_txt(String user_txt) {
        this.user_txt = user_txt;
    }

    public String getAction_txt() {
        return action_txt;
    }

    public void setAction_txt(String action_txt) {
        this.action_txt = action_txt;
    }

    public String getAction_short_txt() {
        return action_short_txt;
    }

    public void setAction_short_txt(String action_short_txt) {
        this.action_short_txt = action_short_txt;
    }

    public String getCode_iza() {
        return code_iza;
    }

    public void setCode_iza(String code_iza) {
        this.code_iza = code_iza;
    }
}
