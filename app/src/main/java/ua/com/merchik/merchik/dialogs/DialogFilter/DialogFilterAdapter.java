package ua.com.merchik.merchik.dialogs.DialogFilter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.EasyTextViewHolder;
import ua.com.merchik.merchik.ViewHolders.RadioButton3ViewHolder;
import ua.com.merchik.merchik.ViewHolders.SpinnerCustomViewHorder;

public class  DialogFilterAdapter <T> extends RecyclerView.Adapter {

    private Context context;
    private Globals.SourceAct source;
    private List<T> data;

    private List<DialogFilter.WpRecycler> WPRES;

    private Click click;

    DialogFilterAdapter(Context context, Globals.SourceAct source, Click click) {
        this.context = context;
        this.source = source;
        this.click = click;
    }

    public void setData(List<DialogFilter.WpRecycler> data){
        this.WPRES = data;
    }

    @Override
    public int getItemViewType(int position) {
        return WPRES.get(position).datatype;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case 0: return new EasyTextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_text, parent, false));
            case 1: return new RadioButton3ViewHolder<>(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_radiobutt3, parent, false));
            case 2: return new SpinnerCustomViewHorder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_spinner, parent, false));
            default: return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        EasyTextViewHolder EasyTextVH;
        RadioButton3ViewHolder RadioButtonVH;
        SpinnerCustomViewHorder VH3;

        switch (holder.getItemViewType()){
            case 0:
                EasyTextVH = (EasyTextViewHolder) holder;
                EasyTextVH.bind("test text", null);
                break;

            case 1:
                RadioButtonVH = (RadioButton3ViewHolder) holder;
                RadioButtonVH.bind(source, click);
                break;

            case 2:
                VH3 = (SpinnerCustomViewHorder) holder;
                VH3.bindFilter(source, click);
                break;
        }
    }

    @Override
    public int getItemCount() {
        try {
            return WPRES.size();
        }catch (Exception e){
            return 0;
        }
    }
}
