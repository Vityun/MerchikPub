package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import org.junit.Test;

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
}
