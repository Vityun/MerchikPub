package ua.com.merchik.merchik.data.synchronization;

import com.google.gson.JsonObject;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ChatSyncPolicyTest {
    @Test public void initialMessagesRequestIncludesZero() {
        JsonObject request = ChatSyncPolicy.request(TableName.CHAT, 0);
        assertEquals("chat", request.get("mod").getAsString());
        assertEquals("list_message", request.get("act").getAsString());
        assertEquals("0", request.get("dt_change_from").getAsString());
    }

    @Test public void initialGroupsRequestIncludesZero() {
        JsonObject request = ChatSyncPolicy.request(TableName.CHAT_GROUP, 0);
        assertEquals("chat", request.get("mod").getAsString());
        assertEquals("list", request.get("act").getAsString());
        assertEquals(0, request.get("dt_change_from").getAsLong());
    }

    @Test public void incrementalRequestsUseIndependentUnixSeconds() {
        assertEquals("1793263380", ChatSyncPolicy.request(TableName.CHAT, 1793263380L)
                .get("dt_change_from").getAsString());
        assertEquals("1793263000", ChatSyncPolicy.request(TableName.CHAT_GROUP, 1793263000L)
                .get("dt_change_from").getAsString());
    }

    @Test public void negativeWatermarkStartsAtZero() {
        assertEquals(0, ChatSyncPolicy.requestWatermark(-1, null));
        assertEquals(0, ChatSyncPolicy.request(TableName.CHAT, -1).get("dt_change_from").getAsLong());
    }

    @Test public void validSavedWatermarkIsUsedWithoutAnotherOverlap() {
        assertEquals(1000, ChatSyncPolicy.requestWatermark(1000, 2000L));
    }

    @Test public void futureSavedWatermarkIsCorrectedByTrustedClock() {
        assertEquals(1880, ChatSyncPolicy.requestWatermark(9000, 2000L));
    }

    @Test public void unknownClockDoesNotDiscardSavedWatermark() {
        assertEquals(9000, ChatSyncPolicy.requestWatermark(9000, null));
        assertEquals(9000, ChatSyncPolicy.savedWatermark(9000, null));
    }

    @Test public void firstDownloadWithoutTrustedClockRemainsFull() {
        assertEquals(0, ChatSyncPolicy.savedWatermark(0, null));
    }

    @Test public void successUsesRequestStartWithOverlapNotCompletionTime() {
        long start = 1793263500L;
        assertEquals(1793263380L, ChatSyncPolicy.savedWatermark(1793262000L, start));
    }

    @Test public void earlyClockDoesNotMakeNegativeWatermark() {
        assertEquals(0, ChatSyncPolicy.savedWatermark(0, 50L));
        assertEquals(0, ChatSyncPolicy.requestWatermark(200, 50L));
    }

    @Test public void successfulEmptyListIsValid() {
        assertTrue(ChatSyncPolicy.requireSuccessfulList(true, Collections.emptyList()).isEmpty());
    }

    @Test(expected = IllegalStateException.class) public void serverErrorOverridesSuccessfulChatState() {
        ChatSyncPolicy.requireSuccessfulList(true, "SQL error", Collections.emptyList());
    }

    @Test public void validRowsAreNotFilteredOrReplaced() {
        List<String> rows = Arrays.asList("one", "two");
        assertSame(rows, ChatSyncPolicy.requireSuccessfulList(true, rows));
    }

    @Test(expected = IllegalStateException.class) public void falseStateRejectsEvenEmptyList() {
        ChatSyncPolicy.requireSuccessfulList(false, Collections.emptyList());
    }

    @Test(expected = IllegalStateException.class) public void missingStateRejectsResponse() {
        ChatSyncPolicy.requireSuccessfulList(null, Collections.emptyList());
    }

    @Test(expected = IllegalStateException.class) public void missingListIsNotAnEmptySuccess() {
        ChatSyncPolicy.requireSuccessfulList(true, null);
    }

    @Test(expected = IllegalStateException.class) public void malformedRowRejectsWholeResponse() {
        ChatSyncPolicy.requireSuccessfulList(true, Arrays.asList("one", null));
    }

    @Test public void fcmSchedulesBothTables() {
        assertArrayEquals(new TableName[]{TableName.CHAT, TableName.CHAT_GROUP},
                ChatSyncPolicy.notificationTables(false));
    }

    @Test public void websocketSchedulesOnlyGroups() {
        assertArrayEquals(new TableName[]{TableName.CHAT_GROUP}, ChatSyncPolicy.notificationTables(true));
    }

    @Test public void persistedTableNamesAreDistinctAndRoundTrip() {
        assertEquals(TableName.CHAT, TableName.fromCode("chat"));
        assertEquals(TableName.CHAT_GROUP, TableName.fromCode("chat_grp"));
        assertNotEquals(TableName.CHAT.getCode(), TableName.CHAT_GROUP.getCode());
    }

    @Test(expected = IllegalArgumentException.class) public void unrelatedTableCannotUseChatEndpoint() {
        ChatSyncPolicy.request(TableName.WP_DATA, 0);
    }
}
