package ua.com.merchik.merchik.Activities.TaskAndReclamations;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARHomeFrag;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARSecondFrag;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.dialogs.DialodTAR.DialogCreateTAR;
import ua.com.merchik.merchik.toolbar_menus;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class TARActivity extends toolbar_menus implements TARFragmentHome.OnFragmentInteractionListener, TARHomeFrag.OnFragmentInteractionListener{

    private Globals globals = new Globals();


    private TARHomeFrag homeFrag;
    private TARSecondFrag secondFrag;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private FragmentManager fragmentManager;

//    private FragmentManager fragmentManager;
    public static TextView activity_title;

    public static int TARType;

    public static FloatingActionButton fab;

    public DialogCreateTAR dialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent();
    }


    /**
     * 07.06.2021
     * Устанавливаю контент для Задач или Рекламаций
     */
    private void setActivityContent() {
        setContentView(R.layout.drawler_tasks);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        setTabs();

//        activity_title = (TextView) findViewById(R.id.activity_title);

//        TARType = getIntent().getIntExtra("TAR_type", 1);
//        if (TARType == 1) {
//            activity_title.setText("Задачи");
//        } else if (TARType == 0) {
//            activity_title.setText("Рекламации");
//        }

//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //Убирает фокус с полей ввода

        try {
            fab = findViewById(R.id.fab);

            Log.e("SET_TAR_FAB", "CLICK");
            if (TARType == 1) {
                textLesson = 1179;
                videoLesson = 1180;
                setFab(this, fab);
                Log.e("SET_TAR_FAB", "task");
            } else if (TARType == 0) {
                textLesson = 1181;
                videoLesson = 1182;
                setFab(this, fab);
                Log.e("SET_TAR_FAB", "report");
            }


            findViewById(R.id.fabAdd).setVisibility(View.GONE);
            findViewById(R.id.fabAdd).setOnClickListener(v -> {
                Intent intent = new Intent(this, PhotoLogActivity.class);

                dialog = new DialogCreateTAR(this);
                dialog.setClose(dialog::dismiss);
                dialog.setTarType(TARType);
                dialog.setRecyclerView(new Clicks.clickVoid() {
                    @Override
                    public void click() {
                        intent.putExtra("choise", true);
                        intent.putExtra("resultCode", 100);
                        if (dialog.address != null) {
                            intent.putExtra("address", dialog.address.getAddrId());
                        }

                        if (dialog.customer != null) {
                            intent.putExtra("customer", dialog.customer.getId());
                        } else {
                            intent.putExtra("customer", "");
                        }

                        startActivityForResult(intent, 100);
                    }
                });
                dialog.clickSave(() -> {

                }, 1);
                dialog.show();
            });

            initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));

        } catch (Exception e) {
            globals.alertDialogMsg(this, "Ошибка: " + e);
        }

        // -----------------------------------------------------------------------------------------

//        setHomeFrag();
    }

    // =================================== --- onCreate --- ========================================
    @Override
    public void onBackPressed() {
        fragmentManager = getSupportFragmentManager();
        Log.e("TasksActivity_T", "onBackPressed. Количество фрагментов в момент нажатия назад: " + fragmentManager.getBackStackEntryCount());

        if (fragmentManager.getBackStackEntryCount() >= 1) {

            Log.e("TasksActivity_T", "Я Должен удалить предыдущий фрагмент и его контекст из памяти что б он не открылся по новой");

            Log.e("TasksActivity_T", "1. Фрагментов не 0: " + fragmentManager.getBackStackEntryCount());
            Log.e("TasksActivity_T", "1. fragmentManager.getFragments(): " + fragmentManager.getFragments());

            Log.e("TasksActivity_T", "Скока Вас: " + fragmentManager.getFragments().size());
            Log.e("TasksActivity_T", "Как ты выглядишь, демон?: " + fragmentManager.getFragments().get(0));

            for (int i = 1; i < fragmentManager.getFragments().size(); i++) {
                Log.e("TasksActivity_T", "УДОЛЯЮ: " + fragmentManager.getFragments().get(i));
                fragmentManager.beginTransaction()
                        .remove(fragmentManager.getFragments().get(i))
                        .commit();
                fragmentManager.popBackStack(fragmentManager.getFragments().get(i).getId(), 0);
            }

            fragmentManager.popBackStackImmediate();


            if (TARType == 1) {
                TARActivity.activity_title.setText("Задачи");
                textLesson = 1179;
                videoLesson = 1180;
                setFab(this, fab);
                Log.e("SET_TAR_FAB", "task");
            } else if (TARType == 0) {
                TARActivity.activity_title.setText("Рекламации");
                textLesson = 1181;
                videoLesson = 1182;
                setFab(this, fab);
                Log.e("SET_TAR_FAB", "report");
            }


            TARActivity.activity_title.setBackground(this.getResources().getDrawable(R.drawable.shadow_tab_item_active));

            Log.e("TasksActivity_T", "Скока Вас 2: " + fragmentManager.getFragments().size());


        } else {
//            startActivity(getIntent());

            Log.e("TasksActivity_T", "Ну вообще я тут должен просто назад вернуться. Например в предыдущую активность");

            Log.e("TasksActivity_T", "2. Фрагментов нет: " + fragmentManager.getBackStackEntryCount());
            Log.e("TasksActivity_T", "2. fragmentManager.getFragments(): " + fragmentManager.getFragments());
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            Log.d("test", "data: " + requestCode + resultCode);

            if (resultCode == 100) {
                int id = data.getIntExtra("stack_photo_id", 0);
                StackPhotoDB photoDB = StackPhotoRealm.getById(id);
                homeFrag.dialog.setData(photoDB.getAddr_id(), photoDB.getClient_id(), photoDB.getCode_dad2(), photoDB);
                homeFrag.dialog.setDataUpdate();
                homeFrag.dialog.refreshAdaper(photoDB);
            }

            if (resultCode == 101) {
                int id1 = TARFragmentHome.secondFragId;
                String tag = TARFragmentHome.secondFragTAG;

                List<Fragment> fragments = fragmentManager.getFragments();
                TARFragmentHome fragmentHome = (TARFragmentHome) fragments.get(0);

                int id = data.getIntExtra("stack_photo_id", 0);
                fragmentHome.secondFrag.setPhoto(id);
                Log.e("test", "test" + secondFrag);
            }

            if (requestCode == 200) {
                TasksAndReclamationsSDB tar = SQL_DB.tarDao().getById(TARSecondFrag.TaRID);
                StackPhotoDB stackPhotoDB = savePhoto(MakePhoto.openCameraPhotoUri, tar);
                MakePhoto.openCameraPhotoUri = null;
                secondFrag.setPhoto(Integer.valueOf(stackPhotoDB.getId()));
            }


        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "TARActivity.onActivityResult", "Exception e: " + e);
        }
    }


    private void setTabs(){

        String homeTabTitle = "Задачи";
        TARType = getIntent().getIntExtra("TAR_type", 1);
        if (TARType == 1) {
            homeTabTitle = "Задачи";
        } else if (TARType == 0) {
            homeTabTitle = "Рекламации";
        }

        tabLayout.getTabAt(0).setText(homeTabTitle);
        tabLayout.getTabAt(1).setText("Карта");

        fragmentManager = getSupportFragmentManager();
        TARHomeTab tabAdapter = new TARHomeTab(fragmentManager, tabLayout.getTabCount());
        viewPager.setAdapter(tabAdapter);
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


    private StackPhotoDB savePhoto(String str, TasksAndReclamationsSDB tar) {
        int id = RealmManager.stackPhotoGetLastId();
        id++;
        StackPhotoDB stackPhotoDB = new StackPhotoDB();
        stackPhotoDB.setId(id);
        stackPhotoDB.setDt(System.currentTimeMillis() / 1000);
        stackPhotoDB.setTime_event(Clock.getHumanTime2(System.currentTimeMillis() / 1000));

        stackPhotoDB.setAddr_id(tar.addr);
        stackPhotoDB.setClient_id(tar.client);
        stackPhotoDB.setUser_id(tar.vinovnik);
        stackPhotoDB.setPhoto_type(0);
        stackPhotoDB.setCode_dad2(tar.codeDad2);

        if (tar.themeId == 1174){
            stackPhotoDB.setDvi(1);
        }

        stackPhotoDB.setCreate_time(System.currentTimeMillis() / 1000);

        stackPhotoDB.setPhoto_hash(globals.getHashMD5FromFilePath(str, null));
        stackPhotoDB.setPhoto_num(str);
        RealmManager.stackPhotoSavePhoto(stackPhotoDB);
        return stackPhotoDB;
    }

    @Override
    public void messageFromParentFragment(String msg) {

    }

    @Override
    public void messageFromChildFragment(String msg) {

    }

    // =================================== --- @Override --- =======================================

//    private void setHomeFrag() {
//        fragmentManager = getSupportFragmentManager();
//
//        homeFrag = new TARHomeFrag().newInstance(TARType, new Globals.TARInterface() {
//            @Override
//            public void onSuccess(TasksAndReclamationsSDB data) {
//                // Открываю новый фрагмент
//                secondFrag = new TARSecondFrag(fragmentManager, data);
//                fragmentManager.beginTransaction()
//                        .addToBackStack(null)
//                        .hide(homeFrag)
//                        .add(R.id.fragment, secondFrag)
//                        .commit();
//
//                Log.e("TasksActivity_T", "3. Открыли новый фрагмент. кол-во: " + fragmentManager.getBackStackEntryCount());
//                Log.e("TasksActivity_T", "3. fragmentManager.getFragments(): " + fragmentManager.getFragments());
//            }
//
//            @Override
//            public void onFailure(String error) {
//            }
//        });
//
//        fragmentManager.beginTransaction()
//                .add(R.id.fragment, homeFrag)
//                .commit();
//
//        Log.e("TasksActivity_T", "4. Количество фрагментов: " + fragmentManager.getBackStackEntryCount());
//        Log.e("TasksActivity_T", "4. Открыли БАЗОВЫЙ фрагмент. кол-во: " + fragmentManager.getBackStackEntryCount());
//        Log.e("TasksActivity_T", "4. fragmentManager.getFragments(): " + fragmentManager.getFragments());
//    }
}
