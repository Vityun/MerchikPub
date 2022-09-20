package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupClientSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class OptionControlEKL<T> extends OptionControl {
    public int OPTION_CONTROL_EKL_ID = 84006;

    private WpDataDB wpDataDB;
    private AddressSDB addressSDB;
    private List<TovarGroupClientSDB> tovarGroupClientSDB;
    private List<TovarGroupSDB> tovarGroupSDB;
    private List<EKL_SDB> eklSDB;
    private UsersSDB usersSDBPTT;

    private long documentDt;

    private String PTT;
    private String controllerType = "ПТТ";   //по умолчанию ЭКЛ получаем у ПТТ, но для 132629 у ПРОВЕРЯЕМОГО

    private boolean signal = false;


    public OptionControlEKL(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
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
            wpDataDB = (WpDataDB) document;

            documentDt = wpDataDB.getDt().getTime() / 1000;

            addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
            usersSDBPTT = SQL_DB.usersDao().getById(wpDataDB.ptt_user_id);
        }
    }

    private void executeOption() {
        try {
            createTZN();


        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlEKL/executeOption/Exception", "Exception: " + e);
        }
    }


    /*Тут в теории должен собираться ТЗН "с одной строки" для подальшей работы с ним. Пока сути для
    меня не вижу кроме как указать что ПТТшник может быть пустым настарте и мы его ПОТОМ переопределим*/
    private void createTZN() {
        PTT = "";   // Сбрасываем ПТТшника в режим "любой"

        // TODO Это на будущее. Пока это не надо. Можно закоментить.
        // Индивидуальный-ЭКЛ (это заглушка, пока на стороне 1С нормально эт не реализовано)
        /*if (optionDB.getOptionId().equals("151140")) {
            Globals.writeToMLOG("INFO", "OptionControlEKL.executeOption.optionDB.getOptionId().equals(\"151140\")", "Вы попали в Заглушку");

            if (wpDataDB.ptt_user_id != 0) {    //если в ОИ явно указан интересующий нас ПТТ (для данной опции заполняется в момент создания ОИ ... и у ДАННОГО ПТТ и надо брать ИЭКЛ)
                // TODO !!! --- Не понимаю откуда брать признак уволенности сотрудника.
                // Проверка уволен/ не уволен ли этот ПТТ. Пока что не могу ответить на этот вопрос.
                //Если ДокИст.ПТТ.Уволен=0 Тогда //если в ОИ данный ПТТ НЕ уволен то можно его использовать для проверки наличия ИЭКЛ
                //        ПТТ=ДокИст.ПТТ;
                //КонецЕсли;

                PTT = String.valueOf(wpDataDB.ptt_user_id);
            }

            if (PTT.equals("") || PTT.equals("0")) {    //если ПТТ не определен то заглянем в ДТ ... там должен храниться "свежий" ПТТ для данного адреса
                //ПТТ=ТзнДопТребований(,,ДокИст.Заказ,,Адр,,,,,глОпция151140,,3); //=3-вернуть Сотрудника (ПТТ для ИЭКЛ)
                RealmResults<AdditionalRequirementsDB> additionalRequirementsDB = AdditionalRequirementsRealm.getAdditionalRequirementsDBTest(wpDataDB.getClient_id(), String.valueOf(wpDataDB.getAddr_id()), optionDB.getOptionId());
                PTT = Objects.requireNonNull(additionalRequirementsDB.where().findFirst()).userId;
            }
        }*/


        int userId = wpDataDB.getUser_id();
        String ptt = PTT;
        long dateFrom = Clock.getDatePeriodLong(documentDt, -1);
        long dateTo = Clock.getDatePeriodLong(documentDt, 1);


        // Определем Группу Товаров
        if (optionDB.getOptionId().equals("132629")) {
            //для 132629-Контроль ЭКЛ между ПРОВЕРЯЮЩИМ и ПРОВЕРЯЕМЫМ (электронный контрольный лист) НЕ имеет значения в каком он отделе
        } else if (optionDB.getOptionId().equals("143968")) {
            //для 143968-Контроль ЭКЛ между исполнителем и сотрудником КЛИЕНТА (электронный контрольный лист) НЕ имеет значения в каком он отделе
        } else if (optionDB.getOptionId().equals("151140") && (ptt.equals("") || ptt.equals("0"))) {
            //для 151140-Контроль ЭКЛ между исполнителем и КОНКРЕТНЫМ ПТТ (ИНДИВИДУАЛЬНЫЙ электронный контрольный лист) НЕ имеет значения в каком отделе ПТТ. Отдел НЕ важен, если ПТТ для подписания ИЭКЛ определен, а если НЕТ то все-таки нужно определить отдел
            //ГрупТов.ДобавитьЗначение(ПТТ.Отдел); //если мы уже определились с ПТТ в этом режиме то и отдел получим из ПТТ ... клиент сам решил использовать ЭТОГО ПТТ независимо от отдела
        } else if (optionDB.getOptionId().equals("84006")) {
            // TODO !!! --- У меня нет СЕТИ в ГруппахТоваровКлиентов. По этому просто рассматриваю ТОЛЬКО второй вариант.
            // ГрупТов=ТзнГруппТоваровКлиента(Тзн.Зак,,,0,,,,,,Сеть,2,1); //получаем список групп товаров по которым работает данный клиент в ДАННОЙ СЕТИ !!!
            tovarGroupClientSDB = SQL_DB.tovarGroupClientDao().getAllBy(wpDataDB.getClient_id());
            if (tovarGroupClientSDB != null && tovarGroupClientSDB.size() > 0) {
                List<Integer> ids = new ArrayList<>();
                for (TovarGroupClientSDB item : tovarGroupClientSDB) {
                    ids.add(item.tovarGrpId);
                }
                tovarGroupSDB = SQL_DB.tovarGroupDao().getAllByIds(ids);
            } else { //добавим группы товаров КЛИЕНТА (пока без учета подчиненности) Дело в том, что ЭКЛ регистрируется по группе товара полученной из карточки клиента! ... был случай с Блуми, когда один их элементов помечен на удаление и не попадает в выборку ... короче костыль. 02.08.2019
                // TODO !!! --- У меня нет в таблице Клиентов(заказчик) - Групп Товаров
                // ГрупТов.ДобавитьЗначение(Тзн.Зак.ГруппыТоваров);
                Log.d("test", "nosing to show");
            }
        }

        // Готовим часть Сообщения.
        if (optionDB.getOptionId().equals("143968")) {
            controllerType = "между сотрудником: (" + wpDataDB.getUser_txt() + ") и любым сотрудником КЛИЕНТА";
        } else if (optionDB.getOptionId().equals("151140")) {
            controllerType = "между сотрудником: (" + wpDataDB.getUser_txt() + ") и ПТТ " + ptt + " (Индивидуальный ЭКЛ)";
        } else {
            controllerType = "между сотрудником: (" + wpDataDB.getUser_txt() + ") и любым ПТТ по отделу(ам): " + new TovarGroupSDB().getNmFromList(tovarGroupSDB);
        }

        // лезем в таблицу ЭКЛ и проверяем, еслть ли ПОДПИСАННЫЙ ЭКЛ по данным условиям
        //ТзнЭКЛ=ТзнЭКЛ(ДатС,ДатПо,,,Тзн.Зак,Адр,Исп,ПТТ,,,,1,,БезПТТ,,,1); //ПоказатьТЗ(ТзнЭКЛ,"ТзнЭКЛ4"); //проверяем наличие ЭКЛ по данному клиенту,
        // TODO !!! --- Я не могу сделать такой запрос. Только Теоретически.
        //eklSDB = SQL_DB.eklDao().getBy(dateFrom/1000, dateTo/1000, wpDataDB.getClient_id(), wpDataDB.getAddr_id(), wpDataDB.getUser_id(), wpDataDB.ptt_user_id);
        eklSDB = SQL_DB.eklDao().getByClientId(wpDataDB.getClient_id());

        // TODO !!! --- У меня нет в табл. ЭКЛ ГруппТоваров.
        //Если ТзнЭКЛ.КоличествоСтрок()=0 Тогда //если исполнитель не получил ЭКЛ по данному клиенту, проверим, может он получил ЭКЛ по данной товарной группе? Это допустимо, если исполнитель обслуживает в одном отделе сразу несколько клиентов
        //        ТзнЭКЛ=ТзнЭКЛ(ДатС,ДатПо,,,,Адр,Исп,ПТТ,,,,1,ГрупТов,БезПТТ,,,1); //ПоказатьТЗ(ТзнЭКЛ,"ТзнЭКЛ4"); //проверяем наличие кода не по данному клиенту, а по всей группе товаров к которой он относится
        //КонецЕсли;


        // Проверка ЭКЛов
        if (eklSDB == null || eklSDB.size() == 0) {
            signal = false;
            stringBuilderMsg.append("За период с ")
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
        } else {
            // TODO !!! --- НашТелефон(ПТТ,3) -- откуда єто взять
            //Прим="За период с "+ДатС+" по "+ДатПо+" получено "+ТзнЭКЛ.КоличествоСтрок()+" ЭКЛ у "+ФИОИнициалами(ПТТ,)+" ("+ПТТ.Отдел+") тел: "+НашТелефон(ПТТ,3);
            stringBuilderMsg.append("За период с ")
                    .append(Clock.getHumanTime3(dateFrom / 1000)).append(" по ")
                    .append(Clock.getHumanTime3(dateTo / 1000))
                    .append(" получено ").append(eklSDB.size()).append(" ЭКЛ у ").append(usersSDBPTT.fio)
                    .append(" (").append(usersSDBPTT.department).append(") тел: ").append("");

            if (false){ //Если (ПТТ.Уволен=1) и (Опц=глОпция132629) и (ПустоеЗначение(ПТТ.ДатаУвол)=0) и (ПТТ.ДатаУвол<Дат) и (Тем<>Тема421) Тогда //для случая когда Контролер берет ЭКЛ у проверяеМОГО но это НЕ разбор з/п (в т.ч. с уволенным)
                Log.d("test", "nosing to show");
            }else if (false){   //
                Log.d("test", "nosing to show");
            }
        }

    }


}


