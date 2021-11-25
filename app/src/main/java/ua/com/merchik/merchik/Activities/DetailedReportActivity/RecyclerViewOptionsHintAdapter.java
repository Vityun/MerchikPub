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

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RetrofitResponse.RecentItem;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class RecyclerViewOptionsHintAdapter extends RecyclerView.Adapter<RecyclerViewOptionsHintAdapter.ViewHolder>{
    private Context mContext;
    private List<RecentItem> itemList;
    private String dataType;
    private Dialog dialog;
    private ReportPrepareDB reportPrepareTovar;

    private String currentTime = "" + System.currentTimeMillis()/1000;

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

                switch (dataType){
                    case ("face") :
                        RealmManager.INSTANCE.executeTransaction(realm -> {
                            reportPrepareTovar.setFace(recentItem.getValue());
                            reportPrepareTovar.setUploadStatus(1);
                            reportPrepareTovar.setDtChange(currentTime);
                            RealmManager.setReportPrepareRow(reportPrepareTovar);
                        });
                        break;

                    case ("price") :
                        RealmManager.INSTANCE.executeTransaction(realm -> {
                            reportPrepareTovar.setPrice(recentItem.getValue());
                            reportPrepareTovar.setUploadStatus(1);
                            reportPrepareTovar.setDtChange(currentTime);
                            RealmManager.setReportPrepareRow(reportPrepareTovar);
                        });
                        break;

                    case ("amount") :
                        RealmManager.INSTANCE.executeTransaction(realm -> {
                            reportPrepareTovar.setAmount(Integer.parseInt(recentItem.getValue()));
                            reportPrepareTovar.setUploadStatus(1);
                            reportPrepareTovar.setDtChange(currentTime);
                            RealmManager.setReportPrepareRow(reportPrepareTovar);
                        });
                        break;
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
