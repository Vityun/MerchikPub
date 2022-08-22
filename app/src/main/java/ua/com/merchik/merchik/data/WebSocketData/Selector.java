package ua.com.merchik.merchik.data.WebSocketData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Selector {
    @SerializedName("platform_id")
    @Expose
    public Integer platformId;
}
