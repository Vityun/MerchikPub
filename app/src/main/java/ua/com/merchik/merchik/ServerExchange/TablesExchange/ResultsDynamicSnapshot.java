package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.merchik.merchik.data.Database.Room.ResultsDynamicSDB;

/** Validates the entire snapshot before any existing rows can be removed. */
public final class ResultsDynamicSnapshot {
    private static final String[] FIELDS = {"ID", "isp", "client_id", "addr_id", "yearmonth",
            "ob_prod_plan", "avg_num_plan", "num_main_plan", "ob_prod_done", "avg_num_done",
            "num_main_done", "avg_oos", "total_face", "total_sku", "total_documents", "last_phot"};

    private ResultsDynamicSnapshot() { }

    public static JsonObject request() {
        JsonObject request = new JsonObject();
        request.addProperty("mod", "plan");
        request.addProperty("act", "results_dynamic");
        return request;
    }

    public static List<ResultsDynamicSDB> parse(JsonObject response) {
        JsonElement state = response == null ? null : response.get("state");
        if (state == null || !state.isJsonPrimitive()
                || !("true".equals(state.getAsString()) || "1".equals(state.getAsString()))) {
            throw new IllegalArgumentException("results_dynamic: state is not successful");
        }
        JsonElement list = response.get("list");
        if (list == null || !list.isJsonArray()) {
            throw new IllegalArgumentException("results_dynamic: list must be an array");
        }

        Gson gson = new Gson();
        List<ResultsDynamicSDB> rows = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<String> keys = new HashSet<>();
        for (JsonElement element : list.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("results_dynamic: invalid row " + rows.size());
            }
            JsonObject source = element.getAsJsonObject();
            JsonObject clean = new JsonObject();
            for (String field : FIELDS) {
                JsonElement value = source.get(field);
                if (value == null || !value.isJsonPrimitive() || value.getAsJsonPrimitive().isBoolean()) {
                    throw new IllegalArgumentException("results_dynamic: invalid " + field + " in row " + rows.size());
                }
                clean.addProperty(field, value.getAsString());
            }
            ResultsDynamicSDB row = gson.fromJson(clean, ResultsDynamicSDB.class);
            row.id = positiveId(row.id, "ID");
            row.clientId = positiveId(row.clientId, "client_id");
            row.addrId = positiveId(row.addrId, "addr_id");
            if (!row.yearmonth.matches("[0-9]{2}(0[1-9]|1[0-2])")) {
                throw new IllegalArgumentException("results_dynamic: invalid yearmonth=" + row.yearmonth);
            }
            String key = row.yearmonth + "/" + row.clientId + "/" + row.addrId;
            if (!ids.add(row.id)) {
                throw new IllegalArgumentException("results_dynamic: duplicate ID=" + row.id);
            }
            if (!keys.add(key)) {
                throw new IllegalArgumentException("results_dynamic: duplicate period/client/address=" + key);
            }
            rows.add(row);
        }
        return rows;
    }

    private static String positiveId(String value, String field) {
        if (value == null || !value.matches("[0-9]+") || new BigInteger(value).signum() <= 0) {
            throw new IllegalArgumentException("results_dynamic: invalid " + field);
        }
        return new BigInteger(value).toString();
    }
}
