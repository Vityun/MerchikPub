package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ua.com.merchik.merchik.data.UploadToServ.ReportPrepareServ;

/** Pure per-row debounce state. Times are monotonic milliseconds, not server timestamps. */
public final class ReportPrepareUploadPolicy {
    public static final long QUIET_MS = 5_000L;
    private static final Gson GSON = new Gson();
    private final Map<String, JsonElement> observed = new HashMap<>();
    private final Map<String, Long> changedAt = new HashMap<>();

    public void edited(String id, long now) {
        changedAt.put(id, now);
    }

    public long remaining(ReportPrepareServ row, long now) {
        String id = row.getElement_id();
        JsonElement payload = snapshot(row);
        JsonElement previous = observed.put(id, payload);
        if (previous == null) {
            changedAt.putIfAbsent(id, now);
        } else if (!previous.equals(payload)) {
            changedAt.put(id, now);
        }
        return Math.max(0L, QUIET_MS - (now - changedAt.get(id)));
    }

    public void retain(Set<String> pendingIds) {
        observed.keySet().retainAll(pendingIds);
        changedAt.keySet().retainAll(pendingIds);
    }

    public void clear() {
        observed.clear();
        changedAt.clear();
    }

    public static JsonElement snapshot(ReportPrepareServ row) {
        return GSON.toJsonTree(row);
    }

    public static boolean canAcknowledge(boolean requestSucceeded, boolean rowSucceeded,
                                         JsonElement sent, ReportPrepareServ current) {
        return requestSucceeded && rowSucceeded && sent != null && current != null
                && sent.equals(snapshot(current));
    }
}
