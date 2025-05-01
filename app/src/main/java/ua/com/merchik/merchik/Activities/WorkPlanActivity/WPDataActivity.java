package ua.com.merchik.merchik.Activities.WorkPlanActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import dagger.hilt.android.AndroidEntryPoint;
import io.realm.RealmResults;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.FabYoutube;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.RecycleViewWPAdapter;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dialogs.features.LoadingDialogWithPercent;
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel;
import ua.com.merchik.merchik.retrofit.CheckInternet.CheckServer;
import ua.com.merchik.merchik.retrofit.CheckInternet.NetworkUtil;
import ua.com.merchik.merchik.toolbar_menus;

@AndroidEntryPoint
public class WPDataActivity extends toolbar_menus {

    private RealmResults<WpDataDB> workPlan;

    private FabYoutube fabYoutube = new FabYoutube();
    private FloatingActionButton fabYouTube;
    private TextView badgeTextView;
    public static final Integer[]  WPDataActivity_VIDEO_LESSONS = new Integer[]{817};

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private FragmentManager fragmentManager;
    private WPDataFragmentHome homeFrag;
    private WPDataFragmentMap mapFrag;


    Globals globals = new Globals();

    TextView activity_title;
    EditText editsearch;
    EditText searchView;
    private ImageButton filter;

    //----------------------------------------------------------------------------------------------
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawler_wp);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        filter = findViewById(R.id.filter);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        fabYouTube = findViewById(R.id.fab3);
        badgeTextView = findViewById(R.id.badge_text_view_tar);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //Убирает фокус с полей ввода

        // Установка закладок
        setTabs(getIntent().getBooleanExtra("initialOpent",false));

        textLesson = 816;
        videoLesson = 817;
        videoLessons = null;
        setFab(this, findViewById(R.id.fab), ()->{});
        fabYoutube.setFabVideo(fabYouTube, WPDataActivity_VIDEO_LESSONS, () -> fabYoutube.showYouTubeFab(fabYouTube, badgeTextView, WPDataActivity_VIDEO_LESSONS));
        fabYoutube.showYouTubeFab(fabYouTube, badgeTextView, WPDataActivity_VIDEO_LESSONS);

        initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(129);

        wpDataInfo();   // Сообщение какие-то.

    }//------------------------------- /ON CREATE --------------------------------------------------



    private RecyclerView recyclerView;
    private RecycleViewWPAdapter adapter;
    private void visualizeWpData(){

        recyclerView = (RecyclerView) findViewById(R.id.RecyclerViewWorkPlan);

        adapter = new RecycleViewWPAdapter(this, workPlan);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        searchView = (EditText) findViewById(R.id.searchView);
        searchView.setText(Clock.today);
        searchView.clearFocus();

        adapter.getFilter().filter(searchView.getText());

        Log.e("TAG_SEARCH_VIEW", "WP: " + searchView);

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() != 0){
                    adapter.getFilter().filter(s);
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


    private void setTabs(boolean initialOpen){
        tabLayout.getTabAt(0).setText(getText(R.string.title_0));
        tabLayout.getTabAt(1).setText(getText(R.string.title_1));

        fragmentManager = getSupportFragmentManager();
        WPDataTab tabAdapter = new WPDataTab(fragmentManager, getLifecycle(), tabLayout.getTabCount(), initialOpen);
        viewPager.setAdapter(tabAdapter);
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

    /**
     * 24.11.23.
     *
     * */
    Globals.InternetStatus internetStatus;
    private void wpDataInfo(){
        try {
            Intent intent = getIntent();
            String extra = intent.getStringExtra("InternetStatusMassage");

            if (NetworkUtil.isNetworkConnected(this)){
                CheckServer.isServerConnected(this, CheckServer.ServerConnect.DEFAULT, null, new Clicks.clickStatusMsg() {
                    @Override
                    public void onSuccess(String data) {
                        // Типо всё ок
                        if (extra != null && extra.equals("SHOW_MASSAGE")){
                            internetStatus = Globals.InternetStatus.INTERNET;
//                            Globals.showInternetStatusMassage(WPDataActivity.this, internetStatus);
                            Toast.makeText(WPDataActivity.this, "Все нормально, сервер онлайн", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        if (extra != null && extra.equals("SHOW_MASSAGE")){
                            internetStatus = Globals.InternetStatus.NO_SERVER;
                            Globals.showInternetStatusMassage(WPDataActivity.this, internetStatus);
                        }
                    }
                });
            }else {
                if (extra != null && extra.equals("SHOW_MASSAGE")){
                    internetStatus = Globals.InternetStatus.NO_INTERNET;
                    Globals.showInternetStatusMassage(this, internetStatus);
                }
            }
        }catch (Exception e){
            Log.e("wpDataInfo", "Exception e: " + e);
            e.printStackTrace();
        }
    }
}
