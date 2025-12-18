package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.HIDE_FOR_USER;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.ShowTovarRequisites;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
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

// 07.03.2024 Петров Переписана опція, що була написана декілько років тому ... приведена к "стандартному" вигляду. Об"єднав опції 84005 та 84967 164985 додав опрацювання ОСВ на перспективу

// Виконується перевірка НАЯВНОСТІ ДОСГ у товарів ЗВ. В залежності від опції перевіряються різні варіанти ...
// 84005 - контроль наявності дати закінчення терміну придатності (сроков годности) хоча б у ОДНОГО товара наявного на полиці.
// 84967 - контроль наявності дати закінчення терміну придатності (сроков годности) в УСІХ товарів, наявних на полицях.
// 164985 - контроль наявності дати закінчення терміну придатності (сроков годности) у товарів у котрих (в доп.вимогах) встановлений признак "особое внимание" - поточна опция. Если в ДТ нет товаров с "особым вниманием" то проверяем наличие указания этой информации у ОДНОГО ЛЮБОГО товара
// 165276 -

// ДокИст - документ - источник типа ОтчетИсполнителя, Задача, ОтчетИсполнителя, ОтчетОСтажировке и пр. к которому подчинена данная опция
// ДокОпц - документ - набор опций (на момент передачи в эту функцию позиционирован на конкретную строку с ДАННОЙ опцией)
// ТзнТов - таблица со списком данных отчета о товарах (используется для ускорения ГРУППОВЫХ расчетов опций)
// ВыбЗап =1 сохранить результаты в документе (используется при вызове роботом), 0(пусто)-НЕ выполнять запись документа (вызов выполняется интерактивно из самого документа)
// ВыбПред - флаг возвращаемых результатов
//			=1 - выводить предупреждение,
//			=2 - выводить сообщение,
//			=3 - выводить Тзн (в самих функциях в этом режиме ничего делать НЕ надо письмо пишется в функции РазрешитьДействия(), которая их вызывает)
//			=4 - выполнить запись в ЧАТ + Сообщение
//			=0(пусто) - выполняет все действия, но возвращает 0 без предупреждений и сообщений
public class OptionControlCheckingForAnAchievement<T> extends OptionControl {

    public int OPTION_CONTROL_CheckingForAnAchievement1_ID = 84005;
    public int OPTION_CONTROL_CheckingForAnAchievement2_ID = 84967;
    public int OPTION_CONTROL_CheckingForAnAchievement3_ID = 164985;
    public int OPTION_CONTROL_CheckingForAnAchievement4_ID = 165276;

    public boolean signal = true;
    private SpannableStringBuilder resultMsg = new SpannableStringBuilder();

    private List<Integer> tovListOSV;
    private WpDataDB wpDataDB;
    private CustomerSDB customerSDBDocument;
    private UsersSDB usersSDBDocument;
    private AddressSDB addressSDBDocument;
    private List<ReportPrepareDB> reportPrepareDB = new ArrayList<>();
    private List<TovarDB> tovarResultList = new ArrayList<>();

    private int colMax = 0;

    public OptionControlCheckingForAnAchievement(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        try {
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
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionControlCheckingForAnAchievement", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        try {
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;

                customerSDBDocument = SQL_DB.customerDao().getById(wpDataDB.getClient_id());
                usersSDBDocument = SQL_DB.usersDao().getById(wpDataDB.getUser_id());
                addressSDBDocument = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
                reportPrepareDB = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(wpDataDB.getCode_dad2()));


                if (optionDB.getAmountMax() != null && !optionDB.getAmountMax().equals("")){
                    colMax = Integer.parseInt(optionDB.getAmountMax());
                }
            }


        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlCheckingForAnAchievement/getDocumentVar", "Exception e: " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        try {
            //4.0. получим список товаров с особым вниманием (хранится в Доп.Требованиях)
            if (optionDB.getOptionId().equals("164985") || optionDB.getOptionControlId().equals("164985")) {
                Integer ttCategory = null;

                AddressSDB addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
                if (addressSDB != null) {
                    ttCategory = addressSDB.ttId;
                }
                // 18.12.2025 поменял запрос в бд
                List<AdditionalRequirementsDB> ad = AdditionalRequirementsRealm.getData3(document, HIDE_FOR_USER, ttCategory, null, 1);
//                List<AdditionalRequirementsDB> ad = AdditionalRequirementsRealm.getAdditionalRequirements(wpDataDB.getClient_id(), wpDataDB.getAddr_id(), 164985);

                for (AdditionalRequirementsDB item : ad) {
                    if (item.getTovarId() != null && !item.getTovarId().equals("")) {
                        tovListOSV.add(Integer.valueOf(item.getTovarId()));
                    }
                }
            }

            //5.0. заполним итоговую Тзн данными ОСВ
            int find = 0;
            for (ReportPrepareDB item : reportPrepareDB) {

                TovarDB tovarDB = TovarRealm.getById(item.tovarId);

                item.error = 0;

                int fase;
                if (item.face != null && !item.face.equals("")) {
                    fase = Integer.parseInt(item.face);
                } else {
                    fase = 0;
                }

//                item.colTov = fase > 0 ? 1 : 0;
                if (fase > 0) {
                    item.colTov = 1;
                } else {
                    item.colTov = 0;
                }
//                item.colDOSG = item.dtExpire != null ? 1 : 0;
                if (item.dtExpire != null && !item.dtExpire.equals("") && !item.dtExpire.equals("0000-00-00")) {
                    item.colDOSG = 1;
                } else {
                    item.colDOSG = 0;
                }

                if (item.tovarId != null && !item.tovarId.equals("") && !item.tovarId.equals("0")) {
                    item.OSV = 1;
                } else {
                    item.OSV = 0;
                }

                // 164985 - контроль наявності дати закінчення терміну придатності (сроков годности) у товарів у котрих (в доп.вимогах) встановлений признак "особое внимание"
                if (optionDB.getOptionId().equals("164985") && item.colTov > 0 && item.OSV == 1 && item.colDOSG == 0) {
                    item.error = 1;
                    item.errorNote = "Для товара з ОСУ (Особою Увагою) ви повинні зазначити ДЗТП (ДОСГ).";
                    find = 1;
                    tovarResultList.add(tovarDB);
                }

                // 84967 - контроль наявності дати закінчення терміну придатності (сроков годности) в УСІХ товарів, наявних на полицях
                if (optionDB.getOptionId().equals("84967") && item.colTov > 0 && item.colDOSG == 0) {
                    item.error = 1;
                    item.errorNote = "Для поточного товара ви повинні зазначити ДЗТП (ДОСГ).";
                    find = 1;
                    tovarResultList.add(tovarDB);
                }

                // 165276 -
                if (optionDB.getOptionId().equals("165276") && item.colTov > 0 && item.colDOSG == 0 && tovarDB.expirePeriod > 0 && colMax > 0 && tovarDB.expirePeriod <= colMax) {
                    item.error = 1;
                    item.errorNote = "Для поточного товара ви повинні зазначити ДЗТП (ДОСГ).";
                    find = 1;
                    tovarResultList.add(tovarDB);
                }
            }

            // Тзн.Итог("ОСВ")
            int sumOSV = 0;
            try {
                sumOSV = reportPrepareDB.stream()
                        .mapToInt(report -> report.OSV)
                        .sum();
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "OptionControlCheckingForAnAchievement/executeOption/Тзн.Итог(ОСВ)", "Exception e: " + e);
            }

            // Тзн.Итог("КолДОСГ")
            int sumDOSG = 0;
            try {
                sumDOSG = reportPrepareDB.stream()
                        .mapToInt(report -> report.colDOSG)
                        .sum();
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "OptionControlCheckingForAnAchievement/executeOption/Тзн.Итог(КолДОСГ)", "Exception e: " + e);
            }

            // Тзн.Итог("Наруш")
            int sumERR = 0;
            try {
                sumERR = reportPrepareDB.stream()
                        .mapToInt(report -> report.error)
                        .sum();
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "OptionControlCheckingForAnAchievement/executeOption/Тзн.Итог(Наруш)", "Exception e: " + e);
            }

            // Тзн.Итог("colTov")
            int sumcolTov = 0;
            try {
                sumcolTov = reportPrepareDB.stream()
                        .mapToInt(report -> report.colTov)
                        .sum();
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "OptionControlCheckingForAnAchievement/executeOption/Тзн.Итог(Наруш)", "Exception e: " + e);
            }

            //6.0. готовим сообщение и сигнал
            if (reportPrepareDB.size() == 0) {
                resultMsg.append("Товарів, по котрим треба перевірити факт наякності ДОСГ (Дати Закінчення Терміну Придатності), не знайдено.");
                signal = true;
            } else if (optionDB.getOptionId().equals("164985") && sumOSV == 0 && sumDOSG == 0) {
                resultMsg.append("На поточний момент, товари з ОСВ (Особым Вниманием) не знайдено, це значить, що достатньо зазначити ДОСГ у любого отного товару, але ви цього теж не зробили.");
                signal = true;
            } else if (optionDB.getOptionId().equals("164985") && sumOSV > 0 && sumERR > 0) {
                resultMsg.append("Знайдено ").append("" + sumERR).append(" товарів, присутніх на вітрині, з ОСВ (Особым Вниманием) по котрим не зазначена ДОСГ (Дата Закінчення Терміну Придатності).");
                signal = true;
            } else if (optionDB.getOptionId().equals("84967") && sumERR > 0) {      // 84967 - контроль наявності дати закінчення терміну придатності (сроков годности) в УСІХ товарів, наявних на полицях
                resultMsg.append("Знайдено ").append("" + sumERR).append(" товарів, присутніх на вітрині, але з не зазначеною ДОСГ (Датою Закінчення Терміну Придатності).");
                signal = true;
            } else if (optionDB.getOptionId().equals("84967") && sumDOSG < sumcolTov) {      // 84967 - контроль наявності дати закінчення терміну придатності (сроков годности) в УСІХ товарів, наявних на полицях
                int tovWithoutDOSG = sumcolTov - sumDOSG;
                resultMsg.append("Ви не зазначили ДОСГ (Датою Закінчення Терміну Придатності) у ")
                        .append("" + tovWithoutDOSG)
                        .append(" товарів.");
                signal = true;
            } else if (optionDB.getOptionId().equals("84005") && sumcolTov > 0 && sumDOSG == 0) {     // 84005 - контроль наявності дати закінчення терміну придатності (сроков годности) хоча б у ОДНОГО товара наявного на полиці
                resultMsg.append("У жодного з  ").append("" + sumcolTov).append(" товарів, присутніх на вітрині, не зазначена ДОСГ (Дата Закінчення Терміну Придатності).");
                signal = true;

            } else if (optionDB.getOptionId().equals("165276") && sumcolTov > 0 && sumERR > 0) {
                resultMsg.append("У ")
                        .append("" + sumERR)
                        .append(" товарів, присутніх на вітрині, і терміном придатності менше ")
                        .append("" + colMax)
                        .append(" діб, не зазначена ДОСГ (Дата Закінчення Терміну Придатності).");
                signal = true;

            } else if (sumcolTov == 0) {     // Не заповнений звіт
                resultMsg.append("У жодного з ").append("" + reportPrepareDB.size()).append(" товарів, не зазначена наявність на вітрині.");
                signal = true;
            } else {
                resultMsg.append("Зауважень по наданню інформації про ДОСГ (Дати Закінчення Терміну Придатності) нема.");
                signal = false;
            }

            resultMsg.append("\n\n");
            for (TovarDB item : tovarResultList) {
                String msg = String.format("(%s) %s (%s)", item.getBarcode(), item.getNm(), item.getWeight());
                resultMsg.append(createLinkedString(msg, item)).append("\n\n");
            }

            // Сохранение
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

            if (signal) {
                if (optionDB.getBlockPns().equals("1")) {
                    setIsBlockOption(signal);
//                    showUnlockCodeDialogInMainThread(wpDataDB, signal);
                    resultMsg.append("\n\n").append("Документ проведен не будет!");
                } else {
                    resultMsg.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете делать Достижения.");
                }
            }

            spannableStringBuilder.append(resultMsg);

            checkUnlockCode(optionDB);

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlCheckingForAnAchievement/executeOption", "Exception e: " + e);
        }
    }

    private SpannableString createLinkedString(String msg, TovarDB tov) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                try {
                    showDialogs(textView.getContext(), tov);
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

