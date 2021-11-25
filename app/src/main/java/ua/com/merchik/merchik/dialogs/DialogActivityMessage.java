package ua.com.merchik.merchik.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import ua.com.merchik.merchik.R;

public class DialogActivityMessage {

    private Dialog dialog;
    private TextView msg;
    private ImageButton imgBtnClose;
    private ImageButton imgBtnLesson;
    private ImageButton imgBtnVideoLesson;

    public DialogActivityMessage(Context context, String text) {
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_massage_with_x);
        msg = dialog.findViewById(R.id.dialog_massage_with_x_text);
        msg.setText(text);

        imgBtnClose = dialog.findViewById(R.id.imageButtonClose);
        imgBtnLesson = dialog.findViewById(R.id.imageButtonLesson);
        imgBtnVideoLesson = dialog.findViewById(R.id.imageButtonVideoLesson);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imgBtnVideoLesson.getBackground().setTint(Color.RED);
        }else {
            imgBtnVideoLesson.setBackgroundColor(Color.RED);
        }
        imgBtnVideoLesson.setColorFilter(Color.WHITE);

        imgBtnClose.setOnClickListener(v -> {
            dialog.dismiss();
            dialog.cancel();
        });

        imgBtnLesson.setOnClickListener(v -> {
            Toast.makeText(context, "Подсказка пока не доступна.", Toast.LENGTH_SHORT).show();
        });

        imgBtnVideoLesson.setOnClickListener(v -> {
            Toast.makeText(context, "Видеоподсказка пока не доступна.", Toast.LENGTH_SHORT).show();
        });
    }

    public void show() {if(dialog != null) dialog.show();}
}
