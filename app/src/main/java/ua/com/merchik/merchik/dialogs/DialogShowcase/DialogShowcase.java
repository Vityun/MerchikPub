package ua.com.merchik.merchik.dialogs.DialogShowcase;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.dialogs.DialogData;

public class DialogShowcase extends DialogData {

    private Context context;
    private Dialog dialog;

    public ImageButton close, help, videoHelp, call, addSotr;
    private TextView title;
    private Button cancel;
    private RecyclerView recyclerView;

    public DialogShowcase(Context context){
        this.context = context;

        try {
            initializeDialog();
            populateDialogData();
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "DialogShowcase", "Exception e: " + e);
        }
    }

    public void setClose(DialogData.DialogClickListener clickListener) {
        close.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }

    public void show() {
        if (dialog != null) dialog.show();
    }

    private void initializeDialog(){
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_showcase);

        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.70);
        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        close = dialog.findViewById(R.id.imageButtonClose);
        help = dialog.findViewById(R.id.imageButtonLesson);
        videoHelp = dialog.findViewById(R.id.imageButtonVideoLesson);
        call = dialog.findViewById(R.id.imageButtonCall);

        title = dialog.findViewById(R.id.title);
        cancel = dialog.findViewById(R.id.button);
        recyclerView = dialog.findViewById(R.id.recyclerView);
    }

    private void populateDialogData(){
        setRecyclerView();
    }

    private void setRecyclerView() {
//        recyclerView = adapter;
    }


}
