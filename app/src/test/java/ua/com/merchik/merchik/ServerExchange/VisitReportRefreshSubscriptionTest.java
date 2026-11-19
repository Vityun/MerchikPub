package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.assertEquals;

public class VisitReportRefreshSubscriptionTest {
    @Test public void appliedResponseNotifiesOnlyOnce() {
        AtomicInteger calls = new AtomicInteger();
        VisitReportRefresh.Subscription subscription = new VisitReportRefresh.Subscription(calls::incrementAndGet);
        subscription.complete(true);
        subscription.complete(true);
        assertEquals(1, calls.get());
    }

    @Test public void emptyFailedOrSkippedResponseDoesNotRefreshScreen() {
        AtomicInteger calls = new AtomicInteger();
        VisitReportRefresh.Subscription subscription = new VisitReportRefresh.Subscription(calls::incrementAndGet);
        subscription.complete(false);
        subscription.complete(true);
        assertEquals(0, calls.get());
    }

    @Test public void destroyedScreenDoesNotReceiveLateResponse() {
        AtomicInteger calls = new AtomicInteger();
        VisitReportRefresh.Subscription subscription = new VisitReportRefresh.Subscription(calls::incrementAndGet);
        subscription.cancel();
        subscription.complete(true);
        assertEquals(0, calls.get());
    }

    @Test public void cancellingOldScreenDoesNotCancelJoinedScreen() {
        AtomicInteger calls = new AtomicInteger();
        VisitReportRefresh.Subscription oldScreen = new VisitReportRefresh.Subscription(() -> calls.addAndGet(10));
        VisitReportRefresh.Subscription newScreen = new VisitReportRefresh.Subscription(calls::incrementAndGet);
        oldScreen.cancel();
        oldScreen.complete(true);
        newScreen.complete(true);
        assertEquals(1, calls.get());
    }

    @Test public void failingListenerIsReleasedBeforeInvocation() {
        AtomicInteger calls = new AtomicInteger();
        VisitReportRefresh.Subscription subscription = new VisitReportRefresh.Subscription(() -> {
            calls.incrementAndGet();
            throw new IllegalStateException("screen gone");
        });
        try { subscription.complete(true); } catch (IllegalStateException expected) { }
        subscription.complete(true);
        assertEquals(1, calls.get());
    }
}
