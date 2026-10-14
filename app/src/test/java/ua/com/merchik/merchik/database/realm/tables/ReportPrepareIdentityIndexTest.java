package ua.com.merchik.merchik.database.realm.tables;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReportPrepareIdentityIndexTest {
    private final ReportPrepareIdentityIndex index = new ReportPrepareIdentityIndex();

    @Test
    public void sameIdAndProductCanBeUpdated() {
        index.remember(1L, "100", "10");
        assertNull(index.conflict(1L, "100", "10"));
    }

    @Test
    public void otherIdForSameVisitAndProductIsRejectedAndBothIdsAreLogged() {
        index.remember(1L, "100", "10");
        String conflict = index.conflict(2L, "100", "10");
        assertTrue(conflict.contains("reason=DUPLICATE_PAIR"));
        assertTrue(conflict.contains("dad2=100, tovarId=10, incomingId=2, keptId=1"));
    }

    @Test
    public void firstAcceptedRowReservesPairWithinBatch() {
        assertNull(index.conflict(2L, "100", "10"));
        index.remember(2L, "100", "10");
        assertNotNull(index.conflict(3L, "100", "10"));
        assertNull(index.conflict(2L, "100", "10"));
    }

    @Test
    public void sameProductInAnotherVisitAndAnotherProductInSameVisitAreAllowed() {
        index.remember(1L, "100", "10");
        assertNull(index.conflict(2L, "200", "10"));
        assertNull(index.conflict(3L, "100", "20"));
    }

    @Test
    public void sameIdCannotMoveToAnotherProductOrVisit() {
        index.remember(1L, "100", "10");
        assertTrue(index.conflict(1L, "100", "20").contains("reason=ID_KEY_MISMATCH"));
        assertTrue(index.conflict(1L, "200", "10").contains("keptDad2=100, keptTovarId=10"));
    }

    @Test
    public void pairIsNotAnAmbiguousConcatenation() {
        index.remember(1L, "12", "34");
        assertNull(index.conflict(2L, "123", "4"));
    }

    @Test
    public void invalidIdentityIsRejected() {
        assertNotNull(index.conflict(null, "100", "10"));
        assertNotNull(index.conflict(1L, null, "10"));
        assertNotNull(index.conflict(1L, "100", ""));
        assertNotNull(index.conflict(1L, " ", "10"));
        assertFalse(ReportPrepareIdentityIndex.hasIdentity(null, "100", "10"));
    }

    @Test
    public void existingDuplicatesDoNotReplaceFirstLocalIdentity() {
        index.remember(1L, "100", "10");
        index.remember(2L, "100", "10");
        assertNull(index.conflict(1L, "100", "10"));
        assertTrue(index.conflict(3L, "100", "10").contains("keptId=1"));
    }

    @Test
    public void invalidLegacyRowStillReservesItsPrimaryId() {
        index.remember(1L, null, null);
        assertTrue(index.conflict(1L, "100", "10").contains("reason=ID_KEY_MISMATCH"));
        assertNull(index.conflict(2L, "100", "10"));
    }
}
