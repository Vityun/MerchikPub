package ua.com.merchik.merchik.data.Database.Room.Chat;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
}
