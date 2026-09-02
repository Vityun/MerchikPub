package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.PRICE;
import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Date;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.DoubleSpinner;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.EditTextAndSpinner;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Number;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Text;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.PhotoDescriptionText;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.ErrorDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

/**
 * 24.09.2025
 * <p>
 * ID: 579
 * Опция контроля наличия цен у Товаров. (Переписано для контроля ЦЕН, вместо Акций)
 */
public class OptionControlAvailabilityOfPrices<T> extends OptionControl {

    public int OPTION_CONTROL_AVAILABILITY_OF_PRICES_ID = 579;
    public int OPTION_CONTROL_AVAILABILITY_OF_PRICES_OSV_ID = 174974;


    public boolean signal = true;

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
        boolean isPriceOption = isOptionOrControl(OPTION_CONTROL_AVAILABILITY_OF_PRICES_ID);
        boolean isOsvOnlyOption = isOptionOrControl(OPTION_CONTROL_AVAILABILITY_OF_PRICES_OSV_ID);

        // Получение Доп. Требований с дополнительными фильтрами.
        List<AdditionalRequirementsDB> additionalRequirements = Collections.emptyList();
        int requirementsOptionId = isOsvOnlyOption
                ? OPTION_CONTROL_AVAILABILITY_OF_PRICES_OSV_ID
                : OPTION_CONTROL_AVAILABILITY_OF_PRICES_ID;
        String[] tovIds;
        if (isPriceOption || isOsvOnlyOption) {
            additionalRequirements = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(document, true, requirementsOptionId, null, wpDataDB.getDt(), wpDataDB.getDt(), null, null, null, null);
            List<String> tovIdList = new ArrayList<>();
            for (int i = 0; i < additionalRequirements.size(); i++) {
                String tovId = additionalRequirements.get(i).getTovarId();
                if (tovId != null) {
                    tovId = tovId.trim();
                }
                if (tovId != null && !tovId.isEmpty() && !tovId.equals("0") && !tovIdList.contains(tovId)) {
                    tovIdList.add(tovId);
                }
            }
            tovIds = tovIdList.toArray(new String[0]);
            Arrays.sort(tovIds);
        } else {
            tovIds = new String[0];
        }


        SpannableStringBuilder errMsg = new SpannableStringBuilder();
        errMsg.append("Для следующих товара(ов) с ОСВ (Особым Вниманием) вы должны обязательно указать ЦЕНУ:").append("\n\n");

        // 5.0
        List<String> osvTovarIds = Arrays.asList(tovIds);
        boolean hasOsvList = !osvTovarIds.isEmpty();
        int totalOSV = 0;
        int foundWithPrice = 0;
        int missingPriceCount = 0;
        List<String> missingPriceTovarIds = new ArrayList<>();

        for (ReportPrepareDB item : reportPrepare) {
            String itemTovarId = normalizeId(item.getTovarId());
            boolean isOSV = osvTovarIds.contains(itemTovarId);

            TovarDB tov = getTovarByIdSafe(itemTovarId);
            if (tov != null) {
                String msg = String.format("(%s) %s (%s)", itemTovarId, tov.getNm(), tov.getWeight());

                // Если товар на витрине (face > 0) — нас он интересует
                if (!hasPositiveFace(item)) {
                    // пропускаем товары, которых нет на витрине
                    continue;
                }

                boolean hasPrice = hasPositivePrice(item);

                if (hasOsvList) {
                    if (!isOSV) {
                        // Если список ОСВ заполнен, товары без ОСВ пропускаем, как в 1С.
                        continue;
                    }

                    totalOSV++;
                    if (hasPrice) {
                        foundWithPrice++;
                        item.find = 1;
                    } else {
                        // Для товара с ОСВ и присутствующего на витрине, цена не указана -> ошибка
                        err++;
                        missingPriceCount++;
                        missingPriceTovarIds.add(itemTovarId);
                        errMsg.append(createLinkedString(msg, item, tov)).append("\n");
                    }
                } else {
                    if (isOsvOnlyOption) {
                        // 174974 работает только по ОСВ. Если ОСВ для текущей ТТ нет,
                        // проверять остальные товары не нужно.
                        continue;
                    }

                    // Если список ОСВ пуст, проверяем цены по товарам на витрине с учетом КолМин.
                    if (hasPrice) {
                        foundWithPrice++;
                        item.find = 1;
                    } else if (colMin == 0) {
                        // КолМин=0 означает, что цена обязательна у всех товаров на витрине.
                        err++;
                        missingPriceCount++;
                        missingPriceTovarIds.add(itemTovarId);
                        errMsg.append(createLinkedString(msg, item, tov)).append("\n");
                    }
                }
            }
        }

        // 5.1. Если менеджер указал КолМин > числа записей — используем фактическое количество
//        colMin = reportPrepare.size() < colMin ? reportPrepare.size() : colMin;

        // Формирование сообщения общего вида
        if (missingPriceCount > 0) {
            spannableStringBuilder.append(errMsg);
        }

        // 6.0 Логика коротких сообщений (приближённо соответствует 1С)
        int totalRelevant = 0; // количество товаров, присутствующих на витрине (face>0)
        for (ReportPrepareDB rp : reportPrepare) {
            if (hasPositiveFace(rp)) {
                totalRelevant++;
            }
        }

        // вычислим сколько товаров вообще отмечено как "нашли" (с ценой)
        int found = foundWithPrice;

        if (reportPrepare.size() == 0 || totalRelevant == 0) {
            spannableStringBuilder.append("Товаров, по которым надо проверять факт наличия ЦЕН, не обнаружено.");
            signal = false; // нет товаров — замечаний нет
        } /* скорее всего придется поменять местами с нижним блоком totalOSV == 0, так логично для меня, но сделал как в 1с */ else if (missingPriceCount > 0 && (isPriceOption || isOsvOnlyOption)) {
//            spannableStringBuilder.append("Не предоставлена информация о ЦЕНАХ по товару (" + missingPriceCount + " шт.) (в т.ч. с ОСВ (Особым Вниманием)). См. таблицу.");
            signal = true;
        } else if ((found == 0 && hasOsvList) &&
                (optionDB.getOptionId().equals("174974") || optionDB.getOptionControlId().equals("174974"))) {
            spannableStringBuilder.append("Жоден з товарiв ")
                    .append(String.valueOf(totalOSV))
                    .append(" з Особою увагою не присутнiй на вiтринi. Зауважень по зазначенню цiн немає");
            signal = false;
        } else if (isOsvOnlyOption && !hasOsvList) {
            spannableStringBuilder.append("Для данной ТТ, на текущий момент, нет товаров с ОСВ (Особым Вниманием). Контролировать нечего. Замечаний нет.");
            signal = false;
        } else if (hasOsvList && totalOSV == 0 && (isPriceOption || isOsvOnlyOption)) {
            spannableStringBuilder.append("Для данной ТТ, на текущий момент, нет товаров с ОСВ (Особым Вниманием). Контролировать нечего. Замечаний нет.");
            signal = false;
        } else if (isOsvOnlyOption) {
            spannableStringBuilder.append("Замечаний по предоставлению информации о Ценах по товарам с ОСВ (Особым Вниманием) нет.");
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

        if (isOsvOnlyOption) {
            logOsvOnlySummary(requirementsOptionId, tovIds, reportPrepare.size(), totalOSV, foundWithPrice, missingPriceCount, missingPriceTovarIds, signal);
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
                    double penalty = calculatePenalty(wpDataDB);
                    optionDB.setIsSignal("1");
                    optionDB.setSumPenalty(String.valueOf(penalty));
                } else {
                    optionDB.setIsSignal("2");
                    optionDB.setSumPenalty("0.00");
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

    private boolean hasPositiveFace(ReportPrepareDB item) {
        return parsePositiveNumber(item != null ? item.getFace() : null);
    }

    private boolean hasPositivePrice(ReportPrepareDB item) {
        return parsePositiveNumber(item != null ? item.getPrice() : null);
    }

    private boolean parsePositiveNumber(String value) {
        if (value == null) {
            return false;
        }

        String normalized = value.trim().replace(",", ".");
        if (normalized.isEmpty()) {
            return false;
        }

        try {
            return Double.parseDouble(normalized) > 0d;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isOptionOrControl(int id) {
        if (optionDB == null) {
            return false;
        }

        String expected = String.valueOf(id);
        return expected.equals(optionDB.getOptionId()) || expected.equals(optionDB.getOptionControlId());
    }

    private void logOsvOnlySummary(
            int requirementsOptionId,
            String[] tovIds,
            int reportPrepareCount,
            int totalOsvWithFace,
            int foundWithPrice,
            int missingPriceCount,
            List<String> missingPriceTovarIds,
            boolean signal
    ) {
        Globals.writeToMLOG(
                "INFO",
                "AvailabilityOfPrices/174974",
                "dad2=" + dad2
                        + ", addrId=" + (wpDataDB != null ? wpDataDB.getAddr_id() : 0)
                        + ", clientId=" + safe(wpDataDB != null ? wpDataDB.getClient_id() : null)
                        + ", themeId=" + (wpDataDB != null ? wpDataDB.getTheme_id() : 0)
                        + ", mainOption=" + safe(wpDataDB != null ? wpDataDB.getMain_option_id() : null)
                        + ", requirementsOptionId=" + requirementsOptionId
                        + ", reportPrepare=" + reportPrepareCount
                        + ", osvIds=" + previewIds(tovIds)
                        + ", totalOsvWithFace=" + totalOsvWithFace
                        + ", prices=" + foundWithPrice
                        + ", missing=" + missingPriceCount
                        + ", missingIds=" + previewIds(missingPriceTovarIds)
                        + ", signal=" + signal
        );
    }

    private String previewIds(String[] ids) {
        if (ids == null || ids.length == 0) {
            return "";
        }

        int limit = Math.min(ids.length, 10);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                result.append(",");
            }
            result.append(ids[i]);
        }
        if (ids.length > limit) {
            result.append(",...");
        }
        return result.toString();
    }

    private String previewIds(List<String> ids) {
        return ids != null ? previewIds(ids.toArray(new String[0])) : "";
    }

    private TovarDB getTovarByIdSafe(String tovId) {
        try {
            String id = normalizeId(tovId);
            if (id == null || id.isEmpty() || id.equals("0")) {
                return null;
            }
            return TovarRealm.getById(id);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalizeId(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private String safe(String value) {
        return value != null ? value : "";
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
