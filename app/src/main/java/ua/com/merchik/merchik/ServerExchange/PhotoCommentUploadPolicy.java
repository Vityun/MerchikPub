package ua.com.merchik.merchik.ServerExchange;

/** Main-thread request ownership and retry timing; pending text stays in Realm. */
final class PhotoCommentUploadPolicy {
    static final long RETRY_MS = 30_000L;
    private int userId;
    private long revision;
    private long retryAfter;
    private boolean running;

    boolean useUser(int user) {
        if (userId == user) return false;
        userId = user;
        revision++;
        running = false;
        retryAfter = 0;
        return true;
    }

    long start(long now) {
        if (userId <= 0 || running || remaining(now) > 0) return 0;
        running = true;
        return ++revision;
    }

    boolean owns(long request) {
        return running && request == revision;
    }

    boolean finish(long request, boolean accepted, long now) {
        if (!owns(request)) return false;
        running = false;
        retryAfter = accepted ? 0 : now + RETRY_MS;
        return true;
    }

    boolean isRunning() {
        return running;
    }

    long remaining(long now) {
        return Math.max(0, retryAfter - now);
    }
}
