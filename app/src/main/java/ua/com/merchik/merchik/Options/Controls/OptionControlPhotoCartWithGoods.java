package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

public class OptionControlPhotoCartWithGoods<T> extends OptionControl {
    public int OPTION_CONTROL_PhotoCartWithGoods_ID = 132971;

    public boolean signal = true;

    private WpDataDB wp;
    private CustomerSDB customerSDBDocument;
    private UsersSDB usersSDBDocument;
    private AddressSDB addressSDBDocument;
    private Integer themeId;

    private Long dateDocument;  // В секундах
    private Long dateFrom = 0L;
    private Long dateTo = 0L;
    private int wpDataSize = 0;
    private Integer tpId; // идентификатор сети (сильпо, атб..)

    private Integer[] groups = {383, 434};  // исключаем из отчетов: 383-АШАН, 434-АТБ

    public OptionControlPhotoCartWithGoods(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;

        getDocumentVar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                executeOption();
            } catch (Exception e) {
                Globals.writeToMLOG("INFO", "OptionControlPhotoCartWithGoods/executeOption", "Exception e: " + e);
            }
        }
    }

    private void getDocumentVar() {
        try {
            if (document instanceof WpDataDB wpDataDB) {

                wp = wpDataDB;
                customerSDBDocument = SQL_DB.customerDao().getById(wpDataDB.getClient_id());
                usersSDBDocument = SQL_DB.usersDao().getById(wpDataDB.getUser_id());
                addressSDBDocument = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());

                themeId = wp.getTheme_id();
                if (addressSDBDocument != null) {
                    tpId = addressSDBDocument.tpId;
                }


                dateDocument = wpDataDB.getDt().getTime() / 1000;

                if (themeId == 1178) {    // если это выкуп продукции в ТТ
                    dateFrom = Clock.getDatePeriodLong(dateDocument * 1000, -2) / 1000;
                } else {
                    dateFrom = Clock.getDatePeriodLong(dateDocument * 1000, -30) / 1000;
                }
                dateTo = Clock.getDatePeriodLong(dateDocument * 1000, 2) / 1000;

                wpDataSize = WpDataRealm.getWpDataBy(new Date(dateFrom * 1000), new Date(dateTo * 1000), null, wp.getAddr_id(), wp.getClient_id(), wp.getUser_id()).size();

            }

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPhotoCartWithGoods/getDocumentVar", "Exception e: " + e);
            Globals.writeToMLOG("ERROR", "OptionControlPhotoCartWithGoods/getDocumentVar", "Exception e: " + Arrays.toString(e.getStackTrace()));
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        try {
            List<StackPhotoDB> stackPhotoDBList;

            int photoCount = 0;
            int photoDVICount = 0;
            int experience = 1000; // стаж
            int workCount = 0;


            //2.0. проверим наличие фото. Ступенями для того, чтобы ускорить проведение документа
            if (themeId != null && (themeId == 1178 || themeId == 1003)) {   // 1178-выкуп продукции, 1003-курьерские
                stackPhotoDBList = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getPhoto(dateFrom, dateTo, wp.getUser_id(), wp.getAddr_id(), wp.getClient_id(), wp.getCode_dad2(), StackPhotoDB.PHOTO_CART_WITH_GOODS, null));

                photoCount = stackPhotoDBList.size();
                photoDVICount = stackPhotoDBList.stream().map(table -> table.dvi).reduce(0, Integer::sum);
            } else if (tpId != null && Arrays.stream(groups).anyMatch(x -> Objects.equals(x, tpId)) || Arrays.stream(groups).anyMatch(x -> Objects.equals(x, tpId))) {
                //для 8196-Ашанов и 6164-АТБ пока, исключения
                // TODO А мне точно надо это место?
                Globals.writeToMLOG("INFO", "OptionControlPhotoCartWithGoods/stream(groups)", "Может это все же не надо делать и тратить на это ресурсы?");
            } else {
                experience = calculateExperience(usersSDBDocument, dateDocument);
                if (experience > 30 && usersSDBDocument != null && usersSDBDocument.reportDate40 != null && dateDocument > usersSDBDocument.reportDate40.getTime() / 1000) {
                    workCount = wpDataSize;
                    if (workCount > 3) {
                        stackPhotoDBList = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getPhoto(dateFrom * 1000, dateTo * 1000, null, wp.getAddr_id(), wp.getClient_id(), null, StackPhotoDB.PHOTO_CART_WITH_GOODS, null));
                        photoCount = stackPhotoDBList.size();
                        photoDVICount = stackPhotoDBList.stream().map(table -> table.dvi).reduce(0, Integer::sum);
                    }
                }
            }

            // 3.0. подведем итог
            if (Arrays.stream(groups).anyMatch(x -> Objects.equals(x, tpId)) && themeId != null && themeId != 1178 && themeId != 1003) {   // //для 8196-Ашанов и 6164-АТБ  //1178-выкуп продукции, 1003-курьерские
                stringBuilderMsg.append("Фото Тележки с Товаром (ФТТ) для сети (Ашае/АТБ) пока не проверяем.");
                signal = false;
                unlockCodeResultListener.onUnlockCodeSuccess();
            } else if (photoCount > 0 && themeId != null && themeId == 1003 && photoCount == photoDVICount) {
                stringBuilderMsg.append("Фото Тележки с Товаром (ФТТ) ").append(usersSDBDocument.fio)
                        .append(" выполнено (").append(photoCount).append(") но помечено на ДВИ. Для данной темы ДВИ ставить НЕ нужно!");
                signal = false;
                unlockCodeResultListener.onUnlockCodeSuccess();
            } else if (photoCount > 0) {
                stringBuilderMsg.append("Фото Тележки с Товаром (ФТТ) ").append(usersSDBDocument.fio)
                        .append(" выполнено (").append(photoCount).append(") и будет проверено ОСП.");
                signal = false;
                unlockCodeResultListener.onUnlockCodeSuccess();
            } else if (experience < 30 && usersSDBDocument.reportDate40 != null && usersSDBDocument.reportDate40.getTime() / 1000 < dateDocument && themeId != null && themeId == 998) {
                stringBuilderMsg.append("Сотрудник ").append(usersSDBDocument.fio)
                        .append(" имеет стаж ").append("experience").append(" дней и провел ").append(usersSDBDocument.reportCount)
                        .append(" отчетов. Данный тип фото начинаем проверять после 30-и дней стажа или после проведения 40-а отчетов.");
                signal = false;
                unlockCodeResultListener.onUnlockCodeSuccess();
            } else if (workCount < 4 && themeId != null && themeId == 998) {
                stringBuilderMsg.append("Сотрудник ").append(usersSDBDocument.fio)
                        .append(" за период с ").append(Clock.getHumanTimeSecPattern(dateFrom, "MM-dd"))
                        .append(" по ").append(Clock.getHumanTimeSecPattern(dateTo, "MM-dd"))
                        .append(" дней и провел ").append(workCount).append(" отчетов по данному КлиентоАдресу. Данный тип фото начинаем проверять после проведения более 3-х отчетов за 30-ь дней.");
                signal = false;
                unlockCodeResultListener.onUnlockCodeSuccess();
            } else {
                stringBuilderMsg.append("Фото Тележки с Товаром (ФТТ) отсутствует (или помечено на ДВИ) по данному клиенту и адресу за период с ")
                        .append(Clock.getHumanTimeSecPattern(dateFrom, "MM-dd"))
                        .append(" по ").append(Clock.getHumanTimeSecPattern(dateTo, "MM-dd"))
                        .append(" . При этом проведено ").append(workCount).append(" отчетов.")
                        .append(" Вы можете получить Премиальные больше, если разместите ФТТ.");
                signal = true;
                unlockCodeResultListener.onUnlockCodeFailure();
            }


            // Сохранение
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

            if (signal) {
                if (optionDB.getBlockPns().equals("1")) {
                    setIsBlockOption(signal);
                    stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
                } else {
                    stringBuilderMsg.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете делать Достижения.");
                }
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPhotoCartWithGoods/executeOption", "Exception e: " + e);
        }
    }


    // Подсчёт стажа
    private int calculateExperience(UsersSDB usersSDB, Long dateDocument) {
        Long firstReport = usersSDB.reportDate01.getTime() / 1000;
        long daysInSec = dateDocument - firstReport;

        // Это в случае, если я нормальный программист
//        Duration duration = Duration.ofSeconds(seconds);
//        long days = duration.toDays();

        return (int) (daysInSec / (24 * 60 * 60));
    }


}
