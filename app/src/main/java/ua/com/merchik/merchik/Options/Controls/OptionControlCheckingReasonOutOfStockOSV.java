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
import ua.com.merchik.merchik.Globals;
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

public class OptionControlCheckingReasonOutOfStockOSV<T> extends OptionControl {
    public int OPTION_CONTROL_CheckingReasonOutOfStockOSV_ID = 157243;

    private WpDataDB wpDataDB;
    public boolean signal = false;

    public OptionControlCheckingReasonOutOfStockOSV(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        try {
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;

            getDocumentVar();
            executeOption();
        }catch (Exception e){
            Globals.writeToMLOG("ERR", "OptionControlCheckingReasonOutOfStockOSV/", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            this.wpDataDB = (WpDataDB) document;
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
        List<ReportPrepareDB> detailedReportRPList = ReportPrepareRealm.getReportPrepareByDad2(wpDataDB.getCode_dad2());

        // Получаем Товары с особым вниманием
        List<AdditionalRequirementsDB> additionalRequirementsDBS = AdditionalRequirementsRealm.getData3(document, DEFAULT, null);
        for (AdditionalRequirementsDB item : additionalRequirementsDBS) {
            if (item.getTovarId() != null && !item.getTovarId().equals("") && !item.getTovarId().equals("0")) {
                tovarIds.add(Integer.valueOf(item.getTovarId()));
            }
        }

        Log.e("OCReasonOutOfStockOSV", "tovarIds: " + tovarIds);
        Log.e("OCReasonOutOfStockOSV", "detailedReportRPList: " + detailedReportRPList);

        // проверим, по каким из товаров с ОСВ отсутствуют на витрине?
        for (ReportPrepareDB item : detailedReportRPList) {

            boolean osv = false; // Товар с Особым Вниманием

            if (item.getFace() != null && !item.getFace().equals("") && !item.getFace().equals("0")) {
                //если товар есть то и проверять нечего
                continue;
            }

            Integer tovId = Integer.valueOf(item.getTovarId());
            if (tovarIds.contains(tovId)) {
                osv = true;
            }

            if (osv && (item.getErrorId() == null || item.getErrorId().equals("") || item.getErrorId().equals("0"))) {
                errCnt++;
                result.add(item);
            }else {
                find++;
            }
        }


        // Подготовка Сообщения
        if (detailedReportRPList.size() == 0) {
            signal = true;
            resultMsg.append("Товаров, по которым надо указать причины отсутствия, не обнаружено.").append("\n\n");
        } else if (errCnt > 0) {
            signal = true;
            resultMsg.append("Не предоставлена информация о ПИЧИНАХ отсутствия товара (в т.ч. с ОСВ (Особым Вниманием)). См. ниже.").append("\n\n");
            for (ReportPrepareDB item : result){
                TovarDB tov = TovarRealm.getById(item.getTovarId());
                String msg = String.format("(%s) %s (%s)", tov.getBarcode(), tov.getNm(), tov.getWeight());

                resultMsg.append(createLinkedString(msg, item, tov)).append("\n");
            }
//        }else if (find == 0){
//            signal = true;
//            resultMsg.append("Ни у одного ОТСУТСТВУЮЩЕГО товара не указана ПРИЧИНА отсутствия.").append("\n\n");
        }else {
            signal = false;
            resultMsg.append("Замечаний по предоставлению информации о ПРИЧИНАХ отсутствия товаров (в т.ч. с ОСВ (Особым Вниманием)) нет.").append("\n\n");
        }

        spannableStringBuilder.append(resultMsg);



        // Установка Сигнала
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

        // 8.0 Блокировка проведения
        if (signal) {
            if (optionDB.getBlockPns().equals("1") && wpDataDB.getStatus() == 0){
                spannableStringBuilder.append("Документ проведен не будет!").append("\n\n");
            }else {
                spannableStringBuilder.append("Вы можете получить Премиальные БОЛЬШЕ, если будете указывать информацию о причинах отсутствия товаров.").append("\n\n");
            }
            setIsBlockOption(true);
        }
    }

    private SpannableString createLinkedString(String msg, ReportPrepareDB reportPrepareDB, TovarDB tov) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
//                Toast.makeText(textView.getContext(), "Функция в разработке. Идентификатор Товара: " + reportPrepareDB.getTovarId(), Toast.LENGTH_LONG).show();
                showDialogs(textView.getContext(), tov);
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

    private void showDialogs(Context context, TovarDB tovarDB){
        new ShowTovarRequisites(context, wpDataDB, tovarDB).showDialogs();
    }
}
