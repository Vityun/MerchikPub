package ua.com.merchik.merchik.data.WebSocketData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WebsocketParam {
    @SerializedName("mod")
    @Expose
    public String mod;

    @SerializedName("act")
    @Expose
    public String act;

    @SerializedName("host")
    @Expose
    public String host;

    @SerializedName("port")
    @Expose
    public Integer port;

    @SerializedName("token")
    @Expose
    public String token;

    @SerializedName("user_id")
    @Expose
    public Integer userId;

    @SerializedName("selector")
    @Expose
    public Selector selector;
}
