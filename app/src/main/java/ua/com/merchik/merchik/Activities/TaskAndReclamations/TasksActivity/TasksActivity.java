package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.toolbar_menus;

public class TasksActivity extends toolbar_menus {

    Globals globals = new Globals();
    FragmentManager fragmentManager;

    public static TextView activity_title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent();

        try {

            findViewById(R.id.fab).setOnClickListener(v -> {
//                Toast.makeText(this, "Подсказка к данному разделу не готова", Toast.LENGTH_SHORT).show();

//                textLesson = 1179;
//                videoLesson = 1180;
//                setFab(this, findViewById(R.id.fab));
            });

//            setFab(this, findViewById(R.id.fab));
            initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));

//            NavigationView navigationView = findViewById(R.id.nav_view);
//            navigationView.setCheckedItem(R.id.nav_home);
        } catch (Exception e) {
            globals.alertDialogMsg(this, "Ошибка: " + e);
        }
    }
    // =================================== --- onCreate --- ========================================

    @Override
    public void onBackPressed(){
        fragmentManager = getSupportFragmentManager();
        Log.e("TasksActivity_T", "1. Количество фрагментов в момент нажатия назад: " + fragmentManager.getBackStackEntryCount());

        if (fragmentManager.getBackStackEntryCount() > 2) {

//            fragmentManager.

            Log.e("TasksActivity_T", "1. Фрагментов не 0: " + fragmentManager.getBackStackEntryCount());
            Log.e("TasksActivity_T", "1. fragmentManager.getFragments(): " + fragmentManager.getFragments());

            fragmentManager.popBackStackImmediate();

        } else {
            fragmentManager.beginTransaction()
                    .remove(secondFrag)
                    .commit();

            Intent intent = new Intent(this, TasksActivity.class);
            startActivity(intent);

            Log.e("TasksActivity_T", "2. Фрагментов нет: " + fragmentManager.getBackStackEntryCount());
            Log.e("TasksActivity_T", "2. fragmentManager.getFragments(): " + fragmentManager.getFragments());
            super.onBackPressed();
        }
    }

    // =================================== --- @Override --- =======================================

    private void setActivityContent() {
        setContentView(R.layout.drawler_tasks);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        activity_title = (TextView) findViewById(R.id.activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //Убирает фокус с полей ввода
        setHomeFrag();
    }



    TARHomeFrag homeFrag;
    TARSecondFrag secondFrag;
    private void setHomeFrag(){
        fragmentManager = getSupportFragmentManager();

        homeFrag = new TARHomeFrag().newInstance(0, new Globals.TARInterface() {
            @Override
            public void onSuccess(TasksAndReclamationsSDB data) {
                // Открываю новый фрагмент
                secondFrag = new TARSecondFrag(fragmentManager, data);
                fragmentManager.beginTransaction()
                        .addToBackStack(null)
                        .hide(homeFrag)
                        .add(R.id.fragment, secondFrag)
                        .commit();

                Log.e("TasksActivity_T", "3. Открыли новый фрагмент. кол-во: " + fragmentManager.getBackStackEntryCount());
                Log.e("TasksActivity_T", "3. fragmentManager.getFragments(): " + fragmentManager.getFragments());
            }

            @Override
            public void onFailure(String error) {
            }
        });

        fragmentManager.beginTransaction()
                .add(R.id.fragment, homeFrag)
                .commit();

        Log.e("TasksActivity_T", "4. Открыли БАЗОВЫЙ фрагмент. кол-во: " + fragmentManager.getBackStackEntryCount());
        Log.e("TasksActivity_T", "4. fragmentManager.getFragments(): " + fragmentManager.getFragments());
    }


}
