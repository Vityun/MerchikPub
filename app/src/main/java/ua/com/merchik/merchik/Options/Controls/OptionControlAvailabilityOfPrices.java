package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;
import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.PhotoDescriptionText;
import ua.com.merchik.merchik.data.RealmModels.*;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.PRICE;
import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.*;


/**
 * 24.09.2025
 * <p>
 * ID: 579
 * Опция контроля наличия цен у Товаров. (Переписано для контроля ЦЕН, вместо Акций)
 */
public class OptionControlAvailabilityOfPrices<T> extends OptionControl {

    public int OPTION_CONTROL_AVAILABILITY_OF_PRICES_ID = 579;

    public boolean signal = true;


    private String documentDate, clientId, optionId;
    private int addressId, userId;
    private long dad2;

    private Integer colMin = 1;

    public OptionControlAvailabilityOfPrices(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        try {
            this.context = context;
            this.document = document;
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;
            }
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            this.unlockCodeResultListener = unlockCodeResultListener;

            getDocumentVar();
            executeOption();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityOfPrices", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wpDataDB = (WpDataDB) document;

            documentDate = Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000); //+TODO CHANGE DATE

            clientId = wpDataDB.getClient_id();
            addressId = wpDataDB.getAddr_id();
            userId = wpDataDB.getUser_id();
            dad2 = wpDataDB.getCode_dad2();
            try {
                colMin = Integer.valueOf(optionDB.getAmountMin());
            } catch (Exception e) {
                colMin = 1;
            }
        }
    }

    private void executeOption() {
//        int signalInt = 0;         // 1 - есть замечания, 2 - ок / нет замечаний
        int err = 0;

        // Получение RP по данному документу.
        List<ReportPrepareDB> reportPrepare = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(dad2));

        // Получение Доп. Требований с дополнительными фильтрами.
        List<AdditionalRequirementsDB> additionalRequirements;
        String[] tovIds;
        if (optionDB.getOptionId().equals("579") || optionDB.getOptionControlId().equals("579")) {
            additionalRequirements = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(document, true, OPTION_CONTROL_AVAILABILITY_OF_PRICES_ID, null, wpDataDB.getDt(), wpDataDB.getDt(), null, null, null, null);
            tovIds = new String[additionalRequirements.size()];

            for (int i = 0; i < additionalRequirements.size(); i++) {
                tovIds[i] = additionalRequirements.get(i).getTovarId();
            }
            Arrays.sort(tovIds);
        } else {
            tovIds = new String[0];
        }


        SpannableStringBuilder errMsg = new SpannableStringBuilder();
        errMsg.append("Для следующих товара(ов) с ОСВ (Особым Вниманием) вы должны обязательно указать ЦЕНУ:").append("\n\n");

        // 5.0
        int totalOSV = 0;
        int foundWithPrice = 0;
        int missingPriceCount = 0;

        for (ReportPrepareDB item : reportPrepare) {
            boolean isOSV = Arrays.asList(tovIds).contains(item.getTovarId());

            TovarDB tov = TovarRealm.getById(item.getTovarId());
            if (tov != null) {
                String msg = String.format("(%s) %s (%s)", item.getTovarId(), tov.getNm(), tov.getWeight());

                // Если товар на витрине (face > 0) — нас он интересует
                boolean onFace = false;
                try {
                    // face может быть строкой, проверяем безопасно
                    String face = item.getFace();
                    if (face != null && !face.equals("")) {
                        try {
                            onFace = Integer.parseInt(face) > 0;
                        } catch (Exception e) {
                            // если не число, пробуем проверить не пустую строку
                            onFace = true;
                        }
                    }
                } catch (Exception ignored) {
                }

                if (!onFace) {
                    // пропускаем товары, которых нет на витрине
                    continue;
                }

                if (isOSV) {
                    totalOSV++;
                }

                // Проверяем наличие цены
                boolean hasPrice = false;
                try {
                    String price = item.getPrice();
                    hasPrice = price != null && !price.trim().equals("") && !price.trim().equals("0");
                } catch (Exception ignored) {
                }

                if (isOSV && !hasPrice) {
                    // Для товара с ОСВ и присутствующего на витрине, цена не указана -> ошибка
                    err++;
                    missingPriceCount++;
                    errMsg.append(createLinkedString(msg, item, tov)).append("\n");
                } else if (!isOSV) {
                    // Если список ОСВ пуст (в 1С: проверка по всей витрине в зависимости от colMin)
                    // но у нас товар имеет цену -> считаем найденным
                    if (hasPrice) {
                        foundWithPrice++;
                        item.find = 1;
                    } else {
                        // товар без ОСВ и без цены — если colMin == 0 (требуется у всех) то это нарушение,
                        // но эту логику учтём ниже при суммарных подсчётах
                        missingPriceCount++;
                        errMsg.append(createLinkedString(msg, item, tov)).append("\n");
                    }
                } else {
                    // товар с ОСВ и есть цена
                    if (hasPrice) {
                        foundWithPrice++;
                        item.find = 1;
                    }
                }
            }
        }

        // 5.1. Если менеджер указал КолМин > числа записей — используем фактическое количество
        colMin = reportPrepare.size() < colMin ? reportPrepare.size() : colMin;

        // Формирование сообщения общего вида
        if (missingPriceCount > 0) {
            spannableStringBuilder.append(errMsg);
        }

        // 6.0 Логика коротких сообщений (приближённо соответствует 1С)
        int totalRelevant = 0; // количество товаров, присутствующих на витрине (face>0)
        for (ReportPrepareDB rp : reportPrepare) {
            try {
                String face = rp.getFace();
                if (face != null && !face.equals("")) {
                    try {
                        if (Integer.parseInt(face) > 0) totalRelevant++;
                    } catch (Exception e) {
                        totalRelevant++;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // вычислим сколько товаров вообще отмечено как "нашли" (с ценой)
        int found = foundWithPrice;

        if (reportPrepare.size() == 0 || totalRelevant == 0) {
            spannableStringBuilder.append("Товаров, по которым надо проверять факт наличия ЦЕН, не обнаружено.");
            signal = false; // нет товаров — замечаний нет
        } /* скорее всего придется поменять местами с нижним блоком totalOSV == 0, так логично для меня, но сделал как в 1с */ else if (missingPriceCount > 0 && (optionDB.getOptionId().equals("579") || optionDB.getOptionControlId().equals("579"))) {
//            spannableStringBuilder.append("Не предоставлена информация о ЦЕНАХ по товару (" + missingPriceCount + " шт.) (в т.ч. с ОСВ (Особым Вниманием)). См. таблицу.");
            signal = true;
        } else if (totalOSV == 0 && (optionDB.getOptionId().equals("579") || optionDB.getOptionControlId().equals("579"))) {
            spannableStringBuilder.append("Для данной ТТ, на текущий момент, нет товаров с ОСВ (Особым Вниманием). Контролировать нечего. Замечаний нет.");
            signal = false;
        } else if (found == 0) {
            spannableStringBuilder.append("Ни у одного товара не указана Цена.");
            signal = true;
        } else if (found < colMin) {
            spannableStringBuilder.append("Вы указали данные о ценах у ").append("" + found).append(" товаров, что меньше минимально допустимого ").append("" + colMin);
            signal = true;
        } else {
            spannableStringBuilder.append("Замечаний по предоставлению информации о Ценах по товарам (в т.ч. с ОСВ (Особым Вниманием)) нет.");
            signal = false;
        }


//        // 7.0 сохраним сигнал (если нужно)
//        if (optionDB.getIsSignal().equals("0")) {
//            saveOption(String.valueOf(signalInt));
//        }

        // 8.0 Блокировка проведения
        setIsBlockOption(signal);

        // Сохранение состояния опции в БД
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
                spannableStringBuilder.append("\n\n").append("Документ проведен не будет!");
            } else {
                spannableStringBuilder.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете указывать цены на товары.");
            }
        }

        checkUnlockCode(optionDB);
        // Если есть какой-то сигнал - нужно вывести сообщение

    }

    private SpannableString createLinkedString(String msg, ReportPrepareDB reportPrepareDB, TovarDB tov) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Toast.makeText(textView.getContext(), "id: " + reportPrepareDB.getTovarId(), Toast.LENGTH_LONG).show();

                DialogData dialog = new DialogData(textView.getContext());
                dialog.setTitle("");
                dialog.setText("");
                dialog.setClose(dialog::dismiss);

                dialog.setImage(true, getPhotoFromDB(tov));
                dialog.setAdditionalText(setPhotoInfo(TPL, tov, "", ""));

                // Заполняем операции для цен: operationType будет Number для поля price
                dialog.setOperationSpinnerData(setMapData(Globals.OptionControlName.ERROR_ID));
                dialog.setOperationSpinner2Data(setMapData(Globals.OptionControlName.AKCIYA)); // можно оставить, не используется для цены
                dialog.setOperationTextData(reportPrepareDB.getAkciyaId()); // старые данные не важны, покажем цену ниже
                dialog.setOperationTextData2(reportPrepareDB.getAkciya());

                // Установим текущее значение цены
                dialog.setOperationTextData(reportPrepareDB.getPrice());

                dialog.setOperation(operationType(TPL), getCurrentData(TPL, reportPrepareDB.getCodeDad2(), reportPrepareDB.getTovarId()), setMapData(TPL.getOptionControlName()), () -> {
                    if (dialog.getOperationResult() != null) {
                        operetionSaveRPToDB(TPL, reportPrepareDB, dialog.getOperationResult(), dialog.getOperationResult2(), null, dialog.context);
                        Toast.makeText(dialog.context, "Внесено: " + dialog.getOperationResult(), Toast.LENGTH_LONG).show();
                    }
                });

                dialog.show();
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

//    private void saveOption(String signal) {
//        RealmManager.INSTANCE.executeTransaction(realm -> {
//            if (optionDB != null) {
//                optionDB.setIsSignal(signal);
//                realm.insertOrUpdate(optionDB);
//            }
//        });
//    }


    // Основной TPL теперь настроен на PRICE
    TovarOptions TPL = new TovarOptions(PRICE, "P", "Цена товара", "price", "main", 579);

    // Нужно для заполенния ТПЛ-ов
    private Map<Integer, String> setMapData(Globals.OptionControlName optionControlName) {
        Map<Integer, String> map = new HashMap<>();
        switch (optionControlName) {
            case ERROR_ID:
                RealmResults<ErrorDB> errorDbList = RealmManager.getAllErrorDb();
                for (int i = 0; i < errorDbList.size(); i++) {
                    if (errorDbList.get(i).getNm() != null && !errorDbList.get(i).getNm().equals("")) {
                        map.put(Integer.valueOf(errorDbList.get(i).getID()), errorDbList.get(i).getNm());
                    }
                }
                return map;

            case AKCIYA_ID:
                RealmResults<PromoDB> promoDbList = RealmManager.getAllPromoDb();
                for (int i = 0; i < promoDbList.size(); i++) {
                    if (promoDbList.get(i).getNm() != null && !promoDbList.get(i).getNm().equals("")) {
                        map.put(Integer.valueOf(promoDbList.get(i).getID()), promoDbList.get(i).getNm());
                    }
                }
                return map;

            case AKCIYA:
                map.put(2, "Акция отсутствует");
                map.put(1, "Есть акция");
                return map;

            case PRICE:
                // Для цены нам не нужно заполнять спиннер, но оставим пустую мапу
                return map;

            default:
                return null;
        }
    }

    private File getPhotoFromDB(TovarDB tovar) {
        int id = Integer.parseInt(tovar.getiD());
        StackPhotoDB stackPhotoDB = RealmManager.getTovarPhotoByIdAndType(id, tovar.photoId, 18, false);
        if (stackPhotoDB != null) {
            if (stackPhotoDB.getObject_id() == id) {
                if (stackPhotoDB.getPhoto_num() != null && !stackPhotoDB.getPhoto_num().equals("")) {
                    File file = new File(stackPhotoDB.getPhoto_num());
                    return file;
                }
            }
        }
        return null;
    }

    private PhotoDescriptionText setPhotoInfo(TovarOptions tpl, TovarDB tovar, String finalBalanceData1, String finalBalanceDate1) {
        PhotoDescriptionText res = new PhotoDescriptionText();

        try {
            String weightString = String.format("%s, %s", tovar.getWeight(), tovar.getBarcode()); // составление строк веса и штрихкода для того что б выводить в одно поле

            String title = tpl.getOptionLong();

            if (DetailedReportActivity.rpThemeId == 1178) {
                if (tpl.getOptionId().contains(578) || tpl.getOptionId().contains(1465)) {
                    title = "Кол-во выкуп. товара";
                }

                if (tpl.getOptionId().contains(579)) {
                    title = "Цена выкуп. товара";
                }
            }

            if (DetailedReportActivity.rpThemeId == 33) {
                if (tpl.getOptionId().contains(587)) {
                    title = "Кол-во заказанного товара";
                }
            }

            res.row1Text = title;
            res.row1TextValue = "";
            res.row2TextValue = tovar.getNm();
            res.row3TextValue = weightString;

            res.row4TextValue = RealmManager.getNmById(tovar.getManufacturerId()) != null ? RealmManager.getNmById(tovar.getManufacturerId()).getNm() : "";

            res.row5Text = "Ост.:";
            res.row5TextValue = finalBalanceData1 + " шт на " + finalBalanceDate1;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.setPhotoInfo", "Exception e: " + e);
        }
        return res;
    }

    private DialogData.Operations operationType(TovarOptions tpl) {
        switch (tpl.getOrderField()) {
            case ("price"):
            case ("face"):
            case ("expire_left"):
            case ("amount"):
            case ("oborotved_num"):
            case ("up"):
                return Number;

            case ("dt_expire"):
                return Date;

            case ("akciya_id"):
//                case ("akciya"):
                return DoubleSpinner;

            case ("error_id"):
                return EditTextAndSpinner;

            case ("notes"):
                return Text;

            default:
                return Text;
        }
    }

    private String getCurrentData(TovarOptions tpl, String cd, String id) {
        ReportPrepareDB table = RealmManager.getTovarReportPrepare(cd, id);
        switch (tpl.getOptionControlName()) {
            case PRICE:
                return table.getPrice();

            case FACE:
                return table.getFace();

            case EXPIRE_LEFT:
                return table.getExpireLeft();

            case AMOUNT:
                return String.valueOf(table.getAmount());

            case OBOROTVED_NUM:
                return table.getOborotvedNum();

            case UP:
                return table.getUp();

            case DT_EXPIRE:
                return table.getDtExpire();

            case ERROR_ID:
                return table.getErrorId();

            case AKCIYA_ID:
                return table.getAkciyaId();

            case AKCIYA:
                return table.getAkciya();

            case NOTES:
                return table.getNotes();

        }

        return null;
    }

    private void operetionSaveRPToDB(TovarOptions tpl, ReportPrepareDB rp, String data, String data2, TovarDB tovarDB, Context context) {
        if (data == null || data.equals("")) {
            Toast.makeText(context, "Для сохранения - внесите данные", Toast.LENGTH_SHORT).show();
            return;
        }

        // Сохраняем цену
        if (tpl.getOptionControlName() == PRICE) {
            INSTANCE.executeTransaction(realm -> {
                rp.setPrice(data);
                rp.setUploadStatus(1);
                rp.setDtChange(System.currentTimeMillis() / 1000);
                RealmManager.setReportPrepareRow(rp);
            });
            return;
        }

        // Старый кейс для акции (оставлен для совместимости)
        if (tpl.getOptionControlName() == AKCIYA_ID) {
            INSTANCE.executeTransaction(realm -> {
                rp.setAkciyaId(data);
                rp.setAkciya(data2);
                rp.setUploadStatus(1);
                rp.setDtChange(System.currentTimeMillis() / 1000);
                RealmManager.setReportPrepareRow(rp);
            });
        }
    }

}
