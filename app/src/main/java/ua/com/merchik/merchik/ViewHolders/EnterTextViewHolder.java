package ua.com.merchik.merchik.ViewHolders;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.TEST_DATA;
import ua.com.merchik.merchik.data.TestViewHolderData;

import static ua.com.merchik.merchik.Globals.NewTARDataType.PREMIYA;

public class EnterTextViewHolder extends RecyclerView.ViewHolder {

    private View view;
    private Context context;
    private EditText editText;
    private TextView textView;

    public EnterTextViewHolder(@NonNull View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        this.view = itemView;
        editText = itemView.findViewById(R.id.editText);
        textView = itemView.findViewById(R.id.textView);
    }

    public void bind(TestViewHolderData data, Clicks.clickListener click) {
        textView.setText(data.title);
        editText.setHint(data.msg);
        if (data.type != null && data.type == PREMIYA){
            editText.setText("30.00");
        }else {
            editText.setText(null);
        }

        editText.setSelectAllOnFocus(true);
        editText.selectAll();

        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            switch (data.type){
                case COMMENT:
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        TEST_DATA test = new TEST_DATA();
                        test.comment = editText.getText().toString();
                        click.click(test);

                        Toast.makeText(context, "Комментарий сохранён", Toast.LENGTH_SHORT).show();

                        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        return true;
                    }
                    break;

                case PREMIYA:
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        TEST_DATA test = new TEST_DATA();
                        test.premiya = editText.getText().toString();
                        click.click(test);

                        Toast.makeText(context, "Премия " + test.premiya + " сохранена.", Toast.LENGTH_SHORT).show();

                        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        return true;
                    }
                    break;
            }

            return false;
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                TEST_DATA test = new TEST_DATA();
                test.comment = s.toString();
                click.click(test);
            }
        });


    }
}
