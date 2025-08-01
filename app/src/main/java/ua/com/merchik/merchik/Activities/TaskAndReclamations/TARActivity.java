package ua.com.merchik.merchik.Activities.TaskAndReclamations;

import static ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.Tab3Fragment.TARCommentIndex;
import static ua.com.merchik.merchik.Globals.getRealPathFromURI;
import static ua.com.merchik.merchik.MakePhoto.MakePhoto.CAMERA_REQUEST_TAR_COMMENT_PHOTO;
import static ua.com.merchik.merchik.MakePhoto.MakePhoto.PICK_GALLERY_IMAGE_REQUEST;
import static ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery.MakePhotoFromGaleryTasksAndReclamationsSDB;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.io.File;
import java.util.List;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARHomeFrag;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARSecondFrag;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.CreatePhotoFile;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.Utils.CustomString;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.AddressDB;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.dialogs.DialodTAR.DialogCreateTAR;
import ua.com.merchik.merchik.toolbar_menus;

public class TARActivity extends toolbar_menus implements TARFragmentHome.OnFragmentInteractionListener, TARHomeFrag.OnFragmentInteractionListener {

    private Globals globals = new Globals();


    private TARHomeFrag homeFrag;
    private TARSecondFrag secondFrag;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private FragmentManager fragmentManager;

    //    private FragmentManager fragmentManager;
    public static TextView activity_title;

    public static int TARType;

    public static FloatingActionButton fab,fabViber;

    public DialogCreateTAR dialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent();
    }

    public TasksAndReclamationsSDB setActivityTAR() {
        return (TasksAndReclamationsSDB) getIntent().getParcelableExtra("TARActivityStart");
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

        try {
            fab = findViewById(R.id.fab);
            fabViber = findViewById(R.id.fab_viber_tar);

            Log.e("SET_TAR_FAB", "CLICK");
            if (TARType == 1) {
                textLesson = 1179;
                videoLesson = 1180;
                videoLessons = null;
                setFab(this, fab, ()->{});
                Log.e("SET_TAR_FAB", "ACTIVITY task");
            } else if (TARType == 0) {
                textLesson = 1181;
                videoLesson = 1182;
                videoLessons = null;
                setFab(this, fab, ()->{});
                Log.e("SET_TAR_FAB", "ACTIVITY report");
            }

            fabViber.setOnClickListener(v -> {
                String format = CustomString.viberLink();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(format));
                this.startActivity(intent);
            });


            findViewById(R.id.fabAdd).setVisibility(View.GONE);
            findViewById(R.id.fabAdd).setOnClickListener(v -> {
                Intent intent = new Intent(this, PhotoLogActivity.class);

                dialog = new DialogCreateTAR(this);
                dialog.setClose(dialog::dismiss);
                dialog.setTarType(TARType);
                dialog.setRecyclerView(new Clicks.click() {
                    @Override
                    public <T> void click(T data) {

                        switch ((Integer) data){
                            case 1:
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
                                break;

                            case 2:
                                try {
                                    MakePhoto makePhoto = new MakePhoto();
                                    makePhoto.openCamera(TARActivity.this, 202);
                                }catch (Exception e){
                                    Globals.writeToMLOG("ERROR", "Tab3Fragment.setAddButton.case2", "Exception e: " + e);
                                }
                                break;

                            default:
                                return;
                        }


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
                videoLessons = null;
                setFab(this, fab, ()->{});
                Log.e("SET_TAR_FAB", "task");
            } else if (TARType == 0) {
                TARActivity.activity_title.setText("Рекламации");
                textLesson = 1181;
                videoLesson = 1182;
                videoLessons = null;
                setFab(this, fab, ()->{});
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

        Log.e("TARActivity", "onActivityResult");
        Log.e("TARActivity", "requestCode: " + requestCode);
        Log.e("TARActivity", "resultCode: " + resultCode);
        Log.e("TARActivity", "Intent: " + data);

        try {
            Log.d("test", "data: " + requestCode + resultCode);
            Globals.writeToMLOG("INFO", "TARActivity.onActivityResult", "resultCode: " + resultCode);
            Globals.writeToMLOG("INFO", "TARActivity.onActivityResult", "requestCode: " + requestCode);

            if (resultCode == 100) {
                int id = data.getIntExtra("stack_photo_id", 0);
                StackPhotoDB photoDB = StackPhotoRealm.getById(id);

                List<Fragment> fragments = fragmentManager.getFragments();
                TARFragmentHome fragmentHome = (TARFragmentHome) fragments.get(0);

                fragmentHome.homeFrag.dialog.setData(photoDB.getAddr_id(), photoDB.getClient_id(), photoDB.getCode_dad2(), photoDB);
                fragmentHome.homeFrag.dialog.setDataUpdate();
                fragmentHome.homeFrag.dialog.refreshAdaper(photoDB);
            }

            if (resultCode == 101) {
                List<Fragment> fragments = fragmentManager.getFragments();
                TARFragmentHome fragmentHome = (TARFragmentHome) fragments.get(0);

                int id = data.getIntExtra("stack_photo_id", 0);
                Globals.writeToMLOG("INFO", "TARActivity.onActivityResult.data.getIntExtra", "stack_photo_id: " + id);
                fragmentHome.secondFrag.setPhoto(id);
                Log.e("test", "test" + secondFrag);
            }

            if (requestCode == 200) {
                if (resultCode != 0){
                    Globals.writeToMLOG("INFO", "TARActivity.onActivityResult.requestCode200", "MakePhoto.openCameraPhotoUri: " + MakePhoto.openCameraPhotoUri);

                    TasksAndReclamationsSDB tar = SQL_DB.tarDao().getById(TARSecondFrag.TaRID);

                    Globals.writeToMLOG("INFO", "TARActivity.onActivityResult.requestCode200", "tar: " + tar);

                    StackPhotoDB stackPhotoDB = savePhoto(MakePhoto.openCameraPhotoUri, tar);
                    String stackJson = new Gson().toJson(stackPhotoDB);
                    Globals.writeToMLOG("INFO", "TARActivity.onActivityResult.requestCode200", "stackPhotoDB: " + stackJson);

                    MakePhoto.openCameraPhotoUri = null;

                    List<Fragment> fragments = fragmentManager.getFragments();
                    TARFragmentHome fragmentHome = (TARFragmentHome) fragments.get(0);

                    fragmentHome.secondFrag.setPhoto(stackPhotoDB.getId());
                }else {
                    Globals.writeToMLOG("INFO", "TARActivity.onActivityResult.resultCode", "resultCode.resultCode: " + resultCode);
                }
            }

            if (requestCode == 202) {
                List<Fragment> fragments = fragmentManager.getFragments();
                TARFragmentHome fragmentHome = (TARFragmentHome) fragments.get(0);

                AddressDB addr = fragmentHome.homeFrag.dialog.address;
                CustomerDB client = fragmentHome.homeFrag.dialog.customer;

                StackPhotoDB stackPhotoDB = saveTestPhoto(new File(MakePhoto.openCameraPhotoUri), addr, client, fragmentHome.secondFrag.data);

                fragmentHome.homeFrag.dialog.setData(stackPhotoDB);
                fragmentHome.homeFrag.dialog.setDataUpdate();
                fragmentHome.homeFrag.dialog.refreshAdaper(stackPhotoDB);
            }

            if (requestCode == PICK_GALLERY_IMAGE_REQUEST) {
                List<Fragment> fragments = fragmentManager.getFragments();
                TARFragmentHome fragmentHome = (TARFragmentHome) fragments.get(0);


                Uri uri = data.getData();
                File file = null;
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    file = new CreatePhotoFile().createDefaultPhotoFile(this, uri);
                } else {
                    Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST", "Uri uri: " + uri);
                    String filePath = getRealPathFromURI(uri);
                    Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST", "filePath: " + filePath);
                    file = new File(filePath);
                    Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST", "file: " + file.length());
                }
                StackPhotoDB stackPhotoDB =  savePhoto(file, MakePhotoFromGaleryTasksAndReclamationsSDB, MakePhotoFromGalery.tovarId, getApplicationContext());

//                StackPhotoDB stackPhotoDB = saveTestPhoto(new File(MakePhoto.openCameraPhotoUri), addr, client, fragmentHome.secondFrag.data);

                try {
                    fragmentHome.secondFrag.setPhoto(stackPhotoDB.getId());
                }catch (Exception e){
                    Log.e("test", "Exception e: " + e);
                }
            }

            if (requestCode == CAMERA_REQUEST_TAR_COMMENT_PHOTO) {
                List<Fragment> fragments = fragmentManager.getFragments();
                TARFragmentHome fragmentHome = (TARFragmentHome) fragments.get(0);

                Globals.writeToMLOG("INFO", "CAMERA_REQUEST_TAR_COMMENT_PHOTO", "fragmentHome.secondFrag.data.addr: " + fragmentHome.secondFrag.data.addr);
                Globals.writeToMLOG("INFO", "CAMERA_REQUEST_TAR_COMMENT_PHOTO", "fragmentHome.secondFrag.data.client: " + fragmentHome.secondFrag.data.client);

                AddressSDB addr = SQL_DB.addressDao().getById(fragmentHome.secondFrag.data.addr);
                CustomerSDB client = SQL_DB.customerDao().getById(fragmentHome.secondFrag.data.client); // TODO починить Клиентов
                CustomerDB clientRealm = CustomerRealm.getCustomerById(fragmentHome.secondFrag.data.client);

                Globals.writeToMLOG("INFO", "CAMERA_REQUEST_TAR_COMMENT_PHOTO", "AddressSDB: " + addr);
                Globals.writeToMLOG("INFO", "CAMERA_REQUEST_TAR_COMMENT_PHOTO", "CustomerSDB: " + client);
                Globals.writeToMLOG("INFO", "CAMERA_REQUEST_TAR_COMMENT_PHOTO", "clientRealm: " + clientRealm);

                StackPhotoDB stackPhotoDB = saveTestPhoto(new File(MakePhoto.openCameraPhotoUri), addr, clientRealm, fragmentHome.secondFrag.data);
                MakePhoto.openCameraPhotoUri = null;

                fragmentHome.secondFrag.setPhotoComment(stackPhotoDB.getId(), TARCommentIndex);
            }


        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "TARActivity.onActivityResult", "Exception e: " + e);
        }
    }


    private void setTabs() {

        String homeTabTitle = getText(R.string.title_task).toString();
        TARType = getIntent().getIntExtra("TAR_type", 1);
        if (TARType == 1) {
            homeTabTitle = getText(R.string.title_task).toString();
        } else if (TARType == 0) {
            homeTabTitle = getText(R.string.title_reclamation).toString();
        }

        tabLayout.getTabAt(0).setText(homeTabTitle);
        tabLayout.getTabAt(1).setText(getText(R.string.title_1));

        fragmentManager = getSupportFragmentManager();
        TARHomeTab tabAdapter = new TARHomeTab(fragmentManager, getLifecycle(), tabLayout.getTabCount());
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


    /*Real*/
    private StackPhotoDB savePhoto(String str, TasksAndReclamationsSDB tar) {
        try {
            int id = RealmManager.stackPhotoGetLastId();
            id++;
            StackPhotoDB stackPhotoDB = new StackPhotoDB();
            stackPhotoDB.setId(id);
            stackPhotoDB.setDt(System.currentTimeMillis() / 1000);
            stackPhotoDB.setTime_event(Clock.getHumanTime2(System.currentTimeMillis() / 1000));

            stackPhotoDB.setAddr_id(tar.addr);
            stackPhotoDB.setClient_id(tar.client);
            stackPhotoDB.setUser_id(tar.vinovnik);


            if (tar.themeId == 150){
                stackPhotoDB.setPhoto_type(18);
                stackPhotoDB.tovar_id = String.valueOf(tar.refId);
            }else {
                stackPhotoDB.setPhoto_type(0);
            }
//            stackPhotoDB.setPhoto_type(0);// 14.09.23. В рамках отладки сделал все фото с типом 0
            stackPhotoDB.setCode_dad2(tar.codeDad2);

            if (tar.themeId == 1174) {
                stackPhotoDB.setDvi(1);
            }

            stackPhotoDB.setCreate_time(System.currentTimeMillis());

            stackPhotoDB.setPhoto_hash(globals.getHashMD5FromFilePath(str, null));
            stackPhotoDB.setPhoto_num(str);
            RealmManager.stackPhotoSavePhoto(stackPhotoDB);
            return stackPhotoDB;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "TARActivity.onActivityResult.savePhoto", "Exception e: " + e);
            return null;
        }
    }

    private StackPhotoDB saveTestPhoto(File photoFile, AddressDB addr, CustomerDB client, TasksAndReclamationsSDB tar) {
        try {
            int id = RealmManager.stackPhotoGetLastId();
            id++;
            StackPhotoDB stackPhotoDB = new StackPhotoDB();
            stackPhotoDB.setId(id);
            stackPhotoDB.setDt(System.currentTimeMillis() / 1000);
            stackPhotoDB.setTime_event(Clock.getHumanTime2(System.currentTimeMillis() / 1000));

            stackPhotoDB.setAddr_id(addr.getAddrId());
            stackPhotoDB.setAddressTxt(addr.getNm());

            stackPhotoDB.setClient_id(client.getId());
            stackPhotoDB.setCustomerTxt(client.getNm());

            stackPhotoDB.setUser_id(Globals.userId);
            if (tar.themeId == 150){
                stackPhotoDB.setPhoto_type(18);
                stackPhotoDB.tovar_id = String.valueOf(tar.refId);
            }else {
                stackPhotoDB.setPhoto_type(0);
            }

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


    /**
     * 15.02.23.
     * Ну это ***. Сделано на ***, как и всё тут. Главное "что б быстро"
     *
     * На момент написания - сохраняет фотографию в БД. Фото берётся из Комментариев отписания на Задачи / Рекламации
     * */
    private StackPhotoDB saveTestPhoto(File photoFile, AddressSDB addr, CustomerDB client, TasksAndReclamationsSDB tar) {
        try {
            int id = RealmManager.stackPhotoGetLastId();
            id++;
            StackPhotoDB stackPhotoDB = new StackPhotoDB();
            stackPhotoDB.setId(id);
            stackPhotoDB.setDt(System.currentTimeMillis() / 1000);
            stackPhotoDB.setTime_event(Clock.getHumanTime2(System.currentTimeMillis() / 1000));

            stackPhotoDB.setAddr_id(addr.id);
            stackPhotoDB.setAddressTxt(addr.nm);

            stackPhotoDB.setClient_id(client.getId());
            stackPhotoDB.setCustomerTxt(client.getNm());

            stackPhotoDB.setUser_id(Globals.userId);
            if (tar.themeId == 150){
                stackPhotoDB.setPhoto_type(18);
                stackPhotoDB.tovar_id = String.valueOf(tar.refId);
            }else {
                stackPhotoDB.setPhoto_type(0);
            }

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


    /**
     * 08.12.23. Сохраняю фото с галереи
     * */
    private StackPhotoDB savePhoto(File file, TasksAndReclamationsSDB tasksAndReclamationsSDB, String tovarId, Context context) {
        try {
            int id = RealmManager.stackPhotoGetLastId();
            id++;
            StackPhotoDB stackPhotoDB = new StackPhotoDB();
            stackPhotoDB.setId(id);
            stackPhotoDB.setDt(tasksAndReclamationsSDB.dt);
            stackPhotoDB.setTime_event(Clock.getHumanTimeSecPattern(tasksAndReclamationsSDB.dt, "yyyy-MM-dd"));

            stackPhotoDB.setAddr_id(tasksAndReclamationsSDB.addr);
            stackPhotoDB.setAddressTxt(tasksAndReclamationsSDB.addrNm);

            stackPhotoDB.setClient_id(tasksAndReclamationsSDB.client);
            stackPhotoDB.setCustomerTxt(tasksAndReclamationsSDB.clientNm);

            stackPhotoDB.code_dad2 = tasksAndReclamationsSDB.codeDad2;

            // 10.09.23. Походу Сервер без этого не воспринимает фотографии (сейчас касается это // Тип фото Остатков из ГАЛЕРЕИ)
            LogMPDB log = Globals.fixMP(null, null);
            String GP = log != null ? log.gp : "";
            stackPhotoDB.gp = GP;

            stackPhotoDB.setUser_id(Globals.userId);
            stackPhotoDB.setUserTxt(SQL_DB.usersDao().getUserName(Globals.userId));
            stackPhotoDB.setPhoto_type(18);      // Фото Товара
            stackPhotoDB.tovar_id = String.valueOf(tasksAndReclamationsSDB.refId);

            stackPhotoDB.setCreate_time(System.currentTimeMillis());

//            String hash = globals.getHashMD5FromFileTEST(uri, context);
            String hash = globals.getHashMD5FromFile2(file, this);
            if (hash == null || hash.equals("")) hash = globals.getHashMD5FromFile(file, this);
            Globals.writeToMLOG("INFO", "TARActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST", "hash: " + hash);

            if (hash == null || hash.equals("")) {
                hash = globals.getHashMD5FromFile(file, this);
            }

            stackPhotoDB.setPhoto_hash(hash);
            stackPhotoDB.setPhoto_num(file.getAbsolutePath());

            String jo = new Gson().toJson(stackPhotoDB);
            Globals.writeToMLOG("INFO", "TARActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST", "stackPhotoDB: " + jo);

            RealmManager.stackPhotoSavePhoto(stackPhotoDB);
            Toast.makeText(context, "Фото (" + id + ") з Галереї успішно збережено!", Toast.LENGTH_LONG).show();
            return stackPhotoDB;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "TARActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST", "Exception e: " + e);
            return null;
        }
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



    /*
                        case 3:
                            try {
                                MakePhotoFromGaleryTasksAndReclamationsSDB = ;
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.setType("image/*");
                                Globals.writeToMLOG("INFO", "TARActivity/Intent.ACTION_PICK", "intent: " + intent);
                                ((TARActivity) v.getContext()).startActivityForResult(Intent.createChooser(intent, "Select Picture"), 500);
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "TARActivity/Intent.ACTION_PICK", "Exception e: " + e);
                            }
                            break;
*/
}
