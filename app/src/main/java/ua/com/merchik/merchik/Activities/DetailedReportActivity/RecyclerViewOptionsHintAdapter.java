package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers.PriceSaveGuard;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.RecentItem;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;

public class RecyclerViewOptionsHintAdapter extends RecyclerView.Adapter<RecyclerViewOptionsHintAdapter.ViewHolder>{
    private Context mContext;
    private List<RecentItem> itemList;
    private String dataType;
    private Dialog dialog;
    private ReportPrepareDB reportPrepareTovar;

    private long currentTime = System.currentTimeMillis()/1000;

    public RecyclerViewOptionsHintAdapter(Context context, List<RecentItem> list, ReportPrepareDB rpt, Dialog dialog, String type) {
        this.mContext = context;
        this.itemList = list;
        this.dataType = type;
        this.dialog = dialog;
        this.reportPrepareTovar = rpt;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        Button button;

        public ViewHolder(View v) {
            super(v);
            button = v.findViewById(R.id.button4);
        }

        public void bind(RecentItem recentItem){
            button.setText(recentItem.getValue());
            button.setOnClickListener(view -> {

                boolean saved;
                switch (dataType){
                    case ("face") :
                        saved = ReportPrepareRealm.updateFields(reportPrepareTovar, current -> {
                            current.setFace(recentItem.getValue());
                            current.setUploadStatus(1);
                            current.setDtChange(currentTime);
                        });
                        break;

                    case ("price") :
                        if (!PriceSaveGuard.savePrice(mContext, reportPrepareTovar, recentItem.getValue(), false)) return;
                        saved = true;
                        break;

                    case ("amount") :
                        saved = ReportPrepareRealm.updateFields(reportPrepareTovar, current -> {
                            current.setAmount(Integer.parseInt(recentItem.getValue()));
                            current.setUploadStatus(1);
                            current.setDtChange(currentTime);
                        });
                        break;
                    default:
                        return;
                }

                if (!saved) {
                    Toast.makeText(mContext, "Запись товара не найдена. Обновите список товаров.", Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(mContext, "Внесено: " + recentItem.getValue(), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            });
        }
    }






    @NonNull
    @Override
    public RecyclerViewOptionsHintAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.button, viewGroup, false);
        return new RecyclerViewOptionsHintAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewOptionsHintAdapter.ViewHolder viewHolder, int i) {
        RecentItem recentItem = itemList.get(i);
        viewHolder.bind(recentItem);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


}
