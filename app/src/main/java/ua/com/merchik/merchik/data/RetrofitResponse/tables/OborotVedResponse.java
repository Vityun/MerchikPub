package ua.com.merchik.merchik.data.RetrofitResponse.tables;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.OborotVedSDB;

/**
 * 22.06.2021
 * POJO Оборотной ведомости
 * Получаемый список - таблица базы данных.
 * */
public class OborotVedResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;
    @SerializedName("list")
    @Expose
    public List<OborotVedSDB> list = null;
    @SerializedName("error")
    @Expose
    public String error;
}
