package ua.com.merchik.merchik.data.Database.Room.Chat;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ReportConductRecovery;
import ua.com.merchik.merchik.database.room.AppDatabase;
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
        try {
            ChatSDB chatSDB = new ChatSDB();
            chatSDB.id = chat.msgId;
            chatSDB.chatId = chat.chatId;
            chatSDB.dt = chat.dtUt;
            chatSDB.userId = chat.senderId;
            chatSDB.userIdTo = chat.recipientId;
            chatSDB.dtRead = chat.dtRead;
            chatSDB.msg = chat.msg;

            int user = Globals.getCurrentUserId();
            AppDatabase db = SQL_DB;
            String session = Globals.getCurrentSessionId();
            Completable.fromAction(() -> {
                        ReportConductRecovery.requireAccount(user, db, session);
                        db.runInTransaction(() -> {
                            ReportConductRecovery.requireAccount(user, db, session);
                            db.chatDao().insertDownloaded(Collections.singletonList(chatSDB));
                            ReportConductRecovery.stage(user, db, session, Collections.singletonList(chatSDB));
                            ReportConductRecovery.requireAccount(user, db, session);
                        });
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            ReportConductRecovery.wake();
                            Log.e("chatExchange", "onComplete()");
                            Globals.writeToMLOG("INFO", "saveChatFromWebSocket/startWebSocket/click/chat_message/onComplete", "OK");
                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                            Log.e("chatExchange", "Throwable e: " + e);
                            Globals.writeToMLOG("ERROR", "saveChatFromWebSocket/startWebSocket/click/chat_message/onError", "Throwable e: " + e);
                        }
                    });
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "saveChatFromWebSocket/startWebSocket/click/chat_message", "Exception e: " + e);
        }
    }
}
