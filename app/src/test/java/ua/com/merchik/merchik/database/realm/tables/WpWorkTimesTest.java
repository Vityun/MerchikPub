package ua.com.merchik.merchik.database.realm.tables;

import org.junit.Test;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TestJsonUpload.StratEndWork.StartEndData;
import static org.junit.Assert.*;

/** Unmanaged model tests; no Realm database or Android runtime. */
public class WpWorkTimesTest {
    private WpDataDB row() {
        WpDataDB wp = new WpDataDB();
        wp.setId(123L);
        wp.setCode_dad2(1161026039283061377L);
        wp.setUser_id(241865);
        wp.setDt_update(300);
        wp.setVisit_start_dt(100);
        wp.setClient_start_dt(100);
        wp.setVisit_end_dt(200);
        wp.setClient_end_dt(200);
        wp.client_work_duration = 100L;
        wp.user_comment = "comment";
        wp.user_comment_author_id = 241865;
        wp.user_comment_dt_update = 300L;
        wp.setUser_opinion_id("1");
        wp.setUser_opinion_author_id("241865");
        wp.setUser_opinion_dt_update(300L);
        return wp;
    }

    private StartEndData sent(WpDataDB wp) {
        StartEndData sent = new StartEndData();
        sent.element_id = String.valueOf(wp.getId());
        sent.code_dad2 = String.valueOf(wp.getCode_dad2());
        sent.user_id = String.valueOf(wp.getUser_id());
        sent.dt_update = wp.getDt_update();
        sent.visit_start_dt = String.valueOf(wp.getVisit_start_dt());
        sent.client_start_dt = String.valueOf(wp.getClient_start_dt());
        sent.visit_end_dt = String.valueOf(wp.getVisit_end_dt());
        sent.client_end_dt = String.valueOf(wp.getClient_end_dt());
        sent.client_work_duration = String.valueOf(wp.client_work_duration);
        sent.status_set = String.valueOf(wp.getSetStatus());
        sent.user_comment = wp.user_comment;
        sent.user_comment_author_id = wp.user_comment_author_id;
        sent.user_comment_dt_update = wp.user_comment_dt_update;
        sent.user_opinion_id = wp.getUser_opinion_id();
        sent.user_opinion_author_id = wp.getUser_opinion_author_id();
        sent.user_opinion_dt_update = wp.getUser_opinion_dt_update();
        return sent;
    }

    @Test public void unchangedSnapshotMayBeAcknowledged() {
        WpDataDB wp = row();
        assertTrue(WpWorkTimes.matchesSentSnapshot(wp, sent(wp)));
    }

    @Test public void optionalFieldsDoNotPreventAcknowledgement() {
        WpDataDB wp = row();
        wp.setVisit_end_dt(0);
        wp.setClient_end_dt(0);
        wp.client_work_duration = null;
        wp.user_comment = null;
        wp.user_comment_dt_update = 0;
        wp.setUser_opinion_id(null);
        wp.setUser_opinion_author_id(null);
        assertTrue(WpWorkTimes.matchesSentSnapshot(wp, sent(wp)));
    }

    @Test public void commentChangedDuringUploadKeepsPending() {
        WpDataDB wp = row();
        StartEndData sent = sent(wp);
        wp.user_comment = "new comment";
        assertFalse(WpWorkTimes.matchesSentSnapshot(wp, sent));
        assertEquals(200, wp.getVisit_end_dt());
        assertEquals(200, wp.getClient_end_dt());
    }

    @Test public void opinionChangedDuringUploadKeepsPending() {
        WpDataDB wp = row();
        StartEndData sent = sent(wp);
        wp.setUser_opinion_id("2");
        assertFalse(WpWorkTimes.matchesSentSnapshot(wp, sent));
    }

    @Test public void correctedTimeDuringUploadKeepsPending() {
        WpDataDB wp = row();
        StartEndData sent = sent(wp);
        wp.setVisit_end_dt(210);
        wp.setClient_end_dt(210);
        assertFalse(WpWorkTimes.matchesSentSnapshot(wp, sent));
    }

    @Test public void newerVersionOfSameValuesIsNotClearedByOldReply() {
        WpDataDB wp = row();
        StartEndData sent = sent(wp);
        wp.setDt_update(301);
        assertFalse(WpWorkTimes.matchesSentSnapshot(wp, sent));
    }

    @Test public void reusedLocalIdForAnotherVisitIsNotAcknowledged() {
        WpDataDB wp = row();
        StartEndData sent = sent(wp);
        wp.setCode_dad2(1171026039283061377L);
        assertFalse(WpWorkTimes.matchesSentSnapshot(wp, sent));
    }

    @Test public void reassignedUserIsNotAcknowledged() {
        WpDataDB wp = row();
        StartEndData sent = sent(wp);
        wp.setUser_id(42);
        assertFalse(WpWorkTimes.matchesSentSnapshot(wp, sent));
    }

    @Test public void inconsistentPairIsNotAcknowledged() {
        WpDataDB wp = row();
        wp.setVisit_end_dt(0);
        assertFalse(WpWorkTimes.matchesSentSnapshot(wp, sent(wp)));
    }

    @Test public void pendingLocalMetadataSurvivesDownloadWithSameTimes() {
        WpDataDB local = row();
        local.startUpdate = true;
        WpDataDB server = row();
        server.user_comment = "old comment";
        server.setUser_opinion_id("0");
        assertTrue(WpWorkTimes.mergeServer(server, local));
        assertEquals(local.user_comment, server.user_comment);
        assertEquals(local.getUser_opinion_id(), server.getUser_opinion_id());
        assertTrue(server.startUpdate);
        assertTrue(WpWorkTimes.valid(server));
    }

    @Test public void metadataIsDownloadedWhenNoLocalEditsPending() {
        WpDataDB local = row();
        WpDataDB server = row();
        server.user_comment = "server update";
        assertTrue(WpWorkTimes.mergeServer(server, local));
        assertEquals("server update", server.user_comment);
        assertFalse(server.startUpdate);
    }

    @Test public void oldDownloadCannotRollBackVersionOrAlreadyUploadedMetadata() {
        WpDataDB local = row();
        WpDataDB server = row();
        server.setDt_update(299);
        server.user_comment = "stale comment";
        server.setUser_opinion_id("0");
        assertTrue(WpWorkTimes.mergeServer(server, local));
        assertEquals(300, server.getDt_update());
        assertEquals(local.user_comment, server.user_comment);
        assertEquals(local.getUser_opinion_id(), server.getUser_opinion_id());
        assertFalse(server.startUpdate);
    }
}
