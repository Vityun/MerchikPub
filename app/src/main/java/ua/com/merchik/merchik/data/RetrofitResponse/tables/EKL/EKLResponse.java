package ua.com.merchik.merchik.data.RetrofitResponse.tables.EKL;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;

public class EKLResponse {
    @SerializedName("state")
    @Expose
    public boolean state;
    @SerializedName("list")
    @Expose
    public List<EKL_SDB> list = null;
}
