package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Pure request/readiness rules for on-demand visit data. */
public final class RnoVisitDataPolicy {
    public enum Part { OPTIONS, REPORT_PREPARE, PRODUCTS }

    private RnoVisitDataPolicy() { }

    public static boolean needsPreparation(int assignedUser, String hash) {
        return assignedUser == 14041 || hasHash(hash);
    }

    public static boolean hasHash(String hash) {
        return hash != null && !hash.trim().isEmpty();
    }

    public static String visitScope(int user, long dad2) {
        if (user <= 0 || dad2 <= 0) throw new IllegalArgumentException("Invalid user/visit");
        return user + ":dad2=" + dad2;
    }

    public static String dataScope(int user, long dad2, String hash) {
        if (!hasHash(hash)) throw new IllegalArgumentException("Missing visit access hash");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(hash.getBytes(StandardCharsets.UTF_8));
            StringBuilder fingerprint = new StringBuilder();
            for (byte value : digest) fingerprint.append(String.format(java.util.Locale.ROOT, "%02x", value & 255));
            return visitScope(user, dad2) + ":" + fingerprint;
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public static boolean loaded(boolean saved, long savedCount, long actualCount) {
        return saved && savedCount >= 0 && actualCount >= savedCount;
    }

    public static boolean ready(boolean optionsLoaded, long optionsCount, boolean rpLoaded,
                                boolean productsLoaded, int missingProducts) {
        return optionsLoaded && optionsCount > 0 && rpLoaded && productsLoaded && missingProducts == 0;
    }

    public static boolean validReportRow(Long id, String dad2, String product, long requestedDad2) {
        return id != null && id > 0 && Long.toString(requestedDad2).equals(dad2)
                && product != null && !product.trim().isEmpty() && !"0".equals(product);
    }

    public static JsonObject request(Part part, long dad2, String hash) {
        if (dad2 <= 0 || !hasHash(hash)) throw new IllegalArgumentException("Missing visit access");
        JsonObject request = new JsonObject();
        request.addProperty("mod", part == Part.OPTIONS ? "plan"
                : part == Part.REPORT_PREPARE ? "report_prepare" : "data_list");
        request.addProperty("act", part == Part.OPTIONS ? "options_list"
                : part == Part.REPORT_PREPARE ? "list_data" : "tovar_list");
        JsonArray visits = new JsonArray();
        visits.add(String.valueOf(dad2));
        request.add("code_dad2", visits);
        if (part != Part.PRODUCTS) {
            JsonArray hashes = new JsonArray();
            hashes.add(hash);
            request.add("code_dad2_hash", hashes);
            request.addProperty("dt_change_from", "0");
        } else {
            // Keep the existing tovar_list contract; never reuse its global VPI here.
            request.addProperty("dt", "0");
        }
        return request;
    }
}
