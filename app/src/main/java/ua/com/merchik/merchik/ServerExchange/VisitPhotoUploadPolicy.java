package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

/** Only the conduct barrier; this policy never acknowledges normal photo uploads or metadata. */
public final class VisitPhotoUploadPolicy {
    private VisitPhotoUploadPolicy() { }

    public static boolean acceptsResponse(boolean successful, JsonObject body) {
        if (!successful) return false;
        if (body == null) return true;
        if (hasError(body.get("error")) || hasError(body.get("error_type"))) return false;
        // state=false may summarize a per-photo rejection. It is not a global failure when list exists.
        JsonElement list = body.get("list");
        boolean photoResults = list != null && list.isJsonArray() && list.getAsJsonArray().size() > 0;
        return !body.has("state") || VisitUploadBatch.yes(body.get("state")) || photoResults;
    }

    private static boolean hasError(JsonElement value) {
        if (value == null || value.isJsonNull()) return false;
        if (!value.isJsonPrimitive()) return true;
        String error = value.getAsString().trim();
        return !error.isEmpty() && !"0".equals(error) && !"false".equalsIgnoreCase(error);
    }

    private static String identity(StackPhotoDB photo) {
        String hash = photo.getPhoto_hash();
        if (hash != null && !hash.isEmpty()) return "hash:" + hash;
        String path = photo.getPhoto_num();
        return path == null || path.isEmpty() ? null : "file:" + photo.getCreate_time() + ":" + path;
    }

    public static boolean record(VisitUploadBatch batch, StackPhotoDB sent, boolean successful, JsonObject body) {
        String identity = identity(sent);
        if (identity == null || !acceptsResponse(successful, body)) return false;
        if (batch.photoHttpReceipts == null) batch.photoHttpReceipts = new java.util.LinkedHashMap<>();
        batch.photoHttpReceipts.put(String.valueOf(sent.getId()), identity);
        return true;
    }

    public static boolean hasServerId(StackPhotoDB photo) {
        try { return photo.getPhotoServerId() != null && Long.parseLong(photo.getPhotoServerId()) > 0; }
        catch (NumberFormatException error) { return false; }
    }

    public static boolean ready(VisitUploadBatch batch, StackPhotoDB photo) {
        // Existing successful uploads do not need another request just to obtain a server ID.
        if (hasServerId(photo) || photo.getUpload_to_server() > 0 || photo.getGet_on_server() > 0) return true;
        String identity = identity(photo);
        return identity != null && batch.photoHttpReceipts != null
                && Objects.equals(identity, batch.photoHttpReceipts.get(String.valueOf(photo.getId())));
    }

    public static boolean allReady(VisitUploadBatch batch, List<StackPhotoDB> photos) {
        for (StackPhotoDB photo : photos) if (!ready(batch, photo)) return false;
        return true;
    }

    public static Set<String> pendingServerIds(VisitUploadBatch batch, List<StackPhotoDB> photos) {
        Set<String> ids = new HashSet<>();
        for (StackPhotoDB photo : photos) {
            if (!hasServerId(photo) && ready(batch, photo)) ids.add(String.valueOf(photo.getId()));
        }
        return ids;
    }
}
