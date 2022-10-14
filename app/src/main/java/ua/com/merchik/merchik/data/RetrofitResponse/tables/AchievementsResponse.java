package ua.com.merchik.merchik.data.RetrofitResponse.tables;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB;

public class AchievementsResponse {
    @SerializedName("list")
    @Expose
    public List<AchievementsSDB> list = null;

    @SerializedName("page_total")
    @Expose
    public long pageTotal;

    @SerializedName("state")
    @Expose
    public boolean state;
}
