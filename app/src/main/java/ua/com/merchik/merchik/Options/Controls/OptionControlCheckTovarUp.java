package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.data.RealmModels.StackPhotoDB.PHOTO_CART_WITH_GOODS;
import static ua.com.merchik.merchik.data.RealmModels.StackPhotoDB.PHOTO_SHOWCASE_BEFORE_START_WORK;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;

// проверяем заполнение данных о количестве товара поднятого со склада
public class OptionControlCheckTovarUp<T> extends OptionControl {
    public static int OPTION_CONTROL_CHECK_TOVAR_UP_ID = 138644;

    // option data
    private boolean signal = false;
    private int offset = 0;
    private StringBuilder note = new StringBuilder();
    private int sumNumberOfTovar = 0;
    private int sumOffset = 0;

    private int tznErrorExist = 0;
    private StringBuilder tznNotes = new StringBuilder();  // тзн. примечание
    private int tznOffset = 0;

    // document data
    private UsersSDB usersSDB;
    private AddressSDB addressSDB;

    private long dad2 = 0;
    private long documentDate;  // На данный момент в миллисекундах
    private Integer tpId; // идентификатор сети (сильпо, атб..)

    public OptionControlCheckTovarUp(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        try {
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            getDocumentVar();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                executeOption();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Build.VERSION.SDK_INT: ").append(Build.VERSION.SDK_INT).append("  Build.VERSION_CODES.N: ").append(Build.VERSION_CODES.N);
                Globals.writeToMLOG("INFO", "OptionControlCheckTovarUp", "sb: " + sb);
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlCheckTovarUp", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wp = (WpDataDB) document;

//            addrId = wp.getAddr_id();
//            clientId = wp.getClient_id();
            dad2 = wp.getCode_dad2();
            documentDate = wp.getDt().getTime();

            usersSDB = SQL_DB.usersDao().getById(wp.getUser_id());
            addressSDB = SQL_DB.addressDao().getById(wp.getAddr_id());

            tpId = addressSDB.tpId;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        // Получаем "ТЗН Тов" (RP по ДАД2)
        List<ReportPrepareDB> reportPrepare = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(dad2));

        // Получаем Фото Товара с Тележки
        List<StackPhotoDB> stackPhoto = StackPhotoRealm.getPhoto(
                Clock.getDatePeriodLong(documentDate, -1),
                Clock.getDatePeriodLong(documentDate, 3),
                null,
                null,
                null,
                dad2,
                PHOTO_CART_WITH_GOODS
        );

        // если ФТТ нет и это АШАН то проверяем еще наличие ФВ До начала работ... для Ашанов сделано исключение
        if (stackPhoto == null || stackPhoto.size() == 0) {
            if (tpId == 8196) {
                stackPhoto = StackPhotoRealm.getPhoto(
                        Clock.getDatePeriodLong(documentDate, -1),
                        Clock.getDatePeriodLong(documentDate, 3),
                        null,
                        null,
                        null,
                        dad2,
                        PHOTO_SHOWCASE_BEFORE_START_WORK
                );
            }
        }

        // 5 проверим кол-во поднятого товара
        int sumUp = 0;
        try {
            sumUp = reportPrepare.stream().map(table -> Integer.parseInt(table.getUp())).reduce(0, Integer::sum);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlCheckTovarUp/executeOption/sumUp", "Exception e: " + e);
        }

        if (sumUp == 0 && optionDB.getBlockPns().equals("1")) {
            for (ReportPrepareDB itemRP : reportPrepare) {
                int face = Integer.parseInt(itemRP.getFace());
                int errorId = Integer.parseInt(itemRP.getErrorId());

                if (face == 0) continue;
                itemRP.numberOfTovar = 1;

                if (errorId == 17 || errorId == 18) {
                    itemRP.offset = 1;
                }
            }


            try {
                sumNumberOfTovar = reportPrepare.stream().map(table -> table.numberOfTovar).reduce(0, Integer::sum);
                sumOffset = reportPrepare.stream().map(table -> table.offset).reduce(0, Integer::sum);
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "OptionControlCheckTovarUp/executeOption/sumNumberOfTovar", "Exception e: " + e);
            }


            if (sumNumberOfTovar == sumOffset) {
                offset = 1; //если ошибка указана по всем строкам, которые ЕСТЬ на витрине и НЕ поднимались то счтиаем "подъем" выполненным
                note.append("Товар на витрину НЕ поднимался НО, по каждой позиции, указана причина (в поле 'Ошибка')");
            } else {
                note.append("Товар на витрину НЕ поднимался и по ").append(sumNumberOfTovar - sumOffset).append(" позиций НЕ указана причина (в поле 'Ошибка')");
            }
        }

        //5.2. проверим наличие фото поднятого товара
        int stackPhotoSize;
        if (stackPhoto != null) {
            stackPhotoSize = stackPhoto.size();
        } else {
            stackPhotoSize = 0;
        }

        //5.3. построчно анализируем
        if (sumUp > 0 && stackPhotoSize == 0) {
            tznNotes.append("Поднято ").append(sumUp).append(" единиц товара, но при этом нет ФТТ (Фото Тележки с Товаром) подтверждающего это.");
            tznErrorExist = 1;
        } else if (sumUp == 0 && stackPhotoSize > 0) {
            tznNotes.append("Есть ФотоТТ подтверждающего подъем товара но в дет.отчете не указано кол-во поднятого товара.");
            tznErrorExist = 1;
        } else if (sumUp == 0 && stackPhotoSize == 0 && sumNumberOfTovar == 0 && sumOffset == 1 && optionDB.getBlockPns().equals("1")) {
            tznNotes.append("Товар со склада на витрину не поднимался, НО по всем позициям указана ПРИЧИНА (в поле 'Ошибка').");
            tznErrorExist = 1;
        } else if (sumUp == 0 && stackPhotoSize == 0 && sumNumberOfTovar == 0) {
            tznNotes.append("Товар со склада на витрину не поднимался и по части позиций НЕ указана ПРИЧИНА (в поле 'Ошибка').");
            tznErrorExist = 1;
        } else if (sumUp == 0 && stackPhotoSize == 0) {
            tznNotes.append("Товар со склада на витрину не поднимался.");
            tznErrorExist = 1;
        } else {
            tznOffset = 1;
        }

        //5.4. исключения
        if (tznErrorExist == 1) {
            if (usersSDB.reportDate20 == null) {
                tznNotes.append(" Но мерч. еще не провел своего 20-го отчета.");
                tznErrorExist = 0;
            } else if (usersSDB.reportDate20.getTime() >= documentDate) {
                tznNotes.append(" Но мерч. еще не провел своего 20-го отчета.");
                tznErrorExist = 0;
            }
        }


        //6.0. подведем итог
        if (tznErrorExist > 0 && tznOffset > 0 && optionDB.getBlockPns().equals("1")) {
            stringBuilderMsg.append("Товар со склада на витрину не поднимался, НО по всем позициям указана ПРИЧИНА (в поле 'Ошибка').");
            signal = false;
        } else if (tznErrorExist == 0 && tznOffset > 0) {
            stringBuilderMsg.append("Поднято (со склада): ").append(sumUp).append(" единиц товара, и есть: ")
                    .append(stackPhotoSize).append(" ФТТ, подтверждающих это. Зачтено поднятие у ")
                    .append(tznOffset).append(" клиентов.");
            signal = false;
        } else if (tznErrorExist == 0) {
            stringBuilderMsg.append("Поднято (со склада): ").append(sumUp).append(" единиц товара, и есть: ")
                    .append(stackPhotoSize).append(" ФТТ, подтверждающих это. Зачтено поднятие у ")
                    .append(tznOffset).append(" клиентов.");
            signal = false;
        } else if (tznOffset > 0) {
            stringBuilderMsg.append("Выполнены работы по поднятию товаров (со склада) у ").append(tznOffset).append(" клиентов.");
            signal = false;
        } else {
            stringBuilderMsg.append("Товар зі складу не підіймався. Бонус не нарахован.");
            signal = true;
        }

        stringBuilderMsg.append("\n\n").append(tznNotes);


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
