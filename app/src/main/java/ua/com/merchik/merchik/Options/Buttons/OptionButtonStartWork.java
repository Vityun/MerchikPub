package ua.com.merchik.merchik.Options.Buttons;

import static ua.com.merchik.merchik.trecker.enabledGPS;

import android.content.Context;
import android.widget.Toast;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;

public class OptionButtonStartWork<T> extends OptionControl {
    public int OPTION_BUTTON_PHOTO_BEFORE_START_WORK_ID = 135809;

    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButtonStartWork(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;
        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            this.wpDataDB = (WpDataDB) document;
        }
    }

    private void executeOption() {
        new Globals().fixMP(wpDataDB, null);// Фиксация Местоположения в таблице ЛогМп
        try {
            optionStartWork_138518(context, wpDataDB, optionDB, msgType, nnkMode, unlockCodeResultListener);
//            WPDataObj wpDataObj = new WorkPlan().getKPS(wpDataDB.getId());
//            if (enabledGPS) {
//                if (wpDataObj != null) {
//                    if (wpDataObj.getLatitude() > 0 && wpDataObj.getLongitude() > 0) {
//                        if (Globals.CoordX != 0 && Globals.CoordY != 0) {
//                            double distance = ua.com.merchik.merchik.trecker.coordinatesDistanse(wpDataObj.getLatitude(), wpDataObj.getLongitude(), Globals.CoordX, Globals.CoordY);
//                            if (distance > 500) {
//                                DialogData dialogData138518 = new DialogData(context);
//                                dialogData138518.setTitle("Система не визначила Вас на ТТ");
//                                dialogData138518.setText("Можливо Ви помилково обрали не правильне відвідування? Перевірте це!");
//                                OptionsDB finalOption = optionDB;
//                                dialogData138518.setOk("Продовжити", () -> {
//                                    dialogData138518.dismiss();
//                                    DialogData dialogData2138518 = new DialogData(context);
//                                    dialogData2138518.setTitle("Система  не виявила вас на ТТ");
//                                    dialogData2138518.setText("Відмовитись від початку робіт?");
//                                    dialogData2138518.setOk("Так", dialogData2138518::dismiss);
//                                    dialogData2138518.setCancel("Ні", () -> {
//                                        optionStartWork_138518(context, wpDataDB, finalOption, msgType, nnkMode, unlockCodeResultListener);
//                                        dialogData2138518.dismiss();
//                                    });
//                                    dialogData2138518.setClose(dialogData2138518::dismiss);
//                                    dialogData2138518.show();
//                                });
//                                dialogData138518.setCancel("Закрити", dialogData138518::dismiss);
//                                dialogData138518.setClose(dialogData138518::dismiss);
//                                dialogData138518.show();
//                            } else {
//                                optionStartWork_138518(context, wpDataDB, optionDB, msgType, nnkMode, unlockCodeResultListener);
//                            }
//                        }else {
//                            DialogData dialogData = new DialogData(context);
//                            dialogData.setTitle("Система не визначила Вас на ТТ");
//                            dialogData.setText("Система не отримала координати. Спробуйте вийти на вулицю і отримати координати.");
//                            dialogData.setClose(dialogData::dismiss);
//                            dialogData.show();
//                        }
//                    }else {
//                        DialogData dialogData = new DialogData(context);
//                        dialogData.setTitle("Система не визначила Вас на ТТ");
//                        dialogData.setText("У Торгівельної Точки не визначені координати, зверніться до свого керівника!");
//                        dialogData.setClose(dialogData::dismiss);
//                        dialogData.setOk("", ()->{
//                            optionStartWork_138518(context, wpDataDB, optionDB, msgType, nnkMode, unlockCodeResultListener);
//                            dialogData.dismiss();
//                        });
//                        dialogData.show();
//                    }
//                }
//            }else {
//                DialogData dialogData = new DialogData(context);
//                dialogData.setTitle("Система не визначила Вас на ТТ");
//                dialogData.setText("У Вас вимкнений GPS! Увімкніть його та повторість спробу. ");
//                dialogData.setClose(dialogData::dismiss);
//                dialogData.show();
//            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionButtonStartWork/executeOption/Exception", "Exception e: " + e);
        }
    }


    private boolean optionStartWork_138518(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, Options.NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        boolean result;
        Globals.fixMP(wpDataDB, null);
        Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork", "ENTER. wpDataDB.codeDAD2: " + wpDataDB.getCode_dad2());
        if (wpDataDB.getVisit_start_dt() > 0) {
            Toast.makeText(context, "Работа уже начата!", Toast.LENGTH_SHORT).show();
            unlockCodeResultListener.onUnlockCodeSuccess();
            result = true;
        } else {
            try {
                long startTime = System.currentTimeMillis() / 1000;
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    wpDataDB.setDt_update(System.currentTimeMillis() / 1000);
                    wpDataDB.setVisit_start_dt(startTime);
                    wpDataDB.setClient_start_dt(startTime);
                    wpDataDB.startUpdate = true;
                    realm.insertOrUpdate(wpDataDB);
                });

                // Это жосткие костыли
                Exchange exchange = new Exchange();
                exchange.sendWpDataToServer(new Click() {
                    @Override
                    public <T> void onSuccess(T data) {
                        String msg = (String) data;
                        Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork.onSuccess", "msg: " + msg);
                    }

                    @Override
                    public void onFailure(String error) {
                        Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork.onFailure", "error: " + error);
                    }
                });

                Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork", "Вы начали работу в: " + startTime + " / отчёт: " + wpDataDB.getDoc_num_otchet() + " wpDataDB.getCode_dad2(): " + wpDataDB.getCode_dad2());

                Toast.makeText(context, "Вы начали работу в: " + Clock.getHumanTimeOpt(startTime * 1000), Toast.LENGTH_SHORT).show();
                unlockCodeResultListener.onUnlockCodeSuccess();
                result = true;
            } catch (Exception e) {
                Toast.makeText(context, "Возникла ошибка: " + e, Toast.LENGTH_SHORT).show();
                Globals.writeToMLOG("ERROR", "DetailedReportButtons.class.pressStartWork", "wpDataDB.getCode_dad2(): " + wpDataDB.getCode_dad2() + "Exception e: " + e);
                unlockCodeResultListener.onUnlockCodeFailure();
                result = false;
            }
        }


        Exchange exchange = new Exchange();
        exchange.sendWpDataToServer(new Click() {
            @Override
            public <T> void onSuccess(T data) {
                String msg = (String) data;
                Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork.onSuccess", "msg: " + msg);
            }

            @Override
            public void onFailure(String error) {
                Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork.onFailure", "error: " + error);
            }
        });
        return result;
    }
}
