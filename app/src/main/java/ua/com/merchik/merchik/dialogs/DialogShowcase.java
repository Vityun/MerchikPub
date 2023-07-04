package ua.com.merchik.merchik.dialogs;

import android.app.Dialog;
import android.content.Context;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;

public class DialogShowcase extends DialogData{

    private Context context;
    private Dialog dialog;

    public DialogShowcase(Context context){
        this.context = context;

        try {
            initializeDialog();
            populateDialogData();
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "DialogShowcase", "Exception e: " + e);
        }
    }

    private void initializeDialog(){
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_ekl);


    }

    private void populateDialogData(){

    }


}
