package ua.com.merchik.merchik.Utils;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class CustomString {

    public static SpannableString underlineString(String text) {
        SpannableString spannableSt = new SpannableString(text);
        spannableSt.setSpan(new UnderlineSpan(), 0, spannableSt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableSt;
    }

    public static SpannableString underlineString(String text, OptionsDB option) {
        String isSignal = option.getIsSignal();
        ForegroundColorSpan foregroundSpan = switch (isSignal) {
            case "0", "2" -> new ForegroundColorSpan(Color.GREEN);
            case "1" -> new ForegroundColorSpan(Color.RED);
            default -> new ForegroundColorSpan(Color.DKGRAY);
        };
        SpannableString spannableSt = new SpannableString(text);
        spannableSt.setSpan(new UnderlineSpan(), 0, spannableSt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableSt.setSpan(foregroundSpan, 0, spannableSt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableSt;
    }


    public static SpannableString coloredString(String text, OptionsDB option) {
        String isSignal = option.getIsSignal();
        ForegroundColorSpan foregroundSpan = switch (isSignal) {
            case "0", "2" -> new ForegroundColorSpan(Color.GREEN);
            case "1" -> new ForegroundColorSpan(Color.RED);
            default -> new ForegroundColorSpan(Color.DKGRAY);
        };
        SpannableString spannableSt = new SpannableString(text);
        spannableSt.setSpan(foregroundSpan, 0, spannableSt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableSt;
    }
}
