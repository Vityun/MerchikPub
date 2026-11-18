package ua.com.merchik.merchik.data.synchronization;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChatReadPolicyTest {
    @Test public void preservesLocalReadWhenServerReplaysUnread() {
        assertEquals(123L, ChatReadPolicy.mergeReadTime(123L, 0L));
        assertEquals(123L, ChatReadPolicy.mergeReadTime(123L, null));
    }
    @Test public void acceptsReadFromAnotherDevice() {
        assertEquals(234L, ChatReadPolicy.mergeReadTime(0L, 234L));
        assertEquals(234L, ChatReadPolicy.mergeReadTime(123L, 234L));
    }
    @Test public void missingOrNegativeDatesRemainUnread() {
        assertEquals(0L, ChatReadPolicy.mergeReadTime(null, null));
        assertEquals(0L, ChatReadPolicy.mergeReadTime(-10L, -1L));
    }
    @Test public void offscreenAndTinySliversDoNotMarkRead() {
        assertFalse(ChatReadPolicy.isVisible(-100, 100, 0, 600));
        assertFalse(ChatReadPolicy.isVisible(600, 100, 0, 600));
        assertFalse(ChatReadPolicy.isVisible(599, 100, 0, 600));
        assertFalse(ChatReadPolicy.isVisible(-51, 100, 0, 600));
    }
    @Test public void halfVisibleOrLargerMessagesAreReadable() {
        assertTrue(ChatReadPolicy.isVisible(-50, 100, 0, 600));
        assertTrue(ChatReadPolicy.isVisible(0, 100, 0, 600));
        assertTrue(ChatReadPolicy.isVisible(-200, 1200, 0, 600));
    }
    @Test public void emptyViewportCannotRead() {
        assertFalse(ChatReadPolicy.isVisible(0, 100, 0, 0));
        assertFalse(ChatReadPolicy.isVisible(0, 0, 0, 600));
    }
    @Test public void receiptAddressesOnlyTheViewedMessageWithoutDateFilter() {
        JsonObject request = ChatReadPolicy.request(14214942);
        assertEquals("chat", request.get("mod").getAsString());
        assertEquals("mark_read", request.get("act").getAsString());
        assertFalse(request.has("dt_change_from"));
        assertEquals(1, request.getAsJsonArray("data").size());
        JsonObject row = request.getAsJsonArray("data").get(0).getAsJsonObject();
        assertEquals(14214942, row.get("element_id").getAsInt());
        assertEquals(14214942, row.get("msg_id").getAsInt());
    }
    @Test(expected = IllegalArgumentException.class) public void invalidMessageCannotBeSent() {
        ChatReadPolicy.request(0);
    }
    @Test public void requiresSuccessfulServerAcknowledgement() {
        assertTrue(ChatReadPolicy.acknowledged(json("{\"state\":true}")));
        assertTrue(ChatReadPolicy.acknowledged(json("{\"state\":1,\"error\":null}")));
        assertTrue(ChatReadPolicy.acknowledged(json("{\"state\":\"1\",\"error\":\"\"}")));
        assertFalse(ChatReadPolicy.acknowledged(null));
        assertFalse(ChatReadPolicy.acknowledged(json("{}")));
        assertFalse(ChatReadPolicy.acknowledged(json("{\"state\":false}")));
        assertFalse(ChatReadPolicy.acknowledged(json("{\"state\":true,\"error\":\"failed\"}")));
    }
    private JsonObject json(String value) { return JsonParser.parseString(value).getAsJsonObject(); }
}
