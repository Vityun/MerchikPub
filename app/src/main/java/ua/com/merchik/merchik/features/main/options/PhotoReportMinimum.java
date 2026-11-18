package ua.com.merchik.merchik.features.main.options;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.DEFAULT;

import java.util.Arrays;
import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;

public final class PhotoReportMinimum {
    private PhotoReportMinimum() { }

    public static int requiredCount(Object document, long dad2, OptionsDB option,
                                    Integer exactRequiredCount) {
        return requiredCount(document, dad2, option, exactRequiredCount, false);
    }

    public static int requiredCount(Object document, long dad2, OptionsDB option,
                                    Integer exactRequiredCount, boolean failOnError) {
        if (exactRequiredCount != null) return Math.max(0, exactRequiredCount);
        String rawMinimum = option.getAmountMin();
        if (rawMinimum == null || rawMinimum.trim().isEmpty()) rawMinimum = "0";
        String optionId = option.getOptionId();
        WpDataDB visit = document instanceof WpDataDB ? (WpDataDB) document : null;
        int minimum;
        if ("0".equals(rawMinimum)) {
            minimum = PhotoReportCountPolicy.defaultMinimum(optionId, option.getOptionControlId());
            if ("141360".equals(optionId)
                    && StackPhotoRealm.getPhotosByDAD2(dad2, 31).where()
                    .equalTo("example_id", "78").count() > 0) minimum = 2;
            try {
                if ("157277".equals(optionId)) {
                    List<ReportPrepareDB> report = RealmManager.INSTANCE.copyFromRealm(
                            ReportPrepareRealm.getReportPrepareByDad2(dad2));
                    List<AdditionalRequirementsDB> requirements = AdditionalRequirementsRealm.getData3(
                            visit, DEFAULT, null, null, 0);
                    String[] productIds = new String[requirements.size()];
                    for (int i = 0; i < requirements.size(); i++) {
                        productIds[i] = requirements.get(i).getTovarId();
                    }
                    int count = 0;
                    for (ReportPrepareDB row : report) {
                        if (row.akciyaId != null && !row.akciyaId.isEmpty()
                                && Arrays.asList(productIds).contains(row.getTovarId())) count++;
                    }
                    if (!requirements.isEmpty()) minimum = count;
                }
                if (visit != null && ("164352".equals(optionId) || "164351".equals(optionId))) {
                    AddressSDB address = SQL_DB.addressDao().getById(visit.getAddr_id());
                    if ("77190".equals(visit.getClient_id()) && address != null) {
                        int checkouts = address.kolKass != null ? address.kolKass : 0;
                        int selfCheckouts = address.kolKassSo != null ? address.kolKassSo : 0;
                        minimum = Math.max(0, checkouts - selfCheckouts);
                    }
                }
            } catch (Exception error) {
                Globals.writeToMLOG("ERROR", "PhotoReportMinimum",
                        "dad2=" + dad2 + ", option=" + optionId + ", error=" + error);
                // Keep the counter's existing fallback; capture must not close on an incomplete calculation.
                if (failOnError) throw new IllegalStateException("Cannot resolve photo minimum", error);
            }
        } else {
            minimum = Integer.parseInt(rawMinimum);
        }
        return PhotoReportCountPolicy.adjustedMinimum(optionId, minimum,
                visit != null ? visit.getSku() : null, null);
    }
}
