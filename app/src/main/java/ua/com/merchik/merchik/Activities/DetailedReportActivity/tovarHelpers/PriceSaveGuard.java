package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.text.TextUtils;
import android.widget.Toast;

import java.math.BigDecimal;

import kotlin.Unit;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;

public final class PriceSaveGuard {
    private PriceSaveGuard() {
    }

    public static boolean savePrice(Context context, ReportPrepareDB snapshot, String value,
                                    boolean beforePromotion) {
        if (context == null) {
            Globals.writeToMLOG("ERROR", "PriceSaveGuard", "No editor context; price not saved");
            return false;
        }
        if (value == null || value.isEmpty()) {
            Toast.makeText(context, "Для сохранения - внесите данные", Toast.LENGTH_SHORT).show();
            return false;
        }

        String[] invalidPrices = new String[2];
        long changedAt = System.currentTimeMillis() / 1000;
        boolean saved = ReportPrepareRealm.updateFields(snapshot, current -> {
            String price = beforePromotion ? current.getPrice() : value;
            String priceMin = beforePromotion ? value : current.getPriceMin();
            if (isPriceAboveBeforePromotion(price, priceMin)) {
                invalidPrices[0] = price;
                invalidPrices[1] = priceMin;
                return false;
            }
            return true;
        }, current -> {
            if (beforePromotion) {
                current.setPriceMin(value);
                current.setPriceMax(value);
            } else {
                current.setPrice(value);
            }
            current.setUploadStatus(1);
            current.setDtChange(changedAt);
        });

        if (invalidPrices[0] != null) {
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
            new MessageDialogBuilder((Activity) host)
                    .setTitle("Изменения не сохранены")
                    .setStatus(DialogStatus.ERROR)
                    .setMessage("У товара " + TextUtils.htmlEncode(product)
                            + " АКЦИОННАЯ цена (" + TextUtils.htmlEncode(invalidPrices[0])
                            + " грн) выше чем цена ДО начала акции (" + TextUtils.htmlEncode(invalidPrices[1])
                            + " грн).<br><br>Проверьте цены и введите корректные значения.")
                    .setOnConfirmAction("Закрыть", () -> Unit.INSTANCE)
                    .show();
        } else if (!saved) {
            Toast.makeText(context, "Запись товара не найдена. Обновите список товаров.", Toast.LENGTH_LONG).show();
        }
        return saved;
    }

    public static boolean isPriceAboveBeforePromotion(String price, String priceMin) {
        BigDecimal current = positivePrice(price);
        BigDecimal beforePromotion = positivePrice(priceMin);
        // Missing prices are checked by the options; do not block entering the first of the two prices.
        return current != null && beforePromotion != null && current.compareTo(beforePromotion) > 0;
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
