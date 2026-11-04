package ua.com.merchik.merchik.Options.Controls;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers.FaceSaveGuard;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

/** Shared entry point for the shelf button and the correction links in control 1465. */
public final class ShelfLengthEntry {
    public static final int BUTTON_ID = 138023;
    private static final String MISSING = "У цьому відвідуванні не знайдено позицію для заміру довжини полиць. Виконайте обмін даними.";

    private ShelfLengthEntry() {}

    public static void removeUncontrolledCopies(List<OptionsDB> buttons) {
        Set<String> controlled = new HashSet<>();
        for (OptionsDB row : buttons) {
            if ("138023".equals(row.getOptionId()) && "1465".equals(row.getOptionControlId()))
                controlled.add(row.getCodeDad2());
        }
        if (!controlled.isEmpty()) buttons.removeIf(row -> "138023".equals(row.getOptionId())
                && "0".equals(row.getOptionControlId()) && controlled.contains(row.getCodeDad2()));
    }

    static Map<String, TovarDB> cards(List<ReportPrepareDB> rows) {
        Map<String, TovarDB> cards = new HashMap<>();
        Set<String> ids = new HashSet<>();
        for (ReportPrepareDB row : rows) if (row.getTovarId() != null) ids.add(row.getTovarId());
        if (!ids.isEmpty()) {
            for (TovarDB product : TovarRealm.getByIds(ids.toArray(new String[0]))) cards.put(product.getiD(), product);
        }
        return cards;
    }

    static boolean eligible(String client, String mainOptionId, ReportPrepareDB row, TovarDB product) {
        if (row == null || client == null || client.isEmpty() || "0".equals(client)) return false;
        if (product != null) return ShelfLengthPolicy.eligible(client, row.getKli(),
                product.getClientId(), product.getManufacturerId());
        return "1465".equals(mainOptionId) && client.equals(row.getKli())
                && ShelfLengthPolicy.UNCATALOGUED_MEASUREMENT_PRODUCT.equals(row.getTovarId());
    }

    static String entryLabel(String productId, TovarDB product) {
        return FaceCountPolicy.productLabel(productId, product == null ? null : product.getNm(),
                product == null ? null : product.getWeight());
    }

    private static Map<String, String> choices(WpDataDB visit) {
        List<ReportPrepareDB> rows = ReportPrepareRealm.getReportPrepareByDad2_LIST(visit.getCode_dad2());
        return choices(rows, cards(rows), visit.getClient_id(), visit.getMain_option_id());
    }

    static Map<String, String> choices(List<ReportPrepareDB> rows, Map<String, TovarDB> cards,
                                       String client, String mainOptionId) {
        Map<String, String> choices = new LinkedHashMap<>();
        for (ReportPrepareDB row : rows) {
            TovarDB product = cards.get(row.getTovarId());
            if (eligible(client, mainOptionId, row, product))
                choices.put(row.getTovarId(), entryLabel(row.getTovarId(), product));
        }
        return choices;
    }

    public static void open(Context ctx, WpDataDB visit, Runnable finished) {
        Map<String, String> choices = choices(visit);
        String[] ids = choices.keySet().toArray(new String[0]);
        if (ids.length == 0) {
            unavailable(ctx);
            finished.run();
        } else if (ids.length == 1) {
            edit(ctx, visit, ids[0], true, finished);
        } else {
            new AlertDialog.Builder(ctx).setTitle("Оберіть товарну групу")
                    .setItems(choices.values().toArray(new String[0]), (dialog, index) ->
                            edit(ctx, visit, ids[index], true, finished))
                    .setNegativeButton("Скасувати", null).show();
        }
    }

    private static void unavailable(Context ctx) {
        DialogData dialog = new DialogData(ctx);
        dialog.setTitle("Довжина поличного простору");
        dialog.setText(MISSING);
        dialog.setClose(dialog::dismiss);
        dialog.setOk("OK", dialog::dismiss);
        dialog.show();
    }

    static void edit(Context ctx, WpDataDB visit, String productId, boolean face, Runnable finished) {
        long dad2 = visit.getCode_dad2();
        ReportPrepareDB current = ReportPrepareRealm.getReportPrepareByTov(String.valueOf(dad2), productId);
        TovarDB product = TovarRealm.getById(productId);
        if (!eligible(visit.getClient_id(), visit.getMain_option_id(), current, product)) {
            unavailable(ctx);
            finished.run();
            return;
        }
        DialogData editor = new DialogData(ctx);
        editor.setTitle(face ? "Общее количество фейсов группы" : "Суммарная длина полок группы (метров)");
        editor.setText(entryLabel(productId, product));
        editor.setClose(editor::dismiss);
        Runnable next = () -> {
            Activity activity = Globals.unwrap(ctx);
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
            if (face) edit(ctx, visit, productId, false, finished);
            else finished.run();
        };
        if (!face) editor.setOnDismissAction(next);
        editor.setCancel("Пропустить", () -> {
            editor.setOnDismissAction(next);
            editor.dismiss();
        });
        editor.setOperation(DialogData.Operations.Number, face ? current.getFace() : String.valueOf(current.getAmount()),
                Collections.emptyMap(), () -> {
                    int value;
                    try {
                        value = new BigDecimal(FaceCountPolicy.faceInput(editor.getOperationResult())).intValueExact();
                    } catch (IllegalArgumentException | ArithmeticException e) {
                        editor.rejectNumberInput("Укажите целое число не меньше 0.");
                        return;
                    }
                    ReportPrepareDB latest = ReportPrepareRealm.getReportPrepareByTov(String.valueOf(dad2), productId);
                    if (!eligible(visit.getClient_id(), visit.getMain_option_id(), latest, TovarRealm.getById(productId))) {
                        editor.rejectNumberInput(MISSING);
                        return;
                    }
                    if (face && FaceSaveGuard.canSaveFace(ctx, WpDataRealm.getWpDataRowByDad2Id(dad2),
                            latest, String.valueOf(value)).isError()) return;
                    if (!ReportPrepareRealm.updateFields(latest, row -> {
                        if (face) row.setFace(String.valueOf(value));
                        else row.setAmount(value);
                        row.setDtChange(System.currentTimeMillis() / 1000);
                        row.setUploadStatus(1);
                    })) {
                        editor.rejectNumberInput("Запись товара не найдена. Обновите список товаров.");
                        return;
                    }
                    editor.setOnDismissAction(next);
                });
        editor.show();
    }
}
