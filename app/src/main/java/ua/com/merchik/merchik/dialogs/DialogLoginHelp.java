package ua.com.merchik.merchik.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import ua.com.merchik.merchik.R;

public class DialogLoginHelp extends DialogData{

    private Dialog dialog;

    private TextView textView, title;

    private String helpMsg;

    public DialogLoginHelp(Context context) {
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_help_suppr);

        title = dialog.findViewById(R.id.title);

        imgBtnClose = dialog.findViewById(R.id.imageButtonClose);
        imgBtnLesson = dialog.findViewById(R.id.imageButtonLesson);
        imgBtnVideoLesson = dialog.findViewById(R.id.imageButtonVideoLesson);
        imgBtnCall = dialog.findViewById(R.id.imageButtonCall);

        textView = dialog.findViewById(R.id.dialogText);
    }

    public void show(){
        dialog.show();
    }

    public void dismiss(){
        if (dialog != null) dialog.dismiss();
    }

    @Override
    public void setClose(DialogClickListener clickListener) {
        imgBtnClose.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }


    public void setLesson(Context context, boolean visualise, int objectId) {
        super.setLesson(context, visualise, objectId);
    }


    public void setVideoLesson(Context context, boolean visualise, int objectId, DialogClickListener clickListener) {
        super.setVideoLesson(context, visualise, objectId, clickListener);
    }


    public void setImgBtnCall(Context context) {
        super.setImgBtnCall(context);
    }


    //========================================== DATA ==============================================

    public void setHelpMsg(Spannable s){
        if (s != null && !s.equals("")){
            textView.setHighlightColor(dialog.getContext().getResources().getColor(android.R.color.transparent));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(s);
        }
    }

    public void setTitle(String s){
        title.setText(s);
    }

    //==============================================================================================


}
