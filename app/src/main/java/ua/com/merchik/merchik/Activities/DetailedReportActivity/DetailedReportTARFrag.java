package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.UniversalAdapter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.AddressRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.dialogs.DialodTAR.DialogCreateTAR;
import ua.com.merchik.merchik.dialogs.DialogData;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class DetailedReportTARFrag extends Fragment {

    private Context mContext;
    private WpDataDB wpDataDB;

    public DetailedReportTARFrag(Context mContext, WpDataDB wpDataDB) {
        this.mContext = mContext;
        this.wpDataDB = wpDataDB;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_dr_tar, container, false);

        try {
            FloatingActionButton fabAdd = v.findViewById(R.id.fabAdd);
            RecyclerView recycler = v.findViewById(R.id.recycler);

            try {
                // Установка Ресайклера
                UniversalAdapter recyclerViewReclamations = new UniversalAdapter(mContext, SQL_DB.tarDao().getAllByInfo(1, wpDataDB.getClient_id(), wpDataDB.getAddr_id(), (System.currentTimeMillis() / 1000 - 5184000)), new Globals.TARInterface() {
                    @Override
                    public void onSuccess(TasksAndReclamationsSDB data) {
                        DialogData dialog = new DialogData(v.getContext());
                        dialog.setTitle("Раздел находится в разработке");
                        dialog.setText("Отображение Задачи/Рекламации в данном контексте ещё не реализовано. \n\nПопробуйте открыть этот ЗиР из соответствующего раздела в Меню.");
                        dialog.setClose(dialog::dismiss);
                        dialog.show();
                    }

                    @Override
                    public void onFailure(String error) {
                        DialogData dialog = new DialogData(v.getContext());
                        dialog.setTitle("Раздел находится в разработке");
                        dialog.setText("Отображение Задачи/Рекламации в данном контексте ещё не реализовано. \n\nПопробуйте открыть этот ЗиР из соответствующего раздела в Меню.");
                        dialog.setClose(dialog::dismiss);
                        dialog.show();
                    }
                });

                recycler.setAdapter(recyclerViewReclamations);
                recycler.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            }catch (Exception e){
                Globals.writeToMLOG("ERROR", "DetailedReportTARFrag/onCreateView/UniversalAdapter", "Exception e: " + e);
            }

            // Установка Fab
            fabAdd.setOnClickListener(v1 -> {
                Intent intent = new Intent(v.getContext(), PhotoLogActivity.class);

                DialogCreateTAR dialog = new DialogCreateTAR(v.getContext());
                dialog.setClose(dialog::dismiss);
                dialog.address = AddressRealm.getAddressById(wpDataDB.getAddr_id());
                dialog.customer = CustomerRealm.getCustomerById(wpDataDB.getClient_id());
                dialog.setRecyclerView(() -> {
                    intent.putExtra("choise", true);
                    if (dialog.address != null){
                        intent.putExtra("address", dialog.address.getAddrId());
                    }

                    if (dialog.customer != null){
                        intent.putExtra("customer", dialog.customer.getId());
                    }

                    startActivityForResult(intent, 100);
                });
                dialog.show();
            });

        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "DetailedReportTARFrag/onCreateView", "Exception e: " + e);
        }

        return v;
    }
}
