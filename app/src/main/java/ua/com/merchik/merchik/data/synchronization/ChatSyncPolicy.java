package ua.com.merchik.merchik.data.synchronization;

import com.google.gson.JsonObject;

import java.util.List;
import ua.com.merchik.merchik.ServerExchange.SyncResponsePolicy;

public final class ChatSyncPolicy {
    private static final long OVERLAP_SECONDS = 120;

    private ChatSyncPolicy() { }

    public static JsonObject request(TableName table, long from) {
        JsonObject request = new JsonObject();
        request.addProperty("mod", "chat");
        request.addProperty("act", action(table));
        request.addProperty("dt_change_from", Long.toString(Math.max(0, from)));
        return request;
    }

    public static String action(TableName table) {
        if (table == TableName.CHAT) return "list_message";
        if (table == TableName.CHAT_GROUP) return "list";
        throw new IllegalArgumentException("Not a chat table: " + table);
    }

    public static long requestWatermark(long saved, Long trustedRequestStart) {
        long from = Math.max(0, saved);
        return trustedRequestStart == null ? from
                : Math.min(from, Math.max(0, trustedRequestStart - OVERLAP_SECONDS));
    }

    public static long savedWatermark(long from, Long trustedRequestStart) {
        // Never advance to response time: changes made during the request must be fetched again.
        return trustedRequestStart == null ? Math.max(0, from)
                : Math.max(from, Math.max(0, trustedRequestStart - OVERLAP_SECONDS));
    }

    public static <T> List<T> requireSuccessfulList(Boolean state, List<T> rows) {
        return requireSuccessfulList(state, null, rows);
    }

    public static <T> List<T> requireSuccessfulList(Boolean state, String error, List<T> rows) {
        return SyncResponsePolicy.requireList(state, error, rows);
    }

    public static TableName[] notificationTables(boolean websocket) {
        return websocket ? new TableName[]{TableName.CHAT_GROUP}
                : new TableName[]{TableName.CHAT, TableName.CHAT_GROUP};
    }
}
