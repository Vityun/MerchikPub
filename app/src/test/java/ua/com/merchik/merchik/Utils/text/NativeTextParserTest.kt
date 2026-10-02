package ua.com.merchik.merchik.Utils.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.com.merchik.merchik.Utils.text.NativeTextAction.Kind

class NativeTextParserTest {
    private val document = "\u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442"
    private val photo = "\u0444\u043e\u0442\u043e"
    private val task = "\u0437\u0430\u0434\u0430\u0447\u0430"
    private val reclamation = "\u0440\u0435\u043a\u043b\u0430\u043c\u0430\u0446\u0438\u044f"
    private val phone = "\u0442\u0435\u043b\u0435\u0444\u043e\u043d"
    private val unlock = "\u043a\u043e\u0434 \u0440\u0430\u0437\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u043a\u0438"
    private val report = "\u0410\u041e\u0438-03217103"
    private val dad2 = "1021026038634061377"
    private val oneC = "_1\u0441"

    @Test
    fun legacyDocumentMarkerDisplaysClickableWordAndKeepsExactDad2() {
        val parsed = NativeTextParser.parse("{$document|$dad2|$dad2}")
        assertEquals(document, parsed.text)
        assertEquals(listOf(NativeTextLink(0, document.length, NativeTextAction(Kind.VISIT_BY_DAD2, dad2))), parsed.links)
    }

    @Test
    fun unlockNotificationContainsIndependentCodeAndDocumentActions() {
        val before = "Sender $unlock d18d option 84006: "
        val after = ". 02.10.2026, address, client."
        val parsed = NativeTextParser.parse(before + "{$document|$dad2|$dad2}" + after)
        assertEquals(before + document + after, parsed.text)
        assertEquals(listOf(Kind.COPY_UNLOCK_CODE, Kind.VISIT_BY_DAD2), parsed.links.map { it.action.kind })
        assertEquals(listOf("d18d", dad2), parsed.links.map { it.action.value })
        assertEquals(listOf("d18d", document), labels(parsed))
    }

    @Test
    fun documentSupportsShortFormCustomLabelWhitespaceAndWebPlatform() {
        for (marker in listOf("{$document|$dad2}", "{$document|$dad2|}",
            "{$document|$dad2|$dad2|4}", "{$document|code_dad2:$dad2|$dad2}")) {
            val parsed = NativeTextParser.parse(marker)
            assertEquals(document, parsed.text)
            assertEquals(NativeTextAction(Kind.VISIT_BY_DAD2, dad2), parsed.links.single().action)
        }
        val custom = NativeTextParser.parse("{ ${document.uppercase()} | $dad2 |Visit|3}")
        assertEquals("Visit", custom.text)
        assertEquals(NativeTextAction(Kind.VISIT_BY_DAD2, dad2), custom.links.single().action)
    }

    @Test
    fun legacyNumbersAndCodesSurviveMarkersWithCorrectUtf16Offsets() {
        val parsed = NativeTextParser.parse("\uD83D\uDCDD $report\n{$photo|59682417|Photo} $unlock 0B2F; $report")
        assertEquals(listOf(report, "Photo", "0B2F", report), labels(parsed))
        assertEquals(listOf(Kind.VISIT_BY_NUMBER, Kind.PHOTO_BY_SERVER_ID, Kind.COPY_UNLOCK_CODE, Kind.VISIT_BY_NUMBER),
            parsed.links.map { it.action.kind })
        assertEquals(3, parsed.links.first().start)
        parsed.links.zipWithNext().forEach { (left, right) -> assertTrue(left.end <= right.start) }
    }

    @Test
    fun photoUsesServerIdAndDoesNotReparseItsLabelAsVisit() {
        val parsed = NativeTextParser.parse("{$photo|59682417|$report}")
        assertEquals(listOf(report), labels(parsed))
        assertEquals(NativeTextAction(Kind.PHOTO_BY_SERVER_ID, "59682417"), parsed.links.single().action)
    }

    @Test
    fun tasksAndReclamationsKeepIdAndDocumentNumberSeparate() {
        val cases = listOf(
            Triple(task, "2667429", Kind.TASK_BY_ID),
            Triple(reclamation, "2667429", Kind.RECLAMATION_BY_ID),
            Triple(task + oneC, "AZad-123456", Kind.TASK_BY_NUMBER),
            Triple(reclamation + oneC, "ARek-278811", Kind.RECLAMATION_BY_NUMBER)
        )
        cases.forEach { (type, id, kind) ->
            val parsed = NativeTextParser.parse("{$type|$id|Open}")
            assertEquals(NativeTextAction(kind, id), parsed.links.single().action)
            assertEquals("Open", parsed.text)
        }
    }

    @Test
    fun phoneHasDialActionButUssdAndOtherSchemesStayLiteral() {
        val parsed = NativeTextParser.parse("{$phone|+380 (67) 123-45-67|Call}")
        assertEquals(NativeTextAction(Kind.DIAL, "+380 (67) 123-45-67"), parsed.links.single().action)
        for (value in listOf("*123#", "tel:12345", "https://example.com", "+()")) {
            assertLiteral("{$phone|$value|Call}")
        }
    }

    @Test
    fun unsupportedMarkersIncludingDetailedReportRemainUnchanged() {
        for (marker in listOf("{unknown|1|$report}",
            "{\u0434\u0435\u0442\u0430\u043b\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0439_\u043e\u0442\u0447\u0451\u0442|$report|Report}",
            "{$document}", "{$document||Visit}", "{$document|1|Visit|3|extra}", "{$document|123")) {
            assertLiteral(marker)
        }
    }

    @Test
    fun invalidIdsAndFilterExpressionsAreNotMistakenForObjectIds() {
        for (id in listOf("0", "-1", "9223372036854775808", "1.0", "id:12", "addr:3,client:4", "1e3", "abc")) {
            assertLiteral("{$document|$id|Visit}")
        }
        assertLiteral("{$task|2147483648|Task}")
        assertLiteral("{$photo|-1|Photo}")
    }

    @Test
    fun doesNotLinkArbitraryCodesOrLongerReportNumbers() {
        val parsed = NativeTextParser.parse("d18d 84006 $unlock 12345 ${report}9")
        assertTrue(parsed.links.isEmpty())
    }

    @Test
    fun repeatedMarkersAreSeparateLinksWithoutChangingPunctuation() {
        val parsed = NativeTextParser.parse("({$document|$dad2|$dad2}), {$document|$dad2|$dad2}.")
        assertEquals("($document), $document.", parsed.text)
        assertEquals(listOf(document, document), labels(parsed))
    }

    @Test
    fun serverFormattingCreatesStylesWithoutExecutingHtmlOrCreatingLinks() {
        val cases = listOf(
            "\u0442\u0435\u043a\u0441\u0442_\u0436\u0438\u0440\u043d\u044b\u0439" to NativeTextStyle.Kind.BOLD,
            "\u0442\u0435\u043a\u0441\u0442_\u043a\u0443\u0440\u0441\u0438\u0432" to NativeTextStyle.Kind.ITALIC,
            "\u0442\u0435\u043a\u0441\u0442_\u043f\u043e\u0434\u0447\u0435\u0440\u043a\u043d\u0443\u0442\u044b\u0439" to NativeTextStyle.Kind.UNDERLINE
        )
        cases.forEach { (type, style) ->
            val parsed = NativeTextParser.parse("Text {$type|0|<b>Label</b>}")
            assertEquals("Text <b>Label</b>", parsed.text)
            assertEquals(listOf(NativeTextStyle(5, parsed.text.length, style)), parsed.styles)
            assertTrue(parsed.links.isEmpty())
        }
    }

    @Test
    fun colorSupportsCssNamesAndHexButRejectsInvalidValues() {
        val color = "\u0442\u0435\u043a\u0441\u0442_\u0446\u0432\u0435\u0442"
        for (value in listOf("red", "#F00", "#ff0000")) {
            val parsed = NativeTextParser.parse("{$color|$value|Text}")
            assertEquals(0xffff0000.toInt(), parsed.styles.single().color)
            assertEquals("Text", parsed.text)
        }
        assertLiteral("{$color|url(example)|Text}")
        assertLiteral("{$color|#XYZ|Text}")
    }

    @Test
    fun emptyAndOrdinaryMessagesAreUnchanged() {
        assertEquals(ParsedNativeText("", emptyList(), emptyList()), NativeTextParser.parse(null))
        val text = "Plain text\nsecond line."
        assertLiteral(text)
        assertFalse(NativeTextParser.parse("$unlock 0001").links.isEmpty())
    }

    private fun labels(parsed: ParsedNativeText) = parsed.links.map { parsed.text.substring(it.start, it.end) }

    private fun assertLiteral(text: String) {
        assertEquals(ParsedNativeText(text, emptyList(), emptyList()), NativeTextParser.parse(text))
    }
}
