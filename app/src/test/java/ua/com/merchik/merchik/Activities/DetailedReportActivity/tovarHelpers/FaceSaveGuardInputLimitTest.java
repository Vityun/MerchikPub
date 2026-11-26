package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import org.junit.Test;

import ua.com.merchik.merchik.Utils.FaceValue;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

import static org.junit.Assert.*;

public class FaceSaveGuardInputLimitTest {
    @Test public void acceptsLimitAndRoundedFractionsBelowIt() {
        assertTrue(FaceSaveGuard.checkInputLimit(null, "999"));
        assertTrue(FaceSaveGuard.checkInputLimit(null, "999,00"));
        assertTrue(FaceSaveGuard.checkInputLimit(null, "998.9"));
    }

    @Test public void rejectsTypedPastedAndIncrementedExcessWithoutContext() {
        assertFalse(FaceSaveGuard.checkInputLimit(null, "1000"));
        assertFalse(FaceSaveGuard.checkInputLimit(null, "999.01"));
        assertFalse(FaceSaveGuard.checkInputLimit(null, "999999999999999999999"));
    }

    @Test public void saveIsRejectedBeforeVisitLookupOrCompletedWorkExceptions() {
        assertTrue(FaceSaveGuard.canSaveFace(null, null, null, "1000").isError());
        assertTrue(FaceSaveGuard.canSaveFace(null, null, null, "999,1").isError());
    }

    @Test public void manufacturer5491AllowsFourDigitsInEditorAndOnSave() {
        ReportPrepareDB row = new ReportPrepareDB();
        row.tovarDB = new TovarDB();
        row.tovarDB.setManufacturerId("5491");
        row.tovarDB.setGroupId("123");
        WpDataDB visit = new WpDataDB();
        int limit = FaceSaveGuard.inputLimitForRow(row);
        assertEquals(9999, limit);
        for (String value : new String[]{"1000", FaceValue.step("9998", 1), "9999,00"}) {
            assertTrue(FaceSaveGuard.checkInputLimit(null, value, limit));
            assertFalse(FaceSaveGuard.canSaveFace(null, visit, row, value).isError());
        }
        for (String value : new String[]{"9999.01", FaceValue.step("9999", 1)}) {
            assertFalse(FaceSaveGuard.checkInputLimit(null, value, limit));
            assertTrue(FaceSaveGuard.canSaveFace(null, visit, row, value).isError());
        }
    }

    @Test public void group5491OrUnknownManufacturerDoesNotRaiseLimit() {
        ReportPrepareDB row = new ReportPrepareDB();
        row.tovarDB = new TovarDB();
        row.tovarDB.setGroupId("5491");
        assertEquals(999, FaceSaveGuard.inputLimitForRow(row));
        row.tovarDB.setManufacturerId("123");
        assertTrue(FaceSaveGuard.canSaveFace(null, new WpDataDB(), row, "1000").isError());
        assertEquals(999, FaceSaveGuard.inputLimitForRow(null));
    }
}
