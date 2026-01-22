package ua.com.merchik.merchik.data.UploadToServ;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UploadResponse {
    public boolean state;

    // mapping localId -> serverId
    public List<Item> data;

    // решение сервера по заявке (по serverId)
    public List<ResultItem> result;

    public static class Item {
        @SerializedName("element_id") public String elementId; // local ID
        @SerializedName("state") public boolean state;
        @SerializedName("id") public String serverId; // server ID (string)
    }

    public static class ResultItem {
        @SerializedName("ID") public String id;       // server ID (как "2973870")
        @SerializedName("state") public boolean state; // принято/отказано (или approved/declined)
        @SerializedName("comment") public String comment;
    }
}
