package ua.com.merchik.merchik.Activities.ReferencesActivity.Chat;

import android.text.SpannableString;

import java.util.regex.Pattern;

import ua.com.merchik.merchik.Utils.text.NativeTextLinks;
import ua.com.merchik.merchik.Utils.text.NativeTextParser;

/** Compatibility entry point for existing chat and dialog callers. */
public final class ChatVisitLinks {
    static final Pattern REPORT_DOCUMENT_PATTERN = NativeTextParser.REPORT_DOCUMENT_PATTERN;
    static final Pattern UNLOCK_CODE_PATTERN = NativeTextParser.UNLOCK_CODE_PATTERN;

    private ChatVisitLinks() {
    }

    public static SpannableString linkify(CharSequence message) {
        return NativeTextLinks.linkify(message);
    }
}
