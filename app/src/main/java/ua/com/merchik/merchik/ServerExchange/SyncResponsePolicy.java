package ua.com.merchik.merchik.ServerExchange;

import java.io.IOException;
import java.util.List;

/** Response validation shared by incremental downloads; no database side effects. */
public final class SyncResponsePolicy {
    private SyncResponsePolicy() { }

    public static <T> List<T> requireList(Boolean state, String error, List<T> rows) {
        if (!Boolean.TRUE.equals(state) || (error != null && !error.trim().isEmpty())
                || rows == null || rows.contains(null)) {
            throw new IllegalStateException("Invalid response: state=" + state
                    + ", error=" + shortError(error) + ", rows=" + (rows == null ? "null" : rows.size()));
        }
        return rows;
    }

    public static String shortError(String text) {
        if (text == null) return "null";
        String bounded = text.substring(0, Math.min(text.length(), 256))
                .replaceAll("[\\p{Cntrl}\\u2028\\u2029]", " ");
        return text.length() > 256 ? bounded + "..." : bounded;
    }

    public static final class HttpFailure extends IllegalStateException {
        public final int status;
        public HttpFailure(int status) {
            super("HTTP " + status);
            this.status = status;
        }
    }

    public static boolean shouldRetry(Throwable error, int attempt) {
        if (attempt >= 2) return false;
        boolean networkFailure = false;
        for (int depth = 0; error != null && depth < 16; depth++, error = error.getCause()) {
            if (error instanceof HttpFailure) {
                int status = ((HttpFailure) error).status;
                return status == 408 || status == 429 || (status >= 500 && status <= 599);
            }
            if (error instanceof javax.net.ssl.SSLHandshakeException
                    || error instanceof javax.net.ssl.SSLPeerUnverifiedException
                    || error instanceof com.google.gson.JsonParseException
                    || error instanceof com.google.gson.stream.MalformedJsonException) return false;
            networkFailure |= error instanceof IOException;
        }
        return networkFailure;
    }
}
