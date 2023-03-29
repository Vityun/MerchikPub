package ua.com.merchik.merchik.dialogs.DialogFilter;

import android.app.Dialog;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Map;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.SpinnerDecode;
import ua.com.merchik.merchik.database.realm.tables.PhotoTypeRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class DialogFilterPhotoLog {

    private Dialog dialog;
    private Context context;

    private String text;

    // --- Start Ресурсы ---
    private TextView title;
    private EditText editText;
    private Spinner spinner;
    private AutoCompleteTextView clientAutoTV;

    private Button apply;
    // --- End Ресурсы ---

    private ImageButton close, help, videoHelp, call;


    public DialogFilterPhotoLog(Context context, String findText) {
        this.context = context;
        this.text = findText;

        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_filter_photo_log);


        // Подключаю ресурсы
        title = dialog.findViewById(R.id.title);
        editText = dialog.findViewById(R.id.editText);
        spinner = dialog.findViewById(R.id.spinner);
        clientAutoTV = dialog.findViewById(R.id.clientAutoTV);

        apply = dialog.findViewById(R.id.apply);

        close = dialog.findViewById(R.id.imageButtonClose);


        setElements();
    }

    public void show() {
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void setClose(DialogData.DialogClickListener clickListener) {
        close.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }

    //==============================================================================================


    /**
    * 20.08.2021
     * Установка элементов фильтра
    * */
    private void setElements(){
        editText.setText(text);

        Map<Integer, String> map =  PhotoTypeRealm.getPhotoTypeMap();

        String[] res = map.values().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_spinner_item, res);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        SpinnerDecode spinnerDialogData = new SpinnerDecode();
        spinnerDialogData.setData(map);
        spinner.setOnItemSelectedListener(spinnerDialogData);
    }




    // ============== Results ==============
    public void setApply(Click click){
        apply.setOnClickListener(v -> {

            String photoTypeTxt = spinner.getSelectedItem().toString();

            DFPhotoLogDataResult result = new DFPhotoLogDataResult();

            result.editText = editText.getText().toString();
//            result.photoType = spinner.getSelectedItem();

            click.onSuccess(result);
        });
    }



    /*Результат фильтра Журнала фото*/
    public class DFPhotoLogDataResult{
        public String editText;
        public Integer photoType;
        public Integer clientId;
    }
}


