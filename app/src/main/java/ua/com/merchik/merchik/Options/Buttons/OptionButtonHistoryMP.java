package ua.com.merchik.merchik.Options.Buttons;

import android.Manifest;
import android.content.Context;

import androidx.annotation.RequiresPermission;
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
import ua.com.merchik.merchik.Utils.WorkStartNetworkSnapshot;
import ua.com.merchik.merchik.Utils.WifiPresencePolicy;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.LogMPRealm;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;
import ua.com.merchik.merchik.trecker;

// 138773
public class OptionButtonHistoryMP<T> extends OptionControl {
    public int OPTION_BUTTON_ADD_COMMENT_ID = 138773;

    private WpDataDB wpDataDB;
    private List<LogMPDB> logMPDBList;

    private int validTime = 1800;   // 30 (1800сек) минут допустимого времени.
    private long startTime;
    private long endTime;

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

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN})
    private void executeOption() {
        new MessageDialogBuilder(Globals.unwrap(context))
                .setTitle(Translate.translationText(8576, "Історія місцерозташування"))
                .setSubTitle(Translate.translationText(8577, "Історія місцерозташування виконавця під час відвідування знаходиться в лічильнику на кнопці-"))
                .setMessage(Translate.translationText(8578, "Визначити та додати поточне розташування пристрою до бази даних?"))
                .setStatus(DialogStatus.NORMAL)
                .setOnConfirmAction(() -> {
                    afterDialog(this::captureNewMeasurement);
                    return Unit.INSTANCE;
                })
                .setOnCancelAction(() -> Unit.INSTANCE)
                .show();
    }

    private void afterDialog(Runnable action) {
        // MessageDialogBuilder dismisses the current dialog after its confirm callback.
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            android.app.Activity activity = Globals.unwrap(context);
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) action.run();
        });
    }

    private void refreshContent() {
        if (updateContent != null) updateContent.invoke();
    }

    private void captureNewMeasurement() {
        WpDataDB fresh = RealmManager.getWorkPlanRowByCodeDad2(wpDataDB.getCode_dad2());
        if (fresh != null) wpDataDB = fresh;
        long endedAt = Math.max(wpDataDB.getClient_end_dt(), wpDataDB.getVisit_end_dt());
        if (endedAt > 0) {
            new MessageDialogBuilder(Globals.unwrap(context))
                    .setTitle("Визначення місцерозташування")
                    .setSubTitle("Новий замір не виконувався")
                    .setMessage("Роботу завершено " + Clock.getHumanTime2(endedAt)
                            + ". Можна переглянути результат перевірки МП за історією.")
                    .setStatus(DialogStatus.ALERT)
                    .setOnConfirmAction(() -> {
                        afterDialog(() -> showHistory(false));
                        return Unit.INSTANCE;
                    }).show();
            return;
        }

        trecker.SetUpLocationListener(context);
        Globals.delayGPSTime = 0;
        Globals.LocationCapture capture = Globals.captureLocation(wpDataDB, context);
        OptionControlMP control = new OptionControlMP(context, wpDataDB, optionDB, msgType,
                Options.NNKMode.MAKE, unlockCodeResultListener);
        // Recalculate even when the user closes the measurement dialog without continuing.
        control.showMassage(false, new Clicks.clickStatusMsg() {
            @Override public void onSuccess(String data) { }
            @Override public void onFailure(String error) { }
        });
        refreshContent();

        StringBuilder message = new StringBuilder();
        LogMPDB point = capture.coordinates;
        if (point != null) {
            message.append("Збережено запис №").append(point.id)
                    .append(". Джерело: ").append(Integer.valueOf(1).equals(point.provider) ? "GPS" : "NET")
                    .append(".\nЧас заміру: ").append(Clock.getHumanTime2(point.CoordTime / 1000))
                    .append(".\n");
            if (point.distance >= 0) message.append("Відстань до ТТ: ").append(point.distance).append(" м.\n");
            else message.append("Відстань не визначено: немає координат магазину.\n");
        } else {
            message.append("Новий запис GPS/NET не додано. ");
            if ("already_recorded".equals(capture.coordinateResult)) {
                message.append("Доступний замір вже збережений.");
            } else if ("permission_denied".equals(capture.coordinateResult)) {
                message.append("Немає дозволу на визначення місцеположення.");
            } else if ("save_failed".equals(capture.coordinateResult)) {
                message.append("Помилка збереження координат. Повторіть спробу.");
            } else {
                message.append("Немає придатного заміру за останні 10 хвилин. "
                        + "Координати без часу, з майбутнім часом або фіктивні не приймаються. "
                        + "Перевірте геолокацію та повторіть спробу.");
            }
            message.append("\n");
        }
        boolean wifiConfirmed = capture.wifi != null && capture.wifi.inPlace == 1;
        if (wifiConfirmed) {
            message.append("\nWi-Fi: місцеположення підтверджено саме для цієї ТТ. Запис №")
                    .append(capture.wifi.id).append(", час перевірки: ")
                    .append(Clock.getHumanTime2(capture.wifi.CoordTime / 1000)).append(".");
        } else {
            message.append("\nWi-Fi: ").append(wifiResultText(capture.wifiResult));
        }
        message.append("\n\nРезультат перевірки МП за історією буде показано окремо.");
        boolean onSiteNow = wifiConfirmed || (point != null
                && WifiPresencePolicy.onSite(point, wpDataDB.getAddr_id(), control.distanceMin));
        new MessageDialogBuilder(Globals.unwrap(context))
                .setTitle("Визначення місцерозташування")
                .setSubTitle("Новий замір")
                .setMessage(message.toString())
                .setStatus(onSiteNow ? DialogStatus.NORMAL : DialogStatus.ALERT)
                .setOnConfirmAction(() -> {
                    afterDialog(() -> showHistory(true));
                    return Unit.INSTANCE;
                }).show();
    }

    private String wifiResultText(String reason) {
        switch (reason) {
            case "wifi_off": return "вимкнений. Увімкніть Wi-Fi та повторіть визначення МП.";
            case "permission_denied": return "немає дозволів для перевірки мереж.";
            case "no_fresh_scan": return "немає свіжих результатів сканування мереж.";
            case "no_reference": return "немає еталонних мереж для цієї ТТ.";
            case "not_confirmed": return "недостатньо збігів з мережами цієї ТТ.";
            case "confirmed": return "мережі збіглися, але координати магазину не визначені.";
            case "save_failed": return "помилка збереження результату перевірки.";
            default: return "не вдалося підтвердити місцеположення за мережами.";
        }
    }

    private void showHistory(boolean collectNetworks) {
        OptionControlMP control = new OptionControlMP(context, wpDataDB, optionDB, msgType,
                Options.NNKMode.MAKE, unlockCodeResultListener);
        control.showMassage(false, new Clicks.clickStatusMsg() {
            @Override public void onSuccess(String data) { showHistoryMessage(data, true, collectNetworks); }
            @Override public void onFailure(String error) { showHistoryMessage(error, false, collectNetworks); }
        });
        refreshContent();
    }

    private void showHistoryMessage(String message, boolean confirmed, boolean collectNetworks) {
        new MessageDialogBuilder(Globals.unwrap(context))
                .setTitle("Визначення місцерозташування")
                .setSubTitle("Перевірка МП за історією")
                .setMessage(message)
                .setStatus(confirmed ? DialogStatus.NORMAL : DialogStatus.ERROR)
                .setOnConfirmAction(() -> {
                    if (collectNetworks) afterDialog(this::collectStoreNetworks);
                    return Unit.INSTANCE;
                }).show();
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN})
    private void collectStoreNetworks() {
        WorkStartNetworkSnapshot.captureAndLog(context, wpDataDB, 1383, trecker::coordinatesDistanse, result -> {
            Globals.writeToMLOG(result.isOk() ? "INFO" : "WARNING", "OptionButtonHistoryMP/networkCollection",
                    "dad2=" + wpDataDB.getCode_dad2() + ", wifiAdded=" + result.wifiAdded
                            + ", btAdded=" + result.btAdded + ", error=" + result.error);
            afterDialog(() -> new MessageDialogBuilder(Globals.unwrap(context))
                    .setTitle("Збір мереж для карти магазину")
                    .setSubTitle("Wi-Fi / Bluetooth")
                    .setMessage((result.isOk()
                            ? "До черги передачі на сервер додано нових мереж: Wi-Fi " + result.wifiAdded
                                    + ", Bluetooth " + result.btAdded + "."
                            : result.error) + "\n\nЦей збір не змінює результат контролю МП за історією.")
                    .setStatus(result.isOk() ? DialogStatus.NORMAL : DialogStatus.ALERT)
                    .setOnConfirmAction(() -> Unit.INSTANCE)
                    .show());
        });
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
