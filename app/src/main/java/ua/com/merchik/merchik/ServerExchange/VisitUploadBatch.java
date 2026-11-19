package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Durable snapshots: a legacy uploader clearing a dirty flag is not an upload acknowledgement. */
@androidx.annotation.Keep
public final class VisitUploadBatch {
    @androidx.annotation.Keep
    public enum Kind {
        WP("plan", "update_data"), RP("report_prepare", "set_report_data"),
        PHOTO_COMMENT("images_view", "set_comment"), PHOTO_DVI("images_view", "set_dvi"),
        PHOTO_SCORE("images_view", "set_score"), VOTE("images_view", "set_score"),
        ACHIEVEMENT("images_achieve", "add_row"), AR("additional_requirements", "set_score"),
        TAR("reclamation", "update_data"), TAR_CREATE("reclamation", "create"),
        TAR_COMMENT("reclamation", "set_comment"), EKL("sms_verification", "verification_check"),
        PAUSE("plan", "pause_save"), PLANOGRAM("planogram", "vizit_showcase_save"),
        ANSWER("quest_data", "add_row"), LOCATION("location", "track"),
        PLAN_BUDGET("plan_budget", "wp_data_request_add"), LOG("log", "save");

        final String mod, act;
        Kind(String mod, String act) { this.mod = mod; this.act = act; }
    }

    @androidx.annotation.Keep
    public static final class Row {
        public Kind kind;
        public String id;
        public String authorId;
        public JsonObject data;
        public JsonObject acknowledgement;
        public boolean applied;

        public Row(Kind kind, Object id, JsonObject data) {
            this.kind = kind;
            this.id = String.valueOf(id);
            this.data = data.deepCopy();
        }

        String key() { return kind + ":" + id; }
    }

    public int userId;
    public long dad2;
    public long retryAfter;
    public boolean complete;
    public Map<String, Row> rows = new LinkedHashMap<>();
    // Conduct-only HTTP receipts. Never replace StackPhotoDB's normal upload state.
    public Map<String, String> photoHttpReceipts = new LinkedHashMap<>();
    public java.util.Set<Integer> recoveryMessages = new java.util.HashSet<>();

    public boolean retryAfterConductFailure(int messageId) {
        if (messageId <= 0) throw new IllegalArgumentException("Missing failure message ID");
        if (!recoveryMessages.add(messageId)) return false;
        // A new conduct failure requests a fresh attempt, not reuse of a rejected photo's HTTP receipt.
        if (photoHttpReceipts != null) photoHttpReceipts.clear();
        for (Row row : rows.values()) {
            // Replay updates only. Replaying acknowledged creates would duplicate server records.
            switch (row.kind) {
                case WP: case RP: case PHOTO_COMMENT: case PHOTO_DVI: case PHOTO_SCORE:
                case VOTE: case AR: case TAR: case EKL: case PAUSE: case PLANOGRAM:
                    if (row.acknowledgement != null && row.applied) {
                        row.acknowledgement = null;
                        row.applied = false;
                    }
                    break;
                default: break;
            }
        }
        complete = false;
        retryAfter = 0;
        return true;
    }

    public boolean merge(List<Row> current) {
        boolean changed = false;
        for (Row row : current) {
            if (row.kind == Kind.AR) {
                boolean removed = rows.values().removeIf(old -> old.kind == Kind.AR && !old.id.equals(row.id)
                        && string(old.data, "element_id").equals(string(row.data, "element_id"))
                        && string(old.data, "tp_id").equals(string(row.data, "tp_id")));
                if (removed) { complete = false; changed = true; }
            }
            Row previous = rows.get(row.key());
            if (previous == null || !previous.data.equals(row.data)) {
                rows.put(row.key(), row);
                complete = false;
                changed = true;
            }
        }
        return changed;
    }

    public boolean tracks(Kind kind, Object id) { return rows.containsKey(kind + ":" + id); }

    public List<Row> next() {
        return next(java.util.Collections.emptySet());
    }

    public boolean allAcknowledged() {
        return allAcknowledged(java.util.Collections.emptySet());
    }

    public boolean allAcknowledged(java.util.Set<String> photosWithoutServerId) {
        for (Row row : rows.values()) {
            if (waitsForPhotoId(row, photosWithoutServerId)) continue;
            if (row.acknowledgement == null || !row.applied) return false;
        }
        return true;
    }

    public List<Row> next(java.util.Set<Kind> deferred) {
        return next(deferred, java.util.Collections.emptySet());
    }

    public List<Row> next(java.util.Set<Kind> deferred, java.util.Set<String> photosWithoutServerId) {
        List<Row> result = new ArrayList<>();
        for (Row row : rows.values()) {
            if (row.acknowledgement != null || deferred.contains(row.kind)) continue;
            if (waitsForPhotoId(row, photosWithoutServerId)) continue;
            if (!result.isEmpty() && result.get(0).kind != row.kind) continue;
            if (!result.isEmpty() && row.kind == Kind.AR
                    && !string(result.get(0).data, "tp_id").equals(string(row.data, "tp_id"))) continue;
            result.add(row);
            // These endpoints have single-row/positional responses, not a reliable batch key.
            if (row.kind == Kind.WP || row.kind == Kind.TAR || row.kind == Kind.TAR_CREATE
                    || row.kind == Kind.TAR_COMMENT || row.kind == Kind.PAUSE || result.size() == 100) break;
        }
        return result;
    }

    private static boolean waitsForPhotoId(Row row, java.util.Set<String> photosWithoutServerId) {
        return (row.kind == Kind.PHOTO_COMMENT || row.kind == Kind.PHOTO_DVI || row.kind == Kind.PHOTO_SCORE)
                && !positiveId(row.data.get("id")) && photosWithoutServerId.contains(row.id);
    }

    public boolean acknowledge(Row sent, JsonObject acknowledgement) {
        Row current = rows.get(sent.key());
        if (current == null || !current.data.equals(sent.data)) return false;
        current.acknowledgement = acknowledgement.deepCopy();
        return true;
    }

    public void reconcileCreatedTask(String localId, String serverId, Long dad2) {
        Row update = rows.remove(Kind.TAR + ":" + localId);
        if (update != null) {
            update.id = serverId;
            update.data.addProperty("element_id", Integer.parseInt(serverId));
            // A freshly collected edit under the server ID is newer than the queued local-ID row.
            rows.putIfAbsent(update.key(), update);
        }
        for (Row row : rows.values()) {
            if (row.acknowledgement != null) continue;
            if (row.kind == Kind.TAR && serverId.equals(row.id) && dad2 != null && dad2 > 0) {
                row.data.addProperty("code_dad2", dad2);
            }
            if (row.kind == Kind.TAR_COMMENT && localId.equals(string(row.data, "id"))) {
                row.data.addProperty("id", serverId);
            }
        }
    }

    public void reconcileCommentId(Row row, String serverId) {
        rows.remove(row.key());
        row.id = serverId;
        row.data.addProperty("comment_id", serverId);
        rows.putIfAbsent(row.key(), row);
    }

    public static JsonObject request(List<Row> rows) {
        Kind kind = rows.get(0).kind;
        JsonObject request = new JsonObject();
        request.addProperty("mod", kind.mod);
        request.addProperty("act", kind.act);
        JsonArray data = new JsonArray();
        for (Row row : rows) {
            JsonObject payload = row.data.deepCopy();
            if (kind == Kind.WP) {
                // Persisted/replayed payloads must not carry the legacy conduct command either.
                payload.remove("status_set");
                payload.remove("set_status");
            }
            data.add(payload);
        }
        request.add(kind == Kind.PLANOGRAM ? "list" : "data", data);
        return request;
    }

    public static boolean yes(JsonElement value) {
        return value != null && value.isJsonPrimitive()
                && ("true".equalsIgnoreCase(value.getAsString()) || "1".equals(value.getAsString()));
    }

    public static String string(JsonObject object, String field) {
        JsonElement value = object == null ? null : object.get(field);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }

    private static boolean error(JsonObject object) {
        JsonElement value = object.get("error");
        if (value != null && !value.isJsonNull() && !value.isJsonPrimitive()) return true;
        String error = string(object, "error");
        return !error.isEmpty() && !"0".equals(error) && !"false".equals(error);
    }

    private static boolean hasOtherResults(JsonObject body, String expected) {
        for (String field : new String[]{"plan", "list", "data", "result", "geo_result", "log"}) {
            if (!field.equals(expected) && body.has(field) && !body.get(field).isJsonNull()) return true;
        }
        return false;
    }

    /** Null means unconfirmed; never infer acceptance from HTTP 200 or a nonempty list. */
    public static JsonObject accepted(Row sent, JsonObject body) {
        if (sent.kind == Kind.LOG) {
            if (body == null || error(body) || (body.has("state") && !yes(body.get("state")))) return null;
            JsonElement log = body.get("log");
            JsonElement item = log != null && log.isJsonObject() ? log.getAsJsonObject().get(sent.id) : null;
            if (item == null || !item.isJsonObject()) return null;
            JsonObject row = item.getAsJsonObject();
            // log/save returns keyed receipts; older responses have no state field.
            return !error(row) && (row.has("state") ? yes(row.get("state")) : row.size() > 0) ? row : null;
        }
        if (body == null || error(body)) return null;
        boolean optionalState = sent.kind == Kind.AR && (!body.has("state") || body.get("state").isJsonNull());
        if (!optionalState && !yes(body.get("state"))) return null;
        String field = sent.kind == Kind.WP ? "plan"
                : sent.kind == Kind.LOCATION ? "geo_result" : "list";
        if (sent.kind == Kind.RP || sent.kind == Kind.PLAN_BUDGET) field = "data";
        JsonElement list = body.get(field);
        if (sent.kind == Kind.PLANOGRAM && list != null && list.isJsonObject()) {
            return yes(list.getAsJsonObject().get(sent.id)) ? body : null;
        }
        if (sent.kind == Kind.LOCATION && list != null && list.isJsonObject()) {
            JsonElement item = list.getAsJsonObject().get(sent.id);
            return item != null && item.isJsonObject() && yes(item.getAsJsonObject().get("state"))
                    && !error(item.getAsJsonObject()) && positiveId(item.getAsJsonObject().get("geo_id")) ? item.getAsJsonObject() : null;
        }
        if (list != null && list.isJsonArray()) {
            if (sent.kind == Kind.WP && list.getAsJsonArray().size() == 0
                    && !hasOtherResults(body, field)) return body;
            JsonObject found = null;
            for (JsonElement item : list.getAsJsonArray()) {
                if (!item.isJsonObject()) continue;
                JsonObject row = item.getAsJsonObject();
                String id = string(row, "element_id");
                if (sent.kind == Kind.EKL && id.isEmpty()) id = string(row, "id");
                boolean positional = (sent.kind == Kind.TAR_COMMENT || sent.kind == Kind.TAR
                        || sent.kind == Kind.PAUSE) && list.getAsJsonArray().size() == 1 && id.isEmpty();
                String expectedId = string(sent.data, "element_id");
                if (expectedId.isEmpty()) expectedId = sent.id;
                if (!expectedId.equals(id) && !positional) continue;
                if (found != null) return null;
                if (error(row) || (row.has("state") && !yes(row.get("state")))) return null;
                if (sent.kind != Kind.WP && sent.kind != Kind.ANSWER
                        && !yes(row.get("state"))) return null;
                if ((sent.kind == Kind.ACHIEVEMENT || sent.kind == Kind.PLAN_BUDGET || sent.kind == Kind.TAR_CREATE)
                        && !positiveId(row.get("id"))) return null;
                if (sent.kind == Kind.TAR_COMMENT) {
                    JsonObject info = row.has("info") && row.get("info").isJsonObject() ? row.getAsJsonObject("info") : row;
                    if (!positiveId(info.get("comment_id"))) return null;
                }
                found = row;
            }
            return found;
        }
        // Single-row endpoints also acknowledge idempotent updates without a changed-fields list.
        if ((sent.kind == Kind.WP || sent.kind == Kind.PAUSE || sent.kind == Kind.TAR)
                && (list == null || list.isJsonNull()) && !hasOtherResults(body, field)) return body;
        return null;
    }

    static boolean positiveId(JsonElement value) {
        try { return value != null && value.getAsLong() > 0; }
        catch (RuntimeException e) { return false; }
    }
}
