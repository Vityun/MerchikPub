package ua.com.merchik.merchik.data.RetrofitResponse.tables.ReclamationPercentage;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.ReclamationPercentageSDB;

public class ReclamationPercentageResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("list")
    @Expose
    public List<ReclamationPercentageSDB> list = null;

    @SerializedName("error")
    @Expose
    public String error;
}
