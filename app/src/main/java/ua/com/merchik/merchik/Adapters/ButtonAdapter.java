package ua.com.merchik.merchik.Adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.ButtonViewHorder;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;

public class ButtonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SiteHintsDB> data;
    private Clicks.click click;

    public ButtonAdapter(List<SiteHintsDB> data, Clicks.click click) {
        this.data = data;
        this.click = click;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ButtonViewHorder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_button_2, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ButtonViewHorder viewHolder3 = (ButtonViewHorder)holder;
        viewHolder3.bind(data.get(position), click);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
