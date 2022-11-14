package ua.com.merchik.merchik.data.RetrofitResponse.tables.ChatGrp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChatPerson {
    @SerializedName("ID")
    @Expose
    public String id;

    @SerializedName("chat_id")
    @Expose
    public String chatId;

    @SerializedName("user_id")
    @Expose
    public String userId;
}
