package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.features.main.options.PhotoReportCountPolicy;

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

    public static boolean requiresWarehouseComment(StackPhotoDB photo) {
        return Integer.valueOf(31).equals(photo.getPhoto_type())
                && PhotoReportCountPolicy.isEmptyWarehouseSample(photo.getExample_id());
    }

    public static boolean alreadyUploaded(StackPhotoDB photo) {
        return hasServerId(photo) || photo.getUpload_to_server() > 0 || photo.getGet_on_server() > 0;
    }

    private static String identity(StackPhotoDB photo) {
        String hash = photo.getPhoto_hash();
        if (hash != null && !hash.isEmpty()) return "hash:" + hash;
        return fileIdentity(photo);
    }

    private static String fileIdentity(StackPhotoDB photo) {
        String path = photo.getPhoto_num();
        return path == null || path.isEmpty() ? null : "file:" + photo.getCreate_time() + ":" + path;
    }

    public static boolean record(VisitUploadBatch batch, StackPhotoDB sent, boolean successful, JsonObject body) {
        String identity = identity(sent);
        if (identity == null || !acceptsResponse(successful, body)) return false;
        if (sent.getCode_dad2() == batch.dad2 && requiresWarehouseComment(sent)) {
            // A rejected file cannot acquire the server ID needed to address its comment.
            JsonElement list = body == null ? null : body.get("list");
            if (list == null || !list.isJsonArray() || list.getAsJsonArray().size() != 1
                    || !list.getAsJsonArray().get(0).isJsonObject()) return false;
            JsonObject row = list.getAsJsonArray().get(0).getAsJsonObject();
            if (!VisitUploadBatch.yes(row.get("state"))
                    && !"photo_already_exist".equals(VisitUploadBatch.string(row, "error_type"))) return false;
        }
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
        if (alreadyUploaded(photo)) return true;
        String identity = identity(photo);
        String receipt = batch.photoHttpReceipts == null ? null : batch.photoHttpReceipts.get(String.valueOf(photo.getId()));
        // A receipt obtained before hash recovery still identifies the same capture.
        return receipt != null && (Objects.equals(identity, receipt)
                || (photo.getCode_dad2() == batch.dad2 && requiresWarehouseComment(photo) && receipt.equals(fileIdentity(photo))));
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
