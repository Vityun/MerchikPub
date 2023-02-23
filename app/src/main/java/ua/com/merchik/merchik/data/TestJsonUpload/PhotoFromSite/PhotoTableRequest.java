package ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite;
/*Вот это мне надо*/
public class PhotoTableRequest {

    public String mod;
    public String act;
    public String tovar_only;    // Костыль для получения ТОЛЬКО товаров. Для этого передавать "1"
    public String nolimit;      // 1 - без лимитов, скинутся ВСЕ фото
    public String image_type;    // Тип фото(размер фото) - small или full

//    public List<String> id_list; // Фильтр. список idшников фоток
    public String id_list;      // Фильтр. список idшников фоток

    public String sotr_id;       // Фильтр. ID сотрудника по которому надо отобрать фото
    public String dt_upload;    // Время последнего изменения (для того что б не качать себе каждый раз фулл обьем)
    public String date_from;     // Фильтр. Дата с
    public String date_to;       // Фильтр. Дато по
    public String addrId;       // Фильтр. Адрес
    public String clientId;     // Фильтр. Клиент
    public String cityId;       // Фильтр. Город
    public String groupId;      // Фильтр. Группа(Например: АТБ, Сильпо, Ашан)
    public String photoType;    // Фильтр. Тип фото(передаётся ID типа фото)
    public String codeDAD2;     // Фильтр. Код ДАД 2 - реквизит по которому можно синхронизовать данные между таблицами

    public PhotoTableRequest() {
    }


}
