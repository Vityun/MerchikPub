package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.util.Log;

import java.sql.Date;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ImagesTypeListRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;

public class OptionControlPhoto<T> extends OptionControl {
    public int OPTION_CONTROL_PHOTO_ID = 84932;
    public boolean signal = true;
    private StringBuilder optionResultStr = new StringBuilder();

    private WpDataDB wpDataDB;
    private UsersSDB usersSDB;
    private Long dad2;
    private AddressSDB addressSDB;
    private String clientName;

    public OptionControlPhoto(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
            Log.e("OptionControlPhoto", "Exception e: " + e);
            Globals.writeToMLOG("ERR", "OptionControlPhoto", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            this.addressSDB = SQL_DB.addressDao().getById(((WpDataDB) document).getAddr_id());
            this.wpDataDB = (WpDataDB) document;
            this.dad2 = wpDataDB.getCode_dad2();
            this.clientName = SQL_DB.customerDao().getById(((WpDataDB) document).getClient_id()).nm;

            usersSDB = SQL_DB.usersDao().getUserById(((WpDataDB) document).getUser_id());
            Date data = usersSDB.reportDate20; // Дата проведения 20й отчетности
        } else if (document instanceof TasksAndReclamationsSDB) {
            this.dad2 = ((TasksAndReclamationsSDB) document).codeDad2SrcDoc;
        }
    }


    private void executeOption() {

        String optionId;
        if (nnkMode.equals(Options.NNKMode.BLOCK)) {
            optionId = optionDB.getOptionId();
        } else {
            optionId = optionDB.getOptionControlId();
        }

        String typeNm = "";
        int m = Integer.parseInt(optionDB.getAmountMin());
//        if (m == 0) {
//            m = 1;  // Большая кака, но народ попросил так, надо будет у Петрова уточнить как именно оно должно работать ибо у него вроде как так же работатет 29.07.24
////            if (optionId.equals("164352")){
////                m = 1;
////            }else {
////                m = 3;
////            }
//        }
        int photoType = 0;

        long dad2ForGetStackPhotoDB = dad2;
        String[] codeIZAForGetStackPhotoDB = null;
        long dateFromForGetStackPhotoDB = 0;
        long dateToForGetStackPhotoDB = 0;

        switch (optionId) {
            case "151594":  // Контроль наличия фото витрины (до начала работ) !smarti!
                photoType = 14;
                m = m > 0 ? m : 3;
                break;

            case "164354":  // Фото Планограмми ТТ
                photoType = 5;
                m = m > 0 ? m : 1;
                break;

            case "164352":  // Контроль наявності світлини прикасової зони
                photoType = 45;
                typeNm = "світлина прикасової зони";
                m = m > 0 ? m : 1;
                break;

            case "134583": //!smarti!
            case "84932":
                photoType = 0;
                m = m > 0 ? m : 3;
                break;

            case "132971": {
                int quantityMax = Integer.parseInt(optionDB.getAmountMax());
                if (quantityMax > 0) {
                    dad2ForGetStackPhotoDB = 0;
                    long date = wpDataDB.getDt().getTime();
                    codeIZAForGetStackPhotoDB = new String[] {wpDataDB.getCode_iza(),
                            replaceSubstring(wpDataDB.getCode_iza(), wpDataDB.getIsp_fact(), 1, 5)};
                    dateFromForGetStackPhotoDB = Clock.getDatePeriodLong(date, -(quantityMax-1));
                    dateToForGetStackPhotoDB = Clock.getDatePeriodLong(date, 4);
                }
                photoType = 10; // Проверка наличия Фото тележка с товаром (тип 10)
                m = m > 0 ? m : 1;
                break;
            }

           case "141361": {
                int quantityMax = Integer.parseInt(optionDB.getAmountMax());
                if (quantityMax > 0) {
                    dad2ForGetStackPhotoDB = 0;
                    long date = wpDataDB.getDt().getTime();
                    codeIZAForGetStackPhotoDB = new String[] {wpDataDB.getCode_iza(),
                            replaceSubstring(wpDataDB.getCode_iza(), wpDataDB.getIsp_fact(), 1, 5)};
                    dateFromForGetStackPhotoDB = Clock.getDatePeriodLong(date, -(quantityMax-1));
                    dateToForGetStackPhotoDB = Clock.getDatePeriodLong(date, 4);
                }

                photoType = 31; // Фото товара на скалде
                m = m > 0 ? m : 1;
                break;
            }

            case "158606":  // Корпоративный блок
                photoType = 40;
                m = m > 0 ? m : 3;
                break;

            case "158607":  // Наполненность полки
                photoType = 41;
                m = m > 0 ? m : 3;
                break;

            case "158608":  // Приближенная фото
                photoType = 39;
                m = m > 0 ? m : 3;
                break;

            case "158609":  // Дополнительное место продаж
                photoType = 42;
                m = m > 0 ? m : 3;
                break;

            case "159726":  // Фото торговой точки
            case "159725":  // Кнопка "Фото Торговой Точки (ФТТ)" !smarti!
                photoType = 37;
                m = m > 0 ? m : 3;
                break;

            case "165482":  // Контроль наличия Фото - скан посещения в приложении Эффи
                photoType = 46; // 46 - фото скан посещения в приложении эффи
                m = m > 0 ? m : 1;
                break;

        }


        int adress = ((WpDataDB) document).getAddr_id();
//        получаем данные из таблицы фото
        RealmResults<StackPhotoDB> stackPhotoDB =
                dad2ForGetStackPhotoDB > 0 ?
                        StackPhotoRealm.getPhotosByDAD2(dad2, photoType) :
                        StackPhotoRealm.getPhotosByRangeDt(dateFromForGetStackPhotoDB / 1000, dateToForGetStackPhotoDB / 1000, codeIZAForGetStackPhotoDB, adress, photoType);

        String photoTypeName = ImagesTypeListRealm.getByID(photoType).getNm();

//        подводим итог
        if (stackPhotoDB != null && stackPhotoDB.size() < m) {
            ImagesTypeListDB item = ImagesTypeListRealm.getByID(photoType);
            stringBuilderMsg.append("Вы должны сделать: ").append(m).append(" фото с типом: ").append(item != null ? item.getNm() : typeNm).append(", а сделали: ").append(stackPhotoDB.size()).append(" - доделайте фотографии.");
            signal = true;
//            unlockCodeResultListener.onUnlockCodeFailure();
        } else {
            stringBuilderMsg.append("Жалоб по фыполнению фото нет. Сделано: ").append(stackPhotoDB.size()).append(" фото.");
            signal = false;
//            unlockCodeResultListener.onUnlockCodeSuccess();
        }

        // Исключения
        if (optionId.equals("141361") || optionId.equals("132971")) {
            if (addressSDB.tpId == 383) {   // Для АШАН-ов(8196 - у петрова такое тут, странно) ФЗ ФТС НЕ проверяем
                signal = false;
                stringBuilderMsg.append(", але для Ашанів, наявність ФЗ ФТС не перевіряємо.");
            } else if (addressSDB.tpId == 434) {   // Для АТБ ФЗ ФТС НЕ проверяем
                signal = false;
                stringBuilderMsg.append(", але для АТБ, наявність ФЗ ФТС не перевіряємо.");
            } else if (addressSDB.tpId == 6698) {   // Для КОЛО ФЗ ФТС НЕ проверяем
                signal = false;
                stringBuilderMsg.append(", але для КОЛО, наявність ФЗ ФТС не перевіряємо.");
            } else if (addressSDB.tpId == 7135) {   // Для БОКС-маркет ФЗ ФТС НЕ проверяем
                signal = false;
                stringBuilderMsg.append(", але для БОКС-маркет, наявність ФЗ ФТС не перевіряємо.");
            } else if (Integer.parseInt(wpDataDB.getClient_id()) == 91478 || //91478-Уяви
                    Integer.parseInt(wpDataDB.getClient_id()) == 10822 ||   //10822-Эгмонт
                    Integer.parseInt(wpDataDB.getClient_id()) == 70484 ||   //70484-Кідді Ко
                    Integer.parseInt(wpDataDB.getClient_id()) == 14365 ||   //14365-флеш
                    Integer.parseInt(wpDataDB.getClient_id()) == 10349) {   //10349-Гифт-К
                signal = false;
                stringBuilderMsg.append("Обнаружено (")
                        .append(stackPhotoDB.size())
                        .append(")")
                        .append(photoTypeName)
                        .append(" но, для ")
                        .append(clientName)
                        .append(" сделано исключение.");
            }
        }


        //7.0. сохраним сигнал
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                if (signal) {
                    optionDB.setIsSignal("1");
//                    setIsBlockOption(signal);
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
        checkUnlockCode(optionDB);
    }

    // Метод для замены символов
    public static String replaceSubstring(String original, String replacement, int start, int end) {
        // Проверяем, что строки достаточно длинные для операции
        if (original == null || replacement == null || start < 0 || end >= original.length() || end - start + 1 != replacement.length()) {
            throw new IllegalArgumentException("Некорректные данные");
        }

        // Разделяем исходную строку на части
        String part1 = original.substring(0, start);  // Часть до замены
        String part2 = original.substring(end + 1);   // Часть после замены

        // Возвращаем новую строку, объединяя части и замену
        return part1 + replacement + part2;
    }
}
