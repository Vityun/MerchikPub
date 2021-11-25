package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

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
import java.util.List;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.RecycleViewDRAdapter;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

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

//        if (TARActivity.TARType == 0){
//            Log.e("SET_TAR_FAB", "TYTA");
//            toolbar_menus.textLesson = 0;
//            toolbar_menus.videoLesson = 0;
//        }else {
//            Log.e("SET_TAR_FAB", "ZDESA");
//            toolbar_menus.textLesson = 0;
//            toolbar_menus.videoLesson = 0;
//        }

//        toolbar_menus.setFab(v.getContext(), TARActivity.fab); // ОПЦИИ

        setRecycler();

        return v;
    }


    /**
     * 22.03.2021
     * Установка Опций для Задач И Рекламаций (ЗИР) по коду ДАД2
     * */
    private void setRecycler(){
        List<OptionsDB> list = OptionsRealm.getOptionsByDAD2(String.valueOf(data.codeDad2));

        Log.e("Tab2Fragment_L", "list: " + list.size());


//        Collections.sort(list, (o1, o2) -> o1.getSo().compareTo(o2.getSo()));

        List<Integer> ids = new ArrayList<>();
        for (OptionsDB item : list){
            ids.add(Integer.parseInt(item.getOptionId()));
        }

        // Запрос к SQL БДшке. Получаем список обьектов сайта
        List<SiteObjectsSDB> listTr = SQL_DB.siteObjectsDao().getObjectsById(ids);


        RecycleViewDRAdapter recycleViewDRAdapter = new RecycleViewDRAdapter(mContext, null, list, listTr);
        recyclerView.setAdapter(recycleViewDRAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }
}
