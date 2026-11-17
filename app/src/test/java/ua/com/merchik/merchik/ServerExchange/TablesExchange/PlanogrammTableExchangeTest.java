package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.junit.Test;

import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB;
import ua.com.merchik.merchik.retrofit.FlexibleIntegerTypeAdapter;

import static org.junit.Assert.*;

public class PlanogrammTableExchangeTest {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Integer.class, new FlexibleIntegerTypeAdapter(null))
            .create();

    @Test public void requestIncludesPreviouslyDownloadedPlanograms() {
        JsonObject request = gson.toJsonTree(PlanogrammTableExchange.planogramRequest()).getAsJsonObject();
        assertEquals("planogram", request.get("mod").getAsString());
        assertEquals("list", request.get("act").getAsString());
        assertEquals("1", request.get("nolimit").getAsString());
        assertFalse(request.has("id_exclude_old"));
        assertEquals(3, request.size());
    }

    @Test public void missingAndEmptyProductGroupRemainCommon() {
        assertNull(gson.fromJson("{\"ID\":1104}", PlanogrammSDB.class).tovarGrp);
        for (String value : new String[]{"null", "\"\"", "\"  \""}) {
            assertNull(planogram(value).tovarGrp);
        }
    }

    @Test public void zeroProductGroupRemainsCommon() {
        assertEquals(Integer.valueOf(0), planogram("0").tovarGrp);
        assertEquals(Integer.valueOf(0), planogram("\"0\"").tovarGrp);
    }

    @Test public void numericAndStringProductGroupsUseSameIdentifier() {
        assertEquals(Integer.valueOf(35), planogram("35").tovarGrp);
        assertEquals(Integer.valueOf(35), planogram("\"35\"").tovarGrp);
    }

    @Test public void metadataRefreshCanChangeOrClearProductGroupWithoutChangingId() {
        PlanogrammSDB previous = planogram("35");
        PlanogrammSDB updated = planogram("36");
        PlanogrammSDB common = planogram("\"\"");
        assertEquals(previous.id, updated.id);
        assertEquals(previous.id, common.id);
        assertEquals(Integer.valueOf(36), updated.tovarGrp);
        assertNull(common.tovarGrp);
    }

    private PlanogrammSDB planogram(String groupJson) {
        return gson.fromJson("{\"ID\":1104,\"tovar_grp\":" + groupJson + "}", PlanogrammSDB.class);
    }
}
