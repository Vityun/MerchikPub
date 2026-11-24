package ua.com.merchik.merchik.database.realm.tables;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;

import static org.junit.Assert.*;

public class TARCommentImportTest {
    @Test public void mixedBatchPreservesPhotoAndQueuedReplyAndCanBeReplayed() {
        Map<String, TARCommentsDB> stored = new HashMap<>();
        TARCommentsDB photo = row("10");
        photo.photo = "100";
        photo.photo_hash = "local-hash";
        stored.put(photo.id, photo);
        TARCommentsDB pending = row("20");
        pending.comment = "edited offline";
        pending.startUpdate = true;
        stored.put(pending.id, pending);

        TARCommentsDB first = row("30"), second = row("40");
        List<TARCommentsDB> response = Collections.unmodifiableList(
                Arrays.asList(row("10"), first, row("20"), row("30"), second));
        List<TARCommentsDB> insert = TARCommentsRealm.missingComments(response, stored::containsKey);
        assertEquals(Arrays.asList(first, second), insert);
        for (TARCommentsDB item : insert) stored.put(item.id, item);

        assertTrue(TARCommentsRealm.missingComments(response, stored::containsKey).isEmpty());
        assertEquals(5, response.size());
        assertEquals("100", stored.get("10").photo);
        assertEquals("local-hash", stored.get("10").photo_hash);
        assertTrue(stored.get("20").startUpdate);
        assertEquals("edited offline", stored.get("20").comment);
    }

    @Test public void rowsWithoutAnIdDoNotHideTheNextComment() {
        TARCommentsDB valid = row("50");
        assertEquals(Collections.singletonList(valid), TARCommentsRealm.missingComments(
                Arrays.asList(null, row(null), row(" "), valid), id -> false));
    }

    private static TARCommentsDB row(String id) {
        TARCommentsDB item = new TARCommentsDB();
        item.setID(id);
        return item;
    }
}
