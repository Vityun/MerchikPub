package ua.com.merchik.merchik.data.RetrofitResponse.tables;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.VoteSDB;

public class VoteResponse {
    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("error")
    @Expose
    public String error;

    @SerializedName("list")
    @Expose
    public List<VoteSDB> list = null;
}
