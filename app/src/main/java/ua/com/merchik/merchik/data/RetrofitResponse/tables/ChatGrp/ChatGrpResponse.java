package ua.com.merchik.merchik.data.RetrofitResponse.tables.ChatGrp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.Chat.ChatGrpSDB;

public class ChatGrpResponse {
    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("list")
    @Expose
    public List<ChatGrpSDB> list = null;

    @SerializedName("chat_person")
    @Expose
    public List<ChatPerson> chatPerson = null;
}
