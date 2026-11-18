package ua.com.merchik.merchik.data.synchronization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class ChatReadPolicy {
    private ChatReadPolicy() { }

    public static long mergeReadTime(Long local, Long remote) {
        return Math.max(Math.max(0L, local == null ? 0L : local), remote == null ? 0L : remote);
    }

    public static boolean isVisible(int offset, int size, int viewportStart, int viewportEnd) {
        if (size <= 0 || viewportEnd <= viewportStart) return false;
        long visible = Math.max(0L, Math.min((long) offset + size, viewportEnd)
                - Math.max(offset, viewportStart));
        return visible * 2 >= Math.min(size, (long) viewportEnd - viewportStart);
    }

    public static JsonObject request(int messageId) {
        if (messageId <= 0) throw new IllegalArgumentException("Invalid chat message ID");
        JsonObject item = new JsonObject();
        item.addProperty("element_id", messageId);
        item.addProperty("msg_id", messageId);
        JsonArray rows = new JsonArray();
        rows.add(item);
        JsonObject request = new JsonObject();
        request.addProperty("mod", "chat");
        request.addProperty("act", "mark_read");
        request.add("data", rows);
        return request;
    }

    public static boolean acknowledged(JsonObject response) {
        if (response == null) return false;
        JsonElement state = response.get("state");
        if (state == null || !state.isJsonPrimitive()) return false;
        String value = state.getAsString();
        if (!"true".equalsIgnoreCase(value) && !"1".equals(value)) return false;
        JsonElement error = response.get("error");
        return error == null || error.isJsonNull()
                || (error.isJsonPrimitive() && error.getAsString().trim().isEmpty());
    }
}
