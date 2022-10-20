package ua.com.merchik.merchik.Activities.WorkPlanActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.RecycleViewWPAdapter;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFilter.DialogFilter;

public class WPDataFragmentHome extends Fragment {

    private Globals globals = new Globals();
    private RealmResults<WpDataDB> workPlan;

    private RecyclerView recyclerView;
    private RecycleViewWPAdapter adapter;

    private EditText searchView;
    private ImageButton filter;

    private Date dateFrom;
    private Date dateTo;

    public WPDataFragmentHome() {
        Log.d("test", "test");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_tab_wp_home, container, false);

        recyclerView = v.findViewById(R.id.RecyclerViewWorkPlan);
        searchView = v.findViewById(R.id.searchView);
        filter = v.findViewById(R.id.filter);

        // Данные для фильтра даты
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        dateFrom = cal.getTime();
        dateTo = Clock.timeLongToDAte(Clock.getDatePeriodLong(cal.getTime().getTime(), +3) / 1000);

        workPlan = RealmManager.getAllWorkPlan();
//        workPlan = workPlan.where().between("dt", dateFrom, dateTo).sort("dt_start", Sort.ASCENDING, "addr_id", Sort.ASCENDING).findAll();


        if (workPlan == null || workPlan.size() == 0) {
            DialogData dialogData = new DialogData(v.getContext());
            dialogData.setTitle("План работ пуст.");
            dialogData.setText("Выполните Синхронизацию таблиц для получения Плана работ.");
            dialogData.setClose(dialogData::dismiss);
            dialogData.show();
        } else {
            try {
                visualizeWpData();
            } catch (Exception e) {
                globals.alertDialogMsg(v.getContext(), "Возникла ошибка. Сообщите о ней своему администратору. Ошибка1: " + e);
            }
        }

        return v;
    }//------------------------------- /ON CREATE --------------------------------------------------


    private void visualizeWpData() {
        adapter = new RecycleViewWPAdapter(getContext(), workPlan);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        searchView.setText(Clock.today);
        searchView.clearFocus();

        adapter.getFilter().filter(searchView.getText());
        recyclerView.scheduleLayoutAnimation();

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    adapter.getFilter().filter(s);
                    recyclerView.scheduleLayoutAnimation();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        setFilter();
    }


    private void setFilter() {
        try {
            DialogFilter dialog = new DialogFilter(getContext(), Globals.SourceAct.WP_DATA);

            try {
                dialog.setTextFilter(searchView.getText().toString());
                dialog.setClose(dialog::dismiss);

//                dialog.dateFrom = Clock.getHumanTimeYYYYMMDD(dateFrom.getTime());
//                dialog.dateTo = Clock.getHumanTimeYYYYMMDD(dateTo.getTime());

                dialog.setCancel(() -> {
                    searchView.setText("");
                    setFilterIco(dialog);
                    adapter.updateData(workPlan);
                    recyclerView.scheduleLayoutAnimation();
                    adapter.notifyDataSetChanged();
                });
                dialog.setApply(() -> {
                    RealmResults<WpDataDB> wp = RealmManager.getAllWorkPlan();
                    if (dialog.clientId != null) {
                        wp = wp.where().equalTo("client_id", dialog.clientId).findAll();
                    }

                    if (dialog.addressId != null) {
                        wp = wp.where().equalTo("addr_id", dialog.addressId).findAll();
                    }

                    if (dialog.dateFrom != null && dialog.dateTo != null) {

                        Date dt1 = Clock.stringDateConvertToDate(dialog.dateFrom);
                        Date dt2 = Clock.stringDateConvertToDate(dialog.dateTo);

                        if (dt1 != null && dt2 != null) {
                            wp = wp.where().between("dt", dt1, dt2).findAll();
                        }
                    }


                    adapter.updateData(wp);
                    if (dialog.textFilter != null && !dialog.textFilter.equals("")) {
                        searchView.setText(dialog.textFilter);
                    }
                    adapter.notifyDataSetChanged();
                    setFilterIco(dialog);
                });
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "WPDataFragmentHome/setFilter/createDialog", "Exception e: " + e + "\n\n" + Arrays.toString(e.getStackTrace()));
            }

            try {
                setFilterIco(dialog);
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "WPDataFragmentHome/setFilter/setFilterIco", "Exception e: " + e + "\n\n" + Arrays.toString(e.getStackTrace()));
            }
            try {
                filter.setOnClickListener((v) -> {
                    dialog.show();
                });
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "WPDataFragmentHome/setFilter/setOnClickListener", "Exception e: " + e + "\n\n" + Arrays.toString(e.getStackTrace()));
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "WPDataFragmentHome/setFilter", "Exception e: " + e + "\n\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Set that
     */
    private void setFilterIco(DialogFilter dialog) {
        if (dialog.isFiltered()) {
            filter.setImageDrawable(getResources().getDrawable(R.drawable.ic_filterbold));
        } else {
            filter.setImageDrawable(getResources().getDrawable(R.drawable.ic_filter));
        }
    }
}
