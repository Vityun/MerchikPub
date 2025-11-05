package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;

import android.util.Log;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Realm.VirtualAdditionalMaterialsDB;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsJOIN.AdditionalMaterialsJOINAdditionalMaterialsAddressSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;

public class OptionControlAdditionalMaterialsMark<T> extends OptionControl {
    public int OPTION_CONTROL_ADD_COMMENT_ID = 138342;
    public boolean signal = true;

    private WpDataDB wpDataDB;
    private long dateDocumentLong;

    public OptionControlAdditionalMaterialsMark(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;
        getDocumentVar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeOption();
        } else {
            stringBuilderMsg.append("Произошла ошибка. VERSION_CODES. Обратитесь к руководителю.");
        }
    }

    private void getDocumentVar() {
        try {
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;
                dateDocumentLong = wpDataDB.getDt().getTime();
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAdditionalMaterialsMark/getDocumentVar", "Exception e: " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        try {
            Integer markSum = 0;            // Подсчёт суммы оценок
            Integer nedotochSum = 0;        // Подсчёт суммы nedotoch
            Integer offsetSum = 0;        // Подсчёт суммы Зачет
            double averageRating = 0.0d;    // Средняя оценка
            double deviationFromTheMeanSum = 0.0d;     // Отклонение от среднего
            String json = "";

            // Для формирования итогового сообщения
            StringBuilder msg = new StringBuilder();

            // Создание виртуальной таблички.
            List<VirtualAdditionalMaterialsDB> virtualTable = null;

            long dateFrom = Clock.getDatePeriodLong(dateDocumentLong, -15) / 1000; // Дата документа -15 дней
            long dateTo = Clock.getDatePeriodLong(dateDocumentLong, +4) / 1000;     // Дата документа +3 дня

            // Получаем Доп.материалы.
//            List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> data = SQL_DB.additionalMaterialsDao().getAllForOptionTEST(optionDB.getClientId(), Integer.parseInt(optionDB.getAddrId()), "1", "0");
//            List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> data = SQL_DB.additionalMaterialsDao().getAllForOptionTEST(optionDB.getClientId(), Integer.parseInt(optionDB.getAddrId()), "0");
            List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> data = SQL_DB.additionalMaterialsDao().getAllForOptionTEST2(optionDB.getClientId(), "0");

            // Проверяем, есть ли вообще данные
            if (data != null && data.size() > 0) {

                // Получаем Оценки этих Доп. материалов.
                RealmResults<AdditionalRequirementsMarkDB> marks = AdditionalRequirementsMarkRealm.getAdditionalRequirementsMarksAM(dateFrom, dateTo, wpDataDB.getUser_id(), "0", data);

                // Херачим "Виртуальную" таблицу, как в 1С
                Gson gson = new Gson();
                json = gson.toJson(data);
                Type listType = new TypeToken<ArrayList<VirtualAdditionalMaterialsDB>>() {
                }.getType();

                // Запись того что выше накопировани в виртуальную табличку
                virtualTable = new Gson().fromJson(json, listType);

                for (VirtualAdditionalMaterialsDB item : virtualTable) {

                    item.offset = 0;
                    item.nedotoch = 1; //по умолчанию оценки НЕТ
                    item.note = "Нет ни одной оценки по этому Доп.материалу поставленной " + wpDataDB.getUser_txt();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (marks.stream().filter(m -> m.getItemId().equals(item.id)).findFirst().orElse(null) != null) {
                            AdditionalRequirementsMarkDB currentMark = marks.where().equalTo("itemId", item.id).findFirst();
                            if (currentMark != null) {
                                item.mark = Integer.valueOf(currentMark.getScore());
                                item.nedotoch = 0;
                                item.note = "";
                            } else {
                                item.mark = 0;
                            }
                        } else {
                            item.mark = 0;
                        }
                    }
                }

                //подсчитаем отклонение от средней оценки (для того, чтобы ребята не ставили ОДНУ и ту-же оценку по всем ДТ)
                try {
                    markSum = virtualTable.stream().map(table -> table.mark).reduce(0, Integer::sum);
                    nedotochSum = virtualTable.stream().map(table -> table.nedotoch).reduce(0, Integer::sum);
                    offsetSum = virtualTable.stream().map(table -> table.offset).reduce(0, Integer::sum);

                    averageRating = markSum / (virtualTable.size() - nedotochSum);
                } catch (Exception e) {
                    averageRating = 0;
                }

                // Считаем отклонение от среднего, для каждого элемента
                for (VirtualAdditionalMaterialsDB item : virtualTable) {
                    item.deviationFromTheMean = Math.abs(averageRating - item.mark);
                }

                // Подсчёт суммы отклонения от среднего
                deviationFromTheMeanSum = virtualTable.stream().map(table -> table.deviationFromTheMean).reduce(0.0d, Double::sum);
            }


            // Установка сигналов.
            // TODO ИначеЕсли ((ПустоеЗначение(Исп.ДатаОМ05)=1) или (Дат<=Исп.ДатаОМ05)) и (ДокИст.Вид()="ОтчетИсполнителя") Тогда
            //		глТекстЧата="Не проверяю оценку Доп.требований до 5-й отчетности.";
            //		СигнКон=0;
            if (virtualTable == null || virtualTable.size() == 0) {

                msg.append("У клиента ")
                        .append(CustomerRealm.getCustomerById(wpDataDB.getClient_id()).getNm())
                        .append(" нет доп. материалов по этому адресу");
                signal = false;

            } else if (offsetSum == virtualTable.size()) {
                msg.append("Все доп.материалы были изменены после текущего посещения, проверка не выполняется.");

                signal = true;
            } else if (nedotochSum > 0) {

                msg.append("За период с ")
                        .append(Clock.getHumanTimeDDMMYYYY(dateFrom))
                        .append(" по ")
                        .append(Clock.getHumanTimeDDMMYYYY(dateTo))
                        .append(" ")
                        .append(wpDataDB.getUser_txt())
                        .append(" НЕ поставил оценку(и) по ")
                        .append(nedotochSum)
                        .append(" Доп.материалам. ");
                Globals.writeToMLOG("OptionControlAdditionalMaterialsMark.executeOption","AdditionalMaterialsJOINAdditionalMaterialsAddressSDB: ",json);

                signal = true;
            } else if (virtualTable.size() > 1 && deviationFromTheMeanSum < 0.5) {

                msg.append("Вы оценили Все (")
                        .append(virtualTable.size())
                        .append(") Доп.материалы ОДНОЙ и той-же оценкой (")
                        .append(averageRating)
                        .append(")! Это НЕ даёт возможность улучшить их качество! " +
                                "Оценивайте эти материалы ОБЬЕКТИВНО!");

                signal = true;
            } else {
                msg.append("За период с ")
                        .append(Clock.getHumanTimeDDMMYYYY(dateFrom))
                        .append(" по ")
                        .append(Clock.getHumanTimeDDMMYYYY(dateTo))
                        .append(" ")
                        .append(wpDataDB.getUser_txt())
                        .append(" поставил оценку(и) по ")
                        .append(virtualTable.size())
                        .append(" Доп.материалам. Замечаний по выполнению опции нет.");

                signal = false;
            }

            // Установка сообщения
            stringBuilderMsg = msg;

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

            // 6.0
            // Установка блокирует ли опция работу приложения или нет
            if (signal) {
                if (optionDB.getBlockPns().equals("1")) {
                    setIsBlockOption(signal);
                    stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
                } else {
                    stringBuilderMsg.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете ставить оценки Доп.материалам.");
                }
            }
//            setIsBlockOption(signal);

            checkUnlockCode(optionDB);

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAdditionalMaterialsMark/executeOption", "Exception e: " + e);
        }
    }

}
