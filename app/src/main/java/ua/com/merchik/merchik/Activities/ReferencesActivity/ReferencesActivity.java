package ua.com.merchik.merchik.Activities.ReferencesActivity;

import static ua.com.merchik.merchik.Globals.userId;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import ua.com.merchik.merchik.Activities.ReferencesActivity.Chat.ChatGrpAdapter;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.Utils.UniversalAdapter.AdapterUtil;
import ua.com.merchik.merchik.Utils.UniversalAdapter.UniversalAdapterData;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.dialogs.BlockingProgressDialog;
import ua.com.merchik.merchik.toolbar_menus;

public class ReferencesActivity extends toolbar_menus {
    Globals globals = new Globals();
    AdapterUtil adapter;
    private UniversalAdapterData data = new UniversalAdapterData();

    TextView activity_title;
    RecyclerView recycler;

    private FloatingActionButton floatingActionButtonReferences;

    Globals.ReferencesEnum referencesEnum;

    //=====================
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.drawler_references);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        referencesEnum = (Globals.ReferencesEnum) this.getIntent().getSerializableExtra("ReferencesEnum");

        floatingActionButtonReferences = findViewById(R.id.fabReferences);

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        activity_title = (TextView) findViewById(R.id.activity_title);

        initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));


        setData();
        setModuleData();
    }

    private void setData() {
        Log.e("navigationmenuclick", "setData1");
        Log.e("navigationmenuclick", "activity_title: " + activity_title);
        Log.e("navigationmenuclick", "adapter: " + adapter);
        activity_title.setText("Справочник: " + getModuleTitle());
        Log.e("navigationmenuclick", "setData2");


        adapter = new AdapterUtil(this, data, referencesEnum);
        Log.e("navigationmenuclick", "setData3");
        recycler.setAdapter(adapter);
        Log.e("navigationmenuclick", "setData4");
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        Log.e("navigationmenuclick", "setData5");
    }


    public String getModuleTitle() {
        String res = "";
        if (referencesEnum != null) {
            switch (referencesEnum) {
                case ACHIEVEMENTS:
                    Long dateFrom = Clock.getDatePeriodLong(System.currentTimeMillis(), -30) / 1000;
                    Long dateTo = Clock.getDatePeriodLong(System.currentTimeMillis(), 2) / 1000;

                    data.achievementsSDBS = SQL_DB.achievementsDao().getList(dateFrom, dateTo, Globals.userId);

                    floatingActionButtonReferences.setVisibility(View.VISIBLE);
                    floatingActionButtonReferences.setOnClickListener(v->{
                        String link = String.format("/mobile.php?mod=images_achieve**act=list_achieve**date_from=%s**date_to=%s", dateFrom, dateTo);
                        AppUsersDB appUser = AppUserRealm.getAppUserById(userId);

                        String hash = String.format("%s%s%s", appUser.getUserId(), appUser.getPassword(), "AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3");
                        hash = Globals.getSha1Hex(hash);

                        String format = String.format("https://merchik.com.ua/sa.php?&u=%s&s=%s&l=%s", userId, hash, link);

                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(format));
                        this.startActivity(browserIntent);
                    });
                    Log.e("test", "data: " + data);
                    Log.e("test", "data.achievementsSDBS: " + data.achievementsSDBS);
                    Log.e("test", "data.achievementsSDBS.s: " + data.achievementsSDBS.size());
                    return "Досягнення (" + data.achievementsSDBS.size() + ")";
                case ADDRESS:
//                    data.addressDBList = AddressRealm.getAll();
                    Log.e("navigationmenuclick", "getModuleTitle1");
                    data.address = SQL_DB.addressDao().getAll();
                    Log.e("navigationmenuclick", "getModuleTitle2");
//                    return "Адреса (" +data.addressDBList.size()+ ")";
                    return "Адреса (" + data.address.size() + ")";

                case USERS:
                    CompositeDisposable disposable = new CompositeDisposable();
                    BlockingProgressDialog pg = BlockingProgressDialog.show(this, "Отображение данных", "Подождите пока данные подготовятся");
                    disposable.add(SQL_DB.usersDao().getAllSortedFIO(Globals.userId)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<List<UsersSDB>>() {
                                @Override
                                public void accept(List<UsersSDB> res) throws Throwable {
                                    Log.e("navigationmenuclick", "USERS.count: " + res.size());
                                    UniversalAdapterData uData = new UniversalAdapterData();
                                    uData.users = res;
                                    adapter.refresh(uData);

                                    if (pg != null)
                                        if (pg.isShowing())
                                            pg.dismiss();

                                    disposable.dispose();

                                    activity_title.setText("Справочник: " + "Сотрудники (" + res.size() + ")");
                                }
                            })
                    );

                    Log.e("navigationmenuclick", "USERS.end1");
                    return "Сотрудники (" + count + ")";

                case CUSTOMER:
//                    data.customerDBList = CustomerRealm.getAll();
                    Log.e("navigationmenuclick", "getModuleTitle1");
                    data.customers = SQL_DB.customerDao().getAll();
                    Log.e("navigationmenuclick", "getModuleTitle2");
//                    return "Клиенты (" + data.customerDBList.size() + ")";
                    return "Клиенты (" + data.customers.size() + ")";

/*                case CHAT:
                    int chatSize = 0;

                    SQL_DB.chatGrpDao().getAll()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(res1 -> {
                                UniversalAdapterData uData = new UniversalAdapterData();
                                uData.chats = res1;
                                adapter.refresh(uData);
                                activity_title.setText("Справочник: " + "Чаты (" + res1.size() + ")");
                            });

//                    SQL_DB.chatDao().getAll()
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(res1 -> {
//                                UniversalAdapterData uData = new UniversalAdapterData();
//                                uData.chats = res1;
//                                adapter.refresh(uData);
//                                activity_title.setText("Справочник: " + "Чаты (" + res1.size() + ")");
//                            });


                    return "Чаты (" + chatSize + ")";*/
            }
        }

        return res;
    }

    /**
     * 15.11.22.
     * На момент 15.11.22. Обновлён только раздел ЧАТЫ.
     * <p>
     * <p>
     * По мере добавления - переходить на этот вариант обработки.
     */
    private void setModuleData() {
        if (referencesEnum != null) {
            switch (referencesEnum) {
                case CHAT:  // Если при открытии данного раздела у нас выяснилось что это раздел ЧАТЫ
                    setChatData();
                    break;
            }
        }
    }

    /**
     * 15.11.22.
     * Тут я буду заполнять Активность в соответствии с данными Чата
     */
    private ChatGrpAdapter chatAdapter;

    private void setChatData() {

        BlockingProgressDialog pg = BlockingProgressDialog.show(this, "Почекайте");

        try {
            Log.d("chat_grp_time", "insertInTemp time start");
            SQL_DB.chatGrpDao().getTempChatGrp().observeOn(AndroidSchedulers.mainThread())  // Формирую временную табличку
                    .subscribe(result -> {
                                Log.d("chat_grp_time", "get temp table result");
                                SQL_DB.chatGrpDao().insertInTemp(result)                    // Записываю в неё данные
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new DisposableCompletableObserver() {
                                            @Override
                                            public void onComplete() {
                                                Log.d("chat_grp_time", "insert in temp table result");
                                                SQL_DB.chatGrpDao().getChatGrpJoinedTemp().observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(result -> {
                                                            Log.d("chat_grp_time", "result adapter data: " + result);
                                                            if (pg.isShowing()) pg.dismiss();
                                                            chatAdapter = new ChatGrpAdapter(result, ReferencesActivity.this);
                                                            recycler.setAdapter(chatAdapter);
                                                            activity_title.setText("Справочник: " + "Чаты (" + result.size() + ")");
                                                        });
                                            }

                                            @Override
                                            public void onError(@NonNull Throwable e) {
                                                Log.d("chat_grp_time", " error insert in temp table result: " + e);
                                                if (pg.isShowing()) pg.dismiss();
                                            }
                                        });
                            }
                    );

        } catch (Exception e) {
            Log.d("chat_grp_time", "Exception e: " + e);
        }

/*        Log.d("chat_grp_time", "time start");
        SQL_DB.chatGrpDao().getAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.d("chat_grp_time", "time result");
                    chatAdapter = new ChatGrpAdapter(result, this);
                    recycler.setAdapter(chatAdapter);
                    activity_title.setText("Справочник: " + "Чаты (" + result.size() + ")");
                });*/

    }

}
