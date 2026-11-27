package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.text.TextUtils;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;

import kotlin.Unit;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.DataFromServer.PhotoData.ImagesTypeList;
import ua.com.merchik.merchik.data.Database.Room.ImagesTypeListSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.AddressRealm;
import ua.com.merchik.merchik.database.realm.tables.PromoRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;

public final class PriceSaveGuard {
    private PriceSaveGuard() {
    }

    public static boolean savePrice(Context context, ReportPrepareDB snapshot, String value,
                                    boolean beforePromotion) {
        return savePrice(context, snapshot, value, beforePromotion, true);
    }

    public static boolean savePrice(Context context, ReportPrepareDB snapshot, String value,
                                    boolean beforePromotion, boolean showError) {
        return savePrice(context, snapshot, value, beforePromotion, showError, false, null, 0, null);
    }

    public static boolean saveInlinePrice(Context context, ReportPrepareDB snapshot, String value,
                                          boolean beforePromotion, PromotionPriceCheck approval, int approvedSalary,
                                          Set<String> linkedProducts) {
        return savePrice(context, snapshot, value, beforePromotion, false, true, approval, approvedSalary, linkedProducts);
    }

    public static boolean hasPrePromotionPriceOption(List<OptionsDB> options) {
        if (options != null) for (OptionsDB option : options) {
            if (!"1".equals(option.getDeleted()) && ("175121".equals(option.getOptionId())
                    || "175121".equals(option.getOptionControlId()))) return true;
        }
        return false;
    }

    public static Set<String> linkedPriceProducts(List<OptionsDB> options, List<AdditionalRequirementsDB> requirements) {
        if (!hasPrePromotionPriceOption(options) || requirements == null) return Collections.emptySet();
        Set<String> ids = new HashSet<>();
        for (AdditionalRequirementsDB requirement : requirements) {
            String id = requirement.getTovarId();
            if (id != null && !id.isEmpty() && !"0".equals(id)) ids.add(id);
        }
        return ids;
    }

    public static Set<String> loadLinkedPriceProducts(String dad2) {
        long id;
        try { id = Long.parseLong(dad2); }
        catch (NumberFormatException ignored) { return Collections.emptySet(); }
        List<OptionsDB> options = RealmManager.getTovarOptionInReportPrepare(dad2, null);
        if (!hasPrePromotionPriceOption(options)) return Collections.emptySet();
        WpDataDB visit = RealmManager.getWorkPlanRowByCodeDad2(id);
        if (visit == null || visit.getDt() == null || AddressRealm.getAddressById(visit.getAddr_id()) == null)
            return Collections.emptySet();
        return linkedPriceProducts(options, AdditionalRequirementsRealm.getData3(visit,
                AdditionalRequirementsRealm.AdditionalRequirementsModENUM.DEFAULT, null, null, 0));
    }

    public static boolean linksPrices(ReportPrepareDB row, Set<String> eligibleProducts) {
        return row != null && isPromotionExplicitlyAbsent(row.getAkciya())
                && eligibleProducts.contains(row.getTovarId());
    }

    /** The editor, confirmation dialog and transaction must validate the same resulting prices. */
    public static final class PriceEdit {
        public final String price, min, max;
        private final boolean writeCurrent, writeBefore;

        public PriceEdit(String price, String min, String max, String entered, boolean before, boolean link) {
            writeCurrent = !before || link;
            writeBefore = before || link;
            this.price = writeCurrent ? entered : price;
            this.min = writeBefore ? entered : min;
            this.max = writeBefore ? entered : max;
        }

        void apply(ReportPrepareDB row) {
            if (writeCurrent) row.setPrice(price);
            if (writeBefore) {
                row.setPriceMin(min);
                row.setPriceMax(max);
            }
        }
    }

    private static PriceEdit edit(ReportPrepareDB row, String entered, boolean before, Set<String> products) {
        boolean link = isPromotionExplicitlyAbsent(row.getAkciya()) && linksPrices(row,
                products != null ? products : loadLinkedPriceProducts(row.getCodeDad2()));
        return new PriceEdit(row.getPrice(), row.getPriceMin(), row.getPriceMax(), entered, before, link);
    }

    private static boolean savePrice(Context context, ReportPrepareDB snapshot, String value,
                                     boolean beforePromotion, boolean showError,
                                     boolean checkDiscount, PromotionPriceCheck approval, int approvedSalary,
                                     Set<String> linkedProducts) {
        if (context == null) {
            Globals.writeToMLOG("ERROR", "PriceSaveGuard", "No editor context; price not saved");
            return false;
        }
        if (value == null || value.isEmpty()) {
            if (showError) Toast.makeText(context, "Для сохранения - внесите данные", Toast.LENGTH_SHORT).show();
            return false;
        }

        String[] invalidPrices = new String[2];
        String[] missingBeforePrice = {null};
        boolean[] markPromotionPresent = {false};
        String[] promotionTypeToSave = {null};
        PriceEdit[] accepted = {null};
        long changedAt = System.currentTimeMillis() / 1000;
        boolean saved = ReportPrepareRealm.updateFields(snapshot, current -> {
            PriceEdit edit = edit(current, value, beforePromotion, linkedProducts);
            String invalidBefore = invalidBeforePrice(edit.price, edit.min, edit.max);
            if (invalidBefore != null) {
                invalidPrices[0] = edit.price;
                invalidPrices[1] = invalidBefore;
                return false;
            }
            if (checkDiscount) {
                int salary = Globals.getAverageSalary();
                if (exceededSalaryLimit(value, salary) != null && salary != approvedSalary) return false;
                PromotionPriceCheck check = getPromotionPriceCheck(current, edit.price, edit.max);
                if (check.outsideRange() && !check.matches(approval)) return false;
            }
            // Inline input saves prefixes; fill its missing prices only when editing finishes.
            if (!edit.writeBefore && !checkDiscount) {
                missingBeforePrice[0] = missingPricesForNoPromotion(current.getAkciya(), value,
                        current.getPriceMin(), current.getPriceMax());
            }
            // Decide from the current DB row, not from the possibly stale editor snapshot.
            markPromotionPresent[0] = shouldAutoMarkPromotion(edit.price, edit.min, current.getAkciya());
            if (markPromotionPresent[0] && isPromotionTypeMissing(current.getAkciyaId())) {
                promotionTypeToSave[0] = resolvePromotionTypeId(current);
            }
            accepted[0] = edit;
            return true;
        }, current -> {
            accepted[0].apply(current);
            if (missingBeforePrice[0] != null) {
                current.setPriceMin(missingBeforePrice[0]);
                current.setPriceMax(missingBeforePrice[0]);
            }
            if (markPromotionPresent[0]) {
                current.setAkciya("1");
                if (promotionTypeToSave[0] != null) {
                    current.setAkciyaId(promotionTypeToSave[0]);
                }
            }
            current.setUploadStatus(1);
            current.setDtChange(changedAt);
        });

        if (saved && promotionTypeToSave[0] != null) {
            Globals.writeToMLOG("INFO", "PriceSaveGuard/autoPromotion",
                    "rpId=" + snapshot.getID() + ", dad2=" + snapshot.getCodeDad2()
                            + ", tovarId=" + snapshot.getTovarId()
                            + ", promoTpId=" + promotionTypeToSave[0]);
        }

        if (invalidPrices[0] != null) {
            if (!showError) return false;
            String tovarId = snapshot.getTovarId();
            TovarDB tovar = TovarRealm.getById(tovarId);
            String product = productLabel(tovarId, tovar);
            Globals.writeToMLOG("INFO", "PriceSaveGuard",
                    "Rejected: rpId=" + snapshot.getID() + ", dad2=" + snapshot.getCodeDad2()
                            + ", tovarId=" + tovarId + ", beforePromotion=" + beforePromotion
                            + ", price=" + invalidPrices[0] + ", priceMin=" + invalidPrices[1]);
            Context host = context;
            while (!(host instanceof Activity) && host instanceof ContextWrapper) {
                Context base = ((ContextWrapper) host).getBaseContext();
                if (base == host) break;
                host = base;
            }
            if (!(host instanceof Activity) || ((Activity) host).isFinishing() || ((Activity) host).isDestroyed()) {
                Globals.writeToMLOG("ERROR", "PriceSaveGuard", "Cannot show price error: no active activity");
                return false;
            }
            priceErrorDialog((Activity) host, product, invalidPrices[0], invalidPrices[1]).show();
        } else if (!saved && showError) {
            Toast.makeText(context, "Запись товара не найдена. Обновите список товаров.", Toast.LENGTH_LONG).show();
        }
        return saved;
    }

    public static boolean fillMissingPricesForNoPromotion(ReportPrepareDB snapshot) {
        String[] missingPrice = {null};
        long changedAt = System.currentTimeMillis() / 1000;
        return ReportPrepareRealm.updateFields(snapshot, current -> {
            missingPrice[0] = missingPricesForNoPromotion(current.getAkciya(), current.getPrice(),
                    current.getPriceMin(), current.getPriceMax());
            return missingPrice[0] != null;
        }, current -> {
            current.setPriceMin(missingPrice[0]);
            current.setPriceMax(missingPrice[0]);
            current.setUploadStatus(1);
            current.setDtChange(changedAt);
        });
    }

    private static String resolvePromotionTypeId(ReportPrepareDB current) {
        String tovarId = current.getTovarId();
        String dad2 = current.getCodeDad2();
        if (tovarId == null || tovarId.isEmpty() || "0".equals(tovarId) || dad2 == null) return null;

        // Avoid loading the visit's requirements on each keystroke when no promotion type is supplied.
        if (RealmManager.INSTANCE.where(AdditionalRequirementsDB.class)
                .equalTo("tovarId", tovarId).greaterThan("promoTpId", 0).findFirst() == null) {
            return null;
        }

        long codeDad2;
        try {
            codeDad2 = Long.parseLong(dad2);
        } catch (NumberFormatException e) {
            Globals.writeToMLOG("ERROR", "PriceSaveGuard/autoPromotion", "Invalid dad2=" + dad2);
            return null;
        }
        WpDataDB wp = RealmManager.getWorkPlanRowByCodeDad2(codeDad2);
        if (wp == null || wp.getDt() == null || wp.getClient_id() == null
                || AddressRealm.getAddressById(wp.getAddr_id()) == null) return null;

        List<AdditionalRequirementsDB> requirements = AdditionalRequirementsRealm.getData3(
                wp, AdditionalRequirementsRealm.AdditionalRequirementsModENUM.DEFAULT, null, null, 0);
        String promoId = findPromotionTypeId(tovarId, requirements);
        if (promoId != null && PromoRealm.getPromoDBById(promoId) == null) {
            Globals.writeToMLOG("WARN", "PriceSaveGuard/autoPromotion",
                    "PromoDB not found: dad2=" + dad2 + ", tovarId=" + tovarId + ", promoTpId=" + promoId);
            return null;
        }
        return promoId;
    }

    public static int getDiscountPercent(ReportPrepareDB current) {
        PromoDB promo = getPromotion(current);
        return promo == null ? 0 : promo.getDiscountPercent();
    }

    private static PromoDB getPromotion(ReportPrepareDB current) {
        if (current == null || isPromotionExplicitlyAbsent(current.getAkciya())) return null;
        String promoId = current.getAkciyaId();
        if (isPromotionTypeMissing(promoId)) promoId = resolvePromotionTypeId(current);
        return isPromotionTypeMissing(promoId) ? null : PromoRealm.getPromoDBById(promoId);
    }

    public static PromotionPriceCheck getPromotionPriceCheck(ReportPrepareDB current, String price, String before) {
        PromoDB promo = getPromotion(current);
        return new PromotionPriceCheck(price, before, promo == null ? 0 : promo.getDiscountPercent(),
                promo == null ? "" : promo.getNm());
    }

    public static String productLabel(String tovarId, TovarDB tovar) {
        StringBuilder result = new StringBuilder("(").append(tovarId).append(")");
        if (tovar != null) {
            for (String part : new String[]{tovar.getNm(), tovar.getWeight()}) {
                if (part != null && !part.trim().isEmpty()) result.append(" ").append(part.trim());
            }
        }
        return result.toString();
    }

    public static BigDecimal exceededSalaryLimit(String value, int salary) {
        BigDecimal price = positivePrice(value);
        if (salary <= 0 || price == null) return null;
        BigDecimal limit = BigDecimal.valueOf(salary).divide(BigDecimal.valueOf(20));
        return price.compareTo(limit) > 0 ? limit : null;
    }

    public static MessageDialogBuilder priceWarningDialog(Activity activity, String product,
            PromotionPriceCheck check, String entered, boolean beforePromotion, BigDecimal exceededLimit,
            Runnable save, Runnable refuse, Runnable dismissed) {
        boolean[] answered = {false};
        String message = "";
        if (exceededLimit != null) {
            message = "Вы внесли " + (beforePromotion ? "цену ДО начала акции" : "текущую цену")
                    + " " + TextUtils.htmlEncode(entered) + " грн. Это превышает предполагаемую возможную цену.";
        }
        if (check != null) {
            String promotion = check.promotionName.isEmpty() ? "текущей акции"
                : "акции «" + TextUtils.htmlEncode(check.promotionName) + "»";
            ImagesTypeListSDB item = RoomManager.SQL_DB.imagesTypeListDao().getById(28);
            if (!message.isEmpty()) message += "<br><br>";
            message += "Вы указали цену ДО начала акции " + TextUtils.htmlEncode(PriceDisplay.format(check.priceBefore))
                    + " грн и текущую АКЦИОННУЮ цену " + TextUtils.htmlEncode(PriceDisplay.format(check.price))
                    + " грн. При этом скидка по " + promotion + " составляет " + check.discountPercent
                    + "%. Предполагаемая сумма скидки должна быть: " + check.expectedDiscountText()
                    + " грн, а у вас она составляет " + check.actualDiscountText() + " грн. Перепроверьте цены."
                    + (item == null ? "" : " Если цены указаны правильно, сделайте " + TextUtils.htmlEncode(item.nm) + ".");
        }
        return new MessageDialogBuilder(activity)
                .setTitle("Товари")
                .setSubTitle(product)
                .setStatus(DialogStatus.ALERT)
                .setMessage(message + "<br><br>ОТКАЗАТЬСЯ от сохранения цены?")
                .setOnConfirmAction("Да", () -> { answered[0] = true; refuse.run(); return Unit.INSTANCE; })
                .setOnCancelAction("Нет", () -> { answered[0] = true; save.run(); return Unit.INSTANCE; })
                .setOnDismissListener((kotlin.jvm.functions.Function0<Unit>) () -> {
                    if (!answered[0]) refuse.run();
                    dismissed.run();
                    return Unit.INSTANCE;
                });
    }

    /** Legacy editors resume their existing save/next-editor callbacks only after confirmation. */
    public static void confirmPriceSave(Activity activity, List<ReportPrepareDB> rows, String value,
                                        boolean beforePromotion, Runnable save, Runnable refuse) {
        List<ReportPrepareDB> snapshots = new ArrayList<>();
        for (ReportPrepareDB row : rows) if (row != null) snapshots.add(row);
        confirmPriceSave(activity, snapshots, value, beforePromotion, new HashMap<>(), 0, save, refuse);
    }

    private static void confirmPriceSave(Activity activity, List<ReportPrepareDB> snapshots, String value,
                                         boolean beforePromotion, Map<Long, PromotionPriceCheck> approvals,
                                         int approvedSalary, Runnable save, Runnable refuse) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        // Re-read all rows after each answer; a background exchange must not invalidate an approval silently.
        Map<Long, PromotionPriceCheck> checks = new LinkedHashMap<>();
        Map<Long, String> products = new HashMap<>();
        Map<String, Set<String>> linkedByVisit = new HashMap<>();
        for (ReportPrepareDB snapshot : snapshots) {
            ReportPrepareDB current = ReportPrepareRealm.getCurrentCopy(snapshot);
            if (current == null) {
                Toast.makeText(activity, "Запись товара не найдена. Обновите список товаров.", Toast.LENGTH_LONG).show();
                refuse.run();
                return;
            }
            Set<String> productsForVisit = isPromotionExplicitlyAbsent(current.getAkciya())
                    ? linkedByVisit.computeIfAbsent(current.getCodeDad2(), PriceSaveGuard::loadLinkedPriceProducts)
                    : Collections.emptySet();
            PriceEdit edit = edit(current, value, beforePromotion, productsForVisit);
            TovarDB tovar = TovarRealm.getById(current.getTovarId());
            String product = productLabel(current.getTovarId(), tovar);
            String invalidBefore = invalidBeforePrice(edit.price, edit.min, edit.max);
            if (invalidBefore != null) {
                priceErrorDialog(activity, product, edit.price, invalidBefore)
                        .setOnDismissListener((kotlin.jvm.functions.Function0<Unit>) () -> {
                            refuse.run(); return Unit.INSTANCE;
                        }).show();
                return;
            }
            Long id = current.getID();
            checks.put(id, getPromotionPriceCheck(current, edit.price, edit.max));
            products.put(id, product);
        }
        // One entered price/limit per bulk operation; discounts are approved per product.
        int salary = Globals.getAverageSalary();
        BigDecimal limit = approvedSalary == salary ? null : exceededSalaryLimit(value, salary);
        for (Map.Entry<Long, PromotionPriceCheck> entry : checks.entrySet()) {
            Long id = entry.getKey();
            PromotionPriceCheck check = entry.getValue();
            if (check.outsideRange() && !check.matches(approvals.get(id))) {
                priceWarningDialog(activity, products.get(id), check, value, beforePromotion, limit, () -> {
                    approvals.put(id, check);
                    confirmPriceSave(activity, snapshots, value, beforePromotion, approvals, salary, save, refuse);
                }, refuse, () -> {}).show();
                return;
            }
        }
        if (limit != null) {
            String label = products.size() == 1 ? products.values().iterator().next() : "Выбрано товаров: " + products.size();
            priceWarningDialog(activity, label, null, value, beforePromotion, limit,
                    () -> confirmPriceSave(activity, snapshots, value, beforePromotion, approvals, salary, save, refuse),
                    refuse, () -> {}).show();
            return;
        }
        save.run();
    }

    static boolean isPromotionTypeMissing(String promoId) {
        return promoId == null || promoId.trim().isEmpty() || "0".equals(promoId.trim());
    }

    static String findPromotionTypeId(String tovarId, List<AdditionalRequirementsDB> requirements) {
        if (tovarId == null || tovarId.isEmpty() || "0".equals(tovarId) || requirements == null) return null;
        for (AdditionalRequirementsDB requirement : requirements) {
            if (requirement != null && tovarId.equals(requirement.getTovarId())
                    && requirement.getPromoTpId() > 0) {
                return String.valueOf(requirement.getPromoTpId());
            }
        }
        return null;
    }

    public static MessageDialogBuilder priceErrorDialog(Activity activity, String product,
                                                         String price, String priceMin) {
        return new MessageDialogBuilder(activity)
                .setTitle("Изменения не сохранены")
                .setStatus(DialogStatus.ERROR)
                .setMessage("У товара " + TextUtils.htmlEncode(product)
                        + " АКЦИОННАЯ цена (" + TextUtils.htmlEncode(PriceDisplay.format(price))
                        + " грн) выше чем цена ДО начала акции (" + TextUtils.htmlEncode(PriceDisplay.format(priceMin))
                        + " грн).<br><br>Проверьте цены и введите корректные значения.")
                .setOnConfirmAction("Закрыть", () -> Unit.INSTANCE);
    }

    public static String invalidBeforePrice(String price, String min, String max) {
        if (isPriceAboveBeforePromotion(price, min)) return min;
        return isPriceAboveBeforePromotion(price, max) ? max : null;
    }

    public static boolean isPriceAboveBeforePromotion(String price, String priceMin) {
        BigDecimal current = positivePrice(price);
        BigDecimal beforePromotion = positivePrice(priceMin);
        // Missing prices are checked by the options; do not block entering the first of the two prices.
        return current != null && beforePromotion != null
                && PriceDisplay.cents(current).compareTo(PriceDisplay.cents(beforePromotion)) > 0;
    }

    static boolean shouldAutoMarkPromotion(String price, String priceMin, String akciya) {
        return !isPromotionExplicitlyAbsent(akciya) && hasBothPositivePrices(price, priceMin);
    }

    public static String missingPricesForNoPromotion(String akciya, String price,
                                                     String priceMin, String priceMax) {
        if (!isPromotionExplicitlyAbsent(akciya) || positivePrice(price) == null
                || !isEmptyOrZeroPrice(priceMin) || !isEmptyOrZeroPrice(priceMax)) {
            return null;
        }
        return price;
    }

    private static boolean isEmptyOrZeroPrice(String value) {
        if (value == null || value.trim().isEmpty()) return true;
        try {
            return new BigDecimal(value.trim().replace(',', '.')).signum() == 0;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static boolean isPromotionExplicitlyAbsent(String akciya) {
        // 0 = not selected, 1 = present, 2 = explicitly absent. Respect the persisted choice.
        return akciya != null && "2".equals(akciya.trim());
    }

    static boolean hasBothPositivePrices(String price, String priceMin) {
        return positivePrice(price) != null && positivePrice(priceMin) != null;
    }

    private static BigDecimal positivePrice(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            BigDecimal result = new BigDecimal(value.trim().replace(',', '.'));
            return result.signum() > 0 ? result : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
