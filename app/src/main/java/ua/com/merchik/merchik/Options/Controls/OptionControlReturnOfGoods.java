package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.DEFAULT;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.ShowTovarRequisites;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;

/**
 * // Петров 28.05.2023 Написал на основании 157243-ПровПричОтсТовОСВ
 * <p>
 * // Выполняется проверка НАЛИЧИЯ данных о количестве ВОЗВРАТА товара или запись в поле "ошибка" о том, что его "возвращать НЕ нужно". Если в ДТ нет товаров с "особым вниманием" то проверяем наличие указания этой информации у КАЖДОГО товара, у которого поле "возврат=0" и "Фейсы">0
 * <p>
 * // ДокИст - документ - источник типа ОтчетИсполнителя, Задача, ОтчетИсполнителя, ОтчетОСтажировке и пр. к которому подчинена данная опция
 * // ДокОпц - документ - набор опций (на момент передачи в эту функцию позиционирован на конкретную строку с ДАННОЙ опцией)
 * // ТзнТов - таблица со списком данных отчета о товарах (используется для ускорения ГРУППОВЫХ расчетов опций)
 * // ВыбЗап =1 сохранить результаты в документе (используется при вызове роботом), 0(пусто)-НЕ выполнять запись документа (вызов выполняется интерактивно из самого документа)
 * // ВыбПред - флаг возвращаемых результатов
 * //			=1 - выводить предупреждение,
 * //			=2 - выводить сообщение,
 * //			=3 - выводить Тзн (в самих функциях в этом режиме ничего делать НЕ надо письмо пишется в функции РазрешитьДействия(), которая их вызывает)
 * //			=4 - выполнить запись в ЧАТ + Сообщение
 * //			=0(пусто) - выполняет все действия, но возвращает 0 без предупреждений и сообщений
 */
public class OptionControlReturnOfGoods<T> extends OptionControl {
    public int OPTION_CONTROL_ReturnOfGoods_ID = 135591;

    private WpDataDB wpDataDB;
    public boolean signal = false;
    public boolean badDate = false;

    private int colMax = 0;

    public OptionControlReturnOfGoods(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        try {
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            this.unlockCodeResultListener = unlockCodeResultListener;

            getDocumentVar();
            executeOption();
        } catch (Exception e) {
            Log.e("OCReturnOfGoods", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            this.wpDataDB = (WpDataDB) document;

            if (optionDB.getAmountMax() != null && !optionDB.getAmountMax().equals("")) {
                int max = Integer.parseInt(optionDB.getAmountMax());
                colMax = max == 0 ? 30 : max;
            }
        }
    }

    private void executeOption() {
        int find = 0;
        int errCnt = 0;
        List<Integer> tovarIds = new ArrayList<>();
        List<ReportPrepareDB> result = new ArrayList<>();
        SpannableStringBuilder resultMsg = new SpannableStringBuilder();

        resultMsg.append("Для товара с ОСВ (Особым Вниманием) Вы должны обязательно указать ПРИЧИНУ его отсутствия.").append("\n\n");

        // Получение Репорт Препэйра
        List<ReportPrepareDB> detailedReportRPList = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(wpDataDB.getCode_dad2()));

        // Получаем Товары с особым вниманием
        List<AdditionalRequirementsDB> additionalRequirementsDBS = AdditionalRequirementsRealm.getData3(document, DEFAULT, null, optionDB.getOptionId(),0);
        for (AdditionalRequirementsDB item : additionalRequirementsDBS) {
            if (item.getTovarId() != null && !item.getTovarId().equals("") && !item.getTovarId().equals("0")) {
                tovarIds.add(Integer.valueOf(item.getTovarId()));
            }
        }

        // проверим, по каким из товаров с ОСВ отсутствуют на витрине?
        int osvSize = tovarIds.size();
        for (ReportPrepareDB item : detailedReportRPList) {

//            TovarDB tovarDB = RealmManager.INSTANCE.copyFromRealm(TovarRealm.getById(item.tovarId));

            boolean osv = false; // Товар с Особым Вниманием

            if (item.getFace() != null && !item.getFace().equals("") && item.getFace().equals("0")) {
                //если товар есть то и проверять нечего
                continue;
            }

            if (osvSize == 0) {
                Log.e("OCReturnOfGoods", "item.iD: " + item.iD);
                Log.e("OCReturnOfGoods", "item.expireLeft: " + item.expireLeft);
                Log.e("OCReturnOfGoods", "item.getErrorId(): " + item.getErrorId());

                long dat = wpDataDB.getDt().getTime() / 1000;
                long colMaxLong = colMax * 86400L;
                long datRes = dat + colMaxLong;
                long test = 0;

                if (item.dtExpire != null && !item.dtExpire.equals("") && !item.dtExpire.equals("0000-00-00")){
                    test = Clock.dateConvertToLong(item.dtExpire)/1000;
                }

                if ((optionDB.getOptionControlId().equals("165275") || optionDB.getOptionId().equals("165275"))
//                if ((optionDB.getOptionControlId().equals("135591") || optionDB.getOptionId().equals("135591"))
                        && (item.dtExpire != null && !item.dtExpire.equals("") && !item.dtExpire.equals("0000-00-00"))
                        && item.expireLeft != null && item.expireLeft.equals("0")
                        && test <= datRes) {
                    errCnt++;
                    item.error = 1;
                    item.errorNote = "Для наявного товара, якщо ДОСГ менше " + Clock.getHumanTimeSecPattern(datRes, "yyyy-MM-dd") + " (" + colMax + " діб) ви зобов'язані зазначити або кількість товару, що підлягає поверненню, або обрати 'помилку' = товар поверненню НЕ підлягає.";
                    result.add(item);
                } else if ((optionDB.getOptionControlId().equals("135591") || optionDB.getOptionId().equals("135591"))
                        && (item.expireLeft != null && item.expireLeft.equals("0")) /*&&
                        (item.getErrorId() != null && !item.getErrorId().equals("") && item.getErrorId().equals("0"))*/) {
                    errCnt++;
                    item.error = 1;
                    item.errorNote = "Для кожного наявного товара ви зобов'язані зазначити або кількіть товару, що підлягає поверненню, або обрати 'помилку' = товар поверненню НЕ підлягає.";
                    result.add(item);
                    Log.e("OCReturnOfGoods", "Добавил iD: " + item.iD);
                } else {
                    Log.e("OCReturnOfGoods", "НЕ Добавил iD: " + item.iD);
                }
            } else { //если есть товары с ОСВ то проверяем только по ним
                Integer tovId = Integer.valueOf(item.getTovarId());
                if (tovarIds.contains(tovId)) {
                    osv = true;
                }

                if (osv &&
                        (item.expireLeft != null && item.expireLeft.equals("0")) &&
                        (item.getErrorId() != null && !item.getErrorId().equals("") && item.getErrorId().equals("0"))) {
                    errCnt++;
                    item.error = 1;
                    item.errorNote = "Для товара с ОСВ (Особливою увагою) ви зобов'язані зазначити або кількіть товару, що підлягає поверненню, або обрати 'привід' = товар поверненню НЕ підлягає.";
                    result.add(item);
                }
            }

            Log.e("OCReturnOfGoods", "-----------------------------------------");
        }


        // Подготовка Сообщения
        if (detailedReportRPList.size() == 0) {
            signal = true;
            resultMsg.append("Товарів, по котрим треба перевірити необхідність повернення, не знайдено.").append("\n\n");
        } else if (errCnt > 0) {
            signal = true;
            resultMsg.append("Не надана інформация про кількість товару що підлягає поверненню у").append(""+errCnt).append(" товарів. Див. таблицю: ").append("\n\n");
            for (ReportPrepareDB item : result) {
                TovarDB tov = TovarRealm.getById(item.getTovarId());
                String msg = String.format("(%s) %s (%s): %s", tov.getBarcode(), tov.getNm(), tov.getWeight(), item.errorNote);

                resultMsg.append(createLinkedString(msg, item, tov)).append("\n\n");
            }
        } else {
            signal = false;
            resultMsg.append("Зауважень по наданню інформації про необхідність повернення товару (в т.р. з ОСУ (Особовою Увагою)) нема.").append("\n\n");
        }

        spannableStringBuilder = resultMsg;
        spannableStringBuilder.append("\n\n");


        // Установка Сигнала
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

        checkUnlockCode(optionDB);

        // 8.0 Блокировка проведения
        if (signal) {
            if (optionDB.getBlockPns().equals("1") && wpDataDB.getStatus() == 0) {
                resultMsg.append("Документ проведен не будет!").append("\n\n");
            } else {
                resultMsg.append("Вы можете получить Премиальные БОЛЬШЕ, если будете указывать информацию о причинах отсутствия товаров.").append("\n\n");
            }
            setIsBlockOption(true);
        }
    }

    private SpannableString createLinkedString(String msg, ReportPrepareDB reportPrepareDB, TovarDB tov) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                try {
                    showDialogs(context, tov);
                }catch (Exception e){
                    Log.e("createLinkedString", "Exception e: " + e);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return res;
    }


    private void showDialogs(Context context, TovarDB tovarDB) {
        new ShowTovarRequisites(context, wpDataDB, tovarDB).showDialogs();
    }
}
