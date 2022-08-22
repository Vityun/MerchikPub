package ua.com.merchik.merchik.data.WebSocketData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WebSocketData {

    /*
    * 1. chat_message == Сообщение приходящее в чат. (chat/WSChat)
    * 2. global_notice == Текстовая заметка. (text)
    * 3.
    * */

    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("mod")
    @Expose
    public String mod;

    @SerializedName("action")
    @Expose
    public String action;

    @SerializedName("chat")
    @Expose
    public WSChat chat;

    @SerializedName("text")
    @Expose
    public String text;
}
