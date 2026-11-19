package ua.com.merchik.merchik.features.main.options;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.DEFAULT;

import java.util.Arrays;
import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.PhotoEvidencePolicy;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
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
        return requiredCount(document, dad2, option, exactRequiredCount, failOnError, null);
    }

    static int requiredCount(Object document, long dad2, OptionsDB option,
                             Integer exactRequiredCount, boolean failOnError,
                             List<StackPhotoDB> visitPhotos) {
        if (exactRequiredCount != null) return Math.max(0, exactRequiredCount);
        String rawMinimum = option.getAmountMin();
        if (rawMinimum == null || rawMinimum.trim().isEmpty()) rawMinimum = "0";
        String optionId = option.getOptionId();
        WpDataDB visit = document instanceof WpDataDB ? (WpDataDB) document : null;
        int minimum;
        if ("0".equals(rawMinimum)) {
            minimum = PhotoReportCountPolicy.defaultMinimum(optionId, option.getOptionControlId());
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
        minimum = PhotoReportCountPolicy.adjustedMinimum(optionId, minimum,
                visit != null ? visit.getSku() : null, null);
        if (PhotoReportCountPolicy.isWarehouseOption(optionId, option.getOptionControlId())) {
            List<StackPhotoDB> photos = visitPhotos != null
                    ? visitPhotos : StackPhotoRealm.getPhotosByDAD2(dad2, 31);
            int emptyWarehousePhotos = emptyWarehousePhotoCount(photos);
            return PhotoReportCountPolicy.warehouseMinimum(minimum, emptyWarehousePhotos);
        }
        return minimum;
    }

    public static int emptyWarehousePhotoCount(Iterable<StackPhotoDB> photos) {
        int count = 0;
        for (StackPhotoDB photo : photos) {
            if (photo != null && Integer.valueOf(31).equals(photo.getPhoto_type())
                    && PhotoReportCountPolicy.isEmptyWarehouseSample(photo.getExample_id())
                    && PhotoEvidencePolicy.isReadyPhoto(photo.getPhotoServerId(), photo.getPhoto_num())) count++;
        }
        return count;
    }

    public static int completedCount(OptionsDB option, Iterable<StackPhotoDB> photos) {
        if (PhotoReportCountPolicy.isWarehouseOption(option.getOptionId(), option.getOptionControlId())) {
            int emptyWarehousePhotos = emptyWarehousePhotoCount(photos);
            if (emptyWarehousePhotos > 0) return emptyWarehousePhotos;
        }
        int count = 0;
        for (StackPhotoDB photo : photos) {
            if (photo != null && PhotoEvidencePolicy.isReadyPhoto(photo.getPhotoServerId(), photo.getPhoto_num())) count++;
        }
        return count;
    }
}
