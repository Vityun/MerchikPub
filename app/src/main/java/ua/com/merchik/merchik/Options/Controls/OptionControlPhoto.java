package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import java.util.Date;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ImagesTypeListRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

public class OptionControlPhoto<T> extends OptionControl {
    public int OPTION_CONTROL_PHOTO_ID = 84932;
    public boolean signal = true;
    private StringBuilder optionResultStr = new StringBuilder();

    private WpDataDB wpDataDB;
    private UsersSDB usersSDB;
    private Long dad2;
    private AddressSDB addressSDB;
    private String clientName;
    private String clientId;
    private CustomerSDB client;

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
            this.client = SQL_DB.customerDao().getById(((WpDataDB) document).getClient_id());
            this.clientName = client.nm;
            this.clientId = client.id;

            usersSDB = SQL_DB.usersDao().getUserById(((WpDataDB) document).getUser_id());
//            Date data = DetailedReportActivity.usersSDB.reportDate05; // Дата проведения 5й отчетности
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
        Date data05report = usersSDB.reportDate05; // Дата проведения 20й отчетности
        Date documentDate = wpDataDB.getDt();

        switch (optionId) {
            case "151594":  // Контроль наличия фото витрины (до начала работ) !smarti!
                photoType = 14;
                m = m > 0 ? m : 3;
                break;

            case "164354":  // Фото Планограмми ТТ
            {
                int quantityMax = Integer.parseInt(optionDB.getAmountMax());
                if (quantityMax > 0) {
                    dad2ForGetStackPhotoDB = 0;
                    long date = wpDataDB.getDt().getTime();
                    codeIZAForGetStackPhotoDB = new String[]{wpDataDB.getCode_iza(),
                            replaceSubstring(wpDataDB.getCode_iza(), wpDataDB.getIsp(), 1, 5),
                            replaceSubstring(wpDataDB.getCode_iza(), wpDataDB.getIsp_fact(), 1, 5),
                            replaceSubstring(wpDataDB.getCode_iza(), "03693", 1, 5)};
                    dateFromForGetStackPhotoDB = Clock.getDatePeriodLong(date, -(quantityMax - 1));
                    dateToForGetStackPhotoDB = Clock.getDatePeriodLong(date, 4);
                }

                photoType = 5;
                // c 21.02.25 поменял на 0, требование Петрова
                m = m > 0 ? m : 0;
                break;
            }

            case "164352":  // Контроль наявності світлини прикасової зони
                photoType = 45;
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
                    codeIZAForGetStackPhotoDB = new String[]{wpDataDB.getCode_iza(),
                            replaceSubstring(wpDataDB.getCode_iza(), wpDataDB.getIsp(), 1, 5),
                            replaceSubstring(wpDataDB.getCode_iza(), wpDataDB.getIsp_fact(), 1, 5),
                            replaceSubstring(wpDataDB.getCode_iza(), "03693", 1, 5)};
                    dateFromForGetStackPhotoDB = Clock.getDatePeriodLong(date, -(quantityMax - 1));
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
                    codeIZAForGetStackPhotoDB = new String[]{wpDataDB.getCode_iza(),
                            replaceSubstring(wpDataDB.getCode_iza(), wpDataDB.getIsp(), 1, 5),
                            replaceSubstring(wpDataDB.getCode_iza(), wpDataDB.getIsp_fact(), 1, 5),
                            replaceSubstring(wpDataDB.getCode_iza(), "03693", 1, 5)};
                    dateFromForGetStackPhotoDB = Clock.getDatePeriodLong(date, -(quantityMax - 1));
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
                m = m > 0 ? m : 3; // 20.01 изменил как в 1С (3.1)
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

//        List<StackPhotoDB> stackPhotoDBList = RealmManager.INSTANCE.copyFromRealm(stackPhotoDB);
//        подводим итог
        if (stackPhotoDB.size() < m) { // главнй итог
            ImagesTypeListDB item = ImagesTypeListRealm.getByID(photoType);
            spannableStringBuilder.append("Ви повинні зробити: ")
                    .append(String.valueOf(m)).append(" фото з типом: ")
                    .append(item != null ? item.getNm() : photoTypeName)
                    .append(", а зробили: ")
                    .append(String.valueOf(stackPhotoDB.size()))
                    .append(" - доробiть фотографії.");
            signal = true;
//            unlockCodeResultListener.onUnlockCodeFailure();
        } else {
            spannableStringBuilder.append("Скарг щодо виконання фото немає. Усього зроблено: ")
                    .append(String.valueOf(stackPhotoDB.size())).append(" фото.");
            signal = false;
//            unlockCodeResultListener.onUnlockCodeSuccess();
        }

        // Исключения
        // 3.1
        if (signal && optionId.equals("158609")) {
//        List<String> promotionalTov = new ArrayList<>();
            List<AdditionalRequirementsDB> additionalRequirementsDBList = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(document, true, Integer.parseInt(optionId), null, wpDataDB.getDt(), wpDataDB.getDt(), null, null, null, null);
//        List<AdditionalRequirementsDB> additionalRequirementsDBList = AdditionalRequirementsRealm.getAdditionalRequirements(wpDataDB.getClient_id(), Integer.parseInt(optionId));
            if (!additionalRequirementsDBList.isEmpty()) {
                // Инициализируем флаги для поиска
                boolean hasAddrOrGrp = false;

                for (AdditionalRequirementsDB item : additionalRequirementsDBList) {
//                promotionalTov.add(item.getTovarId());
                    if (Integer.parseInt(item.getAddrId()) == (wpDataDB.getAddr_id())
                    ) {
                        hasAddrOrGrp = true;
                        break;
                    } else if (
                            Integer.parseInt(item.getAddrId()) == 0 &&
                                    Integer.parseInt(item.getGrpId()) == (addressSDB.tpId)
                    ) {
                        hasAddrOrGrp = true;
                        break;
                    }
                }
                // Логика обработки
                if (hasAddrOrGrp) {
                    // ОСВ найдены для этой Сети/Адреса
                    // Выполняем дальнейшую проверку
                    Log.e("GOOD", "++");
                } else {
                    signal = false;
                    spannableStringBuilder.append("\nВідповідно до ДВ ").append(photoTypeName)
                            .append(" у поточній Адреса/Мережа виготовлення НЕ ОБОВ'ЯЗКОВО. Перевірка не проводилась");
                }
            }
        }

        // 3.2
        // для 141361 от 27.03.2025
        if (optionId.equals("141361")) {
            RealmResults<StackPhotoDB> stackPhotoDB141361 = StackPhotoRealm.getPhotosByDAD2(dad2, 31);
            long count = stackPhotoDB141361.where()
                    .equalTo("example_id", "78")
                    .count();
            if (count > 0) {
                m = 2;
                int photoWithComment = 0;
                spannableStringBuilder.clear();
                String baseEmptyComment = "У свiтлин: ";
                List<StackPhotoDB> stackPhotoDBList = RealmManager.INSTANCE.copyFromRealm(stackPhotoDB141361);
                for (StackPhotoDB photo : stackPhotoDBList) {
                    if ("78".equals(photo.getExample_id())) {
                        String comment = photo.getComment();
                        if (comment != null && comment.length() > 10) {
                            photoWithComment++;
                        } else {
                            baseEmptyComment = baseEmptyComment + photo.getPhotoServerId() + ", ";
                        }
                    }
                }
                if (photoWithComment < m) {
                    if (count < 2) {
                        signal = true;
                        spannableStringBuilder.append("Для випадку, коли на складі ТТ немає товару, кiлькiсть світлин за зразком 78 має бути не менше ніж ")
                                .append(String.valueOf(m))
                                .append(", а зроблено: ")
                                .append(String.valueOf(count));
                    } else
//                    if (baseEmptyComment.length() > 20)
                    {
                        baseEmptyComment = baseEmptyComment.replaceFirst(",(?!.*?,)", "") + "немає коментаря.\n";
                        signal = true;
                        spannableStringBuilder.append(baseEmptyComment)
                                .append("Для випадку, коли на складі ТТ немає товару, для кожної світлини, виготовленої за зразком 78, повинен бути доданий коментар довжиною більше 10 символів");
                    }
                } else {
                    spannableStringBuilder.append("Скарг щодо виконання фото немає. Зроблено: ").append(String.valueOf(count)).append(" фото.");
                    signal = false;
                }
            }
        }

        if (!signal) {
            List<AdditionalRequirementsDB> additionalRequirementsDBList = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(document, true, Integer.parseInt(optionId), null, wpDataDB.getDt(), wpDataDB.getDt(), null, null, null, null);
//            AdditionalRequirementsDB additional = additionalRequirementsDBList.stream().findFirst().get();
            if (!stackPhotoDB.isEmpty()) {
                for (StackPhotoDB stackPhoto : stackPhotoDB) {
                    additionalRequirementsDBList.removeIf(req -> req.getTovarId().equals(stackPhoto.getTovar_id()));
//                    String photoId = stackPhoto.getTovar_id();
//                    String adId = additional.getTovarId();
//                    Log.e("!!!!!!", "photoId: " + photoId + " | adId: " + adId + " = " + (photoId.equals(adId)));
//                    if (stackPhoto.getTovar_id().equals(additional.getTovarId()))
//                        Log.e("!", "_+");
//                    additionalRequirementsDBList.remove("");
//                    promotionalTov.remove(stackPhoto.tovar_id);
                }
            }
            if (!additionalRequirementsDBList.isEmpty()) {
                List<ReportPrepareDB> reportPrepare = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(dad2));
                signal = true;
//                spannableStringBuilder.clear();
                String fullText = spannableStringBuilder.toString();
                String startPhrase = "Відповідно до ДВ";
                String endPhrase = "Перевірка не проводилась";

                int startIndex = fullText.indexOf(startPhrase);
                int endIndex = fullText.indexOf(endPhrase) + endPhrase.length();
                String text = "\nОднак, Вам треба зробити свiтлини " + photoTypeName + " товарiв:\n";

                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    // Заменяем найденный диапазон на новый SpannableString
                    spannableStringBuilder.replace(startIndex, endIndex, text);
                } else {
                    spannableStringBuilder.append(text);
                }

                for (AdditionalRequirementsDB item : additionalRequirementsDBList) {
                    TovarDB tovar = TovarRealm.getById(item.getTovarId());
                    ReportPrepareDB prepareDB = reportPrepare.stream()
                            .filter(reportPrepareDB -> item.getTovarId().equals(reportPrepareDB.getTovarId()))
                            .findFirst().get();
                    String code = tovar.getiD();
                    String result = "(" + code + ") " + tovar.getNm();
                    spannableStringBuilder.append(createLinkedString(result, prepareDB, photoType));

                }
            }
        }


        if (optionId.equals("141361") || optionId.equals("132971")) {
            if (addressSDB.tpId == 383) {   // Для АШАН-ов(8196 - у петрова такое тут, странно) ФЗ ФТС НЕ проверяем
                signal = false;
                spannableStringBuilder.append(", але для Ашанів, наявність ФЗ ФТС не перевіряємо.");
            } else if (addressSDB.tpId == 434) {   // Для АТБ ФЗ ФТС НЕ проверяем
                signal = false;
                spannableStringBuilder.append(", але для АТБ, наявність ФЗ ФТС не перевіряємо.");
            } else if (addressSDB.tpId == 6698) {   // Для КОЛО ФЗ ФТС НЕ проверяем
                signal = false;
                spannableStringBuilder.append(", але для КОЛО, наявність ФЗ ФТС не перевіряємо.");
            } else if (addressSDB.tpId == 7135) {   // Для БОКС-маркет ФЗ ФТС НЕ проверяем
                signal = false;
                spannableStringBuilder.append(", але для БОКС-маркет, наявність ФЗ ФТС не перевіряємо.");
            } else if (Integer.parseInt(wpDataDB.getClient_id()) == 91478 || //91478-Уяви
                    Integer.parseInt(wpDataDB.getClient_id()) == 10822 ||   //10822-Эгмонт
                    Integer.parseInt(wpDataDB.getClient_id()) == 70484 ||   //70484-Кідді Ко
                    Integer.parseInt(wpDataDB.getClient_id()) == 14365 ||   //14365-флеш
                    Integer.parseInt(wpDataDB.getClient_id()) == 10349) {   //10349-Гифт-К
                signal = false;
                spannableStringBuilder.append("Обнаружено (")
                        .append(String.valueOf(stackPhotoDB.size()))
                        .append(")")
                        .append(photoTypeName)
                        .append(" но, для ")
                        .append(clientName)
                        .append(" сделано исключение.");
            } else if (data05report == null || data05report.after(documentDate)) {
                signal = false;
                spannableStringBuilder.append(", але виконавець ще не провiв свого 5го звiту, наявність ФЗ ФТС не перевіряємо.");
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
                spannableStringBuilder.append("\n\n").append("Документ проведено не буде!");
            } else {
                spannableStringBuilder.append("\n\n").append("Вы можете отримати Преміальні БІЛЬШЕ, якщо будете збільшувати кількість фейсів товарів замовника на полиці.");
            }
        }
        checkUnlockCode(optionDB);
    }

    // Метод для замены символов
    public static String replaceSubstring(String original, String replacement, int start, int end) {
        // Проверяем, что строки достаточно длинные для операции
        if (original == null ||
                replacement == null ||
                start < 0 ||
                end >= original.length()
        ) {
            throw new IllegalArgumentException("Некорректные данные");
        }
        // Вычисляем сколько символов нужно заменить
        int replaceLength = end - start + 1;
        // Если replacement короче, чем нужно - дополняем нулями слева
        String adjustedReplacement = replacement;
        if (replacement.length() < replaceLength) {
            int zerosToAdd = replaceLength - replacement.length();
            adjustedReplacement = "0".repeat(zerosToAdd) + replacement;
        }
        // Разделяем исходную строку на части
        String part1 = original.substring(0, start);  // Часть до замены
        String part2 = original.substring(end + 1);   // Часть после замены

        // Возвращаем новую строку, объединяя части и замену
        return part1 + adjustedReplacement + part2;
    }

    private SpannableString createLinkedString(String msg, ReportPrepareDB rp, int photoType) {
        SpannableString res = new SpannableString(msg);

        try {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    new TovarRequisites(TovarRealm.getById(rp.tovarId), rp, photoType).createDialog(context, WpDataRealm.getWpDataRowByDad2Id(Long.parseLong(rp.codeDad2)),
                            optionDB, () -> {
                            }).show();
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
//                    ds.setColor(Color.GREEN);
                }
            };
            int count = msg.length();
            res.setSpan(clickableSpan, 0, count, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityControlPhotoRemainingGoods/executeOption/createLinkedString/Exception", "Exception e: " + e);
        }
        return res;
    }

}
