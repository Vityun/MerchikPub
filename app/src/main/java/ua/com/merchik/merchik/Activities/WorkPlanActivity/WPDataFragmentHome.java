package ua.com.merchik.merchik.Activities.WorkPlanActivity;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFilter.DialogFilter;

public class WPDataFragmentHome extends Fragment {

    private Globals globals = new Globals();
    private RealmResults<WpDataDB> workPlan;

    private enum TitleMode {SHORT, FULL}

    private RecyclerView recyclerView;
    private RecycleViewWPAdapter adapter;

    private EditText searchView;
    private TextView title;
    private ImageButton filter;
    private ImageView titleClose;

    private StringBuilder titleMsg;

    private Date dateFrom;
    private Date dateTo;

    public WPDataFragmentHome() {
        Log.d("test", "test");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_wp_home, container, false);

        try {
            recyclerView = v.findViewById(R.id.RecyclerViewWorkPlan);
            searchView = v.findViewById(R.id.searchView);
            filter = v.findViewById(R.id.filter);
            title = v.findViewById(R.id.title);
            title.setTextColor(-10987432);  // Как у закладки "План работ"
            title.setOnClickListener(view -> title.setVisibility(View.GONE));
            title.setPaintFlags(title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            titleClose = v.findViewById(R.id.titleClose);
            titleClose.setOnClickListener(view -> {
                title.setVisibility(View.GONE);
                titleClose.setVisibility(View.GONE);
            });

            // Данные для фильтра даты
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            dateFrom = cal.getTime();
            dateTo = Clock.timeLongToDAte(Clock.getDatePeriodLong(cal.getTime().getTime(), +7) / 1000);

            workPlan = RealmManager.getAllWorkPlan();
            try {
                if (workPlan != null){
                    Globals.writeToMLOG("INFO", "WPDataFragmentHome", "workPlan: " + workPlan.size());
                    StringBuilder wpDebugData = new StringBuilder();
                    for (WpDataDB item : workPlan){
                        wpDebugData.append("id:").append(item.getId()).append("/").append("dad2:").append(item.getCode_dad2()).append("\n");
                    }
                    Globals.writeToMLOG("INFO", "WPDataFragmentHome", "wpDebugData: " + wpDebugData);
                }else {
                    Globals.writeToMLOG("INFO", "WPDataFragmentHome", "workPlan is null");
                }
            }catch (Exception e){
                Globals.writeToMLOG("ERROR", "WPDataFragmentHome", "Exception e: " + e);
            }
//        workPlan = workPlan.where().between("dt", dateFrom, dateTo).sort("dt_start", Sort.ASCENDING, "addr_id", Sort.ASCENDING).findAll();

            UsersSDB usersSDB = SQL_DB.usersDao().getById(Globals.userId);
            if (System.currentTimeMillis() / 1000 < 1668124799) {
                showAlertMsg();
            } else if (usersSDB != null && usersSDB.reportCount <= 10) {
                showAlertMsg();
            } else {
                // nothing to do
            }


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
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "WPDataFragmentHome/onCreateView", "Exception e: " + e);
        }

        return v;
    }//------------------------------- /ON CREATE --------------------------------------------------

    private SpannableStringBuilder createTitleMsg(RealmResults<WpDataDB> wp, TitleMode mode) {
        SpannableStringBuilder res = new SpannableStringBuilder();

        if (wp != null && wp.size() > 0) {
            // Запланированные работы
            int wpSum = wp.sum("cash_ispolnitel").intValue();

            // Выполненные работы
            RealmResults<WpDataDB> wpStatus = wp.where().equalTo("status", 1).findAll();
            int wpStatus1Size = wpStatus.size();    // Количество проведённых отчётов
            int wpStatus1Sum = wpStatus.sum("cash_ispolnitel").intValue();  // Сумма полученная за проведенные отчёты
            int percentWpStatus1 = (wpStatus1Size * 100) / wp.size(); // Процент выполненных работ

            // Не Віполненные
            RealmResults<WpDataDB> wpStatus0 = wp.where().equalTo("status", 0).findAll();
            int wpStatus0Size = wpStatus0.size();
            int wpStatus0Sum = wpStatus0.sum("cash_ispolnitel").intValue();
            int percentWpStatus0 = (wpStatus0Size * 100) / wp.size();

            if (mode.equals(TitleMode.FULL)) {
                res.append(Html.fromHtml("<b>За період: </b> з ")).append(Clock.getHumanTimeYYYYMMDD(wp.get(0).getDt().getTime() / 1000)).append(" по ").append(Clock.getHumanTimeYYYYMMDD(wp.get(wp.size() - 1).getDt().getTime() / 1000)).append("\n\n");
                res.append(Html.fromHtml("<b>Заплановано робіт (Пр): </b>")).append("" + wp.size()).append(" (100%),").append(" на суму ").append("" + wpSum).append(" грн.").append("\n\n");
                res.append(Html.fromHtml("<b>Виконано робіт (Вр): </b>")).append("" + wpStatus1Size).append(" (").append("" + percentWpStatus1).append("%), на суму ").append("" + wpStatus1Sum).append(" грн.").append("\n\n");
                res.append(Html.fromHtml("<b>Не виконано робіт (Нр): </b>")).append("" + wpStatus0Size).append(" (").append("" + percentWpStatus0).append("%), на суму ").append("" + wpStatus0Sum).append(" грн.");
            } else if (mode.equals(TitleMode.SHORT)) {
                res.append("Пр: ").append("" + wp.size()).append(" (").append("" + wpSum).append("гр) / ").append("Вр: ").append("" + wpStatus1Size).append(" (").append("" + wpStatus1Sum).append("гр) / ").append("Нр: ").append("" + wpStatus0Size).append(" (").append("" + wpStatus0Sum).append("гр)");
            }
        } else {
            res.append("План робіт пустий.");
        }

        return res;
    }

    private void showAlertMsg() {
        DialogData dialogData = new DialogData(getContext());
        dialogData.setTitle("УВАГА!");
        dialogData.setText("Крім поточних робіт, РЕКОМЕНДУЄМО сьогодні виконати роботи, які заплановані НА ЗАВТРА та післязавтра.");
        dialogData.setClose(dialogData::dismiss);
        dialogData.show();
    }

    private void visualizeWpData() {
        Globals.writeToMLOG("INFO", "WPDataFragmentHome/visualizeWpData", "workPlan: " + workPlan.size());
        adapter = new RecycleViewWPAdapter(getContext(), workPlan);
        title.setText(createTitleMsg(workPlan, TitleMode.SHORT));
        title.setOnClickListener(view -> {
            DialogData dialogData = new DialogData(getContext());
            dialogData.setTitle("ІНФО");
            dialogData.setText(createTitleMsg(workPlan, TitleMode.FULL));
            dialogData.setClose(dialogData::dismiss);
            dialogData.show();
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

//        searchView.setText(Clock.today);
        searchView.setText("");
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
        adapter.notifyDataSetChanged();
    }


    private void setFilter() {
        try {
            DialogFilter dialog = new DialogFilter(getContext(), Globals.SourceAct.WP_DATA);

            try {
                dialog.setDates(dateFrom, dateTo);
                dialog.setRecycler();

                dialog.setTextFilter(searchView.getText().toString());
                dialog.setClose(dialog::dismiss);

                dialog.setCancel(() -> {
                    searchView.setText("");
                    setFilterIco(dialog);
                    adapter.updateData(workPlan);
                    title.setText(createTitleMsg(workPlan, TitleMode.SHORT));
                    title.setOnClickListener(view -> {
                        DialogData dialogData = new DialogData(getContext());
                        dialogData.setTitle("ІНФО");
                        dialogData.setText(createTitleMsg(workPlan, TitleMode.FULL));
                        dialogData.setClose(dialogData::dismiss);
                        dialogData.show();
                    });
                    recyclerView.scheduleLayoutAnimation();
                    adapter.notifyDataSetChanged();
                });
                dialog.setApply(() -> {
                    applyFilter(dialog);
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


            applyFilter(dialog);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "WPDataFragmentHome/setFilter", "Exception e: " + e + "\n\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    private void applyFilter(DialogFilter dialog) {
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

        title.setText(createTitleMsg(wp, TitleMode.SHORT));
        RealmResults<WpDataDB> finalWp = wp;
        title.setOnClickListener(view -> {
            DialogData dialogData = new DialogData(getContext());
            dialogData.setTitle("ІНФО");
            dialogData.setText(createTitleMsg(finalWp, TitleMode.FULL));
            dialogData.setClose(dialogData::dismiss);
            dialogData.show();
        });
        if (dialog.textFilter != null && !dialog.textFilter.equals("")) {
            searchView.setText(dialog.textFilter);
        }
        adapter.notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
        setFilterIco(dialog);
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
