package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.Globals.OptionControlName.PRICE;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Number;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.sql.SQLData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers.PriceSaveGuard;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers.PriceDisplay;
import ua.com.merchik.merchik.data.DataFromServer.PhotoData.ImagesTypeList;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites;
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;
import ua.com.merchik.merchik.features.main.options.ProductIssueCorrection;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Translate;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.PhotoDescriptionText;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.PromoRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.database.room.RoomManager;
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
    public int OPTION_CONTROL_AVAILABILITY_OF_PRICES_MIN_MAX_ID = 175121;

    private static final String MIN_MAX_PRICE_TEXT =
            "ЦЕНУ ДО ПОЧАТКУ АКЦІЇ (див. ЗАКРЕСЛЕНУ ціну на АКЦІЙНОМУ ціннику)";
    private static final String MIN_MAX_PRICE_NAME =
            "ЦІНА ДО ПОЧАТКУ АКЦІЇ (див. ЗАКРЕСЛЕНУ ціну на АКЦІЙНОМУ ціннику)";


    public boolean signal = true;

    private long dad2;
    private static final AtomicLong PRICE_CHECK_RUN = new AtomicLong();
    private String priceCheckTrace;
    private final LinkedHashSet<String> productIssues = new LinkedHashSet<>();

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
            if (priceCheckTrace != null) {
                Globals.writeToMLOG("ERROR", "AvailabilityOfPrices/175121/error", priceCheckTrace + ", error=" + e);
            }
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityOfPrices", "Exception e: " + e);
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityOfPrices", "StackTrace: " + Arrays.toString(e.getStackTrace()));
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

        boolean isMinMaxOption = isOptionOrControl(OPTION_CONTROL_AVAILABILITY_OF_PRICES_MIN_MAX_ID);
        if (isMinMaxOption) {
            notCloseSpannableStringBuilderDialog = true;
            spannableStringBuilder.clear();
            spannableStringBuilder.clearSpans();
            stringBuilderMsg.setLength(0);
            productIssues.clear();
            priceCheckTrace = "run=" + PRICE_CHECK_RUN.incrementAndGet() + ", dad2=" + dad2
                    + ", report=" + (wpDataDB == null ? "" : safe(wpDataDB.getDoc_num_otchet()))
                    + ", mode=" + nnkMode;
            Globals.writeToMLOG("INFO", "AvailabilityOfPrices/175121/start", priceCheckTrace
                    + ", visitDate=" + (wpDataDB == null ? null : wpDataDB.getDt())
                    + ", blockPns=" + safe(optionDB.getBlockPns()));
        }
        // Получение RP по данному документу.
        List<ReportPrepareDB> reportPrepare = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(dad2));
        boolean isPriceOption = isOptionOrControl(OPTION_CONTROL_AVAILABILITY_OF_PRICES_ID) || isMinMaxOption;
        boolean isOsvOnlyOption = isOptionOrControl(OPTION_CONTROL_AVAILABILITY_OF_PRICES_OSV_ID);

        // Получение Доп. Требований с дополнительными фильтрами.
        List<AdditionalRequirementsDB> additionalRequirements = Collections.emptyList();
        int requirementsOptionId = isOsvOnlyOption
                ? OPTION_CONTROL_AVAILABILITY_OF_PRICES_OSV_ID
                : (isMinMaxOption
                   ? OPTION_CONTROL_AVAILABILITY_OF_PRICES_MIN_MAX_ID
                   : OPTION_CONTROL_AVAILABILITY_OF_PRICES_ID);
        String[] tovIds;
        if (isPriceOption || isOsvOnlyOption) {

            Date date = wpDataDB.getDt();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, -2);
            Date dateMinusTwoDays = calendar.getTime();

            additionalRequirements = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(document, true, requirementsOptionId, null, wpDataDB.getDt(), wpDataDB.getDt(), null, null, null, dateMinusTwoDays.getTime() / 1000);
//            additionalRequirements = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(document, true, requirementsOptionId, null, wpDataDB.getDt(), wpDataDB.getDt(), null, null, null, null);
            Log.e("additionalRequirements", "AvailabilityOfPrices: " + new Gson().toJson(additionalRequirements));
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
        errMsg.append("Для следующих товара(ов) с ОСВ (Особым Вниманием) вы должны обязательно указать ")
                .append(isMinMaxOption ? MIN_MAX_PRICE_TEXT : "ЦЕНУ")
                .append(":")
                .append("\n\n");
        SpannableStringBuilder prePromotionIssues = new SpannableStringBuilder();
        Map<String, PromoDB> promosByType = new HashMap<>();
        Map<String, String> tradeMarkNames = new HashMap<>();
        Map<String, Integer> promotionPhotoCounts = isMinMaxOption
                ? StackPhotoRealm.getReadyProductPhotoCounts(dad2, 28) : Collections.emptyMap();
        String corridorTemplate = isMinMaxOption ? Translate.translationText(
                PrePromotionPriceMessage.TRANSLATION_ID, PrePromotionPriceMessage.DEFAULT_TEMPLATE) : "";

        // 5.0
        List<String> osvTovarIds = Arrays.asList(tovIds);
        boolean hasOsvList = !osvTovarIds.isEmpty();
        int totalOSV = 0;
        int foundWithPrice = 0;
        int missingPriceCount = 0;
        int invalidPriceCount = 0;
        int missingPromotionPresenceCount = 0;
        int photoExceptionCount = 0;
        List<String> missingPriceTovarIds = new ArrayList<>();
        int noFaceCount = 0;
        int missingTovarCount = 0;
        int nonOsvCount = 0;
        int evaluatedCount = 0;
        StringBuilder itemDiagnostics = new StringBuilder();
        Globals.writeToMLOG("INFO", "AvailabilityOfPrices/input",
                "dad2=" + dad2
                        + ", optionId=" + safe(optionDB != null ? optionDB.getOptionId() : null)
                        + ", optionControlId=" + safe(optionDB != null ? optionDB.getOptionControlId() : null)
                        + ", isMinMaxOption=" + isMinMaxOption
                        + ", isPriceOption=" + isPriceOption
                        + ", isOsvOnlyOption=" + isOsvOnlyOption
                        + ", requirementsOptionId=" + requirementsOptionId
                        + ", addrId=" + (wpDataDB != null ? wpDataDB.getAddr_id() : 0)
                        + ", clientId=" + safe(wpDataDB != null ? wpDataDB.getClient_id() : null)
                        + ", themeId=" + (wpDataDB != null ? wpDataDB.getTheme_id() : 0)
                        + ", mainOptionId=" + safe(wpDataDB != null ? wpDataDB.getMain_option_id() : null)
                        + ", visitDate=" + (wpDataDB != null ? wpDataDB.getDt() : null)
                        + ", colMin=" + colMin
                        + ", reportPrepareCount=" + reportPrepare.size()
                        + ", additionalRequirementsCount=" + additionalRequirements.size()
                        + ", osvTovarIds=" + previewIds(tovIds)
                        + ", requirements=" + previewRequirements(additionalRequirements));

        // 5.2
        for (ReportPrepareDB item : reportPrepare) {
            String itemTovarId = normalizeId(item.getTovarId());
            boolean isOSV = osvTovarIds.contains(itemTovarId);

            TovarDB tov = getTovarByIdSafe(itemTovarId);
            if (tov == null) missingTovarCount++;
            if (tov == null && !isMinMaxOption) {
                appendItemDiagnostic(itemDiagnostics, itemDiagnostics.length(), itemTovarId,
                        "skip=PRODUCT_NOT_FOUND, face=" + safe(item.getFace()) + ", isOsv=" + isOSV);
            } else {
                // Если товар на витрине (face > 0) — нас он интересует
                if (!hasPositiveFace(item)) {
                    noFaceCount++;
                    appendItemDiagnostic(itemDiagnostics, itemDiagnostics.length(), itemTovarId,
                            "skip=FACE_NOT_POSITIVE, face=" + safe(item.getFace()) + ", isOsv=" + isOSV);
                    // пропускаем товары, которых нет на витрине
                    continue;
                }

                evaluatedCount++;
                if (!isOSV) {
                    if (hasOsvList) {
                        nonOsvCount++;
                    }
                    appendItemDiagnostic(itemDiagnostics, itemDiagnostics.length(), itemTovarId,
                            "skip=" + (hasOsvList ? "NOT_IN_OSV_LIST" : "OSV_LIST_EMPTY")
                                    + ", face=" + safe(item.getFace())
                                    + ", price=" + safe(item.getPrice())
                                    + ", priceMin=" + safe(item.getPriceMin())
                                    + ", priceMax=" + safe(item.getPriceMax()));
                    // Every option handled here checks prices only for products in the active OSV list.
                    continue;
                }

                String msg = tov == null ? "(" + itemTovarId + ")"
                        : String.format("(%s) %s (%s)", itemTovarId, tov.getNm(), tov.getWeight());
                if (tov != null) {
                    String tradeMarkName = tradeMarkNames.computeIfAbsent(safe(tov.getManufacturerId()), id -> {
                        TradeMarkDB tradeMark = RealmManager.getNmById(id);
                        return tradeMark == null ? "" : safe(tradeMark.getNm()).trim();
                    });
                    if (!tradeMarkName.isEmpty()) msg += ", " + tradeMarkName;
                }

                totalOSV++;
                String priceCheck = "price=" + safe(item.getPrice())
                        + ", priceMin=" + safe(item.getPriceMin())
                        + ", priceMax=" + safe(item.getPriceMax());
                if (isMinMaxOption) {
                    PrePromotionPricePolicy.PromotionCheck promotion = PrePromotionPricePolicy.promotionCheck(
                            wpDataDB.getDt(), item.getAkciyaId(), item.getAkciya());
                    PromoDB promo = promotion == PrePromotionPricePolicy.PromotionCheck.NOT_APPLICABLE ? null
                            : promosByType.computeIfAbsent(item.getAkciyaId().trim(), PromoRealm::getPromoDBById);
                    int discount = promo == null ? 0 : promo.getDiscountPercent();
                    int readyPhotoCount = promotionPhotoCounts.getOrDefault(itemTovarId, 0);
                    PrePromotionPricePolicy.Evaluation evaluation = PrePromotionPricePolicy.evaluate(
                            item.getPrice(), item.getPriceMin(), item.getPriceMax(), promotion, discount,
                            readyPhotoCount > 0);
                    PrePromotionPricePolicy.Issue issue = evaluation.issue;
                    if (evaluation.photoExceptionApplied) photoExceptionCount++;
                    item.find = issue.currentPriceFound() ? 1 : 0;
                    foundWithPrice += item.find;
                    Globals.writeToMLOG("INFO", "AvailabilityOfPrices/175121/item", priceCheckTrace
                            + ", rpId=" + item.getID() + ", tovarId=" + itemTovarId
                            + ", barcode=" + (tov == null ? "" : safe(tov.getBarcode()))
                            + ", face=" + safe(item.getFace()) + ", " + priceCheck
                            + ", akciyaId=" + safe(item.getAkciyaId()) + ", akciya=" + safe(item.getAkciya())
                            + ", promotionCheck=" + promotion + ", discountPercent=" + discount
                            + ", photoType=28, readyProductPhotos=" + readyPhotoCount
                            + ", photoExceptionApplied=" + evaluation.photoExceptionApplied
                            + ", " + evaluation.corridorDiagnostic() + ", found=" + item.find + ", result=" + issue);
                    if (issue != PrePromotionPricePolicy.Issue.NONE) {
                        err++;
                        item.errorExist = 1;
                        if (issue == PrePromotionPricePolicy.Issue.MISSING_CURRENT || issue == PrePromotionPricePolicy.Issue.MISSING_BEFORE) {
                            missingPriceCount++;
                            missingPriceTovarIds.add(itemTovarId);
                        } else if (issue == PrePromotionPricePolicy.Issue.MISSING_PROMOTION_PRESENCE) {
                            missingPromotionPresenceCount++;
                        } else {
                            invalidPriceCount++;
                        }
                        appendPrePromotionIssue(prePromotionIssues, evaluation, msg, item, promo, corridorTemplate);
                    }
                    continue;
                }
                if (hasPositivePrice(item)) {
                    foundWithPrice++;
                    item.find = 1;
                    appendItemDiagnostic(itemDiagnostics, itemDiagnostics.length(), itemTovarId,
                            "result=PRICE_PRESENT, face=" + safe(item.getFace()) + ", " + priceCheck);
                } else {
                    // Для товара с ОСВ и присутствующего на витрине, цена не указана -> ошибка
                    err++;
                    missingPriceCount++;
                    missingPriceTovarIds.add(itemTovarId);
                    errMsg.append(link(msg, v -> openCurrentPriceEditor(v.getContext(), item.getCodeDad2(), item.getTovarId()))).append("\n");
                    appendItemDiagnostic(itemDiagnostics, itemDiagnostics.length(), itemTovarId,
                            "result=PRICE_MISSING, required=price"
                                    + ", face=" + safe(item.getFace()) + ", " + priceCheck);
                }
            }
        }

        if (isMinMaxOption) {
            Globals.writeToMLOG("INFO", "AvailabilityOfPrices/175121/items",
                    priceCheckTrace
                            + ", evaluated=" + evaluatedCount
                            + ", noFace=" + noFaceCount
                            + ", productNotFound=" + missingTovarCount
                            + ", notOsv=" + nonOsvCount
                            + ", totalOsvWithFace=" + totalOSV
                            + ", pricesPresent=" + foundWithPrice
                            + ", pricesMissing=" + missingPriceCount
                            + ", pricesInvalid=" + invalidPriceCount
                            + ", missingPromotionPresence=" + missingPromotionPresenceCount
                            + ", photoExceptions=" + photoExceptionCount
                            + ", skippedPreview=" + itemDiagnostics);
        }

        // 5.1. Если менеджер указал КолМин > числа записей — используем фактическое количество
//        colMin = reportPrepare.size() < colMin ? reportPrepare.size() : colMin;

        // Формирование сообщения общего вида
        if (isMinMaxOption) {
            spannableStringBuilder.append(prePromotionIssues);
        } else if (missingPriceCount > 0) {
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

        if (reportPrepare.size() == 0) {
            spannableStringBuilder.append("Товаров, по которым надо проверять факт наличия ЦЕН, не обнаружено.");
            signal = false; // нет товаров — замечаний нет
        } /* скорее всего придется поменять местами с нижним блоком totalOSV == 0, так логично для меня, но сделал как в 1с */ else if (totalRelevant == 0 && Objects.equals(wpDataDB.getUser_opinion_id(), "59")) {
            spannableStringBuilder.append("Товаров клиента вообще нет ни на складе, ни на витрине ТТ.");
            signal = false;
        } else if (totalRelevant == 0) {
            spannableStringBuilder.append("Товаров клиента вообще нет ни на складе, ни на витрине ТТ и не указанно мнение: ")
                    .append(RoomManager.SQL_DB.opinionDao().getOpinionById(59).nm)
                    .append(". Если товаров заказчика ДЕЙСТВИТЕЛЬНО нет на ТТ выберите указанное мнение.");
            signal = true;
        } else if (err > 0 && (isPriceOption || isOsvOnlyOption)) {
//            spannableStringBuilder.append("Не предоставлена информация о ЦЕНАХ по товару (" + missingPriceCount + " шт.) (в т.ч. с ОСВ (Особым Вниманием)). См. таблицу.");
            signal = true;
        } else if ((found == 0 && hasOsvList) &&
                (optionDB.getOptionId().equals("174974") || optionDB.getOptionControlId().equals("174974"))) {
            spannableStringBuilder.append("Жоден з товарiв ")
                    .append(String.valueOf(totalOSV))
                    .append(" з Особою увагою не присутнiй на вiтринi. Зауважень по зазначенню цiн немає");
            signal = false;
        } else if (!hasOsvList) {
            spannableStringBuilder.append("Для данной ТТ нет действующих дополнительных требований по товарам с ОСВ (Особым Вниманием). Контролировать нечего. Замечаний нет.");
            signal = false;
        } else if (hasOsvList && totalOSV == 0 && (isPriceOption || isOsvOnlyOption)) {
            spannableStringBuilder.append("Для данной ТТ, на текущий момент, нет товаров с ОСВ (Особым Вниманием). Контролировать нечего. Замечаний нет.");
            signal = false;
        } else if (isOsvOnlyOption) {
            spannableStringBuilder.append("Замечаний по предоставлению информации о Ценах по товарам с ОСВ (Особым Вниманием) нет.");
            signal = false;
        } else if (found == 0) {
            if (isMinMaxOption) {
                spannableStringBuilder.append("Ни у одного товара не указана ")
                        .append(MIN_MAX_PRICE_NAME)
                        .append(".");
            } else {
                spannableStringBuilder.append("Ни у одного товара не указана Цена.");
            }
            signal = true;
        } else if (found < colMin) {
            if (isMinMaxOption) {
                spannableStringBuilder.append("Вы указали ")
                        .append(MIN_MAX_PRICE_TEXT.toLowerCase())
                        .append(" у ")
                        .append(String.valueOf(found))
                        .append(" товаров, что меньше минимально допустимого ")
                        .append(String.valueOf(colMin));
            } else {
                spannableStringBuilder.append("Вы указали данные о ценах у ").append("" + found).append(" товаров, что меньше минимально допустимого ").append("" + colMin);
            }
            signal = true;
        } else {
            if (isMinMaxOption) {
                spannableStringBuilder.append("Замечаний по предоставлению информации о ")
                        .append(MIN_MAX_PRICE_NAME.toLowerCase())
                        .append(" по товарам (в т.ч. с ОСВ (Особым Вниманием)) нет.");
            } else {
                spannableStringBuilder.append("Замечаний по предоставлению информации о Ценах по товарам (в т.ч. с ОСВ (Особым Вниманием)) нет.");
            }
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
                spannableStringBuilder.append("\n\n").append(isMinMaxOption
                        ? "Вы можете получить Премиальные БОЛЬШЕ, если будете указывать цену ДО ПОЧАТКУ АКЦІЇ и акционную цену на товары."
                        : "Вы можете получить Премиальные БОЛЬШЕ, если будете указывать цены на товары.");
            }
        }

        Globals.writeToMLOG("INFO", "AvailabilityOfPrices/input","spannableStringBuilder: " + spannableStringBuilder);

        String resultReason;
        if (reportPrepare.isEmpty() || totalRelevant == 0) {
            resultReason = "NO_RELEVANT_REPORT_ROWS";
        } else if (invalidPriceCount > 0) {
            resultReason = "INVALID_PRE_PROMOTION_PRICES";
        } else if (missingPriceCount > 0 && (isPriceOption || isOsvOnlyOption)) {
            resultReason = "REQUIRED_PRICE_MISSING";
        } else if (missingPromotionPresenceCount > 0) {
            resultReason = "MISSING_PROMOTION_PRESENCE";
        } else if (!hasOsvList) {
            resultReason = "NO_OSV_REQUIREMENTS";
        } else if (hasOsvList && totalOSV == 0 && (isPriceOption || isOsvOnlyOption)) {
            resultReason = "NO_OSV_PRODUCTS_ON_DISPLAY";
        } else if (isOsvOnlyOption) {
            resultReason = "OSV_PRICE_CHECK_PASSED";
        } else if (found == 0) {
            resultReason = "NO_PRICES_FOUND";
        } else if (found < colMin) {
            resultReason = "BELOW_MINIMUM_COUNT";
        } else {
            resultReason = "PRICE_CHECK_PASSED";
        }

        checkUnlockCode(optionDB);
        Globals.writeToMLOG("INFO", isMinMaxOption ? "AvailabilityOfPrices/175121/result" : "AvailabilityOfPrices/result",
                (isMinMaxOption ? priceCheckTrace + ", violations=" + err
                        + ", missingPromotionPresence=" + missingPromotionPresenceCount : "dad2=" + dad2)
                        + ", optionId=" + safe(optionDB != null ? optionDB.getOptionId() : null)
                        + ", optionControlId=" + safe(optionDB != null ? optionDB.getOptionControlId() : null)
                        + ", requirementsOptionId=" + requirementsOptionId
                        + ", hasOsvList=" + hasOsvList
                        + ", reportPrepare=" + reportPrepare.size()
                        + ", totalRelevant=" + totalRelevant
                        + ", totalOsvWithFace=" + totalOSV
                        + ", found=" + found
                        + ", missing=" + missingPriceCount
                        + ", pricesInvalid=" + invalidPriceCount
                        + ", colMin=" + colMin
                        + ", resultReason=" + resultReason
                        + ", calculatedSignal=" + signal
                        + ", blockPns=" + safe(optionDB != null ? optionDB.getBlockPns() : null)
                        + ", isSignal=" + safe(optionDB != null ? optionDB.getIsSignal() : null)
                        + ", blocked=" + isBlockOption());

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

    private String previewRequirements(List<AdditionalRequirementsDB> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return "";
        }

        int limit = Math.min(requirements.size(), 10);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            AdditionalRequirementsDB requirement = requirements.get(i);
            if (i > 0) {
                result.append(";");
            }
            result.append("option=").append(safe(requirement.getOptionId()))
                    .append("/tovar=").append(safe(requirement.getTovarId()))
                    .append("/addr=").append(safe(requirement.getAddrId()))
                    .append("/grp=").append(safe(requirement.getGrpId()))
                    .append("/theme=").append(safe(requirement.getThemeId()))
                    .append("/mainOption=").append(safe(requirement.getMainOptionId()));
        }
        if (requirements.size() > limit) {
            result.append(";...");
        }
        return result.toString();
    }

    private void appendItemDiagnostic(StringBuilder diagnostics, int currentLength, String tovarId, String details) {
        if (currentLength >= 2500) {
            if (!diagnostics.toString().endsWith("[truncated]")) {
                diagnostics.append("[truncated]");
            }
            return;
        }
        if (diagnostics.length() > 0) {
            diagnostics.append("; ");
        }
        diagnostics.append("tovar=").append(safe(tovarId)).append(":").append(details);
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

    private void appendPrePromotionIssue(SpannableStringBuilder out, PrePromotionPricePolicy.Evaluation evaluation,
                                         String product, ReportPrepareDB row, PromoDB promo, String corridorTemplate) {
        PrePromotionPricePolicy.Issue issue = evaluation.issue;
        StringBuilder detail = new StringBuilder();
        String current = PriceDisplay.format(row.getPrice());
        switch (issue) {
            case MISSING_CURRENT:
                detail.append("Не зазначена поточна ціна.");
                break;
            case MISSING_BEFORE:
                detail.append("Ви повинні зазначити ціну ДО ПОЧАТКУ АКЦІЇ (див. закреслену ціну на акційному ціннику).");
                break;
            case ABOVE_MIN:
            case ABOVE_MAX:
                detail.append("Ви вказали АКЦІЙНУ ціну ").append(current)
                        .append(" грн, БІЛЬШУ ніж ціна ДО ПОЧАТКУ АКЦІЇ: ")
                        .append(PriceDisplay.format(issue == PrePromotionPricePolicy.Issue.ABOVE_MIN
                                ? row.getPriceMin() : row.getPriceMax())).append(" грн. Змінити поточну ціну.");
                break;
            case MISSING_PROMOTION_PRESENCE:
                String promotionName = promo == null || safe(promo.getNm()).trim().isEmpty()
                        ? row.getAkciyaId() : promo.getNm();
                detail.append("Ви не зазначили НАЯВНІСТЬ або ВІДСУТНІСТЬ акції: (").append(promotionName).append(").");
                break;
            case EQUAL_TO_MAX:
                detail.append("Ви вказали АКЦІЙНУ ціну ").append(current)
                        .append(" грн, що ДОРІВНЮЄ ціні ДО ПОЧАТКУ АКЦІЇ: ")
                        .append(PriceDisplay.format(row.getPriceMax()))
                        .append(" грн. Або правильно зазначте цін, або вкажіть, що Акція відсутня.");
                break;
            case OUTSIDE_CORRIDOR_DELTA:
                detail.append(PrePromotionPriceMessage.corridor(corridorTemplate, row.getPrice(),
                        promo.getDiscountPercent(), evaluation));
                break;
        }
        String description = detail.toString();
        if (!productIssues.add(row.getTovarId())) return;
        if (out.length() > 0) out.append("\n────────────────\n");
        out.append(link(product, v -> openIssueProducts(v, Collections.singletonList(row.getTovarId()),
                product + ": " + description))).append(": ").append(description);
    }

    @Override
    protected boolean allowOutsideDismiss() { return productIssues.isEmpty(); }

    @Override
    protected CharSequence optionMessageHeading() {
        if (productIssues.isEmpty()) return "";
        List<String> ids = new ArrayList<>(productIssues);
        String count = String.valueOf(ids.size());
        return new SpannableStringBuilder(link( count + " товарів:",
                v -> openIssueProducts(v, ids, "Ви повинні виправити всі зауваження щодо " + count + " товарів")))
                .append("\n\n");
    }

    private void openIssueProducts(View view, List<String> ids, String subtitle) {
        ProductIssueCorrection.open(view, dad2, wpDataDB.getClient_id(), ids, subtitle, () -> {
            try {
                executeOption();
                refreshOpenSpannableOptionMessage();
            } catch (RuntimeException e) {
                logOptionError("refreshAfterPriceEdit", e);
                Toast.makeText(view.getContext(), "Не вдалося оновити список зауважень.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private SpannableString link(String label, View.OnClickListener action) {
        SpannableString result = new SpannableString(label);
        result.setSpan(new ClickableSpan() {
            @Override public void onClick(View view) { action.onClick(view); }
        }, 0, result.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return result;
    }

    private void openCurrentPriceEditor(Context editorContext, String visit, String productId) {
        ReportPrepareDB current = RealmManager.getTovarReportPrepare(visit, productId);
        if (current == null) {
            Toast.makeText(editorContext, "Запись товара не найдена. Обновите список товаров.", Toast.LENGTH_LONG).show();
            return;
        }
        DialogData editor = new DialogData(editorContext);
        editor.setTitle("");
        editor.setText("");
        editor.setClose(editor::dismiss);
        TovarDB tovar = getTovarByIdSafe(productId);
        if (tovar != null) {
            editor.setImage(true, getPhotoFromDB(tovar));
            editor.setAdditionalText(setPhotoInfo(TPL, tovar, "", ""));
        } else {
            editor.setTitle(TovarRequisites.optionTitleForVisit(TPL, visit));
            editor.setText("(" + productId + ")");
        }
        editor.tovarOptions = TPL;
        editor.reportPrepareDB = current;
        editor.setOperation(Number, current.getPrice(), Collections.emptyMap(), () -> {
            if (PriceSaveGuard.savePrice(editorContext, current, editor.getOperationResult(), false))
                Toast.makeText(editorContext, "Внесено: " + editor.getOperationResult(), Toast.LENGTH_LONG).show();
        });
        editor.show();
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

            String title = TovarRequisites.optionTitleForVisit(tpl, String.valueOf(dad2));

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

}
