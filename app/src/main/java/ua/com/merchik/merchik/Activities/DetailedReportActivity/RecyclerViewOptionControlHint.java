package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RetrofitResponse.RecentItem;

public class RecyclerViewOptionControlHint extends RecyclerView.Adapter<RecyclerViewOptionControlHint.ViewHolder>{

    private List<RecentItem> itemList;
    private HintListener listener;

    public RecyclerViewOptionControlHint(List<RecentItem> itemList, HintListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerViewOptionControlHint.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.button, parent, false);
        return new RecyclerViewOptionControlHint.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentItem recentItem = itemList.get(position);
        holder.bind(recentItem);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public interface HintListener {
        void onClick(String s);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        Button button;

        public ViewHolder(View v) {
            super(v);
            button = v.findViewById(R.id.button4);
        }

        public void bind(RecentItem recentItem){
            button.setText(recentItem.getValue());
            button.setOnClickListener(v -> listener.onClick(recentItem.getValue()));
        }
    }



}
