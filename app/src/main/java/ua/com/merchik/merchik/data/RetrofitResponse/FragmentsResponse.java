package ua.com.merchik.merchik.data.RetrofitResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;

public class FragmentsResponse {

    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("error")
    @Expose
    public String error;

    @SerializedName("list")
    @Expose
    public List<FragmentSDB> list = null;
}
