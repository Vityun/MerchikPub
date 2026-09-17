package ua.com.merchik.merchik.Activities.ReferencesActivity.Chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

public final class ChatVisitLinks {

    static final Pattern REPORT_DOCUMENT_PATTERN = Pattern.compile(
            "(?<![\\p{L}\\p{N}_])\u0410\u041e\u0438-[0-9]{8}(?![\\p{L}\\p{N}_])");

    private ChatVisitLinks() {
    }

    public static SpannableString linkify(CharSequence message) {
        SpannableString text = new SpannableString(message == null ? "" : message);
        Matcher matcher = REPORT_DOCUMENT_PATTERN.matcher(text);
        while (matcher.find()) {
            String documentNumber = matcher.group();
            text.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    openVisit(widget.getContext(), documentNumber);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(Color.BLUE);
                    ds.setUnderlineText(true);
                }
            }, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    private static void openVisit(Context context, String documentNumber) {
        try {
            // Resolve on tap: the visit may be loaded or reassigned after the message was shown.
            WpDataDB wpDataDB = WpDataRealm.getWpDataRowByDocNumOtchet(documentNumber);
            if (wpDataDB == null) {
                Toast.makeText(context,
                        "Відвідування за звітом " + documentNumber + " відсутнє на цьому пристрої",
                        Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(context, DetailedReportActivity.class);
            intent.putExtra("WpDataDB_ID", wpDataDB.getId());
            context.startActivity(intent);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "ChatVisitLinks/openVisit",
                    "documentNumber=" + documentNumber + ", exception=" + e);
            Toast.makeText(context, "Не вдалося відкрити відвідування", Toast.LENGTH_LONG).show();
        }
    }
}
