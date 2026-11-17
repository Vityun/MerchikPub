package ua.com.merchik.merchik.Options.Controls;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.time.Instant;

import ua.com.merchik.merchik.database.room.DaoInterfaces.ShelfSizeDao;

import static org.junit.Assert.*;

public class ShelfLengthHistoryLogTest {
    @Test public void logsEverySourceIdAndMeasurementFieldWithExactVisitId() {
        ShelfSizeDao.HistoryMeasurement row = row("2026-11-15T10:00:00Z");
        row.sourceIds = "101,102";
        row.sourceNetworkIds = "351,0";
        row.groupId = 35;
        row.addrId = 28847;
        row.addressName = "Store \"A\"\nSecond line";
        row.clientId = "9382";
        row.clientName = "Client";
        row.authorId = 42;
        row.authorName = "Author";
        row.width = 9.7f;
        row.planValue = 0f;
        row.dtDay = "2026-11-15";
        row.dtChange = row.dt + 60;

        String log = OptionControlShelfLength.historyDetails(1161126028847061792L, 351, row);
        JsonObject data = JsonParser.parseString(log).getAsJsonObject();
        assertFalse(log.contains("\n"));
        assertEquals(1161126028847061792L, data.get("dad2").getAsLong());
        assertEquals(351, data.get("network").getAsInt());
        assertEquals("101,102", data.get("sourceIds").getAsString());
        assertEquals("351,0", data.get("sourceNetworkIds").getAsString());
        assertEquals(35, data.get("groupId").getAsInt());
        assertEquals(28847, data.get("addrId").getAsInt());
        assertEquals(row.addressName, data.get("addressName").getAsString());
        assertEquals("9382", data.get("clientId").getAsString());
        assertEquals("Client", data.get("clientName").getAsString());
        assertEquals(42, data.get("authorId").getAsInt());
        assertEquals("Author", data.get("authorName").getAsString());
        assertEquals(9.7f, data.get("width").getAsFloat(), 0f);
        assertEquals(0f, data.get("planValue").getAsFloat(), 0f);
        assertEquals(row.dt, data.get("dt").getAsLong());
        assertEquals(row.dtDay, data.get("dtDay").getAsString());
        assertEquals(row.dtChange.longValue(), data.get("dtChange").getAsLong());
        assertEquals("2026-11-15T12:00:00+02:00", data.get("dateKyiv").getAsString());
    }

    @Test public void missingDictionaryEntriesAndNullableValuesAreExplicit() {
        JsonObject data = JsonParser.parseString(OptionControlShelfLength.historyDetails(123, 351,
                row("2026-11-15T10:00:00Z"))).getAsJsonObject();
        for (String key : new String[]{"addressName", "clientName", "authorId", "authorName", "width", "dtDay", "dtChange"}) {
            assertTrue(key, data.has(key) && data.get(key).isJsonNull());
        }
    }

    @Test public void dateUsesKyivSummerOffsetRegardlessOfDeviceTimezone() {
        JsonObject data = JsonParser.parseString(OptionControlShelfLength.historyDetails(123, 351,
                row("2026-07-15T10:00:00Z"))).getAsJsonObject();
        assertEquals("2026-07-15T13:00:00+03:00", data.get("dateKyiv").getAsString());
    }

    private static ShelfSizeDao.HistoryMeasurement row(String instant) {
        ShelfSizeDao.HistoryMeasurement row = new ShelfSizeDao.HistoryMeasurement();
        row.dt = Instant.parse(instant).getEpochSecond();
        return row;
    }
}
