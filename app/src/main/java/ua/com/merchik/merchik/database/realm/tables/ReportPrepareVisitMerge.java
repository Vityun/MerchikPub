package ua.com.merchik.merchik.database.realm.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;

/** Validates the entire targeted response before changing any row or primary key. */
final class ReportPrepareVisitMerge {
    static final class Change {
        final ReportPrepareDB server;
        final ReportPrepareDB pending;
        final List<Long> obsoleteIds;

        Change(ReportPrepareDB server, ReportPrepareDB pending, List<Long> obsoleteIds) {
            this.server = server;
            this.pending = pending;
            this.obsoleteIds = obsoleteIds;
        }

        ReportPrepareDB apply() {
            if (pending == null) {
                server.setUploadStatus(0);
            } else {
                // Keep the complete upload payload, while accepting new server-only plan fields.
                server.setDtChange(pending.getDtChange());
                server.setDtReport(pending.getDtReport());
                server.setKli(pending.getKli());
                server.setAddrId(pending.getAddrId());
                server.setPrice(pending.getPrice());
                server.setFace(pending.getFace());
                server.setAmount(pending.getAmount());
                server.setDtExpire(pending.getDtExpire());
                server.setExpireLeft(pending.getExpireLeft());
                server.setNotes(pending.getNotes());
                server.setUp(pending.getUp());
                server.setAkciya(pending.getAkciya());
                server.setAkciyaId(pending.getAkciyaId());
                server.setOborotvedNum(pending.getOborotvedNum());
                server.setErrorId(pending.getErrorId());
                server.setErrorComment(pending.getErrorComment());
                server.buyerOrderId = pending.buyerOrderId;
                server.setPriceMin(pending.getPriceMin());
                server.setPriceMax(pending.getPriceMax());
                server.setUploadStatus(1);
            }
            return server;
        }
    }

    private ReportPrepareVisitMerge() { }

    static List<Change> plan(long dad2, List<ReportPrepareDB> local, List<ReportPrepareDB> incoming) {
        if (dad2 <= 0 || incoming == null) throw new IllegalArgumentException("Invalid visit response");
        String visit = String.valueOf(dad2);
        Map<Long, ReportPrepareDB> byId = new HashMap<>();
        Map<String, List<ReportPrepareDB>> byProduct = new HashMap<>();
        for (ReportPrepareDB row : local) {
            byId.put(row.getID(), row);
            if (visit.equals(row.getCodeDad2()))
                byProduct.computeIfAbsent(row.getTovarId(), key -> new ArrayList<>()).add(row);
        }
        Set<Long> ids = new HashSet<>();
        Set<String> products = new HashSet<>();
        List<Change> result = new ArrayList<>();
        for (ReportPrepareDB row : incoming) {
            if (row == null || row.getID() == null || row.getID() <= 0
                    || !visit.equals(row.getCodeDad2()) || row.getTovarId() == null
                    || row.getTovarId().trim().isEmpty())
                throw new IllegalArgumentException("Invalid/out-of-scope RP row, dad2=" + dad2);
            if (!ids.add(row.getID()) || !products.add(row.getTovarId()))
                throw new IllegalArgumentException("Ambiguous RP response, dad2=" + dad2
                        + ", id=" + row.getID() + ", tovar=" + row.getTovarId());
            ReportPrepareDB sameId = byId.get(row.getID());
            if (sameId != null && (!visit.equals(sameId.getCodeDad2())
                    || !Objects.equals(row.getTovarId(), sameId.getTovarId())))
                throw new IllegalArgumentException("RP ID belongs to another product/visit, id=" + row.getID());
            ReportPrepareDB pending = null;
            List<Long> obsolete = new ArrayList<>();
            List<ReportPrepareDB> matches = byProduct.get(row.getTovarId());
            if (matches != null) {
                for (ReportPrepareDB existing : matches) {
                    if (existing.getUploadStatus() == 1) {
                        if (pending != null)
                            throw new IllegalArgumentException("Multiple pending RP rows, dad2=" + dad2
                                    + ", tovar=" + row.getTovarId());
                        pending = existing;
                    }
                    if (!Objects.equals(existing.getID(), row.getID())) obsolete.add(existing.getID());
                }
            }
            result.add(new Change(row, pending, obsolete));
        }
        // Absence from this endpoint is not a deletion marker. Unreturned local rows stay intact.
        return result;
    }
}
