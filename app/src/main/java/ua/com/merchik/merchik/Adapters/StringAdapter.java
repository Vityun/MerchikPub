package ua.com.merchik.merchik.Adapters;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;

public class StringAdapter extends RecyclerView.Adapter<StringAdapter.ViewHolder> {

    private Context context;
    private List<SpannableStringBuilder> dataList;

    public StringAdapter(Context context, List<SpannableStringBuilder> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public StringAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        StringAdapter.ViewHolder strAdap = new StringAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rv_text, parent, false));
        return strAdap;
    }

    @Override
    public void onBindViewHolder(@NonNull StringAdapter.ViewHolder holder, int position) {
        holder.bind(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        try {
            return dataList.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            layout = itemView.findViewById(R.id.layout);
        }

        public void bind(SpannableStringBuilder data) {
            try {
                textView.setText(data);
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "StringAdapter/onBindViewHolder/bind", "Exception e: " + e);
            }
        }
    }
}
