package ua.com.merchik.merchik.Activities.PhotoLogActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.ViewHolders.PhotoAndInfoViewHolder;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.dialogs.DialogFilter.DialogFilterPhotoLog;
import ua.com.merchik.merchik.toolbar_menus;

public class PhotoLogActivity extends toolbar_menus {

    private TextView activity_title;
    private ImageButton filter;

//    private boolean choice = false;


    //----------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContent();
        } catch (Exception e) {
            DialogData dialog = new DialogData(this);
            dialog.setTitle("Ошибка");
            dialog.setText("Журнал фото 2: " + e);
            dialog.setClose(dialog::dismiss);
            dialog.show();
        }


        try {
            initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));
//            NavigationView navigationView;
//            navigationView = findViewById(R.id.nav_view);
//            navigationView.setCheckedItem(R.id.nav_dr);
        } catch (Exception e) {
            DialogData dialog = new DialogData(this);
            dialog.setTitle("Ошибка");
            dialog.setText("Журнал фото 3: " + e);
            dialog.setClose(dialog::dismiss);
            dialog.show();
        }

    }//------------------------------------ /ON CREATE ---------------------------------------------

    public static void checkClick(Clicks.click click) {
        click.click(PhotoAndInfoViewHolder.stackPhotoDB);
    }

    public void setChoice() {
//        choice = true;
    }

    public boolean getChoice() {
        return this.getIntent().getBooleanExtra("choise", false);
    }

    /**
     * 16.02.2021
     * Установка контента активности
     */
    private void setContent() {
        try {
            setBaseContent();

            if (getChoice()) {
                setRecycler();
            } else {
                setRecycler();
            }
        } catch (Exception e) {
            Log.e("ERROR_setContent", "Exception e: " + e);
            e.printStackTrace();
        }
    }


    /**
     * 16.02.2021
     * Установка базового интерфейса
     */
    private void setBaseContent() {
        setContentView(R.layout.drawler_photo_log);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title = (TextView) findViewById(R.id.activity_title);
        activity_title.setText(getResources().getString(R.string.activity_photo_log_title));

        filter = findViewById(R.id.filter);
    }


    /**
     * 16.02.2021
     */
    RecyclerView recyclerView;
    PhotoLogAdapter recycleViewPLAdapter;

    private void setRecycler() {
        EditText editText = (EditText) findViewById(R.id.searchViewPhotoLog);
        editText.setHint("Введите текст для поиска по любым реквизитам");
        editText.clearFocus();

        filter = findViewById(R.id.filter);
        filter.setOnClickListener(v -> {
/*            DialogFilter dialog = new DialogFilter(this, Globals.SourceAct.PHOTO_LOG);
            dialog.setClose(dialog::dismiss);

            dialog.serEditFilter(editText.getHint(), editText.getText());
            dialog.setFilters();

            dialog.show();*/

            DialogFilterPhotoLog dialog = new DialogFilterPhotoLog(this, editText.getText().toString());
            dialog.setApply(new Click() {
                @Override
                public <T> void onSuccess(T data) {

                }

                @Override
                public void onFailure(String error) {

                }
            });
            dialog.show();

            Log.e("test error", "dafgadfgadgadgadfgadga");
        });


        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewPhotoLog);


        Log.e("PHOTO_LOG_ACT", "RealmManager.getStackPhoto(): " + RealmManager.getStackPhoto().size());


        PhotoLogMode photoLogMode;
        RealmResults stackPhoto;
        try {
            if (getChoice()) {
                photoLogMode = PhotoLogMode.CHOICE;
                stackPhoto = StackPhotoRealm.getTARFilterPhoto(
                        this.getIntent().getIntExtra("address", 0),
                        this.getIntent().getStringExtra("customer")
                );
                Globals.writeToMLOG("INFO", "PhotoLogActivity/setRecycler/StackPhotoRealm.getTARFilterPhoto", "stackPhoto: " + stackPhoto.size());

            } else if (this.getIntent().getBooleanExtra("planogram", false)) {
                photoLogMode = PhotoLogMode.PLANOGRAM;
                stackPhoto = StackPhotoRealm.getPlanogramPhoto(
                        this.getIntent().getIntExtra("address", 0),
                        this.getIntent().getStringExtra("customer")
                );
                Globals.writeToMLOG("INFO", "PhotoLogActivity/setRecycler/planogram", "stackPhoto: " + stackPhoto.size());

            } else if (this.getIntent().getBooleanExtra("report_prepare", false)) {
                photoLogMode = PhotoLogMode.REPORT_PREPARE;
                stackPhoto = RealmManager.getStackPhotoLogByDad2(this.getIntent().getLongExtra("dad2", 0));
                if (stackPhoto == null || stackPhoto.size() == 0) {
                    Toast.makeText(this, "По данному отчёту фото не найдено", Toast.LENGTH_SHORT).show();
                }
            } else {
                photoLogMode = PhotoLogMode.BASE;
                stackPhoto = RealmManager.getStackPhoto();
                Globals.writeToMLOG("INFO", "PhotoLogActivity/setRecycler/RealmManager.getStackPhoto()", "stackPhoto: " + stackPhoto.size());

            }
        } catch (Exception e) {
            photoLogMode = PhotoLogMode.BASE;
            stackPhoto = RealmManager.getStackPhoto();
            Globals.writeToMLOG("ERROR", "PhotoLogActivity/setRecycler", "Exeption: " + e);
        }


        Integer resultCode = this.getIntent().getIntExtra("resultCode", 0);
        recycleViewPLAdapter = new PhotoLogAdapter(this, stackPhoto, getChoice(), new Clicks.click() {
            @Override
            public <T> void click(T data) {
                PhotoAndInfoViewHolder.stackPhotoDB = (StackPhotoDB) data;

                Intent intent = new Intent();
                intent.putExtra("stack_photo_id", PhotoAndInfoViewHolder.stackPhotoDB.getId());

                if (resultCode != null && resultCode != 0) {
                    switch (resultCode) {
                        case 100:
                            setResult(100, intent);
                            break;

                        case 101:
                            setResult(101, intent);
                            break;

                        default:
                            setResult(100, intent);
                            break;
                    }
                }

                finish();
            }
        });
        recycleViewPLAdapter.setPhotoLogMode(photoLogMode);

        recyclerView.setAdapter(recycleViewPLAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // NEW
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    recycleViewPLAdapter.getFilter().filter(s);
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

}
