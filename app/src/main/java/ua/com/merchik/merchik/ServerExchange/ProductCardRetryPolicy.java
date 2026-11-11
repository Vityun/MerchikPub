package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Retry counters are per account/product, independent of visit scopes and download VPI. */
public final class ProductCardRetryPolicy {
    public static final int MAX_ATTEMPTS = 5;

    private ProductCardRetryPolicy() { }

    public static boolean needsBatch(boolean refresh, boolean downloaded) {
        return refresh || !downloaded;
    }

    public static String scope(int user, String productId) {
        return user + ":" + productId;
    }

    public static Set<String> eligible(Set<String> missing, Map<String, Long> attempts) {
        Set<String> result = new TreeSet<>();
        for (String id : missing) {
            if (attempts.getOrDefault(id, 0L) < MAX_ATTEMPTS) result.add(id);
        }
        return result;
    }

    public static long nextAttempt(long previous) {
        return previous >= MAX_ATTEMPTS ? MAX_ATTEMPTS : Math.max(0, previous) + 1;
    }

    public static JsonObject request(Set<String> missing) {
        if (missing.isEmpty()) throw new IllegalArgumentException("No missing products");
        JsonObject request = new JsonObject();
        request.addProperty("mod", "data_list");
        request.addProperty("act", "tovar_list");
        JsonArray ids = new JsonArray();
        for (String id : new TreeSet<>(missing)) ids.add(id);
        request.add("id", ids);
        return request;
    }
}
