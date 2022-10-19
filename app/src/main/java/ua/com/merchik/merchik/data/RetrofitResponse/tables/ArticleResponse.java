package ua.com.merchik.merchik.data.RetrofitResponse.tables;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.ArticleSDB;

public class ArticleResponse {
    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("list")
    @Expose
    public List<ArticleSDB> list = null;

    @SerializedName("server_time")
    @Expose
    public long serverTime;
}
