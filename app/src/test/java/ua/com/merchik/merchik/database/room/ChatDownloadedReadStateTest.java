package ua.com.merchik.merchik.database.room;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatSDB;
import ua.com.merchik.merchik.database.room.DaoInterfaces.ChatDao;
import static org.junit.Assert.*;

public class ChatDownloadedReadStateTest {
    @Test public void replayPreservesReadButRefreshesText() {
        MemoryDao dao = new MemoryDao();
        dao.rows.put(1, row(1, 123L, "old"));
        dao.insertDownloaded(Collections.singletonList(row(1, 0L, "updated")));
        assertEquals(Long.valueOf(123), dao.rows.get(1).dtRead);
        assertEquals("updated", dao.rows.get(1).msg);
    }
    @Test public void preservesIndependentNewAndRemoteReadMessages() {
        MemoryDao dao = new MemoryDao();
        List<ChatSDB> rows = new ArrayList<>();
        rows.add(row(1, null, "new"));
        rows.add(row(2, 456L, "read on server"));
        dao.insertDownloaded(rows);
        assertEquals(Long.valueOf(0), dao.rows.get(1).dtRead);
        assertEquals(Long.valueOf(456), dao.rows.get(2).dtRead);
    }
    @Test public void largeDeltaIsBatchedBelowSqliteParameterLimit() {
        MemoryDao dao = new MemoryDao();
        List<ChatSDB> rows = new ArrayList<>();
        for (int i = 1; i <= 1201; i++) {
            dao.rows.put(i, row(i, 123L, "old"));
            rows.add(row(i, 0L, "new"));
        }
        dao.insertDownloaded(rows);
        assertEquals(3, dao.lookups);
        assertEquals(1201, dao.rows.size());
        for (ChatSDB saved : dao.rows.values()) assertEquals(Long.valueOf(123), saved.dtRead);
    }
    @Test public void emptyDeltaDoesNotWrite() {
        MemoryDao dao = new MemoryDao();
        dao.insertDownloaded(Collections.emptyList());
        assertEquals(0, dao.lookups);
        assertEquals(0, dao.inserts);
    }
    @Test public void duplicateIdsWithinOneDeltaCannotResetRead() {
        MemoryDao dao = new MemoryDao();
        List<ChatSDB> rows = new ArrayList<>();
        rows.add(row(1, 123L, "first copy"));
        rows.add(row(1, 0L, "updated copy"));
        dao.insertDownloaded(rows);
        assertEquals(Long.valueOf(123), dao.rows.get(1).dtRead);
        assertEquals("updated copy", dao.rows.get(1).msg);
    }
    private static ChatSDB row(int id, Long readAt, String text) {
        ChatSDB row = new ChatSDB();
        row.id = id;
        row.chatId = 10;
        row.dtRead = readAt;
        row.msg = text;
        return row;
    }
    private static class MemoryDao implements ChatDao {
        final Map<Integer, ChatSDB> rows = new HashMap<>();
        int lookups;
        int inserts;
        @Override public List<ChatSDB> getByIds(List<Integer> ids) {
            assertTrue(ids.size() <= 500);
            lookups++;
            List<ChatSDB> found = new ArrayList<>();
            for (Integer id : ids) if (rows.containsKey(id)) found.add(rows.get(id));
            return found;
        }
        @Override public void insertAll(List<ChatSDB> data) {
            inserts++;
            for (ChatSDB row : data) rows.put(row.id, row);
        }
        @Override public Single<List<ChatSDB>> getAll() { throw new UnsupportedOperationException(); }
        @Override public Single<List<ChatSDB>> getAllById(int id) { throw new UnsupportedOperationException(); }
        @Override public Flowable<List<ChatSDB>> observeConversation(int id) { throw new UnsupportedOperationException(); }
        @Override public Completable insertData(List<ChatSDB> data) { throw new UnsupportedOperationException(); }
        @Override public int markRead(int chatId, int messageId, long readAt) { throw new UnsupportedOperationException(); }
    }
}
