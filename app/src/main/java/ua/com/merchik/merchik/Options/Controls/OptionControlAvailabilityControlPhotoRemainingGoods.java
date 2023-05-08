package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;


/**
 * 04.05.23.
 * // 30.04.2023 Петров Создал на основании ПровФотоОстТовКли
 * // Контроль наличия Фото Остатков Товаров (по ОТСУТСТВУЮЩИМ товарам, ... если указано ОСВ то проверяем их, а если нет то проверяем ВСЕ ОТСУТСТВУЮЩИЕ).
 * Крое этой есть еще такая-же но с контролем наличия остатков по всем клиентам и отдельно по КОНКРЕТНОМУ клиенту
 * // Вызывается из функции КонтрольОпций.
 * // ДокИст - документ - источник типа Задача, ОтчетИсполнителя, ОтчетОСтажировке и пр. к которому подчинена данная опция
 * // ДокОпц - документ - набор опций (на момент передачи в єту функцию позиционирован на конкретную строку с ДАННОЙ опцией)
 */
public class OptionControlAvailabilityControlPhotoRemainingGoods<T> extends OptionControl {

    public int OPTION_CONTROL_AVAILABILITY_CONTROL_PHOTO_REMAINING_GOODS_ID = 159707;

    private boolean signal = true;

    private int userId;
    private long dad2;

    // 1.2
    private Integer[] groups = {434};  // исключаем из отчетов: 434-АТБ
    private String[] tovIds;    // Список Товаров с ОСВ.

    private WpDataDB wpDataDB;
    private AddressSDB addressSDBDocument;
    private CustomerSDB customerSDBDocument;

    public OptionControlAvailabilityControlPhotoRemainingGoods(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        getDocumentVar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeOption();
        }
    }

    private void getDocumentVar() {
        try {
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;

                userId = wpDataDB.getUser_id();
                dad2 = wpDataDB.getCode_dad2();
                customerSDBDocument = SQL_DB.customerDao().getById(wpDataDB.getClient_id());
                addressSDBDocument = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityControlPhotoRemainingGoods/getDocumentVar", "Exception e: " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        try {

            //2.0. получим данные о товарах в отчете (если она еще не рассчитана)
            List<ReportPrepareDB> reportPrepare = ReportPrepareRealm.getReportPrepareByDad2(dad2);

            //3.0. получим список товаров с особым вниманием (хранится в Доп.Требованиях)
            List<AdditionalRequirementsDB> additionalRequirements = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(document, true, OPTION_CONTROL_AVAILABILITY_CONTROL_PHOTO_REMAINING_GOODS_ID, null, null, null);

            //3.1. получаем список товаров для которых установлен признак ОСВ
            if (additionalRequirements != null && additionalRequirements.size() > 0) {
                tovIds = new String[additionalRequirements.size()];
                for (int i = 0; i < additionalRequirements.size(); i++) {
                    tovIds[i] = additionalRequirements.get(i).getTovarId();
                }
                Arrays.sort(tovIds);
            }
            //3.2. если нет товаров с ОСВ для данной опции, то берем все товары из самого отчета
            else {
                tovIds = new String[reportPrepare.size()];
                for (int i = 0; i < reportPrepare.size(); i++) {
                    tovIds[i] = reportPrepare.get(i).getTovarId();
                }
                Arrays.sort(tovIds);
            }

            //4.0. получим данные о размещенных ФОТ по конкретному ДАД2
            List<StackPhotoDB> stackPhotoList = StackPhotoRealm.getPhoto(null, null, userId, null, null, dad2, 4, tovIds); // Тип фото, Исполнитель, Дад2, Список Товаров . 4-Фото Остатков Товаров,

            //5.2. заполним ее данными ОСВ
            for (ReportPrepareDB item : reportPrepare) {
                int face = Integer.parseInt(item.face);
                if (face == 0 && stackPhotoList.stream().anyMatch(stackPhoto -> stackPhoto.tovar_id.equals(item.getTovarId()))) {
                    TovarDB tovar = TovarRealm.getById(item.getTovarId());
                    item.error = 1;
                    item.errorNote = "Ви повинні завантажити в нашу систему світлину з залишком товару: (" + tovar.getiD() + ") " + tovar.getNm() + " отриману з додатку мережі.";
                }
            }

            // Итоговое количество нарушений
            int errorSum = reportPrepare.stream()
                    .mapToInt(rp -> rp.error)
                    .sum();

            //6.0. готовим сообщение и сигнал
            if (reportPrepare.size() == 0) {
                stringBuilderMsg.append("Товарів, не знайдено.");
                signal = true;
            } else if (errorSum > 0) {
                stringBuilderMsg.append("Не надані світлини з ЗАЛИШКАМИ ").append(errorSum).append(" відсутніх товарів. Див. таблицю. Таким чином Ви повинні підтвердити, що даних товарів нема на залишках.");
                signal = true;
            } else {
                stringBuilderMsg.append("Зауваженнь по наданню світлин залишків по відсутнім товарам нема.");
                signal = false;
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
                    stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
                } else {
                    stringBuilderMsg.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете делать Достижения.");
                }
            }


        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityControlPhotoRemainingGoods/executeOption", "Exception e: " + e);
        }
    }
}
