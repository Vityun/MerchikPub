package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.data.RealmModels.StackPhotoDB.PHOTO_TOV_LEFT;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;

/**
 * 06.04.2023.
 * Контроль наличия Фото Остатков Товаров (для любого из Клиентов)
 */
public class OptionControlPhotoTovarsLeft<T> extends OptionControl {
    public int OPTION_CONTROL_PHOTO_TOVARS_LEFT_ID = 1470;

    private static final int DEFAULT_DAY_FROM = 8;  // Количество дней "С"
    private static final int COL_MIN = 1;  // Минимальное кол-во фоток которое нужно для работы опции

    // option data
    public boolean signal = false;
    private Integer[] groups = {346, 434};  // исключаем из отчетов: 346-Сильпо, 434-АТБ
    private Integer[] groupsNew = {434};  // исключаем из отчетов: 434-АТБ

    // document data
    private UsersSDB usersSDB;
    private AddressSDB addressSDB;

    private int addrId;
    private String clientId;
    private long dad2 = 0;
    private long documentDate;  // На данный момент в миллисекундах
    private long dateFrom;
    private long dateTo;
    private Integer tpId; // идентификатор сети (сильпо, атб..)

    public OptionControlPhotoTovarsLeft(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
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
        if (document instanceof WpDataDB) {
            WpDataDB wp = (WpDataDB) document;

            addrId = wp.getAddr_id();
            clientId = wp.getClient_id();
            dad2 = wp.getCode_dad2();
            documentDate = wp.getDt().getTime();

            usersSDB = SQL_DB.usersDao().getById(wp.getUser_id());
            addressSDB = SQL_DB.addressDao().getById(wp.getAddr_id());

            tpId = addressSDB.tpId;

            calculateDateSpan();    // Получение dateFrom/dateTo
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        List<StackPhotoDB> stackPhoto = StackPhotoRealm.getPhoto(dateFrom, dateTo, null, addrId, null,  null, PHOTO_TOV_LEFT, null);

        if (tpId == 8923 && (usersSDB.reportDate01 != null || usersSDB.reportDate05.getTime() >= documentDate)) {
            stringBuilderMsg.append("Для Новуса наличие ФОТ не проверяем до 5-го отчета.");
            signal = false;
        } else if (tpId == 8923 && stackPhoto.size() < COL_MIN) {
            stringBuilderMsg.append("За период с ").append(Clock.getHumanTimeSecPattern(dateFrom / 1000, "dd-MM"))
                    .append(" по ").append(Clock.getHumanTimeSecPattern(dateTo / 1000, "dd-MM"))
                    .append(" есть ").append(stackPhoto.size())
                    .append(" ФОТ. Для Новуса можно использовать ФОТ из приложения НОВУС. Но ФОТ должно быть столько-же, сколько СКЮ: ")
                    .append(COL_MIN).append(" (не более 10-и)");
            signal = true;
        } else if (stackPhoto.size() > 0) {
            stringBuilderMsg.append("За период с ").append(Clock.getHumanTimeSecPattern(dateFrom / 1000, "dd-MM"))
                    .append(" по ").append(Clock.getHumanTimeSecPattern(dateTo / 1000, "dd-MM"))
                    .append(" Сотрудником ").append(usersSDB.fio).append(" выполнено ").append(stackPhoto.size())
                    .append(" ФОТ (Фото Остатков Товаров)");
            signal = false;
        } else if (usersSDB.reportDate20 == null || documentDate <= usersSDB.reportDate20.getTime() || (usersSDB.reportDate20 != null && usersSDB.reportCount < 30)) {
            stringBuilderMsg.append("Не проверяю наличие ФОТ (Фото Остатков Товаров) для исполнителей не провевших 20-ть отчетов");
            signal = false;
        } else if (System.currentTimeMillis()/1000 < 1683849600 && Arrays.stream(groups).anyMatch(x -> Objects.equals(x, tpId))) {
            stringBuilderMsg.append("Не проверяю наличие ФОТ (Фото Остатков Товаров) для сетей: Сільпо, АТБ");  // todo Нужно будет Сети нормально вписать, а не руками.
            signal = false;
        }else if (System.currentTimeMillis()/1000 > 1683849600 && Arrays.stream(groupsNew).anyMatch(x -> Objects.equals(x, tpId))){
            stringBuilderMsg.append("Не проверяю наличие ФОТ (Фото Остатков Товаров) для сетей: АТБ");  // todo Нужно будет Сети нормально вписать, а не руками.
            signal = false;
        }else {
            stringBuilderMsg.append("За период с ").append(Clock.getHumanTimeSecPattern(dateFrom / 1000, "dd-MM"))
                    .append(" по ").append(Clock.getHumanTimeSecPattern(dateTo / 1000, "dd-MM"))
                    .append(" Сотрудником ").append(usersSDB.fio).append(" НЕ выполнено ни одного ФОТ (Фото Остатков Товаров) по ЛЮБОМУ Адресо/Клиенту.");
            signal = true;
        }

        saveOptionResultInDB();
        if (signal) {
            if (optionDB.getBlockPns().equals("1")) {
                setIsBlockOption(signal);
                stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
            } else {
                stringBuilderMsg.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете вносить отчетность корректно.");
            }
        }

    }

    /**
     * 09.01.2023.
     * Вычисление Промежутка Дат
     * Определяем ДатаС и ДатаПо для работы опции контроля
     */
    private void calculateDateSpan() {
        int colMax = optionDB.getAmountMax() != null ? Integer.parseInt(optionDB.getAmountMax()) : 0;   // Получаю Кол.Макс. из Опции
        int dayFrom = colMax > 0 ? colMax : DEFAULT_DAY_FROM;  // В зависимости от Кол. Макс - смотрю

        if (tpId == 8196) {  // 8196 -- Ашан (//для Ашана добавим еще 7-ь дней )
            dayFrom += 7;
        }

        dateFrom = Clock.getDatePeriodLong(documentDate, -dayFrom);
        dateTo = Clock.getDatePeriodLong(documentDate, 4);  // дадим возможность ребятам ЗАВТРА внести ФОТ (12.01.23. изменил от балды на 4. Теоретически это Сегодня +3 дня)
    }

    /**
     * Сохранение данных об опции контроля в БД
     */
    private void saveOptionResultInDB() {
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
    }

}
