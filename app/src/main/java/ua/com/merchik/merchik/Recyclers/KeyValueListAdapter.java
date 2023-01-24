package ua.com.merchik.merchik.Recyclers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;

public class KeyValueListAdapter extends RecyclerView.Adapter<KeyValueListAdapter.ViewHolder> {

    private List<KeyValueData> data;

    public KeyValueListAdapter(List<KeyValueData> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_key_value, parent, false);
        return new KeyValueListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout;
        private TextView key, value;

        ViewHolder(View v) {
            super(v);
            layout = v.findViewById(R.id.layout);
            key = v.findViewById(R.id.key);
            value = v.findViewById(R.id.value);
        }

        public void bind(KeyValueData keyValueData) {
            key.setText(keyValueData.key);
            value.setText(keyValueData.value);
            value.setOnClickListener(view -> {
                if (keyValueData.click != null) keyValueData.click.click();
            });
        }
    }
}
