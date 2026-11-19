package ua.com.merchik.merchik.features.main.options;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;

/** Row presentation only; the signal, values and text actions are left unchanged. */
public final class OptionTextColors {
    public static final int DEFAULT_TEXT = 0xFF424242;
    public static final int NEUTRAL_COUNTER = 0xFF888888;

    private OptionTextColors() { }

    public static int counterColor(boolean signalVisible, int signalTint, int green, int red) {
        return signalVisible && (signalTint == green || signalTint == red) ? signalTint : NEUTRAL_COUNTER;
    }

    public static CharSequence withColor(CharSequence source, int color) {
        SpannableString text = new SpannableString(source == null ? "" : source);
        // HTML can carry a different color. Preserve links, underlines and emphasis, but replace foregrounds.
        for (ForegroundColorSpan span : text.getSpans(0, text.length(), ForegroundColorSpan.class)) {
            text.removeSpan(span);
        }
        if (text.length() > 0) {
            text.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    public static void apply(OptionItemState row, int green, int red) {
        int counter = counterColor(row.signal.visibility == View.VISIBLE, row.signal.tint, green, red);
        row.title.text = withColor(row.title.text, DEFAULT_TEXT);
        row.counter.text = withColor(row.counter.text, counter);
        row.secondaryCounter.text = withColor(row.secondaryCounter.text, counter);
    }
}
