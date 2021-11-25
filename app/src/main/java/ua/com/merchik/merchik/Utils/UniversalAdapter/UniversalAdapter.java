package ua.com.merchik.merchik.Utils.UniversalAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.ViewHolders.EasyTextViewHolder;
import ua.com.merchik.merchik.data.Database.Room.StandartSDB;

public class UniversalAdapter <T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<T> dataList;
    private Clicks.click click;

    // ===============================

    public UniversalAdapter(Context context, List<T> dataList, Clicks.click click) {
        this.context = context;
        this.dataList = dataList;
        this.click = click;
    }


    // ===============================

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EasyTextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EasyTextViewHolder easyVH = (EasyTextViewHolder) holder;

        StandartSDB standart = (StandartSDB) dataList.get(position);
        easyVH.bind(standart, click);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // ===============================


}
