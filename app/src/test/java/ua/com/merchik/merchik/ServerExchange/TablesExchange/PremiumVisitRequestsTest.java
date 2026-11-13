package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.JsonObject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PremiumVisitRequestsTest {
    private static final String DAD2 = "1121126044315061639";
    private static final String DATE = "2026-11-12";

    @Test
    public void optionsKeepExactVisitIdAndDate() {
        JsonObject request = OptionsExchange.createRequest(DATE, DATE, DAD2);

        assertEquals("plan", request.get("mod").getAsString());
        assertEquals("options_list", request.get("act").getAsString());
        assertEquals(DAD2, request.get("code_dad2").getAsString());
        assertTrue(request.get("code_dad2").getAsJsonPrimitive().isString());
        assertEquals(DATE, request.get("date_from").getAsString());
        assertEquals(DATE, request.get("date_to").getAsString());
    }

    @Test
    public void legacyEmptyDad2DoesNotCrashOptionsRequest() {
        JsonObject request = OptionsExchange.createRequest(DATE, DATE, "");

        assertEquals("", request.get("code_dad2").getAsString());
        assertEquals(DATE, request.get("date_from").getAsString());
    }

    @Test
    public void legacyNullDad2DoesNotCrashOptionsRequest() {
        JsonObject request = OptionsExchange.createRequest(DATE, DATE, null);

        assertFalse(request.has("code_dad2"));
        assertEquals(DATE, request.get("date_from").getAsString());
    }

    @Test
    public void missingVisitReportLoadsFromZeroDespiteRecentGlobalVpi() {
        JsonObject request = ReportPrepareExchange.createRequest(DATE, DATE, DAD2, 1794562462L);

        assertEquals("report_prepare", request.get("mod").getAsString());
        assertEquals("list_data", request.get("act").getAsString());
        assertEquals(DAD2, request.get("code_dad2").getAsString());
        assertEquals(DATE, request.get("date_from").getAsString());
        assertEquals(DATE, request.get("date_to").getAsString());
        assertEquals("0", request.get("dt_change_from").getAsString());
    }

    @Test
    public void targetedReportWithoutGlobalCheckpointAlsoStartsAtZero() {
        JsonObject request = ReportPrepareExchange.createRequest(DATE, DATE, DAD2, 0);

        assertEquals("0", request.get("dt_change_from").getAsString());
    }

    @Test
    public void legacyDateOnlyReportRetainsItsRequestCursor() {
        for (String dad2 : new String[]{null, "", " "}) {
            JsonObject request = ReportPrepareExchange.createRequest(DATE, DATE, dad2, 1794562462L);

            assertEquals("1794562462", request.get("dt_change_from").getAsString());
        }
    }
}
