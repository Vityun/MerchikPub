package ua.com.merchik.merchik.Utils;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;

import ua.com.merchik.merchik.database.realm.RealmManager;

public class CustomString {

    public static SpannableString underlineString(String text) {
        SpannableString spannableSt = new SpannableString(text);
        spannableSt.setSpan(new UnderlineSpan(), 0, spannableSt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableSt;
    }
}
