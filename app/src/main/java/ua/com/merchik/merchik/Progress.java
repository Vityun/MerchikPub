package ua.com.merchik.merchik;

import android.app.ProgressDialog;
import android.content.Context;

public class Progress {
    private ProgressDialog progressDialog;
    private String title;
    private String massage;
    private String massageDissmiss;

    public Progress(Context context, String title, String massage, boolean setCancelable) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(setCancelable);
        progressDialog.setTitle(title);
        progressDialog.setMessage(massage);
    }

/*    public void setData(String title, String massage, String massageDissmiss) {
        this.title = title;
        Log.e("Progress", "titleSet: " + this.title);
        this.massage = massage;
        this.massageDissmiss = massageDissmiss;
    }*/

    public void show(){
        if(progressDialog != null) progressDialog.show();
    }

    public void dissmiss(){
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
}
