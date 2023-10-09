package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.Global.UnlockCode.UnlockCodeMode.CODE_DAD_2_AND_OPTION;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Global.UnlockCode;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
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

    public int getResultCode() {
        return resultCode;
    }

    public OptionControlEKL(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
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

        Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 2");

        int userId = wpDataDB.getUser_id();
        String ptt = PTT;
        long dateFrom = Clock.getDatePeriodLong(documentDt * 1000, -3);
        long dateTo = Clock.getDatePeriodLong(documentDt * 1000, 4);

        Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 3");

        if (addressSDB.tpId == 434 && !optionDB.getOptionControlId().equals("132629") && documentDt < 1725148800) { // 1725148800 == 01.09.2024 / 434 = АТБ
            optionMsg.append("Не проверяем для АТБ до 01.09.2024");
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
                tovarGroupClientSDB = SQL_DB.tovarGroupClientDao().getAllBy(wpDataDB.getClient_id(), addressSDB.tpId);  // Получаю ГруппыТоваров по Адресу и Сети!
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
            eklSDB = SQL_DB.eklDao().getBy(dateFrom/1000, dateTo/1000, wpDataDB.getClient_id(), wpDataDB.getAddr_id(), wpDataDB.getUser_id());
//            eklSDB = SQL_DB.eklDao().getBy(dateFrom, dateTo, wpDataDB.getClient_id(), wpDataDB.getAddr_id(), wpDataDB.getUser_id());
//        eklSDB = SQL_DB.eklDao().getBy(dateFrom, dateTo, wpDataDB.getClient_id(), wpDataDB.getAddr_id(), wpDataDB.getUser_id(), wpDataDB.ptt_user_id);

            List<EKL_SDB> eklSDB2 = SQL_DB.eklDao().getAll();
            Log.e("OptionControlEKL", "HERE TEST OptionControlEKL eklSDB: " + new Gson().toJson(eklSDB));
            Log.e("OptionControlEKL", "HERE TEST OptionControlEKL eklSDB2: " + new Gson().toJson(eklSDB2));

            Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 5");
            if (eklSDB == null || eklSDB.size() == 0) {
                List<Integer> ids = new ArrayList<>();
                for (TovarGroupSDB item : tovarGroupSDB) {
                    ids.add(item.id);
                }

                String msgDebug = String.format("dateFrom: %s/dateTo: %s/ids: %s/addr: %s/user: %s/ptt: %s", dateFrom, dateTo, ids, wpDataDB.getAddr_id(), wpDataDB.getUser_id(), wpDataDB.ptt_user_id);
                Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", msgDebug);

                /*
                {"addr_id":24655,"client_id":"14176","code":"61827","code_dad2":1261222024655052738,
                "department":824,"eklCode":"56386ec2be278c6de8b1b05912b02458b56f31dc","code_check":"56386ec2be278c6de8b1b05912b02458b56f31dc",
                "ID":630450,"user_id_verify":214355,"state":true,"upload":true,"user_id":204206,"vpi":1672046893656}

                {"addr_id":24655,"client_id":"8169","code":"99257","code_dad2":1271222024655052833,
                "department":1440,"eklCode":"03634c8d9adf70c88ecf40447c6517bb65f9ed85","code_check":"03634c8d9adf70c88ecf40447c6517bb65f9ed85",
                "ID":630468,"user_id_verify":197055,"state":true,"upload":true,"user_id":204206,"vpi":1672049203918}
                 */

                eklSDB = SQL_DB.eklDao().getBy(dateFrom, dateTo, ids, wpDataDB.getAddr_id(), wpDataDB.getUser_id());

                if (eklSDB != null) {
                    Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "eklSDB1: " + eklSDB.size());
                } else {
                    Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "eklSDB1: EMPTY");
                }
            } else {
                Globals.writeToMLOG("INFO", "OptionControlEKL/createTZN", "eklSDB2: " + eklSDB.size());
            }

            Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 6");

            // Проверка ЭКЛов
            if (eklSDB == null || eklSDB.size() == 0) {
                signal = true;
                optionMsg.append("За период с ")
                        .append(Clock.getHumanTime3(dateFrom / 1000))
                        .append(" по ")
                        .append(Clock.getHumanTime3(dateTo / 1000))
                        .append(" НЕ получено ни одного ЭКЛ ")
                        .append(controllerType);
            /*  //добавим исключение
				Если (ДокИст.Вид()="ОтчетИсполнителя") и (КодВлад=8196) Тогда //для Ашанов, котоые работают через ДОТ и ФОТ НЕ проверяем ЭКЛ
					Если (ПустоеЗначение(ДокИст.ОператорДОТ)=0) или (ПустоеЗначение(ДокИст.ОператорФОТ)=0) Тогда
						Причина=Причина+", но для Ашанов по которым работаем с ДОТ или ФОТ ЭКЛ не проверяем.";
						НеПроверятьПр=1;
						Тзн.Зачет=1;
					КонецЕсли;
				КонецЕсли;*/

                Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 7");
            } else {
                if (usersSDBPTT == null) {
                    usersSDBPTT = SQL_DB.usersDao().getById(eklSDB.get(0).sotrId);
                }
                optionMsg.append("За период с ")
                        .append(Clock.getHumanTime3(dateFrom / 1000)).append(" по ")
                        .append(Clock.getHumanTime3(dateTo / 1000))
                        .append(" получено ").append(eklSDB.size()).append(" ЭКЛ у ").append(usersSDBPTT.fio)
                        .append(" (").append(usersSDBPTT.department).append(") тел: ").append(usersSDBPTT.tel)
                        .append(", ").append(usersSDBPTT.tel2);


                //Если (ПТТ.Уволен=1) и (Опц=глОпция132629) и (ПустоеЗначение(ПТТ.ДатаУвол)=0) и (ПТТ.ДатаУвол<Дат) и (Тем<>Тема421) Тогда //для случая когда Контролер берет ЭКЛ у проверяеМОГО но это НЕ разбор з/п (в т.ч. с уволенным)
                if (usersSDBPTT.fired == 1 && optionDB.getOptionControlId().equals("132629") && (usersSDBPTT.firedDt != null && usersSDBPTT.firedDt != 0) && wpDataDB.getTheme_id() != 421) {
                    Log.d("test", "nosing to show");
                /*  Тзн.Наруш=1;
					Причина="ПТТ уволен! ("+СокрЛП(ПТТ.ПричинаУвольнения)+")";
					Тзн.Прим=Прим+", но "+Причина;
					Если Спис.НайтиЗначение(Причина)=0 Тогда
						Спис.ДобавитьЗначение(Причина); //для передачи в чат
					КонецЕсли;
					*/
                } else if (usersSDBPTT.fired == 1 && optionDB.getOptionControlId().equals("133317") && optionDB.getOptionControlId().equals("84006")) {   //для случая, когда берем ЭКЛ у ПТТ
                    signal = false;
                    optionMsg.append(", но ").append("ПТТ уволен! (").append(usersSDBPTT.firedReason).append(")");
                } else if (usersSDBPTT.workAddrId != wpDataDB.getAddr_id() && optionDB.getOptionControlId().equals("133317") && optionDB.getOptionControlId().equals("84006")) {    //для случая, когда берем ЭКЛ у ПТТ
                    signal = false;
                    optionMsg.append(", но ").append("ПТТ не работает по адресу: ").append(addressSDB.nm);
                } else if (usersSDBPTT.otdelId == null || usersSDBPTT.otdelId == 0) {
                    signal = false;
                    optionMsg.append(", но ").append("у ПТТ ").append(usersSDBPTT.fio).append(" не указан отдел в котором он работает!");
                } else if (usersSDBPTT.otdelId != null && SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).parent == 0) {    // нет у меня понятия УРОВЕНЬ
                    signal = false;
                    // TODO otdel lvl
                    Globals.writeToMLOG("INFO", "OptionControlEKL/else if /01.09.23./Sheva", "usersSDBPTT.otdelId: " + usersSDBPTT.otdelId);
                    Globals.writeToMLOG("INFO", "OptionControlEKL/else if /01.09.23./Sheva", "SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).parent: " + SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).parent);
                    optionMsg.append(", но ").append("у ПТТ указан отдел ").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm).append(" c нарушением уровня вложенности.")
                    /*.append(" (").append("-- otdel lvl --").append(" из уровня  вложенности!)")*/;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (tovarGroupSDB.stream().filter(item -> item.id.equals(usersSDBPTT.otdelId)).findFirst().orElse(null) != null
                            && optionDB.getOptionControlId().equals("132629") && (addressSDB.kolKass > 5 || addressSDB.kolKass == 0)) {
                        if (documentUser.reportDate05 != null && documentUser.reportDate05.getTime() >= wpDataDB.getDt().getTime()) {
                            signal = true;
                            optionMsg.append(", но ").append("ПТТ работает в отделе ").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm).append(" и не может подписывать ЭКЛ для: ")
                                    .append(TG.getNmFromList(tovarGroupSDB)).append(" (но исполнитель не провел свой 5-й отчет и эту блокировку пропускаем)");
                        } else {
                            signal = false;
                            optionMsg.append(", но ").append("ПТТ работает в отделе ").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm).append(" и не может подписывать ЭКЛ для: ")
                                    .append(TG.getNmFromList(tovarGroupSDB)).append(" (для магазина в котором более 5 касс и исполнитель провел 5-й отчет)");
                        }
                    } else if (tovarGroupSDB.stream().filter(item -> item.id.equals(usersSDBPTT.otdelId)).findFirst().orElse(null) != null
                            && optionDB.getOptionControlId().equals("132629") && (addressSDB.kolKass > 0 || addressSDB.kolKass <= 5)) {
                        if (documentUser.reportDate05 != null && documentUser.reportDate05.getTime() >= wpDataDB.getDt().getTime()) {
                            signal = true;
                            optionMsg.append(", но ").append("ПТТ работает в отделе ").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm).append(" и не может подписывать ЭКЛ для: ")
                                    .append(TG.getNmFromList(tovarGroupSDB)).append(" (но исполнитель не провел свой 5-й отчет и эту блокировку пропускаем)");
                        } else {
                            signal = true;
                            optionMsg.append(", но ").append("ПТТ работает в отделе ").append(SQL_DB.tovarGroupDao().getById(usersSDBPTT.otdelId).nm).append(" и не может подписывать ЭКЛ для: ")
                                    .append(TG.getNmFromList(tovarGroupSDB)).append(" (но в данном магазине ").append(addressSDB.kolKass).append(" касс и это допустимо)");
                        }
                    }
                } else {
                    signal = true;
                }
            }

            Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 8");
        }

        // "подводим итог"
        // Изначально ЭТО не надо было вообще писать, НО для парней с < 5 отчётами надо сделать исключение
        if (signal && (documentUser.reportDate05 == null || documentUser.reportDate05.getTime() > wpDataDB.getDt().getTime())) {
            signal = false;
            stringBuilderMsg.append("Исполнитель еще не провел свою пятую отчетность! ЭКЛ не подписан!").append("\n\n");
        }

        Log.e("OptionControlEKL", "HERE TEST OptionControlEKL 9");
        stringBuilderMsg.append(optionMsg);

        // Установка блокирует ли опция работу приложения или нет
        if (signal) {
            if (optionDB.getBlockPns().equals("1") && (nnkMode.equals(Options.NNKMode.MAKE) || nnkMode.equals(Options.NNKMode.BLOCK)) || nnkMode.equals(Options.NNKMode.BLOCK)) {
                showUnlockCodeDialogInMainThread();
            } else {
                stringBuilderMsg.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете получать ЭКЛ у ПТТ.");
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
    }


    public void showUnlockCodeDialogInMainThread() {

            new UnlockCode().showDialogUnlockCode(context, wpDataDB, optionDB, CODE_DAD_2_AND_OPTION, new Clicks.clickStatusMsg() {
                @Override
                public void onSuccess(String data) {
                    signal = false;
                    setIsBlockOption(signal);
                    unlockCodeResultListener.onUnlockCodeSuccess();
                }

                @Override
                public void onFailure(String error) {
                    setIsBlockOption(signal);
                    stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
                    spannableStringBuilder.append(stringBuilderMsg);
                    showOptionMassage("");
                    unlockCodeResultListener.onUnlockCodeFailure();
                }
            });

    }


}


