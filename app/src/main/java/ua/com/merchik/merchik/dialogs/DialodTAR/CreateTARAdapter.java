package ua.com.merchik.merchik.dialogs.DialodTAR;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.AutoCompleteTextViewHolder;
import ua.com.merchik.merchik.ViewHolders.ButtonViewHorder;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.ViewHolders.EnterTextViewHolder;
import ua.com.merchik.merchik.ViewHolders.PhotoAndInfoViewHolder;
import ua.com.merchik.merchik.ViewHolders.SpinnerCustomViewHorder;
import ua.com.merchik.merchik.data.TestViewHolderData;

public class CreateTARAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    public List<TestViewHolderData> data;
    private Clicks.clickListener click;


    public Integer addr;
    public Integer customer;

    public CreateTARAdapter(Context context, List<TestViewHolderData> list, Clicks.clickListener click) {
        this.context = context;
        this.click = click;
        data = list;
        Log.e("VIEW_HOLDER_TEST", "HERE");
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).typeNumber;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e("VIEW_HOLDER_TEST", "viewType: " + viewType);
        switch (viewType) {
            case 0: return new PhotoAndInfoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_photo_and_info, parent, false));
            case 1: return new AutoCompleteTextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_auto_complete_text, parent, false));
            case 2: return new EnterTextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_enter_text, parent, false));
            case 3: return new ButtonViewHorder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_button, parent, false));
            case 4: return new SpinnerCustomViewHorder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_spinner, parent, false));
        }

        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

//        Log.e("VIEW_HOLDER_TEST", "1_position: " + position);
//        Log.e("VIEW_HOLDER_TEST", "holder.getItemViewType(): " + holder.getItemViewType());

        switch (holder.getItemViewType()) {
            case 0:
                PhotoAndInfoViewHolder viewHolder0 = (PhotoAndInfoViewHolder)holder;
                viewHolder0.bind(data.get(position), click);
                break;

            case 1:
                AutoCompleteTextViewHolder viewHolder1 = (AutoCompleteTextViewHolder)holder;
                viewHolder1.bind(data.get(position), click);
                break;

            case 2:
                EnterTextViewHolder viewHolder2 = (EnterTextViewHolder)holder;
                viewHolder2.bind(data.get(position), click);
                break;

            case 3:
                ButtonViewHorder viewHolder3 = (ButtonViewHorder)holder;
                viewHolder3.bind(data.get(position), click);
                break;

            case 4:
                SpinnerCustomViewHorder viewHolder4 = (SpinnerCustomViewHorder)holder;
                viewHolder4.bind(data.get(position), click);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
