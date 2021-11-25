package ua.com.merchik.merchik.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import ua.com.merchik.merchik.R;

public class DialogRegestration extends DialogData{

    private Dialog dialog;

    private ImageButton imgBtnClose;
    private ImageButton imgBtnLesson;
    private ImageButton imgBtnVideoLesson;

    public void showDialog(Activity activity){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.fragment_menu_login_regestration);

        imgBtnClose = dialog.findViewById(R.id.imageButtonClose);
        imgBtnLesson = dialog.findViewById(R.id.imageButtonLesson);
        imgBtnVideoLesson = dialog.findViewById(R.id.imageButtonVideoLesson);

        Button dialogButton = (Button) dialog.findViewById(R.id.button7);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "Этот раздел в разработке", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void setClose(DialogClickListener clickListener) {
        imgBtnClose.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }

    @Override
    public void setLesson(Context context, boolean visualise, int objectId) {
        super.setLesson(context, visualise, objectId);
    }

    @Override
    public void setVideoLesson(Context context, boolean visualise, int objectId, DialogClickListener clickListener) {
        if (visualise) {
            imgBtnVideoLesson.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imgBtnVideoLesson.getBackground().setTint(Color.RED);
            } else {
                imgBtnVideoLesson.setBackgroundColor(Color.RED);
            }
            imgBtnVideoLesson.setColorFilter(Color.WHITE);
        }

        imgBtnVideoLesson.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }


    @Override
    public void setImgBtnCall(Context context) {
        super.setImgBtnCall(context);
    }
}
