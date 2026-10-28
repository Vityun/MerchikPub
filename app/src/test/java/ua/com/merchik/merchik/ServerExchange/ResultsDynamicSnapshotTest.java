package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.List;

import ua.com.merchik.merchik.ServerExchange.TablesExchange.ResultsDynamicSnapshot;
import ua.com.merchik.merchik.data.Database.Room.ResultsDynamicSDB;

import static org.junit.Assert.*;

public class ResultsDynamicSnapshotTest {
    @Test public void requestContainsOnlyEndpointWithoutVpiOrFilters() {
        JsonObject request = ResultsDynamicSnapshot.request();
        assertEquals(2, request.entrySet().size());
        assertEquals("plan", request.get("mod").getAsString());
        assertEquals("results_dynamic", request.get("act").getAsString());
    }

    @Test public void parsesActualResponseWithoutAddressFields() {
        ResultsDynamicSDB row = ResultsDynamicSnapshot.parse(response(row())).get(0);
        assertEquals("44718897", row.id);
        assertEquals("03693", row.isp);
        assertEquals("2609", row.yearmonth);
        assertEquals("10076", row.clientId);
        assertEquals("28847", row.addrId);
        assertEquals("121", row.avgNumDone);
        assertEquals("603", row.totalFace);
        JsonObject saved = new Gson().toJsonTree(row).getAsJsonObject();
        assertEquals(16, saved.entrySet().size());
        assertFalse(saved.has("tp_id"));
        assertFalse(saved.has("tt_id"));
        assertFalse(saved.has("kol_kass"));
    }

    @Test public void preservesFractionalAndLargeValuesWithoutDoubleRounding() {
        JsonObject row = row();
        row.addProperty("avg_num_done", "1234567890123456789.12345");
        assertEquals("1234567890123456789.12345",
                ResultsDynamicSnapshot.parse(response(row)).get(0).avgNumDone);
    }

    @Test public void acceptsJsonNumbersAsStrings() {
        JsonObject row = row();
        row.addProperty("ID", 44718897);
        row.addProperty("total_face", 603);
        assertEquals("603", ResultsDynamicSnapshot.parse(response(row)).get(0).totalFace);
    }

    @Test public void acceptsExplicitEmptySnapshot() {
        assertTrue(ResultsDynamicSnapshot.parse(response()).isEmpty());
    }

    @Test public void acceptsStateOne() {
        JsonObject response = response(row());
        response.addProperty("state", 1);
        assertEquals(1, ResultsDynamicSnapshot.parse(response).size());
    }

    @Test public void rejectsUnsuccessfulAndMissingState() {
        for (String body : new String[]{"{}", "{\"state\":false,\"list\":[]}",
                "{\"state\":0,\"list\":[]}", "{\"state\":null,\"list\":[]}",
                "{\"state\":{},\"list\":[]}"}) {
            reject(JsonParser.parseString(body).getAsJsonObject());
        }
        reject(null);
    }

    @Test public void rejectsMissingNullAndObjectListInsteadOfClearingCache() {
        for (String body : new String[]{"{\"state\":true}", "{\"state\":true,\"list\":null}",
                "{\"state\":true,\"list\":{}}"}) {
            reject(JsonParser.parseString(body).getAsJsonObject());
        }
    }

    @Test public void rejectsNonObjectRowEvenAfterValidRow() {
        JsonObject response = response(row());
        response.getAsJsonArray("list").add(JsonNull.INSTANCE);
        reject(response);
        response.getAsJsonArray("list").set(1, JsonParser.parseString("42"));
        reject(response);
    }

    @Test public void rejectsTruncatedRow() {
        JsonObject row = row();
        row.remove("total_sku");
        reject(response(row));
    }

    @Test public void rejectsNullBooleanAndObjectValues() {
        JsonObject row = row();
        row.add("avg_oos", JsonNull.INSTANCE);
        reject(response(row));
        row.addProperty("avg_oos", true);
        reject(response(row));
        row.add("avg_oos", new JsonObject());
        reject(response(row));
    }

    @Test public void rejectsInvalidKeyParts() {
        for (String field : new String[]{"ID", "client_id", "addr_id"}) {
            for (String value : new String[]{"", "0", "-1", "1.5", "abc"}) {
                JsonObject row = row();
                row.addProperty(field, value);
                reject(response(row));
            }
        }
    }

    @Test public void rejectsInvalidMonthInsteadOfInventingDate() {
        for (String value : new String[]{"2600", "2613", "2026-10", "202610", "26", "2610 "}) {
            JsonObject row = row();
            row.addProperty("yearmonth", value);
            reject(response(row));
        }
    }

    @Test public void rejectsDuplicateServerIdAcrossDifferentKeys() {
        JsonObject other = row();
        other.addProperty("addr_id", "31093");
        reject(response(row(), other));
    }

    @Test public void rejectsDuplicateNaturalKeyWithDifferentServerIdAndIsp() {
        JsonObject other = row();
        other.addProperty("ID", "44897481");
        other.addProperty("isp", "12345");
        reject(response(row(), other));
    }

    @Test public void normalizesNumericKeysToPreventDisguisedDuplicates() {
        JsonObject other = row();
        other.addProperty("ID", "44897481");
        other.addProperty("addr_id", "028847");
        other.addProperty("client_id", "010076");
        reject(response(row(), other));
    }

    @Test public void allowsSameClientAndAddressInDifferentMonths() {
        JsonObject other = row();
        other.addProperty("ID", "44897481");
        other.addProperty("yearmonth", "2610");
        assertEquals(2, ResultsDynamicSnapshot.parse(response(row(), other)).size());
    }

    @Test public void newSnapshotDoesNotAccumulateOldRowsOrValues() {
        List<ResultsDynamicSDB> old = ResultsDynamicSnapshot.parse(response(row()));
        JsonObject updated = row();
        updated.addProperty("ID", "44897481");
        updated.addProperty("total_face", "700");
        List<ResultsDynamicSDB> fresh = ResultsDynamicSnapshot.parse(response(updated));
        assertEquals(1, fresh.size());
        assertEquals("44897481", fresh.get(0).id);
        assertEquals("700", fresh.get(0).totalFace);
        assertEquals("603", old.get(0).totalFace);
    }

    private static void reject(JsonObject response) {
        try {
            ResultsDynamicSnapshot.parse(response);
            fail("Invalid snapshot accepted");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("results_dynamic"));
        }
    }

    private static JsonObject response(JsonObject... rows) {
        JsonObject response = new JsonObject();
        response.addProperty("state", true);
        JsonArray list = new JsonArray();
        for (JsonObject row : rows) list.add(row);
        response.add("list", list);
        return response;
    }

    private static JsonObject row() {
        return JsonParser.parseString("{\"ID\":\"44718897\",\"isp\":\"03693\",\"client_id\":\"10076\","
                + "\"ob_prod_plan\":\"0\",\"avg_num_plan\":\"0\",\"num_main_plan\":\"0\","
                + "\"ob_prod_done\":\"0\",\"avg_num_done\":\"121\",\"num_main_done\":\"76\","
                + "\"avg_oos\":\"87\",\"total_face\":\"603\",\"total_sku\":\"381\","
                + "\"total_documents\":\"5\",\"yearmonth\":\"2609\",\"addr_id\":\"28847\","
                + "\"last_phot\":\"0\",\"tp_id\":\"351\",\"tt_id\":\"32\",\"kol_kass\":\"22\"}")
                .getAsJsonObject();
    }
}
