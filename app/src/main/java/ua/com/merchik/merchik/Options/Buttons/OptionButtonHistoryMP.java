package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import ua.com.merchik.merchik.Adapters.RecyclerAndPhotoAdapter;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Controls.OptionControlMP;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.Translate;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.LogMPRealm;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;

// 138773
public class OptionButtonHistoryMP<T> extends OptionControl {
    public int OPTION_BUTTON_ADD_COMMENT_ID = 138773;

    private WpDataDB wpDataDB;
    private List<LogMPDB> logMPDBList;

    private int validTime = 1800;   // 30 (1800сек) минут допустимого времени.
    private long startTime;
    private long endTime;

    private LogMPDB currentLogMP;

    private String workStatusSub = "";
    private String workStatusMessage = "";

    private String id = "";
    private String time = "";

    private Function0<Unit> updateContent;

    public OptionButtonHistoryMP(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        try {
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            this.unlockCodeResultListener = unlockCodeResultListener;
            getDocumentVar();
            executeOption();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionButtonHistoryMP", "Exception e: " + e);
        }
    }

    public OptionButtonHistoryMP(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener, @NotNull Function0<Unit> updateContent) {
        try {
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            this.unlockCodeResultListener = unlockCodeResultListener;
            this.updateContent = updateContent;
            getDocumentVar();
            executeOption();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionButtonHistoryMP", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        try {
            wpDataDB = (WpDataDB) document;
            startTime = (wpDataDB.getVisit_start_dt() > 0)
                    ? wpDataDB.getVisit_start_dt() - validTime
                    : (System.currentTimeMillis() / 1000) - validTime;

            endTime = wpDataDB.getVisit_end_dt() > 0 ? wpDataDB.getVisit_end_dt() : System.currentTimeMillis() / 1000;

            logMPDBList = LogMPRealm.getLogMPTime(startTime * 1000, endTime * 1000);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionButtonHistoryMP/getDocumentVar", "Exception e: " + e);
        }
    }

    private void executeOption() {
//        Intent intent = new Intent(context, FeaturesActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putString("viewModel", LogMPDBViewModel.class.getCanonicalName());
//        bundle.putString("dataJson", new Gson().toJson(wpDataDB));
//        bundle.putString("title", "Історія місцерозташування");
//        intent.putExtras(bundle);
//        context.startActivity(intent);


        if (wpDataDB.getClient_end_dt() > 0) {
            workStatusSub = "Роботи з поточного відвідування закінчені у " + Clock.getHumanTime2(wpDataDB.getClient_end_dt() * 1000);
            workStatusMessage = "Запис до бази даних з поточними координатами не додано";
        }

        new MessageDialogBuilder(Globals.unwrap(context))
                .setTitle(Translate.translationText(8576, "Історія місцерозташування"))
                .setSubTitle(Translate.translationText(8577, "Історія місцерозташування виконавця під час відвідування знаходиться в лічильнику на кнопці-"))
                .setMessage(Translate.translationText(8578, "Визначити та додати поточне розташування пристрою до бази даних?"))
                .setStatus(DialogStatus.NORMAL)
                .setOnConfirmAction(() -> {
                    LogMPDB logMPDB = Globals.fixMP(wpDataDB, context);
                    if (logMPDB != null) {
                        id = String.valueOf(logMPDB.id);
                        time = Clock.getHumanTime2(logMPDB.vpi);
                    } else {
                        id = String.valueOf(LogMPRealm.getLogMPCount() + 1);
                        time = Clock.getHumanTime2(System.currentTimeMillis() / 1000);
                    }
                    OptionControlMP optionControlMP = new OptionControlMP(context, wpDataDB, optionDB, msgType, Options.NNKMode.MAKE, unlockCodeResultListener);
                    optionControlMP.showMassage(false, new Clicks.clickStatusMsg() {
                        @Override
                        public void onSuccess(String data) {
                            if (workStatusMessage.isEmpty() && workStatusSub.isEmpty())
                                new MessageDialogBuilder(Globals.unwrap(context))
                                        .setTitle(Translate.translationText(8576, "Визначення місцерозташування"))
                                        .setSubTitle(String.format("Запис %s додано до бази даних з поточними координатами %s", id, time))
                                        .setMessage(data)
                                        .setStatus(DialogStatus.NORMAL)
                                        .setOnConfirmAction(() -> {
                                            if (updateContent != null)
                                                updateContent.invoke();
                                            return Unit.INSTANCE;
                                        })
                                        .show();
                            else
                                new MessageDialogBuilder(Globals.unwrap(context))
                                        .setTitle(Translate.translationText(8576, "Визначення місцерозташування"))
                                        .setSubTitle(workStatusSub)
                                        .setMessage(workStatusMessage)
                                        .setStatus(DialogStatus.ALERT)
                                        .setOnConfirmAction(() -> {
                                            if (updateContent != null)
                                                updateContent.invoke();
                                            return Unit.INSTANCE;
                                        })
                                        .show();

                        }

                        @Override
                        public void onFailure(String error) {
                            if (workStatusMessage.isEmpty() && workStatusSub.isEmpty())
                                new MessageDialogBuilder(Globals.unwrap(context))
                                        .setTitle(Translate.translationText(8576, "Визначення місцерозташування"))
                                        .setSubTitle(String.format("Запис %s додано до бази даних з поточними координатами %s", id, time))
                                        .setMessage(error)
                                        .setStatus(DialogStatus.ERROR)
                                        .setOnConfirmAction(() -> {
                                            if (updateContent != null)
                                                updateContent.invoke();
                                            return Unit.INSTANCE;
                                        })
                                        .show();
                            else
                                new MessageDialogBuilder(Globals.unwrap(context))
                                        .setTitle(Translate.translationText(8576, "Визначення місцерозташування"))
                                        .setSubTitle(workStatusSub)
                                        .setMessage(workStatusMessage)
                                        .setStatus(DialogStatus.NORMAL)
                                        .setOnConfirmAction(() -> {
                                            if (updateContent != null)
                                                updateContent.invoke();
                                            return Unit.INSTANCE;
                                        })
                                        .show();

                        }
                    });

                    return Unit.INSTANCE;
                })
                .setOnCancelAction(() -> Unit.INSTANCE)
                .show();
//        try {
//            Globals.fixMP(wpDataDB, context);
//            DialogData dialog = new DialogData(context);
//            dialog.setTitle("Історія місцеположення");
//            dialog.setText("Нижче зазначені данні із місцезнаходженням вашого пристрою за період: з " + Clock.getHumanTimeSecPattern(startTime, "dd.MM HH:mm:ss") + " по " + Clock.getHumanTimeSecPattern(endTime, "dd.MM HH:mm:ss"));
//            dialog.setRecycler(createAdapter(dialog.context, logMPDBList, wpDataDB.getAddr_id()), new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//            dialog.setClose(dialog::dismiss);
//            dialog.setOk("Запит МП", ()->{
//                Globals.fixMP(wpDataDB, null);
//                Toast.makeText(context, "Запит створено!", Toast.LENGTH_SHORT).show();
//            });
//            dialog.show();
//        }catch (Exception e){
//            Globals.writeToMLOG("ERROR", "OptionButtonHistoryMP/executeOption", "Exception e: " + e);
//        }
    }

    /**
     * 01.09.23.
     * Создание адаптера для отображения списка Истории местоположений
     *
     * @return*/
//    private RecyclerView.Adapter createAdapter(Context context, List<LogMPDB> logMPDBList){
//        LogMPAdapter adapter = new LogMPAdapter(context, logMPDBList);
//        return adapter;
//    }
    private RecyclerView.Adapter createAdapter(Context context, List<LogMPDB> logMPDBList, int addr) {
        RecyclerAndPhotoAdapter adapter = new RecyclerAndPhotoAdapter(context, logMPDBList, addr);
        return adapter;
    }
}
