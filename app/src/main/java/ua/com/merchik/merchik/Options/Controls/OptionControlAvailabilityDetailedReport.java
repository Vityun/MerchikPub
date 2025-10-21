package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.OFS;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.SKUFact;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.SKUPlan;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.detailedReportRPList;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.detailedReportTovList;
import static ua.com.merchik.merchik.Globals.userId;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.SMS.SMSLogSDB;
import ua.com.merchik.merchik.data.Database.Room.SMS.SMSPlanSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

public class OptionControlAvailabilityDetailedReport<T> extends OptionControl {
    public int OPTION_CONTROL_AVAILABILITY_OF_A_DR_ID = 76815;

    private long dad2;
    private String clientId;
    private int docStatus;  // Проведён ли документ
    private String comment;
    private WpDataDB wp;
    private AddressSDB addressSDB;


    public boolean signal = false;
    private int find = 0;


    public OptionControlAvailabilityDetailedReport(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
            Log.e("OCAvailabilityDReport", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(((WpDataDB) document).getCode_dad2());   // ЧТО ЭТО? ЗАЧЕМ???
            dad2 = wp.getCode_dad2();
            clientId = wp.getClient_id();
            docStatus = wp.getStatus();
            comment = wp.user_comment;
            addressSDB = SQL_DB.addressDao().getById(wp.getAddr_id());
            this.wp = wp;
        } else if (document instanceof TasksAndReclamationsSDB) {
            TasksAndReclamationsSDB tasksAndReclamationsSDB = (TasksAndReclamationsSDB) document;
            dad2 = tasksAndReclamationsSDB.codeDad2;
            clientId = tasksAndReclamationsSDB.client;
        }
    }

    @SuppressLint("NewApi")
    private void executeOption() {
        int test = 0;

        SKUPlan = 0;
        SKUFact = 0;
        OFS = 0;

        // Получение Товаров для Отчёта исполнителя
//        if (detailedReportTovList == null || detailedReportTovList.isEmpty()) {
        detailedReportTovList = RealmManager.INSTANCE.copyFromRealm(RealmManager.getTovarListFromReportPrepareByDad2(dad2));
//        }

        SKUPlan = detailedReportTovList.size();

        // Получение REPORT PREPARE для Отчёта исполнителя
//        if (detailedReportRPList == null || detailedReportRPList.isEmpty()) {
        detailedReportRPList = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(dad2));
//        }

        // Обработка опции контроля
        if (detailedReportRPList.size() > 0) {
            for (ReportPrepareDB item : detailedReportRPList) {
                if ((item.getFace() != null && !item.getFace().equals("") && !item.getFace().equals("0")) || item.getAmount() > 0) {
                    SKUFact++;
                    test++;
                }

                if (find == 0 && item.getNotes() != null && item.getNotes().length() > 1) {
                    find = item.getNotes().length();
                } else if (find == 0 && comment != null && comment.length() > 1) {
                    find = comment.length();
                }
            }

            Log.e("test", "test: " + test);
            try {
                if (SKUPlan != 0) {
                    OFS = 100 - 100 * (SKUFact / SKUPlan);
                } else {
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

        spannableStringBuilder.append(Html.fromHtml("<b>Представленность товара.</b>")).append("\n\n");

        // Формирование сообщения для пользователя
        // . . .
        spannableStringBuilder.append(Html.fromHtml("<b>СКЮ (план)=</b>")).append(String.valueOf((int) SKUPlan)).append("шт.,\n").append(Html.fromHtml("<b>СКЮ (факт)=</b>"))
                .append(String.valueOf((int) SKUFact)).append("шт.,\n").append(Html.fromHtml("<b> ОФС: </b>"))
                .append(SKUFact > SKUPlan ? "товаров больше, чем должно быть на " + String.format("%.2f", OFS) + "%" : "отсутствует " + String.format("%.2f", OFS) + "% товаров.");


        // Блокировки
        // Блокировка для Витмарка
        if (clientId.equals("9382")) {    // Витмарк
            if (OFS >= 90) {
                optionDB.setBlockPns("1");
                signal = true;
                spannableStringBuilder.append("\n\nВы можете снять сигнал, если напишите комментарии о причинах отсутствия товара.");
            } else {
                optionDB.setBlockPns("0");
            }
        }


        Long dtFrom = wp.getDt().getTime() / 1000 - 604800;   // -7 дней в секундах.. на самом деле должно біть минус 6, но оно  счтиает старт дня
        Long dtTo = wp.getDt().getTime() / 1000 + 345600;   // +4 дней в секундах.. на самом деле должно біть минус 3, но оно  счтиает старт дня

        // Формирование Сигналов для БЛОКИРОВКИ
        List<String> clientExclusionList = List.of(
                "9295", //Бетта
                "10275", //Прошанский
                "8633" //Лантманнен
//                ,"85844" //Тітрейд (для налагодження)
        );
        if (OFS == 100) {
            signal = true;

            List<SMSPlanSDB> smsPlanSDBS = SQL_DB.smsPlanDao().getAll(dtFrom, dtTo, 1172, wp.getAddr_id(), wp.getClient_id());
            List<SMSLogSDB> smsLogSDBS = SQL_DB.smsLogDao().getAll(dtFrom, dtTo, 1172, wp.getAddr_id(), wp.getClient_id());

            if (smsPlanSDBS != null && smsPlanSDBS.size() > 0) {
                signal = false;
                spannableStringBuilder.append("\n").append("СМС об ОТСУТСТВИИ товара заказчику отправлено, сигнал отменён!");
            } else if (smsLogSDBS != null && smsLogSDBS.size() > 0) {
                signal = false;
                spannableStringBuilder.append("\n").append("СМС об ОТСУТСТВИИ товара заказчику отправлено, сигнал отменён!");
            } else if (addressSDB.tpId == 383) {   // Для АШАН-ов(8196 - у петрова такое тут, странно) которые работают через ДОТ ОФС ДЗ НЕ проверяем
                if (wpDataDB.getDot_user_id() > 0) {
                    signal = false;
                    stringBuilderMsg.append(", але для Ашанів, по котрим праюємо з ДОТ, ОФС ДЗ не перевіряємо.");
                }
            } else {
                spannableStringBuilder.append("\n\nВы можете снять сигнал, если полностью и правильно заполните детализированный отчет! \n" +
                        "В случае, если на витрине (и на складе) реально нет части товара напишите об этом в комментарии (см. на кнопку \"Комментарий\")");
            }

        } else if (OFS > Integer.parseInt(optionDB.getAmountMax()) && Integer.parseInt(optionDB.getAmountMax()) > 0) {
            signal = true;
            spannableStringBuilder.append(" и это больше ").append(optionDB.getAmountMax()).append("% (максимально допустимого).");

            if (clientExclusionList.contains(clientId) && find > 1) {
                signal = false;
                spannableStringBuilder.append(" Але, сигнал знятий, так як наданий коментар про ПРИЧИНИ відсутності товару.");
            }
            else if (SQL_DB.smsPlanDao().getAll(dtFrom, dtTo, 727, wp.getAddr_id(), wp.getClient_id()).size() > 0) {
                signal = false;
                spannableStringBuilder.append(" СМС о МАЛОМ количестве товара отправлено заказчику, сигнал отменен!");
            } else if (SQL_DB.smsLogDao().getAll(dtFrom, dtTo, 727, wp.getAddr_id(), wp.getClient_id()).size() > 0) {
                signal = false;
                spannableStringBuilder.append(" СМС о МАЛОМ количестве товара отправлено заказчику, сигнал отменен!");
//            } else if (find > 0) {
//                signal = false;
//                spannableStringBuilder.append(" Примечание об отсутствии товара отписано, сигнал отменен!");
                // глТекстЧата=глТекстЧата+". СМС об отсутствии товара заказчику отправлено, сигнал отменен!";
            } else if (clientExclusionList.contains(clientId)) {
                spannableStringBuilder.append(" Вы можете снять сигнал, если полностью и правильно заполните детализированный отчет! \n" +
                        "В случае, если на витрине (и на складе) реально нет части товара напишите об этом в комментарии (см. на кнопку \"Комментарий\")");
            } else {
                spannableStringBuilder.append("  Вы можете снять сигнал, если отправите СМС заказчику о том, что товара МАЛО. \n");
                // massageToUser += " Вы можете снять сигнал, если Примечание к Товару заказчику о том, что товара мало (или он отсутствует).";
                // глТекстЧата=глТекстЧата+" Вы можете снять сигнал, если отправите СМС заказчику о том, что товара мало (или он отсутствует).";
            }
        }

        spannableStringBuilder.append("\n\nОписание:\n").append(Html.fromHtml("<b>СКЮ (План)</b>"))
                .append(" - количество товарных позиций которые должны быть в торговой точке по плану.\n")
                .append(Html.fromHtml("<b>СКЮ (Факт)</b>")).append(" - количество товарных позиций которые фактически стоят на витрине.\n")
                .append(Html.fromHtml("<b>ОФС</b>")).append(" - процент товара, который отсутствует по сравнению с планом");

        // Штатная блокировка
        if (signal) {
            if (optionDB.getBlockPns().equals("1") && docStatus == 0) {         //блокировать проведение ОИ, если есть сигнал
                spannableStringBuilder.append("\n\nДокумент проведен не будет!");
            } else if (OFS == 100 && docStatus == 0) {                          //блокировать проведение ОИ, если вообще не указаны товары
                spannableStringBuilder.append("\n\nДокумент проведен не будет!");
            } else {
                spannableStringBuilder.append("\n\nВы можете получить Премиальные БОЛЬШЕ, если ОФС не будет превышать ")
                        .append(String.valueOf(Integer.parseInt(optionDB.getAmountMax()))).append("%");
            }
        } else {
            spannableStringBuilder.append("\n\nЗамечаний нет.");
        }

        spannableStringBuilder.append("\n\n");
        spannableStringBuilder.append(createLinkedString("Отправка СМС", makeLink()));
        notCloseSpannableStringBuilderDialog = true;

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

        setIsBlockOption(signal);

        checkUnlockCode(optionDB);
    }

    private SpannableString createLinkedString(String msg, String link) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                textView.getContext().startActivity(browserIntent);
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

    private String makeLink() {
        AppUsersDB appUser = AppUserRealm.getAppUserById(userId);
        String hash = String.format("%s%s%s", appUser.getUserId(), appUser.getPassword(), "AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3");
        hash = Globals.getSha1Hex(hash);

        String addrId = String.valueOf(wp.getAddr_id());
        String date = Clock.getHumanTimeSecPattern(wp.getDt().getTime() / 1000, "yyyy-MM-dd");
        String clientId = String.valueOf(wp.getClient_id());

        return String.format("https://merchik.com.ua/sa.php?&u=%s&s=%s&l=/mobile.php?mod=message**act=to_client_addr**addr_id=%s**date=%s**client_id=%s**code_dad2=%s", userId, hash, addrId, date, clientId, wp.getCode_dad2());
    }

}
