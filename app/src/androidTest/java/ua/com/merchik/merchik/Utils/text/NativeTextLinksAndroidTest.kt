package ua.com.merchik.merchik.Utils.text

import android.text.Spanned
import android.text.style.ClickableSpan
import org.junit.Assert.assertEquals
import org.junit.Test
import ua.com.merchik.merchik.Activities.ReferencesActivity.Chat.ChatVisitLinks

/** Runs on Android: a desktop JVM does not reproduce ICU's rejection of an unescaped closing brace. */
class NativeTextLinksAndroidTest {
    @Test
    fun ordinaryChatMessageInitializesParserWithoutCrashing() {
        val text = ChatVisitLinks.linkify("Plain chat message")
        assertEquals("Plain chat message", text.toString())
        assertEquals(0, text.getSpans(0, text.length, ClickableSpan::class.java).size)
    }

    @Test
    fun unlockNotificationCreatesCodeAndLegacyDocumentLinksOnAndroid() {
        val document = "\u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442"
        val unlock = "\u043a\u043e\u0434 \u0440\u0430\u0437\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u043a\u0438"
        val dad2 = "1021026038634061377"
        val prefix = "Sender $unlock d18d: "
        val text = ChatVisitLinks.linkify(prefix + "{$document|$dad2|$dad2}.")

        assertEquals(prefix + document + ".", text.toString())
        val links = text.getSpans(0, text.length, ClickableSpan::class.java)
            .sortedBy { text.getSpanStart(it) }
        assertEquals(listOf("d18d", document), links.map {
            text.subSequence(text.getSpanStart(it), text.getSpanEnd(it)).toString()
        })
        links.forEach { assertEquals(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE, text.getSpanFlags(it)) }
    }
}
