package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;
import android.util.Log;

import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.OFS;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.SKUFact;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.SKUPlan;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.detailedReportRPList;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.detailedReportTovList;

public class OptionControlAvailabilityDetailedReport<T> extends OptionControl {
    public int OPTION_CONTROL_AVAILABILITY_OF_A_DR_ID = 76815;

    private long dad2;
    private String clientId;
    private int docStatus;  // Проведён ли документ
    private String comment;

    private boolean signal = false;
    private int find = 0;


    public OptionControlAvailabilityDetailedReport(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
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
            WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(((WpDataDB) document).getCode_dad2());
            dad2 = wp.getCode_dad2();
            clientId = wp.getClient_id();
            docStatus = wp.getStatus();
            comment = wp.user_comment;
        } else if (document instanceof TasksAndReclamationsSDB) {
            TasksAndReclamationsSDB tasksAndReclamationsSDB = (TasksAndReclamationsSDB) document;
            dad2 = tasksAndReclamationsSDB.codeDad2;
            clientId = tasksAndReclamationsSDB.client;
        }
    }

    private void executeOption() {
        int test = 0;

        SKUPlan = 0;
        SKUFact = 0;
        OFS = 0;

        // Получение Товаров для Отчёта исполнителя
//        if (detailedReportTovList == null || detailedReportTovList.isEmpty()) {
            detailedReportTovList = RealmManager.getTovarListFromReportPrepareByDad2(dad2);
//        }

        SKUPlan = detailedReportTovList.size();

        // Получение REPORT PREPARE для Отчёта исполнителя
//        if (detailedReportRPList == null || detailedReportRPList.isEmpty()) {
            detailedReportRPList = ReportPrepareRealm.getReportPrepareByDad2(dad2);
//        }

        // Обработка опции контроля
        if (detailedReportRPList.size() > 0) {
            for (ReportPrepareDB item : detailedReportRPList) {
                if (item.getFace() != null && !item.getFace().equals("") && !item.getFace().equals("0") || item.getAmount() > 0) {
                    SKUFact++;
                    test++;
                }

                if (find == 0 && item.getNotes().length() > 1) {
                    find = item.getNotes().length();
                }else if (find == 0 && comment != null && comment.length() > 1){
                    find = comment.length();
                }
            }

            Log.e("test", "test: " + test);
            try {
                if (SKUPlan != 0){
                    OFS = 100 - 100 * (SKUFact / SKUPlan);
                }else {
                    OFS = 0;
                }
            } catch (Exception e) {
                OFS = 0;
            }

            // TODO СПРОСИТЬ У ПЕТРОВА ПРО ООС и ОФС
            // OOS = 100 - 100 * (SKUFact / SKUPlan);
            // OFS = 100 * (SKUFact / SKUPlan);
        } else {
            OFS = 100;
            SKUFact = 0;
        }

        stringBuilderMsg.append("Представленность товара.\n\n");

        // Формирование сообщения для пользователя
        // . . .
        stringBuilderMsg.append("СКЮ (план)=").append((int)SKUPlan).append("шт., СКЮ (факт)=")
                .append((int)SKUFact).append("шт., ОФС: ")
                .append(SKUFact > SKUPlan ? "товаров больше, чем должно быть на " + String.format("%.2f", OFS) + "%" : "отсутствует " + String.format("%.2f", OFS) + "% товаров.");

        // Формирование Сигналов для БЛОКИРОВКИ
        if (OFS == 100) {
            signal = true;
            stringBuilderMsg.append("\n\nВы можете снять сигнал, если полностью и правильно заполните детализированный отчет! \n" +
                    "В случае, если на витрине (и на складе) реально нет части товара напишите об этом в комментарии (см. на кнопку \"Комментарий\")");
        } else if (OFS > Integer.parseInt(optionDB.getAmountMax()) && Integer.parseInt(optionDB.getAmountMax()) > 0) {
            signal = true;
            stringBuilderMsg.append(" и это больше ").append(optionDB.getAmountMax()).append("% (максимально допустимого).");

            if (clientId.equals("9295") && find > 1) {   // Костыль для клиента Бетта
                signal = false;
                stringBuilderMsg.append(" Комментарий об отсутствии товара написан, сигнал отменён!");
            } else if (find > 0) {
                signal = false;
                stringBuilderMsg.append(" Примечание об отсутствии товара отписано, сигнал отменен!");
                // глТекстЧата=глТекстЧата+". СМС об отсутствии товара заказчику отправлено, сигнал отменен!";
            } else if (clientId.equals("9295")) {
                stringBuilderMsg.append(" Вы можете снять сигнал, если полностью и правильно заполните детализированный отчет! \n" +
                        "В случае, если на витрине (и на складе) реально нет части товара напишите об этом в комментарии (см. на кнопку \"Комментарий\")");
            } else {
                stringBuilderMsg.append(" Вы можете снять сигнал, если полностью и правильно заполните детализированный отчет! \n" +
                        "В случае, если на витрине (и на складе) реально нет части товара напишите об этом в комментарии (см. на кнопку \"Комментарий\")");
                // massageToUser += " Вы можете снять сигнал, если Примечание к Товару заказчику о том, что товара мало (или он отсутствует).";
                // глТекстЧата=глТекстЧата+" Вы можете снять сигнал, если отправите СМС заказчику о том, что товара мало (или он отсутствует).";
            }
        }

        // Блокировки
        // Блокировка для Витмарка
        if (clientId.equals("9382")) {    // Витмарк
            if (OFS >= 90) {
                optionDB.setBlockPns("1");
                signal = true;
                stringBuilderMsg.append("\n\nВы можете снять сигнал, если напишите комментарии о причинах отсутствия товара.");
            } else {
                optionDB.setBlockPns("0");
            }
        }

        stringBuilderMsg.append("\n\nОписание:\nСКЮ (План) - количество товарных позиций которые должны быть в торговой точке по плану.\nСКЮ (Факт) - количество товарных позиций которые фактически стоят на витрине.\nОФС - процент товара, который отсутствует по сравнению с планом");

        // Штатная блокировка
        if (signal) {
            if (optionDB.getBlockPns().equals("1") && docStatus == 0) {         //блокировать проведение ОИ, если есть сигнал
                stringBuilderMsg.append("\n\nДокумент проведен не будет!");
            } else if (OFS == 100 && docStatus == 0) {                          //блокировать проведение ОИ, если вообще не указаны товары
                stringBuilderMsg.append("\n\nДокумент проведен не будет!");
            } else {
                stringBuilderMsg.append("\n\nВы можете получить Премиальные БОЛЬШЕ, если ОФС не будет превышать ")
                        .append(Integer.parseInt(optionDB.getAmountMax())).append("%");
            }
        }else {
            stringBuilderMsg.append("\n\nЗамечаний нет.");
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

        setIsBlockOption(signal);
    }

}
