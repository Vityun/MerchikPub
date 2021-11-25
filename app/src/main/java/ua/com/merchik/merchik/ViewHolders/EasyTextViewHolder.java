package ua.com.merchik.merchik.ViewHolders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.StandartSDB;

public class EasyTextViewHolder <T> extends RecyclerView.ViewHolder {

    private Context context;
    private View view;

    private ConstraintLayout layout;
    private TextView textView;

    public EasyTextViewHolder(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        view = itemView;

        layout = itemView.findViewById(R.id.layout);
        textView = itemView.findViewById(R.id.text);
    }

    public void bind(T data, Clicks.click click){
        try {
            StandartSDB standart = (StandartSDB) data;
            textView.setText(standart.nm);
            layout.setOnClickListener(v -> {
                click.click(data);
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
