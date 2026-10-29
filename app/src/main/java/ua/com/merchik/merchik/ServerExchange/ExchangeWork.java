package ua.com.merchik.merchik.ServerExchange;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.core.CompletableTransformer;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.core.SingleTransformer;

public final class ExchangeWork {
    public static final ExchangeRunTracker TRACKER = new ExchangeRunTracker();

    private ExchangeWork() { }

    public static void dispatch(java.util.concurrent.Executor executor, Runnable action) {
        ExchangeRunTracker.Ticket ticket = TRACKER.beginWork();
        try {
            executor.execute(() -> {
                try { action.run(); }
                finally { ticket.close(); }
            });
        } catch (RuntimeException | Error error) {
            ticket.close();
            throw error;
        }
    }

    // Compose last, after observeOn/subscribeOn: queued work and downstream saves must finish first.
    public static <T> Tracking<T> track() {
        return new Tracking<>();
    }

    public static final class Tracking<T> implements CompletableTransformer,
            SingleTransformer<T, T>, ObservableTransformer<T, T> {
        @Override
        public CompletableSource apply(Completable upstream) {
            return Completable.defer(() -> {
                ExchangeRunTracker.Ticket ticket = TRACKER.beginWork();
                return upstream.doFinally(ticket::close);
            });
        }

        @Override
        public SingleSource<T> apply(Single<T> upstream) {
            return Single.defer(() -> {
                ExchangeRunTracker.Ticket ticket = TRACKER.beginWork();
                return upstream.doFinally(ticket::close);
            });
        }

        @Override
        public ObservableSource<T> apply(Observable<T> upstream) {
            return Observable.defer(() -> {
                ExchangeRunTracker.Ticket ticket = TRACKER.beginWork();
                return upstream.doFinally(ticket::close);
            });
        }
    }
}
