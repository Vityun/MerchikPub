package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.R;

public class ButtonDialogViewHolder extends RecyclerView.ViewHolder {

    private ConstraintLayout layout;
    private Button button;

    public ButtonDialogViewHolder(@NonNull View itemView) {
        super(itemView);
        layout = itemView.findViewById(R.id.layout);
        button = itemView.findViewById(R.id.button);
    }

    public void bind(ViewHolderTypeList.ButtonLayoutData buttonBlock) {
        button.setText(buttonBlock.data);
        button.setOnClickListener((v)->{
            buttonBlock.click.onSuccess(true);
        });
    }
}
