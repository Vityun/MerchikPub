package ua.com.merchik.merchik.Options;

import androidx.test.InstrumentationRegistry;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;

import static org.junit.Assert.*;

public class ReportWorkEndPersistenceTest {
    private static final long DAD2 = 1141026037940061377L;
    private Realm realm;

    @Before public void setUp() {
        Realm.init(InstrumentationRegistry.getTargetContext());
        realm = Realm.getInstance(new RealmConfiguration.Builder()
                .name("work-end-" + UUID.randomUUID()).inMemory().build());
        realm.executeTransaction(r -> {
            WpDataDB wp = r.createObject(WpDataDB.class, 1L);
            wp.setCode_dad2(DAD2);
            wp.setVisit_start_dt(100);
            wp.setClient_start_dt(110);
            wp.setDt_update(120);
            wp.setClient_txt("latest customer");
            OptionsDB option = r.createObject(OptionsDB.class, "work-end-control");
            option.setCodeDad2(String.valueOf(DAD2));
            option.setOptionControlId("138521");
            option.setIsSignal("1");
        });
    }

    @After public void tearDown() {
        if (realm != null) realm.close();
    }

    private WpDataDB wp() {
        return realm.where(WpDataDB.class).equalTo("code_dad2", DAD2).findFirst();
    }

    private ReportConductState pending() {
        ReportConductState state = new ReportConductState();
        state.dad2 = DAD2;
        state.requestWorkEnd(100, 110, null);
        state.stage = ReportConductState.Stage.WAITING_CONFIRMATION;
        return state;
    }

    @Test public void noConsentLeavesEndAndUploadFlagUnchanged() {
        assertFalse(ReportWorkEndPersistence.save(realm, pending(), 250));
        assertEquals(0, wp().getVisit_end_dt());
        assertEquals(0, wp().getClient_end_dt());
        assertEquals(120, wp().getDt_update());
        assertFalse(wp().startUpdate);
        assertEquals("1", realm.where(OptionsDB.class).findFirst().getIsSignal());
    }

    @Test public void confirmationUpdatesOnlyWorkEndFields() {
        ReportConductState state = pending();
        state.prepareWorkEnd(250, true);
        realm.executeTransaction(r -> wp().setClient_txt("changed while dialog open"));
        assertTrue(ReportWorkEndPersistence.save(realm, state, 260));
        assertEquals(250, wp().getVisit_end_dt());
        assertEquals(250, wp().getClient_end_dt());
        assertEquals(Long.valueOf(140), wp().client_work_duration);
        assertEquals(260, wp().getDt_update());
        assertEquals("changed while dialog open", wp().getClient_txt());
        assertTrue(wp().startUpdate);
        assertEquals("2", realm.where(OptionsDB.class).findFirst().getIsSignal());
    }

    @Test public void restartAfterRealmCommitDoesNotOverwriteOrDirtyUploadedRow() {
        ReportConductState state = pending();
        state.prepareWorkEnd(250, true);
        assertTrue(ReportWorkEndPersistence.save(realm, state, 260));
        realm.executeTransaction(r -> { wp().startUpdate = false; wp().setDt_update(270); });
        Gson gson = new Gson();
        ReportConductState restored = gson.fromJson(gson.toJson(state), ReportConductState.class);
        assertTrue(ReportWorkEndPersistence.save(realm, restored, 300));
        assertEquals(250, wp().getVisit_end_dt());
        assertEquals(270, wp().getDt_update());
        assertFalse(wp().startUpdate);
    }

    @Test public void changedStartRejectsStaleConsent() {
        ReportConductState state = pending();
        state.prepareWorkEnd(250, true);
        realm.executeTransaction(r -> wp().setVisit_start_dt(200));
        assertFalse(ReportWorkEndPersistence.save(realm, state, 260));
        assertEquals(0, wp().getVisit_end_dt());
        assertFalse(wp().startUpdate);
    }

    @Test public void anotherCompletionIsNeverOverwritten() {
        ReportConductState state = pending();
        state.prepareWorkEnd(250, true);
        realm.executeTransaction(r -> { wp().setVisit_end_dt(240); wp().setClient_end_dt(240); });
        assertFalse(ReportWorkEndPersistence.save(realm, state, 260));
        assertEquals(240, wp().getVisit_end_dt());
    }

    @Test public void calculatedEndIsUsedInsteadOfConfirmationClock() {
        ReportConductState state = pending();
        state.requestedWorkEnd = 700L;
        state.prepareWorkEnd(1000, true);
        assertTrue(ReportWorkEndPersistence.save(realm, state, 1000));
        assertEquals(700, wp().getVisit_end_dt());
        assertEquals(Long.valueOf(590), wp().client_work_duration);
    }

    @Test public void deletedVisitIsNotRecreatedFromSnapshot() {
        ReportConductState state = pending();
        state.prepareWorkEnd(250, true);
        realm.executeTransaction(r -> wp().deleteFromRealm());
        assertFalse(ReportWorkEndPersistence.save(realm, state, 260));
        assertEquals(0, realm.where(WpDataDB.class).count());
    }
}
