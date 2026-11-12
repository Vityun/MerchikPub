package ua.com.merchik.merchik.ServerExchange;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.merchik.merchik.data.QuestionAnswerDB;
import ua.com.merchik.merchik.data.UploadToServ.QuestionAnswerUpload;
import ua.com.merchik.merchik.data.UploadToServ.QuestionAnswerUploadResponse;
import ua.com.merchik.merchik.data.UploadToServ.QuestionAnswerUploadResponseList;

/** Upload acknowledgements, not the quest_data download watermark. */
public final class QuestionAnswerAckPolicy {
    public static final long LOCAL_ID_MIN = 100_000_000_000L;
    private QuestionAnswerAckPolicy() { }

    public static long uploadedThrough(long legacyVpi, long uploadAck) {
        // Older versions stored local millisecond IDs in VPI. Do not mistake a download
        // timestamp in seconds for an upload acknowledgement, and never advance that VPI.
        return Math.max(uploadAck, legacyVpi >= LOCAL_ID_MIN ? legacyVpi : 10_000_000L);
    }

    public static boolean isConfirmed(long id, long uploadedThrough) {
        // Local answers use System.currentTimeMillis(); downloaded rows have server IDs.
        return id > 0 && (id < LOCAL_ID_MIN || id <= uploadedThrough);
    }

    public static List<Long> pendingReleasingIds(List<QuestionAnswerDB> answers, long uploadedThrough) {
        List<Long> pending = new ArrayList<>();
        for (QuestionAnswerDB answer : answers) {
            // One confirmed qualifying answer already satisfies the control.
            if (isConfirmed(answer.getId(), uploadedThrough)) return new ArrayList<>();
            pending.add(answer.getId());
        }
        return pending;
    }

    public static long acknowledgedThrough(long previous, List<QuestionAnswerUpload> sent,
                                           QuestionAnswerUploadResponse response) {
        if (response == null || !Boolean.TRUE.equals(response.state) || response.list == null) {
            return previous;
        }
        Set<String> accepted = new HashSet<>();
        Set<String> rejected = new HashSet<>();
        for (QuestionAnswerUploadResponseList item : response.list) {
            if (item == null || item.elementId == null) continue;
            if (Boolean.FALSE.equals(item.state) || (item.error != null && !item.error.isEmpty())) {
                rejected.add(item.elementId);
            } else {
                accepted.add(item.elementId);
            }
        }
        long through = previous;
        // The upload query is ordered by ID. Never jump over an unconfirmed row.
        for (QuestionAnswerUpload item : sent) {
            if (!accepted.contains(item.elementId) || rejected.contains(item.elementId)) break;
            long id = Long.parseLong(item.elementId);
            if (id > through) through = id;
        }
        return through;
    }
}
