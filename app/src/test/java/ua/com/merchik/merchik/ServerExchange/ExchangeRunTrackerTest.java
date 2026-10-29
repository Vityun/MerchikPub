package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class ExchangeRunTrackerTest {
    @Test public void dispatchAloneCompletesOnce() {
        ExchangeRunTracker tracker = new ExchangeRunTracker();
        AtomicInteger completed = new AtomicInteger();
        ExchangeRunTracker.Ticket root = tracker.beginRun(completed::incrementAndGet);
        assertTrue(tracker.isRunning());
        assertNull(tracker.beginRun(() -> fail("Duplicate run")));
        root.close();
        root.close();
        assertFalse(tracker.isRunning());
        assertEquals(1, completed.get());
    }

    @Test public void waitsForNetworkThenDatabaseContinuation() {
        ExchangeRunTracker tracker = new ExchangeRunTracker();
        ExchangeRunTracker.Ticket root = tracker.beginRun(() -> { });
        ExchangeRunTracker.Ticket http = tracker.beginWork();
        root.close();
        ExchangeRunTracker.Ticket database = tracker.beginWork();
        http.close();
        assertTrue(tracker.isRunning());
        assertNull(tracker.beginRun(() -> { }));
        database.close();
        assertFalse(tracker.isRunning());
    }

    @Test public void includesAlreadyQueuedInitialExchangeWork() {
        ExchangeRunTracker tracker = new ExchangeRunTracker();
        ExchangeRunTracker.Ticket initial = tracker.beginWork();
        tracker.beginRun(() -> { }).close();
        assertTrue(tracker.isRunning());
        initial.close();
        assertFalse(tracker.isRunning());
    }

    @Test public void lateDuplicateCloseCannotFinishNextRun() {
        ExchangeRunTracker tracker = new ExchangeRunTracker();
        ExchangeRunTracker.Ticket first = tracker.beginRun(() -> { });
        first.close();
        ExchangeRunTracker.Ticket second = tracker.beginRun(() -> { });
        first.close();
        assertTrue(tracker.isRunning());
        second.close();
    }

    @Test public void onlyOneConcurrentCallerStarts() throws Exception {
        ExchangeRunTracker tracker = new ExchangeRunTracker();
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger winners = new AtomicInteger();
        Thread[] threads = new Thread[16];
        ExchangeRunTracker.Ticket[] roots = new ExchangeRunTracker.Ticket[16];
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try { start.await(); }
                catch (InterruptedException e) { throw new AssertionError(e); }
                roots[index] = tracker.beginRun(() -> { });
                if (roots[index] != null) winners.incrementAndGet();
            });
            threads[i].start();
        }
        start.countDown();
        for (Thread thread : threads) thread.join();
        assertEquals(1, winners.get());
        for (ExchangeRunTracker.Ticket root : roots) if (root != null) root.close();
        assertFalse(tracker.isRunning());
    }

    @Test public void completionCanStartNextRun() {
        ExchangeRunTracker tracker = new ExchangeRunTracker();
        ExchangeRunTracker.Ticket[] next = {null};
        tracker.beginRun(() -> next[0] = tracker.beginRun(() -> { })).close();
        assertNotNull(next[0]);
        assertTrue(tracker.isRunning());
        next[0].close();
    }
}
