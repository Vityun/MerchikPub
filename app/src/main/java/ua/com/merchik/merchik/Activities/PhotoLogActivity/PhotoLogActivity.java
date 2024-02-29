package ua.com.merchik.merchik.Activities.PhotoLogActivity;

import static ua.com.merchik.merchik.MakePhoto.MakePhoto.CAMERA_REQUEST_TAKE_PHOTO;
import static ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm.getPhotosByDAD2;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.dialogs.DialogAchievement.DialogAchievement.clickVoidAchievement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.ViewHolders.PhotoAndInfoViewHolder;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFilter.DialogFilter;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;
import ua.com.merchik.merchik.toolbar_menus;

public class PhotoLogActivity extends toolbar_menus {

    private EditText editText;
    private TextView activity_title;
    private ImageButton filter;
    private RecyclerView recyclerView;
    private ImageView imageView;
    private long codeDad2;

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
        } catch (Exception e) {
            DialogData dialog = new DialogData(this);
            dialog.setTitle("Ошибка");
            dialog.setText("Журнал фото 3: " + e);
            dialog.setClose(dialog::dismiss);
            dialog.show();
        }

    }//------------------------------------ /ON CREATE ---------------------------------------------

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

        editText = (EditText) findViewById(R.id.searchViewPhotoLog);
        filter = findViewById(R.id.filter);
        imageView = findViewById(R.id.imageView);

        setFilter();
    }


    private PhotoLogAdapter recycleViewPLAdapter;
    private RealmResults<StackPhotoDB> stackPhoto;

    // Pika вынес сюда чтоб иметь доступ потом ниже
    List<SamplePhotoSDB> samplePhotoSDBList;
    boolean isSample=false;

    private void setRecycler() {

        editText.setHint("Введите текст для поиска по любым реквизитам");
        editText.clearFocus();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewPhotoLog);

        Log.e("PHOTO_LOG_ACT", "RealmManager.getStackPhoto(): " + RealmManager.getStackPhoto().size());

        PhotoLogMode photoLogMode;

        try {
            if (getChoice() && !this.getIntent().getBooleanExtra("achievements", false)) {
                photoLogMode = PhotoLogMode.CHOICE;
                stackPhoto = StackPhotoRealm.getTARFilterPhoto(
                        this.getIntent().getIntExtra("address", 0),
                        this.getIntent().getStringExtra("customer")
                );
                Globals.writeToMLOG("INFO", "PhotoLogActivity/setRecycler/StackPhotoRealm.getTARFilterPhoto", "stackPhoto: " + stackPhoto.size());

            } else if (this.getIntent().getBooleanExtra("planogram", false)) {
                codeDad2 = this.getIntent().getLongExtra("dad2", 0);
                imageView.setVisibility(View.VISIBLE);
                imageView.setRotation(45);
                imageView.setOnClickListener((view) -> {
                    Toast.makeText(view.getContext(), "open camera", Toast.LENGTH_SHORT).show();
                    MakePhoto makePhoto = new MakePhoto();
                    makePhoto.openCamera(this, CAMERA_REQUEST_TAKE_PHOTO);
                });

                photoLogMode = PhotoLogMode.PLANOGRAM;
                int addr = this.getIntent().getIntExtra("address", 0);
                String cust = this.getIntent().getStringExtra("customer");
                stackPhoto = StackPhotoRealm.getPlanogramPhoto(addr, cust);

                if (stackPhoto == null) {
                    Toast.makeText(this, "Фото Планограмм НЕ найдено. \n\nОбратитесь к Вашему руководителю.", Toast.LENGTH_LONG).show();
                } else {
                    Globals.writeToMLOG("INFO", "PhotoLogActivity/setRecycler/planogram", "stackPhoto: " + stackPhoto.size());
                }
            } else if (this.getIntent().getBooleanExtra("report_prepare", false)) {
                photoLogMode = PhotoLogMode.REPORT_PREPARE;
                stackPhoto = RealmManager.getStackPhotoLogByDad2(this.getIntent().getLongExtra("dad2", 0));
                if (stackPhoto == null || stackPhoto.size() == 0) {
                    Toast.makeText(this, "По данному отчёту фото не найдено", Toast.LENGTH_SHORT).show();
                }
            } else if (this.getIntent().getBooleanExtra("SamplePhoto", false)) {

                // Pika не знаю какие виды фото может отображать эта активити, поэтому делаю флаг чтоб знать когда она
                // работает именно с типами - образец фото
                isSample=true;

                if (this.getIntent().getBooleanExtra("SamplePhotoActivity", false)) {
                    photoLogMode = PhotoLogMode.SAMPLE_PHOTO_ACTIVITY;   // Тип откуда открыли Журнал Фото
                } else {
                    photoLogMode = PhotoLogMode.SAMPLE_PHOTO;   // Тип откуда открыли Журнал Фото
                }

                int photoTp = this.getIntent().getIntExtra("photoTp", 999);     // Если открыли Журнал фото с каким-то типом = он тут
                int grpId = this.getIntent().getIntExtra("grpId", 999);

                Globals.writeToMLOG("INFO", "PhotoLogActivity/setRecycler/SamplePhoto", "SamplePhoto TP: " + photoTp);

                // Pika Убрал отсюда поскольку мне нужен доступ к нему в дркгом методе
                // Получаем список Образцов Фото для фрмирования запроса к БД Стэк Фото
                // List<SamplePhotoSDB> samplePhotoSDBList;

                if (photoTp == 999) {
                    samplePhotoSDBList = SQL_DB.samplePhotoDao().getPhotoLogActive(1);
                } else {
                    samplePhotoSDBList = SQL_DB.samplePhotoDao().getPhotoLogActiveAndTp(1, photoTp, grpId);
                }

                // --- DEBUG ---
                if (samplePhotoSDBList != null) {
                    Globals.writeToMLOG("INFO", "PhotoLogActivity/setRecycler/SamplePhoto/samplePhotoSDBList", "samplePhotoSDBList.Size: " + samplePhotoSDBList.size());
                } else {
                    Globals.writeToMLOG("INFO", "PhotoLogActivity/setRecycler/SamplePhoto/samplePhotoSDBList", "samplePhotoSDBList is NULL");
                }
                // --- DEBUG ---

                // Формируем ID шники для Стэк Фото
                String[] photoIds = new String[samplePhotoSDBList.size()];
                for (int i = 0; i < samplePhotoSDBList.size(); i++) {
                    photoIds[i] = String.valueOf(samplePhotoSDBList.get(i).photoId);
                }

                Globals.writeToMLOG("INFO", "PhotoLogActivity/setRecycler/SamplePhoto", "photoIds: " + Arrays.toString(photoIds));

                stackPhoto = StackPhotoRealm.getByIds2(photoIds);

                Globals.writeToMLOG("INFO", "PhotoLogActivity/setRecycler/SamplePhoto", "stackPhoto size: " + stackPhoto.size());

            } else if (this.getIntent().getBooleanExtra("achievements", false)) {
                photoLogMode = PhotoLogMode.ACHIEVEMENTS;
                long dad2 = this.getIntent().getLongExtra("dad2", 0);
                int photoType = this.getIntent().getIntExtra("photoType", 0);

                stackPhoto = getPhotosByDAD2(dad2, photoType);
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
        PhotoLogMode finalPhotoLogMode = photoLogMode;
        recycleViewPLAdapter = new PhotoLogAdapter(this, stackPhoto, getChoice(), new Clicks.click() {
            @Override
            public <T> void click(T data) {
                try {
                    // TODO тут есть косяк, у мерчиков по какой-то причине вылетало приложение. resultCode == 101 не срабатывал там где должен был. Вообще это лучше по другому делать.
                    switch (finalPhotoLogMode) {
                        case SAMPLE_PHOTO:
                            if (stackPhoto != null && stackPhoto.size() == 1) {
                                finish();
                            }
                            break;

                        case ACHIEVEMENTS:
                            if (clickVoidAchievement != null) {
                                Toast.makeText(getApplicationContext(), "Натиснули на якусь фотку", Toast.LENGTH_LONG).show();
                                clickVoidAchievement.click(data);
                            }
                            finish();
                            break;

                        default:
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
                            break;
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "PhotoLogActivity/PhotoLogAdapter/click", "Exeption: " + e);
                    DialogData dialogData = new DialogData(getApplicationContext());
                    dialogData.setTitle("Произошла ошибка");
                    dialogData.setText("Попробуйте повторить попытку, если ошибка повторяется - передайте руководителю отладочный файл.");
                    dialogData.setClose(dialogData::dismiss);
                    dialogData.show();
                }
            }
        }, new PhotoLogPhotoAdapter.OnPhotoClickListener() {
            @Override
            public void onPhotoClicked(Context context, StackPhotoDB photoDB) {
//                DialogFullPhotoR dialog = new DialogFullPhotoR(getApplicationContext());
                DialogFullPhotoR dialog = new DialogFullPhotoR(context);
                dialog.setPhoto(photoDB);
                // Pika
                // dialog.setComment(photoDB.getComment());

                // Pika Сделал более универсально - если это фото оьразца то коммент будет в поле "about"
                // образцов - его и берем, а если это просто фото - то коммент из поля комментария самого фото
                // то же касается и образца если для него не сделали нормальный комментв поле "about" образцов
                // плюс учитывается что поскольку это журнал, то может быть несколько разных фоток,
                // поэтому подбираю коммент для соответственной фотки из списка образцов по ИД фотки
                int photoId=Integer.parseInt(photoDB.getPhotoServerId());
                String commentPhoto="";
                for (SamplePhotoSDB a:samplePhotoSDBList) {
                    if (a.photoId==photoId) {
                        commentPhoto=a.about;
                        break;
                    }
                }
                if (commentPhoto==null || commentPhoto=="") {
                    commentPhoto=photoDB.getComment();
                }
                dialog.setComment(commentPhoto);
                // Pika для образца фото делаю такое масштабирование
                if (isSample) { dialog.scaleType(ImageView.ScaleType.FIT_CENTER); }
                // ----------------------------------------------

                dialog.setClose(dialog::dismiss);
                dialog.show();
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

    private void setFilter() {
        DialogFilter dialog = new DialogFilter(this, Globals.SourceAct.WP_DATA);
        dialog.setTextFilter(editText.getText().toString());
        dialog.setClose(dialog::dismiss);
        dialog.setCancel(() -> {
            editText.setText("");
            setFilterIco(dialog);
            recycleViewPLAdapter.updateData(stackPhoto);
            recyclerView.scheduleLayoutAnimation();
            recycleViewPLAdapter.notifyDataSetChanged();
        });
        dialog.setApply(() -> {
            RealmResults<StackPhotoDB> data = StackPhotoRealm.getAllRealm();
            if (dialog.clientId != null) {
                data = data.where().equalTo("client_id", dialog.clientId).findAll();
            }

            if (dialog.addressId != null) {
                data = data.where().equalTo("addr_id", dialog.addressId).findAll();
            }

            if (dialog.dateFrom != null && dialog.dateTo != null) {

                Long dt1 = Clock.dateConvertToLong(dialog.dateFrom);
                Long dt2 = Clock.dateConvertToLong(dialog.dateTo);

                if (dt1 != null && dt2 != null) {
                    data = data.where().between("dt", dt1, dt2).findAll();
                }
            }


            recycleViewPLAdapter.updateData(data);
            if (dialog.textFilter != null && !dialog.textFilter.equals("")) {
                editText.setText(dialog.textFilter);
            }
            recycleViewPLAdapter.notifyDataSetChanged();
            setFilterIco(dialog);
        });

        setFilterIco(dialog);

        filter.setOnClickListener((v) -> {
            dialog.show();
        });
    }

    /**
     * Set that
     */
    private void setFilterIco(DialogFilter dialog) {
        if (dialog.isFiltered()) {
            filter.setImageDrawable(getResources().getDrawable(R.drawable.ic_filterbold));
        } else {
            filter.setImageDrawable(getResources().getDrawable(R.drawable.ic_filter));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CAMERA_REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    savePhoto();
                } else {
                    // Тут надо будет обработать удаление, наверно, фотки.
                }
                break;
        }
    }

    /**
     * Сделано для сохранения фото планограмм.
     */
    private void savePhoto() {
        if (codeDad2 != 0) {
            WpDataDB wp = RealmManager.INSTANCE.copyFromRealm(WpDataRealm.getWpDataRowByDad2Id(codeDad2));

            AddressSDB addr = SQL_DB.addressDao().getById(wp.getAddr_id());
            CustomerSDB client = SQL_DB.customerDao().getById(wp.getClient_id());

            StackPhotoDB stackPhotoDB = saveTestPhoto(new File(MakePhoto.openCameraPhotoUri), addr, client);
            MakePhoto.openCameraPhotoUri = null;
        }
    }

    private StackPhotoDB saveTestPhoto(File photoFile, AddressSDB addr, CustomerSDB client) {
        try {
            int id = RealmManager.stackPhotoGetLastId();
            id++;
            StackPhotoDB stackPhotoDB = new StackPhotoDB();
            stackPhotoDB.setId(id);
            stackPhotoDB.setDt(System.currentTimeMillis() / 1000);
            stackPhotoDB.setTime_event(Clock.getHumanTime2(System.currentTimeMillis() / 1000));

            stackPhotoDB.setAddr_id(addr.id);
            stackPhotoDB.setAddressTxt(addr.nm);

            stackPhotoDB.setClient_id(client.id);
            stackPhotoDB.setCustomerTxt(client.nm);

            stackPhotoDB.setUser_id(Globals.userId);
            stackPhotoDB.setPhoto_type(5);

            stackPhotoDB.setDvi(1);

            stackPhotoDB.setCreate_time(System.currentTimeMillis());

            stackPhotoDB.setPhoto_hash(globals.getHashMD5FromFile2(photoFile, this));
            stackPhotoDB.setPhoto_num(photoFile.getAbsolutePath());


            RealmManager.stackPhotoSavePhoto(stackPhotoDB);
            return stackPhotoDB;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "TARActivity.onActivityResult.savePhoto", "Exception e: " + e);
            return null;
        }
    }

}
