package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;

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
import ua.com.merchik.merchik.data.Database.Realm.VirtualAdditionalRequirementsDB;
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
    private boolean signal = true;

    private WpDataDB wpDataDB;
    private long date;

    public OptionControlAdditionalRequirementsMark(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        try {
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;
                date = Clock.dateConvertToLong(Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000));
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAdditionalRequirementsMark/getDocumentVar", "Exception e: " + e);
        }
    }

    private void executeOption() {
        try {
            double averageRating = 0;  // Средняя Оценка
            double deviationFromTheMeanSize = 0;    // Отклонение от среднего
            int markSum = 0;
            int nedotochSize = 0;

            StringBuilder msg = new StringBuilder();


            long dt = date;       // Дата документа в Unix
            long dateFrom = Clock.getDatePeriodLong(date, -30) / 1000; // Дата документа -30 дней
            long dateTo = Clock.getDatePeriodLong(date, +3) / 1000;     // Дата документа +3 дня

            // Получаем Доп.Требования.
            RealmResults<AdditionalRequirementsDB> realmResults = AdditionalRequirementsRealm.getData3(document);
            List<AdditionalRequirementsDB> data = RealmManager.INSTANCE.copyFromRealm(realmResults);

            // Получаем Оценки этих Доп. требований.
            RealmResults<AdditionalRequirementsMarkDB> marks = AdditionalRequirementsMarkRealm.getAdditionalRequirementsMarks(dateFrom, dateTo, wpDataDB.getUser_id(), data);

            Gson gson = new Gson();

            String json = gson.toJson(data);

            Type listType = new TypeToken<ArrayList<VirtualAdditionalRequirementsDB>>() {
            }.getType();
            List<T> test = new Gson().fromJson(json, listType);
            List<VirtualAdditionalRequirementsDB> virtualTable = (List<VirtualAdditionalRequirementsDB>) test;

            for (VirtualAdditionalRequirementsDB item : virtualTable) {
                if (marks.get(0).getScore() != null && !marks.get(0).getScore().equals("") && !marks.get(0).getScore().equals("0")) {
                    item.mark = Integer.valueOf(marks.get(0).getScore());
                    item.dtChange = String.valueOf(marks.get(0).getDt());
                }

                if (Long.parseLong(item.dtChange) >= dt) {
                    item.nedotoch = 0;
                    item.notes = "ДТ измененно ПОСЛЕ проведения работ и проверке не подлежит";
                } else if (Clock.dateConvertToLong(item.dtEnd) == dt) {
                    item.nedotoch = 0;
                    item.notes = "у ДТ заканчивается срок действия и голосование по нему проверке не подлежит";
                } else if (item.mark == 0) {
                    item.nedotoch = 1;
                    item.notes = "";
                } else {
                    item.nedotoch = 0;
                    item.notes = "";
                }


                nedotochSize = +item.nedotoch;
                markSum = +item.mark;
            }


            try {
                averageRating = markSum / (virtualTable.size() - nedotochSize);
            } catch (Exception e) {
                averageRating = 0;
            }

            for (VirtualAdditionalRequirementsDB item : virtualTable) {
                item.deviationFromTheMean = Math.abs(averageRating - item.mark);
                deviationFromTheMeanSize = +item.deviationFromTheMean;
            }
            // Математика закончена


            // Установка сигналов.
            // У меня 2 это 0 в 1С, а 1 это 1 в 1С
            if (virtualTable.size() == 0) {

                msg.append("У клиента ")
                        .append(CustomerRealm.getCustomerById(wpDataDB.getClient_id()).getNm())
                        .append(" нет доп. требований по этому адресу");
                signal = false;

            } else if (nedotochSize > 0) {

                msg.append("За период с ")
                        .append(Clock.getHumanTime3(dateFrom))
                        .append(" по ")
                        .append(Clock.getHumanTime3(dateTo))
                        .append(" ")
                        .append(wpDataDB.getUser_txt())
                        .append(" НЕ поставил оценку(и) по ")
                        .append(nedotochSize)
                        .append(" Доп.требованиям. ");

                signal = true;
            } else if (virtualTable.size() > 1 && deviationFromTheMeanSize < 0.5) {

                msg.append("Вы оценили Все (")
                        .append(virtualTable.size())
                        .append(") Доп.требований ОДНОЙ и той-же оценкой (")
                        .append(averageRating)
                        .append(")! Это НЕ даёт возможность улучшить их качество! " +
                                "Оценивайте эти требования ОБЬЕКТИВНО!");

                signal = true;
            } else {
                msg.append("За период с ")
                        .append(Clock.getHumanTime3(dateFrom))
                        .append(" по ")
                        .append(Clock.getHumanTime3(dateTo))
                        .append(" ")
                        .append(wpDataDB.getUser_txt())
                        .append(" поставил оценку(и) по ")
                        .append(virtualTable.size())
                        .append(" Доп.требованиям. Замечаний по выполнению опции нет.");

                signal = true;
            }


            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionDB != null) {
                    if (signal){
                        optionDB.setIsSignal("1");
                    }else {
                        optionDB.setIsSignal("2");
                    }
                    realm.insertOrUpdate(optionDB);
                }
            });

            // 6.0
            setIsBlockOption(signal);

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAdditionalRequirementsMark/executeOption", "Exception e: " + e);
        }
    }
}
