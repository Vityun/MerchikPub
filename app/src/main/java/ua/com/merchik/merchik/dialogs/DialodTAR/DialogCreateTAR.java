package ua.com.merchik.merchik.dialogs.DialodTAR;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.AddressDB;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.TestViewHolderData;
import ua.com.merchik.merchik.database.realm.tables.AddressRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class DialogCreateTAR extends DialogData {

    private Context context;

    private Dialog dialog;

    private TextView title;

    private ImageButton imgBtnClose;
    private ImageButton imgBtnLesson;
    private ImageButton imgBtnVideoLesson;
    private ImageButton imgBtnCall;

    private Button close;
    private Button save;

    public RecyclerView recyclerView;

    //-------------- Даные для кнопки Сохранить
    public StackPhotoDB photo;
    public AddressDB address;
    public CustomerDB customer;
    private ThemeDB theme;
    private OpinionSDB opinion;
    public String comment;
    private long dad2;


    private String MASSAGE;

    public DialogCreateTAR(Context context) {
        super(context);
        this.context = context;

        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_create_tar);
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.70);
        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        title = dialog.findViewById(R.id.title);

        imgBtnClose = dialog.findViewById(R.id.imageButtonClose);
        imgBtnLesson = dialog.findViewById(R.id.imageButtonLesson);
        imgBtnVideoLesson = dialog.findViewById(R.id.imageButtonVideoLesson);
        imgBtnCall = dialog.findViewById(R.id.imageButtonCall);

        recyclerView = dialog.findViewById(R.id.recycler);

        save = dialog.findViewById(R.id.save);
        close = dialog.findViewById(R.id.close);

//        clickSave();
    }
    //----------------------------------------------------------------------------------------------

    public void setClose(DialogClickListener clickListener) {
        imgBtnClose.setOnClickListener(v -> {
            clickListener.clicked();
        });
        close.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }

    public void show() {
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
    }

    public void refreshAdaper(StackPhotoDB photo){
        Log.d("test", "data: " + adapter.data);

        this.photo = photo;

        adapter.data.get(0).photo = photo;
        adapter.notifyDataSetChanged();
    }

    //----------------------------------------------------------------------------------------------

    public void setTitle(String title){
        this.title.setText(title);
    }


    public CreateTARAdapter adapter;
    public void setRecyclerView(Clicks.clickVoid click) {
        adapter = new CreateTARAdapter(context, setViewHoldersData(), data -> {

            try {
                if (data.type == 1){
                    click.click();
                }
            }catch (Exception e){
                // todo ПОПРАВИТЬ, ТАК НЕ ДОЛЖНО БЫТЬ
            }


            if (data.photo != null){
                photo = data.photo;
                Toast.makeText(context, "Фото: " + photo, Toast.LENGTH_SHORT).show();
            }

            if (data.address != null){
                address = data.address;
            }

            if (data.customer != null){
                customer = data.customer;
            }

            if (data.theme != null){
                theme = data.theme;
            }

            if (data.opinion != null){
                opinion = data.opinion;
            }

            if (data.comment != null){
                comment = data.comment;
            }


            Log.e("TEST_CLICK", "photo: " + photo);
            Log.e("TEST_CLICK", "address: " + address);
            Log.e("TEST_CLICK", "customer: " + customer);
            Log.e("TEST_CLICK", "theme: " + theme);
            Log.e("TEST_CLICK", "opinion: " + opinion);
            Log.e("TEST_CLICK", "comment: " + comment);

            Log.e("TEST_CLICK", "Test_1: " + data.testSpinner1);
            Log.e("TEST_CLICK", "Test_2: " + data.testSpinner2);

        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
    }


    /** 28.10.2021
     * Постараюсь сделать настраиваемый модуль*/
    private List<TestViewHolderData> viewHolderData = new ArrayList<>();
    public void serCustomRecyclerView(Clicks.click click){
        adapter = new CreateTARAdapter(context, viewHolderData, data -> {

            Log.e("test", "data test: " + data);

            if (data.photo != null){
                photo = data.photo;
                Toast.makeText(context, "Фото: " + photo, Toast.LENGTH_SHORT).show();
            }

            if (data.address != null){
                address = data.address;
            }

             if (data.customer != null){
                customer = data.customer;
            }

            if (data.theme != null){
                theme = data.theme;
            }

            if (data.opinion != null){
                opinion = data.opinion;
            }

            if (data.comment != null){
                comment = data.comment;
            }

            try {
                if (data.type == 1){
                    click.click(data.type);
                    return;
                }

                if (data.type == 2){
                    click.click(data.type);
                    return;
                }
            }catch (Exception e){
                // todo ПОПРАВИТЬ, ТАК НЕ ДОЛЖНО БЫТЬ
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
    }

    /**28.10.2021
     * Добавление фото и инфы к нему
     * */
    public void addPhoto(String msg){
        TestViewHolderData res = new TestViewHolderData();
        res.typeNumber = 0;
        res.msg = msg;
        viewHolderData.add(res);
    }

    /**28.10.2021
     * Добавление поля для ввода и подсказки к нему
     * */
    public void addEditText(String msg){
        TestViewHolderData res = new TestViewHolderData();
        res.typeNumber = 2;
        res.msg = msg;
        viewHolderData.add(res);
    }

    /**
     * Получение и формирование данных для ресайклера
     */
    private List<TestViewHolderData> setViewHoldersData() {
        List<TestViewHolderData> res = new ArrayList<>();

        res.add(getPhotoInfo());

        res.add(getAutoText(Globals.NewTARDataType.ADDRESS));
        res.add(getAutoText(Globals.NewTARDataType.CUSTOMER));
        res.add(getAutoText(Globals.NewTARDataType.THEME));
        res.add(getAutoText(Globals.NewTARDataType.OPINION));

        res.add(getEditText());

        res.add(getButton());

//        res.add(getSpinner("тест спинер №1"));
//        res.add(getSpinner("тест спинер №2"));

        return res;
    }


    /**
     * Получение данных для Фото и короткой инфы о нём
     */
    /*Тип 0 это: ФОТО*/
    private TestViewHolderData getPhotoInfo() {
        TestViewHolderData res = new TestViewHolderData();

        res.typeNumber = 0;
//        res.msg = "Какой-то текст к фотке\nЭто вторая строка текста\nЕщё одна строка, последняя\nЧетвёртая строчечка";
        res.msg = MASSAGE;

        return res;
    }


    /**
     * Формирвание данных для
     */
    /*Тип 1 это: АВТОТЕКСТ*/
    private TestViewHolderData getAutoText(Globals.NewTARDataType type) {
        TestViewHolderData res = new TestViewHolderData();

        res.typeNumber = 1;
        res.type = type;

        switch (type) {
            case ADDRESS:
                res.msg = "Начните вносить адресс";
                if (address != null){
                    res.addressList = Collections.singletonList(address);
                }else {
                    res.addressList = AddressRealm.getAll();
                }
                return res;

            case CUSTOMER:
                res.msg = "Начните вносить клиента";
                if (customer != null){
                    res.customerList = Collections.singletonList(customer);
                }else {
                    res.customerList = CustomerRealm.getAll();
                }

                return res;

            case THEME:
                res.msg = "Начните вносить тему";
                res.themeList = ThemeRealm.getTARTheme();
                return res;

            case OPINION:
                res.msg = "Начните вносить мнение";
                List<OpinionSDB> test = SQL_DB.opinionDao().getAll();

                Log.e("OPINIONS_TAR", "test: " + test.size());

                res.opinionList = test;
                return res;
        }


        return res;
    }


    /*Тип 2 это: ПОЛЕ ДЛЯ ВНОСА ТЕКСТА*/
    private TestViewHolderData getEditText() {
        TestViewHolderData res = new TestViewHolderData();

        res.typeNumber = 2;
        res.msg = "Внесите комментарий";

        return res;
    }


    /*Тип3 это: КНОПКА*/
    private TestViewHolderData getButton() {
        TestViewHolderData res = new TestViewHolderData();

        res.typeNumber = 3;
        res.msg = "История";

        return res;
    }


    private TestViewHolderData getSpinner(String text){
        TestViewHolderData res = new TestViewHolderData();

        res.typeNumber = 4;
        res.msg = text;

        return res;
    }

    public void clickSave(Clicks.clickVoid click, int mode){
        save.setOnClickListener(v -> {


            if (mode == 1 && address == null){
                Toast.makeText(v.getContext(), "Не внесли Адрес. Укажите, пожалуйста, Адрес", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mode == 1 && customer == null){
                Toast.makeText(v.getContext(), "Не внесли Клиента. Укажите, пожалуйста, Клиента", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mode == 1 && theme == null){
                Toast.makeText(v.getContext(), "Не внесли Тему. Укажите, пожалуйста, Тему", Toast.LENGTH_SHORT).show();
                return;
            }

//            if (opinion == null){
//                Toast.makeText(context, "Не внесли Мнение. Укажите, пожалуйста, Мнение", Toast.LENGTH_SHORT).show();
//                return;
//            }



            if (mode == 2 && comment == null){
                Toast.makeText(v.getContext(), "Не внесли Комментарий. Укажите, пожалуйста, Комментарий", Toast.LENGTH_SHORT).show();
                return;
            }


//            String id = String.valueOf(System.currentTimeMillis());

            if (mode == 1){
                TasksAndReclamationsSDB task = new TasksAndReclamationsSDB();
//            task.id = System.currentTimeMillis()/1000;
                task.tp = 1;
                task.state = 0;
                task.dt = System.currentTimeMillis()/1000;
                task.vinovnik = Globals.userId;
                task.codeDad2SrcDoc = this.dad2;
                if (photo!=null){
                    task.photo = photo.getId();
                }
                if (address != null)
                    task.addr = address.getAddrId();
                if (customer != null)
                    task.client = customer.getId();
                if (theme != null)
                    task.themeId = Integer.valueOf(theme.getID());
                if (opinion != null) {
                    task.sotrOpinionId = opinion.id;
                }
                task.comment = comment;

                SQL_DB.tarDao().insertData(Collections.singletonList(task))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Log.d("test", "here");
//                            click.click();
                                dialog.dismiss();
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                Log.d("test", "Throwable e: " + e);
                                Globals.writeToMLOG("INFO_ERR", "Save TAR in DialogCreateTAR", "Throwable e: " + e);
//                            click.click();
                                dialog.dismiss();
                            }
                        });

                click.click();

                Toast.makeText(v.getContext(), "ЗиР создан", Toast.LENGTH_SHORT).show();
            }


            if (mode == 2){
                click.click();
            }
        });
    }


    public void setData(int addr, String cust, long dad2, StackPhotoDB photoDB) {
        address = AddressRealm.getAddressById(addr);
        customer = CustomerRealm.getCustomerById(cust);
        this.dad2 = dad2;
        MASSAGE = "Код ДАД2: " + dad2;
        this.photo = photoDB;
    }

    public void setDataUpdate(){
        adapter.data.get(1).addressList = Collections.singletonList(address);
        adapter.data.get(2).customerList = Collections.singletonList(customer);
    }
}
