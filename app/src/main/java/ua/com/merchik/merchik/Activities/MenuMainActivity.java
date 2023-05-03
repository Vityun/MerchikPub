package ua.com.merchik.merchik.Activities;

import static ua.com.merchik.merchik.Globals.userId;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.toolbar_menus;


public class MenuMainActivity extends toolbar_menus {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent();


        try {

            findViewById(R.id.fab).setOnClickListener(v -> {
                Toast.makeText(this, "Подсказка к данному разделу не готова", Toast.LENGTH_SHORT).show();
                test();
            });

            findViewById(R.id.fab).setOnLongClickListener(v -> {
                Toast.makeText(this, "Отладочная информация!\nДолгий клик по подсказке.", Toast.LENGTH_SHORT).show();
//                test2(v.getContext());
                return true;
            });


            initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_home);
        } catch (Exception e) {
            globals.alertDialogMsg(this, "ОшибкаMain: " + e);
        }
    }

    private void test() {
        String link = String.format("/mobile.php?mod=ticket**act=create**theme_id=611**client_id**addr_id");
        link = link.replace("&", "**");

        AppUsersDB appUser = AppUserRealm.getAppUserById(userId);

        String hash = String.format("%s%s%s", appUser.getUserId(), appUser.getPassword(), "AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3");
        hash = Globals.getSha1Hex(hash);

        String format = String.format("https://merchik.com.ua/sa.php?&u=%s&s=%s&l=%s", userId, hash, link);


        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(format));
        this.startActivity(browserIntent);
    }

    // =================================== --- onCreate --- ========================================

    private void setActivityContent() {
        setContentView(R.layout.toolbar_new);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        TextView activity_title = (TextView) findViewById(R.id.activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title.setText(getString(R.string.title_activity_menu_main));
    }



}
