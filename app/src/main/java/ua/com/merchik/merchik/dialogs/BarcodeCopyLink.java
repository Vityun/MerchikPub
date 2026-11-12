package ua.com.merchik.merchik.dialogs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public final class BarcodeCopyLink {
    private BarcodeCopyLink() {
    }

    public static void apply(TextView view, String barcode) {
        if (view == null) return;
        SpannableStringBuilder text = new SpannableStringBuilder(view.getText());
        CopySpan[] previous = text.getSpans(0, text.length(), CopySpan.class);
        for (CopySpan span : previous) text.removeSpan(span);

        boolean linked = false;
        int start = findBarcodeStart(text, barcode, 0);
        while (start >= 0) {
            int end = start + barcode.length();
            // Keep existing action links intact (for example, opening a product editor).
            if (text.getSpans(start, end, ClickableSpan.class).length == 0) {
                text.setSpan(new CopySpan(barcode), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                linked = true;
            }
            start = findBarcodeStart(text, barcode, end);
        }
        if (linked || previous.length > 0) view.setText(text);
        if (linked) view.setMovementMethod(LinkMovementMethod.getInstance());
    }

    static int findBarcodeStart(CharSequence text, String barcode, int fromIndex) {
        if (text == null || barcode == null || barcode.trim().isEmpty()) return -1;
        String value = text.toString();
        for (int start = value.indexOf(barcode, fromIndex); start >= 0;
             start = value.indexOf(barcode, start + 1)) {
            int end = start + barcode.length();
            if ((start == 0 || !Character.isLetterOrDigit(value.charAt(start - 1)))
                    && (end == value.length() || !Character.isLetterOrDigit(value.charAt(end)))) {
                return start;
            }
        }
        return -1;
    }

    private static final class CopySpan extends ClickableSpan {
        private final String barcode;

        private CopySpan(String barcode) {
            this.barcode = barcode;
        }

        @Override
        public void onClick(View widget) {
            ClipboardManager clipboard = (ClipboardManager) widget.getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null) return;
            clipboard.setPrimaryClip(ClipData.newPlainText("Штрихкод", barcode));
            Toast.makeText(widget.getContext(), "Штрихкод скопійовано", Toast.LENGTH_SHORT).show();
        }
    }
}
