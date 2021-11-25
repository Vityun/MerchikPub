package ua.com.merchik.merchik.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.R;

public class TestDialogClass {

    private Dialog dialog;
    private Context context;
    private LinearLayout globalLayout; // Общий testLayout1 для всего диалога
    private ConstraintLayout testElementLayout; // Общий testLayout1 для всего диалога
    private ConstraintLayout testLayout1;    // Область к которой буду всё цеплять
    private ConstraintLayout testLayout2;    // Область к которой буду всё цеплять

    private TextView TV;

    public TestDialogClass(Context context) {
        this.context = context;
        dialog = new Dialog(context);
//        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

//        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.90);

        globalLayout = new LinearLayout(context);
        globalLayout.addView(View.inflate(globalLayout.getContext(), R.layout.dialog_test, null));


        dialog.setContentView(globalLayout);
//        dialog.setContentView(R.testLayout1.dialog_test);


        testLayout1 = dialog.findViewById(R.id.testLayout1);
        testLayout2 = dialog.findViewById(R.id.testLayout2);
        testElementLayout = dialog.findViewById(R.id.testElementLayout);
        TV = dialog.findViewById(R.id.textView58);




        setLayouts();


//        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
    }


    public void show() {
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
//            dialog.cancel();
        }
    }


    private List<Integer> layoutIds = new ArrayList<>();
    public void setLayouts(){
        //---------
        TextView textView = new TextView(context);
        textView.setText("GREEN");

        //---------
        ConstraintLayout layout = new ConstraintLayout(context);


        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(testElementLayout);
        constraintSet.connect(layout.getId(), ConstraintSet.RIGHT, testLayout1.getId(), ConstraintSet.RIGHT, 100);
        constraintSet.applyTo(testElementLayout);

        layout.setBackgroundColor(context.getResources().getColor(R.color.greenCol));
        layout.addView(textView);


        testElementLayout.addView(layout);
        dialog.setContentView(globalLayout);
    }



    public void setLayouts2(){

        ConstraintLayout layout = new ConstraintLayout(context);
        TextView textView = new TextView(context);
        textView.setText("YELLOW");

        layoutIds.add(layout.getId());

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);

        if (layoutIds.size() <= 1){
//            constraintSet.connect(testLayout1.getAddrId(), ConstraintSet.LEFT, testLayout1.getAddrId(), ConstraintSet.LEFT, 8);
            constraintSet.connect(layout.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 8);


            Log.e("setLayouts", " yellow <=1");
        }else if (layoutIds.size() > 1){
//            constraintSet.connect(testLayout1.getAddrId(), ConstraintSet.LEFT, layoutIds.get(layoutIds.size()-1), ConstraintSet.LEFT, 16);
            constraintSet.connect(layout.getId(), ConstraintSet.TOP, layoutIds.get(layoutIds.size()-1), ConstraintSet.TOP, 16);
            Log.e("setLayouts", "yellow >1");
        }

        constraintSet.applyTo(layout);

        layout.setBackgroundColor(context.getResources().getColor(R.color.colorInetYellow));
        layout.addView(textView);


        globalLayout.addView(layout);
        dialog.setContentView(globalLayout);
    }



}
