package ua.com.merchik.merchik.Activities.ReferencesActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import ua.com.merchik.merchik.Activities.ReferencesActivity.Chat.ChatGrpAdapter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.Utils.UniversalAdapter.AdapterUtil;
import ua.com.merchik.merchik.Utils.UniversalAdapter.UniversalAdapterData;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.toolbar_menus;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class ReferencesActivity extends toolbar_menus {
    Globals globals = new Globals();
    AdapterUtil adapter;
    UniversalAdapterData data = new UniversalAdapterData();

    TextView activity_title;
    RecyclerView recycler;

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

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        activity_title = (TextView) findViewById(R.id.activity_title);

        initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));


        setData();
        setModuleData();
    }

    private void setData() {
        Log.e("navigationmenuclick", "setData1");
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
                case ADDRESS:
//                    data.addressDBList = AddressRealm.getAll();
                    Log.e("navigationmenuclick", "getModuleTitle1");
                    data.address = SQL_DB.addressDao().getAll();
                    Log.e("navigationmenuclick", "getModuleTitle2");
//                    return "Адреса (" +data.addressDBList.size()+ ")";
                    return "Адреса (" + data.address.size() + ")";

                case USERS:
                    CompositeDisposable disposable = new CompositeDisposable();
                    ProgressDialog pg = ProgressDialog.show(this, "Отображение данных", "Подождите пока данные подготовятся", true, true);
                    disposable.add(SQL_DB.usersDao().getAll()
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
     *
     *
     * По мере добавления - переходить на этот вариант обработки.
     * */
    private void setModuleData(){
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
     * */
    private ChatGrpAdapter chatAdapter;
    private void setChatData(){
        SQL_DB.chatGrpDao().getAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    chatAdapter = new ChatGrpAdapter(result, this);
                    recycler.setAdapter(chatAdapter);
                    activity_title.setText("Справочник: " + "Чаты (" + result.size() + ")");
                });

    }

}
