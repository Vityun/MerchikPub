package ua.com.merchik.merchik.Options;

import com.google.gson.JsonParser;
import java.math.BigDecimal;
import org.junit.Test;

import static org.junit.Assert.*;

public class OvertimeRulePreviewTest {
    private final BigDecimal plan = OvertimeOfferPolicy.plannedSeconds("30");

    @Test public void serverRequestPrecheckKeepsCasesThatCanPassTheTenPercentTier() {
        int precheck = OvertimeOfferPolicy.MIN_OVERTIME_PERCENT;
        assertEquals(10, precheck);
        assertFalse(OvertimeOfferPolicy.exceedsPlan(33 * 60, plan, precheck));
        assertTrue(OvertimeOfferPolicy.exceedsPlan(33 * 60 + 1, plan, precheck));
    }

    @Test public void serverAverageUsesExactSixtyAndOneHundredTwentyMinuteBoundaries() {
        assertEquals(30, OvertimeOfferPolicy.overtimePercentForAverage(average("59.99")));
        assertEquals(20, OvertimeOfferPolicy.overtimePercentForAverage(average("60")));
        assertEquals(20, OvertimeOfferPolicy.overtimePercentForAverage(average("120")));
        assertEquals(10, OvertimeOfferPolicy.overtimePercentForAverage(average("120.01")));
    }

    @Test public void selectedPercentIsAppliedToPlanAndRequiresStrictExcess() {
        assertFalse(exceedsPlan(39 * 60, "59.99"));
        assertTrue(exceedsPlan(39 * 60 + 1, "59.99"));
        assertFalse(exceedsPlan(36 * 60, "120"));
        assertTrue(exceedsPlan(36 * 60 + 1, "120"));
        assertFalse(exceedsPlan(33 * 60, "120.01"));
        assertTrue(exceedsPlan(33 * 60 + 1, "120.01"));
    }

    @Test public void highAverageDoesNotRelaxTheIndependentThirtyPercentShortfallLimit() {
        BigDecimal longPlan = OvertimeOfferPolicy.plannedSeconds("200");
        BigDecimal tooShort = average("139.99");
        assertEquals(10, OvertimeOfferPolicy.overtimePercentForAverage(tooShort));
        assertFalse(OvertimeOfferPolicy.acceptsAverage(longPlan, tooShort));
        assertTrue(OvertimeOfferPolicy.acceptsAverage(longPlan, average("140")));
    }

    @Test public void tenMinuteAverageRejectsOvertimeForThirtyMinutePlan() {
        assertTrue(exceedsPlan(45 * 60, "10"));
        BigDecimal average = average("10");
        assertEquals(new BigDecimal("600"), average);
        assertFalse(OvertimeOfferPolicy.acceptsAverage(plan, average));
    }

    @Test public void averageExactlyThirtyPercentBelowPlanIsAccepted() {
        assertFalse(OvertimeOfferPolicy.acceptsAverage(plan, average("20.99")));
        assertTrue(OvertimeOfferPolicy.acceptsAverage(plan, average("21")));
    }

    @Test public void thereIsNoSecondOvertimeThresholdAgainstServerAverage() {
        assertTrue(exceedsPlan(40 * 60, "50"));
        assertTrue(OvertimeOfferPolicy.acceptsAverage(plan, average("50")));
    }

    @Test public void missingOrZeroAverageIsNotEnoughToOfferOvertime() {
        assertFalse(OvertimeOfferPolicy.acceptsAverage(plan, OvertimeOfferPolicy.averageSeconds(null)));
        assertFalse(OvertimeOfferPolicy.acceptsAverage(plan, average("0")));
    }

    private boolean exceedsPlan(long actualSeconds, String averageMinutes) {
        return OvertimeOfferPolicy.exceedsPlan(actualSeconds, plan,
                OvertimeOfferPolicy.overtimePercentForAverage(average(averageMinutes)));
    }

    private static BigDecimal average(String minutes) {
        return OvertimeOfferPolicy.averageSeconds(JsonParser.parseString(
                "{\"state\":true,\"duration\":" + minutes + "}").getAsJsonObject());
    }
}
