package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
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

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.ViewHolder> {

    public boolean subAdapter;
    private final List<ViewHolderTypeList.TablePremiumLayoutData.PremiumTableRow> table;

    public TableAdapter(List<ViewHolderTypeList.TablePremiumLayoutData.PremiumTableRow> table) {
        this.table = table;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_table_premium_row, parent, false);
        return new TableAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            title = itemView.findViewById(R.id.title);
            column1 = itemView.findViewById(R.id.titleColumn1);
            column2 = itemView.findViewById(R.id.titleColumn2);
            column3 = itemView.findViewById(R.id.titleColumn3);
            column4 = itemView.findViewById(R.id.titleColumn4);
            recyclerView = itemView.findViewById(R.id.recycler_view);
        }

        public void bind(ViewHolderTypeList.TablePremiumLayoutData.PremiumTableRow premiumTableRow) {
            try {
                SpannableString string = new SpannableString(premiumTableRow.titleColumn);
                string.setSpan(new UnderlineSpan(), 0, string.length(), 0);
                title.setText(string);

                SpannableString string1 = new SpannableString(premiumTableRow.column1);
                string1.setSpan(new UnderlineSpan(), 0, string1.length(), 0);

                SpannableString string2 = new SpannableString(premiumTableRow.column2);
                string2.setSpan(new UnderlineSpan(), 0, string2.length(), 0);

                SpannableString string3 = new SpannableString(premiumTableRow.column3);
                string3.setSpan(new UnderlineSpan(), 0, string3.length(), 0);

                SpannableString string4 = new SpannableString(premiumTableRow.column4);
                string4.setSpan(new UnderlineSpan(), 0, string4.length(), 0);


                column1.setText(string1);
                column2.setText(string2);
                column3.setText(string3);
                column4.setText(string4);
            } catch (Exception e) {

            }

            column1.setOnClickListener(view -> premiumTableRow.click.onFailure("Раздел ещё не доработан"));
            column2.setOnClickListener(view -> premiumTableRow.click.onFailure("Раздел ещё не доработан"));
            column3.setOnClickListener(view -> premiumTableRow.click.onFailure("Раздел ещё не доработан"));
            column4.setOnClickListener(view -> premiumTableRow.click.onFailure("Раздел ещё не доработан"));

            layout.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorUnselectedTab));

            recyclerView.setVisibility(View.VISIBLE);

            try {
                TableSubAdapter adapter = new TableSubAdapter(premiumTableRow.data.detailed);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.VERTICAL, false));
            }catch (Exception e){

            }

            premiumTableRow.clicks = TableAdapter.this::notifyDataSetChanged;
        }

    }
}
