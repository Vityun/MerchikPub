package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

import static ua.com.merchik.merchik.ServerExchange.Exchange.sendWpData2;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

@SuppressLint("ValidFragment")
public class DetailedReportOptionsFrag extends Fragment {

    private Context mContext;
    private ArrayList<Data> list;
    private WpDataDB wpDataDB;

    RecycleViewDRAdapter recycleViewDRAdapter;

    interface RefreshInterface{
        public void refreshAdapterFragmentB();
    }

    public DetailedReportOptionsFrag() {
    }

    public DetailedReportOptionsFrag(Context context, ArrayList<Data> list, WpDataDB wpDataDB) {
        // Required empty public constructor
        this.mContext = context;
        this.list = list;
        this.wpDataDB = wpDataDB;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dr_option, container, false);
        try {
            Button buttonSave = (Button) v.findViewById(R.id.button);
            Button buttonMakeAReport  = (Button) v.findViewById(R.id.button3);

            ImageView check = v.findViewById(R.id.check);
            if (wpDataDB.getSetStatus() == 1){
                check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_question_circle_regular));
                check.setColorFilter(mContext.getResources().getColor(R.color.colorInetYellow));
            }else {
                if (wpDataDB.getStatus() == 1){
                    check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_check));
                    check.setColorFilter(mContext.getResources().getColor(R.color.greenCol));
                }else {
                    if (Clock.dateConvertToLong(wpDataDB.getDt()) < System.currentTimeMillis()){
                        check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_exclamation_mark_in_a_circle));
                        check.setColorFilter(mContext.getResources().getColor(R.color.red_error));
                    }else {
                        check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_check));
                        check.setColorFilter(mContext.getResources().getColor(R.color.shadow));
                    }
                }
            }


            Options options = new Options();
            options.checkingSignalsOfTheExecutorReport(mContext, wpDataDB, 0, 0);

            WorkPlan workPlan = new WorkPlan();
            RecyclerView rvContacts = v.findViewById(R.id.DRRecycleView);

            List<OptionsDB> optionsButtons = workPlan.getOptionButtons2(workPlan.getWpOpchetId(wpDataDB), wpDataDB.getId());

            Collections.sort(optionsButtons, (o1, o2) -> o1.getSo().compareTo(o2.getSo()));

            buttonSave.setOnClickListener(b -> {
                Toast.makeText(mContext, "Данный раздел находится в разработке", Toast.LENGTH_LONG).show();
            });
            buttonMakeAReport.setOnClickListener(b -> {
                // Если среди опций есть опции с Блоком ПНС - не могу проводить
                int pnsCnt = 0;
                for (OptionsDB item : optionsButtons){
                    if (item.getBlockPns().equals("1")){
                        pnsCnt++;
                    }
                }


                new Options().conduct(getContext(), wpDataDB, optionsButtons, 3);

                if (wpDataDB.getSetStatus() == 1){
                    check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_question_circle_regular));
                    check.setColorFilter(mContext.getResources().getColor(R.color.colorInetYellow));

                    sendWpData2();  // Выгрузка статуса
                }

//                Toast.makeText(mContext, "Данный раздел находится в разработке. Провести отчёт следует в МВС Опций с блоком ПНС: " + pnsCnt, Toast.LENGTH_LONG).show();
            });

            Log.e("R_TRANSLATES", "convertedObject: START");

            List<Integer> ids = new ArrayList<>();
            for (OptionsDB item : optionsButtons){
                ids.add(Integer.parseInt(item.getOptionId()));
            }

            Log.e("R_TRANSLATES", "item: " + ids.size());


            for (Integer item : ids){
                Log.e("R_TRANSLATES", "Integeritem: " + item);
            }

            // Запрос к SQL БДшке. Получаем список обьектов сайта
            List<SiteObjectsSDB> list = SQL_DB.siteObjectsDao().getObjectsById(ids);

            Log.e("R_TRANSLATES", "item: " + list.size());

            for (SiteObjectsSDB item : list){
                Log.e("R_TRANSLATES", "SiteObjectsSDBitem: " + item.id);
            }

            Log.e("TEST_OPTIONS", "optionsButtons SIZE: " + optionsButtons.size());
            for (OptionsDB item : optionsButtons){
                options.optionControl(mContext, wpDataDB, item, null, Options.NNKMode.NULL);
            }


            recycleViewDRAdapter = new RecycleViewDRAdapter(mContext, wpDataDB, optionsButtons, list);
            rvContacts.setAdapter(recycleViewDRAdapter);
            rvContacts.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        }catch (Exception e){
            Log.e("R_TRANSLATES", "convertedObjectERROR: " + e);
            e.printStackTrace();
        }

        return v;
    }


}


/*
* Вова, Дай список стандартов
*
* Ограничить по коддад 2
*
* Вова, дай код которым показываешь мне стандарты
*
*
*
* */
