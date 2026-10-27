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

    @Test public void exactOneAndHalfIsNotOvertime() {
        BigDecimal plan = OvertimeOfferPolicy.plannedSeconds("60");
        assertFalse(OvertimeOfferPolicy.exceeds(5399, plan));
        assertFalse(OvertimeOfferPolicy.exceeds(5400, plan));
        assertTrue(OvertimeOfferPolicy.exceeds(5401, plan));
    }

    @Test public void serverDurationRemainsSecondsWithoutLocalCorrections() {
        BigDecimal average = OvertimeOfferPolicy.averageSeconds(json("{\"state\":true,\"duration\":1759}"));
        assertEquals(new BigDecimal("1759"), average);
        assertFalse(OvertimeOfferPolicy.exceeds(2638, average));
        assertTrue(OvertimeOfferPolicy.exceeds(2639, average));
    }

    @Test public void unavailableServerMeanDoesNotOffer() {
        assertNull(OvertimeOfferPolicy.averageSeconds(null));
        for (String body : new String[]{"{}", "{\"state\":false,\"duration\":100}",
                "{\"state\":true}", "{\"state\":true,\"duration\":null}",
                "{\"state\":true,\"duration\":0}", "{\"state\":true,\"duration\":-1}",
                "{\"state\":true,\"duration\":\"NaN\"}"}) {
            assertNull(body, OvertimeOfferPolicy.averageSeconds(json(body)));
        }
        assertFalse(OvertimeOfferPolicy.exceeds(10000, null));
        assertFalse(OvertimeOfferPolicy.exceeds(10000, BigDecimal.ZERO));
        assertFalse(OvertimeOfferPolicy.exceeds(0, BigDecimal.ONE));
        assertFalse(OvertimeOfferPolicy.exceeds(-1, BigDecimal.ONE));
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

    @Test public void presentationRoundingDoesNotAffectComparison() {
        assertEquals("29", OvertimeOfferPolicy.minutes(new BigDecimal("1759")));
        assertEquals("60", OvertimeOfferPolicy.minutes(new BigDecimal("3600")));
        BigDecimal baseline = OvertimeOfferPolicy.plannedSeconds("60");
        assertEquals("90", OvertimeOfferPolicy.minutes(new BigDecimal("5400")));
        assertEquals("90", OvertimeOfferPolicy.minutes(new BigDecimal("5401")));
        assertFalse(OvertimeOfferPolicy.exceeds(5400, baseline));
        assertTrue(OvertimeOfferPolicy.exceeds(5401, baseline));
    }

    @Test public void displayedMinutesRoundHalfUpWithoutIntermediateRounding() {
        assertEquals("29", OvertimeOfferPolicy.minutes(new BigDecimal("1769.99")));
        assertEquals("30", OvertimeOfferPolicy.minutes(new BigDecimal("1770")));
        assertEquals("31", OvertimeOfferPolicy.minutes(OvertimeOfferPolicy.plannedSeconds("30.5")));
        assertEquals("0", OvertimeOfferPolicy.minutes(BigDecimal.ZERO));
    }

    private static JsonObject json(String value) { return JsonParser.parseString(value).getAsJsonObject(); }
}
