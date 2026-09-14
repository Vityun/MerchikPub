package ua.com.merchik.merchik.data.synchronization;

import org.junit.Test;

import static org.junit.Assert.*;

public class StartupPolicyTest {
    @Test public void offlineLoginNeverShowsMandatoryLoading() {
        boolean offline = StartupPolicy.isOffline(true, true, true, true);
        assertTrue(offline);
        assertFalse(StartupPolicy.shouldShowLoading(false, offline));
    }

    @Test public void failedServerAndDisconnectedNetworkAreOffline() {
        assertTrue(StartupPolicy.isOffline(false, true, true, false));
        assertTrue(StartupPolicy.isOffline(false, false, true, true));
    }

    @Test public void unknownServerStatusIsNotMistakenForAnOutage() {
        assertFalse(StartupPolicy.isOffline(false, true, false, false));
        assertTrue(StartupPolicy.shouldShowLoading(false, false));
    }

    @Test public void readyLocalDataNeverRequiresMandatoryLoading() {
        assertFalse(StartupPolicy.shouldShowLoading(true, false));
        assertFalse(StartupPolicy.shouldShowLoading(true, true));
    }

    @Test public void incompleteOnlineStartupKeepsLoadingUntilRequiredTablesArrive() {
        for (int poll = 0; poll < 120; poll++) {
            assertTrue(StartupPolicy.shouldShowLoading(false, false));
        }
        assertFalse(StartupPolicy.shouldShowLoading(true, false));
    }

    @Test public void recoveredNetworkResumesInitialLoading() {
        assertTrue(StartupPolicy.shouldShowLoading(false, false));
        assertFalse(StartupPolicy.shouldShowLoading(false, true));
        assertTrue(StartupPolicy.shouldShowLoading(false, false));
    }

    @Test public void planAloneNeverMakesInitialDataReady() {
        assertFalse(StartupPolicy.areRequiredTablesReady(true, false, false, false, false, false));
        assertFalse(StartupPolicy.areRequiredTablesReady(true, false, true, true, false, true));
    }

    @Test public void staleFlagsCannotReplaceMissingServerSiteObjects() {
        assertFalse(StartupPolicy.areRequiredTablesReady(true, true, true, true, false, true));
    }

    @Test public void missingRoomThemesAlsoPreventReadiness() {
        assertFalse(StartupPolicy.areRequiredTablesReady(true, true, true, true, true, false));
    }

    @Test public void everyRequiredFlagAndDictionaryMustBeReady() {
        for (int mask = 0; mask < 64; mask++) {
            assertEquals("state=" + mask, mask == 63, StartupPolicy.areRequiredTablesReady(
                    (mask & 1) != 0, (mask & 2) != 0, (mask & 4) != 0,
                    (mask & 8) != 0, (mask & 16) != 0, (mask & 32) != 0));
        }
    }

    @Test public void successfulEmptyPlanCanStillBeReadyWithDownloadedDictionaries() {
        // wpLoaded/optionsLoaded are set by a successful exchange, even for an empty list.
        assertTrue(StartupPolicy.areRequiredTablesReady(true, true, true, true, true, true));
    }

    @Test public void temporaryHttpFailuresAllowOfflineFallback() {
        for (int code : new int[]{408, 429, 500, 502, 503, 504}) {
            assertTrue("HTTP " + code, StartupPolicy.isTemporaryHttpFailure(code));
        }
    }

    @Test public void authenticationRejectionsAreNotTemporaryFailures() {
        for (int code : new int[]{200, 400, 401, 403, 404}) {
            assertFalse("HTTP " + code, StartupPolicy.isTemporaryHttpFailure(code));
        }
    }
}
