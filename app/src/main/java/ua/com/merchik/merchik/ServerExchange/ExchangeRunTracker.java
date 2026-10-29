package ua.com.merchik.merchik.ServerExchange;

import java.util.concurrent.atomic.AtomicBoolean;

/** Counts finite metadata requests and their database work, including queued continuations. */
public final class ExchangeRunTracker {
    private int pending;
    private boolean running;
    private Runnable completion;

    public synchronized Ticket beginRun(Runnable onComplete) {
        if (running) return null;
        running = true;
        completion = onComplete;
        return beginWork();
    }

    public synchronized Ticket beginWork() {
        pending++;
        return new Ticket();
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public final class Ticket implements AutoCloseable {
        private final AtomicBoolean closed = new AtomicBoolean();

        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) return;
            Runnable done = null;
            synchronized (ExchangeRunTracker.this) {
                pending--;
                if (pending == 0 && running) {
                    running = false;
                    done = completion;
                    completion = null;
                }
            }
            if (done != null) done.run();
        }
    }
}
