package ua.com.merchik.merchik.data.WebSocketData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WSChat {
    @SerializedName("dt_ut")
    @Expose
    public Long dtUt;   // unix

    @SerializedName("dt")
    @Expose
    public String dt;   //real time mask: 'hh:mm:ss'

    @SerializedName("sender_id")
    @Expose
    public Integer senderId;

    @SerializedName("sender")
    @Expose
    public String sender;

    @SerializedName("recipient_id")
    @Expose
    public Integer recipientId;

    @SerializedName("msg_id")
    @Expose
    public Integer msgId;

    @SerializedName("chat_id")
    @Expose
    public Long chatId;

    @SerializedName("dt_read")
    @Expose
    public Long dtRead;

    @SerializedName("msg")
    @Expose
    public String msg;

    @SerializedName("nm")
    @Expose
    public String nm;

    @SerializedName("theme")
    @Expose
    public String theme;

    @SerializedName("theme_id")
    @Expose
    public Integer themeId;

    @SerializedName("addr_id")
    @Expose
    public Integer addrId;

    @SerializedName("client_id")
    @Expose
    public String clientId;

    @SerializedName("doc_id")
    @Expose
    public Long docId;

    @SerializedName("code_dad2")
    @Expose
    public Long codeDad2;
}
