package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

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
import ua.com.merchik.merchik.data.RealmModels.PPADB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.PPARealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;


/*
    09.05.2025
    Выполняется проверка наличия остатков по Оборотной ведомости (работает для двух опций - режимов).
    2243 Проверим Предоставление информации о товарном запасе по оборотной ведомости (по всем позициям из ППА),
    135448 Проверим Предоставление информации о товарном запасе по оборотной ведомости (по отдельным позициям указанным в Доп.требованиях)
 * */
public class OptionControlStockTovarLeft<T> extends OptionControl {

    public int OPTION_CONTROL_ = 2243;

    public boolean signal = true;

    private int colMax;
    private int numberSKUForAccountingSUM;     // реквизит для подсчета количества СКЮ по УЧЕТУ
    private int numberSKUForFactSUM;           // реквизит для подсчета количества СКЮ по ФАКТУ
    private int numberoborotvedNumSUM;
    private int colSUM;
    private int errorSUM;
//    private int difference;                 // Разница

    private int percentageDeviationNumberSKYU;      // Відсоток відхилення кількості СКЮ
    private int percentageDeviationTotalInventory;  // Відсоток відхилення загального товарного запасу

//    private StringBuilder resMassage = new StringBuilder();

    private WpDataDB wpDataDB;
    private AddressSDB addressSDBDocument;
    private CustomerSDB customerSDBDocument;
    private UsersSDB usersSDBDocument;

    public OptionControlStockTovarLeft(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;

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
//            //2.0. получим данные о товарах в отчете (если она еще не расчитана)
//            List<PPADB> ppadbList = PPARealm.getPPAIZAList(wpDataDB.getCode_iza(), wpDataDB.getClient_id(), String.valueOf(wpDataDB.getAddr_id()));
//            List<ReportPrepareDB> reportPrepare = ReportPrepareRealm.getReportPrepareByDad2(wpDataDB.getCode_dad2());
//
//
//            //3.0. сформируем итоговую таблицу
//            List<ReportPrepareDB> resultRP = new ArrayList<>();
//
//
//            //3.2. сравним количество товара на складе + витрине с товарным запасом по УЧЕТУ
//            for (ReportPrepareDB item : reportPrepare) {
//                //определяем количество СКЮ
//                item.numberSKUForAccounting = Integer.parseInt(item.oborotvedNum) > 0 ? 1 : 0;
//                item.numberSKUForFact = item.amount > 0 ? 1 : 0;
//                item.difference = Integer.parseInt(item.oborotvedNum) - item.amount;
//
//                //сравниваем реквизиты Фейс и (ОборотВед
//                if (Integer.parseInt(item.face) > 0 && item.amount == 0) {
//                    item.error = 1;
//                    item.errorNote = "Товар є в НАЯВНОСТІ на вітрині (Фейс>0), та НЕ зазначений на ФАКТИЧНИХ залишках на (склад + вітрина)";
//
//                    resultRP.add(item);
//                }
//
//                //сравниваем реквизиты ОборотВед и Остаток по Факту (Склад + Витрина)
//                if (Integer.parseInt(item.oborotvedNum) > 0 && item.amount == 0) {
//                    item.error = 1;
//                    item.errorNote = "Товар рахується по ОБЛІКУ (ОборотВед>0), та НЕ зазначений на ФАКТИЧНИХ залишках на (склад + вітрина) і користувач не зазначив 'помилку'";
//
//                    resultRP.add(item);
//                }
//
//                //сравниваем реквизиты ОборотВед и Остаток по Факту (Склад + Витрина)
//                if (Integer.parseInt(item.oborotvedNum) == 0 && item.amount > 0) {
//                    item.error = 1;
//                    item.errorNote = "Товар фактично є в НАЯВНОСТІ (склад + вітрина), та не значиться по ОБЛІКУ (ОборотВед>0) і користувач не зазначив 'помилку'";
//
//                    resultRP.add(item);
//                }
//
//                if (item.error == 1 && Integer.parseInt(item.tovarError) > 0) {
//                    item.error = 0;
//                    item.errorNote = "";
//                }
//            }
//
//
//            //4.0. подсчитаем процент отклонения СКЮ (учетного от фактического)

//
//            try {
//                percentageDeviationNumberSKYU = 100 * numberSKUForAccountingSUM / numberSKUForFactSUM;
//            } catch (Exception e) {
//                percentageDeviationNumberSKYU = 100;
//            }
//
//            try {
//                percentageDeviationTotalInventory = 100 * numberoborotvedNumSUM / colSUM;
//            } catch (Exception e) {
//                percentageDeviationTotalInventory = 100;
//            }
//
//            //5.0. готовим сообщение и сигнал
//            errorSUM = reportPrepare.stream().map(table -> table.error).reduce(0, Integer::sum);
//            if (reportPrepare.size() == 0) {
//                resMassage.append("Нема даних для аналізу.");
//                signal = false;
//            } else if (errorSUM > 0) {
//                resMassage.append("по ").append(errorSUM).append(" товарам є зауваження по проведенню інвентарізації.");
//                signal = true;
//            } else {
//                resMassage.append("По ОБЛІКУ рахується ").append(numberSKUForAccountingSUM)
//                        .append(" товарів (СКЮ), загальною кількістю ").append(numberoborotvedNumSUM)
//                        .append(" шт, ФАКТИЧНО знайдено (склад + вітрина) ").append(numberSKUForFactSUM)
//                        .append(" товарів загальною кількістю ").append(reportPrepare.size())
//                        .append(" шт. Відсоток відхилення СКЮ=").append(percentageDeviationNumberSKYU)
//                        .append("%. Відсоток відхилення товарного запасу = ").append(percentageDeviationTotalInventory)
//                        .append("%");
//                signal = true;
//            }
            UsersSDB usersSDB = SQL_DB.usersDao().getUserById(wpDataDB.getUser_id());

//            // 1. Определим список СКЮ
//            List<PPADB> skuList;
//
////            if (optionDB.getOptionControlId() == OPTION_CONTROL_) {
//            // 2243 – по всем позициям из ППА
//            skuList = RealmManager.INSTANCE.copyFromRealm(
//                    PPARealm.getPPAList(
//                            wpDataDB.getClient_id(),
//                            String.valueOf(wpDataDB.getAddr_id())
//                    ));
////            } else if (wpDataDB.getOption() == GLOption.OPT_135448) {
//////                 135448 – по отдельным позициям из "Доп. требований"
////                skuList = AdditionalRequirementsRealm.getSkuListByRequirements(
////                        wpDataDB.getCode_dad2(),
////                        wpDataDB.getClient_id()
//////                         здесь могут быть и другие параметры, если нужно
////                );
////            } else {
////                skuList = new ArrayList<>();
////            }

// 3. Получим данные из отчета

            List<ReportPrepareDB> reportPrepare = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(wpDataDB.getCode_dad2()));
//
//            for (ReportPrepareDB item : reportPrepare) {
//                item.numberSKUForAccounting = Integer.parseInt(item.oborotvedNum) > 0 ? 1 : 0;
//                item.numberSKUForFact = Integer.parseInt(item.face) > 0 ? 1 : 0;
//                item.difference = Integer.parseInt(item.oborotvedNum) - item.amount;
//            }
//            for (ReportPrepareDB item : reportPrepare) {
//                //сравниваем реквизиты Фейс и ОборотВед
//                if (item.numberSKUForFact > 0) {
//                    item.error = 1;
////                    item.errorNote = "Товар (" + item.tovarId + ") ВІДСУТЕН на вітрині (Фейс=0), але ЧИСЛИТЬСЯ на складі (ОборотВед=" + item.numberSKUForAccounting + ")";
//                }
//            }
//            // 4. Формируем результирующую таблицу
//            List<ReportPrepareDB> result = new ArrayList<>();
//
//            for (PPADB sku : skuList) {
//                String skuCode;
//
//                try {
//                    skuCode = sku.getTovarId();
//                } catch (Exception e) {
//                    skuCode = sku.getTovarId();
//                }
//
//                ReportPrepareDB matched = null;
//                for (ReportPrepareDB item : reportPrepare) {
//                    if (skuCode.equals(item.getTovarId())) {
//                        matched = item;
//                        break;
//                    }
//                }
//
//                ReportPrepareDB newResult = new ReportPrepareDB();
//                newResult.setTovarId(skuCode);
//
//                if (matched == null) {
//                    newResult.error = 1;
//                    newResult.errorNote = "В отчете отсутствует указанный товар.";
//                } else {
//                    int oborotved = Integer.parseInt(matched.oborotvedNum);
//                    newResult.oborotvedNum = matched.oborotvedNum;
//                    newResult.amount = matched.amount;
//
//                    if (oborotved == 0 && (matched.errorNote == null || matched.errorNote.trim().isEmpty())) {
//                        newResult.error = 1;
//                        newResult.errorNote = "Не указан остаток товара по Оборотной ведомости. Если товара по учету нет – укажите это в примечании 'товара нет'.";
//                    }
//                }
//
//                result.add(newResult);
//            }

            int totalErrors = 0;
            for (ReportPrepareDB item : reportPrepare) {
//                //определяем количество СКЮ
                item.numberSKUForAccounting = Integer.parseInt(item.oborotvedNum) > 0 ? 1 : 0;
                item.errorExist = Integer.parseInt(item.errorId) > 0 ? 1 : 0;

                if (item.errorExist == 1)
                    totalErrors++;
            }
//            numberSKUForAccountingSUM = reportPrepare.stream().map(table -> table.numberSKUForAccounting).reduce(0, Integer::sum);
//            numberSKUForFactSUM = reportPrepare.stream().map(table -> table.numberSKUForFact).reduce(0, Integer::sum);
            numberoborotvedNumSUM = reportPrepare.stream().map(table -> table.numberSKUForAccounting).reduce(0, Integer::sum);
//            colSUM = reportPrepare.stream().map(table -> table.amount).reduce(0, Integer::sum);

// 5. Готовим сообщение и сигнал
            if (reportPrepare.isEmpty()) {
                spannableStringBuilder.append("Товаров, по которым надо проверять остатки, не обнаружено. Скорее всего не заполнена таблица Доп.требований.");
                signal = false;
            } else if (reportPrepare.size() == numberoborotvedNumSUM) {
                spannableStringBuilder.append("Обнаружено ").append(String.valueOf(reportPrepare.size())).append(" товаров с указанием остатков по оборотной ведомости. Замечаний нет.");
                signal = false;
            } else if (usersSDB.reportDate20 == null && usersSDB.reportCount < 20) {
                spannableStringBuilder.append("Исполнитель ").append(usersSDB.fio).append(" еще не провел своего 20-го отчета.");
                signal = false;
            } else {
                int different = reportPrepare.size() - numberoborotvedNumSUM;
                if (different > totalErrors) {
                    spannableStringBuilder.append("Отсутствуют данные об остатках товаров по Оборотной ведомости у ")
                            .append(String.valueOf(different)).append(" товара. Если товара по учету нет то укажите это в примечании 'товара нет'. Вы должны сперва исправить замечания, а затем проводить данный отчет!");
                    signal = true;
                } else {
                    spannableStringBuilder.append("Отсутствуют данные об остатках товаров по Оборотной ведомости у ")
                            .append(String.valueOf(different)).append(" товара. Но в примечании указано, что товара нет в наличии. Замечаний нет.");
                    signal = false;
                }
            }


//            spannableStringBuilder.append(resMassage);
//
//            for (ReportPrepareDB item : resultRP) {
//                TovarDB tov = TovarRealm.getById(item.tovarId);
//                spannableStringBuilder.append(createLinkedString(tov.getNm() + " " + item.errorNote, item, tov));
//            }


            //7.0. сохраним сигнал
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionDB != null) {
                    if (signal) {
                        optionDB.setIsSignal("1");
//                    setIsBlockOption(signal);
                    } else {
                        optionDB.setIsSignal("2");
                    }
                    realm.insertOrUpdate(optionDB);
                }
            });

            //8.0. блокировка проведения
            // Установка блокирует ли опция работу приложения или нет
            if (signal) {
                if (optionDB.getBlockPns().equals("1")) {
                    setIsBlockOption(signal);
                    spannableStringBuilder.append("\n\n").append("Документ проведено не буде!");
                } else {
                    spannableStringBuilder.append("\n\n").append("Вы можете отримати Преміальні БІЛЬШЕ, якщо будете збільшувати кількість фейсів товарів замовника на полиці.");
                }
            }

            checkUnlockCode(optionDB);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlInventory/executeOption", "Exception e: " + e);
        }
    }

    private SpannableString createLinkedString(String msg, ReportPrepareDB reportPrepareDB, TovarDB tov) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

                Toast.makeText(textView.getContext(), msg, Toast.LENGTH_LONG).show();

//                Toast.makeText(textView.getContext(), "id: " + reportPrepareDB.getTovarId(), Toast.LENGTH_LONG).show();
//
//                DialogData dialog = new DialogData(textView.getContext());
//                dialog.setTitle("");
//                dialog.setText("");
//                dialog.setClose(dialog::dismiss);
//
//                dialog.setImage(true, getPhotoFromDB(tov));
//                dialog.setAdditionalText(setPhotoInfo(TPL, tov, "", ""));
//
//                dialog.setOperationSpinnerData(setMapData(AKCIYA_ID));
//                dialog.setOperationSpinner2Data(setMapData(Globals.OptionControlName.AKCIYA));
//                dialog.setOperationTextData(reportPrepareDB.getAkciyaId());
//                dialog.setOperationTextData2(reportPrepareDB.getAkciya());
//
//                dialog.setOperation(operationType(TPL), getCurrentData(TPL, reportPrepareDB.getCodeDad2(), reportPrepareDB.getTovarId()), setMapData(TPL.getOptionControlName()), () -> {
//                    if (dialog.getOperationResult() != null) {
//                        operetionSaveRPToDB(TPL, reportPrepareDB, dialog.getOperationResult(), dialog.getOperationResult2(), null, dialog.context);
//                        Toast.makeText(dialog.context, "Внесено: " + dialog.getOperationResult(), Toast.LENGTH_LONG).show();
//                    }
//                });
//
//                dialog.show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return res;
    }

}
