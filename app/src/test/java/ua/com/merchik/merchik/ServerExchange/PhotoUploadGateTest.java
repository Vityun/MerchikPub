package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class PhotoUploadGateTest {
    @Test public void onlyOneBatchCanRun() {
        PhotoUploadGate gate = new PhotoUploadGate();
        PhotoUploadGate.Lease first = gate.tryBatch();
        assertNotNull(first);
        assertNull(gate.tryBatch());
        first.close();
        assertNotNull(gate.tryBatch());
    }

    @Test public void manualUploadPreventsBackgroundBatch() {
        PhotoUploadGate gate = new PhotoUploadGate();
        PhotoUploadGate.Lease photo = gate.tryPhoto(1, "photo.jpg");
        assertNull(gate.tryBatch());
        photo.close();
        assertNotNull(gate.tryBatch());
    }

    @Test public void samePhotoCannotUploadAcrossDifferentPaths() {
        PhotoUploadGate gate = new PhotoUploadGate();
        PhotoUploadGate.Lease first = gate.tryPhoto(1, "old.jpg");
        assertNull(gate.tryPhoto(1, "new.jpg"));
        first.close();
        assertNotNull(gate.tryPhoto(1, "new.jpg"));
    }

    @Test public void duplicateRowsSharingFileCannotUploadTogether() {
        PhotoUploadGate gate = new PhotoUploadGate();
        assertNotNull(gate.tryPhoto(1, "photo.jpg"));
        assertNull(gate.tryPhoto(2, "photo.jpg"));
        assertNotNull(gate.tryPhoto(2, "other.jpg"));
    }

    @Test public void missingPathsDoNotMakeDifferentPhotosTheSame() {
        PhotoUploadGate gate = new PhotoUploadGate();
        assertNotNull(gate.tryPhoto(1, null));
        assertNotNull(gate.tryPhoto(2, ""));
        assertNull(gate.tryPhoto(1, ""));
    }

    @Test public void staleReleaseCannotUnlockNewOwner() {
        PhotoUploadGate gate = new PhotoUploadGate();
        PhotoUploadGate.Lease old = gate.tryPhoto(1, "photo.jpg");
        old.close();
        PhotoUploadGate.Lease next = gate.tryPhoto(1, "photo.jpg");
        old.close();
        assertNull(gate.tryPhoto(1, "photo.jpg"));
        next.close();
        assertNotNull(gate.tryPhoto(1, "photo.jpg"));
    }

    @Test public void staleBatchReleaseCannotUnlockNewBatch() {
        PhotoUploadGate gate = new PhotoUploadGate();
        PhotoUploadGate.Lease old = gate.tryBatch();
        old.close();
        assertNotNull(gate.tryBatch());
        old.close();
        assertNull(gate.tryBatch());
    }

    @Test public void batchReleaseDoesNotUnlockInFlightPhoto() {
        PhotoUploadGate gate = new PhotoUploadGate();
        PhotoUploadGate.Lease batch = gate.tryBatch();
        PhotoUploadGate.Lease photo = gate.tryPhoto(1, "photo.jpg");
        batch.close();
        assertNull(gate.tryBatch());
        assertNull(gate.tryPhoto(1, "photo.jpg"));
        photo.close();
        assertNotNull(gate.tryBatch());
    }

    @Test public void competingThreadsAcquirePhotoOnlyOnce() throws Exception {
        PhotoUploadGate gate = new PhotoUploadGate();
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger acquired = new AtomicInteger();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Thread thread = new Thread(() -> {
                try {
                    start.await();
                    if (gate.tryPhoto(1, "photo.jpg") != null) acquired.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(thread);
            thread.start();
        }
        start.countDown();
        for (Thread thread : threads) thread.join();
        assertEquals(1, acquired.get());
    }
}
