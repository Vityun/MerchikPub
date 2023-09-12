package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.checkVideos;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.imageView;
import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.DEFAULT;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.Utils.CustomRecyclerView;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.ViewListSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.OptionsServer;
import ua.com.merchik.merchik.data.RetrofitResponse.ReportPrepareServer;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.PPADBRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogVideo;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

@SuppressLint("ValidFragment")
public class DetailedReportTovarsFrag extends Fragment {

    public static final Integer[] DETAILED_REPORT_FRAGMENT_TOVAR_VIDEO_LESSONS = new Integer[]{823, 815};

    private static Context mContext;
    private ArrayList<Data> list;
    private WpDataDB wpDataDB;
    private TasksAndReclamationsSDB tasksAndReclamationsSDB;

    private long codeDad2;
    private String clientId;
    private int addressId;

    private EditText editText;
    private TextView allTov;
    //    private RecyclerView recyclerView;
    private CustomRecyclerView recyclerView;
    private ImageView fullTovList, filter;
    private FloatingActionButton fab;
    private TextView badgeTextView;

    RecycleViewDRAdapterTovar adapter;

    private boolean flag = true;

    public DetailedReportTovarsFrag() {
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/1", "create");
    }

    public DetailedReportTovarsFrag(Context context, ArrayList<Data> list, WpDataDB wpDataDB) {
        // Required empty public constructor
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/2", "create");
        this.mContext = context;
        this.list = list;
        this.wpDataDB = wpDataDB;

        this.codeDad2 = wpDataDB.getCode_dad2();
        this.clientId = wpDataDB.getClient_id();
        this.addressId = wpDataDB.getAddr_id();
    }

    public static DetailedReportTovarsFrag newInstance(AppCompatActivity context, ArrayList<Data> list, WpDataDB wpDataDB) {
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/newInstance", "DetailedReportTovarsFrag newInstance");
        DetailedReportTovarsFrag fragment = new DetailedReportTovarsFrag();
        Bundle args = new Bundle();
        args.putParcelableArrayList("list", list);
        args.putParcelable("wpDataDB", wpDataDB);
//        args.putSerializable("appCompatActivity", (Serializable) context);// Передача AppCompatActivity в аргументах
        mContext = context;
        fragment.setArguments(args);
        return fragment;
    }

    public static DetailedReportTovarsFrag newInstance(Context context, TasksAndReclamationsSDB tasksAndReclamationsSDB) {
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/newInstance", "DetailedReportTovarsFrag newInstance");
        DetailedReportTovarsFrag fragment = new DetailedReportTovarsFrag();
        Bundle args = new Bundle();
        args.putParcelable("tasksAndReclamationsSDB", tasksAndReclamationsSDB);
        mContext = context;
        fragment.setArguments(args);
        return fragment;
    }

    public DetailedReportTovarsFrag(Context context, TasksAndReclamationsSDB tasksAndReclamationsSDB) {
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/3", "create");
        this.mContext = context;
        this.tasksAndReclamationsSDB = tasksAndReclamationsSDB;
        this.codeDad2 = tasksAndReclamationsSDB.codeDad2SrcDoc;
        this.clientId = tasksAndReclamationsSDB.client;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/onSaveInstanceState", "outState: " + outState);
        try {
            ArrayList<Parcelable> parcelableList = new ArrayList<>();
            for (Data data : list) {
                parcelableList.add(data);
            }
            outState.putParcelableArrayList("list", parcelableList);
            outState.putParcelable("wpDataDB", wpDataDB);
            outState.putParcelable("tasksAndReclamationsSDB", tasksAndReclamationsSDB);
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/onSaveInstanceState", "Exception e: " + e);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/onViewStateRestored", "savedInstanceState: " + savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<Parcelable> parcelableList = savedInstanceState.getParcelableArrayList("list");
            if (parcelableList != null) {
                list = new ArrayList<>();
                for (Parcelable parcelable : parcelableList) {
                    if (parcelable instanceof Data) {
                        list.add((Data) parcelable);
                    }
                }
            }
            wpDataDB = savedInstanceState.getParcelable("wpDataDB");
            tasksAndReclamationsSDB = savedInstanceState.getParcelable("tasksAndReclamationsSDB");
//            mContext = (AppCompatActivity) savedInstanceState.getSerializable("appCompatActivity");
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onCreate");
        Bundle args = getArguments();
        if (args != null) {
//            mContext = (AppCompatActivity) args.getSerializable("appCompatActivity");
//            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onCreate/mContext: " + mContext);
            list = args.getParcelableArrayList("list");
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onCreate/list: " + list);
            wpDataDB = args.getParcelable("wpDataDB");
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onCreate/wpDataDB: " + wpDataDB);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onDetach");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/onCreateView", "inflater: " + inflater);
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/onCreateView", "container: " + container);
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/onCreateView", "create: " + savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_dr_tovar, container, false);

        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/onCreateView", "v: " + v);

        try {

            // TODO 10.09.23. могут быть проблемы с Товарами в ЗИР изза этого. Проблемы будущего меня
            if (wpDataDB != null){
                this.codeDad2 = wpDataDB.getCode_dad2();
                this.clientId = wpDataDB.getClient_id();
                this.addressId = wpDataDB.getAddr_id();
            }


            badgeTextView = v.findViewById(R.id.badge_text_view);
            fab = v.findViewById(R.id.fab);
            editText = (EditText) v.findViewById(R.id.drEditTextFindTovar);
            fullTovList = v.findViewById(R.id.full_tov_list);
            filter = v.findViewById(R.id.filter);

            recyclerView = v.findViewById(R.id.DRRecyclerViewTovar);
            allTov = v.findViewById(R.id.textLikeLink);

            setPopup();

            downloadData();
            addRecycleView(getTovListNew(TovarDisplayType.DETAILED_REPORT));

            setFabVideo(v.getContext(), this::showYouTubeFab);
            showYouTubeFab();

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/onCreateView", "Exception e: " + e);
        }
        return v;
    }

    private void downloadData(){
        List<TovarDB> res = new ArrayList<>();
        res = RealmManager.INSTANCE.copyFromRealm(Objects.requireNonNull(RealmManager.getTovarListFromReportPrepareByDad2(codeDad2)));
        if (res.size() == 0) {
            downloadDetailedReportTovarsData(TovarDisplayType.DETAILED_REPORT, new Clicks.clickStatusMsg() {
                @Override
                public void onSuccess(String data) {
                    Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();
                    Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList.onSuccess", "String data: " + data);
                    addRecycleView(getTovListNew(TovarDisplayType.DETAILED_REPORT));
                    updateTov = false;
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
                    Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList.onSuccess", "String error: " + error);
                    updateTov = false;
                }
            });
        }
    }

    private void setFabVideo(Context context, Clicks.clickVoid click){
        fab.setOnClickListener(view -> {
            DialogVideo dialogVideo = new DialogVideo(context);
            dialogVideo.setTitle("Перелік відео уроків");
            dialogVideo.setVideos(getSiteHints(DETAILED_REPORT_FRAGMENT_TOVAR_VIDEO_LESSONS), click);
            dialogVideo.setClose(dialogVideo::dismiss);
            dialogVideo.show();
        });
    }

    private void showYouTubeFab(){
        List<ViewListSDB> videos = checkVideos(DETAILED_REPORT_FRAGMENT_TOVAR_VIDEO_LESSONS, ()->{});
        if (videos.size() >= DETAILED_REPORT_FRAGMENT_TOVAR_VIDEO_LESSONS.length){
            fab.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            badgeTextView.setVisibility(View.GONE);
        }else {
            fab.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            int must = DETAILED_REPORT_FRAGMENT_TOVAR_VIDEO_LESSONS.length;
            int have = videos.size();
            int res = must - have;
            badgeTextView.setText("" + res);
        }
    }

    private List<SiteHintsDB> getSiteHints(Integer[] integers){
        List<SiteObjectsDB> siteObjects = RealmManager.getLesson(integers);
        List<SiteHintsDB> data = null;
        try {
            if (siteObjects != null) {
                Integer[] siteObjectIds = new Integer[siteObjects.size()];
                for (int i = 0; i < siteObjects.size(); i++) {
                    int lessId = Integer.parseInt(siteObjects.get(i).getLessonId());
                    if (lessId != 0) siteObjectIds[i] = lessId;
                }
                data = RealmManager.getVideoLesson(siteObjectIds);
                if (data != null) Collections.reverse(data);
            }
        } catch (Exception e) {
            Log.e("setVideoLesson", "Exception e: " + e);
        }

        return data;
    }


    /**
     * 22.01.2021
     */
    private void setTextLikeLink() {
        filter.setOnClickListener(v -> {
            // TODO ВСТАВИТЬ ДИАЛОГ С ФИЛЬТРОМ
        });
        Log.e("setTextLikeLink", "Here");

        fullTovList.setRotation(45);
        fullTovList.setOnClickListener((v) -> {
            try {
                if (!flag) {
                    Log.e("setTextLikeLink", "!flag");
                    fullTovList.setRotation(45);
                    fullTovList.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.ic_letter_x));
                    DetailedReportActivity.additionalRequirementsFilter = true;
                } else {
                    Log.e("setTextLikeLink", "flag");
                    fullTovList.setRotation(0);
                    fullTovList.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.ic_minus));
                    DetailedReportActivity.additionalRequirementsFilter = false;
                }
                flag = !flag;
//                addRecycleView(getTovList());
                addRecycleView(getTovListNew(TovarDisplayType.DETAILED_REPORT));
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "", "Exception e: " + e);
            }
        });
    }


    /**
     * 03.04.23.
     * Делаю так что-б выпадал список, при клике на плюсик.
     */
    private void setPopup() {
        fullTovList.setRotation(45);
        fullTovList.setOnClickListener(view -> {
            showPopup(view);
        });
    }

    /**
     * 03.04.23.
     * Отображаю попап
     *
     * @param view
     */
    private void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.popup_dr_tovar_list);

        popupMenu.setOnMenuItemClickListener(item -> {
            CustomerSDB customerSDB = null;
            try {
                customerSDB = SQL_DB.customerDao().getById(wpDataDB.getClient_id());
            } catch (Exception e) {
                // Обработка исключения при получении по идентификатору из wpDataDB
            }

            if (customerSDB == null) {
                try {
                    customerSDB = SQL_DB.customerDao().getById(tasksAndReclamationsSDB.client);
                } catch (Exception e) {
                    // Обработка исключения при получении по идентификатору из tasksAndReclamationsSDB
                }
            }


            DialogData dialogData = new DialogData(getContext());
            dialogData.setTitle("Увага");
            dialogData.setText(Html.fromHtml("<font color='RED'>По даному замовнику КАТЕГОРИЧНО ЗАБОРОНЕНО додавати товари! <br><br>Відмовитися від додавання товарів?</font>"));
            dialogData.setDialogIco();
            dialogData.setOk("Так", () -> {
            });


            List<TovarDB> tovarDBList;
            switch (item.getItemId()) {
                case R.id.popup_dr:
                    tovarDBList = getTovListNew(TovarDisplayType.DETAILED_REPORT);
                    Toast.makeText(getContext(), "Видалено зайві товари.(" + tovarDBList.size() + ")", Toast.LENGTH_SHORT).show();
                    addRecycleView(tovarDBList);
                    return true;

                case R.id.popup_ppa:
                    tovarDBList = getTovListNew(TovarDisplayType.PPA);

                    if (customerSDB != null && customerSDB.ppaAuto == 1 && !customerSDB.id.equals("9382") && !customerSDB.id.equals("32246")) { // 9382 - Витмарк, 32246 - Ласунка
                        dialogData.setCancel("Ні", () -> {
                            Toast.makeText(getContext(), "Додано товари з ППА. (" + tovarDBList.size() + ")", Toast.LENGTH_SHORT).show();
                            addTovarToRecyclerView(tovarDBList);
                            dialogData.dismiss();
                        });
                        dialogData.setClose(dialogData::dismiss);
                        dialogData.show();
                    } else {
                        Toast.makeText(getContext(), "Додано товари з ППА. (" + tovarDBList.size() + ")", Toast.LENGTH_SHORT).show();
                        addTovarToRecyclerView(tovarDBList);
                    }
                    return true;

                case R.id.popup_all:
                    tovarDBList = getTovListNew(TovarDisplayType.ALL);

                    if (customerSDB != null && customerSDB.ppaAuto == 1 && !customerSDB.id.equals("9382") && !customerSDB.id.equals("32246")) {
                        dialogData.setCancel("Ні", () -> {
                            openAllTov(tovarDBList);
                            dialogData.dismiss();
                        });
                        dialogData.setClose(dialogData::dismiss);
                        dialogData.show();
                    } else {
                        openAllTov(tovarDBList);
                    }
                    return true;

                case R.id.popup_tov:
                    if (customerSDB != null && customerSDB.ppaAuto == 1 && !customerSDB.id.equals("9382") && !customerSDB.id.equals("32246")) {
                        dialogData.setCancel("Ні", () -> {
                            openOneTov();
                            dialogData.dismiss();
                        });
                        dialogData.setClose(dialogData::dismiss);
                        dialogData.show();
                    } else {
                        openOneTov();
                    }
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void openAllTov(List<TovarDB> tovarDBList) {
        Toast.makeText(getContext(), "Додано всі товари.(" + tovarDBList.size() + ")", Toast.LENGTH_SHORT).show();
        addRecycleView(tovarDBList);
    }

    private void openOneTov() {
        DialogData dialog = new DialogData(getContext());
        dialog.setTitle("Оберіть Товар");
        dialog.setText("");

        RecycleViewDRAdapterTovar adapter = new RecycleViewDRAdapterTovar(getContext(), getTovListNew(TovarDisplayType.ONE), wpDataDB, RecycleViewDRAdapterTovar.OpenType.DIALOG);
        adapter.elementClick(new Clicks.click() {
            @Override
            public <T> void click(T data) {
                TovarDB tov = (TovarDB) data;
                Toast.makeText(getContext(), "Додано товар: " + tov.getNm(), Toast.LENGTH_SHORT).show();
                addTovarToRecyclerView(Collections.singletonList(tov));
                dialog.dismiss();
            }
        });
        adapter.getFilter().filter(dialog.getEditTextSearchText());

        dialog.setEditTextSearch(adapter);
        dialog.setRecycler(adapter, new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        dialog.setClose(dialog::dismiss);

        dialog.show();
    }

    private enum TovarDisplayType {
        DETAILED_REPORT,
        PPA,
        ALL,
        ONE
    }

    /**
     * 03.04.23.
     * По новой формирую Товары
     */
    private List<TovarDB> getTovListNew(TovarDisplayType type) {
        List<TovarDB> res = new ArrayList<>();
        switch (type) {
            case DETAILED_REPORT:
                if (RealmManager.getTovarListFromReportPrepareByDad2(codeDad2) != null) {
                    res = RealmManager.INSTANCE.copyFromRealm(Objects.requireNonNull(RealmManager.getTovarListFromReportPrepareByDad2(codeDad2)));
                }
                return res;

            case PPA:
                res = PPADBRealm.getTovarListByPPA(clientId, addressId, null);
                return res;

            case ONE:
            case ALL:
            default:
                res = RealmManager.INSTANCE.copyFromRealm(RealmManager.getTovarListByCustomer(clientId));
                return res;
        }

    }


    /**
     * 10.04.23.
     * Отвечает за именно добавление данных в Товары
     */
    private void addTovarToRecyclerView(List<TovarDB> newTovarList) {
        LinkedList<TovarDB> data = new LinkedList<>(adapter.getAdapterDataList());

        Set<TovarDB> set = new LinkedHashSet<>(newTovarList);
        set.addAll(data);
        data.clear();
        data.addAll(set);

        adapter.updateAdapterData(data);
        adapter.notifyDataSetChanged();
    }


    /**
     * 22.01.2021
     * Устанавливаем адаптер
     */
    private void addRecycleView(List<TovarDB> list) {
        try {
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/List<TovarDB> list", "list: " + list);
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/List<TovarDB>", "list.size(): " + list.size());

            Log.e("DetailedReportTovarsF", "list.size(): " + list.size());


            Log.e("АКЦИЯ_ТОВАРА", "START");
            List<Integer> promotionalTov = new ArrayList<>();
            List<AdditionalRequirementsDB> data = null;

            try {
                if (wpDataDB != null) {
                    data = AdditionalRequirementsRealm.getData3(wpDataDB, DEFAULT, null, 0);
                } else {
                    data = AdditionalRequirementsRealm.getData3(tasksAndReclamationsSDB, DEFAULT, null, 0);
                }

                Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/AdditionalRequirementsDB", "data: " + data);

                for (AdditionalRequirementsDB item : data) {
                    if (item.getTovarId() != null && !item.getTovarId().equals("0") && !item.getTovarId().equals("")) {

                        long startDt = Clock.dateConvertToLong(item.getDtStart()) / 1000;
                        long endDt = Clock.dateConvertToLong(item.getDtEnd()) / 1000;

                        long docDt = wpDataDB.getDt().getTime() / 1000;
                        long docDtMinus2 = Clock.getDatePeriodLong(docDt, -2);
                        long docDtPlus1 = Clock.getDatePeriodLong(docDt, 1);

                        Log.e("AR_DATE", "item.getId(): " + item.getId());
                        Log.e("AR_DATE", "item.getDtStart(): " + item.getDtStart());
                        Log.e("AR_DATE", "item.getDtEnd(): " + item.getDtEnd());
                        Log.e("AR_DATE", "item.getTovarId(): " + item.getTovarId());
                        Log.e("AR_DATE", "Tovar: " + TovarRealm.getById(item.getTovarId()).getNm());


                        if ((startDt > 0 && endDt > 0 && docDtMinus2 < endDt) || (startDt > 0 && endDt == 0)){
                            promotionalTov.add(Integer.valueOf(item.getTovarId()));
                        }
                    }
                }
                Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/AdditionalRequirementsDB", "promotionalTov: " + promotionalTov);
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/addRecycleView/AdditionalRequirementsDB", "Exception e: " + e);
            }


            // TODO OH SHIT
//            RecycleViewDRAdapterTovar recycleViewDRAdapter;
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/RecycleViewDRAdapterTovar", "mContext: " + mContext);
            if (wpDataDB != null) {
                adapter = new RecycleViewDRAdapterTovar(mContext, list, wpDataDB, RecycleViewDRAdapterTovar.OpenType.DEFAULT);
            } else {
                adapter = new RecycleViewDRAdapterTovar(mContext, list, tasksAndReclamationsSDB, RecycleViewDRAdapterTovar.OpenType.DEFAULT);
            }
            adapter.setAkciyaTovList(promotionalTov, data);

            adapter.setTplType(RecycleViewDRAdapterTovar.DRAdapterTovarTPLTypeView.GONE);

//            RecycleViewDRAdapterTovar finalRecycleViewDRAdapter = recycleViewDRAdapter;
            adapter.refreshAdapter(() -> {
                adapter.switchTPLView();
                recyclerView.setAdapter(adapter);
//                rvTovar.setAdapter(finalRecycleViewDRAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
//                rvTovar.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                adapter.notifyDataSetChanged();
            });

            recyclerView.setAdapter(adapter);
//            rvTovar.setAdapter(recycleViewDRAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
//            rvTovar.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() != 0) {
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
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/addRecycleView/List<TovarDB> list", "Exception e: " + e);
        }
    }


    /**
     * 22.01.2021
     * Данные для адаптера
     * <p>
     * Отображает список товаров по ППА или весь список товаров по данному клиенту
     */
    private boolean updateTov = true;

    private ArrayList<TovarDB> getTovList() {
        ArrayList<TovarDB> list = null;
        if (flag) {
            List<TovarDB> dataTovar = RealmManager.getTovarListFromReportPrepareByDad2(codeDad2);
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList", "codeDad2: " + codeDad2);
            if (dataTovar != null && dataTovar.size() > 0) {
                list = (ArrayList<TovarDB>) RealmManager.INSTANCE.copyFromRealm(dataTovar);
                Toast.makeText(mContext, "Отображен список только тех товаров, которые установлены в матрице ППА", Toast.LENGTH_SHORT).show();
                Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList", "list: " + list.size());
            } else {
                Toast.makeText(mContext, "Список товаров по ППА пуст.", Toast.LENGTH_SHORT).show();

                if (updateTov && codeDad2 != 0) {
                    Log.d("test", "tovars is empty");
                    Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList", "tovars is empty");
/*                    downloadDetailedReportTovarsData(new Clicks.clickStatusMsg() {
                        @Override
                        public void onSuccess(String data) {
                            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();
                            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList.onSuccess", "String data: " + data);
//                            addRecycleView(getTovList());
                            addRecycleView(getTovListNew(TovarDisplayType.DETAILED_REPORT));
                            updateTov = false;
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
                            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList.onSuccess", "String error: " + error);
                            updateTov = false;
                        }
                    });*/
                }

            }
        } else {
            List<TovarDB> dataTovar = RealmManager.getTovarListByCustomer(clientId);
            list = (ArrayList<TovarDB>) RealmManager.INSTANCE.copyFromRealm(dataTovar);
            Toast.makeText(mContext, "Отображен список всех товаров", Toast.LENGTH_SHORT).show();
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList", "dataTovar list: " + list.size());
        }
        return list;
    }

    private void downloadDetailedReportTovarsData(TovarDisplayType type, Clicks.clickStatusMsg click) {
        try {
            ProgressDialog pg = ProgressDialog.show(mContext, "Загрузка списка товаров", "Подождите окончания загрузки. Это может занять время.", true, true);
            downloadReportPrepareByDad2(pg, click);

            ProgressDialog pg2 = ProgressDialog.show(mContext, "Загрузка списка опций", "Подождите окончания загрузки. Это может занять время.", true, true);
            downloadOptionByDad2(pg2, click);
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList/downloadDetailedReportTovarsData", "Exception e: " + e);
        }
    }

    private void downloadReportPrepareByDad2(ProgressDialog pg, Clicks.clickStatusMsg click) {
        StandartData standartData = new StandartData();
        standartData.mod = "report_prepare";
        standartData.act = "list_data";
        standartData.date_from = Clock.getDatePeriod(-30);
        standartData.date_to = Clock.getDatePeriod(1);
        standartData.code_dad2 = String.valueOf(codeDad2);

        Gson gson = new Gson();
        String json = gson.toJson(standartData);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<ReportPrepareServer> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_REPORT_PREPARE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<ReportPrepareServer>() {
            @Override
            public void onResponse(Call<ReportPrepareServer> call, Response<ReportPrepareServer> response) {
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().getState()) {
                                if (response.body().getList() != null && response.body().getList().size() > 0) {
                                    ReportPrepareRealm.setAll(response.body().getList());
                                    click.onSuccess("Товары успешно обновлены!");
                                } else {
                                    click.onFailure("Список Товаров пуст. Обратитесь к своему руководителю.");
                                }
                            } else {
                                if (response.body().getError() != null) {
                                    click.onFailure("Данные обновить не получилось. Это связанно с: " + response.body().getError());
                                } else {
                                    click.onFailure("Данные обновить не получилось. Ошибку обнаружить не получилось. Обратитесь к руководителю.");
                                }
                            }
                        } else {
                            click.onFailure("Запрашиваемых данных не обнаружено.");
                        }
                    } else {
                        click.onFailure("Произошла ошибка при обновлении списка товаров. Ошибка: " + response.code());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/downloadReportPrepareByDad2/onResponse", "Exception e: " + e);
                    click.onFailure("Произошла ошибка. Передайте её руководителю: " + e);
                }

                if (pg != null && pg.isShowing()) pg.dismiss();
            }

            @Override
            public void onFailure(Call<ReportPrepareServer> call, Throwable t) {
                if (pg != null && pg.isShowing()) pg.dismiss();
                Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/downloadReportPrepareByDad2/onFailure", "Throwable t: " + t);
                click.onFailure(t.toString());
            }
        });
    }

    private void downloadOptionByDad2(ProgressDialog pg, Clicks.clickStatusMsg click) {
        StandartData standartData = new StandartData();
        standartData.mod = "plan";
        standartData.act = "options_list";
        standartData.code_dad2 = String.valueOf(codeDad2);

        Gson gson = new Gson();
        String json = gson.toJson(standartData);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<OptionsServer> call = RetrofitBuilder.getRetrofitInterface().GET_OPTIONS(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<OptionsServer>() {
            @Override
            public void onResponse(Call<OptionsServer> call, Response<OptionsServer> response) {
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().getState()) {
                                if (response.body().getList() != null && response.body().getList().size() > 0) {
                                    RealmManager.saveDownloadedOptions(response.body().getList());
                                    click.onSuccess("Опции успешно обновлены!");
                                } else {
                                    click.onFailure("Список Опций пуст. Обратитесь к своему руководителю.");
                                }
                            } else {
                                if (response.body().getError() != null) {
                                    click.onFailure("Данные обновить не получилось. Это связанно с: " + response.body().getError());
                                } else {
                                    click.onFailure("Данные обновить не получилось. Ошибку обнаружить не получилось. Обратитесь к руководителю.");
                                }
                            }
                        } else {
                            click.onFailure("Запрашиваемых данных не обнаружено.");
                        }
                    } else {
                        click.onFailure("Произошла ошибка при обновлении списка опций. Ошибка: " + response.code());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/downloadOptionByDad2/onResponse", "Exception e: " + e);
                    click.onFailure("Произошла ошибка. Передайте её руководителю: " + e);
                }
                if (pg != null && pg.isShowing()) pg.dismiss();
            }

            @Override
            public void onFailure(Call<OptionsServer> call, Throwable t) {
                if (pg != null && pg.isShowing()) pg.dismiss();
                Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/downloadOptionByDad2/onFailure", "Throwable t: " + t);
                click.onFailure(t.toString());
            }
        });
    }

}
