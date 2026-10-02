package ua.com.merchik.merchik.Options;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class OptionMessageLinksAndroidTest {
    private static final int LINK_COLOR = 0xff008ced;

    @Test
    public void overridesLegacyLinkColorsWithoutChangingWarningsOrActions() {
        SpannableStringBuilder original = new SpannableStringBuilder("photo warning");
        final boolean[] clicked = {false};
        ClickableSpan link = new ClickableSpan() {
            @Override public void onClick(View widget) { clicked[0] = true; }

            @Override public void updateDrawState(TextPaint paint) {
                paint.setColor(Color.GREEN);
                paint.setUnderlineText(false);
            }
        };
        original.setSpan(link, 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        original.setSpan(new ForegroundColorSpan(Color.RED), 0, original.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder styled = OptionMessageLinks.style(original, LINK_COLOR);
        assertEquals(original.toString(), styled.toString());
        assertSame(link, styled.getSpans(0, 5, ClickableSpan.class)[0]);
        styled.getSpans(0, 5, ClickableSpan.class)[0].onClick(null);
        assertTrue(clicked[0]);
        assertEquals(LINK_COLOR, paintAt(styled, 1).getColor());
        assertFalse(paintAt(styled, 1).isUnderlineText());
        assertEquals(Color.RED, paintAt(styled, 7).getColor());
        assertEquals(Color.RED, paintAt(original, 1).getColor());
    }

    @Test
    public void stylesEachLinkAndPreservesUnderlines() {
        SpannableStringBuilder message = new SpannableStringBuilder("59682417, 59682528 comment");
        int[][] ranges = {{0, 8}, {10, 18}, {19, message.length()}};
        for (int[] range : ranges) {
            message.setSpan(new ClickableSpan() {
                @Override public void onClick(View widget) { }
            }, range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        SpannableStringBuilder styled = OptionMessageLinks.style(message, LINK_COLOR);
        assertEquals(3, styled.getSpans(0, styled.length(), ClickableSpan.class).length);
        for (int[] range : ranges) {
            TextPaint paint = paintAt(styled, range[0] + 1);
            assertEquals(LINK_COLOR, paint.getColor());
            assertTrue(paint.isUnderlineText());
        }
        assertEquals(Color.BLACK, paintAt(styled, 8).getColor());
    }

    @Test
    public void plainTextHasNoAddedSpans() {
        SpannableStringBuilder styled = OptionMessageLinks.style("Plain message", LINK_COLOR);
        assertEquals("Plain message", styled.toString());
        assertEquals(0, styled.getSpans(0, styled.length(), CharacterStyle.class).length);
    }

    private static TextPaint paintAt(Spanned text, int offset) {
        TextPaint paint = new TextPaint();
        paint.setColor(Color.BLACK);
        paint.linkColor = Color.MAGENTA;
        for (CharacterStyle span : text.getSpans(offset, offset + 1, CharacterStyle.class)) {
            span.updateDrawState(paint);
        }
        return paint;
    }
}
