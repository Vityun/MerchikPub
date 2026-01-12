package ua.com.merchik.merchik.data.UploadToServ;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UploadResponse {
    public boolean state;
    public List<Item> data;

    public static class Item {
        @SerializedName("element_id") public String elementId; // это твой local ID
        @SerializedName("state") public boolean state;
        @SerializedName("id") public String serverId; // если нужно, сохрани отдельно
        @SerializedName("auto_approved") public boolean autoApproved; // автоподтверждение

    }
}
