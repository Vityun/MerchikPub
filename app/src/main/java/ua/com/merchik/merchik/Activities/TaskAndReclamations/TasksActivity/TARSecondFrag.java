package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;

public class TARSecondFrag extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private TasksAndReclamationsSDB data;
    private FragmentManager fragmentManager;

    public static int TaRID = 0;


    public TARSecondFrag(FragmentManager fragmentManager, TasksAndReclamationsSDB tar) {
        this.fragmentManager = fragmentManager;
        this.data = tar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tar_tabs, container, false);

        tabLayout = v.findViewById(R.id.tabLayout);
        viewPager = v.findViewById(R.id.viewPagerChild);

        setTab(v.getContext());

        Log.e("TARSecondFrag_T", "fragmentManager.getFragments(): " + fragmentManager.getFragments());

        return v;
    }



    TARTab adapter;
    private void setTab(Context context) {
        tabLayout.getTabAt(0).setText("Главная");
        tabLayout.getTabAt(1).setText("Опции");
        tabLayout.getTabAt(3).setText("Переписка");
        tabLayout.getTabAt(2).setText("Товары");


        adapter = new TARTab(context, fragmentManager, tabLayout.getTabCount(), data);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void setPhoto(Integer id){
        Globals.writeToMLOG("INFO", "TARSecondFrag.setPhoto", "Photo ID: " + id);
        adapter.setDataToFrag3(id);
    }



}
