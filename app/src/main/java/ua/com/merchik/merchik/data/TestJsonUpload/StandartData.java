package ua.com.merchik.merchik.data.TestJsonUpload;

import java.util.List;

public class StandartData<T> {

    public String mod;
    public String act;
    public String vpi;

    public String dt;
    public String id;
    public String id_list;      // Фильтр. список idшников фоток
    public String login;
    public String term;

    public List<Integer> addr_id;
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
    public String type;         // registration - при регистрации учетной записи recover  - при запросе восстановления пароля
    public String tp;           // тип для того что б получать данные о проценте рекламаций по Киеву и Регионам
    public String messenger_type;
    public String smeta;        // 12.06.23. Для Премиальны. Передаю смету для того что б потом по смете получить подробную информацию (в моём случае текст снижения)
    public long doc_type_id;    // 03.08.23 Добавлено для того что б открівалась подробная инфа о смете.
    public long vpo;
    // ------------------------------------

    public static class Filter {
        public String date_from;
        public String date_to;
        public String confirm;
        public String is_view;
    }

    /*Тут формируем оценки Достижений для передачи их на сторону сервера*/
    public static class ImagesAchieve {
        public Integer id;           // - ID достижения
        public Integer score;        // - оценка
        public String comment;       // - коммент к оценке
        public Integer element_id;   // - код элемента, по которому приложение будет отличать ответы
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

    public static class ReportPrepareServ {
        public String element_id; // id from RP
        public String dt;// время создания записи
        public String dt_report; // дата отчёта (YYYY-MM-DD)
        public String client_id; // код клиента
        public String tovar_id; // код товара
        public String addr_id; // код адреса
        public String price; // цена
        public String face; // кол фейсов
        public int amount; // количество товара
        public String dt_expire; // дата окончания срока годности (YYYY-MM-DD)
        public String expire_left; // кол товара с окончанием срока годности
        public String notes; // примечание
        public String up; // количество поднятого товара
        public String akciya; // признак наличия акции
        public String akciya_id; // код акции
        public String oborotved_num; // Остаток по оборотной ведомости
        public String error_id; // код ошибки
        public String error_comment; // комментарий к ошибке
        public String code_dad2; // код дад2
        public String buyer_order_id; // Номер заказа
    }
}




