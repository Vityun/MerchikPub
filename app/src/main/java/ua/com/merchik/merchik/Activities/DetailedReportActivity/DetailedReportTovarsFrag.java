package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;

@SuppressLint("ValidFragment")
public class DetailedReportTovarsFrag extends Fragment {

    private Context mContext;
    private ArrayList<Data> list;
    private WpDataDB wpDataDB;

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
        }catch (Exception e){

        }


        return v;
    }


    /** 22.01.2021
     *
     * */
    private void setTextLikeLink(){
        filter.setOnClickListener(v->{
            // TODO ВСТАВИТЬ ДИАЛОГ С ФИЛЬТРОМ
        });
        Log.e("setTextLikeLink", "Here");

        fullTovList.setRotation(45);
        fullTovList.setOnClickListener((v) -> {
            try {
                if (!flag){
                    Log.e("setTextLikeLink", "!flag");
                    fullTovList.setRotation(45);
                    fullTovList.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.ic_letter_x));
                }else {
                    Log.e("setTextLikeLink", "flag");
                    fullTovList.setRotation(0);
                    fullTovList.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.ic_minus));

/*                String msg = "Внимание! Данные, которые вы сейчас добавите, при " +
                        "следущей синхронизации затрут данные хранящиеся на сервере.";

                DialogData dialog = new DialogData(getContext());
                dialog.setTitle("Внимание!");
                dialog.setText(msg);
                dialog.setDialogIco();
                dialog.setClose(dialog::dismiss);
                dialog.show();*/
                }
                flag = !flag;
                addRecycleView(getTovList());
            }catch (Exception e){
                Globals.writeToMLOG("ERROR", "", "Exception e: " + e);
            }
        });
    }


    /** 22.01.2021
     * Устанавливаем адаптер
     * */
    private void addRecycleView(List<TovarDB> list){

        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/List<TovarDB>", "list.size(): " + list.size());

        Log.e("DetailedReportTovarsF", "list.size(): " + list.size());


        Log.e("АКЦИЯ_ТОВАРА", "START");
        List<Integer> promotionalTov = new ArrayList<>();
        try {
            List<AdditionalRequirementsDB> data = AdditionalRequirementsRealm.getData3(wpDataDB);
            Log.e("АКЦИЯ_ТОВАРА", "data: " + data);
            for (AdditionalRequirementsDB item : data){
                if (item.getTovarId() != null && !item.getTovarId().equals("0") && !item.getTovarId().equals("")){
                    promotionalTov.add(Integer.valueOf(item.getTovarId()));
                }
            }
            Log.e("АКЦИЯ_ТОВАРА", "promotionalTov: " + promotionalTov);
        }catch (Exception e){
            Log.e("АКЦИЯ_ТОВАРА", "List Exception e: " + e);
        }


        RecycleViewDRAdapterTovar recycleViewDRAdapter = new RecycleViewDRAdapterTovar(mContext, list, wpDataDB);
        recycleViewDRAdapter.setAkciyaTovList(promotionalTov);

        recycleViewDRAdapter.setTplType(RecycleViewDRAdapterTovar.DRAdapterTovarTPLTypeView.GONE);

        recycleViewDRAdapter.refreshAdapter(()->{
            recycleViewDRAdapter.switchTPLView();
            rvTovar.setAdapter(recycleViewDRAdapter);
            rvTovar.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            recycleViewDRAdapter.notifyDataSetChanged();
        });

        rvTovar.setAdapter(recycleViewDRAdapter);
        rvTovar.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() != 0){
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


    /** 22.01.2021
     * Данные для адаптера
     *
     * Отображает список товаров по ППА или весь список товаров по данному клиенту
     * */
    private ArrayList<TovarDB> getTovList(){
        ArrayList<TovarDB> list;
        if (flag){
            List<TovarDB> dataTovar = RealmManager.getTovarListFromReportPrepareByDad2(wpDataDB.getCode_dad2());
            list = (ArrayList<TovarDB>) RealmManager.INSTANCE.copyFromRealm(dataTovar);
            Log.e("TOVAR_TEST_SIZE", "TOV_SIZE: " + list.size());
            Toast.makeText(mContext, "Отображен список только тех товаров, которые установлены в матрице ППА", Toast.LENGTH_SHORT).show();
        }else {
            Log.e("TOVAR_TEST_SIZE", "wpDataDB.getClient_id(): " + wpDataDB.getClient_id());
            List<TovarDB> dataTovar = RealmManager.getTovarListByCustomer(wpDataDB.getClient_id());
            list = (ArrayList<TovarDB>) RealmManager.INSTANCE.copyFromRealm(dataTovar);
            Log.e("TOVAR_TEST_SIZE", "ALL_TOV_SIZE: " + list.size());
            Toast.makeText(mContext, "Отображен список всех товаров", Toast.LENGTH_SHORT).show();
        }
        return list;
    }

}
