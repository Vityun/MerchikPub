package ua.com.merchik.merchik.database.realm.tables;

import org.junit.Test;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import static org.junit.Assert.*;

/** Unmanaged objects only: these tests do not open Realm or require a device. */
public class WpWorkTimesDurationTest {
    private WpDataDB visit(Long duration) {
        WpDataDB wp = new WpDataDB();
        wp.setVisit_start_dt(1793000000L);
        wp.setClient_start_dt(1793000000L);
        wp.setVisit_end_dt(1793002766L);
        wp.setClient_end_dt(1793002766L);
        wp.setDt_update(1793003000L);
        wp.client_work_duration = duration;
        return wp;
    }

    @Test public void serverMinuteDurationDoesNotMakeVisitDirty() {
        WpDataDB wp = visit(46L);
        assertTrue(WpWorkTimes.normalize(wp, "test"));
        assertEquals(Long.valueOf(2766), wp.client_work_duration);
        assertFalse(wp.startUpdate);
        assertEquals(1793003000L, wp.getDt_update());
    }

    @Test public void missingDurationDoesNotMakeVisitDirty() {
        WpDataDB wp = visit(null);
        assertTrue(WpWorkTimes.normalize(wp, "test"));
        assertEquals(Long.valueOf(2766), wp.client_work_duration);
        assertFalse(wp.startUpdate);
    }

    @Test public void normalizationPreservesRealPendingEdits() {
        WpDataDB wp = visit(46L);
        wp.startUpdate = true;
        assertTrue(WpWorkTimes.normalize(wp, "test"));
        assertTrue(wp.startUpdate);
        assertEquals(1793003000L, wp.getDt_update());
    }

    @Test public void repeatedServerSnapshotsDoNotTriggerReupload() {
        WpDataDB local = visit(2766L);
        for (int i = 0; i < 3; i++) {
            WpDataDB incoming = visit(46L);
            assertTrue(WpWorkTimes.mergeServer(incoming, local));
            assertFalse(incoming.startUpdate);
            assertEquals(1793003000L, incoming.getDt_update());
            assertEquals(Long.valueOf(2766), incoming.client_work_duration);
            local = incoming;
        }
    }

    @Test public void firstServerSnapshotDoesNotTriggerReupload() {
        WpDataDB incoming = visit(46L);
        assertTrue(WpWorkTimes.mergeServer(incoming, null));
        assertFalse(incoming.startUpdate);
        assertEquals(Long.valueOf(2766), incoming.client_work_duration);
    }

    @Test public void mergePreservesPendingLocalEdits() {
        WpDataDB local = visit(2766L);
        local.startUpdate = true;
        WpDataDB incoming = visit(46L);
        assertTrue(WpWorkTimes.mergeServer(incoming, local));
        assertTrue(incoming.startUpdate);
    }
}
