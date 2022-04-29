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
import android.widget.TextView;
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
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;

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

            Button download = v.findViewById(R.id.download);
            TextView information = v.findViewById(R.id.info_msg);

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

            WorkPlan workPlan = new WorkPlan();
            RecyclerView rvContacts = v.findViewById(R.id.DRRecycleView);

            List<OptionsDB> optionsButtons = workPlan.getOptionButtons2(workPlan.getWpOpchetId(wpDataDB), wpDataDB.getId());

            Collections.sort(optionsButtons, (o1, o2) -> o1.getSo().compareTo(o2.getSo()));

            buttonSave.setOnClickListener(b -> {
                Toast.makeText(mContext, "Данный раздел находится в разработке", Toast.LENGTH_LONG).show();
            });
            buttonMakeAReport.setOnClickListener(b -> {
                new Options().conduct(getContext(), wpDataDB, optionsButtons, 3);

                if (wpDataDB.getSetStatus() == 1){
                    check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_question_circle_regular));
                    check.setColorFilter(mContext.getResources().getColor(R.color.colorInetYellow));

//                    sendWpData2();  // Выгрузка статуса
                    Exchange exchange = new Exchange();
                    exchange.sendWpDataToServer(new Click() {
                        @Override
                        public <T> void onSuccess(T data) {
                            String msg = (String) data;
                            Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
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

            if (optionsButtons != null && optionsButtons.size() > 0){
                recycleViewDRAdapter = new RecycleViewDRAdapter(mContext, wpDataDB, optionsButtons, list);
                rvContacts.setAdapter(recycleViewDRAdapter);
                rvContacts.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            }else {
                // TODO Написать текст или в Обьект или в Ресурсы
                String msg = "По данному посещению не обнаружено ни одной опции. Попробуйте перезайти в отчёт или нажать на кнопку 'Загрузить'. При этом у Вас должен быть включён интернет и обеспеченна связь с сервером.";
                rvContacts.setVisibility(View.GONE);
                download.setVisibility(View.VISIBLE);
                information.setVisibility(View.VISIBLE);

                information.setText(msg);
                download.setOnClickListener(this::clickDownload);
            }

        }catch (Exception e){
            Log.e("R_TRANSLATES", "convertedObjectERROR: " + e);
            e.printStackTrace();
        }

        return v;
    }


    private void clickDownload(View view){
        Toast.makeText(view.getContext(), "Начинаю загрузку Опций", Toast.LENGTH_SHORT).show();

        TablesLoadingUnloading tlu = new TablesLoadingUnloading();
        tlu.downloadOptionsByDAD2(wpDataDB.getCode_dad2(), new Clicks.click() {
            @Override
            public <T> void click(T data) {
                String msg = (String) data;
                Toast.makeText(view.getContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
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
