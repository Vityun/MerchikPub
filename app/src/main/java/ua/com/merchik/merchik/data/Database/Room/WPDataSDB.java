package ua.com.merchik.merchik.data.Database.Room;

import androidx.room.Entity;

/**
 * 10.05.2021
 * Структура Плана Работ в SQL
 *
 *
 * */
@Entity(tableName = "wp_data")
public class WPDataSDB {

    public int ID;
    public String dt;
    public String client_id;
    public int isp;
    public String isp_fact;
    public int tech_sup_active;
    public int addr_id;
    public int user_id;
    public long dt_start;
    public long dt_stop;
    public int action;
    public int action_type;
    public String stajirovka_stage;
    public int one_time_work;
    public int theme_grp;
    public int theme_id;
    public long code_dda;
    public String code_ddas;
    public long codedad;
    public long code_dad2;
    public String smeta;
    public String smeta_1c;
    public String doc_num;
    public int doc_num_grp;
    public int doc_type;
    public String doc_num_1c;
    public long doc_num_1c_id;
    public String doc_num_otchet;
    public int signal_cnt;
    public long doc_num_otchet_id;
    public String smeta_active;
    public int super_id;
    public int territorial_id;
    public int regional_id;
    public int nop_id;
    public int starsh_tt_id;
    public int contacter_id;
    public int fot_user_id;
    public int dot_user_id;
    public long visit_start_dt;
    public Long visit_start_dt_receive;
    public int visit_start_geo_distance;
    public int visit_start_geo_accuracy;
    public int visit_start_geo_id;
    public long visit_end_dt;
    public Long visit_end_dt_receive;
    public int visit_end_geo_distance;
    public int visit_end_geo_accuracy;
    public int visit_end_geo_id;
    public int visit_arrive_dt;
    public int visit_arrive_geo_distance;
    public int visit_arrive_geo_accuracy;
    public int visit_arrive_geo_id;
    public int visit_report_starsh;
    public int visit_report_starsh_quality;
    public long client_start_dt;
    public Long client_start_dt_receive;
    public int client_start_geo_distance;
    public int client_start_geo_accuracy;
    public int client_start_geo_id;
    public int client_start_anybody;
    public long client_end_dt;
    public Long client_end_dt_receive;
    public int client_end_geo_distance;
    public int client_end_geo_accuracy;
    public int client_end_geo_id;
    public int client_end_anybody;
    public int client_report_starsh;
    public Long client_work_duration;  // продолжительность работ по клиенту
    public int priority;
    public int import_type;
    public long dt_update;
    public String code_aadd;
    public String work_stop_reason;
    public int simple_report;
    public int copy_price_days;
    public double cash_zakaz;
    public double cash_sum_30;
    public double cash_sum_addr_30;
    public double cash_ispolnitel;
    public int visit_per_week;
    public int sku;
    public int duration;
    public int mon;
    public int tue;
    public int wed;
    public int thu;
    public int fri;
    public int sat;
    public int sun;
    public String source_change;
    public int status;
    public int set_status;
    public String premiya_total;
    public String addr_location_xd;
    public String addr_location_yd;
    public String addr_txt;
    public String client_txt;
    public String user_txt;
    public String action_txt;
    public String action_short_txt;
    public String code_iza;
}
