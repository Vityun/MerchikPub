package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.Global.UnlockCode.UnlockCodeMode.CODE_DAD_2_AND_OPTION;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.Log;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Global.UnlockCode;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.DossierSotrSDB;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupClientSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class OptionControlEKL<T> extends OptionControl {
    public int OPTION_CONTROL_EKL_ID = 84006;

    private int resultCode = 0; // Переменная для хранения результата


    private WpDataDB wpDataDB;
    private AddressSDB addressSDB;
    private UsersSDB usersSDBPTT, documentUser;
    private CustomerSDB customerSDB;

    private TovarGroupSDB TG = new TovarGroupSDB();

    private List<TovarGroupClientSDB> tovarGroupClientSDB;
    private List<TovarGroupSDB> tovarGroupSDB;
    private List<EKL_SDB> eklSDB;

    private long documentDt;    // в секундах

    private String PTT;
    private String controllerType = "ПТТ";   //по умолчанию ЭКЛ получаем у ПТТ, но для 132629 у ПРОВЕРЯЕМОГО

    public boolean signal = false;

    private final int DAYS = 4;   // ветка дней на склолько раньше проверяем ЭКЛ !!!ПРИ ИЗМЕНЕНИИ РЕАДКТИРОВАТЬ АНАЛОГИЧНОЕ В RecycleViewDRAdapter/counter2EKLText


    public int getResultCode() {
        return resultCode;
    }

    public OptionControlEKL(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        try {
            Log.e("OptionControlEKL", "HERE TEST OptionControlEKL START");
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            this.unlockCodeResultListener = unlockCodeResultListener;

            getDocumentVar();
            executeOption();
            Log.e("OptionControlEKL", "HERE TEST OptionControlEKL END");
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlEKL", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            wpDataDB = (WpDataDB) document;

            documentDt = wpDataDB.getDt().getTime() / 1000;

            addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
            customerSDB = SQL_DB.customerDao().getById(wpDataDB.getClient_id());
            usersSDBPTT = SQL_DB.usersDao().getById(wpDataDB.ptt_user_id);
            documentUser = SQL_DB.usersDao().getUserById(wpDataDB.getUser_id());
        }
    }

    private void executeOption() {
        try {
            createTZN();
        } catch (Exception e) {
            Log.e("OptionControlEKL", "HERE TEST OptionControlEKL executeOption Exception: " + e);
            Globals.writeToMLOG("ERROR", "OptionControlEKL/executeOption/Exception", "Exception: " + e);
            Globals.writeToMLOG("ERROR", "OptionControlEKL/executeOption/Exception", "Exception: " + Arrays.toString(e.getStackTrace()));
        }
    }


    /*Тут в теории должен собираться ТЗН "с одной строки" для подальшей работы с ним. Пока сути для
    меня не вижу кроме как указать что ПТТшник может быть пустым настарте и мы его ПОТОМ переопределим*/
    private void createTZN() {
        Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 1");
        PTT = "";   // Сбрасываем ПТТшника в режим "любой"
        StringBuilder optionMsg = new StringBuilder(); //

        // DEBUG DATA-------------
//        try {
//            List<EKL_SDB> fullEkl = SQL_DB.eklDao().getAll();
//            StringBuilder stringBuilderDEBUG = new StringBuilder();
//            for (EKL_SDB item : fullEkl) {
//                JsonObject object = new Gson().fromJson(new Gson().toJson(item), JsonObject.class);
//                stringBuilderDEBUG.append(object);
//            }
//            Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "fullEkl.size: " + fullEkl.size());
//            Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "stringBuilderDEBUG: " + stringBuilderDEBUG);
//        } catch (Exception e) {
//            Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "stringBuilderDEBUG/Exception e: " + e);
//        }
        // -----------------------

        String ptt = PTT;
        long dateFrom = Clock.getDatePeriodLong(documentDt * 1000, -3) / 1000;
        long dateTo = Clock.getDatePeriodLong(documentDt * 1000, 5) / 1000;

        if (System.currentTimeMillis() / 1000 < 1719878399 && addressSDB.cityId == 41) {
            dateFrom = Clock.getDatePeriodLong(documentDt * 1000, -11) / 1000;
            dateTo = Clock.getDatePeriodLong(documentDt * 1000, 5) / 1000;
        }

        Calendar limitCal = Calendar.getInstance();
        limitCal.set(2026, Calendar.MARCH, 1, 0, 0, 0);
        limitCal.set(Calendar.MILLISECOND, 0);

        Date limitDate = limitCal.getTime();

        // 02.09.2025 поменял на 01.03.2026
        if ((addressSDB.tpId == 434 || addressSDB.tpId == 6767)
                && !optionDB.getOptionControlId().equals("132629")
                && wpDataDB.getDt().before(limitDate)) { //  434 = АТБ или 676 = Акварель
            optionMsg.append("Не проверяем для АТБ или Акварель до 01.03.2026");
            signal = false;
        } else {
            // Определем Группу Товаров
            if (optionDB.getOptionControlId().equals("132629")) {
                //для 132629-Контроль ЭКЛ между ПРОВЕРЯЮЩИМ и ПРОВЕРЯЕМЫМ (электронный контрольный лист) НЕ имеет значения в каком он отделе
            } else if (optionDB.getOptionControlId().equals("143968")) {
                //для 143968-Контроль ЭКЛ между исполнителем и сотрудником КЛИЕНТА (электронный контрольный лист) НЕ имеет значения в каком он отделе
            } else if (optionDB.getOptionControlId().equals("151140") && (ptt.equals("") || ptt.equals("0"))) {
                //для 151140-Контроль ЭКЛ между исполнителем и КОНКРЕТНЫМ ПТТ (ИНДИВИДУАЛЬНЫЙ электронный контрольный лист) НЕ имеет значения в каком отделе ПТТ. Отдел НЕ важен, если ПТТ для подписания ИЭКЛ определен, а если НЕТ то все-таки нужно определить отдел
                //ГрупТов.ДобавитьЗначение(ПТТ.Отдел); //если мы уже определились с ПТТ в этом режиме то и отдел получим из ПТТ ... клиент сам решил использовать ЭТОГО ПТТ независимо от отдела
            } else if (optionDB.getOptionControlId().equals("84006") || (nnkMode.equals(Options.NNKMode.BLOCK) && optionDB.getOptionId().equals("84006"))) {
//                tovarGroupClientSDB = SQL_DB.tovarGroupClientDao().getAllBy(wpDataDB.getClient_id(), addressSDB.tpId);  // Получаю ГруппыТоваров по Адресу и Сети!
//
//                if (tovarGroupClientSDB == null || tovarGroupClientSDB.size() == 0){
//                    tovarGroupClientSDB = SQL_DB.tovarGroupClientDao().getAllBy(wpDataDB.getClient_id(), 0);
//                }

                // 03.02.2025 изменил получение групп товаров, потому что были проблемы когда по некоторой сети получал только 1 товар
                List<TovarGroupClientSDB> list1 = SQL_DB.tovarGroupClientDao().getAllBy(wpDataDB.getClient_id(), addressSDB.tpId);
                List<TovarGroupClientSDB> list2 = SQL_DB.tovarGroupClientDao().getAllBy(wpDataDB.getClient_id(), 0);

                Set<TovarGroupClientSDB> resultSet = new LinkedHashSet<>(list1);
                resultSet.addAll(list2);

                tovarGroupClientSDB = new ArrayList<>(resultSet);


//                tovarGroupClientSDB.addAll(list1);
//                tovarGroupClientSDB.addAll(list2);


                if (tovarGroupClientSDB != null && tovarGroupClientSDB.size() > 0) {
                    List<Integer> ids = new ArrayList<>();
                    for (TovarGroupClientSDB item : tovarGroupClientSDB) {
                        ids.add(item.tovarGrpId);
                    }
                    tovarGroupSDB = SQL_DB.tovarGroupDao().getAllByIds(ids);
                } else { //добавим группы товаров КЛИЕНТА (пока без учета подчиненности) Дело в том, что ЭКЛ регистрируется по группе товара полученной из карточки клиента! ... был случай с Блуми, когда один их элементов помечен на удаление и не попадает в выборку ... короче костыль. 02.08.2019
                    tovarGroupSDB = SQL_DB.tovarGroupDao().getAllByIds(Collections.singletonList(customerSDB.mainTovGrp));
                }
            }

            // Готовим часть Сообщения.
            if (optionDB.getOptionControlId().equals("143968")) {
                controllerType = "между сотрудником: (" + wpDataDB.getUser_txt() + ") и любым сотрудником КЛИЕНТА";
            } else if (optionDB.getOptionControlId().equals("151140")) {
                controllerType = "между сотрудником: (" + wpDataDB.getUser_txt() + ") и ПТТ " + ptt + " (Индивидуальный ЭКЛ)";
            } else {
                controllerType = "между сотрудником: (" + wpDataDB.getUser_txt() + ") и любым ПТТ по отделу(ам): " + TG.getNmFromList(tovarGroupSDB);
            }

            Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 4");


            // лезем в таблицу ЭКЛ и проверяем, еслть ли ПОДПИСАННЫЙ ЭКЛ по данным условиям
            eklSDB = SQL_DB.eklDao().getBy(dateFrom, dateTo, wpDataDB.getClient_id(), wpDataDB.getAddr_id(), wpDataDB.getUser_id());
//            eklSDB = SQL_DB.eklDao().getBy(dateFrom, dateTo, wpDataDB.getClient_id(), wpDataDB.getAddr_id(), wpDataDB.getUser_id());
//        eklSDB = SQL_DB.eklDao().getBy(dateFrom, dateTo, wpDataDB.getClient_id(), wpDataDB.getAddr_id(), wpDataDB.getUser_id(), wpDataDB.ptt_user_id);

            Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "eklSDB(-1): " + new Gson().toJson(eklSDB));

            Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 5");
            if (eklSDB == null || eklSDB.size() == 0) {
                List<Integer> ids = new ArrayList<>();
                signal = true;
                if (tovarGroupSDB != null) {
                    for (TovarGroupSDB item : tovarGroupSDB) {
                        ids.add(item.id);
                    }
                    Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "TovarGroupSDB ids: " + ids);
                    String msgDebug = String.format("dateFrom: %s/dateTo: %s/ids: %s/addr: %s/user: %s/ptt: %s", dateFrom, dateTo, ids, wpDataDB.getAddr_id(), wpDataDB.getUser_id(), wpDataDB.ptt_user_id);
                    Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", msgDebug);

                    eklSDB = SQL_DB.eklDao().getBy(dateFrom, dateTo, ids, wpDataDB.getAddr_id(), wpDataDB.getUser_id());

                    Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "eklSDB(0): " + new Gson().toJson(eklSDB));
                } else {
                    // TODO отсебятина, у меня у Эрики не было групп товаров изза чего проблема выникла, это может быть очень опасно
                    eklSDB = SQL_DB.eklDao().getBy(dateFrom, dateTo, wpDataDB.getAddr_id(), wpDataDB.getUser_id());
                    Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "eklSDB(1): " + new Gson().toJson(eklSDB));
                }

                if (eklSDB.size() == 0 && addressSDB != null && addressSDB.kolKass != null && addressSDB.kolKass <= 5) {
                    eklSDB = SQL_DB.eklDao().getBy(dateFrom, dateTo, wpDataDB.getAddr_id(), wpDataDB.getUser_id());
                    Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "eklSDB(2): " + new Gson().toJson(eklSDB));
                }

                /*test*/
//                Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 0.6");
//                eklSDB = SQL_DB.eklDao().getByTEst(wpDataDB.getAddr_id(), wpDataDB.getUser_id());
//                Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 0.7");
//                eklSDB = SQL_DB.eklDao().getByTEst(dateFrom, dateTo, wpDataDB.getAddr_id(), wpDataDB.getUser_id());
//                Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 0.8");
                /*end test*/

                if (eklSDB != null) {
                    Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "eklSDB1: " + eklSDB.size());
                } else {
                    Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "eklSDB1: EMPTY");
                }
            } else {
                Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "eklSDB2: " + eklSDB.size());
            }

            Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 6");
            Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "eklSDB(3): " + new Gson().toJson(eklSDB));

            // Проверка ЭКЛов
            if (eklSDB == null || eklSDB.size() == 0) {
                if (addressSDB.tpId == 383) {   // АШАН
                    if (wpDataDB.getDot_user_id() != 0 || wpDataDB.getFot_user_id() != 0) {
                        signal = false;
                        optionMsg.append("но для Ашанов по которым работаем с ДОТ или ФОТ ЭКЛ не проверяем.");
                    }
                } else {
                    signal = true;
                    optionMsg.append("За период с ")
                            .append(Clock.getHumanTime3(dateFrom))
                            .append(" по ")
                            .append(Clock.getHumanTime3(dateTo))
                            .append(" НЕ получено ни одного ЭКЛ ")
                            .append(controllerType);
                }

            /*  //добавим исключение
				Если (ДокИст.Вид()="ОтчетИсполнителя") и (КодВлад=8196) Тогда //для Ашанов, котоые работают через ДОТ и ФОТ НЕ проверяем ЭКЛ
					Если (ПустоеЗначение(ДокИст.ОператорДОТ)=0) или (ПустоеЗначение(ДокИст.ОператорФОТ)=0) Тогда
						Причина=Причина+", но для Ашанов по которым работаем с ДОТ или ФОТ ЭКЛ не проверяем.";
						НеПроверятьПр=1;
						Тзн.Зачет=1;
					КонецЕсли;
				КонецЕсли;*/

            } else {
//                if (usersSDBPTT == null) {
//                    usersSDBPTT = SQL_DB.usersDao().getById(eklSDB.get(0).sotrId);
//                }
//                optionMsg.append("За период с ")
//                        .append(Clock.getHumanTime3(dateFrom)).append(" по ")
//                        .append(Clock.getHumanTime3(dateTo))
//                        .append(" получено ").append(eklSDB.size()).append(" ЭКЛ у ").append(usersSDBPTT.fio)
//                        .append(" (").append(usersSDBPTT.department).append(") тел: ").append(usersSDBPTT.tel)
//                        .append(", ").append(usersSDBPTT.tel2);
                // eklSDB: List<...> где есть поле sotrId
// usersSDBPTT: UsersSDB (или как у тебя), может быть null

// 1) сгруппировать ЭКЛ по сотруднику: sotrId -> count
                Map<Integer, Integer> cntBySotr = new LinkedHashMap<>();
                for (int i = 0; i < eklSDB.size(); i++) {
                    int id = eklSDB.get(i).sotrId;
                    Integer prev = cntBySotr.get(id);
                    cntBySotr.put(id, prev == null ? 1 : (prev + 1));
                }

// 2) если сотрудников 1 — оставляем как было
                if (cntBySotr.size() == 1) {
                    int onlyId = cntBySotr.keySet().iterator().next();

                    if (usersSDBPTT == null || usersSDBPTT.id != onlyId) { // подставь правильное поле id
                        usersSDBPTT = SQL_DB.usersDao().getById(onlyId);
                    }

                    optionMsg.append("За период с ")
                            .append(Clock.getHumanTime3(dateFrom)).append(" по ")
                            .append(Clock.getHumanTime3(dateTo))
                            .append(" получено ").append(eklSDB.size()).append(" ЭКЛ у ").append(usersSDBPTT.fio)
                            .append(" (").append(usersSDBPTT.department).append(") тел: ").append(usersSDBPTT.tel)
                            .append(", ").append(usersSDBPTT.tel2);

                } else {
                    // 3) если сотрудников несколько — "ФИО (N), ФИО (N), ..."
                    optionMsg.append("За период с ")
                            .append(Clock.getHumanTime3(dateFrom)).append(" по ")
                            .append(Clock.getHumanTime3(dateTo))
                            .append(" получено ").append(eklSDB.size()).append(" ЭКЛ:\n");

                    boolean first = true;

                    for (Map.Entry<Integer, Integer> e : cntBySotr.entrySet()) {
                        int sotrId = e.getKey();
                        int count = e.getValue();

                        UsersSDB u = SQL_DB.usersDao().getById(sotrId);
                        if (u == null) continue;

                        if (!first) optionMsg.append(",\n");
                        first = false;

                        optionMsg.append(u.fio)
                                .append(" (").append(count).append(")");
                    }
                }


                signal = false;
                TovarGroupSDB tovarGroupSDB1;
                if (usersSDBPTT == null) {
                    tovarGroupSDB1 = null;
                } else
                    tovarGroupSDB1 = SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId);

                //Если (ПТТ.Уволен=1) и (Опц=глОпция132629) и (ПустоеЗначение(ПТТ.ДатаУвол)=0) и (ПТТ.ДатаУвол<Дат) и (Тем<>Тема421) Тогда //для случая когда Контролер берет ЭКЛ у проверяеМОГО но это НЕ разбор з/п (в т.ч. с уволенным)
                if (usersSDBPTT.fired == 1 && optionDB.getOptionControlId().equals("132629") && (usersSDBPTT.firedDt != null && usersSDBPTT.firedDt != 0) && wpDataDB.getTheme_id() != 421) {
                } else if (usersSDBPTT.fired == 1 && optionDB.getOptionControlId().equals("133317") && optionDB.getOptionControlId().equals("84006")) {   //для случая, когда берем ЭКЛ у ПТТ
                    signal = false;
                    optionMsg.append(", но ").append("ПТТ уволен! (").append(usersSDBPTT.firedReason).append(")");
                } else if (usersSDBPTT.workAddrId != wpDataDB.getAddr_id() && optionDB.getOptionControlId().equals("133317") && optionDB.getOptionControlId().equals("84006")) {    //для случая, когда берем ЭКЛ у ПТТ
                    signal = false;
                    optionMsg.append(", но ").append("ПТТ не работает по адресу: ").append(addressSDB.nm);
                } else if (usersSDBPTT.otdelId == null || usersSDBPTT.otdelId == 0) {
//                  29.01.2025 изменил сигнал
                    signal = true;
                    optionMsg.append(", но ").append("у ПТТ ").append(usersSDBPTT.fio).append(" не указан отдел в котором он работает!");
                } else if (usersSDBPTT.otdelId != null && tovarGroupSDB1 != null && tovarGroupSDB1.parent != null && tovarGroupSDB1.parent == 0) {    // нет у меня понятия УРОВЕНЬ
                    signal = false;
                    // TODO otdel lvl
                    Globals.writeToMLOG("INFO", "OptionControlEKL/else if /01.09.23./Sheva", "usersSDBPTT.otdelId: " + usersSDBPTT.otdelId);
                    Globals.writeToMLOG("INFO", "OptionControlEKL/else if /01.09.23./Sheva", "SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).parent: " + SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).parent);
                    optionMsg.append(", но ").append("у ПТТ указан отдел ").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm).append(" c нарушением уровня вложенности.")
                    /*.append(" (").append("-- otdel lvl --").append(" из уровня  вложенности!)")*/;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    try {
//                        Globals.writeToMLOG("INFO", "OptionControlEKL/Build.VERSION.SDK_INT", "tovarGroupSDB: " + new Gson().toJson(tovarGroupSDB));
//                    }catch (Exception e){
//                        Globals.writeToMLOG("INFO", "OptionControlEKL/Build.VERSION.SDK_INT", "Exception e: " + e);
//                    }
                    TovarGroupSDB test = tovarGroupSDB.stream().filter(item -> item.id.equals(usersSDBPTT.otdelId)).findFirst().orElse(null);
                    Log.e("test", "test: " + test);
                    if (tovarGroupSDB.stream().filter(item -> item.id.equals(usersSDBPTT.otdelId)).findFirst().orElse(null) == null
                            && !optionDB.getOptionControlId().equals("132629") && (addressSDB.kolKass > 5 || addressSDB.kolKass == 0)) {
                        if (documentUser.reportDate20 == null || documentUser.reportDate20.getTime() > wpDataDB.getDt().getTime()) {
                            signal = false;
                            optionMsg.append(", но ").append("ПТТ работает в отделе ").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm).append(" (№").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).id).append(")").append(" и не может подписывать ЭКЛ для: ")
                                    .append(TG.getNmFromList(tovarGroupSDB)).append(" (№").append(TG.getIdFromList(tovarGroupSDB)).append(")").append(" (но исполнитель не провел свой 20-й отчет и эту блокировку пропускаем)");
                        } else {
                            signal = true;
                            optionMsg.append(", но ").append("ПТТ работает в отделе ").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm).append(" (№").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).id).append(")").append(" и не может подписывать ЭКЛ для: ")
                                    .append(TG.getNmFromList(tovarGroupSDB)).append(" (№").append(TG.getIdFromList(tovarGroupSDB)).append(")").append(" (для магазина в котором более 5 касс и исполнитель провел 20-й отчет)");
                        }
                    } else if (tovarGroupSDB.stream().filter(item -> item.id.equals(usersSDBPTT.otdelId)).findFirst().orElse(null) == null
                            && !optionDB.getOptionControlId().equals("132629") && (addressSDB.kolKass > 0 && addressSDB.kolKass <= 5)) {
                        if (documentUser.reportDate40 == null || documentUser.reportDate40.getTime() >= wpDataDB.getDt().getTime()) {
                            signal = false;
                            optionMsg.append(", но ").append("ПТТ работает в отделе ").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm).append(" (№").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).id).append(")").append(" и не может подписывать ЭКЛ для: ")
                                    .append(TG.getNmFromList(tovarGroupSDB)).append(" (№").append(TG.getIdFromList(tovarGroupSDB)).append(")").append(" (но исполнитель не провел свой 40-й отчет и эту блокировку пропускаем)");
                        } else {
                            signal = false;
                            optionMsg.append("                    RoomManager.SQL_DB.tovarGroupClientDao().getAllBy(client_id, 0)\n").append("ПТТ работает в отделе ").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm).append(" (№").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).id).append(")").append(" и не может подписывать ЭКЛ для: ")
                                    .append(TG.getNmFromList(tovarGroupSDB)).append(" (№").append(TG.getIdFromList(tovarGroupSDB)).append(")").append(" (но в данном магазине ").append(addressSDB.kolKass).append(" касс и это допустимо)");
                        }
                    }
                } else {
                    signal = true;
                }
            }

            Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 8");
        }


        int bonus = -32;
        CharSequence valBonus = "";
        float shtraf = 0.308f;
        long countDay = wpDataDB.getVisit_start_dt() - (DAYS * 24 * 60 * 60);
        long ekl_date = -1L;

        if (documentUser.last_ekl_date != null) {
            ekl_date = convertDateToSeconds(documentUser.last_ekl_date);
            if (ekl_date != -1 && ekl_date > countDay) {
                shtraf = 0.154f;
                bonus = -16;
            }
        }
        valBonus = "~" + String.format("%.2f", wpDataDB.getCash_zakaz() * shtraf);
        valBonus = Html.fromHtml("<font color=red>" + valBonus + " грн" + "</font>");

        // "подводим итог"
        if (signal) {
            List<DossierSotrSDB> dossierSotrSDBS = SQL_DB.dossierSotrDao().getDataByClientAddress(982L, (long) wpDataDB.getAddr_id(), wpDataDB.getClient_id());

            LocalDate newest = findNewestDossierDate(dossierSotrSDBS);
            LocalDate dat = wpDataDB.getDt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (newest == null) {
                // ПустоеЗначение(ДатПерОИ)=1
                signal = false;
                optionMsg.append("\n\nАле виконавець ");
                optionMsg.append(wpDataDB.getUser_txt());
                optionMsg.append(" на поточний момент ще не провів жодного ЗВ по Клієнту ");
                optionMsg.append(wpDataDB.getClient_txt());
                optionMsg.append(" за цією адресою, тож на перший раз для нього робимо виняток.");
            } else {
                // ДатПерОИ > Дат-15
                LocalDate border = dat.minusDays(15);
                if (newest.isAfter(border)) {
                    signal = false;
                    optionMsg.append("\n\nАле виконавець ");
                    optionMsg.append(wpDataDB.getUser_txt());
                    optionMsg.append(" ще працює за цією Адресою i з цім Клієнтом менше 14 діб. Робимо для нього виняток.");
                }
            }
        }

        // Изначально ЭТО не надо было вообще писать, НО для парней с < 5 отчётами надо сделать исключение
        if (signal && (documentUser.reportDate20 == null || documentUser.reportDate20.getTime() > wpDataDB.getDt().getTime())) {
            signal = false;
            optionMsg.append("Исполнитель еще не провел свою двадцатую отчетность! ЭКЛ не подписан!").append("\n\n");
        }

        // 07.03.25 добавил штрафы/премии в экл
        if (signal)
            optionMsg
                    .append("\n\n")
                    .append("Останній раз ви отримували ЕКЛ - ")
                    .append(documentUser.last_ekl_date != null ? documentUser.last_ekl_date : Html.fromHtml("<font color=red>" + "немає даних" + "</font>"))
                    .append((ekl_date != -1 && ekl_date > countDay) ? ", що меньше " : ", що більше ")
                    .append(DAYS + " днів, тому ваші преміальні ")
                    .append(bonus >= 0 ? "збільшено" : "зменшено").append(" на ")
                    .append(bonus == -32 ? "подвiйну суму, що становитиме " : "")
                    .append(bonus >= 0 ? Html.fromHtml("<font color=green>" + valBonus + "%</font>") : Html.fromHtml("<font color=red>" + valBonus + "</font>")).append(".")
                    .append("\n")
                    .append((ekl_date != -1 && ekl_date > countDay) ? "" : "Якщо ви отримаєте ЕКЛ по будь-яким клієнту, то відсоток поточних відрахувань буде зменшено вдвічі.")
                    .append("");


        if (signal) {
            int[] reportThresholds = {2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000};
            int[][] dateThresholds = {
                    {2025, Calendar.MARCH, 1},
                    {2025, Calendar.APRIL, 1},
                    {2025, Calendar.MAY, 1},
                    {2025, Calendar.JUNE, 1},
                    {2025, Calendar.JULY, 1},
                    {2025, Calendar.AUGUST, 1},
                    {2025, Calendar.SEPTEMBER, 1},
                    {2025, Calendar.OCTOBER, 1},
                    {2025, Calendar.NOVEMBER, 1}
            };

            for (int i = 0; i < reportThresholds.length; i++) {
                int threshold = reportThresholds[i];
                int year = dateThresholds[i][0];
                int month = dateThresholds[i][1];
                int day = dateThresholds[i][2];

                Calendar thresholdDate = Calendar.getInstance();
                thresholdDate.set(year, month, day);
                thresholdDate.set(Calendar.HOUR_OF_DAY, 0);
                thresholdDate.set(Calendar.MINUTE, 0);
                thresholdDate.set(Calendar.SECOND, 0);
                thresholdDate.set(Calendar.MILLISECOND, 0);

                boolean allowByDate;

                if (i == 0) {
                    // Первое условие: строго ДО 01.03.2025
                    allowByDate = wpDataDB.getDt().before(thresholdDate.getTime());
                } else {
                    // Остальные условия: до и ВКЛЮЧИТЕЛЬНО даты
                    allowByDate = !wpDataDB.getDt().after(thresholdDate.getTime());
                }

                if (documentUser.reportCount >= threshold && allowByDate) {
                    signal = false;
                    optionMsg.append(", но сотрудник провел более ")
                            .append(threshold)
                            .append(" отчетов и эту блокировку пропускаем до ")
                            .append(String.format("%02d.%02d.%04d", day, month + 1, year));
                    break;
                }
            }
        }


        spannableStringBuilder.append(optionMsg);


        // Установка блокирует ли опция работу приложения или нет
        if (signal) {
            if (optionDB.getBlockPns().equals("1") && (nnkMode.equals(Options.NNKMode.MAKE) || nnkMode.equals(Options.NNKMode.BLOCK)) || nnkMode.equals(Options.NNKMode.BLOCK)) {
                showUnlockCodeDialogInMainThread();
            } else {
                spannableStringBuilder.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете получать ЭКЛ у ПТТ.");
            }
        }

        // сохраняем сигнал
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


    public void showUnlockCodeDialogInMainThread() {
        new UnlockCode().showDialogUnlockCode(context, wpDataDB, optionDB, CODE_DAD_2_AND_OPTION, new Clicks.clickStatusMsg() {
            @Override
            public void onSuccess(String data) {
                signal = false;
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionDB != null) {
                        if (signal) {
                            optionDB.setBlockPns("1");
                        } else {
                            optionDB.setIsSignal("0");
                        }
                        realm.insertOrUpdate(optionDB);
                    }
                });
                setIsBlockOption(signal);
                unlockCodeResultListener.onUnlockCodeSuccess();
            }

            @Override
            public void onFailure(String error) {
                setIsBlockOption(signal);
                spannableStringBuilder.append("\n\n").append("Документ проведен не будет!");
//                spannableStringBuilder.append(stringBuilderMsg);
                showOptionMassage("");
                unlockCodeResultListener.onUnlockCodeFailure();
            }
        });
    }

    private CharSequence counter2EKLText() {
        CharSequence res = "";

        UsersSDB users = SQL_DB.usersDao().getUserById(wpDataDB.getUser_id());
        if (users != null) {
            float shtraf = 0.32f;
            if (users.last_ekl_date != null) {
                long ekl_date = convertDateToSeconds(users.last_ekl_date);
                long countDay = wpDataDB.getVisit_start_dt() - (DAYS * 24 * 60 * 60);
                if (ekl_date != -1 && ekl_date > countDay)
                    shtraf = 0.16f;
            }
            res = "~" + String.format("%.2f", wpDataDB.getCash_zakaz() * shtraf);
            res = Html.fromHtml("<font color=red>" + res + "грн" + "</font>");
        }

        return res;
    }

    public static long convertDateToSeconds(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // Парсим строку в объект Date
            Date date = dateFormat.parse(dateString);
            // Преобразуем Date в миллисекунды и делим на 1000, чтобы получить секунды
            return date.getTime() / 1000;
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // В случае ошибки возвращаем -1
        }
    }


    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    // 1) парс даты из dt (yyyy-MM-dd)
    private static LocalDate parseDtOrNull(String dt) {
        if (dt == null || dt.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dt.trim(), DT_FMT);
        } catch (DateTimeParseException e) {
            return null; // если внезапно мусор в dt
        }
    }

    // 2) самая свежая дата из списка по полю dt
    public static LocalDate findNewestDossierDate(List<DossierSotrSDB> list) {
        if (list == null || list.isEmpty()) return null;

        Optional<LocalDate> newest = list.stream()
                .map(x -> parseDtOrNull(x.dt))
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());

        return newest.orElse(null);
    }

    /**
     * Повтор логики 1С (в упрощённом виде, как в фрагменте):
     * - если записи нет -> signal=false + текст
     * - else if newest > (dat - 15) -> signal=false + текст
     *
     * @param dat текущая "Дат" из 1С (дата документа/проверки)
     */
    public static boolean apply1CLogic(
            boolean signal,
            LocalDate dat,
            List<DossierSotrSDB> dossierList,
            StringBuilder text // или SpannableStringBuilder
    ) {
        if (!signal) return false; // уже сброшен ранее

        LocalDate newest = findNewestDossierDate(dossierList);

        if (newest == null) {
            // ПустоеЗначение(ДатПерОИ)=1
            signal = false;
            if (text != null) {
                text.append("\nАле виконавець ще не провів жодного ЗВ по цій Співробітник/Клієнто/Адресі і, на перший раз, для нього робимо виключення.");
            }
            return false;
        }

        // ДатПерОИ > Дат-15
        LocalDate border = dat.minusDays(15);
        if (newest.isAfter(border)) {
            signal = false;
            if (text != null) {
                text.append("\nАле виконавець ще працює цій Адресі з цім Клієнтом менше 14 діб. Робимо для нього виключення.");
            }
        }

        return signal;
    }

}


