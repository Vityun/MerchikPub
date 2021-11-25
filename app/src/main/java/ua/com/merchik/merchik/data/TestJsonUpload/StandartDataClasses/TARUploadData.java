package ua.com.merchik.merchik.data.TestJsonUpload.StandartDataClasses;

public class TARUploadData {

    public String element_id; //- твой внутренний номер, под этим номером в ответе будут данные для каждого элемента
    public String theme_id; //- код темы
    public String date; // - дата в формате yyyy-mm-dd
    public String addr_id; // - код адреса
    public String comment; // - текст задачи \ рекламации
    public String photo_id; // - код фото, к которой относится задача или рекламация
    public String report_id; // - код отчёта, к которому относится задача или рекламация
    public String tp; // - тип элемента (0 - рекламация, 1 - задача)
}
