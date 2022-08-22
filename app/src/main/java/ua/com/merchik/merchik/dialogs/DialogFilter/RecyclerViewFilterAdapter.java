package ua.com.merchik.merchik.dialogs.DialogFilter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.dialogs.DialogFilter.ViewHolders.ViewHolderAutoText;
import ua.com.merchik.merchik.dialogs.DialogFilter.data.DialogFilterRecyclerData;

public class RecyclerViewFilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<DialogFilterRecyclerData> data;
    private Clicks.click click;


    public RecyclerViewFilterAdapter(Context context, List<DialogFilterRecyclerData> data, Clicks.click click) {
        this.mContext = context;
        this.data = data;
        this.click = click;
    }

    private int filterTypeToInt(int position){
        switch (data.get(position).filterType){
            case AUTO_TEXT: return 1;
            default: return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return filterTypeToInt(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1:
                return new ViewHolderAutoText(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_auto_complete_text_filter, parent, false)); // TODO создание нового вьюхолдера

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 1:
                ViewHolderAutoText viewHolderAutoText = (ViewHolderAutoText) holder;
                viewHolderAutoText.bind(data.get(position), click);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();   // TODO заполнить дату и возвращать её размер
    }
}
