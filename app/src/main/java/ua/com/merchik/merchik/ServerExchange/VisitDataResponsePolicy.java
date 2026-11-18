package ua.com.merchik.merchik.ServerExchange;

import java.util.Collections;
import java.util.List;

/** List responses used to initialize visits and their required dictionaries. */
public final class VisitDataResponsePolicy {
    private static final String EMPTY_LIST = "\u0421\u043f\u0438\u0441\u043e\u043a \u043f\u0443\u0441\u0442";

    private VisitDataResponsePolicy() { }

    public static <T> List<T> requireList(int httpCode, Boolean state, String error, List<T> rows) {
        if (httpCode < 200 || httpCode >= 300) throw new SyncResponsePolicy.HttpFailure(httpCode);
        // The legacy API reports an empty selection as an error, sometimes without state/list.
        // This completes readiness only; an empty delta must never advance a download VPI.
        if (error != null && EMPTY_LIST.equals(error.trim()) && (rows == null || rows.isEmpty())) {
            return Collections.emptyList();
        }
        return SyncResponsePolicy.requireList(state, error, rows);
    }

    public static <T> List<T> requireDictionary(int httpCode, Boolean state, String error,
                                               List<T> rows, long localCount) {
        List<T> result = requireList(httpCode, state, error, rows);
        if (result.isEmpty() && localCount <= 0) {
            throw new IllegalStateException("Empty initial dictionary");
        }
        return result;
    }
}
