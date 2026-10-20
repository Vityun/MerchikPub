package ua.com.merchik.merchik.Options.Controls;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RemainingGoodsPhotoIndexTest {
    private static final String HASH = "0a7b6237431e336221f44c25826d02bf";
    private static final String OTHER_HASH = "414631604a8a45dc143d421491198327";
    private static final String THIRD_HASH = "9098ad53995e62a773339790d6772afc";
    private static final long FIRST_TIME = 1792497904392L;
    private static final long SECOND_TIME = 1792497917293L;
    private static final long THIRD_TIME = 1792497944239L;

    @Test public void missingPhotosDoNotCount() {
        RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
        assertFalse(index.hasPhotoForTovar("80732"));
        assertFalse(index.hasDuplicatePhotoForTovar("80732"));
        assertEquals(0, index.getOriginalPhotoId(HASH));
    }

    @Test public void incidentCountsOriginalAndRejectsTwoLaterCopies() {
        RemainingGoodsPhotoIndex index = incident();
        assertTrue(index.hasPhotoForTovar("80732"));
        assertFalse(index.hasDuplicatePhotoForTovar("80732"));
        for (String id : Arrays.asList("80737", "142156")) {
            assertFalse(index.hasPhotoForTovar(id));
            assertTrue(index.hasDuplicatePhotoForTovar(id));
        }
        assertEquals(1259, index.getOriginalPhotoId(HASH));
        assertEquals(new LinkedHashSet<>(Arrays.asList("80732", "80737", "142156")),
                index.getTovarsForHash(HASH));
    }

    @Test public void threeDistinctHashesCoverThreeProducts() {
        RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
        index.addPhoto(1259, FIRST_TIME, "80732", HASH);
        index.addPhoto(1260, SECOND_TIME, "80737", OTHER_HASH);
        index.addPhoto(1261, THIRD_TIME, "142156", THIRD_HASH);
        for (String id : Arrays.asList("80732", "80737", "142156")) {
            assertTrue(index.hasPhotoForTovar(id));
            assertFalse(index.hasDuplicatePhotoForTovar(id));
        }
    }

    @Test public void repeatedPhotoForSameProductIsNotAConflict() {
        RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
        index.addPhoto(1261, THIRD_TIME, "80732", HASH);
        index.addPhoto(1260, SECOND_TIME, "80732", HASH);
        index.addPhoto(1259, FIRST_TIME, "80732", HASH);
        assertTrue(index.hasPhotoForTovar("80732"));
        assertFalse(index.hasDuplicatePhotoForTovar("80732"));
        assertEquals(1259, index.getOriginalPhotoId(HASH));
        assertEquals(1, index.getTovarsForHash(HASH).size());
    }

    @Test public void uniqueAlternativeResolvesOnlyItsProduct() {
        RemainingGoodsPhotoIndex index = incident();
        index.addPhoto(1262, THIRD_TIME + 1, "80737", OTHER_HASH);
        assertTrue(index.hasPhotoForTovar("80732"));
        assertTrue(index.hasPhotoForTovar("80737"));
        assertFalse(index.hasPhotoForTovar("142156"));
        assertTrue(index.hasDuplicatePhotoForTovar("80737"));
    }

    @Test public void uniqueAlternativesResolveAllProductsDespiteExtraDuplicates() {
        RemainingGoodsPhotoIndex index = incident();
        int localId = 1262;
        for (String id : Arrays.asList("80732", "80737", "142156")) {
            index.addPhoto(localId++, THIRD_TIME + 1, id, "unique-hash-" + id);
        }
        for (String id : Arrays.asList("80732", "80737", "142156")) {
            assertTrue(index.hasPhotoForTovar(id));
        }
    }

    @Test public void severalCopiesOfOtherProductsPhotosDoNotCount() {
        RemainingGoodsPhotoIndex index = incident();
        index.addPhoto(1262, THIRD_TIME + 1, "80732", OTHER_HASH);
        index.addPhoto(1263, THIRD_TIME + 2, "80737", OTHER_HASH);
        assertTrue(index.hasPhotoForTovar("80732"));
        assertFalse(index.hasPhotoForTovar("80737"));
    }

    @Test public void eachHashHasItsOwnOriginalProduct() {
        RemainingGoodsPhotoIndex index = incident();
        index.addPhoto(1262, THIRD_TIME + 1, "80737", OTHER_HASH);
        index.addPhoto(1263, THIRD_TIME + 2, "80732", OTHER_HASH);
        assertTrue(index.hasPhotoForTovar("80732"));
        assertTrue(index.hasPhotoForTovar("80737"));
        assertFalse(index.hasPhotoForTovar("142156"));
        assertEquals(1259, index.getOriginalPhotoId(HASH));
        assertEquals(1262, index.getOriginalPhotoId(OTHER_HASH));
    }

    @Test public void reverseInsertionOrderStillChoosesEarliestPhoto() {
        RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
        index.addPhoto(1261, THIRD_TIME, "142156", HASH);
        index.addPhoto(1260, SECOND_TIME, "80737", HASH);
        index.addPhoto(1259, FIRST_TIME, "80732", HASH);
        assertTrue(index.hasPhotoForTovar("80732"));
        assertFalse(index.hasPhotoForTovar("80737"));
        assertFalse(index.hasPhotoForTovar("142156"));
        assertEquals(1259, index.getOriginalPhotoId(HASH));
    }

    @Test public void laterCopyDoesNotInvalidateOriginal() {
        RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
        index.addPhoto(1259, FIRST_TIME, "80732", HASH);
        assertTrue(index.hasPhotoForTovar("80732"));
        index.addPhoto(1260, SECOND_TIME, "80737", HASH);
        assertTrue(index.hasPhotoForTovar("80732"));
        assertFalse(index.hasPhotoForTovar("80737"));
    }

    @Test public void creationTimeTakesPriorityOverLocalId() {
        RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
        index.addPhoto(1, SECOND_TIME, "80737", HASH);
        index.addPhoto(2, FIRST_TIME, "80732", HASH);
        assertTrue(index.hasPhotoForTovar("80732"));
        assertFalse(index.hasPhotoForTovar("80737"));
        assertEquals(2, index.getOriginalPhotoId(HASH));
    }

    @Test public void sameCreationTimeIsOrderedByLocalId() {
        RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
        index.addPhoto(1260, FIRST_TIME, "80737", HASH);
        index.addPhoto(1259, FIRST_TIME, "80732", HASH);
        assertTrue(index.hasPhotoForTovar("80732"));
        assertFalse(index.hasPhotoForTovar("80737"));
        assertEquals(1259, index.getOriginalPhotoId(HASH));
    }

    @Test public void missingCreationTimesAreOrderedByLocalId() {
        RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
        index.addPhoto(1261, -1, "142156", HASH);
        index.addPhoto(1260, 0, "80737", HASH);
        index.addPhoto(1259, 0, "80732", HASH);
        assertTrue(index.hasPhotoForTovar("80732"));
        assertFalse(index.hasPhotoForTovar("80737"));
        assertFalse(index.hasPhotoForTovar("142156"));
        assertEquals(1259, index.getOriginalPhotoId(HASH));
    }

    @Test public void knownCreationTimePrecedesMissingTimeRegardlessOfInsertionOrder() {
        for (boolean knownFirst : new boolean[]{true, false}) {
            RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
            if (knownFirst) index.addPhoto(1260, FIRST_TIME, "80737", HASH);
            index.addPhoto(1259, 0, "80732", HASH);
            if (!knownFirst) index.addPhoto(1260, FIRST_TIME, "80737", HASH);
            assertFalse(index.hasPhotoForTovar("80732"));
            assertTrue(index.hasPhotoForTovar("80737"));
            assertEquals(1260, index.getOriginalPhotoId(HASH));
        }
    }

    @Test public void hashCaseAndWhitespaceDoNotHideDuplicates() {
        RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
        index.addPhoto(1259, FIRST_TIME, "80732", HASH);
        String formattedHash = " " + HASH.toUpperCase(Locale.ROOT) + " ";
        index.addPhoto(1260, SECOND_TIME, "80737", formattedHash);
        assertTrue(index.hasPhotoForTovar("80732"));
        assertFalse(index.hasPhotoForTovar("80737"));
        assertEquals(1259, index.getOriginalPhotoId(formattedHash));
    }

    @Test public void blankHashesDoNotMakeUnrelatedPhotosDuplicates() {
        for (String hash : new String[]{null, "", " ", "0", " 0 ",
                "d41d8cd98f00b204e9800998ecf8427e"}) {
            RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
            index.addPhoto(1259, FIRST_TIME, "80732", hash);
            index.addPhoto(1260, SECOND_TIME, "80737", hash);
            assertTrue(index.hasPhotoForTovar("80732"));
            assertTrue(index.hasPhotoForTovar("80737"));
            assertFalse(index.hasDuplicatePhotoForTovar("80732"));
            assertFalse(index.hasDuplicatePhotoForTovar("80737"));
            assertEquals(0, index.getOriginalPhotoId(hash));
            assertTrue(index.getTovarsForHash(hash).isEmpty());
        }
    }

    @Test public void missingProductDoesNotCreateFalseConflict() {
        RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
        index.addPhoto(1259, FIRST_TIME, "80732", HASH);
        for (String id : new String[]{null, "", " ", "0", " 0 "}) {
            index.addPhoto(1, FIRST_TIME - 1, id, HASH);
            assertFalse(index.hasPhotoForTovar(id));
            assertFalse(index.hasDuplicatePhotoForTovar(id));
        }
        assertTrue(index.hasPhotoForTovar("80732"));
        assertEquals(1259, index.getOriginalPhotoId(HASH));
        assertEquals(1, index.getTovarsForHash(HASH).size());
    }

    @Test public void productWhitespaceDoesNotCreateDifferentProducts() {
        RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
        index.addPhoto(1259, FIRST_TIME, "80732", HASH);
        index.addPhoto(1260, SECOND_TIME, " 80732 ", HASH);
        assertTrue(index.hasPhotoForTovar(" 80732 "));
        assertFalse(index.hasDuplicatePhotoForTovar(" 80732 "));
        assertEquals(1, index.getTovarsForHash(HASH).size());
    }

    @Test public void indexesForDifferentVisitsDoNotShareState() {
        RemainingGoodsPhotoIndex firstVisit = new RemainingGoodsPhotoIndex();
        RemainingGoodsPhotoIndex secondVisit = new RemainingGoodsPhotoIndex();
        firstVisit.addPhoto(1259, FIRST_TIME, "80732", HASH);
        secondVisit.addPhoto(1260, SECOND_TIME, "80737", HASH);
        assertTrue(firstVisit.hasPhotoForTovar("80732"));
        assertTrue(secondVisit.hasPhotoForTovar("80737"));
    }

    @Test public void rebuiltIndexChoosesEarliestRemainingPhoto() {
        RemainingGoodsPhotoIndex before = incident();
        assertTrue(before.hasPhotoForTovar("80732"));
        assertFalse(before.hasPhotoForTovar("80737"));
        RemainingGoodsPhotoIndex after = new RemainingGoodsPhotoIndex();
        after.addPhoto(1260, SECOND_TIME, "80737", HASH);
        after.addPhoto(1261, THIRD_TIME, "142156", HASH);
        assertTrue(after.hasPhotoForTovar("80737"));
        assertFalse(after.hasPhotoForTovar("142156"));
        assertEquals(1260, after.getOriginalPhotoId(HASH));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void diagnosticViewCannotModifyIndex() {
        RemainingGoodsPhotoIndex index = incident();
        index.getTovarsForHash(HASH).clear();
    }

    private static RemainingGoodsPhotoIndex incident() {
        RemainingGoodsPhotoIndex index = new RemainingGoodsPhotoIndex();
        index.addPhoto(1259, FIRST_TIME, "80732", HASH);
        index.addPhoto(1260, SECOND_TIME, "80737", HASH);
        index.addPhoto(1261, THIRD_TIME, "142156", HASH);
        return index;
    }
}
