package ua.com.merchik.merchik.Utils.text;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MvsLinkedTextFormatterTest {
    private final List<String> issues = new ArrayList<>();

    private String format(String source) {
        return MvsLinkedTextFormatter.format(source, path -> "https://example.test/" + path, issues::add);
    }

    @Test public void acceptsThreeFieldsWithoutPlatform() {
        assertEquals("<a href=\"https://example.test/mobile.php?mod=images_view&amp;act=image_rotate&amp;id=123\">Photo</a>",
                format("{фото|123|Photo}"));
        assertTrue(issues.isEmpty());
    }

    @Test public void acceptsFourFieldsIncludingEmptyPlatform() {
        String expected = format("{документ|123|Visit}");
        assertEquals(expected, format("{документ|123|Visit|4}"));
        assertEquals(expected, format("{документ|123|Visit|}"));
        assertEquals(expected, format("{документ|123|Visit| }"));
        assertTrue(issues.isEmpty());
    }

    @Test public void normalizesTypeCaseAndWhitespace() {
        assertEquals(format("{фото|123|Photo}"), format("{ ФОТО | 123 | Photo |4}"));
    }

    @Test public void preservesInvalidBlocksIncludingBraces() {
        String[] invalid = {"{}", "{x}", "{фото|123}", "{фото|123|Photo|4|extra}",
                "{|123|Photo}", "{фото||Photo}", "{фото|123| }", "{unknown|123|Photo}"};
        for (String block : invalid) assertEquals(block, format(block));
        assertEquals(invalid.length, issues.size());
        assertTrue(issues.get(0).contains("block={}"));
    }

    @Test public void invalidBlockDoesNotPreventLaterLinks() {
        String first = format("{фото|1|First}");
        String second = format("{фото|2|Second}");
        assertEquals("Before " + first + " {bad} " + second + " after",
                format("Before {фото|1|First} {bad} {фото|2|Second} after"));
        assertEquals(1, issues.size());
    }

    @Test public void retainsUnclosedMarkerAndContinuesAtNextMarker() {
        assertEquals("Before {bad " + format("{фото|2|Second}") + " after",
                format("Before {bad {фото|2|Second} after"));
        assertEquals("Before {unfinished", format("Before {unfinished"));
        assertEquals(2, issues.size());
        assertTrue(issues.get(0).startsWith("unclosed_marker"));
    }

    @Test public void nullAndPlainTextAreSafe() {
        assertEquals("", format(null));
        assertEquals("", format(""));
        assertEquals("Text<br><br>after", format("Text<br><br>after"));
        assertEquals("text=null", issues.get(0));
    }

    @Test public void missingAuthenticationKeepsBlockAsText() {
        assertEquals("{фото|123|Photo}", MvsLinkedTextFormatter.format("{фото|123|Photo}",
                path -> null, issues::add));
        assertEquals("{фото|123|Photo}", MvsLinkedTextFormatter.format("{фото|123|Photo}",
                null, issues::add));
        assertEquals(2, issues.size());
    }

    @Test public void linkFailureDoesNotLoseOtherTextOrLinks() {
        String result = MvsLinkedTextFormatter.format("Before {фото|1|Bad} then {фото|2|Good}", path -> {
            if (path.endsWith("=1")) throw new IllegalStateException("private details");
            return "https://example.test/photo2";
        }, issues::add);
        assertEquals("Before {фото|1|Bad} then <a href=\"https://example.test/photo2\">Good</a>", result);
        assertFalse(issues.toString().contains("private details"));
    }

    @Test public void escapesLabelsUrlsAndInvalidBlocksAsHtml() {
        assertEquals("<a href=\"https://example.test/?x=&quot;y&quot;&amp;z=1\">&lt;b&gt;A&amp;B&lt;/b&gt;</a>",
                MvsLinkedTextFormatter.format("{фото|1|<b>A&B</b>}",
                        path -> "https://example.test/?x=\"y\"&z=1", issues::add));
        assertEquals("{bad|&lt;b&gt;&amp;}", format("{bad|<b>&}"));
    }

    @Test public void keepsEveryLegacyRoute() {
        String[][] routes = {
                {"фото", "mod=images_view&amp;act=image_rotate&amp;id="},
                {"фото_шахматка_фильтр", "mod=photo&amp;act=grid_table&amp;"},
                {"достижение", "mod=images_achieve&amp;act=view&amp;id="},
                {"рекламация", "mod=reclamation&amp;act=view&amp;id="},
                {"задача", "mod=reclamation&amp;act=view&amp;id="},
                {"рекламация_1с", "mod=reclamation&amp;act=reclamation_by_num&amp;doc_num="},
                {"задача_1с", "mod=reclamation&amp;act=reclamation_by_num&amp;doc_num="},
                {"аудио_отчёт", "mod=audio&amp;act=audio_rotate&amp;id="},
                {"детализированный_отчёт", "mod=report_prepare&amp;act=report_by_num&amp;doc_num="},
                {"посещение_фильтр", "mod=report_prepare&amp;act=prepare_tovar&amp;ppa_only=1&amp;"},
                {"товары", "mod=report_prepare&amp;act=prepare_tovar&amp;ppa_only=1&amp;"},
                {"документ", "mod=plan&amp;act=open_document_item&amp;code_dad2="},
                {"контенты", "mod=standart&amp;act=view&amp;code_dad2="}
        };
        for (String[] route : routes) {
            assertEquals(route[0], "<a href=\"https://example.test/mobile.php?" + route[1] + "123\">Link</a>",
                    format("{" + route[0] + "|123|Link|4}"));
        }
        assertTrue(issues.isEmpty());
    }

    @Test public void diagnosticsAreBoundedAndSingleLine() {
        StringBuilder block = new StringBuilder("{bad\n\r");
        for (int i = 0; i < 1000; i++) block.append('x');
        block.append('}');
        assertEquals(block.toString(), format(block.toString()));
        assertTrue(issues.get(0).length() < 350);
        assertFalse(issues.get(0).contains("\n"));
        assertFalse(issues.get(0).contains("\r"));
    }
}
