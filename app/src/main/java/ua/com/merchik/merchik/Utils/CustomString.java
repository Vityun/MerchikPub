package ua.com.merchik.merchik.Utils;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import androidx.annotation.NonNull;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
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

    public static String cleanComment(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        // Удаляем всё, кроме букв (включая русские, украинские и другие)
        return input.replaceAll("[^\\p{L}]", "");
    }

    public static String getTimeDifference(long unixTime1, long unixTime2) {
        Duration duration = Duration.ofSeconds(Math.abs(unixTime1 - unixTime2));
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        if (hours == 0)
            return String.format("%d хв.", minutes);
        else
            return String.format("%d:%d", hours, minutes);
    }

}
