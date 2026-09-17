package ua.com.merchik.merchik.Activities.ReferencesActivity.Chat;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChatVisitLinksTest {
    private static final String PREFIX = "\u0410\u041e\u0438-";

    private List<String> references(String message) {
        List<String> result = new ArrayList<>();
        Matcher matcher = ChatVisitLinks.REPORT_DOCUMENT_PATTERN.matcher(message);
        while (matcher.find()) result.add(matcher.group());
        return result;
    }

    @Test
    public void matchesDocumentAtBeginningAndKeepsLeadingZero() {
        assertEquals(Collections.singletonList(PREFIX + "03187535"),
                references(PREFIX + "03187535 (12.08.26)"));
    }

    @Test
    public void findsEveryDocumentInsideMultilineMessage() {
        List<String> numbers = Arrays.asList(PREFIX + "03187535", PREFIX + "03188413",
                PREFIX + "03185874", PREFIX + "03189671");
        assertEquals(numbers, references("Reports:\n" + String.join(" (12.08.26)\n", numbers)));
    }

    @Test
    public void repeatedDocumentsRemainSeparateLinks() {
        String number = PREFIX + "00000001";
        assertEquals(Arrays.asList(number, number), references(number + "; " + number));
    }

    @Test
    public void supportsPunctuationAndEndOfMessage() {
        String number = PREFIX + "03187535";
        for (String message : Arrays.asList(number, "(" + number + ")", "\u00ab" + number + "\u00bb",
                "Report: " + number + ".", "[" + number + "],")) {
            assertEquals(Collections.singletonList(number), references(message));
        }
    }

    @Test
    public void requiresExactlyEightAsciiDigits() {
        for (String digits : Arrays.asList("", "1234567", "123456789", "12345678\u0669",
                "1234 5678", "abcdefgh", "\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668")) {
            assertTrue(references(PREFIX + digits).isEmpty());
        }
    }

    @Test
    public void doesNotMatchPartOfAnotherIdentifier() {
        String number = PREFIX + "03187535";
        for (String message : Arrays.asList("x" + number, "\u0411" + number, "1" + number,
                "_" + number, number + "x", number + "\u044f", number + "_")) {
            assertTrue(references(message).isEmpty());
        }
    }

    @Test
    public void doesNotTreatOtherDocumentPrefixesAsReportNumbers() {
        for (String prefix : Arrays.asList("AO\u0438-", "\u0410\u041e\u0438", "\u0410\u0420\u0435\u043a-", "\u0410\u041e\u0438\u2013")) {
            assertTrue(references(prefix + "03187535").isEmpty());
        }
        assertTrue(references("Plain message without reports").isEmpty());
        assertTrue(references("").isEmpty());
    }

    @Test
    public void matchOffsetsSelectOnlyTheDocumentNumber() {
        String number = PREFIX + "03187535";
        String before = "Text \ud83d\udcdd: ";
        Matcher matcher = ChatVisitLinks.REPORT_DOCUMENT_PATTERN.matcher(before + number + " (date)");
        assertTrue(matcher.find());
        assertEquals(before.length(), matcher.start());
        assertEquals(before.length() + number.length(), matcher.end());
        assertFalse(matcher.find());
    }
}
