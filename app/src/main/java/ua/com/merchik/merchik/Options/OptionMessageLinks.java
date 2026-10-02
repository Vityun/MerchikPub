package ua.com.merchik.merchik.Options;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import ua.com.merchik.merchik.R;

public final class OptionMessageLinks {
    private OptionMessageLinks() { }

    public static SpannableStringBuilder style(Context context, CharSequence message) {
        return style(message, ContextCompat.getColor(context, R.color.blue));
    }

    static SpannableStringBuilder style(CharSequence message, int color) {
        SpannableStringBuilder result = new SpannableStringBuilder(message);
        // Apply last, after legacy spans with their own colors. Keep actions and underlines intact.
        for (ClickableSpan link : result.getSpans(0, result.length(), ClickableSpan.class)) {
            int start = result.getSpanStart(link);
            int end = result.getSpanEnd(link);
            if (start < end) {
                result.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return result;
    }
}
