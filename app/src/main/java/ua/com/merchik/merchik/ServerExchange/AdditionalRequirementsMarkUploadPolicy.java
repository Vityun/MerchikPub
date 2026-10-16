package ua.com.merchik.merchik.ServerExchange;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirementsMarks.AdditionalRequirementsMarksListServerData;

/** Selection and acknowledgement rules, independent of Realm transactions and HTTP. */
public final class AdditionalRequirementsMarkUploadPolicy {
    public static final String SUPERSEDED = "superseded";

    private AdditionalRequirementsMarkUploadPolicy() { }

    public static boolean isPending(AdditionalRequirementsMarkDB row) {
        return row != null && "0".equals(row.getUploadStatus());
    }

    public static boolean hasIdentity(AdditionalRequirementsMarkDB row) {
        return row != null && row.getId() != null && !row.getId().isEmpty()
                && row.getDt() != null && row.getItemId() != null && row.getItemId() > 0
                && row.getUserId() != null && !row.getUserId().isEmpty()
                && ("0".equals(row.getTp()) || "1".equals(row.getTp()));
    }

    public static List<AdditionalRequirementsMarkDB> latestPending(List<AdditionalRequirementsMarkDB> rows) {
        Map<String, AdditionalRequirementsMarkDB> latest = new LinkedHashMap<>();
        for (AdditionalRequirementsMarkDB row : rows) {
            if (!isPending(row) || !hasIdentity(row)) continue;
            String key = row.getUserId() + ":" + row.getTp() + ":" + row.getItemId();
            AdditionalRequirementsMarkDB previous = latest.get(key);
            if (previous == null || compareNewestFirst(row, previous) < 0) latest.put(key, row);
        }
        return new ArrayList<>(latest.values());
    }

    public static List<AdditionalRequirementsMarkDB> nextBatch(List<AdditionalRequirementsMarkDB> rows, int userId) {
        if (userId <= 0) return Collections.emptyList();
        List<AdditionalRequirementsMarkDB> result = new ArrayList<>();
        String type = null;
        for (AdditionalRequirementsMarkDB row : latestPending(rows)) {
            if (!String.valueOf(userId).equals(row.getUserId())) continue;
            // The response echoes only itemId, so two types with the same itemId cannot share a batch.
            if (type == null) type = row.getTp();
            if (type.equals(row.getTp())) result.add(row);
        }
        return result;
    }

    public static int compareNewestFirst(AdditionalRequirementsMarkDB left, AdditionalRequirementsMarkDB right) {
        int byDate = Long.compare(right.getDt() == null ? 0 : right.getDt(),
                left.getDt() == null ? 0 : left.getDt());
        if (byDate != 0) return byDate;
        String leftId = left.getId() == null ? "" : left.getId();
        String rightId = right.getId() == null ? "" : right.getId();
        int byId = numericId(rightId).compareTo(numericId(leftId));
        return byId != 0 ? byId : rightId.compareTo(leftId);
    }

    private static BigInteger numericId(String id) {
        try {
            return new BigInteger(id);
        } catch (NumberFormatException ignored) {
            return BigInteger.ZERO;
        }
    }

    public static boolean wasAccepted(Integer itemId, List<AdditionalRequirementsMarksListServerData> response) {
        if (itemId == null || response == null) return false;
        boolean accepted = false;
        for (AdditionalRequirementsMarksListServerData row : response) {
            if (row == null || !itemId.equals(row.elementId)) continue;
            // Do not clear a row on an ambiguous or incomplete acknowledgement.
            if (!Boolean.TRUE.equals(row.state)) return false;
            accepted = true;
        }
        return accepted;
    }

    public static boolean canAcknowledge(AdditionalRequirementsMarkDB sent, AdditionalRequirementsMarkDB current) {
        return hasIdentity(sent) && isPending(current)
                && Objects.equals(sent.getId(), current.getId())
                && Objects.equals(sent.getDt(), current.getDt())
                && Objects.equals(sent.getUserId(), current.getUserId())
                && Objects.equals(sent.getItemId(), current.getItemId())
                && Objects.equals(sent.getTp(), current.getTp())
                && Objects.equals(sent.getScore(), current.getScore())
                && Objects.equals(sent.comment, current.comment);
    }
}
