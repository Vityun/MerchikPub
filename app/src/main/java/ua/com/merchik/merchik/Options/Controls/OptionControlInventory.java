package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;


/*
 * Петров 30.03.2023 (снял копию с опции СравнОстНал) и адаптрировал под проведение инвентаризации.
 * Выполняется СВЕРКА реквизитов "остаток (по учету)" (с учетом даты актуальности остатков) и "Кол" (в данном случае, это ПОЛНЫЙ ФАКТИЧЕСКИЙ товарный запас Витрина+Склад). Если товар числится на остатках, но не указан в отчете, то необходимо указать причину, иначе - возвращаем сигнал=1
 * предполагаю, что набор реквизитов, которые должен заполнить исполнитель (на пример ДОСГ или Возврат ... будет регулироваться каким-нибудь переключателем типа КолМин или Цена)
 * в будущем, может понадобится контролировать МАКСИМАЛЬНІЙ ПРОЦЕНТ отклонения УЧЕТНОГО СКЮ (или тованого запаса) от ФАКТИЧЕСКОГО СКЮ (или тованого запаса)
 * */
public class OptionControlInventory<T> extends OptionControl {

    public int OPTION_CONTROL_INVENTORY_ID = 575;

    private boolean signal = true;

    private int colMax;
    private int numberSKUForAccountingSUM;     // реквизит для подсчета количества СКЮ по УЧЕТУ
    private int numberSKUForFactSUM;           // реквизит для подсчета количества СКЮ по ФАКТУ
    private int numberoborotvedNumSUM;
    private int colSUM;
    private int errorSUM;
//    private int difference;                 // Разница

    private int percentageDeviationNumberSKYU;      // Відсоток відхилення кількості СКЮ
    private int percentageDeviationTotalInventory;  // Відсоток відхилення загального товарного запасу

    private StringBuilder resMassage = new StringBuilder();

    private WpDataDB wpDataDB;
    private AddressSDB addressSDBDocument;
    private CustomerSDB customerSDBDocument;
    private UsersSDB usersSDBDocument;

    public OptionControlInventory(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        getDocumentVar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeOption();
        }
    }

    private void getDocumentVar() {
        //1.0. определим переменные
        try {
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;

                customerSDBDocument = SQL_DB.customerDao().getById(wpDataDB.getClient_id());
                usersSDBDocument = SQL_DB.usersDao().getById(wpDataDB.getUser_id());
                addressSDBDocument = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());

                colMax = Integer.parseInt(optionDB.getAmountMax()) > 0 ? Integer.parseInt(optionDB.getAmountMax()) : 20;    //не более 20% нарушений.
            }

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlInventory/getDocumentVar", "Exception e: " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        try {
            //2.0. получим данные о товарах в отчете (если она еще не расчитана)
            List<ReportPrepareDB> reportPrepare = ReportPrepareRealm.getReportPrepareByDad2(wpDataDB.getCode_dad2());


            //3.0. сформируем итоговую таблицу
            List<ReportPrepareDB> resultRP = new ArrayList<>();


            //3.2. сравним количество товара на складе + витрине с товарным запасом по УЧЕТУ
            for (ReportPrepareDB item : reportPrepare) {
                //определяем количество СКЮ
                item.numberSKUForAccounting = Integer.parseInt(item.oborotvedNum) > 0 ? 1 : 0;
                item.numberSKUForFact = item.amount > 0 ? 1 : 0;
                item.difference = Integer.parseInt(item.oborotvedNum) - item.amount;

                //сравниваем реквизиты Фейс и (ОборотВед
                if (Integer.parseInt(item.face) > 0 && item.amount == 0) {
                    item.error = 1;
                    item.errorNote = "Товар є в НАЯВНОСТІ на вітрині (Фейс>0), та НЕ зазначений на ФАКТИЧНИХ залишках на (склад + вітрина)";
                }

                //сравниваем реквизиты ОборотВед и Остаток по Факту (Склад + Витрина)
                if (Integer.parseInt(item.oborotvedNum) > 0 && item.amount == 0) {
                    item.error = 1;
                    item.errorNote = "Товар рахується по ОБЛІКУ (ОборотВед>0), та НЕ зазначений на ФАКТИЧНИХ залишках на (склад + вітрина) і користувач не зазначив 'помилку'";
                }

                //сравниваем реквизиты ОборотВед и Остаток по Факту (Склад + Витрина)
                if (Integer.parseInt(item.oborotvedNum) == 0 && item.amount > 0) {
                    item.error = 1;
                    item.errorNote = "Товар фактично є в НАЯВНОСТІ (склад + вітрина), та не значиться по ОБЛІКУ (ОборотВед>0) і користувач не зазначив 'помилку'";
                }

                if (item.error == 1 && Integer.parseInt(item.tovarError) > 0) {
                    item.error = 0;
                    item.errorNote = "";
                }
            }


            //4.0. подсчитаем процент отклонения СКЮ (учетного от фактического)
            numberSKUForAccountingSUM = reportPrepare.stream().map(table -> table.numberSKUForAccounting).reduce(0, Integer::sum);
            numberSKUForFactSUM = reportPrepare.stream().map(table -> table.numberSKUForFact).reduce(0, Integer::sum);
            numberoborotvedNumSUM = reportPrepare.stream().map(table -> Integer.parseInt(table.oborotvedNum)).reduce(0, Integer::sum);
            colSUM = reportPrepare.stream().map(table -> table.amount).reduce(0, Integer::sum);

            try {
                percentageDeviationNumberSKYU = 100 * numberSKUForAccountingSUM / numberSKUForFactSUM;
            } catch (Exception e) {
                percentageDeviationNumberSKYU = 100;
            }

            try {
                percentageDeviationTotalInventory = 100 * numberoborotvedNumSUM / colSUM;
            } catch (Exception e) {
                percentageDeviationTotalInventory = 100;
            }

            //5.0. готовим сообщение и сигнал
            errorSUM = reportPrepare.stream().map(table -> table.error).reduce(0, Integer::sum);
            if (reportPrepare.size() == 0) {
                stringBuilderMsg.append("Нема даних для аналізу.");
                signal = false;
            } else if (errorSUM > 0) {
                stringBuilderMsg.append("по ").append(errorSUM).append(" товарам є зауваження по проведенню інвентарізації.");
                signal = false;
            } else {
                stringBuilderMsg.append("По ОБЛІКУ рахується ").append(numberSKUForAccountingSUM)
                        .append(" товарів (СКЮ), загальною кількістю ").append(numberoborotvedNumSUM)
                        .append(" шт, ФАКТИЧНО знайдено (склад + вітрина) ").append(numberSKUForFactSUM)
                        .append(" товарів загальною кількістю ").append(reportPrepare.size())
                        .append(" шт. Відсоток відхилення СКЮ=").append(percentageDeviationNumberSKYU)
                        .append("%. Відсоток відхилення товарного запасу = ").append(percentageDeviationTotalInventory)
                        .append("%");
                signal = false;
            }


            // ---
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

            // ---
            if (signal) {
                if (optionDB.getBlockPns().equals("1")) {
                    setIsBlockOption(signal);
                    stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
                } else {
                    stringBuilderMsg.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете делать Достижения.");
                }
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlInventory/executeOption", "Exception e: " + e);
        }
    }

}
