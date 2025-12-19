package ua.com.merchik.merchik.data.RetrofitResponse.tables;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.OpinionThemeSDB;

public class RegistrationResponse {

    @SerializedName("state")
    @Expose
    public Boolean state;
    @SerializedName("notice")
    @Expose
    public String notice;
    @SerializedName("fio")
    @Expose
    public String fio;

}
