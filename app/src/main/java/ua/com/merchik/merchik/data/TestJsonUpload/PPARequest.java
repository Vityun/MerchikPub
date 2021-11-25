package ua.com.merchik.merchik.data.TestJsonUpload;

import java.util.List;

public class PPARequest {

    public String mod;
    public String act;

    public String ispolnitel_company_id;    // - по фирме исполнителю (на текущий момент фильтр игнорируется, но в ближайшее время Петров хочет, чтобы он влиял на результат)
    public String client_id;                // - клиент
//    public String client_id;                // - город
    public String group_id;                 // - группа
//    public String group_id;                 // - адрес
    public String tt_type_id;               // - тип ТТ
    public String client_tovar_group_id;    // - группа товаров клиента
    public String tovar_manufacturer_id;    // - производитель товара
    public String ppa_active_only;          // - только активные (включенные в состояние 1) позиции ППА (эта штука тебе в теории может сэкономить ресурсы, если на стороне приложения не нужны отключенные позиции)
    public List<String> code_iza;

    public PPARequest() {
    }
}
