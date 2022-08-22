package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium.Detailed;

public class TableSubAdapter extends RecyclerView.Adapter<TableSubAdapter.ViewHolder> {

    private final List<Detailed> table;

    public TableSubAdapter(List<Detailed> table) {
        this.table = table;
    }

    @NonNull
    @Override
    public TableSubAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_table_premium_row, parent, false);
        return new TableSubAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TableSubAdapter.ViewHolder holder, int position) {
        holder.bind(table.get(position));
    }

    @Override
    public int getItemCount() {
        try {
            return table.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout;
        private RecyclerView recyclerView;
        private TextView title, column1, column2, column3, column4;

        public ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            title = itemView.findViewById(R.id.title);
            column1 = itemView.findViewById(R.id.titleColumn1);
            column2 = itemView.findViewById(R.id.titleColumn2);
            column3 = itemView.findViewById(R.id.titleColumn3);
            column4 = itemView.findViewById(R.id.titleColumn4);
            recyclerView = itemView.findViewById(R.id.recycler_view);
        }

        public void bind(Detailed detailed) {

            String prihod = String.valueOf((int) detailed.prihod);
            String rashod = String.valueOf((int) detailed.rashod);

            title.setText(detailed.docDefName);
            column2.setText(prihod);
            column3.setText(rashod);
            layout.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorUnselectedTab));
        }
    }
}
