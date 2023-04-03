package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.DEFAULT;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.OptionsServer;
import ua.com.merchik.merchik.data.RetrofitResponse.ReportPrepareServer;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.PPADBRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

@SuppressLint("ValidFragment")
public class DetailedReportTovarsFrag extends Fragment {

    private Context mContext;
    private ArrayList<Data> list;
    private WpDataDB wpDataDB;
    private TasksAndReclamationsSDB tasksAndReclamationsSDB;

    private long codeDad2;
    private String clientId;

    private EditText editText;
    private TextView allTov;
    private RecyclerView rvTovar;
    private ImageView fullTovList, filter;

    private boolean flag = true;

    public DetailedReportTovarsFrag(Context context, ArrayList<Data> list, WpDataDB wpDataDB) {
        // Required empty public constructor
        this.mContext = context;
        this.list = list;
        this.wpDataDB = wpDataDB;

        this.codeDad2 = wpDataDB.getCode_dad2();
        this.clientId = wpDataDB.getClient_id();
    }

    public DetailedReportTovarsFrag(Context context, TasksAndReclamationsSDB tasksAndReclamationsSDB) {
        this.mContext = context;
        this.tasksAndReclamationsSDB = tasksAndReclamationsSDB;
        this.codeDad2 = tasksAndReclamationsSDB.codeDad2SrcDoc;
        this.clientId = tasksAndReclamationsSDB.client;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dr_tovar, container, false);

        try {
            editText = (EditText) v.findViewById(R.id.drEditTextFindTovar);
            fullTovList = v.findViewById(R.id.full_tov_list);
            filter = v.findViewById(R.id.filter);

            rvTovar = (RecyclerView) v.findViewById(R.id.DRRecyclerViewTovar);
            allTov = v.findViewById(R.id.textLikeLink);

//            setTextLikeLink();
            setPopup();
            addRecycleView(getTovList());
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/onCreateView", "Exception e: " + e);
        }
        return v;
    }


    /**
     * 22.01.2021
     */
    private void setTextLikeLink() {
        filter.setOnClickListener(v -> {
            // TODO ВСТАВИТЬ ДИАЛОГ С ФИЛЬТРОМ
        });
        Log.e("setTextLikeLink", "Here");

        fullTovList.setRotation(45);
        fullTovList.setOnClickListener((v) -> {
            try {
                if (!flag) {
                    Log.e("setTextLikeLink", "!flag");
                    fullTovList.setRotation(45);
                    fullTovList.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.ic_letter_x));
                    DetailedReportActivity.additionalRequirementsFilter = true;
                } else {
                    Log.e("setTextLikeLink", "flag");
                    fullTovList.setRotation(0);
                    fullTovList.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.ic_minus));
                    DetailedReportActivity.additionalRequirementsFilter = false;
                }
                flag = !flag;
                addRecycleView(getTovList());
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "", "Exception e: " + e);
            }
        });
    }


    /**
     * 03.04.23.
     * Делаю так что-б выпадал список, при клике на плюсик.
     */
    private void setPopup() {
        fullTovList.setRotation(45);
        fullTovList.setOnClickListener(view -> {
            showPopup(view);
        });
    }

    /**
     * 03.04.23.
     * Отображаю попап
     * @param view
     */
    private void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.popup_dr_tovar_list);

        popupMenu.setOnMenuItemClickListener(item -> {
            try {

            }catch (Exception e){
                Toast.makeText(getContext(), "Произошла ошибка: " + e, Toast.LENGTH_LONG).show();
            }
            List<TovarDB> tovarDBList = new ArrayList<>();
            switch (item.getItemId()) {
                case R.id.popup_dr:
                    tovarDBList = getTovListNew(TovarDisplayType.DETAILED_REPORT);
                    Toast.makeText(getContext(), "Показываю Товары как было.(" + tovarDBList.size() + ")", Toast.LENGTH_SHORT).show();
                    addRecycleView(tovarDBList);
                    return true;

                case R.id.popup_ppa:
                    tovarDBList = getTovListNew(TovarDisplayType.PPA);
                    Toast.makeText(getContext(), "Показываю Товары по ППА.(" + tovarDBList.size() + ")", Toast.LENGTH_SHORT).show();
                    addRecycleView(tovarDBList);
                    return true;

                case R.id.popup_all:
                    tovarDBList = getTovListNew(TovarDisplayType.ALL);
                    Toast.makeText(getContext(), "Показываю все Товары.(" + tovarDBList.size() + ")", Toast.LENGTH_SHORT).show();
                    addRecycleView(tovarDBList);
                    return true;

                case R.id.popup_tov:
                    Toast.makeText(getContext(), "Показываю один Товар. (В РАЗРАБОТКЕ!)", Toast.LENGTH_SHORT).show();
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private enum TovarDisplayType{
        DETAILED_REPORT,
        PPA,
        ALL,
        ONE
    }

    /**
     * 03.04.23.
     * По новой формирую Товары
     * */
    private List<TovarDB> getTovListNew(TovarDisplayType type){
        List<TovarDB> res = new ArrayList<>();
        switch (type){
            case DETAILED_REPORT:
                res = RealmManager.getTovarListFromReportPrepareByDad2(codeDad2);
                return res;

            case PPA:
                res = PPADBRealm.getTovarListByPPA(wpDataDB.getClient_id(), null, null);
                return res;

            case ONE:
                // На данный момент ничего не делаю
                return null;

            case ALL:
            default:
                res = RealmManager.getTovarListByCustomer(clientId);
                return res;
        }

    }


    /**
     * 22.01.2021
     * Устанавливаем адаптер
     */
    private void addRecycleView(List<TovarDB> list) {
        try {
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/List<TovarDB> list", "list: " + list);
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/List<TovarDB>", "list.size(): " + list.size());

            Log.e("DetailedReportTovarsF", "list.size(): " + list.size());


            Log.e("АКЦИЯ_ТОВАРА", "START");
            List<Integer> promotionalTov = new ArrayList<>();
            try {
                // TODO OH SHIT
                List<AdditionalRequirementsDB> data;
                if (wpDataDB != null) {
                    data = AdditionalRequirementsRealm.getData3(wpDataDB, DEFAULT);
                } else {
                    data = AdditionalRequirementsRealm.getData3(tasksAndReclamationsSDB, DEFAULT);
                }

                Log.e("АКЦИЯ_ТОВАРА", "data: " + data);
                for (AdditionalRequirementsDB item : data) {
                    if (item.getTovarId() != null && !item.getTovarId().equals("0") && !item.getTovarId().equals("")) {
                        promotionalTov.add(Integer.valueOf(item.getTovarId()));
                    }
                }
                Log.e("АКЦИЯ_ТОВАРА", "promotionalTov: " + promotionalTov);
            } catch (Exception e) {
                Log.e("АКЦИЯ_ТОВАРА", "List Exception e: " + e);
            }


            // TODO OH SHIT
            RecycleViewDRAdapterTovar recycleViewDRAdapter;
            if (wpDataDB != null) {
                recycleViewDRAdapter = new RecycleViewDRAdapterTovar(mContext, list, wpDataDB);
            } else {
                recycleViewDRAdapter = new RecycleViewDRAdapterTovar(mContext, list, tasksAndReclamationsSDB);
            }
            recycleViewDRAdapter.setAkciyaTovList(promotionalTov);

            recycleViewDRAdapter.setTplType(RecycleViewDRAdapterTovar.DRAdapterTovarTPLTypeView.GONE);

            RecycleViewDRAdapterTovar finalRecycleViewDRAdapter = recycleViewDRAdapter;
            recycleViewDRAdapter.refreshAdapter(() -> {
                finalRecycleViewDRAdapter.switchTPLView();
                rvTovar.setAdapter(finalRecycleViewDRAdapter);
                rvTovar.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                finalRecycleViewDRAdapter.notifyDataSetChanged();
            });

            rvTovar.setAdapter(recycleViewDRAdapter);
            rvTovar.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() != 0) {
                        recycleViewDRAdapter.getFilter().filter(s);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/addRecycleView/List<TovarDB> list", "Exception e: " + e);
        }
    }


    /**
     * 22.01.2021
     * Данные для адаптера
     * <p>
     * Отображает список товаров по ППА или весь список товаров по данному клиенту
     */
    private boolean updateTov = true;

    private ArrayList<TovarDB> getTovList() {
        ArrayList<TovarDB> list = null;
        if (flag) {
            List<TovarDB> dataTovar = RealmManager.getTovarListFromReportPrepareByDad2(codeDad2);
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList", "codeDad2: " + codeDad2);
            if (dataTovar != null && dataTovar.size() > 0) {
                list = (ArrayList<TovarDB>) RealmManager.INSTANCE.copyFromRealm(dataTovar);
                Toast.makeText(mContext, "Отображен список только тех товаров, которые установлены в матрице ППА", Toast.LENGTH_SHORT).show();
                Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList", "list: " + list.size());
            } else {
                Toast.makeText(mContext, "Список товаров по ППА пуст.", Toast.LENGTH_SHORT).show();

                if (updateTov && codeDad2 != 0) {
                    Log.d("test", "tovars is empty");
                    Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList", "tovars is empty");
                    downloadDetailedReportTovarsData(new Clicks.clickStatusMsg() {
                        @Override
                        public void onSuccess(String data) {
                            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();
                            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList.onSuccess", "String data: " + data);
                            addRecycleView(getTovList());
                            updateTov = false;
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
                            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList.onSuccess", "String error: " + error);
                            updateTov = false;
                        }
                    });
                }

            }
        } else {
            List<TovarDB> dataTovar = RealmManager.getTovarListByCustomer(clientId);
            list = (ArrayList<TovarDB>) RealmManager.INSTANCE.copyFromRealm(dataTovar);
            Toast.makeText(mContext, "Отображен список всех товаров", Toast.LENGTH_SHORT).show();
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList", "dataTovar list: " + list.size());
        }
        return list;
    }

    private void downloadDetailedReportTovarsData(Clicks.clickStatusMsg click) {
        try {
            ProgressDialog pg = ProgressDialog.show(mContext, "Загрузка списка товаров", "Подождите окончания загрузки. Это может занять время.", true, true);
            downloadReportPrepareByDad2(pg, click);

            ProgressDialog pg2 = ProgressDialog.show(mContext, "Загрузка списка опций", "Подождите окончания загрузки. Это может занять время.", true, true);
            downloadOptionByDad2(pg2, click);
        } catch (Exception e) {

        }
    }

    private void downloadReportPrepareByDad2(ProgressDialog pg, Clicks.clickStatusMsg click) {
        StandartData standartData = new StandartData();
        standartData.mod = "report_prepare";
        standartData.act = "list_data";
        standartData.date_from = Clock.getDatePeriod(-30);
        standartData.date_to = Clock.getDatePeriod(1);
        standartData.code_dad2 = String.valueOf(codeDad2);

        Gson gson = new Gson();
        String json = gson.toJson(standartData);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<ReportPrepareServer> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_REPORT_PREPARE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<ReportPrepareServer>() {
            @Override
            public void onResponse(Call<ReportPrepareServer> call, Response<ReportPrepareServer> response) {
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().getState()) {
                                if (response.body().getList() != null && response.body().getList().size() > 0) {
                                    ReportPrepareRealm.setAll(response.body().getList());
                                    click.onSuccess("Товары успешно обновлены!");
                                } else {
                                    click.onFailure("Список Товаров пуст. Обратитесь к своему руководителю.");
                                }
                            } else {
                                if (response.body().getError() != null) {
                                    click.onFailure("Данные обновить не получилось. Это связанно с: " + response.body().getError());
                                } else {
                                    click.onFailure("Данные обновить не получилось. Ошибку обнаружить не получилось. Обратитесь к руководителю.");
                                }
                            }
                        } else {
                            click.onFailure("Запрашиваемых данных не обнаружено.");
                        }
                    } else {
                        click.onFailure("Произошла ошибка при обновлении списка товаров. Ошибка: " + response.code());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/downloadReportPrepareByDad2/onResponse", "Exception e: " + e);
                    click.onFailure("Произошла ошибка. Передайте её руководителю: " + e);
                }

                if (pg != null && pg.isShowing()) pg.dismiss();
            }

            @Override
            public void onFailure(Call<ReportPrepareServer> call, Throwable t) {
                if (pg != null && pg.isShowing()) pg.dismiss();
                Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/downloadReportPrepareByDad2/onFailure", "Throwable t: " + t);
                click.onFailure(t.toString());
            }
        });
    }

    private void downloadOptionByDad2(ProgressDialog pg, Clicks.clickStatusMsg click) {
        StandartData standartData = new StandartData();
        standartData.mod = "plan";
        standartData.act = "options_list";
        standartData.code_dad2 = String.valueOf(codeDad2);

        Gson gson = new Gson();
        String json = gson.toJson(standartData);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<OptionsServer> call = RetrofitBuilder.getRetrofitInterface().GET_OPTIONS(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<OptionsServer>() {
            @Override
            public void onResponse(Call<OptionsServer> call, Response<OptionsServer> response) {
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().getState()) {
                                if (response.body().getList() != null && response.body().getList().size() > 0) {
                                    RealmManager.saveDownloadedOptions(response.body().getList());
                                    click.onSuccess("Опции успешно обновлены!");
                                } else {
                                    click.onFailure("Список Опций пуст. Обратитесь к своему руководителю.");
                                }
                            } else {
                                if (response.body().getError() != null) {
                                    click.onFailure("Данные обновить не получилось. Это связанно с: " + response.body().getError());
                                } else {
                                    click.onFailure("Данные обновить не получилось. Ошибку обнаружить не получилось. Обратитесь к руководителю.");
                                }
                            }
                        } else {
                            click.onFailure("Запрашиваемых данных не обнаружено.");
                        }
                    } else {
                        click.onFailure("Произошла ошибка при обновлении списка опций. Ошибка: " + response.code());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/downloadOptionByDad2/onResponse", "Exception e: " + e);
                    click.onFailure("Произошла ошибка. Передайте её руководителю: " + e);
                }
                if (pg != null && pg.isShowing()) pg.dismiss();
            }

            @Override
            public void onFailure(Call<OptionsServer> call, Throwable t) {
                if (pg != null && pg.isShowing()) pg.dismiss();
                Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/downloadOptionByDad2/onFailure", "Throwable t: " + t);
                click.onFailure(t.toString());
            }
        });
    }

}
