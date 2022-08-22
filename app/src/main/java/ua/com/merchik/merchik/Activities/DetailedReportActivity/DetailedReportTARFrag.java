package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.tarList;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARSecondFrag;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.UniversalAdapter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.AddressRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.database.realm.tables.UsersRealm;
import ua.com.merchik.merchik.dialogs.DialodTAR.DialogCreateTAR;
import ua.com.merchik.merchik.dialogs.DialogData;

public class DetailedReportTARFrag extends Fragment {

    private Context mContext;
    private WpDataDB wpDataDB;
    private List<TasksAndReclamationsSDB> tasksAndReclamationsSDBList;

    private FragmentManager fragmentManager;
    private TARSecondFrag secondFrag;

    public DetailedReportTARFrag(Context mContext, WpDataDB wpDataDB) {
        this.mContext = mContext;
        this.wpDataDB = wpDataDB;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dr_tar, container, false);

        fragmentManager = getParentFragmentManager();
        tasksAndReclamationsSDBList = SQL_DB.tarDao().getAllByInfo(1, wpDataDB.getClient_id(), wpDataDB.getAddr_id(), (System.currentTimeMillis() / 1000 - 5184000));

        try {
            FloatingActionButton fabAdd = v.findViewById(R.id.fabAdd);
            RecyclerView recycler = v.findViewById(R.id.recycler);

            try {
                // Установка Ресайклера
                UniversalAdapter recyclerViewReclamations = new UniversalAdapter(mContext, tasksAndReclamationsSDBList, false, new Globals.TARInterface() {
                    @Override
                    public void onSuccess(TasksAndReclamationsSDB data) {
                        Intent intent = new Intent(v.getContext(), TARActivity.class);
                        intent.putExtra("TAR_ID", data.id);
                        v.getContext().startActivity(intent);
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
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "DetailedReportTARFrag/onCreateView/UniversalAdapter", "Exception e: " + e);
            }

            // Установка Fab
            fabAdd.setOnClickListener(v1 -> {
                Intent intent = new Intent(v.getContext(), PhotoLogActivity.class);

                DialogCreateTAR dialog = new DialogCreateTAR(v.getContext());
                dialog.setClose(dialog::dismiss);
                dialog.users = UsersRealm.getUsersDBById(wpDataDB.getUser_id());
                dialog.address = AddressRealm.getAddressById(wpDataDB.getAddr_id());
                dialog.customer = CustomerRealm.getCustomerById(wpDataDB.getClient_id());
                dialog.setTarType(1);
                dialog.setRecyclerView(() -> {
                    intent.putExtra("choise", true);
                    if (dialog.address != null) {
                        intent.putExtra("address", dialog.address.getAddrId());
                    }

                    if (dialog.customer != null) {
                        intent.putExtra("customer", dialog.customer.getId());
                    }

                    startActivityForResult(intent, 100);
                });
                dialog.clickSave(() -> {
                }, 1);
                dialog.show();
            });

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportTARFrag/onCreateView", "Exception e: " + e);
        }

        return v;
    }
}
