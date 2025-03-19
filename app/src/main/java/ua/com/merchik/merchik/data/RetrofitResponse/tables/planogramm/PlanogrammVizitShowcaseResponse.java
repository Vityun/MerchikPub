package ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB;

public class PlanogrammVizitShowcaseResponse {

    @SerializedName("state")
    public Boolean state;

    @SerializedName("list")
    public List<PlanogrammVizitShowcaseSDB> list;

    @SerializedName("error")
    public String error;

}
