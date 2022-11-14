package ua.com.merchik.merchik.Activities.PremiumActivity.PremiumTable;

import android.graphics.Paint;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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
        return new PremiumTableDataAdapter.PremiumTableHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.premium_table_item, parent, false));
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

        public PremiumTableHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.premium_table_item);
            name = itemView.findViewById(R.id.name);
            name.setPaintFlags(name.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            column1 = itemView.findViewById(R.id.col1);
            column2 = itemView.findViewById(R.id.col2);
            column3 = itemView.findViewById(R.id.col3);
            column4 = itemView.findViewById(R.id.col4);
        }

        public void bind(Detailed detailed) {

            if ((int) detailed.prihod == 0) {
                column2.setVisibility(View.INVISIBLE);
            } else {
                column2.setVisibility(View.VISIBLE);
            }

            if ((int) detailed.rashod == 0) {
                column3.setVisibility(View.INVISIBLE);
            } else {
                column3.setVisibility(View.VISIBLE);
            }


            CharSequence prihodChar = (int) detailed.prihod < 0 ? Html.fromHtml("<font color=red>" + (int) detailed.prihod + "</font>") : "" + (int) detailed.prihod;
            CharSequence rashodChar = (int) detailed.rashod < 0 ? Html.fromHtml("<font color=red>" + (int) detailed.rashod + "</font>") : "" + (int) detailed.rashod;

            name.setText(detailed.docNom + "(" + detailed.docDat + ")");
            name.setTextColor(-10987432);
            column1.setText("");
            column1.setVisibility(View.GONE);
            column2.setText(prihodChar);
            column3.setText(rashodChar);
            column4.setText("");

            name.setOnClickListener(view -> {
                Toast.makeText(view.getContext(), "Ви натиснули на " + detailed.docNom + ", він має код дад2: " + detailed.codeDad2, Toast.LENGTH_LONG).show();
            });
        }

    }

    /*Обработчик кликов по заголовку ПУНКТУ премии*/
    public interface PremiumListener {
        void onClick(View view, Detailed item);
    }
}
