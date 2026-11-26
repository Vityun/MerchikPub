package ua.com.merchik.merchik.Activities;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import ua.com.merchik.merchik.data.Database.Room.CoordOnMapSDB;

final class CoordOnMapProbe {
    private static final Gson GSON = new Gson();
    private static final int SAMPLE = 100;
    private static final Pattern FIELD = Pattern.compile("[\\p{L}_][\\p{L}\\p{N}_-]{0,79}");

    private CoordOnMapProbe() {}

    static void logChunks(String raw, BiConsumer<Integer, String> sink) {
        int start = 0;
        int part = 1;
        do {
            // Leave room for Logcat's prefix, including when the payload contains multibyte text.
            int end = start + Math.min(900, raw.length() - start);
            if (end < raw.length() && Character.isSurrogatePair(raw.charAt(end - 1), raw.charAt(end))) {
                end--;
            }
            sink.accept(part++, raw.substring(start, end));
            start = end;
        } while (start < raw.length());
    }

    static JsonObject request() {
        JsonObject request = new JsonObject();
        request.addProperty("mod", "data_list");
        request.addProperty("act", "coord_on_map");
        request.addProperty("dt_change_from", 0);
        request.addProperty("dt_change_to", 0);
        return request;
    }

    static boolean failed(JsonElement body) {
        return body == null || !body.isJsonObject() || !Boolean.TRUE.equals(flag(body, "state"))
                || Boolean.TRUE.equals(flag(body, "need_auth"));
    }

    static List<CoordOnMapSDB> readRows(JsonElement body) {
        if (failed(body)) throw new IllegalArgumentException("Unsuccessful coord_on_map response");
        JsonElement list = body.getAsJsonObject().get("list");
        if (list == null || !list.isJsonArray()) throw new IllegalArgumentException("Missing coord_on_map list");
        List<CoordOnMapSDB> rows = new ArrayList<>(list.getAsJsonArray().size());
        for (JsonElement item : list.getAsJsonArray()) {
            CoordOnMapSDB row = GSON.fromJson(item, CoordOnMapSDB.class);
            if (row == null || row.id <= 0 || row.nm == null) {
                throw new IllegalArgumentException("Invalid coord_on_map row");
            }
            rows.add(row);
        }
        return rows;
    }

    private static Boolean flag(JsonElement body, String key) {
        JsonElement value = body.isJsonObject() ? body.getAsJsonObject().get(key) : null;
        if (value == null || !value.isJsonPrimitive()) return null;
        String s = value.getAsString();
        if ("true".equalsIgnoreCase(s) || "1".equals(s)) return true;
        if ("false".equalsIgnoreCase(s) || "0".equals(s)) return false;
        return null;
    }

    static String describe(JsonElement body) {
        if (body == null || body.isJsonNull()) return "Порожня відповідь";
        StringBuilder result = new StringBuilder();
        for (String key : new String[]{"state", "auth", "need_auth"}) {
            Boolean value = flag(body, key);
            if (value != null) result.append(key).append('=').append(value).append('\n');
        }
        Map<String, Set<String>> types = new LinkedHashMap<>();
        inspect(body, "$", 0, types);
        for (Map.Entry<String, Set<String>> field : types.entrySet()) {
            result.append(field.getKey()).append(": ").append(String.join(" | ", field.getValue())).append('\n');
        }
        return result.toString().trim();
    }

    // Only field names, JSON types and array sizes; never render coordinate or identifier values.
    private static void inspect(JsonElement value, String path, int depth, Map<String, Set<String>> types) {
        if (depth > 8 || (types.size() >= 100 && !types.containsKey(path))) return;
        Set<String> seen = types.computeIfAbsent(path, ignored -> new LinkedHashSet<>());
        if (value.isJsonNull()) {
            seen.add("null");
        } else if (value.isJsonObject()) {
            seen.add("object");
            for (Map.Entry<String, JsonElement> field : value.getAsJsonObject().entrySet()) {
                String name = FIELD.matcher(field.getKey()).matches() ? field.getKey() : "*";
                inspect(field.getValue(), path + "." + name, depth + 1, types);
            }
        } else if (value.isJsonArray()) {
            int size = value.getAsJsonArray().size();
            seen.add(path.contains("[]") ? "array" : "array[" + size + "]"
                    + (size > SAMPLE ? " (перші " + SAMPLE + ")" : ""));
            for (int i = 0; i < Math.min(size, SAMPLE); i++) {
                inspect(value.getAsJsonArray().get(i), path + "[]", depth + 1, types);
            }
        } else {
            JsonPrimitive p = value.getAsJsonPrimitive();
            seen.add(p.isBoolean() ? "boolean" : p.isNumber() ? "number" : "string");
        }
    }
}
