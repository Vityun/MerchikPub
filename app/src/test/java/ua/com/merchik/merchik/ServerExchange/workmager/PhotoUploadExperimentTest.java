package ua.com.merchik.merchik.ServerExchange.workmager;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class PhotoUploadExperimentTest {
    @Test public void baselineAddsNoJobs() {
        assertTrue(PhotoUploadExperiment.additionalOffsetsSeconds(5).isEmpty());
    }

    @Test public void fiftyMeansFortyFiveAdditionalJobs() {
        List<Long> offsets = PhotoUploadExperiment.additionalOffsetsSeconds(50);
        assertEquals(45, offsets.size());
        assertEquals(45, new HashSet<>(offsets).size());
        for (int slot = 0; slot < 5; slot++) {
            assertFalse(offsets.contains(slot * 180L));
        }
        assertEquals(Long.valueOf(18), offsets.get(0));
        assertEquals(Long.valueOf(882), offsets.get(offsets.size() - 1));
    }

    @Test(expected = IllegalArgumentException.class) public void cannotAddUnboundedJobs() {
        PhotoUploadExperiment.additionalOffsetsSeconds(5000);
    }

    @Test public void defaultSessionExpiresAfterFortyFiveMinutes() {
        assertEquals(1000 + 45 * 60_000L, PhotoUploadExperiment.expiresAt(1000, 45));
    }

    @Test(expected = IllegalArgumentException.class) public void cannotRunForever() {
        PhotoUploadExperiment.expiresAt(1000, 121);
    }

    @Test(expected = IllegalArgumentException.class) public void refusesTooShortSession() {
        PhotoUploadExperiment.expiresAt(1000, 0);
    }

    @Test public void releaseBuildNeverRunsTestJobs() {
        assertTrue(PhotoUploadExperiment.mustStop(false, 10000, 1000));
    }

    @Test public void expiresAtBoundaryAndRejectsMissingDeadline() {
        assertFalse(PhotoUploadExperiment.mustStop(true, 10000, 9999));
        assertTrue(PhotoUploadExperiment.mustStop(true, 10000, 10000));
        assertTrue(PhotoUploadExperiment.mustStop(true, 0, 1000));
    }

    @Test public void clockRollbackCannotExtendTestBeyondLimit() {
        assertTrue(PhotoUploadExperiment.mustStop(true, 1000 + 121 * 60_000L, 1000));
    }
}
