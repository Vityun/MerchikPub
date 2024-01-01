package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;

public class TARSecondFrag extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public TasksAndReclamationsSDB data;
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

    // Pika установка ссылки на экземпляр этого класса в переменной внутри класса Tab1Fragment, чтоб можно было симитировать оттуда клик
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Tab1Fragment.secFrag = this;
    }

    // Pika сброс этого экземпляра класса в переменной внутри класса Tab1Fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Tab1Fragment.secFrag = null;
    }

    TARTab adapter;
    private void setTab(Context context) {
        tabLayout.getTabAt(0).setText("Главная");
        tabLayout.getTabAt(1).setText("Опции");
        tabLayout.getTabAt(3).setText("Переписка");
        tabLayout.getTabAt(2).setText("Товары");


        adapter = new TARTab(context, fragmentManager, getLifecycle(), tabLayout.getTabCount(), data);
        viewPager.setAdapter(adapter);
//        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
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

    public void setPhotoComment(Integer id, int tarCommentIndex){
        Globals.writeToMLOG("INFO", "TARSecondFrag.setPhotoComment", "Photo ID: " + id + " tarCommentIndex: " + tarCommentIndex);
        adapter.setDataToFrag3(id, tarCommentIndex);
    }

    // Pika выполнение клика на комменте чтоб перейти в комментарии
    public void clickOn3(){
        tabLayout.selectTab(tabLayout.getTabAt(3));
        tabLayout.performClick();
    }


}
