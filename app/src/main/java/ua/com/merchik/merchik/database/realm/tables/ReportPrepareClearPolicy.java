package ua.com.merchik.merchik.database.realm.tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

public final class ReportPrepareClearPolicy {
    private ReportPrepareClearPolicy() {}

    public enum Status { CLEARED, VISIT_NOT_FOUND, VISIT_FINISHED }

    public static final class Result {
        public final Status status;
        public final List<Long> rowIds;
        public final Set<String> tovarIds;

        private Result(Status status, List<Long> rowIds, Set<String> tovarIds) {
            this.status = status;
            this.rowIds = Collections.unmodifiableList(rowIds);
            this.tovarIds = Collections.unmodifiableSet(tovarIds);
        }

        private static Result refused(Status status) {
            return new Result(status, Collections.emptyList(), Collections.emptySet());
        }
    }

    // The caller must read the visit and apply these edits in the same transaction.
    public static Result clear(long dad2, WpDataDB visit, Iterable<ReportPrepareDB> rows,
                               Set<String> selectedTovarIds, long changedAt) {
        if (dad2 <= 0 || visit == null || visit.getCode_dad2() != dad2) {
            return Result.refused(Status.VISIT_NOT_FOUND);
        }
        if (visit.getVisit_end_dt() > 0 || visit.getClient_end_dt() > 0) {
            return Result.refused(Status.VISIT_FINISHED);
        }

        List<Long> changedIds = new ArrayList<>();
        Set<String> changedTovars = new LinkedHashSet<>();
        String codeDad2 = String.valueOf(dad2);
        for (ReportPrepareDB row : rows) {
            if (row == null || row.getID() == null || !codeDad2.equals(row.getCodeDad2())
                    || !selectedTovarIds.contains(row.getTovarId())) continue;

            // Reset entered requisites only; retain row identity, plan and document metadata.
            row.setPrice("0");
            row.setPriceMin("0");
            row.setPriceMax("0");
            row.setFace("0");
            row.setAmount(0);
            row.setUp("0");
            row.setDtExpire("");
            row.setExpireLeft("0");
            row.setNotes("");
            row.setAkciya("0");
            row.setAkciyaId("0");
            row.setOborotvedNum("0");
            row.oborotved_num_date = "";
            row.setErrorId("0");
            row.setErrorComment("");
            row.setTovarError("0");
            row.setUploadStatus(1);
            row.setDtChange(changedAt);
            changedIds.add(row.getID());
            changedTovars.add(row.getTovarId());
        }
        return new Result(Status.CLEARED, changedIds, changedTovars);
    }
}
