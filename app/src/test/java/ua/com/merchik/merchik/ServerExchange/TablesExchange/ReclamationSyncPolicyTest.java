package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.JsonObject;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;

import static org.junit.Assert.*;

public class ReclamationSyncPolicyTest {
    @Test public void initialDownloadDoesNotFilterOutOldVisits() {
        long from = FilteredSyncPolicy.requestWatermark(null, 1000L, false);
        JsonObject request = ReclamationSyncPolicy.request(from);
        assertEquals("reclamation", request.get("mod").getAsString());
        assertEquals("list", request.get("act").getAsString());
        assertEquals("0", request.get("dt_change_from").getAsString());
        assertEquals(3, request.size());
        assertFalse(request.has("filter"));
    }

    @Test public void nextDownloadUsesAccountScopedCursorWithOverlap() {
        String scope = FilteredSyncPolicy.accountScope(189722, "all");
        SynchronizationCursorSDB cursor = FilteredSyncPolicy.savedCursor(ReclamationSyncPolicy.RESOURCE, scope, 0, 1000L, 136, 136);
        assertEquals(880, cursor.vpi);
        assertEquals("880", ReclamationSyncPolicy.request(FilteredSyncPolicy.requestWatermark(cursor, 2000L, true))
                .get("dt_change_from").getAsString());
        assertNotEquals(scope, FilteredSyncPolicy.accountScope(261007, "all"));
        assertEquals(0, FilteredSyncPolicy.requestWatermark(cursor, 2000L, false));
    }

    @Test public void emptyDeltaDoesNotCreateOrAdvanceCheckpoint() {
        assertNull(FilteredSyncPolicy.savedCursor(ReclamationSyncPolicy.RESOURCE, "scope", 0, 1000L, 0, 0));
        assertNull(FilteredSyncPolicy.savedCursor(ReclamationSyncPolicy.RESOURCE, "scope", 880, 2000L, 136, 0));
    }

    @Test public void replayedNonemptyDeltaAdvancesCheckpoint() {
        SynchronizationCursorSDB cursor = FilteredSyncPolicy.savedCursor(ReclamationSyncPolicy.RESOURCE, "scope", 880, 2000L, 136, 1);
        assertEquals(1880, cursor.vpi);
        assertEquals(136, cursor.itemCount);
    }

    @Test public void newServerRecordIsPreserved() {
        TasksAndReclamationsSDB server = row();
        server.dtRealPost = 1793550381L;
        assertSame(server, ReclamationSyncPolicy.merge(server, null, 2000L));
    }

    @Test public void unsentCreationOrEditsAreNotOverwritten() {
        TasksAndReclamationsSDB local = row();
        local.uploadStatus = 1;
        local.comment = "local comment";
        local.photoHash = "local-photo";
        assertSame(local, ReclamationSyncPolicy.merge(row(), local, 2000L));
    }

    @Test public void pendingScoresOpinionsAndWorkTimesSurviveHistoryRefresh() {
        TasksAndReclamationsSDB local = pending();
        TasksAndReclamationsSDB server = row();
        server.dtRealPost = 1793550381L;
        TasksAndReclamationsSDB merged = ReclamationSyncPolicy.merge(server, local, 2000L);
        assertSame(server, merged);
        assertEquals(Long.valueOf(1793550381L), merged.dtRealPost);
        assertLocalUpdates(local, merged);
    }

    @Test public void ackDuringDownloadDoesNotRestoreStaleServerValues() {
        TasksAndReclamationsSDB local = pending();
        local.voteDtUpload = 2001L;
        assertLocalUpdates(local, ReclamationSyncPolicy.merge(row(), local, 2000L));
    }

    @Test public void previouslyAcknowledgedRowsReceiveServerUpdates() {
        TasksAndReclamationsSDB local = pending();
        local.voteDtUpload = 1000L;
        TasksAndReclamationsSDB server = row();
        server.voteScore = 4;
        TasksAndReclamationsSDB merged = ReclamationSyncPolicy.merge(server, local, 2000L);
        assertEquals(Integer.valueOf(4), merged.voteScore);
        assertEquals(Long.valueOf(1000L), merged.voteDtUpload);
    }

    @Test public void malformedResponseIsRejectedBeforePersistence() {
        for (Boolean state : new Boolean[]{null, false}) {
            expectInvalid(() -> ReferenceSyncPolicy.rows(state, null, Collections.singletonList(row()), r -> r.id));
        }
        expectInvalid(() -> ReferenceSyncPolicy.rows(true, "failure", Collections.singletonList(row()), r -> r.id));
        expectInvalid(() -> ReferenceSyncPolicy.rows(true, null, Arrays.asList(row(), row()), r -> r.id));
    }

    private static void expectInvalid(Runnable action) {
        try {
            action.run();
            fail("Invalid response was accepted");
        } catch (IllegalStateException expected) {
            // Validation must fail before any rows or download cursor are saved.
        }
    }

    private static TasksAndReclamationsSDB row() {
        TasksAndReclamationsSDB row = new TasksAndReclamationsSDB();
        row.id = 2682150;
        return row;
    }

    private static TasksAndReclamationsSDB pending() {
        TasksAndReclamationsSDB local = row();
        local.uploadStatus = 0;
        local.voteDtUpload = 0L;
        local.voteScore = 3;
        local.vinovnikScore = 2;
        local.vinovnikScoreComment = "pending comment";
        local.sotrOpinionId = 64;
        local.dt_start_fact = 1100L;
        local.dt_end_fact = 1200L;
        return local;
    }

    private static void assertLocalUpdates(TasksAndReclamationsSDB local, TasksAndReclamationsSDB merged) {
        assertEquals(local.voteScore, merged.voteScore);
        assertEquals(local.vinovnikScore, merged.vinovnikScore);
        assertEquals(local.vinovnikScoreComment, merged.vinovnikScoreComment);
        assertEquals(local.sotrOpinionId, merged.sotrOpinionId);
        assertEquals(local.dt_start_fact, merged.dt_start_fact);
        assertEquals(local.dt_end_fact, merged.dt_end_fact);
        assertEquals(local.voteDtUpload, merged.voteDtUpload);
        assertEquals(local.uploadStatus, merged.uploadStatus);
    }
}
