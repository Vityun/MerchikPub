package ua.com.merchik.merchik.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;

public class AlphaDialog{


    private Dialog dialog;
    private Context mContext;

    private ImageView imageButtonClose;



    public AlphaDialog(Context context) {
        mContext = context;


    }


    public void show() {
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
    }




}
