package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.RecycleViewDRAdapter;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm;

public class Tab2Fragment extends Fragment {

    TasksAndReclamationsSDB data;

    RecyclerView recyclerView;
    Context mContext;

    public Tab2Fragment() {
    }

    public Tab2Fragment(TasksAndReclamationsSDB data) {
        this.data = data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_item_second, container, false);
        mContext = v.getContext();

        recyclerView = v.findViewById(R.id.recycler_view);

        setRecycler();

        return v;
    }


    /**
     * 22.03.2021
     * Установка Опций для Задач И Рекламаций (ЗИР) по коду ДАД2
     * */
    private void setRecycler(){
        List<OptionsDB> list = RealmManager.INSTANCE.copyFromRealm(OptionsRealm.getOptionsButtonByDAD2(String.valueOf(data.codeDad2)));

        Log.e("Tab2Fragment_L", "list: " + list.size());

        Collections.sort(list, (o1, o2) -> o1.getSo().compareTo(o2.getSo()));

        List<Integer> ids = new ArrayList<>();
        for (OptionsDB item : list){
            ids.add(Integer.parseInt(item.getOptionId()));
        }

        // Запрос к SQL БДшке. Получаем список обьектов сайта
        List<SiteObjectsSDB> listTr = SQL_DB.siteObjectsDao().getObjectsById(ids);

//        Long dad2Wp = data.codeDad2SrcDoc;
////        WpDataDB wp = RealmManager.INSTANCE.copyFromRealm(WpDataRealm.getWpDataRowByDad2Id(dad2Wp));
//        WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(dad2Wp);
//        if (wp!=null){
//            wp = RealmManager.INSTANCE.copyFromRealm(wp);
//        }

        RecycleViewDRAdapter recycleViewDRAdapter = new RecycleViewDRAdapter(mContext, data, list, listTr, ()->{});
        recyclerView.setAdapter(recycleViewDRAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }
}
