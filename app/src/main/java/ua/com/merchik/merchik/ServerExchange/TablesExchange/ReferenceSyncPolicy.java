package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import ua.com.merchik.merchik.ServerExchange.SyncResponsePolicy;

public final class ReferenceSyncPolicy {
    public enum Table {
        PLANOGRAM_IMAGES("planogram", "img_list", true),
        STANDART("standart", "list", false),
        PLANOGRAM_ADDRESSES("planogram", "addr_list", true),
        ARTICLES("data_list", "tovar_vendor_code_list", false),
        PLANOGRAM_GROUPS("planogram", "group_list", true),
        LESSONS("lesson", "list", false),
        QUESTIONS("quest_data", "list", false),
        LOCATIONS("location", "list", false),
        ADDITIONAL_MATERIAL_GROUPS("additional_materials", "group_list", false),
        SAMPLE_PHOTOS("images_view", "list_example", false);

        final String mod;
        final String act;
        final boolean noLimit;

        Table(String mod, String act, boolean noLimit) {
            this.mod = mod;
            this.act = act;
            this.noLimit = noLimit;
        }

        public String resource() { return mod + "/" + act; }
    }

    private ReferenceSyncPolicy() { }

    public static JsonObject request(Table table, long fromSeconds) {
        JsonObject request = new JsonObject();
        request.addProperty("mod", table.mod);
        request.addProperty("act", table.act);
        if (table.noLimit) request.addProperty("nolimit", "1");
        request.addProperty("dt_change_from", Long.toString(Math.max(0, fromSeconds)));
        return request;
    }

    public static <T> List<T> rows(Boolean state, String error, List<T> rows,
                                    Function<T, ? extends Number> id) {
        SyncResponsePolicy.requireList(state, error, rows);
        Set<Long> ids = new HashSet<>();
        for (T row : rows) {
            Number key = row == null ? null : id.apply(row);
            if (key == null || key.longValue() <= 0 || !ids.add(key.longValue())) {
                throw new IllegalStateException("Invalid/duplicate incremental row ID");
            }
        }
        return rows;
    }
}
