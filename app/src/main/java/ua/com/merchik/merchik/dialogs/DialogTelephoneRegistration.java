package ua.com.merchik.merchik.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.TelephoneMask;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.DialogAdapter;

public class DialogTelephoneRegistration {

    private Dialog dialog;

    private TextView dialogTitle, dialogText;
    private ImageButton dialogClose, dialogLesson, dialogVideoLesson, dialogCall;
    private Button dialogButtonOk;

    private AutoCompleteTextView dialogAutoText;
    private EditText dialogTelephoneEditText;
    private RecyclerView dialogRecyclerView;

    // ---------------------------------------------------------------------------------------------
    private String resultCode = "";
    private String resultTelephone = "";
    // ---------------------------------------------------------------------------------------------

    public DialogTelephoneRegistration(Context context) {
        createDialogContext(context);
    }

    public void createDialogContext(Context context){
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);  // Objects.requireNonNull -- нужно ли тут ЭТО?
        dialog.setContentView(R.layout.dialog_telephone_registration);

        dialogTitle = dialog.findViewById(R.id.title);
        dialogText = dialog.findViewById(R.id.text);
        dialogButtonOk = dialog.findViewById(R.id.ok);

        dialogAutoText = dialog.findViewById(R.id.telephoneRegionCode);
        dialogTelephoneEditText = dialog.findViewById(R.id.editText);
        dialogRecyclerView = dialog.findViewById(R.id.recyclerView);

        dialogClose = dialog.findViewById(R.id.imageButtonClose);
        dialogLesson = dialog.findViewById(R.id.imageButtonLesson);
        dialogVideoLesson = dialog.findViewById(R.id.imageButtonVideoLesson);
        dialogCall = dialog.findViewById(R.id.imageButtonCall);
    }

    // -----------------------------------У П Р А В Л Е Н И Е---------------------------------------

    public void show() {
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
    }

    public Context getContext() {
        return dialog.getContext();
    }

    // ---------------------------------------------------------------------------------------------

    public void close(Clicks.clickVoid click){
        dialogClose.setOnClickListener(v -> {
            click.click();
        });
    }

    // ---------------------------------------------------------------------------------------------

    public void setTitle(CharSequence title){
        this.dialogTitle.setVisibility(View.VISIBLE);
        if (title != null && !title.equals("")) {
            this.dialogTitle.setText(title);
        } else {
            this.dialogTitle.setVisibility(View.GONE);
        }
    }

    // Для разноцветненьких заголовков
    public void setTitle(SpannableStringBuilder title){
        this.dialogTitle.setVisibility(View.VISIBLE);
        if (title != null) {
            this.dialogTitle.setText(title);
        } else {
            this.dialogTitle.setVisibility(View.GONE);
        }
    }


    public void setText(CharSequence text) {
        this.dialogText.setVisibility(View.VISIBLE);
        if (text != null && !text.equals("")) {
            this.dialogText.setText(text);
        } else {
            this.dialogText.setVisibility(View.GONE);
        }
    }

    // Для разноцветненьких Текстов внутри диалога
    public void setText(SpannableStringBuilder text) {
        this.dialogText.setVisibility(View.VISIBLE);
        if (text != null) {
            this.dialogText.setText(text);
        } else {
            this.dialogText.setVisibility(View.GONE);
        }
    }

    public void setButtonOk(CharSequence buttonText, Clicks.clickVoid click){
        this.dialogButtonOk.setVisibility(View.VISIBLE);
        if (buttonText != null && !buttonText.equals("")){
            this.dialogButtonOk.setText(buttonText);
            this.dialogButtonOk.setOnClickListener((v)->{
                click.click();
            });
        }else {
            this.dialogButtonOk.setVisibility(View.GONE);
        }
    }

    // ---------------------------------------------------------------------------------------------

    public void setTelephone(){
        TelephoneMask telephoneMask = new TelephoneMask();

        dialogAutoText.setVisibility(View.VISIBLE);
        dialogTelephoneEditText.setVisibility(View.VISIBLE);

        // Установка Кода страны
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(dialog.getContext(), R.layout.item_autotext_small_left, telephoneMask.telephoneRegion);
        dialogAutoText.setAdapter(adapter);
        dialogAutoText.setOnTouchListener((view, motionEvent) -> {
            dialogAutoText.showDropDown();
            return false;
        });
        dialogAutoText.setOnItemClickListener((adapterView, view, i, l) -> {
            resultCode = telephoneMask.telephoneRegion[i];
        });

        setTelephoneMask(telephoneMask);
    }

    private void setTelephoneMask(TelephoneMask telephoneMask){
        dialogTelephoneEditText.setVisibility(View.VISIBLE);

        dialogTelephoneEditText.setSelection(dialogTelephoneEditText.getText().length());

        dialogTelephoneEditText.setSelectAllOnFocus(true);
        dialogTelephoneEditText.selectAll();

        dialogTelephoneEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        dialogTelephoneEditText.setRawInputType(InputType.TYPE_CLASS_PHONE);

        dialogTelephoneEditText.addTextChangedListener(telephoneMask);

        dialogTelephoneEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                resultTelephone = v.getText().toString();
                return true;
            }
            return false;
        });
    }

    public String getTelephone(){
        if (resultCode.equals("")){
            resultCode = "+380";
        }

        if (resultTelephone.equals("")){
            resultTelephone = dialogTelephoneEditText.getText().toString();
        }
        return resultCode + resultTelephone;
    }

    // ---------------------------------------------------------------------------------------------

    public void setDialogRecyclerView(DialogAdapter adapter){
        dialogRecyclerView.setVisibility(View.VISIBLE);
        dialogRecyclerView.setAdapter(adapter);
        dialogRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

}
