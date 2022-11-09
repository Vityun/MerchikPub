package ua.com.merchik.merchik.Activities.PremiumActivity.PremiumTable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium.Detailed;

public class PremiumTableHeaderAdapter extends RecyclerView.Adapter<PremiumTableHeaderAdapter.PremiumTableHeaderViewHolder> {

    private List<PremiumTableHeader> data;
    private PremiumListener listener;

    public PremiumTableHeaderAdapter(List<PremiumTableHeader> data, PremiumListener listener) {
        this.data = data;
        this.listener = listener;
    }

    public void setNewData(List<PremiumTableHeader> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public PremiumTableHeaderAdapter.PremiumTableHeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PremiumTableHeaderAdapter.PremiumTableHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.premium_table_header, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PremiumTableHeaderAdapter.PremiumTableHeaderViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class PremiumTableHeaderViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout, textHeaderLayout;
        private TextView name;
        private TextView column1, column2, column3, column4;
        private RecyclerView recyclerSub;
        private PremiumTableHeaderSubAdapter adapter;

        public PremiumTableHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.premium_table_header);
            textHeaderLayout = itemView.findViewById(R.id.header_text);
            name = itemView.findViewById(R.id.name);
            column1 = itemView.findViewById(R.id.col1);
            column2 = itemView.findViewById(R.id.col2);
            column3 = itemView.findViewById(R.id.col3);
            column4 = itemView.findViewById(R.id.col4);
            recyclerSub = itemView.findViewById(R.id.recycler_view);
        }

        public void bind(PremiumTableHeader premiumTableHeader) {
            textHeaderLayout.setOnClickListener(view -> {
                premiumTableHeader.isExpanded = !premiumTableHeader.isExpanded;
                if (premiumTableHeader.detailedSubHeaders.isEmpty()) premiumTableHeader.isExpanded = false;
                handleRecycler(premiumTableHeader.isExpanded);
            });

            textHeaderLayout.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.active));

            name.setText(premiumTableHeader.detailedHeader.date);
            column1.setText("" + (int) premiumTableHeader.detailedHeader.sumInitialBalance);
            column2.setText("" + (int) premiumTableHeader.detailedHeader.sumComing);
            column3.setText("" + (int) premiumTableHeader.detailedHeader.sumConsumption);
            column4.setText("" + (int) premiumTableHeader.detailedHeader.sumEndBalance);

            adapter = new PremiumTableHeaderSubAdapter(premiumTableHeader.detailedSubHeaders);
            recyclerSub.setAdapter(adapter);
            recyclerSub.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.VERTICAL, false));
        }

        private void handleRecycler(boolean isExpanded) {
            if (isExpanded) {
                recyclerSub.setVisibility(View.VISIBLE);
            } else {
                recyclerSub.setVisibility(View.GONE);
            }
        }
    }

    /*Обработчик кликов по заголовку ПУНКТУ премии*/
    public interface PremiumListener {
        void onClick(View view, Detailed item);
    }
}
