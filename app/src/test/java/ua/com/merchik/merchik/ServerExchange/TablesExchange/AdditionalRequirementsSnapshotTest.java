package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.JsonObject;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirements.AdditionalRequirementsServerData;

import static org.junit.Assert.*;
import static ua.com.merchik.merchik.ServerExchange.TablesExchange.AdditionalRequirementsSnapshot.*;

public class AdditionalRequirementsSnapshotTest {
    @Test public void dailyRequestsExactlySevenDatesWithoutVpiOrVisitFilters() {
        JsonObject request = DAILY.request(LocalDate.of(2027, 1, 1));
        assertEquals(4, request.size());
        assertEquals("additional_requirements", request.get("mod").getAsString());
        assertEquals("list", request.get("act").getAsString());
        assertEquals("2026-12-30", request.get("date_from").getAsString());
        assertEquals("2027-01-05", request.get("date_to").getAsString());
    }

    @Test public void nextKyivMorningIsDueEvenIfLessThan24HoursPassed() {
        LocalDate previous = dayAt(Instant.parse("2026-11-24T21:59:59Z").getEpochSecond());
        LocalDate today = dayAt(Instant.parse("2026-11-24T22:00:00Z").getEpochSecond());
        assertEquals("2026-11-25", today.toString());
        assertFalse(DAILY.alreadyDownloaded(today, null));
        assertFalse(DAILY.alreadyDownloaded(today, previous.toString()));
        assertTrue(DAILY.alreadyDownloaded(today, today.toString()));
    }

    @Test public void fullExchangeRemainsWideAndIndependentOfTheDailyReceipt() {
        LocalDate today = LocalDate.of(2026, 11, 25);
        assertFalse(FULL.alreadyDownloaded(today, today.toString()));
        JsonObject full = FULL.request(today);
        assertEquals("2026-07-28", full.get("date_from").getAsString());
        assertEquals("2026-12-02", full.get("date_to").getAsString());
    }

    @Test public void onlyAnExplicitSuccessfulListCanCompleteTheCheck() {
        AdditionalRequirementsServerData body = new AdditionalRequirementsServerData();
        body.setState(true);
        assertThrows(IllegalStateException.class, () -> DAILY.rows(body));
        body.setList(Collections.emptyList());
        assertTrue(DAILY.rows(body).isEmpty());
        body.setState(false);
        assertThrows(IllegalStateException.class, () -> DAILY.rows(body));
        body.setState(true);
        body.setError("server error");
        assertThrows(IllegalStateException.class, () -> DAILY.rows(body));
    }

    @Test public void rejectsAnIncompleteRowBeforeAnyRowsAreSaved() {
        AdditionalRequirementsServerData body = new AdditionalRequirementsServerData();
        body.setState(true);
        AdditionalRequirementsDB first = new AdditionalRequirementsDB();
        first.setId(66726);
        AdditionalRequirementsDB second = new AdditionalRequirementsDB();
        body.setList(Arrays.asList(first, second));
        assertThrows(IllegalStateException.class, () -> DAILY.rows(body));
        second.setId(66729);
        assertEquals(Arrays.asList(first, second), DAILY.rows(body));
    }
}
