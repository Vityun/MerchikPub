package ua.com.merchik.merchik.database.realm.tables;

import androidx.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers.PriceSaveGuard;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReportPrepareFieldUpdateTest {
    private Realm realm;

    @Before
    public void setUp() {
        Realm.init(InstrumentationRegistry.getTargetContext());
        realm = Realm.getInstance(new RealmConfiguration.Builder()
                .name("report-field-update-" + UUID.randomUUID())
                .inMemory()
                .build());
        realm.executeTransaction(r -> {
            ReportPrepareDB row = r.createObject(ReportPrepareDB.class, 1L);
            row.setCodeDad2("100");
            row.setTovarId("10");
            row.setPrice("0");
            row.setPriceMin("0");
            row.setPriceMax("0");
            row.setFace("3");
            row.setNotes("original note");
        });
    }

    @After
    public void tearDown() {
        if (realm != null) realm.close();
    }

    @Test
    public void stalePromotionDialogDoesNotEraseCurrentPrice() {
        ReportPrepareDB priceDialog = snapshot();
        ReportPrepareDB promotionDialog = snapshot();

        assertTrue(ReportPrepareRealm.updateFields(realm, priceDialog, row -> row.setPrice("10")));
        assertTrue(ReportPrepareRealm.updateFields(realm, promotionDialog, row -> {
            row.setPriceMin("20");
            row.setPriceMax("20");
            row.setUploadStatus(1);
            row.setDtChange(123L);
        }));

        assertPrices("10", "20", "20");
        assertEquals("3", current().getFace());
        assertEquals("original note", current().getNotes());
        assertEquals(1, current().getUploadStatus());
        assertEquals(123L, current().getDtChange());
        assertEquals("20", promotionDialog.getPriceMin());
        assertEquals("10", priceDialog.getPrice());
    }

    @Test
    public void stalePriceDialogDoesNotErasePrePromotionPrices() {
        ReportPrepareDB priceDialog = snapshot();
        ReportPrepareDB promotionDialog = snapshot();
        assertTrue(ReportPrepareRealm.updateFields(realm, promotionDialog, row -> {
            row.setPriceMin("20");
            row.setPriceMax("25");
        }));
        assertTrue(ReportPrepareRealm.updateFields(realm, priceDialog, row -> row.setPrice("10")));
        assertPrices("10", "20", "25");
    }

    @Test
    public void staleFaceAndNoteEditorsPreserveOtherChangesAndRefreshTheirSnapshots() {
        ReportPrepareDB faceDialog = snapshot();
        ReportPrepareDB noteDialog = snapshot();
        realm.executeTransaction(r -> {
            current().setPrice("10");
            current().setPriceMin("20");
            current().setPriceMax("20");
        });
        assertTrue(ReportPrepareRealm.updateFields(realm, faceDialog, row -> row.setFace("5")));
        assertTrue(ReportPrepareRealm.updateFields(realm, noteDialog, row -> row.setNotes("new note")));
        assertPrices("10", "20", "20");
        assertEquals("5", current().getFace());
        assertEquals("new note", current().getNotes());
        assertEquals("5", faceDialog.getFace());
        assertEquals("new note", noteDialog.getNotes());
    }

    @Test
    public void missingRowIsNotRecreatedOrReportedAsSaved() {
        ReportPrepareDB stale = snapshot();
        realm.executeTransaction(r -> current().deleteFromRealm());
        assertFalse(ReportPrepareRealm.updateFields(realm, stale, row -> {
            fail("A missing row must not be edited");
        }));
        assertNull(current());
        assertEquals("0", stale.getPrice());
    }

    @Test
    public void nullMissingIdAndDeletedManagedSnapshotsAreRejected() {
        assertFalse(ReportPrepareRealm.updateFields(realm, null, row -> fail("null snapshot")));
        assertFalse(ReportPrepareRealm.updateFields(realm, new ReportPrepareDB(), row -> fail("missing id")));
        ReportPrepareDB deleted = current();
        realm.executeTransaction(r -> deleted.deleteFromRealm());
        assertFalse(ReportPrepareRealm.updateFields(realm, deleted, row -> fail("deleted snapshot")));
    }

    @Test
    public void managedSnapshotIsNotWrittenOutsideTheTransaction() {
        assertTrue(ReportPrepareRealm.updateFields(realm, current(), row -> row.setPrice("10")));
        assertEquals("10", current().getPrice());
        assertFalse(realm.isInTransaction());
    }

    @Test
    public void failedTransactionDoesNotChangeDatabaseOrDialogSnapshot() {
        ReportPrepareDB stale = snapshot();
        try {
            ReportPrepareRealm.updateFields(realm, stale, row -> {
                row.setPrice("10");
                throw new IllegalStateException("test rollback");
            });
            fail("Expected the transaction to fail");
        } catch (IllegalStateException expected) {
            assertEquals("test rollback", expected.getMessage());
        }
        assertEquals("0", current().getPrice());
        assertEquals("0", stale.getPrice());
        assertFalse(realm.isInTransaction());
    }

    @Test
    public void onlyTheSelectedReportRowIsEdited() {
        realm.executeTransaction(r -> {
            ReportPrepareDB otherVisit = r.createObject(ReportPrepareDB.class, 2L);
            otherVisit.setCodeDad2("200");
            otherVisit.setTovarId("10");
            otherVisit.setPrice("99");
        });
        assertTrue(ReportPrepareRealm.updateFields(realm, snapshot(), row -> row.setPrice("10")));
        assertEquals("99", realm.where(ReportPrepareDB.class).equalTo("iD", 2L).findFirst().getPrice());
    }

    @Test
    public void invalidCurrentPriceDoesNotChangePricesMetadataOrSnapshot() {
        realm.executeTransaction(r -> {
            current().setPrice("10");
            current().setPriceMin("20");
            current().setPriceMax("20");
            current().setUploadStatus(0);
            current().setDtChange(123L);
        });
        ReportPrepareDB editor = snapshot();
        assertFalse(ReportPrepareRealm.updateFields(realm, editor,
                row -> !PriceSaveGuard.isPriceAboveBeforePromotion("30", row.getPriceMin()),
                row -> fail("Rejected prices must not reach the update callback")));

        assertPrices("10", "20", "20");
        assertEquals("10", editor.getPrice());
        assertEquals(0, current().getUploadStatus());
        assertEquals(123L, current().getDtChange());
    }

    @Test
    public void prePromotionPriceIsCheckedAgainstTheLatestCurrentPrice() {
        realm.executeTransaction(r -> current().setPrice("10"));
        ReportPrepareDB staleEditor = snapshot();
        realm.executeTransaction(r -> {
            current().setPrice("30");
            current().setPriceMin("40");
            current().setPriceMax("40");
        });

        assertFalse(ReportPrepareRealm.updateFields(realm, staleEditor,
                row -> !PriceSaveGuard.isPriceAboveBeforePromotion(row.getPrice(), "20"),
                row -> fail("The stale price of 10 must not allow saving 20 over the current price of 30")));
        assertPrices("30", "40", "40");
        assertEquals("0", staleEditor.getPriceMin());
    }

    @Test
    public void currentPriceIsCheckedAgainstTheLatestPrePromotionPrice() {
        realm.executeTransaction(r -> current().setPriceMin("40"));
        ReportPrepareDB staleEditor = snapshot();
        realm.executeTransaction(r -> {
            current().setPrice("10");
            current().setPriceMin("20");
            current().setPriceMax("20");
        });

        assertFalse(ReportPrepareRealm.updateFields(realm, staleEditor,
                row -> !PriceSaveGuard.isPriceAboveBeforePromotion("30", row.getPriceMin()),
                row -> fail("The stale pre-promotion price of 40 must not allow saving 30")));
        assertPrices("10", "20", "20");
    }

    @Test
    public void equalPricesCanBeSavedAndValidatedOnlyOnce() {
        realm.executeTransaction(r -> current().setPrice("20"));
        ReportPrepareDB editor = snapshot();
        int[] validations = {0};
        assertTrue(ReportPrepareRealm.updateFields(realm, editor, row -> {
            validations[0]++;
            assertTrue(realm.isInTransaction());
            return !PriceSaveGuard.isPriceAboveBeforePromotion(row.getPrice(), "20");
        }, row -> {
            row.setPriceMin("20");
            row.setPriceMax("20");
        }));
        assertEquals(1, validations[0]);
        assertPrices("20", "20", "20");
        assertEquals("20", editor.getPriceMin());
        assertEquals("20", editor.getPriceMax());
    }

    private ReportPrepareDB current() {
        return realm.where(ReportPrepareDB.class).equalTo("iD", 1L).findFirst();
    }

    private ReportPrepareDB snapshot() {
        return realm.copyFromRealm(current());
    }

    private void assertPrices(String price, String priceMin, String priceMax) {
        assertEquals(price, current().getPrice());
        assertEquals(priceMin, current().getPriceMin());
        assertEquals(priceMax, current().getPriceMax());
    }
}
