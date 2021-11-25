package ua.com.merchik.merchik.data.RetrofitResponse.tables;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.LanguagesSDB;

/**
 * 10.05.2021
 * POJO Языков с сайта.
 * Получаемый список - таблица базы данных.
 * */
public class LanguagesResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;
    @SerializedName("list")
    @Expose
    public List<LanguagesSDB> list = null;
    @SerializedName("error")
    @Expose
    public String error;
}
