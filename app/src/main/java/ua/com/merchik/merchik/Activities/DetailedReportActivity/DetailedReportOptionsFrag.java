package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery.MakePhotoFromGaleryWpDataDB;
import static ua.com.merchik.merchik.Options.Options.ConductMode.DEFAULT_CONDUCT;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;

@SuppressLint("ValidFragment")
public class DetailedReportOptionsFrag extends Fragment {

    private Context mContext;
    private ArrayList<Data> list;
    private WpDataDB wpDataDB;

    RecycleViewDRAdapter recycleViewDRAdapter;

    public DetailedReportOptionsFrag(Context context, ArrayList<Data> list, WpDataDB wpDataDB) {
        // Required empty public constructor
        this.mContext = context;
        this.list = list;
        this.wpDataDB = wpDataDB;
    }

    @Override
    public void onResume() {
        try {
            recycleViewDRAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("test", "test: " + e);
            /*    java.lang.NullPointerException: Attempt to invoke virtual method 'void ua.com.merchik.merchik.Activities.DetailedReportActivity.RecycleViewDRAdapter.notifyDataSetChanged()' on a null object reference
        at ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportOptionsFrag.onResume(DetailedReportOptionsFrag.java:64)*/
        }
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dr_option, container, false);
        try {
            Button buttonSave = (Button) v.findViewById(R.id.button);
            Button buttonMakeAReport = (Button) v.findViewById(R.id.button3);

            Button download = v.findViewById(R.id.download);
            TextView information = v.findViewById(R.id.info_msg);
            TextView planfact = v.findViewById(R.id.planfact);

            // todo это надо будет вынести в отдельную функцию. И скорее всего перересовывать при клике на "провести"
            StringBuilder sb = new StringBuilder();
            sb.append("Прем.(план): ").append(wpDataDB.getCash_ispolnitel()).append("\n\n");
            sb.append("Прем.(факт): ").append(wpDataDB.cash_fact).append("\n");
            planfact.setText(sb);

            ImageView check = v.findViewById(R.id.check);
            if (wpDataDB.getSetStatus() == 1) {
                check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_question_circle_regular));
                check.setColorFilter(mContext.getResources().getColor(R.color.colorInetYellow));
            } else {
                if (wpDataDB.getStatus() == 1) {
                    check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_check));
                    check.setColorFilter(mContext.getResources().getColor(R.color.greenCol));
                } else {
                    if (Clock.dateConvertToLong(Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000)) < System.currentTimeMillis()) { //+TODO CHANGE DATE
                        check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_exclamation_mark_in_a_circle));
                        check.setColorFilter(mContext.getResources().getColor(R.color.red_error));
                    } else {
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
                try {
                    List<OptionsDB> opt = workPlan.getOptionButtons2(workPlan.getWpOpchetId(wpDataDB), wpDataDB.getId());
                    WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(wpDataDB.getCode_dad2());

                    new Options().conduct(getContext(), wp, opt, DEFAULT_CONDUCT, new Clicks.click() {
                        @Override
                        public <T> void click(T data) {
                            OptionsDB optionsDB = (OptionsDB) data;
                            int scrollPosition = recycleViewDRAdapter.getItemPosition(optionsDB);
                            OptionMassageType msgType = new OptionMassageType();
                            msgType.type = OptionMassageType.Type.DIALOG;
                            new Options().optControl(getContext(), wp, optionsDB, Integer.parseInt(optionsDB.getOptionControlId()), null, msgType, Options.NNKMode.CHECK);
                            rvContacts.smoothScrollToPosition(scrollPosition);
                        }
                    });

                    if (wpDataDB.getSetStatus() == 1) {
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
                }catch (Exception e){
                    Globals.writeToMLOG("ERROR", "DetailedReportOptionsFrag/buttonMakeAReport/setOnClickListener", "Exception e: " + e);
                }
            });


            Log.e("R_TRANSLATES", "convertedObject: START");

            List<Integer> ids = new ArrayList<>();
            for (OptionsDB item : optionsButtons) {
                ids.add(Integer.parseInt(item.getOptionId()));
            }

            Log.e("R_TRANSLATES", "item: " + ids.size());

            for (Integer item : ids) {
                Log.e("R_TRANSLATES", "Integeritem: " + item);
            }

            // Запрос к SQL БДшке. Получаем список обьектов сайта
            List<SiteObjectsSDB> list = SQL_DB.siteObjectsDao().getObjectsById(ids);

            // Получаю все опции по данному отчёту.
            List<OptionsDB> allReportOption = RealmManager.INSTANCE.copyFromRealm(OptionsRealm.getOptionsByDAD2(String.valueOf(wpDataDB.getCode_dad2())));

            Log.e("R_TRANSLATES", "item: " + list.size());

            for (SiteObjectsSDB item : list) {
                Log.e("R_TRANSLATES", "SiteObjectsSDBitem: " + item.id);
            }

            Log.e("TEST_OPTIONS", "optionsButtons SIZE: " + optionsButtons.size());
            for (OptionsDB item : optionsButtons) {
                options.optionControl(mContext, wpDataDB, item, null, Options.NNKMode.NULL);
            }

            if (optionsButtons != null && optionsButtons.size() > 0) {
                recycleViewDRAdapter = new RecycleViewDRAdapter(mContext, wpDataDB, optionsButtons, allReportOption, list, ()->{
                    MakePhotoFromGaleryWpDataDB = wpDataDB;
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    ((DetailedReportActivity) mContext).startActivityForResult(Intent.createChooser(intent, "Select Picture"), 500);
                });
                rvContacts.setAdapter(recycleViewDRAdapter);
                rvContacts.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            } else {
                clickDownload(mContext);

                // TODO Написать текст или в Обьект или в Ресурсы
                String msg = "По данному посещению не обнаружено ни одной опции. Попробуйте перезайти в отчёт или нажать на кнопку 'Загрузить'. При этом у Вас должен быть включён интернет и обеспеченна связь с сервером.";
                rvContacts.setVisibility(View.GONE);
                download.setVisibility(View.VISIBLE);
                information.setVisibility(View.VISIBLE);

                information.setText(msg);
                download.setOnClickListener(v1 -> clickDownload(v1.getContext()));
            }

        } catch (Exception e) {
            Log.e("R_TRANSLATES", "convertedObjectERROR: " + e);
            e.printStackTrace();
        }

        return v;
    }


    private void clickDownload(Context context) {
        Toast.makeText(context, "Начинаю загрузку Опций", Toast.LENGTH_SHORT).show();

        TablesLoadingUnloading tlu = new TablesLoadingUnloading();
        tlu.downloadOptionsByDAD2(wpDataDB.getCode_dad2(), new Clicks.click() {
            @Override
            public <T> void click(T data) {
                String msg = (String) data;
                Globals.writeToMLOG("INFO", "downloadOptionsByDAD2/clickDownload/clickRESULT", "msg: " + msg);
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
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
