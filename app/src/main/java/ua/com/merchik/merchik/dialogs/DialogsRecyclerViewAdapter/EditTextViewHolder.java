package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.R;

public class EditTextViewHolder extends RecyclerView.ViewHolder {

    private ConstraintLayout layout;
    private TextView textView;
    private EditText editText;

    public EditTextViewHolder(@NonNull View itemView) {
        super(itemView);
        layout = itemView.findViewById(R.id.layout);
        textView = itemView.findViewById(R.id.textView);
        editText = itemView.findViewById(R.id.editText);
    }

    public void bind(ViewHolderTypeList.EditTextLayoutData editTextBlock) {
        if (editTextBlock.dataTitle != null) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(editTextBlock.dataTitle);
        }else {
            textView.setVisibility(View.GONE);
        }

        switch (editTextBlock.editTextType){
            case NUMBER:
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                break;

            default:
                break;
        }


        editText.setHint(editTextBlock.dataEditTextHint);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editTextBlock.result = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
