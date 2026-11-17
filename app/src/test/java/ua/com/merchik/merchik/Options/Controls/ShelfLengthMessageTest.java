package ua.com.merchik.merchik.Options.Controls;

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class ShelfLengthMessageTest {
    private static final String GROUP = "(Соки [35])";
    private static final long CHECKED_AT = Instant.parse("2026-12-02T12:00:00Z").toEpochMilli();

    @Test public void includesEnteredLengthNetworkMeanAndUnlockInstruction() {
        ShelfLengthPolicy.History history = new ShelfLengthPolicy.History();
        history.add(13.8f, 25);
        history.add(14.2f, 1);

        assertEquals(GROUP + ": Длина ПП 59м. указанная Вами, значительно отличается от среднего для данной сети 13.815м."
                        + " Это похоже на ошибку. Укажите ПРАВИЛЬНО длину ВСЕХ полок ВСЕХ товаров ВСЕХ поставщиков ЭТОЙ товарной группы."
                        + " Если Вы все таки уверены в своих данных, введите их, получив код разблокировки!",
                issue("854", 59, history));
    }

    @Test public void shortLengthUsesSameMessageWithoutTrailingZerosInMean() {
        ShelfLengthPolicy.History history = new ShelfLengthPolicy.History();
        history.add(30f, 2);
        String message = issue("90", 9, history);
        assertTrue(message.startsWith(GROUP + ": Длина ПП 9м."));
        assertTrue(message.contains("для данной сети 30м. Это похоже на ошибку."));
        assertTrue(message.endsWith("получив код разблокировки!"));
        assertFalse(message.contains("обман"));
    }

    @Test public void displayRoundingDoesNotChangeTheComparison() {
        ShelfLengthPolicy.History history = new ShelfLengthPolicy.History();
        history.add(30f, 2);
        history.add(30.001f, 1);
        assertTrue(issue("100", 10, history).contains("для данной сети 30м."));
    }

    @Test public void missingHistoryAndAcceptedLengthsStillHaveNoWarning() {
        assertEquals("", issue("100", 10, null));
        ShelfLengthPolicy.History history = new ShelfLengthPolicy.History();
        history.add(30f, 2);
        assertEquals("", issue("100", 10, history));
        assertEquals("", issue("900", 90, history));
    }

    private static String issue(String faces, int metres, ShelfLengthPolicy.History history) {
        ShelfLengthPolicy.Measurement measurement = new ShelfLengthPolicy.Measurement(35);
        measurement.add(faces, metres);
        return measurement.issue(GROUP, history, CHECKED_AT);
    }
}
