package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ModImagesView;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ModImagesViewList;

/** Wire validation and field-level merge; no Android or database operations. */
public final class PhotoMetadataPolicy {
    // Old cursors may have advanced on state=true responses containing a SQL error.
    // A new namespace replays metadata once, merging rather than deleting local photos.
    public static final String RESOURCE = "images_view/list_image/metadata-v3";
    private static final Gson GSON = new Gson();

    private PhotoMetadataPolicy() { }

    public static JsonObject filters(JsonObject source) {
        JsonObject result = new JsonObject();
        TreeMap<String, JsonElement> fields = new TreeMap<>();
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            String key = entry.getKey();
            if (!key.equals("dt_upload") && !key.equals("dt_change_from")
                    && !entry.getValue().isJsonNull()) fields.put(key, entry.getValue());
        }
        for (Map.Entry<String, JsonElement> entry : fields.entrySet()) {
            result.add(entry.getKey(), entry.getValue().deepCopy());
        }
        result.addProperty("mod", "images_view");
        result.addProperty("act", "list_image");
        result.addProperty("nolimit", "1");
        if (result.has("id_list")) {
            TreeSet<String> ids = new TreeSet<>();
            for (String id : result.get("id_list").getAsString().split(",", -1)) ids.add(serverId(id));
            result.addProperty("id_list", String.join(",", ids));
        }
        return result;
    }

    public static String scope(int userId, JsonObject filters) {
        return userId + ":" + filters(filters).toString();
    }

    public static JsonObject request(JsonObject filters, long from) {
        JsonObject result = filters(filters);
        result.addProperty("dt_change_from", Long.toString(Math.max(0L, from)));
        return result;
    }

    public static List<ModImagesViewList> rows(ModImagesView response, JsonObject request) {
        if (response == null || !Boolean.TRUE.equals(response.getState()) || response.getList() == null
                || (response.getTotalPages() != null && response.getTotalPages() > 1)) {
            throw new IllegalStateException("Invalid/incomplete list_image response");
        }
        List<ModImagesViewList> rows = uniqueRows(SyncResponsePolicy.requireList(
                response.getState(), response.getError(), response.getList()));
        if (request.has("id_list")) {
            TreeSet<String> missing = new TreeSet<>();
            for (String id : request.get("id_list").getAsString().split(",")) missing.add(serverId(id));
            for (ModImagesViewList row : rows) {
                if (!missing.remove(row.getID())) throw new IllegalStateException("Unexpected photo ID: " + row.getID());
            }
            if (!missing.isEmpty()) throw new IllegalStateException("Missing photo IDs: " + missing);
        }
        return rows;
    }

    public static List<ModImagesViewList> uniqueRows(List<ModImagesViewList> rows) {
        if (rows == null) throw new IllegalArgumentException("Missing photo list");
        Map<String, ModImagesViewList> unique = new LinkedHashMap<>();
        for (ModImagesViewList row : rows) {
            if (row == null) throw new IllegalArgumentException("Null photo row");
            // Validate every value before a transaction, not after partially filling a row.
            String id = serverId(row.getID());
            if (!id.equals(row.getID())) throw new IllegalArgumentException("Noncanonical photo ID");
            optionalInt(row.getMerchikId());
            optionalInt(row.getAddrId());
            optionalInt(row.getPhotoTp());
            optionalInt(row.getDvi());
            optionalInt(row.getApprove());
            optionalLong(row.codeDad2);
            optionalLong(row.getDtUpload());
            if (row.getDt() < 0) throw new IllegalArgumentException("Negative photo date");
            ModImagesViewList previous = unique.putIfAbsent(id, row);
            if (previous != null && !GSON.toJsonTree(previous).equals(GSON.toJsonTree(row))) {
                throw new IllegalArgumentException("Conflicting duplicate photo ID: " + id);
            }
        }
        return new ArrayList<>(unique.values());
    }

    public static void merge(StackPhotoDB current, ModImagesViewList row, boolean isNew, long startedLocalSec) {
        boolean editedDuringRequest = !isNew && current.getVpi() >= startedLocalSec;
        boolean firstLink = !isNew && idOrZero(current.getPhotoServerId()).equals("0");
        current.setPhotoServerId(row.getID());
        if (hasValue(row.getPhotoUrl())) current.setPhotoServerURL(row.getPhotoUrl());
        if (isNew || current.object_id == null) current.object_id = 1;
        if (row.getDt() > 0) {
            current.setDt(row.getDt());
            if (current.getCreate_time() == 0) current.setCreate_time(Math.multiplyExact(row.getDt(), 1000L));
        }
        Long uploaded = optionalLong(row.getDtUpload());
        long uploadedMillis = Math.multiplyExact(uploaded != null && uploaded > 0 ? uploaded : row.getDt(), 1000L);
        if (uploadedMillis > 0) {
            if (current.getUpload_to_server() == 0) current.setUpload_to_server(uploadedMillis);
            if (current.getGet_on_server() == 0) current.setGet_on_server(uploadedMillis);
        }
        if (optionalLong(row.codeDad2) != null) current.code_dad2 = optionalLong(row.codeDad2);
        if (hasValue(row.imgHash)) current.photo_hash = row.imgHash;
        if (row.getTovarId() != null && (isNew || !idOrZero(current.tovar_id).equals(idOrZero(row.getTovarId())))) {
            current.tovar_id = row.getTovarId();
        }
        if (row.showcase_id != null && !(firstLink && idOrZero(row.showcase_id).equals("0"))) {
            current.showcase_id = row.showcase_id;
        }
        if (row.codeIZA != null) current.setCode_iza(row.codeIZA);
        if (optionalInt(row.getMerchikId()) != null) current.setUser_id(optionalInt(row.getMerchikId()));
        if (optionalInt(row.getAddrId()) != null) current.setAddr_id(optionalInt(row.getAddrId()));
        if (row.getClientId() != null) current.setClient_id(row.getClientId());
        if (optionalInt(row.getPhotoTp()) != null) current.setPhoto_type(optionalInt(row.getPhotoTp()));
        if (row.getMerchikIdTxt() != null) current.setUserTxt(row.getMerchikIdTxt());
        if (row.getAddrIdTxt() != null) current.setAddressTxt(row.getAddrIdTxt());
        if (row.getClientIdTxt() != null) current.setCustomerTxt(row.getClientIdTxt());
        if (row.getPhotoTpTxt() != null) current.setPhoto_typeTxt(String.valueOf(row.getPhotoTpTxt()));
        if (optionalInt(row.getApprove()) != null) current.setApprove(optionalInt(row.getApprove()));
        if (row.getPhotoUserId() != null) current.photo_user_id = row.getPhotoUserId();
        if (row.example_id != null) current.example_id = row.example_id;
        if (row.example_img_id != null) current.example_img_id = row.example_img_id;
        if (row.planogram_id != null) current.planogram_id = row.planogram_id;
        if (row.planogram_img_id != null) current.planogram_img_id = row.planogram_img_id;
        if (!current.isCommentUpload() && !editedDuringRequest && !(firstLink && current.getComment() != null)
                && row.getComments() != null) {
            current.setComment(row.getComments());
        }
        if (!current.isDviUpload() && !editedDuringRequest && !(firstLink && current.getDvi() != null)
                && optionalInt(row.getDvi()) != null) {
            current.setDvi(optionalInt(row.getDvi()));
        }
        // Keep local file paths, editing timestamps, ratings and all upload flags intact.
    }

    public static boolean matchesUnassignedLocal(StackPhotoDB local, ModImagesViewList row) {
        return mismatch(local, row, false) == null;
    }

    public static boolean matchesHashResponse(StackPhotoDB local, ModImagesViewList row) {
        return mismatch(local, row, true) == null;
    }

    static StackPhotoDB findLocalForRow(ModImagesViewList row, List<StackPhotoDB> candidates,
                                        List<ModImagesViewList> sameHashRows) {
        StackPhotoDB match = null;
        for (StackPhotoDB candidate : candidates) {
            if (!matchesUnassignedLocal(candidate, row)) continue;
            if (match != null) throw new IllegalStateException("action=not_merged, reason=multiple_locals, "
                    + describeMatch(row, candidates, false));
            match = candidate;
        }
        if (match != null) {
            for (ModImagesViewList other : sameHashRows) {
                if (!row.getID().equals(other.getID()) && matchesUnassignedLocal(match, other)) {
                    throw new IllegalStateException("action=not_merged, reason=multiple_server_ids, otherServerId="
                            + other.getID() + ", " + describeMatch(row, candidates, false));
                }
            }
        }
        return match;
    }

    // A targeted hash response may omit context; the ordinary list must identify the visit and owner.
    static String mismatch(StackPhotoDB local, ModImagesViewList row, boolean hashResponse) {
        if (!hasValue(row.imgHash) || !Objects.equals(local.photo_hash, row.imgHash)) return "hash_mismatch";
        if (!"0".equals(idOrZero(local.photoServerId))
                && !(hashResponse && Objects.equals(local.photoServerId, row.getID()))) return "server_id_assigned";
        Long visit = optionalLong(row.codeDad2);
        if (visit == null ? !hashResponse : local.code_dad2 != visit) return "visit_mismatch";
        Integer user = optionalInt(row.getMerchikId());
        if (user == null ? !hashResponse : !Objects.equals(user, local.user_id)) return "user_mismatch";
        if ((!hashResponse || row.getTovarId() != null)
                && !idOrZero(local.tovar_id).equals(idOrZero(row.getTovarId()))) return "product_mismatch";
        Integer type = optionalInt(row.getPhotoTp());
        if (type != null && local.photo_type != null && !type.equals(local.photo_type)) return "type_mismatch";
        String showcase = idOrZero(row.showcase_id);
        String localShowcase = idOrZero(local.showcase_id);
        if (!"0".equals(showcase) && !"0".equals(localShowcase) && !showcase.equals(localShowcase)) return "showcase_mismatch";
        if (hashResponse && optionalInt(row.getAddrId()) != null
                && !Objects.equals(local.addr_id, optionalInt(row.getAddrId()))) return "address_mismatch";
        if (hashResponse && row.getClientId() != null && !Objects.equals(local.client_id, row.getClientId())) return "client_mismatch";
        return null;
    }

    private static String idOrZero(String value) {
        return hasValue(value) ? value.trim() : "0";
    }

    static String describeMatch(ModImagesViewList row, Iterable<StackPhotoDB> candidates, boolean hashResponse) {
        StringBuilder out = new StringBuilder("serverId=").append(row.getID()).append(", hash=").append(row.imgHash)
                .append(", dad2=").append(row.codeDad2).append(", type=").append(row.getPhotoTp())
                .append(", serverTovar='").append(row.getTovarId()).append("', showcase=").append(row.showcase_id)
                .append(", candidates=[");
        for (StackPhotoDB local : candidates) {
            if (!Objects.equals(row.imgHash, local.photo_hash)) continue;
            String reason = mismatch(local, row, hashResponse);
            out.append("{localId=").append(local.getId()).append(", localTovar='").append(local.tovar_id)
                    .append("', type=").append(local.photo_type).append(", showcase=").append(local.showcase_id)
                    .append(", reason=").append(reason == null ? "match" : reason).append("}");
        }
        return out.append(']').toString();
    }

    private static String serverId(String value) {
        Long id = optionalLong(value);
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid photo ID");
        return Long.toString(id);
    }

    private static Integer optionalInt(String value) {
        Long number = optionalLong(value);
        return number == null ? null : Math.toIntExact(number);
    }

    private static Long optionalLong(String value) {
        return hasValue(value) ? Long.valueOf(value.trim()) : null;
    }

    private static boolean hasValue(String value) { return value != null && !value.trim().isEmpty(); }
}
