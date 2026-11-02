package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public final class PromotionRequisite {
    private PromotionRequisite() {}

    public static Map<Integer, String> types() {
        Map<Integer, String> types = new LinkedHashMap<>();
        types.put(0, "Оберіть тип акції");
        for (PromoDB promo : RealmManager.getAllPromoDb()) {
            int id = choiceId(promo.getID());
            if (id > 0 && promo.getNm() != null && !promo.getNm().isEmpty()) {
                types.put(id, promo.getNm());
            }
        }
        return types;
    }

    public static Map<Integer, String> presence() {
        Map<Integer, String> presence = new LinkedHashMap<>();
        presence.put(0, "Оберіть наявність акції");
        presence.put(1, "Есть акция");
        presence.put(2, "Акция отсутствует");
        return presence;
    }

    public static void configureSelectors(DialogData dialog, ReportPrepareDB row) {
        Map<Integer, String> types = types();
        Map<Integer, String> presence = presence();
        dialog.setOperationSpinnerData(types);
        dialog.setOperationSpinner2Data(presence);
        dialog.setOperationTextData(types.get(choiceId(row.getAkciyaId())));
        dialog.setOperationTextData2(presence.get(choiceId(row.getAkciya())));
    }

    private static int choiceId(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static boolean save(ReportPrepareDB snapshot, String typeId, String presence) {
        String[] missingPrice = {null};
        long changedAt = System.currentTimeMillis() / 1000;
        return ReportPrepareRealm.updateFields(snapshot, current -> {
            // Calculate once from the live row, then mirror the same edit to the UI snapshot.
            missingPrice[0] = PriceSaveGuard.missingPricesForNoPromotion(presence, current.getPrice(),
                    current.getPriceMin(), current.getPriceMax());
            return true;
        }, row -> {
            row.setAkciyaId(typeId == null ? "" : typeId);
            row.setAkciya(presence == null ? "" : presence);
            if (missingPrice[0] != null) {
                row.setPriceMin(missingPrice[0]);
                row.setPriceMax(missingPrice[0]);
            }
            row.setUploadStatus(1);
            row.setDtChange(changedAt);
        });
    }
}
