package ua.com.merchik.merchik.ServerExchange;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class ExchangeWorkTest {
    @Test public void tracksQueuedSubscriptionAndQueuedDatabaseSave() {
        TestScheduler scheduler = new TestScheduler();
        ExchangeRunTracker.Ticket root = ExchangeWork.TRACKER.beginRun(() -> { });
        Single.just(1).subscribeOn(scheduler).compose(ExchangeWork.track()).subscribe(value -> {
            assertTrue(ExchangeWork.TRACKER.isRunning());
            Completable.complete().subscribeOn(scheduler).compose(ExchangeWork.track())
                    .subscribe(() -> assertTrue(ExchangeWork.TRACKER.isRunning()));
        });
        root.close();
        assertTrue(ExchangeWork.TRACKER.isRunning());
        scheduler.triggerActions();
        assertFalse(ExchangeWork.TRACKER.isRunning());
    }

    @Test public void tracksMainThreadCallbackAfterBackgroundCompletes() {
        TestScheduler main = new TestScheduler();
        ExchangeRunTracker.Ticket root = ExchangeWork.TRACKER.beginRun(() -> { });
        Single.just(1).observeOn(main).compose(ExchangeWork.track()).subscribe(value ->
                assertTrue(ExchangeWork.TRACKER.isRunning()));
        root.close();
        assertTrue(ExchangeWork.TRACKER.isRunning());
        main.triggerActions();
        assertFalse(ExchangeWork.TRACKER.isRunning());
    }

    @Test public void failedRequestReleasesRun() {
        TestScheduler scheduler = new TestScheduler();
        ExchangeRunTracker.Ticket root = ExchangeWork.TRACKER.beginRun(() -> { });
        Single.error(new IllegalStateException("HTTP 500")).subscribeOn(scheduler)
                .compose(ExchangeWork.track()).subscribe(value -> fail(), error -> { });
        root.close();
        scheduler.triggerActions();
        assertFalse(ExchangeWork.TRACKER.isRunning());
    }

    @Test public void cancelledSubscriptionReleasesRun() {
        ExchangeRunTracker.Ticket root = ExchangeWork.TRACKER.beginRun(() -> { });
        Disposable request = Completable.never().compose(ExchangeWork.track()).subscribe();
        root.close();
        assertTrue(ExchangeWork.TRACKER.isRunning());
        request.dispose();
        assertFalse(ExchangeWork.TRACKER.isRunning());
    }

    @Test public void queuedDispatchReservesWorkBeforePosting() {
        List<Runnable> queue = new ArrayList<>();
        ExchangeRunTracker.Ticket root = ExchangeWork.TRACKER.beginRun(() -> { });
        ExchangeWork.dispatch(queue::add, () -> assertTrue(ExchangeWork.TRACKER.isRunning()));
        root.close();
        assertTrue(ExchangeWork.TRACKER.isRunning());
        queue.remove(0).run();
        assertFalse(ExchangeWork.TRACKER.isRunning());
    }

    @Test public void rejectedDispatchDoesNotLeakWork() {
        ExchangeRunTracker.Ticket root = ExchangeWork.TRACKER.beginRun(() -> { });
        try {
            ExchangeWork.dispatch(task -> { throw new IllegalStateException("Rejected"); }, () -> { });
            fail();
        } catch (IllegalStateException expected) { }
        root.close();
        assertFalse(ExchangeWork.TRACKER.isRunning());
    }
}
