package ua.com.merchik.merchik.Activities.PremiumActivity.PremiumTable;

import android.text.Html;
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

public class PremiumTableHeaderSubAdapter extends RecyclerView.Adapter<PremiumTableHeaderSubAdapter.PremiumTableHeaderViewHolder> {

    private List<PremiumTableHeader.DetailedSubHeader> data;
    private PremiumTableHeaderAdapter.PremiumListener listener;

    public PremiumTableHeaderSubAdapter(List<PremiumTableHeader.DetailedSubHeader> data) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PremiumTableHeaderSubAdapter.PremiumTableHeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PremiumTableHeaderSubAdapter.PremiumTableHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.premium_table_header, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PremiumTableHeaderSubAdapter.PremiumTableHeaderViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class PremiumTableHeaderViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout, textHeaderLayout;
        private TextView name;
        private TextView column1, column5, column2, column3, column4;
        private RecyclerView recyclerSub;
        private PremiumTableDataAdapter adapter;

        public PremiumTableHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.premium_table_header);
            textHeaderLayout = itemView.findViewById(R.id.header_text);
            name = itemView.findViewById(R.id.name);
            column1 = itemView.findViewById(R.id.col1);
            column5 = itemView.findViewById(R.id.col5);
            column2 = itemView.findViewById(R.id.col2);
            column3 = itemView.findViewById(R.id.col3);
            column4 = itemView.findViewById(R.id.col4);
            recyclerSub = itemView.findViewById(R.id.recycler_view);
        }

        public void bind(PremiumTableHeader.DetailedSubHeader detailedSubHeader) {
            textHeaderLayout.setOnClickListener(view -> {
                detailedSubHeader.isExpanded = !detailedSubHeader.isExpanded;
                if (detailedSubHeader.items.isEmpty()) detailedSubHeader.isExpanded = false;
                handleRecycler(detailedSubHeader.isExpanded);
            });

            textHeaderLayout.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.inActive));

            CharSequence prihod = (int) detailedSubHeader.header.sumComing < 0 ? Html.fromHtml("<font color=red>" + (int) detailedSubHeader.header.sumComing + "</font>") : "" + (int) detailedSubHeader.header.sumComing;
            CharSequence rashod = (int) detailedSubHeader.header.sumConsumption < 0 ? Html.fromHtml("<font color=red>" + (int) detailedSubHeader.header.sumConsumption + "</font>") : "" + (int) detailedSubHeader.header.sumConsumption;

            name.setText(detailedSubHeader.header.date);
            column1.setText("" + ((int) detailedSubHeader.header.sumInitialBalance == 0 ? "" : (int) detailedSubHeader.header.sumInitialBalance));
            column5.setText("-");
            column2.setText(prihod);
            column3.setText(rashod);
            column4.setText("" + ((int) detailedSubHeader.header.sumEndBalance == 0 ? "" : (int) detailedSubHeader.header.sumEndBalance));

            adapter = new PremiumTableDataAdapter(detailedSubHeader.items);
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
