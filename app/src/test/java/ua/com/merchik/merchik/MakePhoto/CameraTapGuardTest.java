package ua.com.merchik.merchik.MakePhoto;

import org.junit.Test;
import static org.junit.Assert.*;

public class CameraTapGuardTest {
    private final CameraTapGuard taps = new CameraTapGuard();

    @Test public void slowPreparationAndQueuedTapsCreateOneAttempt() {
        assertTrue(taps.begin(100, true));
        assertFalse(taps.begin(4200, true));
        taps.dispatched(4300);
        assertFalse(taps.begin(4653, true));
        assertFalse(taps.begin(4800, true));
    }

    @Test public void missingCameraResultDoesNotLockOutNextAttempt() {
        assertTrue(taps.begin(100, true));
        taps.dispatched(200);
        assertFalse(taps.begin(1200, false));
        assertTrue(taps.begin(1201, true));
    }

    @Test public void cancelOrFailureAllowsImmediateRetry() {
        assertTrue(taps.begin(100, true));
        taps.reset();
        assertTrue(taps.begin(101, true));
        taps.dispatched(102);
        taps.reset();
        assertTrue(taps.begin(103, true));
    }
}
