package ua.com.merchik.merchik.Utils;

import android.os.SystemClock;
import android.util.Log;

import ua.com.merchik.merchik.Globals;

public final class TrustedTime {

    private static final String TAG = "TrustedTime";
    private static final long MIN_VALID_SERVER_UNIX_SEC = 1_700_000_000L;
    private static final long FUTURE_WATERMARK_TOLERANCE_SEC = 300L;
    private static final Object LOCK = new Object();

    private static long serverUnixSec;
    private static long elapsedAtReceiveMs;
    private static long localWallAtReceiveSec;

    private TrustedTime() {
    }

    public static void updateFromServer(long serverTimeSec) {
        if (serverTimeSec < MIN_VALID_SERVER_UNIX_SEC) {
            Globals.writeToMLOG("WARN", TAG + "/updateFromServer", "Invalid serverTimeSec=" + serverTimeSec);
            return;
        }

        long elapsed = SystemClock.elapsedRealtime();
        long localSec = System.currentTimeMillis() / 1000L;
        long skewSec = serverTimeSec - localSec;

        synchronized (LOCK) {
            serverUnixSec = serverTimeSec;
            elapsedAtReceiveMs = elapsed;
            localWallAtReceiveSec = localSec;
        }

        Log.e(TAG, "server=" + serverTimeSec + ", local=" + localSec + ", skewSec=" + skewSec);
        if (Math.abs(skewSec) > 60L) {
            Globals.writeToMLOG(
                    "INFO",
                    TAG + "/updateFromServer",
                    "server=" + serverTimeSec + ", local=" + localSec + ", skewSec=" + skewSec
            );
        }
    }

    public static Long nowServerSecOrNull() {
        synchronized (LOCK) {
            if (serverUnixSec <= 0L || elapsedAtReceiveMs <= 0L) {
                return null;
            }

            long deltaSec = Math.max(0L, (SystemClock.elapsedRealtime() - elapsedAtReceiveMs) / 1000L);
            return serverUnixSec + deltaSec;
        }
    }

    public static long nowServerSecOrLocalSec() {
        Long trustedNow = nowServerSecOrNull();
        return trustedNow != null ? trustedNow : System.currentTimeMillis() / 1000L;
    }

    public static long syncWatermarkSec(long previousWatermarkSec, long overlapSec) {
        Long trustedNow = nowServerSecOrNull();
        if (trustedNow == null) {
            Globals.writeToMLOG(
                    "WARN",
                    TAG + "/syncWatermarkSec",
                    "No trusted server time. Keep previousWatermarkSec=" + previousWatermarkSec
            );
            return Math.max(0L, previousWatermarkSec);
        }

        long safeOverlapSec = Math.max(0L, overlapSec);
        long candidate = Math.max(0L, trustedNow - safeOverlapSec);

        if (previousWatermarkSec > candidate + 300L) {
            Globals.writeToMLOG(
                    "WARN",
                    TAG + "/syncWatermarkSec",
                    "Correct future watermark: previous=" + previousWatermarkSec
                            + ", trustedNow=" + trustedNow
                            + ", overlap=" + safeOverlapSec
                            + ", candidate=" + candidate
                            + ", localAtPing=" + localWallAtReceiveSec
            );
        }

        return candidate;
    }

    public static boolean isSyncDue(long lastWatermarkSec, long syncPeriodSec) {
        long now = nowServerSecOrLocalSec();
        long safePeriodSec = Math.max(0L, syncPeriodSec);

        if (lastWatermarkSec > now + FUTURE_WATERMARK_TOLERANCE_SEC) {
            Globals.writeToMLOG(
                    "WARN",
                    TAG + "/isSyncDue",
                    "Future watermark detected: last=" + lastWatermarkSec
                            + ", now=" + now
                            + ", period=" + safePeriodSec
            );
            return true;
        }

        return now >= Math.max(0L, lastWatermarkSec) + safePeriodSec;
    }

    public static long secondsUntilSync(long lastWatermarkSec, long syncPeriodSec) {
        long now = nowServerSecOrLocalSec();
        return Math.max(0L, Math.max(0L, lastWatermarkSec) + Math.max(0L, syncPeriodSec) - now);
    }
}
