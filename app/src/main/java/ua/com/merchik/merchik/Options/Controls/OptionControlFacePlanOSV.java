package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.detailedReportRPList;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.detailedReportTovList;
import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.HIDE_FOR_USER;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;

class OptionControlFacePlanOSV<T> extends OptionControl {

    public int OPTION_CONTROL_FACE_PLAN_ID = 157276;
    private boolean signal = true;
    private StringBuilder optionResultStr = new StringBuilder();

    private WpDataDB wpDataDB;
    private long dad2;

    public OptionControlFacePlanOSV(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;

        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            this.wpDataDB = (WpDataDB) document;

            dad2 = wpDataDB.getCode_dad2();
        }
    }

    private void executeOption() {
        List<ReportPrepareDB> resultErrorList = new ArrayList<>();
        List<ReportPrepareDB> resultOSVList = new ArrayList<>();
        List<ReportPrepareDB> resultSKUList = new ArrayList<>();

        int percentageCompletedPlan = 0;
        int minPercentage, maxPercentage;
        int planSKU = 0;

        minPercentage = (optionDB.getAmountMin() != null && Integer.parseInt(optionDB.getAmountMin()) > 0) ? Integer.parseInt(optionDB.getAmountMin()) : 50;
        maxPercentage = Integer.parseInt(optionDB.getAmountMax());


        //2.0. получим данные о товарах в отчете (если она еще не рассчитана)
        if (detailedReportTovList == null || detailedReportTovList.isEmpty()) {
            detailedReportTovList = RealmManager.getTovarListFromReportPrepareByDad2(dad2);
        }

        if (detailedReportRPList == null || detailedReportRPList.isEmpty()) {
            detailedReportRPList = ReportPrepareRealm.getReportPrepareByDad2(dad2);
        }

        //3.0. получим список товаров с особым вниманием (хранится в Доп.Требованиях)
        //3.1. выделим сперва список товаров
        List<AdditionalRequirementsDB> additionalRequirementsDBS = AdditionalRequirementsRealm.getData3(document, HIDE_FOR_USER);


        //4.0. проверим, по каким из товаров с ОСВ отсутствуют на витрине?
        //5.0. заполним ее данными ОСВ
        for (ReportPrepareDB item : detailedReportRPList) {
            boolean osv = false;    // Признак ОСВ на Товаре
            //КолСКЮ-заполняется еще на этапе формирования ТзнТов (КолСКЮ=1 если колФейс>0)
//            int countSKU = 0;
//            if (Integer.parseInt(item.getFace()) > 0) countSKU = 1;
//
//            if (countSKU == 0) continue;  //если товара на полке нет, то и проверять нечего

            if (Integer.parseInt(item.getFace()) > 0) {
                resultSKUList.add(item);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (additionalRequirementsDBS.stream().filter(obj -> obj.getTovarId().equals(item.getTovarId())).findFirst().orElse(null) != null ){
                    osv = true;
                    resultOSVList.add(item);
                }
            }

            if (osv && Integer.parseInt(item.getFace()) < item.facesPlan){
                resultErrorList.add(item);   //Тзн.Наруш=1;
                optionResultStr.append("Для товара(").append(item.getTovarId()).append(") з ОСУ (Особовою Увагою) вы не виконали ПЛАН по ФЕЙСАМ. (план=")
                        .append(item.facesPlan.toString()).append("шт. факт=").append(item.getFace()).append("шт.)").append("\n");
            }
        }

        //6.0. готовим сообщение и сигнал
        try {
            percentageCompletedPlan = 100*(resultOSVList.size()-resultErrorList.size())/resultOSVList.size();
        }catch (Exception e){
            percentageCompletedPlan = 0;
        }

        if (detailedReportRPList == null || detailedReportRPList.size() == 0){
            signal = true;
            stringBuilderMsg.append("Товарів, по котрим треба виконувати ПЛАН по ФЕЙСАМ, не знайдено.");
        }else if (resultSKUList.size() == 0){
            signal = true;
            stringBuilderMsg.append("Товарів, по котрим треба виконувати ПЛАН по ФЕЙСАМ не знайдено.");
        }else if (resultOSVList.size() == 0){
            signal = false;
            stringBuilderMsg.append("Товарів з ОСУ, по котрим треба виконувати ПЛАН по ФЕЙСАМ не знайдено.");
        }else if (minPercentage > 0 && percentageCompletedPlan < minPercentage){
            signal = true;
            stringBuilderMsg.append("План по фейсам виконан на ").append(percentageCompletedPlan).append("%, що нижче мінімального ")
                    .append(minPercentage).append("%.");
        }else if (maxPercentage > 0 && percentageCompletedPlan > maxPercentage){
            signal = false;
            stringBuilderMsg.append("План по фейсам виконан на ").append(percentageCompletedPlan).append("%, що вишче планового ")
                    .append(maxPercentage).append("% це дуже добре. За це можна отримати премію. Зверніться до керівника.");
        }else {
            signal = false;
            stringBuilderMsg.append("План по фейсам виконан на ").append(percentageCompletedPlan).append("% зауважень немає.")
                    .append((maxPercentage > 0) ? "Але, якщо ви виконаєте його на " + maxPercentage + "% за це можна буде отримати додаткову премію. Зверніться до керівника.": "");
        }

        // Типо показываю с какими именно Товарами была проблема
        stringBuilderMsg.append(optionResultStr);


        //7.0. сохраним сигнал
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

        //8.0. блокировка проведения
        // Установка блокирует ли опция работу приложения или нет
        if (signal) {
            if (optionDB.getBlockPns().equals("1")) {
                setIsBlockOption(signal);
                stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
            } else {
                stringBuilderMsg.append("\n\n").append("Вы можете отримати Преміальні БІЛЬШЕ, якщо будете збільшувати кількість фейсів товарів замовника на полиці.");
            }
        }
    }


}
