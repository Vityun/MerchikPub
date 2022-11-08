package ua.com.merchik.merchik.Activities.PremiumActivity.PremiumTable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.Activities.navigationMenu.MenuHeaderAdapter;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium.Detailed;

public class PremiumTableDataAdapter extends RecyclerView.Adapter<PremiumTableDataAdapter.PremiumTableHeaderViewHolder> {

    private List<Detailed> data;
    private PremiumTableHeaderAdapter.PremiumListener listener;

    public PremiumTableDataAdapter(List<Detailed> data) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PremiumTableDataAdapter.PremiumTableHeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PremiumTableDataAdapter.PremiumTableHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.premium_table_header, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PremiumTableDataAdapter.PremiumTableHeaderViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class PremiumTableHeaderViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout;
        private TextView name;
        private TextView column1, column2, column3, column4;
        private RecyclerView recyclerSub;
        private MenuHeaderAdapter.MenuSubAdapter adapter;

        public PremiumTableHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.premium_table_header);
            name = itemView.findViewById(R.id.name);
            column1 = itemView.findViewById(R.id.col1);
            column2 = itemView.findViewById(R.id.col2);
            column3 = itemView.findViewById(R.id.col3);
            column4 = itemView.findViewById(R.id.col4);
            recyclerSub = itemView.findViewById(R.id.recycler_view);
        }

        public void bind(Detailed detailed) {
          /*  layout.setOnClickListener(view -> {

            });*/

            name.setText(detailed.docDefName);
            column1.setText("");
            column2.setText("" + (int) detailed.prihod);
            column3.setText("" + (int) detailed.rashod);
            column4.setText("");
        }

    }

    /*Обработчик кликов по заголовку ПУНКТУ премии*/
    public interface PremiumListener {
        void onClick(View view, Detailed item);
    }
}
