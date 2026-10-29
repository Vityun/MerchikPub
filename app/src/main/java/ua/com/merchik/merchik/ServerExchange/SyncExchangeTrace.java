package ua.com.merchik.merchik.ServerExchange;

import java.util.List;
import ua.com.merchik.merchik.Globals;

/** Bounded response metadata, without copying/logging HTTP bodies or row contents. */
public final class SyncExchangeTrace {
    private final String resource;
    private final long from;
    private final long started = System.nanoTime();
    private Boolean state;
    private String error;
    private int rows = -1;

    public SyncExchangeTrace(String resource, long from) {
        this.resource = resource;
        this.from = from;
    }

    public void response(Boolean state, String error, List<?> rows) {
        this.state = state;
        this.error = error;
        this.rows = rows == null ? -1 : rows.size();
    }

    public void saved(long vpi) { finish(vpi, null); }
    public void failed(Throwable failure) { finish(null, failure); }

    private void finish(Long savedVpi, Throwable failure) {
        Globals.writeToMLOG(failure == null ? "INFO" : "ERROR", "SyncExchange/result",
                format(resource, from, savedVpi, state, error, rows,
                        (System.nanoTime() - started) / 1_000_000L, failure));
    }

    public static String format(String resource, long from, Long savedVpi, Boolean state,
                                String error, int rows, long elapsedMs, Throwable failure) {
        return "resource=" + resource + ", from=" + from + ", state=" + state
                + ", error=" + SyncResponsePolicy.shortError(error) + ", rows=" + rows
                + ", elapsedMs=" + Math.max(0, elapsedMs) + ", savedVpi="
                + (savedVpi == null ? "unchanged" : savedVpi)
                + (failure == null ? "" : ", failure=" + SyncResponsePolicy.shortError(failure.toString()));
    }
}
