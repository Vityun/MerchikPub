package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.JsonObject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.merchik.merchik.data.Database.Room.ShelfSizeSDB;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShelfSizeResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.TranslatesResponse;
import ua.com.merchik.merchik.data.Translation.SiteTranslationsList;
import ua.com.merchik.merchik.ServerExchange.SyncResponsePolicy;

public final class FilteredSyncPolicy {
    public static final String SHELF_SIZE = "data_list/shelf_size";
    public static final String TRANSLATIONS = "translation/translation_list";
    private static final ZoneId KYIV = ZoneId.of("Europe/Kiev");
    private static final long OVERLAP_SECONDS = 120;

    private FilteredSyncPolicy() { }

    public static LocalDate shelfDate(long nowSeconds) {
        return Instant.ofEpochSecond(nowSeconds).atZone(KYIV).toLocalDate();
    }

    public static String shelfScope(LocalDate today) {
        return today.minusDays(20) + "/" + today.plusDays(1);
    }

    public static String translationScope(int langId) {
        if (langId <= 0) throw new IllegalArgumentException("Invalid translation language");
        return Integer.toString(langId);
    }

    public static String accountScope(int userId, String filterScope) {
        return Math.max(0, userId) + ":" + filterScope;
    }

    public static JsonObject shelfRequest(LocalDate today, long vpi) {
        JsonObject request = new JsonObject();
        request.addProperty("mod", "data_list");
        request.addProperty("act", "shelf_size");
        request.addProperty("dt_from", today.minusDays(20).toString());
        request.addProperty("dt_to", today.plusDays(1).toString());
        request.addProperty("dt_change_from", Long.toString(Math.max(0, vpi)));
        return request;
    }

    public static JsonObject translationRequest(int langId, long vpi) {
        JsonObject request = new JsonObject();
        request.addProperty("mod", "translation");
        request.addProperty("act", "translation_list");
        request.addProperty("lang_id", translationScope(langId));
        request.addProperty("dt", Long.toString(Math.max(0, vpi)));
        return request;
    }

    public static long requestWatermark(SynchronizationCursorSDB cursor, Long started, boolean ready) {
        if (cursor == null || !ready) return 0;
        long previous = Math.max(0, cursor.vpi);
        return started == null ? previous : Math.min(previous, Math.max(0, started - OVERLAP_SECONDS));
    }

    public static SynchronizationCursorSDB savedCursor(String resource, String scope, long from,
                                                       Long started, long count) {
        SynchronizationCursorSDB result = new SynchronizationCursorSDB();
        result.resource = resource;
        result.scope = scope;
        result.vpi = started == null ? Math.max(0, from) : Math.max(from, Math.max(0, started - OVERLAP_SECONDS));
        result.itemCount = count;
        return result;
    }

    public static boolean translationsReady(SynchronizationCursorSDB cursor, long roomCount, long realmCount) {
        return cursor != null && cursor.itemCount == roomCount && roomCount == realmCount;
    }

    public static List<ShelfSizeSDB> shelfRows(ShelfSizeResponse response) {
        if (response == null) throw new IllegalStateException("Empty shelf_size response");
        validate(response.state, response.error, response.list);
        Set<Integer> ids = new HashSet<>();
        for (ShelfSizeSDB row : response.list) {
            if (row == null || row.id == null || row.id <= 0 || !ids.add(row.id)) {
                throw new IllegalStateException("Invalid/duplicate shelf_size ID");
            }
        }
        return response.list;
    }

    public static List<TranslatesSDB> translationRows(TranslatesResponse response, int langId) {
        if (response == null) throw new IllegalStateException("Empty translation_list response");
        validate(response.state, response.error, response.list);
        String language = translationScope(langId);
        Set<String> ids = new HashSet<>();
        for (TranslatesSDB row : response.list) {
            if (row == null || row.id == null || row.id.trim().isEmpty() || !ids.add(row.id)
                    || (row.langId != null && !row.langId.isEmpty() && !language.equals(row.langId))) {
                throw new IllegalStateException("Invalid/duplicate translation ID or language");
            }
        }
        for (TranslatesSDB row : response.list) row.langId = language;
        return response.list;
    }

    private static void validate(Boolean state, String error, List<?> list) {
        SyncResponsePolicy.requireList(state, error, list);
    }

    public static List<SiteTranslationsList> realmTranslations(List<TranslatesSDB> rows) {
        List<SiteTranslationsList> result = new ArrayList<>(rows.size());
        for (TranslatesSDB row : rows) {
            SiteTranslationsList copy = new SiteTranslationsList();
            copy.setID(row.id);
            copy.setNum1c(row.num1c);
            copy.setInternalName(row.internalName);
            copy.setLangId(row.langId);
            copy.setDefaultValue(row.defaultValue);
            copy.setNm(row.nm);
            copy.setTitle(row.title);
            copy.setScriptMod(row.scriptMod);
            copy.setScriptAct(row.scriptAct);
            copy.setUrl(row.url);
            copy.setPlatformId(row.platformId);
            copy.setDtUpdate(row.dtUpdate);
            result.add(copy);
        }
        return result;
    }
}
