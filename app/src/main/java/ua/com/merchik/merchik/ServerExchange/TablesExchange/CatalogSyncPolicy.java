package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB;

/** Exclusions are a bandwidth optimization, never a download watermark. */
public final class CatalogSyncPolicy {
    public static final long FULL_REFRESH_SECONDS = 24 * 60 * 60;

    public enum Table {
        SHOWCASES("rack", "list", "id_exclude"),
        PLANOGRAMS("planogram", "list", "id_exclude_old"),
        ADDRESSES("planogram", "addr_list", "id_exclude_old"),
        GROUPS("planogram", "group_list", "id_exclude_old"),
        IMAGES("planogram", "img_list", "id_exclude_old"),
        TYPES("planogram", "tt_type_list", "id_exclude_old"),
        VISITS("planogram", "vizit_showcase_list", "id_exclude_old");

        final String mod, act, exclusion;
        Table(String mod, String act, String exclusion) {
            this.mod = mod;
            this.act = act;
            this.exclusion = exclusion;
        }
        public String resource() { return mod + "/" + act; }
        public String exclusionIdField() {
            switch (this) {
                case SHOWCASES: return "select_id";
                case ADDRESSES: case GROUPS: case IMAGES: case TYPES: return "planogram_id";
                default: return "ID";
            }
        }
        public String receiptResource() { return "catalog_full_refresh/" + resource(); }
        public JsonObject baseRequest() {
            JsonObject request = new JsonObject();
            request.addProperty("mod", mod);
            request.addProperty("act", act);
            request.addProperty("nolimit", "1");
            return request;
        }
    }

    private CatalogSyncPolicy() { }

    public static <T> List<T> rows(Boolean state, String error, List<T> rows,
                                   Function<T, ? extends Number> id) {
        // The catalog API reports an exhausted exclusion query as state=false.
        if (Boolean.FALSE.equals(state) && "За заданими умовами список відсутній".equals(error)
                && (rows == null || rows.isEmpty())) return Collections.emptyList();
        return ReferenceSyncPolicy.rows(state, error, rows, id);
    }

    public static <T> List<Long> exclusionIds(List<T> rows, Function<T, ?> key, Predicate<T> downloaded) {
        TreeSet<Long> ids = new TreeSet<>();
        TreeSet<Long> pending = new TreeSet<>();
        for (T row : rows) {
            Object value = key.apply(row);
            if (value == null) continue;
            try {
                long id = Long.parseLong(value.toString().trim());
                if (id <= 0) continue;
                if (downloaded.test(row)) ids.add(id);
                else pending.add(id);
            } catch (NumberFormatException ignored) { }
        }
        ids.removeAll(pending);
        return new ArrayList<>(ids);
    }

    public static boolean fullDue(long now, SynchronizationCursorSDB receipt, int knownCount) {
        return receipt == null || receipt.vpi <= 0 || knownCount == 0 || knownCount < receipt.itemCount
                || now < receipt.vpi || now - receipt.vpi >= FULL_REFRESH_SECONDS;
    }

    public static JsonObject request(Table table, JsonObject base, Collection<Long> ids, boolean full) {
        JsonObject request = base.deepCopy();
        request.remove("dt_change_from");
        request.remove("id_exclude");
        request.remove("id_exclude_old");
        if (!full) {
            TreeSet<Long> unique = new TreeSet<>();
            for (Long id : ids) if (id != null && id > 0) unique.add(id);
            if (!unique.isEmpty()) {
                JsonArray excluded = new JsonArray();
                for (Long id : unique) excluded.add(id);
                request.add(table.exclusion, excluded);
            }
        }
        return request;
    }

    public static String scope(int user, JsonObject base) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(base.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte value : digest) hash.append(String.format(java.util.Locale.ROOT, "%02x", value & 0xff));
            return FilteredSyncPolicy.accountScope(user, hash.toString());
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }

    /** Stored separately from all legacy VPI; call only inside the data-save transaction. */
    public static SynchronizationCursorSDB fullReceipt(Table table, String scope, boolean full,
                                                       long completedAt, int received, int knownCount) {
        if (!full || received <= 0 || completedAt <= 0) return null;
        SynchronizationCursorSDB receipt = new SynchronizationCursorSDB();
        receipt.resource = table.receiptResource();
        receipt.scope = scope;
        receipt.vpi = completedAt;
        receipt.itemCount = knownCount;
        return receipt;
    }

    public static List<Long> showcaseIds(List<ShowcaseSDB> rows) {
        TreeSet<Long> ids = new TreeSet<>();
        TreeSet<Long> incomplete = new TreeSet<>();
        for (ShowcaseSDB row : rows) {
            // rack/list uses select_id, not the local row's ID. Reload missing metadata.
            if (row.selectId == null) continue;
            try {
                long id = Long.parseLong(row.selectId.trim());
                if (id > 0 && id != 444 && id != 1028) ids.add(id);
                if (row.mainOptionId == null) incomplete.add(id);
            } catch (NumberFormatException ignored) { }
        }
        ids.removeAll(incomplete);
        return new ArrayList<>(ids);
    }

    public static List<PlanogrammVizitShowcaseSDB> visitRowsToSave(
            List<PlanogrammVizitShowcaseSDB> received, List<PlanogrammVizitShowcaseSDB> beforeRequest,
            List<PlanogrammVizitShowcaseSDB> local) {
        java.util.Set<Integer> pending = new java.util.HashSet<>();
        for (PlanogrammVizitShowcaseSDB row : beforeRequest) if (Integer.valueOf(1).equals(row.uploadStatus)) pending.add(row.id);
        for (PlanogrammVizitShowcaseSDB row : local) if (Integer.valueOf(1).equals(row.uploadStatus)) pending.add(row.id);
        List<PlanogrammVizitShowcaseSDB> saved = new ArrayList<>();
        for (PlanogrammVizitShowcaseSDB row : received) {
            if (pending.contains(row.id)) continue;
            row.uploadStatus = 0;
            saved.add(row);
        }
        return saved;
    }
}
