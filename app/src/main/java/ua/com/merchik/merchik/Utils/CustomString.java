package ua.com.merchik.merchik.Utils;

import static ua.com.merchik.merchik.Globals.userId;

import android.graphics.Color;
import android.text.Html;
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

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.WorkPlanActivity.WPDataFragmentHome;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;

public class CustomString {

    public enum TitleMode {SHORT, FULL}


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


    public static String viberLink() {
        String base = "mobile.php/mobile.php?mod=messenger_connect&type=viber";
        AppUsersDB appUser = AppUserRealm.getAppUserById(userId);
        String hash = String.format("%s%s%s", appUser.getUserId(), appUser.getPassword(), "AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3");
        hash = Globals.getSha1Hex(hash);
        base = base.replace("&", "**");
        return String.format("https://merchik.com.ua/sa.php?&u=%s&s=%s&l=/%s", userId, hash, base);

    }

    public static SpannableStringBuilder createTitleMsg(RealmResults<WpDataDB> wp, TitleMode mode) {
        SpannableStringBuilder res = new SpannableStringBuilder();

        try {
            if (wp != null && wp.size() > 0) {
                // Запланированные работы
                int wpSum = wp.sum("cash_ispolnitel").intValue();

                // Выполненные работы
                RealmResults<WpDataDB> wpStatus = wp.where().equalTo("status", 1).findAll();
                int wpStatus1Size = wpStatus.size();    // Количество проведённых отчётов
                int wpStatus1Sum = wpStatus.sum("cash_ispolnitel").intValue();  // Сумма полученная за проведенные отчёты
                int percentWpStatus1 = (wpStatus1Size * 100) / wp.size(); // Процент выполненных работ

                // Не Віполненные
                RealmResults<WpDataDB> wpStatus0 = wp.where().equalTo("status", 0).findAll();
                int wpStatus0Size = wpStatus0.size();
                int wpStatus0Sum = wpStatus0.sum("cash_ispolnitel").intValue();
                int percentWpStatus0 = (wpStatus0Size * 100) / wp.size();

                if (mode.equals(TitleMode.FULL)) {
                    res.append(Html.fromHtml("<b>За період: </b> з ")).append(Clock.getHumanTimeYYYYMMDD(wp.get(0).getDt().getTime() / 1000)).append(" по ").append(Clock.getHumanTimeYYYYMMDD(wp.get(wp.size() - 1).getDt().getTime() / 1000)).append("\n\n");
                    res.append(Html.fromHtml("<b>Заплановано робіт (Пр): </b>")).append("" + wp.size()).append(" (100%),").append(" на суму ").append("" + wpSum).append(" грн.").append("\n\n");
                    res.append(Html.fromHtml("<b>Виконано робіт (Вр): </b>")).append("" + wpStatus1Size).append(" (").append("" + percentWpStatus1).append("%), на суму ").append("" + wpStatus1Sum).append(" грн.").append("\n\n");
                    res.append(Html.fromHtml("<b>Не виконано робіт (Нр): </b>")).append("" + wpStatus0Size).append(" (").append("" + percentWpStatus0).append("%), на суму ").append("" + wpStatus0Sum).append(" грн.");
                } else if (mode.equals(TitleMode.SHORT)) {
                    res.append("Пр: ").append("" + wp.size()).append(" (").append("" + wpSum).append("гр) / ").append("Вр: ").append("" + wpStatus1Size).append(" (").append("" + wpStatus1Sum).append("гр) / ").append("Нр: ").append("" + wpStatus0Size).append(" (").append("" + wpStatus0Sum).append("гр)");
                }
            } else {
                res.append("План робіт пустий.");
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "WPDataFragmentHome/createTitleMsg", "Exception e: " + e);
            res.append("");
        }
        return res;
    }
}
