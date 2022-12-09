package ua.com.merchik.merchik.data.Database.Room.Chat;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;

import ua.com.merchik.merchik.data.WebSocketData.WSChat;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

@Entity(tableName = "chat")
public class ChatSDB {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName("chat_id")
    @Expose
    @ColumnInfo(name = "chat_id")
    public Integer chatId;

    @SerializedName("dt")
    @Expose
    @ColumnInfo(name = "dt")
    public Long dt;

    @SerializedName("user_id")
    @Expose
    @ColumnInfo(name = "user_id")
    public Integer userId;

    @SerializedName("user_id_to")
    @Expose
    @ColumnInfo(name = "user_id_to")
    public Integer userIdTo;

    @SerializedName("dt_read")
    @Expose
    @ColumnInfo(name = "dt_read")
    public Long dtRead;

    @SerializedName("msg")
    @Expose
    @ColumnInfo(name = "msg")
    public String msg;

    public void saveChatFromWebSocket(WSChat chat){
        ChatSDB chatSDB = new ChatSDB();
        chatSDB.id = chat.msgId;
        chatSDB.chatId = chat.chatId;
        chatSDB.dt = chat.dtUt;
        chatSDB.userId = chat.senderId;
        chatSDB.userIdTo = chat.recipientId;
        chatSDB.dtRead = chat.dtRead;
        chatSDB.msg = chat.msg;

        SQL_DB.chatDao().insertData(Collections.singletonList(chatSDB));
    }
}
