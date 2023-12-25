package ua.com.merchik.merchik.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import ua.com.merchik.merchik.R;

public class BlockingProgressDialog extends Dialog {

    private TextView titleView, textView;

    public BlockingProgressDialog(Context context, String title, String text) {
        super(context, false, null);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.dialog_blocking_progress);

        titleView = findViewById(R.id.dialog_blocking_progress_title);
        textView = findViewById(R.id.dialog_blocking_progress_text);

        titleView.setText(title);
        textView.setText(text);
    }

    @Override
    public void dismiss() {
        if (isShowing()) {
            super.dismiss();
        }
    }

    static public BlockingProgressDialog show(Context context, String title) {
        BlockingProgressDialog dialog = new BlockingProgressDialog(context, title, "");
        dialog.show();

        return dialog;
    }

    static public BlockingProgressDialog show(Context context, String title, String text) {
        BlockingProgressDialog dialog = new BlockingProgressDialog(context, title, text);
        dialog.show();

        return dialog;
    }

    public void updateText(String newText) {
        if (textView != null) {
            textView.setText(newText);
        }
    }
}
