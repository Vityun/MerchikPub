package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import ua.com.merchik.merchik.Activities.ReferencesActivity.Chat.ChatGrpJoinedTemp;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatGrpSDB;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatGrpTEMPSDB;

@Dao
public interface ChatGrpDao {
    @Query("SELECT * FROM chat_grp ORDER BY dt_last_update DESC")
    Single<List<ChatGrpSDB>> getAll();

    /**
     * Записываю данные о кол-ве чатов во временную таблицу
     * */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertInTemp(List<ChatGrpTEMPSDB> data);

    @Query("SELECT \n" +
            " tab.chat_id as chat_id\n" +
            ",SUM(tab.kol) as kol_vsego\n" +
            ",SUM(tab.mread) as kol_read\n" +
            ",SUM(tab.kol)-SUM(tab.mread) as kol_unread\n" +
            "FROM\n" +
            "(\n" +
            "    SELECT \n" +
            "     chg.id as chat_id\n" +
            "    ,msg.dt as dt\n" +
            "    ,msg.user_id as user_id\n" +
            "    ,msg.user_id_to as user_id_to\n" +
            "    ,msg.dt_read as dt_read\n" +
            "    ,1 as kol\n" +
            "    ,CASE WHEN msg.dt_read>0 THEN 1 ELSE 0 END as mread\n" +
            "    FROM chat_grp as chg\n" +
            "    INNER JOIN chat as msg ON (msg.chat_id=chg.id)\n" +
            ") as tab GROUP BY tab.chat_id;")
    Single<List<ChatGrpTEMPSDB>> getTempChatGrp();


    @Query("SELECT \n" +
            " chg.id as chatId\n" +
            ",chg.nm as nm\n" +
            ",chg.dt as dt\n" +
            ",chg.dt_last_update as lastUpdate\n" +
            ",chg.author_id as authorId\n" +
            ",chg.theme_id as themeId\n" +
            ",chg.addr_id as addrId \n" +
            ",chg.client_id as clientId\n" +
            ",chg.doc_id as docId\n" +
            ",chg.code_dad2 as codeDad2\n" +
            ",chg.code as code\n" +
            ",chg.last_msg as lastMsg\n" +
            ",tab.kol_vsego as kolVsego\n" +
            ",tab.kol_read as kolRead\n" +
            ",tab.kol_unread as kolUnread\n" +
            "FROM chat_grp as chg\n" +
            "INNER JOIN t_ch_msg_gr as tab ON (tab.chat_id=chg.id) ORDER BY dt DESC;")
    Single<List<ChatGrpJoinedTemp>> getChatGrpJoinedTemp();




    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<ChatGrpSDB> data);
}
