package ua.com.merchik.merchik.Options;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.*;

public class OvertimeOfferPolicyTest {
    @Test public void planMinutesAreConvertedToSeconds() {
        assertEquals(new BigDecimal("3600"), OvertimeOfferPolicy.plannedSeconds("60"));
        assertEquals(0, new BigDecimal("1830").compareTo(OvertimeOfferPolicy.plannedSeconds("30,5")));
    }

    @Test public void missingAndInvalidPlanNeverBecomeZeroBaseline() {
        for (String value : new String[]{null, "", " ", "0", "-5", "NaN", "Infinity", "abc"}) {
            assertNull(value, OvertimeOfferPolicy.plannedSeconds(value));
        }
    }

    @Test public void overtimeCoefficientCanBeSupplied() {
        BigDecimal plan = OvertimeOfferPolicy.plannedSeconds("40");
        assertFalse(OvertimeOfferPolicy.exceedsPlan(3000, plan, OvertimeOfferPolicy.DEFAULT_OVERTIME_PERCENT));
        assertTrue(OvertimeOfferPolicy.exceedsPlan(3000, plan, 20));
    }

    @Test public void serverDurationInMinutesIsConvertedToSecondsWithoutRounding() {
        assertEquals(new BigDecimal("1680"), OvertimeOfferPolicy.averageSeconds(json("{\"state\":true,\"duration\":28}")));
        assertEquals(new BigDecimal("420"), OvertimeOfferPolicy.averageSeconds(json("{\"state\":true,\"duration\":7}")));
        assertEquals(new BigDecimal("420.60"), OvertimeOfferPolicy.averageSeconds(json("{\"state\":true,\"duration\":7.01}")));
    }

    @Test public void unavailableServerMeanDoesNotOffer() {
        assertNull(OvertimeOfferPolicy.averageSeconds(null));
        for (String body : new String[]{"{}", "{\"state\":false,\"duration\":100}",
                "{\"state\":true}", "{\"state\":true,\"duration\":null}",
                "{\"state\":true,\"duration\":0}", "{\"state\":true,\"duration\":-1}",
                "{\"state\":true,\"duration\":\"NaN\"}"}) {
            assertNull(body, OvertimeOfferPolicy.averageSeconds(json(body)));
        }
        assertFalse(OvertimeOfferPolicy.exceedsPlan(10000, null, OvertimeOfferPolicy.DEFAULT_OVERTIME_PERCENT));
        assertFalse(OvertimeOfferPolicy.exceedsPlan(10000, BigDecimal.ZERO, OvertimeOfferPolicy.DEFAULT_OVERTIME_PERCENT));
        assertFalse(OvertimeOfferPolicy.exceedsPlan(0, BigDecimal.ONE, OvertimeOfferPolicy.DEFAULT_OVERTIME_PERCENT));
        assertFalse(OvertimeOfferPolicy.exceedsPlan(-1, BigDecimal.ONE, OvertimeOfferPolicy.DEFAULT_OVERTIME_PERCENT));
    }

    @Test public void requestExcludesExactLongDad2AndScopesTheAverage() {
        long dad2 = 1231026038653061512L;
        JsonObject request = OvertimeOfferPolicy.averageRequest("9382", 38653, 998, dad2);
        JsonObject decoded = json(request.toString());
        assertEquals(dad2, decoded.get("code_dad2_exclude").getAsLong());
        assertEquals("9382", decoded.get("client_id").getAsString());
        assertEquals(38653, decoded.get("addr_id").getAsInt());
        assertEquals(998, decoded.get("theme_id").getAsInt());
        assertEquals("plan", decoded.get("mod").getAsString());
        assertEquals("avg_time", decoded.get("act").getAsString());
    }

    @Test public void diagnosticSendsRequiredThemeWithoutExcludingAVisit() {
        JsonObject request = OvertimeOfferPolicy.averageRequest("46403", 28847, 998);
        assertEquals(json("{\"mod\":\"plan\",\"act\":\"avg_time\",\"client_id\":\"46403\","
                + "\"addr_id\":28847,\"theme_id\":998}"), request);
    }

    @Test public void presentationRoundingDoesNotAffectComparison() {
        assertEquals("29", OvertimeOfferPolicy.minutes(new BigDecimal("1759")));
        assertEquals("60", OvertimeOfferPolicy.minutes(new BigDecimal("3600")));
        BigDecimal baseline = OvertimeOfferPolicy.plannedSeconds("60");
        assertEquals("78", OvertimeOfferPolicy.minutes(new BigDecimal("4680")));
        assertEquals("78", OvertimeOfferPolicy.minutes(new BigDecimal("4681")));
        assertFalse(OvertimeOfferPolicy.exceedsPlan(4680, baseline, OvertimeOfferPolicy.DEFAULT_OVERTIME_PERCENT));
        assertTrue(OvertimeOfferPolicy.exceedsPlan(4681, baseline, OvertimeOfferPolicy.DEFAULT_OVERTIME_PERCENT));
    }

    @Test public void displayedMinutesRoundHalfUpWithoutIntermediateRounding() {
        assertEquals("29", OvertimeOfferPolicy.minutes(new BigDecimal("1769.99")));
        assertEquals("30", OvertimeOfferPolicy.minutes(new BigDecimal("1770")));
        assertEquals("31", OvertimeOfferPolicy.minutes(OvertimeOfferPolicy.plannedSeconds("30.5")));
        assertEquals("0", OvertimeOfferPolicy.minutes(BigDecimal.ZERO));
    }

    private static JsonObject json(String value) { return JsonParser.parseString(value).getAsJsonObject(); }
}
