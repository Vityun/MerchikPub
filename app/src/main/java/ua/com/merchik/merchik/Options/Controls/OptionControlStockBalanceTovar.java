package ua.com.merchik.merchik.Options.Controls;

import ua.com.merchik.merchik.Utils.FaceValue;
import ua.com.merchik.merchik.Utils.CountValue;

import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA_ID;
import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Date;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.DoubleSpinner;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.EditTextAndSpinner;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Number;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Text;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.PhotoDescriptionText;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.ErrorDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

/**
 * 06.06.2022
 * <p>
 * ID: 80977
 * Опция контроля наличия Акции у Товаров.
 */
public class OptionControlStockBalanceTovar<T> extends OptionControl {

    public int OPTION_CONTROL_STOCK_BALANCE_TOVAR = 141067;

    public boolean signal = true;


    private String documentDate, clientId, optionId;
    private int addressId, userId;
    private long dad2;

    private UsersSDB documentUser;

    private int tovarNaVitrineSUM  = 0;
    private int tovarPoUchetSUM = 0;

    public OptionControlStockBalanceTovar(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        try {
            this.context = context;
            this.document = document;
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;
            }
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            this.unlockCodeResultListener = unlockCodeResultListener;

            getDocumentVar();
            executeOption();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPromotion", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wpDataDB = (WpDataDB) document;

//            documentDate = Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000); //+TODO CHANGE DATE

            clientId = wpDataDB.getClient_id();
            addressId = wpDataDB.getAddr_id();
            userId = wpDataDB.getUser_id();
            dad2 = wpDataDB.getCode_dad2();

            documentUser = SQL_DB.usersDao().getUserById(userId);
        }
    }


    private void executeOption() {
        // values
//        int OSV = 0;            // ОсобоеВнимание


        // 1.0. Определим переменные
        int numberSKUForFactSUM = 0;
        double numberSKUMiddle;
        int numberMin = CountValue.nonNegative(optionDB.getAmountMin());

        int signalInt = 0;         // Сигнал заблокированно или нет
        long days20ago = StockBalancePolicy.cutoffSeconds(System.currentTimeMillis());


        // 2.0. Получим данные о товарах в отчете
        List<ReportPrepareDB> reportPrepare = RealmManager.INSTANCE.copyFromRealm(
                ReportPrepareRealm.getReportPrepareByDad2(dad2)
        );

        if (reportPrepare.isEmpty()) {
            spannableStringBuilder.append("Нет данных для анализа.");
            setIsBlockOption(false);
            return;
        }

        // 3.0. Сформируем итоговую таблицу
        for (ReportPrepareDB item : reportPrepare) {
            int stock = CountValue.rounded(item.oborotvedNum);
            item.numberSKUForAccounting = stock > 0 ? 1 : 0;
            item.numberSKUForFact = FaceValue.rounded(item.face) > 0 ? 1 : 0;
            item.difference = Math.subtractExact(stock, item.amount);
            item.errorExist = 0;
            item.errorNote = "";
        }

        //4.0. сформируем результирующую таблицу
        //4.1. сперва отметим те позиции у которых ЕСТЬ не нулевой остаток для того, чтобы подсчитать СРЕДНИЙ тованый запас. Это нужно, чтобы не наказывать за товар, которого ОТНОСИТЕЛЬНО МАЛО на складе
//        numberSKUForAccountingSUM = reportPrepare.stream().map(table -> table.numberSKUForAccounting).reduce(0, Integer::sum);
        numberSKUForFactSUM = reportPrepare.stream().map(table -> table.numberSKUForFact).reduce(0, Integer::sum);
        numberSKUMiddle = StockBalancePolicy.average(
                reportPrepare.stream().mapToLong(table -> table.amount).sum(), numberSKUForFactSUM);


        //4.2. теперь, имея средний товарный запас, можно сравнить наличие этого товара на витрине (показатель Фейс) с товарным запасом
        for (ReportPrepareDB item : reportPrepare) {
            //сравниваем реквизиты Фейс и ОборотВед
            int stock = CountValue.rounded(item.oborotvedNum);
            if (stock > 0 && item.numberSKUForFact == 0) {
                item.errorExist = 1;
                item.errorNote = "Товар (" + item.tovarId + ") ВІДСУТЕН на вітрині (Фейс=0), але ЧИСЛИТЬСЯ на складі (ОборотВед=" + stock + ")";
            }
            //если товарный запас ДАННОГО товара в десять раз меньше СРЕДНЕГО то нарушения НЕТ ... это может быть просто бой/бомбаж/пересортица
            if (item.errorExist == 1 && StockBalancePolicy.belowMinimum(stock, numberMin, numberSKUMiddle)) {
                item.errorExist = 0;
                item.errorNote = item.errorNote + ", при цьому залишок по обліку усього " + stock +
                        " шт, (що менше " + (numberMin > 0 ? String.valueOf(numberMin) : "1/10 середнього " + (numberSKUMiddle / 10.0)) + "). Робимо виключення.";
            }
            //якщо дані про залишки товару отримані більше 20-и діб тому, ми не вважаємо це за помилку.
            else if (item.errorExist == 1 && item.dtChange < days20ago) {
                item.errorExist = 0;
                item.errorNote = item.errorNote + ", при цьому дані про залишок по обліку отримані більш ніж 20-ть діб тому. Робимо виключення.";
            } else if (item.errorExist == 1 && item.errorId != null && !item.errorId.trim().isEmpty()
                    && new java.math.BigDecimal(item.errorId.trim()).signum() > 0) {
                item.errorExist = 0;
                item.errorNote = item.errorNote + ", при цьому зазначена 'помилка'" +
//                        " \"+НайтиЭлементСпр(\"Ошибки\",\"Код\",ТзнСКЮ.ОшибкаТовара)+\"" +
                        ". Робимо виключення.";
            }
        }


        //5.0. готовим сообщение и сигнал
        tovarNaVitrineSUM = reportPrepare.stream()
                .mapToInt(table -> table.numberSKUForFact)
                .sum(); //количество СКЮ на витрине
        tovarPoUchetSUM = reportPrepare.stream()
                .mapToInt(table -> table.numberSKUForAccounting)
                .sum(); //количество СКЮ по учету
        int errorAll = reportPrepare.stream()
                .mapToInt(table -> table.errorExist)
                .sum();


        if (errorAll > 0) {
            signalInt = 1;
            spannableStringBuilder.append("На вітрині стоїть ")
                    .append(String.valueOf(tovarNaVitrineSUM))
                    .append(" товарів (СКЮ), а на обліку числиться ")
                    .append(String.valueOf(tovarPoUchetSUM))
                    .append(" СКЮ.")
                    .append(" Ви повинні, або знайти усі ці товари, встановити їх на вітрині, та зазначити у звітності, або зазначити причину відсутності кожного з них, у реквізиті 'помилка'.");
        } else {
            signalInt = 0;
            spannableStringBuilder.append("На вітрині стоїть ")
                    .append(String.valueOf(tovarNaVitrineSUM))
                    .append(" товарів (СКЮ), а на обліку числиться ")
                    .append(String.valueOf(tovarPoUchetSUM))
                    .append(" СКЮ. Зауважень нема.");
        }

        //6.0 Виключення
        if (signalInt == 1) {
            if (documentUser != null && (documentUser.reportDate05 == null || documentUser.reportDate05.before(wpDataDB.getDt()))) {
                signalInt = 0;
                spannableStringBuilder.append(", але виконавець ще не провів свого 5-го звіту. Робимо виключення.");
            }
        }


        if (signalInt == 1) {
            signal = true;
        } else {
            signal = false;
        }

        // 7.0 сохраним сигнал
        if (optionDB.getIsSignal().equals("0")) {
            saveOption(String.valueOf(signalInt));
        }

        // 8.0 Блокировка проведения
        if (signalInt == 1) {
            setIsBlockOption(true);
        } else {
            setIsBlockOption(false);
        }

        // Сохранение
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                if (signal) {
                    double penalty = calculatePenalty(wpDataDB);
                    optionDB.setIsSignal("1");
                    optionDB.setSumPenalty(String.valueOf(penalty));
                } else {
                    optionDB.setIsSignal("2");
                    optionDB.setSumPenalty("0.00");
                }
                realm.insertOrUpdate(optionDB);
            }
        });


        if (signal) {
            if (optionDB.getBlockPns().equals("1")) {
                setIsBlockOption(signal);
                spannableStringBuilder.append("\n\n").append("Документ проведен не будет!");
            } else {
                spannableStringBuilder.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете делать Достижения.");
            }
        }

        checkUnlockCode(optionDB);
        // Если есть какой-то сигнал - нужно вывести сообщение

    }

    public String currentStockBalanceCount() {

        return tovarNaVitrineSUM +"/" + tovarPoUchetSUM;
    }


    private void saveOption(String signal) {
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                optionDB.setIsSignal(signal);
                realm.insertOrUpdate(optionDB);
            }
        });
    }

}
