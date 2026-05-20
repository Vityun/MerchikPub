package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.RecycleViewDRAdapterTovar;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.ArticleSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;


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
    private SpannableStringBuilder tovs = new SpannableStringBuilder();

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
            UsersSDB usersSDB = SQL_DB.usersDao().getUserById(wpDataDB.getUser_id());

// 3. Получим данные из отчета

            List<ReportPrepareDB> reportPrepare = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(wpDataDB.getCode_dad2()));
//          4.
            int totalErrors = 0;
            int withoutBalanceButWithComment = 0;

            if (reportPrepare != null) {
                for (ReportPrepareDB item : reportPrepare) {
                    if (item == null) continue;

                    int oborotvedNum = parseIntSafe(item.oborotvedNum);

                    // Аналог СокрЛП(ТЗнТов.Примечание)
                    String note = getReportNote(item);

                    // Сбрасываем расчетные поля перед новым расчетом
                    item.numberSKUForAccounting = 0;
                    item.error = 0;
                    item.errorExist = 0;
                    item.errorNote = "";
                    item.note = "";

                    /*
                     * Аналог логики:
                     *
                     * ТЗн.Остаток = ТЗнТов.Оборотвед
                     *
                     * Если остаток > 0 — товар считаем в СКЮ по учету.
                     * Если остаток = 0 и примечание пустое — тоже считаем, но ставим нарушение.
                     * Если остаток = 0 и примечание НЕ пустое — НЕ считаем в СКЮ по учету.
                     * Если (ТЗнТов.Оборотвед = 0) и (СокрЛП(ТЗнТов.Примечание) = "") Тогда
                     *     ТЗн.Наруш = 1;
                     *     ТЗн.Прим = "Не указан остаток товара..."
                     * КонецЕсли;
                     */
                    if (oborotvedNum > 0) {
                        // Остаток есть — строка нормальная
                        item.numberSKUForAccounting = 1;

                    } else if (!note.isEmpty()) {
                        // Остатка нет, но есть примечание "товара нет" — тоже нормально
                        item.numberSKUForAccounting = 1;
                        withoutBalanceButWithComment++;

                    } else {
                        // Остатка нет и примечания нет — нарушение
                        item.numberSKUForAccounting = 0;

                        item.error = 1;
                        item.errorExist = 1;
                        item.errorNote =
                                "Не указан остаток товара по Оборотной ведомости. " +
                                        "Если товара по учету нет то укажите это в примечании 'товара нет'.";

                        item.note = item.errorNote;

                        totalErrors++;

                        TovarDB tovar = TovarRealm.getById(item.getTovarId());
                        ArticleSDB articleSDB = SQL_DB.articleDao().getByTovId(Integer.parseInt(tovar.getiD()));
                        item.error = 1;

                        String code = tovar.getiD();
                        if (articleSDB != null && articleSDB.vendorCode != null)
                            code = articleSDB.vendorCode;

                        tovs.append(createLinkedString("(" + code + ") " + tovar.getNm() + "\n", item));
                    }

                    numberoborotvedNumSUM += item.numberSKUForAccounting;

                }
            }
            //            for (ReportPrepareDB item : reportPrepare) {
////                //определяем количество СКЮ
//                int oborotvedNum;
//
//                try {
//                    oborotvedNum = Integer.parseInt(item.oborotvedNum);
//                } catch (Exception ignored) {
//                    oborotvedNum = 0;
//                }
//
//                if (oborotvedNum > 0) {
//                    item.numberSKUForAccounting = 1;
//                } else {
//                    item.numberSKUForAccounting =
//                            item.notes != null && item.notes.length() > 10
//                                    ? 0
//                                    : 1;
//                }
//                item.errorExist = Integer.parseInt(item.errorId) > 0 ? 1 : 0;
//
//                if (item.errorExist == 1)
//                    totalErrors++;
//            }
//            numberSKUForAccountingSUM = reportPrepare.stream().map(table -> table.numberSKUForAccounting).reduce(0, Integer::sum);
//            numberSKUForFactSUM = reportPrepare.stream().map(table -> table.numberSKUForFact).reduce(0, Integer::sum);
//            numberoborotvedNumSUM = reportPrepare.stream().map(table -> table.numberSKUForAccounting).reduce(0, Integer::sum);
//            colSUM = reportPrepare.stream().map(table -> table.amount).reduce(0, Integer::sum);

// 5. Готовим сообщение и сигнал
            if (reportPrepare == null || reportPrepare.isEmpty()) {

                spannableStringBuilder.append(
                        "Товаров, по которым надо проверять остатки, не обнаружено. " +
                                "Скорее всего не заполнена таблица Доп.требований."
                );
                signal = false;

            } else if (totalErrors == 0) {

                if (withoutBalanceButWithComment > 0) {
                    spannableStringBuilder
                            .append("Обнаружено ")
                            .append(String.valueOf(reportPrepare.size()))
                            .append(" товаров для проверки остатков по оборотной ведомости. ")
                            .append("У ")
                            .append(String.valueOf(withoutBalanceButWithComment))
                            .append(" товара остаток не указан, но есть примечание, что товара нет в наличии. Замечаний нет.");
                } else {
                    spannableStringBuilder
                            .append("Обнаружено ")
                            .append(String.valueOf(reportPrepare.size()))
                            .append(" товаров с указанием остатков по оборотной ведомости. Замечаний нет.");
                }

                signal = false;

            } else if (usersSDB.reportDate20 == null && usersSDB.reportCount < 20) {

                spannableStringBuilder
                        .append("Исполнитель ")
                        .append(usersSDB.fio)
                        .append(" еще не провел своего 20-го отчета.");

                signal = false;

            } else {

                spannableStringBuilder
                        .append("Отсутствуют данные об остатках товаров по Оборотной ведомости у ")
                        .append(String.valueOf(totalErrors))
                        .append(" товара. Если товара по учету нет то укажите это в примечании 'товара нет'. ")
                        .append("Вы должны сперва исправить замечания, а затем проводить данный отчет!\n\n")
                        .append(tovs);

                signal = true;
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

    private SpannableString createLinkedString(String msg, ReportPrepareDB rp) {
        SpannableString res = new SpannableString(msg);

        try {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    showNotesRequisiteDialog(rp);
//                    new TovarRequisites(TovarRealm.getById(rp.tovarId), rp).createDialog(context, WpDataRealm.getWpDataRowByDad2Id(Long.parseLong(rp.codeDad2)), optionDB, () -> {
//                    }).show();
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
//                    ds.setColor(Color.GREEN);
                }
            };
            int count = msg.length();
            res.setSpan(clickableSpan, 0, count, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityControlPhotoRemainingGoods/executeOption/createLinkedString/Exception", "Exception e: " + e);
        }
        return res;
    }


    private static int parseIntSafe(String value) {
        if (value == null) return 0;

        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String getReportNote(ReportPrepareDB item) {
        if (item == null) return "";

        if (item.notes != null) {
            return item.notes.trim();
        }

        if (item.note != null) {
            return item.note.trim();
        }

        return "";
    }

    private void showNotesRequisiteDialog(ReportPrepareDB rp) {
        try {
            if (rp == null) return;

            TovarDB tovar = TovarRealm.getById(rp.tovarId);
            WpDataDB wpDataDB = WpDataRealm.getWpDataRowByDad2Id(Long.parseLong(rp.codeDad2));

            if (tovar == null || wpDataDB == null) return;

            TovarOptions notesTpl = getNotesTovarOption();
            if (notesTpl == null) return;

            RecycleViewDRAdapterTovar adapter = new RecycleViewDRAdapterTovar(
                    context,
                    Collections.singletonList(tovar),
                    wpDataDB,
                    RecycleViewDRAdapterTovar.OpenType.DEFAULT
            );

            adapter.showDialog(
                    tovar,
                    notesTpl,
                    rp,
                    rp.tovarId,
                    String.valueOf(wpDataDB.getCode_dad2()),
                    wpDataDB.getClient_id(),
                    "0",
                    "?",
                    false,
                    true
            );

            if (!adapter.dialogList.isEmpty()) {
                adapter.dialogList.get(0).show();
            }
        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlAvailabilityControlPhotoRemainingGoods/showNotesRequisiteDialog",
                    "Exception e: " + e
            );
        }
    }

    private TovarOptions getNotesTovarOption() {
        try {
            List<TovarOptions> all = Options.getTovarOptins();
            if (all == null || all.isEmpty()) return null;

            for (TovarOptions item : all) {
                if (item != null && item.getOptionControlName() == Globals.OptionControlName.NOTES) {
                    return item;
                }
            }

            // запасной вариант, если структура как в текущем mapping
            if (all.size() > 10) {
                return all.get(10);
            }
        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlAvailabilityControlPhotoRemainingGoods/getNotesTovarOption",
                    "Exception e: " + e
            );
        }

        return null;
    }
}
