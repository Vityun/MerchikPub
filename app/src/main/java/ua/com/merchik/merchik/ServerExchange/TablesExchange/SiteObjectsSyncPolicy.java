package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SiteObjectsResponse;

public final class SiteObjectsSyncPolicy {
    private static final long OVERLAP_SECONDS = 120;

    private SiteObjectsSyncPolicy() { }

    public static JsonObject request(int langId, long from) {
        if (langId <= 0) throw new IllegalArgumentException("Invalid site_objects language");
        JsonObject json = new JsonObject();
        json.addProperty("mod", "site_objects");
        json.addProperty("act", "list");
        json.addProperty("lang_id", Integer.toString(langId));
        json.addProperty("dt_change_from", Long.toString(Math.max(0, from)));
        return json;
    }

    public static long requestWatermark(long previous, Long requestStart,
                                        boolean roomReady, boolean realmReady) {
        if (!roomReady || !realmReady) return 0;
        long from = Math.max(0, previous);
        return requestStart == null ? from
                : Math.min(from, Math.max(0, requestStart - OVERLAP_SECONDS));
    }

    public static long savedWatermark(long from, Long requestStart) {
        return requestStart == null ? Math.max(0, from)
                : Math.max(from, Math.max(0, requestStart - OVERLAP_SECONDS));
    }

    public static List<SiteObjectsSDB> rows(SiteObjectsResponse response, int langId, boolean full) {
        if (response == null || !Boolean.TRUE.equals(response.state)
                || (response.error != null && !response.error.trim().isEmpty())
                || response.objectSQLList == null) {
            throw new IllegalStateException("Invalid site_objects response");
        }
        if (full && response.objectSQLList.isEmpty()) {
            throw new IllegalStateException("Empty initial site_objects dictionary");
        }
        Set<Integer> ids = new HashSet<>();
        for (SiteObjectsSDB row : response.objectSQLList) {
            if (row == null || row.id <= 0 || !ids.add(row.id)) {
                throw new IllegalStateException("Invalid/duplicate site_objects ID");
            }
        }
        for (SiteObjectsSDB row : response.objectSQLList) {
            // Both tables hold one language; mark it even when the server omits lang_id.
            row.langId = Integer.toString(langId);
        }
        return response.objectSQLList;
    }

    public static List<SiteObjectsDB> realmRows(List<SiteObjectsSDB> rows) {
        List<SiteObjectsDB> result = new ArrayList<>(rows.size());
        for (SiteObjectsSDB row : rows) {
            SiteObjectsDB copy = new SiteObjectsDB();
            copy.setID(row.id);
            copy.setID1c(row.iD1c);
            copy.setNm(row.nm);
            copy.setComments(row.comments);
            copy.setScriptMod(row.scriptMod);
            copy.setScriptAct(row.scriptAct);
            copy.setLessonId(row.lessonId);
            copy.setPlatformId(row.platformId);
            copy.setObjectType(row.objectType);
            copy.setDtChange(row.dtChange);
            copy.setAuthorId(row.authorId);
            copy.setNmTranslation(row.nmTranslation);
            copy.setCommentsTranslation(row.commentsTranslation);
            copy.setLangId(row.langId);
            result.add(copy);
        }
        return result;
    }
}
