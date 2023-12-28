package ua.com.merchik.merchik.ServerExchange.CustomExchange;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;

public class CustomExchangeAdapter extends RecyclerView.Adapter<CustomExchangeAdapter.ViewHolder> {

    private List<SynchronizationTimetableDB> data;

    public CustomExchangeAdapter(List<SynchronizationTimetableDB> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public CustomExchangeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_sync_button, parent, false);
        return new CustomExchangeAdapter.ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull CustomExchangeAdapter.ViewHolder holder, int position) {
        try {
            holder.bind(data.get(position));
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "CustomExchangeAdapter/onBindViewHolder", "Exception e: " + e);
        }
    }

    @Override
    public int getItemCount() {
        try {
            if (data != null && data.size() > 0){
                return data.size();
            }else {
                return 0;
            }
        }catch (Exception e){
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout constraintLayout;
        private TextView textView;
        private TextView textSub;
        private TextView textCounter;

        ViewHolder(View view) {
            super(view);
            constraintLayout = view.findViewById(R.id.layout);
            textView = view.findViewById(R.id.text);
            textSub = view.findViewById(R.id.text_sub);
            textCounter = view.findViewById(R.id.cart_badge1);
        }

        public void bind(SynchronizationTimetableDB data) {
            textView.setText(data.tableTxt/* + " (" + Clock.getHumanTime2(data.getVpi_app()) + ")"*/);
            textSub.setText(Clock.getHumanTimeSecPattern(data.getVpi_app(), "dd HH:mm"));

            textView.setOnClickListener(v->{
                new CustomExchange().startExchangeBySyncTable(v.getContext(), data.getTable_name());
            });
//            textView.setBackgroundColor(textView.getContext().getResources().getColor(R.color.colorUnselectedTab));
        }
    }

}
