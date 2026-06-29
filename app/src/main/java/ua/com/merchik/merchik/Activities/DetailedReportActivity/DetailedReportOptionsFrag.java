package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static io.realm.Realm.getApplicationContext;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.savePhoto;
import static ua.com.merchik.merchik.Options.Options.ConductMode.DEFAULT_CONDUCT;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import kotlin.Unit;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.SMSExchange;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.Utils.PhotoPickerUtils;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.dialogs.features.LoadingDialogWithPercent;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;

@SuppressLint("ValidFragment")
public class DetailedReportOptionsFrag extends Fragment {


    PhotoHandler photoHandler;

    public class PhotoHandler {
        private int photoType;

        public PhotoHandler(int photoType) {
            this.photoType = photoType;
        }

        public int getPhotoType() {
            return photoType;
        }
    }

    //    private static Context mContext;
    private WpDataDB wpDataDB;
    private DetailedReportViewModel viewModel;

    ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                try {
                    Intent data = result.getData();
                    if (result.getResultCode() != Activity.RESULT_OK || data == null || data.getData() == null || getContext() == null || photoHandler == null) {
                        return;
                    }

                    Uri uri = data.getData();
                    PhotoPickerUtils.persistReadPermissionIfPossible(requireContext(), data);
                    File file = PhotoPickerUtils.copyPickedImageToFile(requireContext(), uri);
                    savePhoto(file, wpDataDB, photoHandler.getPhotoType(), MakePhotoFromGalery.tovarId, getApplicationContext());
                } catch (Exception e) {
                    Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST", "Exception e: " + e);
                }
            });


    public static final Integer[] DetailedReportOptionsFrag_VIDEO_LESSONS = new Integer[]{821, 4540};

    private YoYo.YoYoString currentPulse;
    private View currentPulseView;
    private int currentPulsePosition = RecyclerView.NO_POSITION;

    public static RecyclerView rvContacts;
    public static RecycleViewDRAdapter recycleViewDRAdapter;

    public DetailedReportOptionsFrag() {
        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/1", "create");
    }

    public static DetailedReportOptionsFrag newInstance() {
        //        fragment.viewModel = viewModel; // Сохраняем ViewModel
//        Bundle args = new Bundle();
//        args.putParcelable("wpDataDB", wpDataDB);
////        mContext = context;
//        fragment.setArguments(args);
        return new DetailedReportOptionsFrag();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag", "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag", "onCreate");

        viewModel = new ViewModelProvider(requireActivity()).get(DetailedReportViewModel.class);

//        Bundle args = getArguments();
//        if (args != null) {
//            wpDataDB = args.getParcelable("wpDataDB");
//            Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag", "onCreate/wpDataDB: " + wpDataDB);
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag", "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag", "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag", "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag", "onDetach");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onCreateView", "inflater: " + inflater);
        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onCreateView", "container: " + container);
        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onCreateView", "create: " + savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_dr_option, container, false);

        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onCreateView", "v: " + v);

        viewModel.getWpDataDB().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                wpDataDB = data; // Получаем данные

                try {
                    Button buttonSave = (Button) v.findViewById(R.id.button);
                    Button buttonMakeAReport = (Button) v.findViewById(R.id.button3);

                    Button download = v.findViewById(R.id.download);
                    TextView information = v.findViewById(R.id.info_msg);
                    TextView planfact = v.findViewById(R.id.planfact);

                    // todo это надо будет вынести в отдельную функцию. И скорее всего перересовывать при клике на "провести"
                    StringBuilder sb = new StringBuilder();

                    sb.append("Прем.(план): ").append(wpDataDB.getCash_ispolnitel()).append(" грн.\n\n");
                    sb.append("Прем.(факт): ").append(wpDataDB.cash_fact).append(" грн.\n");
                    planfact.setText(sb);

                    ImageView check = v.findViewById(R.id.check);
                    if (wpDataDB.getSetStatus() == 1) {
                        check.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_question_circle_regular));
                        check.setColorFilter(requireContext().getResources().getColor(R.color.colorInetYellow));
                    } else {
                        if (wpDataDB.getStatus() == 1) {
                            check.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_check));
                            check.setColorFilter(requireContext().getResources().getColor(R.color.greenCol));
                        } else {
//                            if (Clock.dateConvertToLong(Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000)) < System.currentTimeMillis()) { //+TODO CHANGE DATE
                            if (wpDataDB.getDt().getTime() < System.currentTimeMillis()) { //+TODO CHANGE DATE
                                check.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_exclamation_mark_in_a_circle));
                                check.setColorFilter(requireContext().getResources().getColor(R.color.red_error));
                            } else {
                                check.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_check));
                                check.setColorFilter(requireContext().getResources().getColor(R.color.shadow));
                            }
                        }
                    }

                    WorkPlan workPlan = new WorkPlan();
                    rvContacts = v.findViewById(R.id.DRRecycleView);
                    rvContacts.addOnChildAttachStateChangeListener(pulseViewGuard);
                    rvContacts.addOnScrollListener(pulsePositionGuard);

                    List<OptionsDB> optionsButtons = workPlan.getOptionButtons2(workPlan.getWpOpchetId(wpDataDB), wpDataDB.getId());

                    setupRecyclerView(optionsButtons);


                    Collections.sort(optionsButtons, (o1, o2) -> o1.getSo().compareTo(o2.getSo()));

                    buttonSave.setOnClickListener(b -> {
                        Toast.makeText(requireContext(), "Данный раздел находится в разработке", Toast.LENGTH_LONG).show();
                    });
                    buttonMakeAReport.setOnClickListener(b -> {
                        try {
                            List<OptionsDB> opt = workPlan.getOptionButtons2(workPlan.getWpOpchetId(wpDataDB), wpDataDB.getId());
//                            WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(wpDataDB.getCode_dad2());
                            Globals.writeToMLOG("ERROR", "buttonMakeAReport.setOnClickListener", "getCode_dad2: " + wpDataDB.getCode_dad2());

                            try {
                                SMSExchange smsExchange = new SMSExchange();
                                smsExchange.smsPlanExchange(new Clicks.clickObjectAndStatus() {
                                    @Override
                                    public void onSuccess(Object data) {

                                    }

                                    @Override
                                    public void onFailure(String error) {

                                    }
                                });

                                smsExchange.smsLogExchange(new Clicks.clickObjectAndStatus() {
                                    @Override
                                    public void onSuccess(Object data) {

                                    }

                                    @Override
                                    public void onFailure(String error) {

                                    }
                                });
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "buttonMakeAReport.setOnClickListener/", "Exception e: " + e);
                            }


                            new Options().conduct(getContext(), wpDataDB, opt, DEFAULT_CONDUCT, new Clicks.click() {
                                @Override
                                public <T> void click(T data) {
                                    OptionsDB optionsDB = (OptionsDB) data;
                                    int scrollPosition = recycleViewDRAdapter.getItemPosition(optionsDB);
                                    OptionMassageType msgType = new OptionMassageType();
                                    msgType.type = OptionMassageType.Type.DIALOG;
                                    new Options().optControl(getContext(), wpDataDB, optionsDB, Integer.parseInt(optionsDB.getOptionControlId()), null, msgType, Options.NNKMode.CHECK, new OptionControl.UnlockCodeResultListener() {
                                        @Override
                                        public void onUnlockCodeSuccess() {
                                            Globals.writeToMLOG("ERROR", "buttonMakeAReport.setOnClickListener.Options().conduct", "onUnlockCodeSuccess");
                                        }

                                        @Override
                                        public void onUnlockCodeFailure() {
                                            Globals.writeToMLOG("ERROR", "buttonMakeAReport.setOnClickListener.Options().conduct", "onUnlockCodeFailure");

                                        }
                                    });
                                    rvContacts.smoothScrollToPosition(scrollPosition);
                                }
                            });

                            if (wpDataDB.getSetStatus() == 1) {
                                check.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_question_circle_regular));
                                check.setColorFilter(requireContext().getResources().getColor(R.color.colorInetYellow));

//                    sendWpData2();  // Выгрузка статуса
                                Exchange exchange = new Exchange();
                                exchange.sendWpDataToServer(new Click() {
                                    @Override
                                    public <T> void onSuccess(T data) {
                                        String msg = (String) data;
                                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                                        Globals.writeToMLOG("ERROR", "buttonMakeAReport.setOnClickListener.exchange.sendWpDataToServer", "onSuccess: " + data);
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                                        Globals.writeToMLOG("ERROR", "buttonMakeAReport.setOnClickListener.exchange.sendWpDataToServer", "onFailure: " + error);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Globals.writeToMLOG("ERROR", "DetailedReportOptionsFrag/buttonMakeAReport/setOnClickListener", "Exception e: " + e);
                        }
                    });

                } catch (Exception e) {
                    Log.e("R_TRANSLATES", "convertedObjectERROR: " + e);
                    e.printStackTrace();
                }
            }
        });

        viewModel.getScrollToIdEvent().observe(this, new Observer<OptionsDB>() {
            @Override
            public void onChanged(OptionsDB optionsDB) {
                int scrollPosition = recycleViewDRAdapter.getItemPositionForOptionControl(optionsDB);
                Log.e("showOrScrollAndWait", "animation optionsDB: " + new Gson().toJson(optionsDB));
                if (scrollPosition > 0) {
                    rvContacts.smoothScrollToPosition(scrollPosition);
                    // запустить попытки найти ViewHolder и затем анимировать
                    waitForViewAndHighlight(scrollPosition, rvContacts);
                }
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        try {

            Log.e("R_TRANSLATES", "convertedObject: START");

            WorkPlan workPlan = new WorkPlan();

            Options options = new Options();

            List<OptionsDB> optionsButtons = workPlan.getOptionButtons2(workPlan.getWpOpchetId(wpDataDB), wpDataDB.getId());

            if (optionsButtons.isEmpty() && wpDataDB != null) {
                ProgressViewModel progress = new ProgressViewModel(1);
                LoadingDialogWithPercent loadingDialog = new LoadingDialogWithPercent(requireActivity(), progress);
                loadingDialog.show();
                progress.onNextEvent("Завантажую опції до цього відвідування", 2000);

                TablesLoadingUnloading tlu = new TablesLoadingUnloading();
                tlu.downloadOptionsByDAD2(wpDataDB.getCode_dad2(), new Clicks.click() {
                    @Override
                    public <T> void click(T data) {
                        progress.onCompleted();
                        if (data instanceof List<?>) {
                            List<?> list = (List<?>) data;
                            if (!list.isEmpty() && list.get(0) instanceof OptionsDB) {
                                // Теперь безопасно приводим к List<OptionsDB>
                                List<OptionsDB> optionsList = (List<OptionsDB>) list;
                                for (OptionsDB item : optionsList) {
                                    options.optionControl(requireContext(), wpDataDB, item, null, Options.NNKMode.NULL, new OptionControl.UnlockCodeResultListener() {
                                        @Override
                                        public void onUnlockCodeSuccess() {

                                        }

                                        @Override
                                        public void onUnlockCodeFailure() {

                                        }
                                    });
                                }

                                if (recycleViewDRAdapter != null) {
                                    setupRecyclerView(optionsList);
                                    recycleViewDRAdapter.setDataButtons(optionsList);
//                                    recycleViewDRAdapter.notifyDataSetChanged();
                                }
                            } else {
                                errorMessage(String.valueOf(data));
                            }
                        } else errorMessage(String.valueOf(data));
                    }
                });

//                AlertDialogMessage alertDialogMessage = new AlertDialogMessage(requireActivity(),
//                        "",
//                        "На даний момент немає даних для відображення. Можливо вони ще не завантаженi з боку сервера. Зачекайте завершення обміну даними з сервером, якщо завантаження не вiдбулося знайдіть місце з кращим інтернет-з'єднанням, натисніть 'Синхронізація' (у правому вехньому кутку) і дочекайтеся завершення процесу. Дані мають відобразитися." +
//                                "\nЯкщо це не допомогло, звернiться до керiвника",
//                        DialogStatus.ALERT);
//                alertDialogMessage.show();
            } else {
//                List<Integer> ids = new ArrayList<>();
//                for (OptionsDB item : optionsButtons) {
//                    ids.add(Integer.parseInt(item.getOptionId()));
//                }

//                Log.e("R_TRANSLATES", "item: " + ids.size());
//
//                for (Integer item : ids) {
//                    Log.e("R_TRANSLATES", "Integeritem: " + item);
//                }

                // Запрос к SQL БДшке. Получаем список обьектов сайта
//                List<SiteObjectsSDB> list = SQL_DB.siteObjectsDao().getObjectsById(ids);

                // Получаю все опции по данному отчёту.
//                List<OptionsDB> allReportOption = RealmManager.INSTANCE.copyFromRealm(OptionsRealm.getOptionsByDAD2(String.valueOf(wpDataDB.getCode_dad2())));

//                Log.e("R_TRANSLATES", "item: " + list.size());

//                for (SiteObjectsSDB item : list) {
//                    Log.e("R_TRANSLATES", "SiteObjectsSDBitem: " + item.id);
//                }

                Log.e("TEST_OPTIONS", "optionsButtons SIZE: " + optionsButtons.size());
                for (OptionsDB item : optionsButtons) {
                    options.optionControl(requireContext(), wpDataDB, item, null, Options.NNKMode.NULL, new OptionControl.UnlockCodeResultListener() {
                        @Override
                        public void onUnlockCodeSuccess() {

                        }

                        @Override
                        public void onUnlockCodeFailure() {

                        }
                    });
                }

                if (recycleViewDRAdapter != null) {
                    recycleViewDRAdapter.setDataButtons(optionsButtons);
                    recycleViewDRAdapter.attachRecyclerView(rvContacts);
                }
            }
        } catch (Exception e) {
            Log.e("R_TRANSLATES", "convertedObjectERROR: " + e);
            e.printStackTrace();
        }
    }

    private void errorMessage(String subTitle) {

        new MessageDialogBuilder(requireActivity())
                .setTitle("Відсутні дані щодо цього відвідування")
                .setStatus(DialogStatus.ERROR)
                .setSubTitle(subTitle)
                .setMessage("На даний момент немає даних для відображення. Можливо вони ще не завантаженi з боку сервера. Зачекайте завершення обміну даними з сервером. Якщо завантаження не вiдбулося знайдіть місце з кращим інтернет-з'єднанням, натисніть 'Синхронізація' (у правому вехньому кутку) і дочекайтеся завершення процесу. Дані мають відобразитися." +
                        "\nЯкщо це не допомогло, звернiться до керiвника")
                .setOnConfirmAction(() -> Unit.INSTANCE)
                .show();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            rvContacts = view.findViewById(R.id.DRRecycleView);
            Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onViewCreated", "enter");
            Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onViewCreated/", "mContext: " + view.getContext());

            Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onViewCreated", "end");
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onViewCreated", "Exception e: " + e);
            Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onViewCreated", "Exception exception: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void setupRecyclerView(List<OptionsDB> optionsButtons) {

        List<Integer> ids = new ArrayList<>();
        for (OptionsDB item : optionsButtons) {
            ids.add(Integer.parseInt(item.getOptionId()));
        }
        Collections.sort(optionsButtons, (o1, o2) -> o1.getSo().compareTo(o2.getSo()));
        // Запрос к SQL БДшке. Получаем список обьектов сайта
        List<SiteObjectsSDB> list = SQL_DB.siteObjectsDao().getObjectsById(ids);
        // Получаю все опции по данному отчёту.
        List<OptionsDB> allReportOption = RealmManager.INSTANCE.copyFromRealm(OptionsRealm.getOptionsByDAD2(String.valueOf(wpDataDB.getCode_dad2())));

//        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onViewCreated/", "wpDataDB: " + wpDataDB);
//        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onViewCreated/", "optionsButtons: " + optionsButtons);
//        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onViewCreated/", "allReportOption: " + allReportOption);
//        Globals.writeToMLOG("INFO", "DetailedReportOptionsFrag/onViewCreated/", "list: " + list);

        recycleViewDRAdapter = new RecycleViewDRAdapter(requireContext(), wpDataDB, optionsButtons, allReportOption, list, new Clicks.click() {
            @Override
            public <T> void click(T data) {
                try {
                    int photoType = (int) data;
                    photoHandler = new PhotoHandler(photoType);
                    imagePickerLauncher.launch(PhotoPickerUtils.createSingleImageChooser());
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "DetailedReportOptionsFrag/Intent.ACTION_PICK", "Exception e: " + e);
                }
            }
        });

        rvContacts.setAdapter(recycleViewDRAdapter);
        rvContacts.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
    }


    private void clickDownload(Context context) {
        Toast.makeText(context, "Начинаю загрузку Опций", Toast.LENGTH_SHORT).show();

        TablesLoadingUnloading tlu = new TablesLoadingUnloading();
        tlu.downloadOptionsByDAD2(wpDataDB.getCode_dad2(), new Clicks.click() {
            @Override
            public <T> void click(T data) {
                String msg = (String) data;
                Globals.writeToMLOG("INFO", "downloadOptionsByDAD2/clickDownload/clickRESULT", "msg: " + msg);
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void test() {

    }

    // ---------- вспомогательный метод в Activity/Fragment (Java) ----------
    private void waitForViewAndHighlight(final int pos, final RecyclerView recyclerView) {
        final int MAX_ATTEMPTS = 12;      // сколько раз попробуем
        final long DELAY_MS = 80L;       // интервал между попытками
        final Handler handler = new Handler(Looper.getMainLooper());
        final AtomicInteger attempts = new AtomicInteger(0);

        Runnable tryRunnable = new Runnable() {
            @Override
            public void run() {
                attempts.incrementAndGet();
                RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(pos);
                if (vh != null) {
                    // нашли view — выполняем подсветку и пульсацию
                    highlightAndTada(vh.itemView, pos);
                } else if (attempts.get() < MAX_ATTEMPTS) {
                    handler.postDelayed(this, DELAY_MS);
                } else {
                    // Не удалось найти view за отведённое время — ничего не делаем
                }
            }
        };

        // запускаем первый раз чуть позже, чтобы scroll начал выполняться
        handler.postDelayed(tryRunnable, 120);
    }

    // ---------- анимация подсветки + "тада" ----------
//    private void highlightAndTada(final View itemView, int pos) {
//        if (itemView == null) return;
//
//        // Запускаем пульсацию
//        Log.e("!!!!!!!!!!!!!!!!!!!!","animation: " + pos);
//
//        YoYo.with(Techniques.Pulse)
//                .duration(850)
//                .repeat(15)
    ////                .onEnd(animator -> itemView.setBackground(originalBg))
//                .playOn(itemView);
//
//    }

    private final RecyclerView.OnChildAttachStateChangeListener pulseViewGuard =
            new RecyclerView.OnChildAttachStateChangeListener() {

                @Override
                public void onChildViewAttachedToWindow(View view) {
                    // Ничего не делаем.
                }

                @Override
                public void onChildViewDetachedFromWindow(View view) {
                    // Останавливаем Pulse лишь тогда,
                    // когда RecyclerView убрал именно мигающую строку.
                    if (view == currentPulseView) {
                        stopCurrentPulse();
                    }
                }
            };

    private void highlightAndTada(final View itemView, int pos) {
        if (itemView == null) {
            return;
        }

        // Если раньше мигала другая кнопка — останавливаем только её.
        stopCurrentPulse();

        currentPulseView = itemView;
        currentPulsePosition = pos;

        Log.e("!!!!!!!!!!!!!!!!!!!!", "animation: " + pos);

        currentPulse = YoYo.with(Techniques.Pulse)
                .duration(850)
                .repeat(15)
                .onEnd(animator -> {
                    if (currentPulseView == itemView) {
                        currentPulse = null;
                        currentPulseView = null;
                        currentPulsePosition = RecyclerView.NO_POSITION;
                    }
                })
                .playOn(itemView);
    }

    private void stopCurrentPulse() {
        if (currentPulse != null) {
            currentPulse.stop();
        }

        currentPulse = null;
        currentPulseView = null;
        currentPulsePosition = RecyclerView.NO_POSITION;
    }

    private final RecyclerView.OnScrollListener pulsePositionGuard =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(
                        RecyclerView recyclerView,
                        int dx,
                        int dy
                ) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (currentPulseView == null) {
                        return;
                    }

                    int actualPosition =
                            recyclerView.getChildAdapterPosition(currentPulseView);

                    // Этот View уже привязан не к нужной позиции
                    // либо выбыл из списка.
                    if (actualPosition != currentPulsePosition) {
                        stopCurrentPulse();
                    }
                }
            };
}
