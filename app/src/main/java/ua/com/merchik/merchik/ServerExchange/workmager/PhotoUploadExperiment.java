package ua.com.merchik.merchik.ServerExchange.workmager;

import java.util.ArrayList;
import java.util.List;

/** Temporary extra jobs; the five production jobs are never replaced. */
public final class PhotoUploadExperiment {
    public static final String TAG = "photo_upload_experiment";
    public static final String EXPIRES_AT = "experiment_expires_at";
    public static final int MAX_MINUTES = 120;

    private PhotoUploadExperiment() { }

    public static List<Long> additionalOffsetsSeconds(int totalCount) {
        if (totalCount != 5 && totalCount != 50) {
            throw new IllegalArgumentException("Expected 5 or 50 photo upload jobs");
        }
        List<Long> result = new ArrayList<>();
        if (totalCount == 50) {
            for (int slot = 1; slot < 50; slot++) {
                // Every tenth slot corresponds to a production job (0/3/6/9/12 minutes).
                if (slot % 10 != 0) result.add(slot * 18L);
            }
        }
        return result;
    }

    public static long expiresAt(long now, int minutes) {
        if (minutes < 15 || minutes > MAX_MINUTES) {
            throw new IllegalArgumentException("Experiment duration must be 15..120 minutes");
        }
        return now + minutes * 60_000L;
    }

    public static boolean mustStop(boolean debug, long expiresAt, long now) {
        return !debug || expiresAt <= now || expiresAt - now > MAX_MINUTES * 60_000L;
    }
}
