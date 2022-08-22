package ua.com.merchik.merchik.Adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.ViewHolders.TextViewHolder;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;

public class TextAdapter <T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<T> data;
    private Clicks.click click;

    public TextAdapter(List<T> data, Clicks.click click) {
        this.data = data;
        this.click = click;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TextViewHolder viewHolder = (TextViewHolder) holder;
        OpinionSDB opinion = (OpinionSDB)data.get(position);
        viewHolder.bind(opinion.nm, click);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
