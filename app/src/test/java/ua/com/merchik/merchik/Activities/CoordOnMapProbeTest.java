package ua.com.merchik.merchik.Activities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.CoordOnMapSDB;

import static org.junit.Assert.*;

public class CoordOnMapProbeTest {
    @Test public void rawChunksPreserveExactTextAndUnicodeAtTheBoundary() {
        String raw = "{\n " + String.join("", Collections.nCopies(896, "a")) + "\uD83D\uDE00" + " : 1.00e+02 }\r\n";
        List<String> chunks = new ArrayList<>();
        CoordOnMapProbe.logChunks(raw, (part, chunk) -> {
            assertEquals(chunks.size() + 1, part.intValue());
            assertFalse(Character.isHighSurrogate(chunk.charAt(chunk.length() - 1)));
            chunks.add(chunk);
        });
        assertEquals(2, chunks.size());
        assertEquals(raw, String.join("", chunks));
    }

    @Test public void emptyResponseProducesOneEmptyLogEntry() {
        List<String> chunks = new ArrayList<>();
        CoordOnMapProbe.logChunks("", (part, chunk) -> chunks.add(chunk));
        assertEquals(Collections.singletonList(""), chunks);
    }

    @Test public void requestsFullListWithNumericZeroFilters() {
        JsonObject request = CoordOnMapProbe.request();
        assertEquals("data_list", request.get("mod").getAsString());
        assertEquals("coord_on_map", request.get("act").getAsString());
        assertEquals("0", request.get("dt_change_from").toString());
        assertEquals("0", request.get("dt_change_to").toString());
    }

    @Test public void readsServerIdsNamesReferencesAndZeroTimestamps() {
        List<CoordOnMapSDB> rows = CoordOnMapProbe.readRows(JsonParser.parseString(
                "{\"state\":true,\"list\":[{\"ID\":\"109\",\"nm\":\"Шулявка\",\"parent_id\":\"1\","
                        + "\"supervisor_id\":\"63651\",\"city_id\":\"2\",\"operator_id\":\"263360\","
                        + "\"author_id\":\"20376\",\"dt_update\":\"0\"},{\"ID\":513,\"nm\":\"Офис\"}]}"));
        assertEquals(2, rows.size());
        CoordOnMapSDB row = rows.get(0);
        assertEquals(109L, row.id);
        assertEquals("Шулявка", row.nm);
        assertEquals(1L, row.parentId);
        assertEquals(63651L, row.supervisorId);
        assertEquals(2L, row.cityId);
        assertEquals(263360L, row.operatorId);
        assertEquals(20376L, row.authorId);
        assertEquals(0L, row.dtUpdate);
        assertEquals(513L, rows.get(1).id);
        assertEquals(0L, rows.get(1).supervisorId);
        assertEquals(0L, rows.get(1).operatorId);
    }

    @Test public void emptyListIsValidButInvalidBatchesAreNotImported() {
        assertTrue(CoordOnMapProbe.readRows(JsonParser.parseString("{\"state\":true,\"list\":[]}")).isEmpty());
        for (String invalid : new String[]{"{\"state\":false,\"list\":[]}", "{\"list\":[]}",
                "{\"state\":true,\"need_auth\":1,\"list\":[]}", "{\"state\":true}",
                "{\"state\":true,\"list\":[{\"ID\":109},{\"ID\":\"0\"}]}"}) {
            try {
                CoordOnMapProbe.readRows(JsonParser.parseString(invalid));
                fail("Invalid response accepted: " + invalid);
            } catch (IllegalArgumentException expected) {
                // A malformed batch must never reach the DAO.
            }
        }
    }

    @Test public void reportsMixedTypesAndNullWithoutLoggingValues() {
        String report = CoordOnMapProbe.describe(JsonParser.parseString(
                "{\"state\":true,\"list\":[{\"lat\":\"50.12345\",\"name\":\"private\"},{\"lat\":null,\"id\":63421}]}"));
        assertTrue(report.contains("state=true"));
        assertTrue(report.contains("$.list: array[2]"));
        assertTrue(report.contains("$.list[].lat: string | null"));
        assertTrue(report.contains("$.list[].id: number"));
        assertFalse(report.contains("50.12345"));
        assertFalse(report.contains("63421"));
        assertFalse(report.contains("private"));
    }

    @Test public void distinguishesEmptyListAndAuthorizationFailure() {
        JsonObject empty = JsonParser.parseString("{\"state\":true,\"list\":[]}").getAsJsonObject();
        assertFalse(CoordOnMapProbe.failed(empty));
        assertTrue(CoordOnMapProbe.describe(empty).contains("$.list: array[0]"));
        assertTrue(CoordOnMapProbe.failed(JsonParser.parseString("{\"state\":false}")));
        assertTrue(CoordOnMapProbe.failed(JsonParser.parseString("{\"need_auth\":1}")));
        assertTrue(CoordOnMapProbe.failed(null));
    }
}
