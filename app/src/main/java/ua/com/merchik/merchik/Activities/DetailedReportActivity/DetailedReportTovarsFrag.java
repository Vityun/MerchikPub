package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.checkVideos;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.imageView;
import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.DEFAULT;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.Utils.CustomRecyclerView;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.ViewListSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.OptionsServer;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.PPADBRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.dialogs.BlockingProgressDialog;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.dialogs.DialogVideo;
import ua.com.merchik.merchik.dialogs.features.LoadingDialogWithPercent;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

@SuppressLint("ValidFragment")
public class DetailedReportTovarsFrag extends Fragment {

    public static final Integer[] DETAILED_REPORT_FRAGMENT_TOVAR_VIDEO_LESSONS = new Integer[]{823, 815};

    //    private static Context mContext;
    private WpDataDB wpDataDB;
    private TasksAndReclamationsSDB tasksAndReclamationsSDB;
    private DetailedReportViewModel viewModel;

    private long codeDad2;
    private String clientId;
    private int addressId;

    private EditText editText;
    //    private TextView allTov;
    //    private RecyclerView recyclerView;
    private CustomRecyclerView recyclerView;
    private ImageView fullTovList, filter;
    private FloatingActionButton fab;
    private TextView badgeTextView;

    private CustomerSDB customerSDB;

    RecycleViewDRAdapterTovar adapter;

    private boolean flag = true;

    private List<TovarDB> tovarDBListFromServer = new ArrayList<>();

    public DetailedReportTovarsFrag() {
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/1", "create");
    }

    public static DetailedReportTovarsFrag newInstance() {
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/newInstance", "DetailedReportTovarsFrag newInstance");
        //        fragment.viewModel = viewModel; // Сохраняем ViewModel

//        Bundle args = new Bundle();
//        args.putParcelable("wpDataDB", wpDataDB);
////        mContext = context;
//        fragment.setArguments(args);
        return new DetailedReportTovarsFrag();
    }

    public static DetailedReportTovarsFrag newInstance(TasksAndReclamationsSDB tasksAndReclamationsSDB) {
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/newInstance", "DetailedReportTovarsFrag newInstance");
        DetailedReportTovarsFrag fragment = new DetailedReportTovarsFrag();
        Bundle args = new Bundle();
        args.putParcelable("tasksAndReclamationsSDB", tasksAndReclamationsSDB);
//        mContext = context;
        fragment.setArguments(args);
        return fragment;
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
        viewModel = new ViewModelProvider(requireActivity()).get(DetailedReportViewModel.class);

        if (args != null) {
//            wpDataDB = args.getParcelable("wpDataDB");
//            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onCreate/wpDataDB: " + wpDataDB);
            tasksAndReclamationsSDB = args.getParcelable("tasksAndReclamationsSDB");
            if (tasksAndReclamationsSDB != null) {
                WpDataDB t = RealmManager.getWorkPlanRowByCodeDad2(tasksAndReclamationsSDB.codeDad2SrcDoc);
                try {
                    wpDataDB = RealmManager.INSTANCE.copyFromRealm(t);
                    this.codeDad2 = wpDataDB.getCode_dad2();
                    this.clientId = wpDataDB.getClient_id();
                    this.addressId = wpDataDB.getAddr_id();

                } catch (Exception ignored) {
                }
            }
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag", "onCreate/tasksAndReclamationsSDB: " + tasksAndReclamationsSDB);
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

        Log.e("TEST_SPEED", "RecycleViewDRAdapterTovar/onCreateView/START");

        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/onCreateView", "inflater: " + inflater);
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/onCreateView", "container: " + container);
        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/onCreateView", "create: " + savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_dr_tovar, container, false);

        Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/onCreateView", "v: " + v);

        try {

            badgeTextView = v.findViewById(R.id.badge_text_view);
            fab = v.findViewById(R.id.fab);
            editText = (EditText) v.findViewById(R.id.drEditTextFindTovar);
            fullTovList = v.findViewById(R.id.full_tov_list);
            filter = v.findViewById(R.id.filter);

            recyclerView = v.findViewById(R.id.DRRecyclerViewTovar);
//            allTov = v.findViewById(R.id.textLikeLink);


        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/onCreateView", "Exception e: " + e);
        }

        Log.e("TEST_SPEED", "RecycleViewDRAdapterTovar/onCreateView/FINISH");
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (viewModel != null && tasksAndReclamationsSDB == null && viewModel.getWpDataDB() != null)
            viewModel.getWpDataDB().observe(getViewLifecycleOwner(), data -> {
                if (data != null) {
                    wpDataDB = data; // Получаем данные
                    this.codeDad2 = wpDataDB.getCode_dad2();
                    this.clientId = wpDataDB.getClient_id();
                    this.addressId = wpDataDB.getAddr_id();

                    setPopup();

                    downloadData();
                    addRecycleView(getTovListNew(TovarDisplayType.DETAILED_REPORT));

                    setFabVideo(this::showYouTubeFab);
                    showYouTubeFab();
                }
            });
        else {
            setPopup();

            downloadData();
            addRecycleView(getTovListNew(TovarDisplayType.DETAILED_REPORT));

            setFabVideo(this::showYouTubeFab);
            showYouTubeFab();
        }
    }

    private void downloadData() {
        List<TovarDB> res = new ArrayList<>();
        res = RealmManager.INSTANCE.copyFromRealm(Objects.requireNonNull(RealmManager.getTovarListFromReportPrepareByDad2(codeDad2)));
        if (res.size() == 0) {
            downloadDetailedReportTovarsData(TovarDisplayType.DETAILED_REPORT, new Clicks.clickStatusMsg() {
                @Override
                public void onSuccess(String data) {
                    Toast.makeText(requireContext(), data, Toast.LENGTH_SHORT).show();
                    Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList.onSuccess", "String data: " + data);
                    addRecycleView(getTovListNew(TovarDisplayType.DETAILED_REPORT));
                    updateTov = false;
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList.onSuccess", "String error: " + error);
                    updateTov = false;
                }
            });
        }
    }

    private void setFabVideo(Clicks.clickVoid click) {
        fab.setOnClickListener(view -> {
            DialogVideo dialogVideo = new DialogVideo(requireContext());
            dialogVideo.setTitle("Перелік відео уроків");
            dialogVideo.setVideos(getSiteHints(DETAILED_REPORT_FRAGMENT_TOVAR_VIDEO_LESSONS), click);
            dialogVideo.setClose(dialogVideo::dismiss);
            dialogVideo.show();
        });
    }

    private void showYouTubeFab() {
        List<ViewListSDB> videos = checkVideos(DETAILED_REPORT_FRAGMENT_TOVAR_VIDEO_LESSONS, () -> {
        });
        if (videos.size() >= DETAILED_REPORT_FRAGMENT_TOVAR_VIDEO_LESSONS.length) {
            fab.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            badgeTextView.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
            if (imageView != null)
                imageView.setVisibility(View.VISIBLE);
            int must = DETAILED_REPORT_FRAGMENT_TOVAR_VIDEO_LESSONS.length;
            int have = videos.size();
            int res = must - have;
            badgeTextView.setText("" + res);
        }
    }

    private List<SiteHintsDB> getSiteHints(Integer[] integers) {
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
                    fullTovList.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_letter_x));
                    DetailedReportActivity.additionalRequirementsFilter = true;
                } else {
                    Log.e("setTextLikeLink", "flag");
                    fullTovList.setRotation(0);
                    fullTovList.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_minus));
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
            customerSDB = null;
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
            item.getItemId();
            switch (item.getTitle().toString()) {
                case "видалити зайві товари":
                    tovarDBList = getTovListNew(TovarDisplayType.DETAILED_REPORT);
                    Toast.makeText(getContext(), "Видалено зайві товари.(" + tovarDBList.size() + ")", Toast.LENGTH_SHORT).show();
                    addRecycleView(tovarDBList);
                    return true;

                case "додати товари з ППА":
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

                case "додати усі товари замовника":
                    if (!Globals.onlineStatus) {
                        new MessageDialogBuilder(requireActivity())
                                .setTitle("Відсутнє інтернет з'єднання")
                                .setStatus(DialogStatus.ERROR)
                                .setMessage("Знайдіть місце з кращим інтернет-з'єднанням і повторіть спробу." +
                                        "\nНові товари не можуть бути додані без інтернету.")
                                .setOnCancelAction(() -> Unit.INSTANCE)
                                .show();
                    } else {
                        ProgressViewModel progress = new ProgressViewModel(1);
                        LoadingDialogWithPercent loadingDialog = new LoadingDialogWithPercent(requireActivity(), progress);
                        loadingDialog.show();
                        progress.onNextEvent("Завантажую усі товари цього клієнта", 3000);


                        tovarDBList = getTovListNew(TovarDisplayType.ALL);
                        List<WpDataDB> dataList = new ArrayList<>();
                        dataList.add(wpDataDB);
                        new TablesLoadingUnloading().downloadTovarTableWhithResult(dataList, new Click() {
                            @Override
                            public <T> void onSuccess(T data) {
                                progress.onCompleted();
                                if (tovarDBListFromServer != null && !tovarDBListFromServer.isEmpty()
                                        && tovarDBListFromServer.size() == ((List<TovarDB>) data).size()) {
                                    new MessageDialogBuilder(requireActivity())
                                            .setTitle("Додати усі товари замовника")
                                            .setStatus(DialogStatus.NORMAL)
                                            .setMessage("Усі доступні товари відображені, на сервері немає нових товарів")
                                            .setOnCancelAction(() -> Unit.INSTANCE)
                                            .show();
                                } else {
                                    tovarDBListFromServer = (List<TovarDB>) data;
                                    for (TovarDB tovarDB : tovarDBListFromServer) {
                                        tovarDB.timeColor = "FAF7BB";
                                    }
                                    new MessageDialogBuilder(requireActivity())
                                            .setTitle("Додати усі товари замовника")
                                            .setStatus(DialogStatus.NORMAL)
                                            .setMessage("Загрузить " + tovarDBListFromServer.size() + " товаров с сервера и добавить в текущий отчет?")
                                            .setOnConfirmAction(() -> {
                                                if (customerSDB != null && customerSDB.ppaAuto == 1 && !customerSDB.id.equals("9382") && !customerSDB.id.equals("32246")) {
                                                    dialogData.setCancel("Ні", () -> {
                                                        tovarDBList.addAll(tovarDBListFromServer);
                                                        openAllTov(tovarDBList);
                                                        dialogData.dismiss();
                                                    });
                                                    dialogData.setClose(dialogData::dismiss);
                                                    dialogData.show();
                                                } else {
                                                    new MessageDialogBuilder(requireActivity())
                                                            .setTitle("Увага")
                                                            .setStatus(DialogStatus.NORMAL)
                                                            .setMessage(tovarDBListFromServer.size() + " товаров загружены с сервера и добавлены в отчет." +
                                                                    "\nДобавленные товары отмечены светло-желтым цветом." +
                                                                    "\n" +
                                                                    "\nОбратите внимание, что ФОТО этих товаров могут быть загружены на протяжении нескольких минут." +
                                                                    "\nУчтите, что в отчете будут реальо СОХРАНЕНЫ только те товары, по которым вы внесете изменения в любой из реквизитов.")
                                                            .setOnConfirmAction(() -> {
                                                                tovarDBList.addAll(tovarDBListFromServer);
                                                                openAllTov(tovarDBList);
                                                                return Unit.INSTANCE;
                                                            })
                                                            .show();
                                                }

                                                return Unit.INSTANCE;
                                            })
                                            .setOnCancelAction(() -> {
                                                tovarDBListFromServer.clear();
                                                return Unit.INSTANCE;
                                            })
                                            .show();
                                }
                            }

                            @Override
                            public void onFailure(String error) {
                                progress.onCanceled();

                                new MessageDialogBuilder(requireActivity())
                                        .setTitle("Сталася помилка")
                                        .setStatus(DialogStatus.ERROR)
                                        .setMessage(error)
                                        .setOnCancelAction(() -> Unit.INSTANCE)
                                        .show();
                            }
                        });
//                        tovarDBList = getTovListNew(TovarDisplayType.ALL);


                    }
                    return true;

                case "додати один товар":
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

        RecycleViewDRAdapterTovar adapter;// = new RecycleViewDRAdapterTovar(getContext(), getTovListNew(TovarDisplayType.ONE), wpDataDB, RecycleViewDRAdapterTovar.OpenType.DIALOG);

        if (wpDataDB != null) {
            adapter = new RecycleViewDRAdapterTovar(getContext(), getTovListNew(TovarDisplayType.ONE), wpDataDB, RecycleViewDRAdapterTovar.OpenType.DIALOG);
        } else {
            adapter = new RecycleViewDRAdapterTovar(getContext(), getTovListNew(TovarDisplayType.ONE), tasksAndReclamationsSDB, RecycleViewDRAdapterTovar.OpenType.DIALOG);
        }

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
                if (RealmManager.INSTANCE.copyFromRealm(RealmManager.getTovarListFromReportPrepareByDad2(codeDad2)) != null) {
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
            Log.e("TEST_SPEED", "RecycleViewDRAdapterTovar/addRecycleView");
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/List<TovarDB> list", "list: " + list);
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/List<TovarDB>", "list.size(): " + list.size());

            Log.e("DetailedReportTovarsF", "list.size(): " + list.size());


            Log.e("АКЦИЯ_ТОВАРА", "START");
            List<Integer> promotionalTov = new ArrayList<>();
            List<AdditionalRequirementsDB> data = null;

            try {
                if (wpDataDB != null) {
                    data = AdditionalRequirementsRealm.getData3(wpDataDB, DEFAULT, null, null, 0);
                } else {
                    data = AdditionalRequirementsRealm.getData3(tasksAndReclamationsSDB, DEFAULT, null, null, 0);
                }

                Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/AdditionalRequirementsDB", "data: " + data);

                Log.e("TEST_SPEED", "RecycleViewDRAdapterTovar/addRecycleView/AdditionalRequirementsDB");
                for (AdditionalRequirementsDB item : data) {
                    if (item.getTovarId() != null && !item.getTovarId().equals("0") && !item.getTovarId().equals("")) {
                        long startDt = item.dtStart != null ? item.dtStart.getTime() / 1000 : 0;
                        long endDt = item.dtEnd != null ? item.dtEnd.getTime() / 1000 : 0;
                        long docDt = wpDataDB.getDt().getTime() / 1000;
                        long docDtMinus2 = Clock.getDatePeriodLong(docDt, -2);
                        long docDtPlus1 = Clock.getDatePeriodLong(docDt, 1);

                        if ((startDt > 0 && endDt > 0 && docDtMinus2 < endDt) || (startDt > 0 && endDt == 0)) {
                            promotionalTov.add(Integer.valueOf(item.getTovarId()));
                        }
                    }
                }
                Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/AdditionalRequirementsDB", "promotionalTov: " + promotionalTov);
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "DetailedReportTovarsFrag/addRecycleView/AdditionalRequirementsDB", "Exception e: " + e);
            }

            Log.e("TEST_SPEED", "RecycleViewDRAdapterTovar/addRecycleView/adapter");
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/addRecycleView/RecycleViewDRAdapterTovar", "mContext: " + requireContext());
            if (wpDataDB != null) {
                adapter = new RecycleViewDRAdapterTovar(requireContext(), list, wpDataDB, RecycleViewDRAdapterTovar.OpenType.DEFAULT);
            } else {
                adapter = new RecycleViewDRAdapterTovar(requireContext(), list, tasksAndReclamationsSDB, RecycleViewDRAdapterTovar.OpenType.DEFAULT);
            }
            adapter.setAkciyaTovList(promotionalTov, data);

            adapter.setTplType(RecycleViewDRAdapterTovar.DRAdapterTovarTPLTypeView.GONE);

            adapter.refreshAdapter(() -> {
                adapter.switchTPLView();
                recyclerView.setAdapter(adapter);
//                rvTovar.setAdapter(finalRecycleViewDRAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
//                rvTovar.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                adapter.notifyDataSetChanged();
            });

            recyclerView.setAdapter(adapter);
//            rvTovar.setAdapter(recycleViewDRAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
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
            List<TovarDB> dataTovar = RealmManager.INSTANCE.copyFromRealm(RealmManager.getTovarListFromReportPrepareByDad2(codeDad2));
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList", "codeDad2: " + codeDad2);
            if (dataTovar != null && dataTovar.size() > 0) {
                list = (ArrayList<TovarDB>) RealmManager.INSTANCE.copyFromRealm(dataTovar);
                Toast.makeText(requireContext(), "Отображен список только тех товаров, которые установлены в матрице ППА", Toast.LENGTH_SHORT).show();
                Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList", "list: " + list.size());
            } else {
                Toast.makeText(requireContext(), "Список товаров по ППА пуст.", Toast.LENGTH_SHORT).show();

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
            Toast.makeText(requireContext(), "Отображен список всех товаров", Toast.LENGTH_SHORT).show();
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList", "dataTovar list: " + list.size());
        }
        return list;
    }

    private void downloadDetailedReportTovarsData(TovarDisplayType type, Clicks.clickStatusMsg click) {
        try {
//            if (tasksAndReclamationsSDB == null){
            BlockingProgressDialog pg = BlockingProgressDialog.show(requireContext(), "Загрузка списка товаров", "Подождите окончания загрузки. Это может занять время.");
            downloadReportPrepareByDad2(pg, click);

//                BlockingProgressDialog pg2 = BlockingProgressDialog.show(mContext, "Загрузка списка опций", "Подождите окончания загрузки. Это может занять время.");
//                downloadOptionByDad2(pg2, click);
//            }
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "DetailedReportTovarsFrag/getTovList/downloadDetailedReportTovarsData", "Exception e: " + e);
        }
    }

    private void downloadReportPrepareByDad2(BlockingProgressDialog pg, Clicks.clickStatusMsg click) {
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

    private void downloadOptionByDad2(BlockingProgressDialog pg, Clicks.clickStatusMsg click) {
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
