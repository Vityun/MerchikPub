package ua.com.merchik.merchik.Options.Controls;

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class ShelfLengthPolicyTest {
    private static final String GROUP = "(Соки [88])";

    private static String issue(String faces, int metres, ShelfLengthPolicy.History history) {
        ShelfLengthPolicy.Measurement m = new ShelfLengthPolicy.Measurement(88);
        m.add(faces, metres);
        return m.issue(GROUP, history);
    }

    private static ShelfLengthPolicy.History history(float width, int count) {
        ShelfLengthPolicy.History history = new ShelfLengthPolicy.History();
        history.add(width, count);
        return history;
    }

    @Test public void usesEndOfDocumentDayInKyivAcrossClockChange() {
        assertEquals(Instant.parse("2026-10-25T22:00:00Z").getEpochSecond(),
                ShelfLengthPolicy.endExclusive(Instant.parse("2026-10-24T21:00:00Z").toEpochMilli()));
        assertEquals(Instant.parse("2026-06-04T21:00:00Z").getEpochSecond(),
                ShelfLengthPolicy.endExclusive(Instant.parse("2026-06-04T12:00:00Z").toEpochMilli()));
    }

    @Test public void missingValuesHavePriorityAndIdentifyTheGroup() {
        assertTrue(issue(null, 0, history(99, 2)).contains(GROUP + " не указаны ни"));
        assertTrue(issue("0", 3, null).contains("в поле ФЕЙСОВ"));
        assertTrue(issue("15", 0, null).contains("в поле ДлинаПП"));
        assertTrue(issue("нет", 3, null).contains("в поле ФЕЙСОВ"));
    }

    @Test public void densityIsRoundedBeforeCheckingInclusiveLimits() {
        assertEquals("", issue("99", 20, null));       // 4.95 -> 5.0
        assertTrue(issue("98", 20, null).contains("4.9 фейсов"));
        assertEquals("", issue("5004", 100, null));    // 50.04 -> 50.0
        assertTrue(issue("1001", 20, history(1000, 2)).contains("50.1 фейсов"));
    }

    @Test public void oneMeasurementOrZeroMeanCannotRejectLength() {
        assertEquals("", issue("50", 10, history(200, 1)));
        assertEquals("", issue("50", 10, history(0, 9)));
    }

    @Test public void bothFactorOfThreeEndpointsAreAccepted() {
        ShelfLengthPolicy.History h = history(30, 2);
        assertEquals("", issue("100", 10, h));
        assertEquals("", issue("900", 90, h));
        assertTrue(issue("90", 9, h).contains("значительно отличается"));
        assertTrue(issue("910", 91, h).contains("значительно отличается"));
    }

    @Test public void fractionalMeanIsNotRoundedForComparison() {
        ShelfLengthPolicy.History h = history(29.9f, 1);
        h.add(30.1f, 1);
        assertEquals("", issue("100", 10, h));
        h.add(30.1f, 1);
        assertTrue(issue("100", 10, h).contains("значительно отличается"));
    }

    @Test public void historyWeightsMeasurementsNotDistinctLengths() {
        ShelfLengthPolicy.History h = history(10, 1);
        h.add(40f, 5);   // actual mean = 35, not 25
        assertTrue(issue("100", 10, h).contains("значительно отличается"));
    }

    @Test public void productRowsSumFacesAndMetresBeforeEvaluation() {
        ShelfLengthPolicy.Measurement m = new ShelfLengthPolicy.Measurement(88);
        m.add("12", 0);
        m.add("8", 4);
        assertEquals("", m.issue(GROUP, null));
    }

    @Test public void emptyReportIsNotTheSameAsNoMeasurementProducts() {
        assertFalse(ShelfLengthPolicy.signal(false, 0, 0));
        assertTrue(ShelfLengthPolicy.signal(true, 0, 0));
        assertFalse(ShelfLengthPolicy.signal(true, 4, 0));
        assertTrue(ShelfLengthPolicy.signal(true, 4, 2));
        assertTrue(ShelfLengthPolicy.summary(false, 0, 0, 0).startsWith("Не определены клиенты"));
        assertTrue(ShelfLengthPolicy.summary(true, 0, 0, 0).startsWith("Данные о Длине полок не заполнены"));
        assertTrue(ShelfLengthPolicy.summary(true, 4, 0, 0).contains("0 из 0"));
        assertTrue(ShelfLengthPolicy.summary(true, 4, 3, 2).contains("Для (2)"));
    }
}
