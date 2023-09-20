package ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.PlanogrammSDB;

public class PlanogrammResponse {
    @SerializedName("state")
    public Boolean state;

    @SerializedName("list")
    public List<PlanogrammSDB> list;

    @SerializedName("error")
    public String error;
}
