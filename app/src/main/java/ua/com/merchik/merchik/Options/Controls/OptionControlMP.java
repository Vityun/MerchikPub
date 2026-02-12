package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.trecker.coordinatesDistanse;
import static ua.com.merchik.merchik.trecker.enabledGPS;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ua.com.merchik.merchik.Activities.MyApplication;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.database.realm.tables.LogMPRealm;
import ua.com.merchik.merchik.dialogs.DialogData;


// 8299
public class OptionControlMP<T> extends OptionControl {

    private List<LogMPDB> logMPList = new ArrayList<>();

    private WpDataDB wpDataDB;
    private AddressSDB addressSDB;

    public int distanceMin = Globals.distanceMin;

    private int validTime = Globals.dalayMaxTimeGPS;   // 30 (1800сек) минут допустимого времени.
    private float coordAddrX, coordAddrY;

    private StringBuilder stringBuilder = new StringBuilder();
    private String period = "";

    public boolean signal = true;
    private LogMPDB latestVpiLog;

    public OptionControlMP(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
        try {

            if (document instanceof WpDataDB) {
                wpDataDB = (WpDataDB) document;
                addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());

                AppUsersDB appUsersDB = AppUserRealm.getAppUserById(wpDataDB.getUser_id());
                if (appUsersDB != null && appUsersDB.user_work_plan_status != null && !appUsersDB.user_work_plan_status.equals("our")) {
                    distanceMin = 800;
                }


                if (addressSDB != null) {
                    int kps = addressSDB.kps;
                    if (kps >= 100)
                        validTime = 60 * 60;
                    else if (kps >= 70)
                        validTime = 40 * 60;
                    else
                        validTime = validTime * 60;

                    coordAddrX = addressSDB.locationXd;
                    coordAddrY = addressSDB.locationYd;
                } else {
                    validTime = validTime * 60;
                    try {
                        if (wpDataDB != null) {
                            coordAddrX = Float.parseFloat(wpDataDB.getAddr_location_xd());
                            coordAddrY = Float.parseFloat(wpDataDB.getAddr_location_yd());
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "OptionControlMP/executeOption", "Exception: " + e.getMessage());
                    }
                }
            }


        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlMP/getDocumentVar", "Exception e: " + e);
        }
    }

    private void executeOption() {
        try {


//            15.07 .2025 изменил по указанию Петрова теперь сигнал расчитывается ИСКЛЮЧИТЕЛЬНО за последние 30 минут если работа не окончена!
            long startTime =
                    (wpDataDB != null &&
                            wpDataDB.getVisit_start_dt() > 0 && wpDataDB.getVisit_end_dt() > 0) ? wpDataDB.getVisit_start_dt() - validTime :
                            (System.currentTimeMillis() / 1000) - validTime;

            long endTime = (wpDataDB.getVisit_end_dt() > 0) ? wpDataDB.getVisit_end_dt() : System.currentTimeMillis() / 1000;

            logMPList = LogMPRealm.getLogMPTime(startTime * 1000, endTime * 1000);

            if (logMPList != null && logMPList.size() > 0) {
                for (LogMPDB item : logMPList) {
                    double distance = coordinatesDistanse(coordAddrX, coordAddrY, item.CoordX, item.CoordY);
                    item.distance = (int) distance;
                }

                LogMPRealm.setLogMP(logMPList);

                // Сортировка списка по полю distance
                Collections.sort(logMPList, new Comparator<LogMPDB>() {
                    @Override
                    public int compare(LogMPDB o1, LogMPDB o2) {
                        return Integer.compare(o1.distance, o2.distance);
                    }
                });
            }

            String date = Clock.getHumanTimeSecPattern(startTime, "dd.MM");
            String dateFrom = Clock.getHumanTimeSecPattern(startTime, "HH:mm");
            String dateTo = Clock.getHumanTimeSecPattern(endTime, "HH:mm");

//            stringBuilder.append("За період з ").append(dateFrom).append(" по ").append(dateTo).append(" ").append("\n\n");
            stringBuilder.append("").append(date).append(" за период с ").append(dateFrom).append(" по ").append(dateTo).append("\n");
            if (addressSDB != null)
                stringBuilder
                        .append("Адрес ТТ: ")
                        .append(addressSDB.nm)
                        .append("\n");

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlMP/executeOption", "Exception e: " + e);
        }
    }

    public void showMassage(boolean showMassage, Clicks.clickStatusMsg click) {
        try {
            if (context == null)
                context = MyApplication.getAppContext();

            DialogData dialog = new DialogData(context);
//            unlockCode();

            StringBuilder title = new StringBuilder();

            boolean okMP = false;
            title.append("Місцеположення");
            if (enabledGPS) {
                if (coordAddrX != 0 && coordAddrY != 0) {
                    if (logMPList == null || logMPList.size() == 0) {
                        stringBuilder.append("Нема жодного запиту на визначення місцезнаходження. ").append("Перевірте чи увімкнений у Вас GPS ").append("Спробуйте підійти до вікна або вийти на вулицю. Через хвилину місцеположення буде визначене і можна продовжувати виконувати роботу.");
                        click.onFailure(stringBuilder.toString());
                    } else {
//                        LogMPDB logMPDB = logMPList.get(0);
//                        double distance = coordinatesDistanse(addressSDB.locationXd, addressSDB.locationYd, logMPDB.CoordX, logMPDB.CoordY);

                        // Найти первый элемент с ненулевым значением distance
//                        LogMPDB logMPDB = null;
//                        for (LogMPDB log : logMPList) {
//                            if (log.distance != 0) {
//                                logMPDB = log;
//
//                                break;
//                            }
//                        }
                        LogMPDB minDistanceLog = null;
                        int validDistanceCount = 0;
                        for (LogMPDB log : logMPList) {
                            if (log.distance != 0) {
                                if (log.distance < distanceMin) {
                                    validDistanceCount++;
                                }
                                if (minDistanceLog == null || log.distance < minDistanceLog.distance) {
                                    minDistanceLog = log;
                                }
                                if (latestVpiLog == null || log.vpi > latestVpiLog.vpi) {
                                    latestVpiLog = log;
                                }
                            }
                        }

                        double distance = minDistanceLog != null ? minDistanceLog.distance : 0;
                        if (distance < distanceMin) {
                            stringBuilder.append(String.format("Местоположение вашего устройства определено %s раз. Из них %s раз система определила вас на ТТ.", logMPList.size(), validDistanceCount))
                                    .append("\n");
                            stringBuilder.append("Ви визначені на торговій точці. Зауважень немає.");
                            okMP = true;
                            if (nnkMode == Options.NNKMode.MAKE) {
                                stringBuilder.setLength(0);
                                stringBuilder
                                        .append("Адрес ТТ: ")
                                        .append(addressSDB.nm)
                                        .append("\n");
                                stringBuilder.append("Поточне розташування присторою станом на ")
                                        .append(Clock.getHumanTime2(latestVpiLog.vpi))
                                        .append(" визначено на вiдстанi ")
                                        .append((int) distance)
                                        .append(" метрiв вiд ТТ");
                            }
                            click.onSuccess(stringBuilder.toString());
                            signal = false;
                        } else {
                            String distanceType = " м ";
                            if (distance > 1000) {
                                distance = distance / 1000;
                                distanceType = " км ";
                            }

                            if (nnkMode == Options.NNKMode.MAKE && latestVpiLog != null) {
                                distance = latestVpiLog.distance;
                                if (distance > 1000) {
                                    distance = distance / 1000;
                                    distanceType = " км ";
                                }
                                stringBuilder.setLength(0);
                                stringBuilder
                                        .append("Адрес ТТ: ")
                                        .append(addressSDB.nm)
                                        .append("\n");
                                stringBuilder
                                        .append("За визначенням системи, ")
                                        .append("станом на ")
                                        .append(Clock.getHumanTime2(latestVpiLog.vpi))
                                        .append(" поточне розташування присторою знаходится на вiдстанi ")
                                        .append((int) distance).append("").append(distanceType)
                                        .append(" вiд ТТ. ")
                                        .append("<font color=red>Местоположение в ТТ не подтверждено<font>");
                            } else {
                                stringBuilder.append(String.format("Местоположение вашего устройства определено %s раз. Из них %s раз система определила вас на ТТ.", logMPList.size(), validDistanceCount))
                                        .append("\n");
                                stringBuilder
                                        .append("За визначенням системи, ")
                                        .append("найближче до ТТ ви знаходились у ").append(Clock.getHumanTimeSecPattern((minDistanceLog != null ? minDistanceLog.CoordTime : System.currentTimeMillis()) / 1000, "HH:mm"))
                                        .append("").append(" на відстані ").append((int) distance).append("").append(distanceType).append("від ТТ ")
                                        .append(", що більше допустимих ").append(distanceMin).append(" метрів. ")
                                        .append("Це може бути помилковим визначенням.")
                                        .append("\n\n")
                                        .append("Якщо ви в дійсності знаходитесь на ТТ").append("\n")
                                        .append("- вийдіть на подвір'я, відкрийте форму відвідування, та натисніть кнопку \"Запит МП\" чи \"Історія МП\".").append("\n")
                                        .append("- якщо це не допомогло зверніться до свого керівника або в службу підтримки merchik.").append("\n");
//                                    .append("Ваше поточне розташування не відповідає адресі, для якої призначено фото. Визначте місце розташування повторно.");
                            }
                            click.onFailure(stringBuilder.toString());
                        }
                    }
                } else {
                    stringBuilder.append("У магазині в якому Ви працюєте не встановлені координати!").append("\n").append("Зверніться до Вашого керівника для виправлення цієї проблеми.");
                    click.onFailure(stringBuilder.toString());
                }
            } else {
                stringBuilder.append("У вас ввимкнено GPS!").append("\n").append("Будь-ласка увімкніть GPS, почекайте поки з`явиться Ваше місцеположення та продовжуйте роботу.");
                click.onFailure(stringBuilder.toString());
            }


            if (showMassage && !okMP) {
                dialog.setImgBtnCall(context);
                dialog.setTitle(title);
                dialog.setText(stringBuilder);
                dialog.setDialogIco();
                dialog.setClose(dialog::dismiss);
                dialog.show();
            }

            stringBuilderMsg = stringBuilder;

            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionDB != null) {
                    if (signal) {
                        optionDB.setIsSignal("1");
                    } else {
                        optionDB.setIsSignal("2");
                    }
                    realm.insertOrUpdate(optionDB);
                }
            });


            setIsBlockOption(signal);
            checkUnlockCode(optionDB);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlMP/showMassage", "Exception e: " + e);
        }
    }

}
