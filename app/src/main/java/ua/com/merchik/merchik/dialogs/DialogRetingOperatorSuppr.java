package ua.com.merchik.merchik.dialogs;

import android.app.Dialog;
import android.content.Context;

import ua.com.merchik.merchik.R;

public class DialogRetingOperatorSuppr extends DialogData {

    private Dialog dialog;


    public DialogRetingOperatorSuppr(Context context) {
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_reting_operator_suppr);

        imgBtnClose = dialog.findViewById(R.id.imageButtonClose);
        imgBtnLesson = dialog.findViewById(R.id.imageButtonLesson);
        imgBtnVideoLesson = dialog.findViewById(R.id.imageButtonVideoLesson);
        imgBtnCall = dialog.findViewById(R.id.imageButtonCall);

        merchikIco = dialog.findViewById(R.id.merchik_ico);
    }


    public void show(){
        dialog.show();
    }

    public void dismiss(){
        if (dialog != null) dialog.dismiss();
    }


    @Override
    public void setClose(DialogClickListener clickListener) {
        super.setClose(clickListener);
    }

    public void setLesson(Context context, boolean visualise, int objectId) {
        super.setLesson(context, visualise, objectId);
    }


    public void setVideoLesson(Context context, boolean visualise, int objectId, DialogClickListener clickListener) {
        try {
            super.setVideoLesson(context, visualise, objectId, clickListener);
        }catch (Exception e){

        }

    }


    public void setImgBtnCall(Context context) {
        super.setImgBtnCall(context);
    }

}
