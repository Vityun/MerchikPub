package ua.com.merchik.merchik.data.TestJsonUpload.StratEndWork;


/**
 * Правки 09.04.2021.
 * Изначально обьект планировался для выгрузки только Начала и конца работ (в Плане Работ)
 * Теперь буду почучуть добавлять другие данные. Сейчас будет добавнен `status_set` - в принципе
 * новое поле в Плане работ - когда я создаю команду на проведение документа
 *
 * 18.02.2025 добавил комментарий и мнение
 * */
public class StartEndData {
    public String element_id;
    public String code_dad2;       //
    public String user_id;         //
    public String client_id;       //
    public String isp;             //
    public long dt_update;

    public String visit_start_dt;  //
    public String visit_end_dt;    //
    public String client_start_dt; //
    public String client_end_dt;   //

    public String client_work_duration;

//    public Integer user_opinion_id;

    public String user_comment;
    public int user_comment_author_id;
    public Long user_comment_dt_update;

    public String user_opinion_id;         // ID мнения
    public String user_opinion_author_id; // ID автора мнения
    public long user_opinion_dt_update;

    public String status_set;   //  Команда на проведение документа

}
