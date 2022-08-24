package ua.com.merchik.merchik.Activities.DetailedReportActivity;

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
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
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

    public DetailedReportTovarsFrag() {
    }

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

            setTextLikeLink();
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
                } else {
                    Log.e("setTextLikeLink", "flag");
                    fullTovList.setRotation(0);
                    fullTovList.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.ic_minus));
                }
                flag = !flag;
                addRecycleView(getTovList());
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "", "Exception e: " + e);
            }
        });
    }


    /**
     * 22.01.2021
     * Устанавливаем адаптер
     */
    private void addRecycleView(List<TovarDB> list) {

        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/List<TovarDB>", "list.size(): " + list.size());

        Log.e("DetailedReportTovarsF", "list.size(): " + list.size());


        Log.e("АКЦИЯ_ТОВАРА", "START");
        List<Integer> promotionalTov = new ArrayList<>();
        try {
            // TODO OH SHIT
            List<AdditionalRequirementsDB> data;
            if (wpDataDB != null) {
                data = AdditionalRequirementsRealm.getData3(wpDataDB);
            } else {
                data = AdditionalRequirementsRealm.getData3(tasksAndReclamationsSDB);
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
    }


    /**
     * 22.01.2021
     * Данные для адаптера
     * <p>
     * Отображает список товаров по ППА или весь список товаров по данному клиенту
     */
    private ArrayList<TovarDB> getTovList() {
        ArrayList<TovarDB> list = null;
        if (flag) {
            List<TovarDB> dataTovar = RealmManager.getTovarListFromReportPrepareByDad2(codeDad2);
            if (dataTovar != null && dataTovar.size() > 0) {
                list = (ArrayList<TovarDB>) RealmManager.INSTANCE.copyFromRealm(dataTovar);
                Toast.makeText(mContext, "Отображен список только тех товаров, которые установлены в матрице ППА", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Список товаров по ППА пуст.", Toast.LENGTH_SHORT).show();
                downloadDetailedReportTovarsData(codeDad2, new Clicks.clickStatusMsg() {
                    @Override
                    public void onSuccess(String data) {
                        Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            List<TovarDB> dataTovar = RealmManager.getTovarListByCustomer(clientId);
            list = (ArrayList<TovarDB>) RealmManager.INSTANCE.copyFromRealm(dataTovar);
            Toast.makeText(mContext, "Отображен список всех товаров", Toast.LENGTH_SHORT).show();
        }
        return list;
    }

    private void downloadDetailedReportTovarsData(long codeDad2, Clicks.clickStatusMsg click) {
        ProgressDialog pg = ProgressDialog.show(mContext, "Загрузка списка товаров", "Подождите окончания загрузки. Это может занять время.", true, true);

        downloadReportPrepareByDad2(pg, click);
        downloadOptionByDad2(pg, click);
    }

    private void downloadReportPrepareByDad2(ProgressDialog pg, Clicks.clickStatusMsg click){
        StandartData standartData = new StandartData();
        standartData.mod = "report_prepare";
        standartData.act = "list_data";
        standartData.date_from = "2022-08-01";
        standartData.date_to = "2022-08-11";
        standartData.code_dad2 = String.valueOf(codeDad2);

        Gson gson = new Gson();
        String json = gson.toJson(standartData);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    if (response.body() != null){
                        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/downloadReportPrepareByDad2/onResponse", "response.body(): " + response.body());
                        click.onSuccess("Товары успешно обновлены!");
                    }else {
                        click.onFailure("Запрашиваемых данных не обнаружено.");
                    }
                }else {
                    click.onFailure("Произошла ошибка при обновлении списка товаров. Ошибка: " + response.code());
                }


                if (pg != null && pg.isShowing()) pg.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (pg != null && pg.isShowing()) pg.dismiss();
                Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/downloadReportPrepareByDad2/onFailure", "Throwable t: " + t);
                click.onFailure(t.toString());
            }
        });
    }

    private void downloadOptionByDad2(ProgressDialog pg, Clicks.clickStatusMsg click){
        StandartData standartData = new StandartData();
        standartData.mod = "plan";
        standartData.act = "options_list";
        standartData.code_dad2 = String.valueOf(codeDad2);

        Gson gson = new Gson();
        String json = gson.toJson(standartData);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    if (response.body() != null){
                        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/downloadOptionByDad2/onResponse", "response.body(): " + response.body());
                        click.onSuccess("Опции товаров успешно обновлены!");
                    }else {
                        click.onFailure("Запрашиваемых данных не обнаружено.");
                    }
                }else {
                    click.onFailure("Произошла ошибка при обновлении опций товаров. Ошибка: " + response.code());
                }


                if (pg != null && pg.isShowing()) pg.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (pg != null && pg.isShowing()) pg.dismiss();
                Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/downloadOptionByDad2/onFailure", "Throwable t: " + t);
                click.onFailure(t.toString());
            }
        });
    }

}
