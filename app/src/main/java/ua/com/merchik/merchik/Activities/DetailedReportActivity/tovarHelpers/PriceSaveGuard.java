package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.text.TextUtils;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.List;

import kotlin.Unit;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.AddressRealm;
import ua.com.merchik.merchik.database.realm.tables.PromoRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
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
        if (context == null) {
            Globals.writeToMLOG("ERROR", "PriceSaveGuard", "No editor context; price not saved");
            return false;
        }
        if (value == null || value.isEmpty()) {
            if (showError) Toast.makeText(context, "Для сохранения - внесите данные", Toast.LENGTH_SHORT).show();
            return false;
        }

        String[] invalidPrices = new String[2];
        boolean[] markPromotionPresent = {false};
        String[] promotionTypeToSave = {null};
        long changedAt = System.currentTimeMillis() / 1000;
        boolean saved = ReportPrepareRealm.updateFields(snapshot, current -> {
            String price = beforePromotion ? current.getPrice() : value;
            String priceMin = beforePromotion ? value : current.getPriceMin();
            if (isPriceAboveBeforePromotion(price, priceMin)) {
                invalidPrices[0] = price;
                invalidPrices[1] = priceMin;
                return false;
            }
            // Decide from the current DB row, not from the possibly stale editor snapshot.
            markPromotionPresent[0] = hasBothPositivePrices(price, priceMin);
            if (markPromotionPresent[0] && isPromotionTypeMissing(current.getAkciyaId())) {
                promotionTypeToSave[0] = resolvePromotionTypeId(current);
            }
            return true;
        }, current -> {
            if (beforePromotion) {
                current.setPriceMin(value);
                current.setPriceMax(value);
            } else {
                current.setPrice(value);
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
            String product = "(" + tovarId + ")";
            if (tovar != null && tovar.getNm() != null) {
                product += " " + tovar.getNm();
            }
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
                        + " АКЦИОННАЯ цена (" + TextUtils.htmlEncode(price)
                        + " грн) выше чем цена ДО начала акции (" + TextUtils.htmlEncode(priceMin)
                        + " грн).<br><br>Проверьте цены и введите корректные значения.")
                .setOnConfirmAction("Закрыть", () -> Unit.INSTANCE);
    }

    public static boolean isPriceAboveBeforePromotion(String price, String priceMin) {
        BigDecimal current = positivePrice(price);
        BigDecimal beforePromotion = positivePrice(priceMin);
        // Missing prices are checked by the options; do not block entering the first of the two prices.
        return current != null && beforePromotion != null && current.compareTo(beforePromotion) > 0;
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
