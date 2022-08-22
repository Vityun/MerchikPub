package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.R;

public class TextViewHolder extends RecyclerView.ViewHolder {

    private ConstraintLayout layout;
    private TextView text;

    public TextViewHolder(@NonNull View itemView) {
        super(itemView);
        layout = itemView.findViewById(R.id.layout);
        text = itemView.findViewById(R.id.text);
    }

    public void bind(ViewHolderTypeList.TextLayoutData block) {
        text.setText(block.data);
        text.setOnClickListener((v)->{
            block.click.onSuccess(text.getText());
        });
    }
}
