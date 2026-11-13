package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARViewModel;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;

public class TARSecondFrag extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabLayoutMediator tabMediator;

    public TasksAndReclamationsSDB data;
    private FragmentManager fragmentManager;
    private TARViewModel viewModel;

    public static int TaRID = 0;
    private static final String ARG_TAR_ID = "tar_id";
    private static final int[] TAB_TITLES = {
            R.string.title_tab_tar_0,
            R.string.title_tab_tar_1,
            R.string.title_tab_tar_3,
            R.string.title_tab_tar_2
    };

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

    public static TARSecondFrag newInstanceById(int tarId) {
        TARSecondFrag fragment = new TARSecondFrag();
        Bundle args = new Bundle();
        args.putInt(ARG_TAR_ID, tarId);
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
        return v;
    }

    private void bindHeader(View view, TasksAndReclamationsSDB tar) {
        TextView title = view.findViewById(R.id.tar_header_title);
        TextView number = view.findViewById(R.id.tar_header_number);
        title.setText(tar.tp != null && tar.tp == 1
                ? R.string.title_task_single : R.string.title_reclamation_single);

        StringBuilder identifier = new StringBuilder();
        if (tar.id != null) {
            identifier.append(tar.id);
        }
        if (tar.id1c != null && !tar.id1c.trim().isEmpty()) {
            if (identifier.length() > 0) identifier.append(" / ");
            identifier.append(tar.id1c.trim());
        }
        number.setText(identifier.length() > 0 ? "ID: " + identifier : "");
        number.setVisibility(identifier.length() > 0 ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.tar_header).setVisibility(View.VISIBLE);
    }

    // Pika установка ссылки на экземпляр этого класса в переменной внутри класса Tab1Fragment, чтоб можно было симитировать оттуда клик
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Tab1Fragment.secFrag = this;

        data = resolveTarData();
        if (data != null) {
            bindHeader(view, data);
            setTab(view.getContext());
        } else if (viewModel != null) {
            viewModel.getTasksAndReclamations().observe(getViewLifecycleOwner(), tar -> {
                if (tar != null && data == null) {
                    data = tar;
                    bindHeader(view, data);
                    setTab(view.getContext());
                }
            });
        }

        if (fragmentManager != null) {
            Log.e("TARSecondFrag_T", "fragmentManager.getFragments(): " + fragmentManager.getFragments());
        }
    }

    // Pika сброс этого экземпляра класса в переменной внутри класса Tab1Fragment
    @Override
    public void onDestroyView() {
        if (tabMediator != null) {
            tabMediator.detach();
            tabMediator = null;
        }
        if (viewPager != null) viewPager.setAdapter(null);
        adapter = null;
        tabLayout = null;
        viewPager = null;
        if (Tab1Fragment.secFrag == this) Tab1Fragment.secFrag = null;
        super.onDestroyView();
    }

    TARTab adapter;
    private void setTab(Context context) {
        if (adapter != null || data == null) {
            return;
        }

        FragmentManager manager = fragmentManager != null ? fragmentManager : getChildFragmentManager();
        adapter = new TARTab(context, manager, getViewLifecycleOwner().getLifecycle(), TAB_TITLES.length, data);
        viewPager.setAdapter(adapter);
        tabMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position]));
        tabMediator.attach();
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
        if (viewPager != null && adapter != null) viewPager.setCurrentItem(3);
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
