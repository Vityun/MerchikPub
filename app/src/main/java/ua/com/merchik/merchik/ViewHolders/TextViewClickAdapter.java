package ua.com.merchik.merchik.ViewHolders;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;

public class TextViewClickAdapter <T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> data;
    private Clicks.click click;

    public TextViewClickAdapter(List<String> data, Clicks.click click) {
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
        viewHolder.bind(data.get(position), click);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
