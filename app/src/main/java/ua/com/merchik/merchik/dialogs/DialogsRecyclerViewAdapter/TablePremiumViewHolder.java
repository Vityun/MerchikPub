package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.R;

public class TablePremiumViewHolder extends RecyclerView.ViewHolder {

    private ConstraintLayout layout;
    private TextView title, column1, column2, column3, column4;
    private RecyclerView recyclerView;

    public TablePremiumViewHolder(@NonNull View itemView) {
        super(itemView);
        layout = itemView.findViewById(R.id.layout);
        recyclerView = itemView.findViewById(R.id.recyclerView);
        title = itemView.findViewById(R.id.title);
        column1 = itemView.findViewById(R.id.titleColumn1);
        column2 = itemView.findViewById(R.id.titleColumn2);
        column3 = itemView.findViewById(R.id.titleColumn3);
        column4 = itemView.findViewById(R.id.titleColumn4);
    }

    public void bind(ViewHolderTypeList.TablePremiumLayoutData block) {
        title.setText(block.title);
        column1.setText(block.titleColumn1);
        column2.setText(block.titleColumn2);
        column3.setText(block.titleColumn3);
        column4.setText(block.titleColumn4);

        recyclerView.setAdapter(new TableAdapter(block.table));
        recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.VERTICAL, false));
    }

}
