package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.HIDE_FOR_USER;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Realm.VirtualAdditionalRequirementsDB;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;

public class OptionControlAdditionalRequirementsMark<T> extends OptionControl {
    public int OPTION_CONTROL_ADD_COMMENT_ID = 138341;
    public boolean signal = true;

    private WpDataDB wpDataDB;
    private long dateDocumentLong;

    public OptionControlAdditionalRequirementsMark(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
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
            Globals.writeToMLOG("ERROR", "OptionControlAdditionalRequirementsMark/getDocumentVar", "Exception e: " + e);
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

            // Для формирования итогового сообщения
            StringBuilder msg = new StringBuilder();

            // Создание виртуальной таблички.
            List<VirtualAdditionalRequirementsDB> virtualTable = new ArrayList<>();

            long dateFrom = Clock.getDatePeriodLong(dateDocumentLong, -15) / 1000; // Дата документа -15 дней
            long dateTo = Clock.getDatePeriodLong(dateDocumentLong, +4) / 1000;     // Дата документа +3 дня

            Integer ttCategory = null;

            AddressSDB addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
            if (addressSDB != null) {
                ttCategory = addressSDB.ttId;
            }

            // 3.2
            // Получаем Доп.Требования.
//            RealmResults<AdditionalRequirementsDB> realmResults = AdditionalRequirementsRealm.getData3(document, HIDE_FOR_USER, ttCategory, 1);
//            List<AdditionalRequirementsDB> data = RealmManager.INSTANCE.copyFromRealm(realmResults);

            List<AdditionalRequirementsDB> data = AdditionalRequirementsRealm.getData3(document, HIDE_FOR_USER, ttCategory, null, 1);

//            // DEBUG DATA-------------
//            try {
//                for (AdditionalRequirementsDB item : data) {
//                    JsonObject object = new Gson().fromJson(new Gson().toJson(item), JsonObject.class);
//                    Globals.writeToMLOG("INFO", "OptionControlAdditionalRequirementsMark/createTZN", "stringBuilderDEBUG: " + object);
//                }
//                Globals.writeToMLOG("INFO", "OptionControlAdditionalRequirementsMark/createTZN", "data.size: " + data.size());
//            } catch (Exception e) {
//                Globals.writeToMLOG("INFO", "OptionControlAdditionalRequirementsMark/createTZN", "stringBuilderDEBUG/Exception e: " + e);
//            }
//            // -----------------------

            Log.e("OptionControlARMark", "1");

            // Проверяем, есть ли вообще данные
            if (data != null && data.size() > 0) {

                // Получаем Оценки этих Доп. требований.
                List<AdditionalRequirementsMarkDB> marks = AdditionalRequirementsMarkRealm.getAdditionalRequirementsMarks(dateFrom, dateTo, wpDataDB.getUser_id(), "1", data);

//                // DEBUG DATA-------------
//                try {
//                    Globals.writeToMLOG("INFO", "OptionControlAdditionalRequirementsMark/createTZN.AdditionalRequirementsMarkDB", "marks.size: " + testMark.size());
//                    for (AdditionalRequirementsMarkDB item : testMark) {
//                        JsonObject object = new Gson().fromJson(new Gson().toJson(item), JsonObject.class);
//                        Globals.writeToMLOG("INFO", "OptionControlAdditionalRequirementsMark/createTZN.AdditionalRequirementsMarkDB", "stringBuilderDEBUG: " + object);
//                    }
//                } catch (Exception e) {
//                    Globals.writeToMLOG("INFO", "OptionControlAdditionalRequirementsMark/createTZN.AdditionalRequirementsMarkDB", "stringBuilderDEBUG/Exception e: " + e);
//                }
//                // -----------------------

                // "Виртуальную" таблицу, как в 1С
                Gson gson = new Gson();
                String json = gson.toJson(data);
                Type listType = new TypeToken<ArrayList<VirtualAdditionalRequirementsDB>>() {
                }.getType();

                Log.e("OptionControlARMark", "3");

                // Запись того что выше накопировани в виртуальную табличку
                virtualTable = new Gson().fromJson(json, listType);

                Log.e("OptionControlARMark", "4");

                for (VirtualAdditionalRequirementsDB item : virtualTable) {

                    item.offset = 0;
                    item.nedotoch = 1; //по умолчанию оценки НЕТ
                    item.note = "Нет ни одной оценки по этому Доп.требованию поставленной " + wpDataDB.getUser_txt();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Log.e("OptionControlARMark", "5");
                        AdditionalRequirementsMarkDB markDB = marks.stream().filter(m -> m.getItemId().equals(item.id)).findFirst().orElse(null);
                        if (markDB != null) {
//                            AdditionalRequirementsMarkDB currentMark = INSTANCE.copyFromRealm(marks.where().equalTo("itemId", item.id).findFirst());
                            AdditionalRequirementsMarkDB currentMark = markDB;

                            Log.e("OptionControlARMark", "5.1");

                            if (currentMark != null) {
                                item.mark = Integer.valueOf(currentMark.getScore());
                                // Эту оценку никто нигде не использует, по этому я не буду лишний раз парсить тудой сюдой
                                // Тзн.ДатаОценки=ПолучитьДатуИзUnix(СокрЛП(ТзнОцен.ДатаЮ));
                            } else {
                                item.mark = 0;
                            }

                            Log.e("OptionControlARMark", "5.2");

                            if (Long.parseLong(item.dtChange) >= dateDocumentLong) {
                                item.nedotoch = 0;
                                item.offset = 1;
                                item.notes = "ДТ измененно ПОСЛЕ проведения работ и проверке не подлежит";
                                continue;
                            } else if (item.dtEnd != null && item.dtEnd.getTime() == dateDocumentLong) {
                                item.nedotoch = 0;
                                item.notes = "у ДТ заканчивается срок действия и голосование по нему проверке не подлежит";
                                continue;
                            } else if (item.mark == 0) {
                                continue;
                            }

                            Log.e("OptionControlARMark", "5.3");

                            item.nedotoch = 0;
                            item.note = "";
                        } else {
                            item.mark = 0;
                        }
                    }
                }

                Log.e("OptionControlARMark", "6");

                //подсчитаем отклонение от средней оценки (для того, чтобы ребята не ставили ОДНУ и ту-же оценку по всем ДТ)
                try {
                    markSum = virtualTable.stream().map(table -> table.mark).reduce(0, Integer::sum);
                    nedotochSum = virtualTable.stream().map(table -> table.nedotoch).reduce(0, Integer::sum);
                    offsetSum = virtualTable.stream().map(table -> table.offset).reduce(0, Integer::sum);

                    averageRating = (double) markSum / (virtualTable.size() - nedotochSum);
                } catch (Exception e) {
                    averageRating = 0;
                }

                Log.e("OptionControlARMark", "7");

                // Считаем отклонение от среднего, для каждого элемента
                for (VirtualAdditionalRequirementsDB item : virtualTable) {
                    item.deviationFromTheMean = Math.abs(averageRating - item.mark);
                }

                // Подсчёт суммы отклонения от среднего
                deviationFromTheMeanSum = virtualTable.stream().map(table -> table.deviationFromTheMean).reduce(0.0d, Double::sum);

//                deviationFromTheMeanSum = virtualTable.stream()
//                        .mapToDouble(table -> table.deviationFromTheMean)
//                        .min()
//                        .orElse(0.0d); // Значение по умолчанию, если коллекция пуста
            }

            Log.e("OptionControlARMark", "8");

//            // DEBUG DATA-------------
            try {
                Globals.writeToMLOG("INFO", "OptionControlAdditionalRequirementsMark/createTZN.virtualTable", "virtualTable.size: " + virtualTable.size());
                if (!virtualTable.isEmpty())
                    for (VirtualAdditionalRequirementsDB item : virtualTable) {
                        JsonObject object = new Gson().fromJson(new Gson().toJson(item), JsonObject.class);
                        Globals.writeToMLOG("INFO", "OptionControlAdditionalRequirementsMark/createTZN.virtualTable", "stringBuilderDEBUG: " + object);
                    }
            } catch (Exception e) {
                Globals.writeToMLOG("INFO", "OptionControlAdditionalRequirementsMark/createTZN.virtualTable", "stringBuilderDEBUG/Exception e: " + e);
            }
//            // -----------------------


            // Установка сигналов.
            // TODO ИначеЕсли ((ПустоеЗначение(Исп.ДатаОМ05)=1) или (Дат<=Исп.ДатаОМ05)) и (ДокИст.Вид()="ОтчетИсполнителя") Тогда
            //		глТекстЧата="Не проверяю оценку Доп.требований до 5-й отчетности.";
            //		СигнКон=0;
            if (virtualTable.isEmpty()) {

                msg.append("У клиента ")
//                        .append(CustomerRealm.getCustomerById(wpDataDB.getClient_id()).getNm())
                        .append(SQL_DB.customerDao().getById(wpDataDB.getClient_id()).nm)
                        .append(" нет доп. требований по этому адресу");
                signal = false;
//                unlockCodeResultListener.onUnlockCodeSuccess();
            } else if (offsetSum == virtualTable.size()) {
                msg.append("Все доп.требования были изменены после текущего посещения, проверка не выполняется.");

                signal = true;
//                unlockCodeResultListener.onUnlockCodeFailure();
            } else if (nedotochSum > 0) {

                msg.append("За период с ")
                        .append(Clock.getHumanTimeDDMMYYYY(dateFrom))
                        .append(" по ")
                        .append(Clock.getHumanTimeDDMMYYYY(dateTo))
                        .append(" ")
                        .append(wpDataDB.getUser_txt())
                        .append(" НЕ поставил оценку(и) по ")
                        .append(nedotochSum)
                        .append(" Доп.требованиям. ");

                signal = true;
//                unlockCodeResultListener.onUnlockCodeFailure();
            } else if (virtualTable.size() > 1 && deviationFromTheMeanSum < 0.5) {

                msg.append("Вы оценили Все (")
                        .append(virtualTable.size())
                        .append(") Доп.требований ОДНОЙ и той-же оценкой (")
                        .append(averageRating)
                        .append(")! Это НЕ даёт возможность улучшить их качество! " +
                                "Оценивайте эти требования ОБЬЕКТИВНО!");

                signal = true;
//                unlockCodeResultListener.onUnlockCodeFailure();
            } else {
                msg.append("За период с ")
                        .append(Clock.getHumanTimeDDMMYYYY(dateFrom))
                        .append(" по ")
                        .append(Clock.getHumanTimeDDMMYYYY(dateTo))
                        .append(" ")
                        .append(wpDataDB.getUser_txt())
                        .append(" поставил оценку(и) по ")
                        .append(virtualTable.size())
                        .append(" Доп.требованиям. Замечаний по выполнению опции нет.");


                signal = false;
//                unlockCodeResultListener.onUnlockCodeSuccess();
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
                    stringBuilderMsg.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете ставить оценки Доп.требованиям.");
                }
            }
//            setIsBlockOption(signal);


            checkUnlockCode(optionDB);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAdditionalRequirementsMark/executeOption", "Exception e: " + e);
        }
    }
}
