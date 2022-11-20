package ua.com.merchik.merchik.data.Database.Room.Chat;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "t_ch_msg_gr")
public class ChatGrpTEMPSDB {

    @PrimaryKey
    @ColumnInfo(name = "chat_id")
    public Integer chatId;

    @ColumnInfo(name = "kol_vsego")
    public Integer sumAll;

    @ColumnInfo(name = "kol_read")
    public Integer sumRead;

    @ColumnInfo(name = "kol_unread")
    public Integer sumUnread;
}
