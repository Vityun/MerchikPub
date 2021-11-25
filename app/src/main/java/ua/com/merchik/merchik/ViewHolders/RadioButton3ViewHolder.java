package ua.com.merchik.merchik.ViewHolders;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.dialogs.DialogFilter.data.DFWpResult;

public class RadioButton3ViewHolder <T> extends RecyclerView.ViewHolder {

    public static Integer info;

    private Context context;

    private ConstraintLayout layout;
    private RadioGroup radioGroup;
    private RadioButton radio1;
    private RadioButton radio2;
    private RadioButton radio3;

    public RadioButton3ViewHolder(@NonNull View itemView) {
        super(itemView);

        layout = itemView.findViewById(R.id.layout);
        radio1 = itemView.findViewById(R.id.radio1);
        radio2 = itemView.findViewById(R.id.radio2);
        radio3 = itemView.findViewById(R.id.radio3);
    }

    public void bind(Globals.SourceAct source, Click click){
        switch (source){
            case WP_DATA:
                DFWpResult result = new DFWpResult();

                radio1.setText("Всё");
                radio2.setText("Проведённые");
                radio3.setText("НЕ Проведённые");

                if (info != null){
                    switch (info){
                        case 1:
                            radio1.setChecked(true);
                            break;
                        case 2:
                            radio2.setChecked(true);
                            break;
                        case 3:
                            radio3.setChecked(true);
                            break;
                    }
                }


                radio1.setOnCheckedChangeListener((v, checked)->{
                    if (checked){
                        info = 1;
                        result.status = null;
                        click.onSuccess(result);
                    }
                });
                radio2.setOnCheckedChangeListener((v, checked)->{
                    if (checked){
                        info = 2;
                        result.status = 1;
                        click.onSuccess(result);
                    }
                });
                radio3.setOnCheckedChangeListener((v, checked)->{
                    if (checked){
                        info = 3;
                        result.status = 0;
                        click.onSuccess(result);
                    }
                });


                if (radio1.isChecked()){
                    info = 1;
                    result.status = null;
                    click.onSuccess(result);
                }else if (radio2.isChecked()){
                    info = 2;
                    result.status = 1;
                    click.onSuccess(result);
                }else if (radio3.isChecked()){
                    info = 3;
                    result.status = 0;
                    click.onSuccess(result);
                }

                break;
        }




    }

    public Globals.SourceAct type(){
        return Globals.SourceAct.WP_DATA;
    }


}
