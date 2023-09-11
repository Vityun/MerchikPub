package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.trecker.coordinatesDistanse;
import static ua.com.merchik.merchik.trecker.enabledGPS;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.database.realm.tables.LogMPRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class OptionControlMP<T> extends OptionControl {

    private List<LogMPDB> logMPList = new ArrayList<>();

    private WpDataDB wpDataDB;
    private AddressSDB addressSDB;

    public int distanceMin = Globals.distanceMin;

    private int validTime = 1800;   // 30 (1800сек) минут допустимого времени.
    private float coordAddrX, coordAddrY;

    private StringBuilder stringBuilder = new StringBuilder();
    private String period = "";

    public OptionControlMP(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;
        getDocumentVar();
//        executeOption();
    }

    private void getDocumentVar() {
        try {

            if (document instanceof WpDataDB) {
                wpDataDB = (WpDataDB) document;
                addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());

                AppUsersDB appUsersDB = AppUserRealm.getAppUserById(wpDataDB.getUser_id());
                if (appUsersDB != null && appUsersDB.user_work_plan_status != null && !appUsersDB.user_work_plan_status.equals("our")){
                    distanceMin = 1800;
                }

                if (addressSDB != null){
                    coordAddrX = addressSDB.locationXd;
                    coordAddrY = addressSDB.locationYd;
                }else {
                    try {
                        if (wpDataDB != null){
                            coordAddrX = Float.parseFloat(wpDataDB.getAddr_location_xd());
                            coordAddrY = Float.parseFloat(wpDataDB.getAddr_location_yd());
                        }
                    }catch (Exception e){}
                }
            }

            long startTime = (wpDataDB.getVisit_start_dt() > 0)
                    ? wpDataDB.getVisit_start_dt() - validTime
                    : (System.currentTimeMillis() / 1000) - validTime;

            long endTime = System.currentTimeMillis() / 1000;

            logMPList = LogMPRealm.getLogMPTime(startTime, endTime);

            if (logMPList != null && logMPList.size() > 0){
                for (LogMPDB item : logMPList){
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
            stringBuilder.append("").append(date).append(" з ").append(dateFrom).append(" по ").append(dateTo).append(" ").append("\n\n");


        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlMP/getDocumentVar", "Exception e: " + e);
        }
    }

    private void executeOption() {
        try {

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlMP/executeOption", "Exception e: " + e);
        }
    }

    public void showMassage(Clicks.clickStatusMsg click) {
        try {
            DialogData dialog = new DialogData(context);
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
                        LogMPDB logMPDB = null;
                        for (LogMPDB log : logMPList) {
                            if (log.distance != 0) {
                                logMPDB = log;
                                break;
                            }
                        }

                        double distance = logMPDB.distance;
                        if (distance < distanceMin) {
                            stringBuilder.append("Ви визначені на торговій точці. Зауважень немає.");
                            okMP = true;
                            click.onSuccess("");
                        } else {
                            String distanceType = " м ";
                            if (distance > 1000) {
                                distance = distance / 1000;
                                distanceType = " км ";
                            }

                            stringBuilder
                                    .append("За визначенням системи, ")
//                                    .append("ви знаходитесь задалеко від торгівельної точки (ТТ)!").append("\n\n")
                                    .append("станом на ").append(Clock.getHumanTimeSecPattern(logMPDB.CoordTime/1000, "HH:mm")).append("").append(", ви знаходились на відстані ").append((int)distance).append("").append(distanceType).append("від ТТ ").append(addressSDB.nm)
                                    .append(", що більше допустимих ").append(distanceMin).append(" метрів. ")
                                    .append("Це може бути помилковим визначенням.")
                                    .append("\n\n")
                                    .append("Якщо ви в дійсності знаходитесь на ТТ").append("\n")
                                    .append("- вийдіть на подвір'я, відкрийте форму відвідування, та натисніть кнопку \"Запит МП\" чи \"Історія МП\".").append("\n")
                                    .append("- якщо це не допомогло зверніться до свого керівника або в службу підтримки merchik.").append("\n");
//                                    .append("Ваше поточне розташування не відповідає адресі, для якої призначено фото. Визначте місце розташування повторно.");
                            click.onFailure(stringBuilder.toString());
                        }
                    }
                }else {
                    stringBuilder.append("У магазині в якому Ви працюєте не встановлені координати!").append("\n").append("Зверніться до Вашого керівника для виправлення цієї проблеми.");
                    click.onFailure(stringBuilder.toString());
                }
            } else {
                stringBuilder.append("У вас ввимкнено GPS!").append("\n").append("Будь-ласка увімкніть GPS, почекайте поки з`явиться Ваше місцеположення та продовжуйте роботу.");
                click.onFailure(stringBuilder.toString());
            }


            if (!okMP) {
                dialog.setImgBtnCall(context);
                dialog.setTitle(title);
                dialog.setText(stringBuilder);
                dialog.setDialogIco();
                dialog.setClose(dialog::dismiss);
                dialog.show();
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlMP/showMassage", "Exception e: " + e);
        }
    }

}
