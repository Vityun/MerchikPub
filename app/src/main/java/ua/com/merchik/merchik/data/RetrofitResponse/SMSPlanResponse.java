package ua.com.merchik.merchik.data.RetrofitResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.SMS.SMSPlanSDB;

public class SMSPlanResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("list")
    @Expose
    public List<SMSPlanSDB> list;

    @SerializedName("error")
    @Expose
    public String error;
}
