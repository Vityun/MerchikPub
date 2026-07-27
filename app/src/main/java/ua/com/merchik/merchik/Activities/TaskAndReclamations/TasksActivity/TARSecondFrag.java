package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARViewModel;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;

public class TARSecondFrag extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public TasksAndReclamationsSDB data;
    private FragmentManager fragmentManager;
    private TARViewModel viewModel;

    public static int TaRID = 0;
    private static final String ARG_TAR_ID = "tar_id";

    public TARSecondFrag() {
    }

    @Deprecated
    public TARSecondFrag(FragmentManager fragmentManager, TasksAndReclamationsSDB tar) {
        this.fragmentManager = fragmentManager;
        this.data = tar;
    }

    public static TARSecondFrag newInstance(TasksAndReclamationsSDB tar) {
        TARSecondFrag fragment = new TARSecondFrag();
        fragment.data = tar;

        Bundle args = new Bundle();
        if (tar != null && tar.id != null) {
            args.putInt(ARG_TAR_ID, tar.id);
        }
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TARViewModel.class);
        data = resolveTarData();

        if (data != null) {
            viewModel.setTasksAndReclamations(data);
            TaRID = data.id != null ? data.id : 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tar_tabs, container, false);

        tabLayout = v.findViewById(R.id.tabLayout);
        viewPager = v.findViewById(R.id.viewPagerChild);

        data = resolveTarData();
        if (data != null) {
            setTab(v.getContext());
        } else if (viewModel != null) {
            viewModel.getTasksAndReclamations().observe(getViewLifecycleOwner(), tar -> {
                if (tar != null && data == null) {
                    data = tar;
                    setTab(v.getContext());
                }
            });
        }

        if (fragmentManager != null) {
            Log.e("TARSecondFrag_T", "fragmentManager.getFragments(): " + fragmentManager.getFragments());
        }

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
        if (adapter != null || data == null) {
            return;
        }

        tabLayout.getTabAt(0).setText(getString(R.string.title_tab_tar_0));
        tabLayout.getTabAt(1).setText(getString(R.string.title_tab_tar_1));
        tabLayout.getTabAt(3).setText(getString(R.string.title_tab_tar_2));
        tabLayout.getTabAt(2).setText(getString(R.string.title_tab_tar_3));


        FragmentManager manager = fragmentManager != null ? fragmentManager : getChildFragmentManager();
        adapter = new TARTab(context, manager, getLifecycle(), tabLayout.getTabCount(), data);
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
        if (adapter != null) {
            adapter.setDataToFrag3(id);
        }
    }

    public void setPhotoComment(Integer id, int tarCommentIndex){
        Globals.writeToMLOG("INFO", "TARSecondFrag.setPhotoComment", "Photo ID: " + id + " tarCommentIndex: " + tarCommentIndex);
        if (adapter != null) {
            adapter.setDataToFrag3(id, tarCommentIndex);
        }
    }

    // Pika выполнение клика на комменте чтоб перейти в комментарии
    public void clickOn3(){
        tabLayout.selectTab(tabLayout.getTabAt(3));
        tabLayout.performClick();
    }

    private TasksAndReclamationsSDB resolveTarData() {
        if (data != null) {
            return data;
        }

        try {
            Bundle args = getArguments();
            if (args != null && args.containsKey(ARG_TAR_ID)) {
                int tarId = args.getInt(ARG_TAR_ID, 0);
                if (tarId > 0) {
                    TasksAndReclamationsSDB tar = SQL_DB.tarDao().getById(tarId);
                    if (tar != null) {
                        return tar;
                    }
                }
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "TARSecondFrag/resolveTarData", "Exception e: " + e);
        }

        return viewModel != null ? viewModel.getTasksAndReclamations().getValue() : null;
    }


}
