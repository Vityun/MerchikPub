package ua.com.merchik.merchik.data.TestJsonUpload.StandartDataClasses;

public class TARUploadData {

    public Integer element_id; //- твой внутренний номер, под этим номером в ответе будут данные для каждого элемента
    public Integer theme_id; //- код темы
    public String date; // - дата в формате yyyy-mm-dd
    public Integer addr_id; // - код адреса
    public Integer user_id;
    public String client_id;
    public Integer vinovnik_id;
    public String comment; // - текст задачи \ рекламации
    public Integer photo_id; // - код фото, к которой относится задача или рекламация
    public String photo_hash;
    public String report_id; // - код отчёта, к которому относится задача или рекламация
    public Integer tp; // - тип элемента (0 - рекламация, 1 - задача)
}
