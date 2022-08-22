package ua.com.merchik.merchik.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;
import ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.ViewHolderTypeList;

public class TextViewHolder extends RecyclerView.ViewHolder {
    private TextView text;

    public TextViewHolder(@NonNull View itemView) {
        super(itemView);
        text = itemView.findViewById(R.id.text);
    }

    public void bind(String data, Clicks.click click) {
        text.setText(data);
        text.setOnClickListener(v->{
            click.click(data);
        });
    }

    private String getTextFromData(OpinionSDB data){
        return data.nm;
    }

    public void bind(ViewHolderTypeList.TextLayoutData textBlock) {
        text.setText(textBlock.data);
        text.setOnClickListener(v->{
            textBlock.click.onSuccess("Нажатие на текст: " + textBlock.data);
            textBlock.result = "Something result";
        });
    }
}
