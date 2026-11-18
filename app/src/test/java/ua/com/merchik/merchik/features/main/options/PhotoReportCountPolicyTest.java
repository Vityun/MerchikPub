package ua.com.merchik.merchik.features.main.options;

import org.junit.Test;

import static org.junit.Assert.*;

public class PhotoReportCountPolicyTest {
    @Test public void closeUpUsesConfiguredMinimumAndExistingSkuAllowance() {
        assertEquals(4, PhotoReportCountPolicy.adjustedMinimum("158309", 3, 5, null));
        assertEquals(3, PhotoReportCountPolicy.adjustedMinimum("158309", 3, 4, null));
        assertEquals(6, PhotoReportCountPolicy.adjustedMinimum("158309", 5, 20, null));
    }

    @Test public void defaultsKeepSpecialPhotoTypes() {
        assertEquals(3, PhotoReportCountPolicy.defaultMinimum("158309", "158608"));
        assertEquals(1, PhotoReportCountPolicy.defaultMinimum("141360", "141361"));
        assertEquals(1, PhotoReportCountPolicy.defaultMinimum("132969", "132971"));
        assertEquals(1, PhotoReportCountPolicy.defaultMinimum("169108", "169109"));
        assertEquals(1, PhotoReportCountPolicy.defaultMinimum("164351", "164352"));
        assertEquals(1, PhotoReportCountPolicy.defaultMinimum("other", "164351"));
        assertEquals(0, PhotoReportCountPolicy.defaultMinimum("164355", "164354"));
    }

    @Test public void warehouseCartAndProductDoNotGetAnExtraPhoto() {
        for (String option : new String[]{"141360", "132969", "135158"}) {
            assertEquals(option, 2, PhotoReportCountPolicy.adjustedMinimum(option, 2, 99, null));
        }
    }

    @Test public void panoramaKeepsItsAdditionalAllowance() {
        assertEquals(5, PhotoReportCountPolicy.adjustedMinimum("158308", 3, 99, null));
        assertEquals(4, PhotoReportCountPolicy.adjustedMinimum("158308", 3, 4, null));
    }

    @Test public void exactMinimumBypassesAllAllowances() {
        assertEquals(2, PhotoReportCountPolicy.adjustedMinimum("158308", 3, 99, 2));
        assertEquals(0, PhotoReportCountPolicy.adjustedMinimum("158308", 3, 99, -1));
    }

    @Test public void closesAtThresholdButNotOnePhotoEarlier() {
        assertFalse(close(true, 10, 10, 39, 39, 3, 4));
        assertTrue(close(true, 10, 10, 39, 39, 4, 4));
        assertTrue(close(true, 10, 10, 39, 39, 5, 4));
    }

    @Test public void cancelledOrFailedSaveNeverClosesEvenWhenMinimumWasAlreadyMet() {
        assertFalse(close(false, 10, 10, 39, 39, 100, 4));
    }

    @Test public void anotherVisitOrPhotoTypeCannotCloseTheCurrentForm() {
        assertFalse(close(true, 10, 11, 39, 39, 4, 4));
        assertFalse(close(true, 10, 10, 39, 31, 4, 4));
        assertFalse(close(true, 0, 0, 39, 39, 4, 4));
    }

    @Test public void unresolvedRequirementDoesNotClose() {
        assertFalse(close(true, 10, 10, -1, -1, 4, 4));
        assertFalse(close(true, 10, 10, 39, 39, 4, -1));
    }

    @Test public void optionalPhotoRequiresANewSuccessfulPhotoToo() {
        assertFalse(close(true, 10, 10, 5, 5, 0, 0));
        assertFalse(close(false, 10, 10, 5, 5, 1, 0));
        assertTrue(close(true, 10, 10, 5, 5, 1, 0));
    }

    @Test public void reopeningDoesNotBlockAdditionalPhotos() {
        assertFalse(close(false, 10, 10, 39, 39, 4, 4));
        assertTrue(close(true, 10, 10, 39, 39, 5, 4));
    }

    @Test public void onlyVisitSampleCaptureSessionsAreEligible() {
        assertTrue(PhotoReportCountPolicy.isReportSession("SAMPLE_PHOTO_FROM_OPTION_158309", false, null));
        assertTrue(PhotoReportCountPolicy.isReportSession("SAMPLE_PHOTO_FROM_OPTION_GENERIC", false, "0"));
        assertFalse(PhotoReportCountPolicy.isReportSession("SAMPLE_PHOTO", false, null));
        assertFalse(PhotoReportCountPolicy.isReportSession("SAMPLE_PHOTO_FOR_PRODUCT", false, null));
        assertFalse(PhotoReportCountPolicy.isReportSession("SAMPLE_PHOTO_FROM_OPTION_158309", true, null));
        assertFalse(PhotoReportCountPolicy.isReportSession("SAMPLE_PHOTO_FROM_OPTION_158309", false, "123"));
        assertFalse(PhotoReportCountPolicy.isReportSession("TASKS", false, null));
        assertFalse(PhotoReportCountPolicy.isReportSession(null, false, null));
    }

    @Test public void productAndUnknownOptionsDoNotUseVisitWidePhotoCounts() {
        for (String option : new String[]{null, "", "135158", "157277", "174878", "175015", "unknown"}) {
            assertEquals(-1, PhotoReportCountPolicy.reportPhotoType(option));
        }
        assertEquals(39, PhotoReportCountPolicy.reportPhotoType("158309"));
        assertEquals(31, PhotoReportCountPolicy.reportPhotoType("141360"));
        assertEquals(0, PhotoReportCountPolicy.reportPhotoType("132968"));
    }

    private boolean close(boolean saved, long visit, long photoVisit, int type, int photoType,
                          int count, int minimum) {
        return PhotoReportCountPolicy.shouldClose(saved, visit, photoVisit, type, photoType, count, minimum);
    }
}
