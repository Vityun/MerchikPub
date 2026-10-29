package ua.com.merchik.merchik.ServerExchange;

import java.util.Objects;

/** One handoff from startup to the first automatic exchange, never a general cache. */
public final class StartupDownloadReuse<K> {
    private K key;
    private long generation;
    private Long completedAt;

    public synchronized long begin(K key) {
        this.key = key;
        completedAt = null;
        return ++generation;
    }

    public synchronized void succeeded(long ticket, long nowMs) {
        if (generation == ticket && key != null) completedAt = nowMs;
    }

    public synchronized boolean consume(K requested, long nowMs) {
        boolean reuse = Objects.equals(key, requested) && completedAt != null
                && nowMs >= completedAt && nowMs - completedAt < 300_000;
        clear();
        return reuse;
    }

    public synchronized void clear() {
        generation++;
        key = null;
        completedAt = null;
    }
}
