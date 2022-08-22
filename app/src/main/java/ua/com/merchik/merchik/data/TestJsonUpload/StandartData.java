package ua.com.merchik.merchik.data.TestJsonUpload;

import java.util.List;

public class StandartData<T> {

    public String mod;
    public String act;
    public String vpi;

    public String dt;
    public String id;
    public String login;
    public String term;

    public Integer addr_id;
    public Double x;    // Координата Х
    public Double y;    // Координата Y

    public Integer option_id;   // На данный момоент используется в ЭКЛах
    public String sotr_id;      // Фильтр для получения &&&&&&&&&&&&&????????????)))))))))))))))
    public String user_id;
    public String lang_id;      // ID языка
    public String code_dad2;     // Код ДАД2
    public String tel_type;     // Номер телефона, если не определена будут использоваться оба номера сотрудника tel1, tel2
    public String photo_type;   // Типо фото
//    public String hash_list;    // - по хэшу фоток

    // Период за который надо получить данные
    public String date_from;
    public String date_to;


    public String dt_change_from;     // unixtime времени изменения
    public String dt_change_to;       // unixtime времени изменения

    // Эти касаются Оценок Доп. ТТребований
    public String dt_from;              // unixtime времени, с которого записи об оценках нужно получить(опционально)
    public String dt_to;                // unixtime времени, по которое записи об оценках нужно получить(опционально)

    public String active;       // Нужно для таблички Стандартов
    public String theme_id;     // Передача темы. Нужно для таблички Стандартов

    public String test;     // Отладочный. 03.11.2021. когда в ЗИР менялись поля.

    public List<T> data;
    public List<T> tovar_id;    // Тестовая хрень, надо поменять
    public List<String> hash_list;

    public String list_type;

    public Filter filter;

    public String nolimit;
    public String tovar_only;   //
    public String image_type;   // Тип фото. small/full для получения с сервера нужного размера фотку

    // ----------- registration -----------
    public String company_id;   // едрпоу
    public String company_name; // название компании
    public String confirmation_code;    // код подтверждения, полученный от администратора (если регистрация происходит на компанию, у которой в прошлом шаге confirmation=true)
    public String client_id;    // код клиента, на которого регистрируется сотрудник
    public String company_type; // может принимать значения new / existing если у тебя осуществляется подключение сотрудника к существующей компании, которая уже зарегистрирована в системе, то ты передаёшь эту переменную со значением existing если пользователь регистрирует новую компанию, которой ещё нет в системе, тогда параметр передаёшь со значение new
    // ------------------------------------

    public static class Filter {
        public String date_from;
        public String date_to;
    }

    public static class StandartDataChat {
        public Integer element_id;
        public Integer msg_id;
        public Integer chat_group;
        public Integer chat_msg;
        public Integer chat_person;
    }

    public static class StandartDataTARUpload {
        public Integer element_id;          // внутренний номер, под этим номером в ответе будут данные для каждого элемента
        public Long code_dad2;              // код дад2 задачи (не ID, а именно дад2)
        public Long dt_start_fact;          // фактическое время начала работ
        public Long dt_end_fact;            // фактическое время окончания работ
        public Integer vote_score;          // Оценка рекламации
        public Integer vinovnik_score;      // Оценка обьективности
        public String vinovnik_score_comment;
        public Integer sotr_opinion_id;     // id-шник мнения о задаче/рекламации
    }
}




