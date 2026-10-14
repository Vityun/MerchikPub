package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class ReportPrepareRequestQueueTest {
    @Test public void uploadAndDownloadWaitForPreviousCommit() {
        ReportPrepareRequestQueue queue = new ReportPrepareRequestQueue(Runnable::run);
        List<String> events = new ArrayList<>();
        Runnable[] complete = new Runnable[2];
        queue.submit(done -> { events.add("upload"); complete[0] = done; }, e -> fail());
        queue.submit(done -> { events.add("download"); complete[1] = done; }, e -> fail());
        queue.submit(done -> { events.add("next upload"); done.run(); }, e -> fail());
        assertEquals(Arrays.asList("upload"), events);
        complete[0].run();
        assertEquals(Arrays.asList("upload", "download"), events);
        // A stale/double completion must not release the active download.
        complete[0].run();
        assertEquals(2, events.size());
        complete[1].run();
        assertEquals(Arrays.asList("upload", "download", "next upload"), events);
    }

    @Test public void setupFailureReleasesQueue() {
        ReportPrepareRequestQueue queue = new ReportPrepareRequestQueue(Runnable::run);
        List<String> events = new ArrayList<>();
        queue.submit(done -> { throw new IllegalStateException("setup"); }, e -> events.add(e.getMessage()));
        queue.submit(done -> { events.add("next"); done.run(); }, e -> fail());
        assertEquals(Arrays.asList("setup", "next"), events);
    }

    @Test public void callbackFailureAfterCompletionCannotReleaseNextRequest() {
        ReportPrepareRequestQueue queue = new ReportPrepareRequestQueue(Runnable::run);
        List<String> events = new ArrayList<>();
        Runnable[] secondDone = new Runnable[1];
        queue.submit(done -> {
            queue.submit(next -> { events.add("second"); secondDone[0] = next; }, e -> fail());
            queue.submit(next -> { events.add("third"); next.run(); }, e -> fail());
            done.run();
            throw new IllegalStateException("callback");
        }, e -> events.add(e.getMessage()));
        assertEquals(Arrays.asList("second", "callback"), events);
        secondDone[0].run();
        assertEquals(Arrays.asList("second", "callback", "third"), events);
    }
}
