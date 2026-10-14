package ua.com.merchik.merchik.ServerExchange;

import java.util.ArrayDeque;
import java.util.function.Consumer;

/** Access only on one thread. Completion is idempotent, including after a callback throws. */
final class ReportPrepareRequestQueue {
    private final ArrayDeque<Request> pending = new ArrayDeque<>();
    private final Consumer<Runnable> dispatch;
    private boolean running;

    ReportPrepareRequestQueue(Consumer<Runnable> dispatch) {
        this.dispatch = dispatch;
    }

    void submit(Consumer<Runnable> start, Consumer<RuntimeException> onError) {
        pending.add(new Request(start, onError));
        dispatch.accept(this::drain);
    }

    private void drain() {
        if (running || pending.isEmpty()) return;
        running = true;
        Request request = pending.removeFirst();
        boolean[] completed = {false};
        Runnable done = () -> {
            if (completed[0]) return;
            completed[0] = true;
            running = false;
            dispatch.accept(this::drain);
        };
        try {
            request.start.accept(done);
        } catch (RuntimeException e) {
            try { request.onError.accept(e); }
            finally { done.run(); }
        }
    }

    private static final class Request {
        final Consumer<Runnable> start;
        final Consumer<RuntimeException> onError;

        Request(Consumer<Runnable> start, Consumer<RuntimeException> onError) {
            this.start = start;
            this.onError = onError;
        }
    }
}
