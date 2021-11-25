package ua.com.merchik.merchik.Activities.TaskAndReclamations.ReclamationsActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARHomeFrag;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARSecondFrag;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.toolbar_menus;

public class ReclamationsActivity extends toolbar_menus {

    Globals globals = new Globals();
    FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent();

        try {
            findViewById(R.id.fab).setOnClickListener(v -> {
//                Toast.makeText(this, "Подсказка к данному разделу не готова", Toast.LENGTH_SHORT).show();
//                textLesson = 1181;
//                videoLesson = 1182;
//                setFab(this, findViewById(R.id.fab));
            });

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
        Log.e("ReclamationsActivity_T", "1. Количество фрагментов в момент нажатия назад: " + fragmentManager.getBackStackEntryCount());
        if (fragmentManager.getBackStackEntryCount() > 0) {
            Log.e("ReclamationsActivity_T", "1. Фрагментов не 0: " + fragmentManager.getBackStackEntryCount());
            Log.e("ReclamationsActivity_T", "1. fragmentManager.getFragments(): " + fragmentManager.getFragments());

            fragmentManager.popBackStackImmediate();
//            fragmentManager.popBackStack();
        } else {
            Log.e("ReclamationsActivity_T", "2. Фрагментов нет: " + fragmentManager.getBackStackEntryCount());
            Log.e("ReclamationsActivity_T", "2. fragmentManager.getFragments(): " + fragmentManager.getFragments());
            super.onBackPressed();
        }
    }

    // =================================== --- @Override --- =======================================

    private void setActivityContent() {
        setContentView(R.layout.drawler_reclamations);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        TextView activity_title = (TextView) findViewById(R.id.activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title.setText("Рекламации");
//        activity_title.setBackgroundColor(Color.parseColor("#B1B1B1"));


        // Тут надо бахнуть фрагмент стартовый
        setHomeFrag();
    }




    /**
     * 19.03.2021
     * Установка базового фрагмента
     * */
    TARHomeFrag homeFrag;
    TARSecondFrag secondFrag;
    private void setHomeFrag(){
        fragmentManager = getSupportFragmentManager();


        homeFrag = new TARHomeFrag().newInstance(1, new Globals.TARInterface() {
            @Override
            public void onSuccess(TasksAndReclamationsSDB data) {
                // Открываю новый фрагмент
//                Toast.makeText(ReclamationsActivity.this, "Я нажал на кнопку: " + data.getID(), Toast.LENGTH_LONG).show();
                secondFrag = new TARSecondFrag(fragmentManager, data);
                fragmentManager.beginTransaction()
                        .addToBackStack(null)
                        .hide(homeFrag)
                        .add(R.id.fragment, secondFrag)
                        .commit();

                Log.e("ReclamationsActivity_T", "3. Открыли новый фрагмент. кол-во: " + fragmentManager.getBackStackEntryCount());
                Log.e("ReclamationsActivity_T", "3. fragmentManager.getFragments(): " + fragmentManager.getFragments());
            }

            @Override
            public void onFailure(String error) {

            }
        });


        fragmentManager.beginTransaction()
                .add(R.id.fragment, homeFrag)
                .addToBackStack(null)
                .commit();


        Log.e("ReclamationsActivity_T", "4. Открыли БАЗОВЫЙ фрагмент. кол-во: " + fragmentManager.getBackStackEntryCount());
        Log.e("ReclamationsActivity_T", "4. fragmentManager.getFragments(): " + fragmentManager.getFragments());
    }




}
