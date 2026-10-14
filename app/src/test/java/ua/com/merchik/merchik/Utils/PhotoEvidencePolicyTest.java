package ua.com.merchik.merchik.Utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PhotoEvidencePolicyTest {
    private static final String PHOTO_HASH = "921d135c1ffc731e577937a6a0607787";
    private static final String EMPTY_HASH = "d41d8cd98f00b204e9800998ecf8427e";
    private static final BooleanSupplier MUST_NOT_RESOLVE = () -> {
        throw new AssertionError("Photo lookup was not expected");
    };

    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void serverPhotoDoesNotRequireLocalCache() {
        assertTrue(PhotoEvidencePolicy.hasPhoto("59786393", null, MUST_NOT_RESOLVE));
    }

    @Test public void uploadedPhotoRemainsValidWithStaleEmptyHash() {
        assertTrue(PhotoEvidencePolicy.hasPhoto("59786393", EMPTY_HASH, MUST_NOT_RESOLVE));
    }

    @Test public void invalidServerIdsAreNotEvidence() {
        for (String id : new String[]{null, "", " ", "0", "00", "-1", "null", "abc", "1.2"}) {
            assertFalse(PhotoEvidencePolicy.hasPhoto(id, null, MUST_NOT_RESOLVE));
        }
    }

    @Test public void positiveServerIdsSupportWhitespaceAndLongValues() {
        assertTrue(PhotoEvidencePolicy.hasServerPhotoId(" 59786393 "));
        assertTrue(PhotoEvidencePolicy.hasServerPhotoId("2147483648"));
        assertFalse(PhotoEvidencePolicy.hasServerPhotoId("9999999999999999999999999999"));
    }

    @Test public void validPendingPhotoWorksOfflineWithoutServerId() {
        assertTrue(PhotoEvidencePolicy.hasPhoto("0", PHOTO_HASH, () -> true));
        assertTrue(PhotoEvidencePolicy.hasPhoto(null, PHOTO_HASH, () -> true));
    }

    @Test public void hashWithoutLocalOrServerEvidenceIsRejected() {
        assertFalse(PhotoEvidencePolicy.hasPhoto("0", PHOTO_HASH, () -> false));
    }

    @Test public void pendingPhotoIsResolvedOnlyOnce() {
        AtomicInteger lookups = new AtomicInteger();
        assertFalse(PhotoEvidencePolicy.hasPhoto(null, PHOTO_HASH, () -> {
            lookups.incrementAndGet();
            return false;
        }));
        assertEquals(1, lookups.get());
    }

    @Test public void emptyFileHashFromIncidentNeverCountsAsPendingPhoto() {
        assertFalse(PhotoEvidencePolicy.hasPhoto("0", EMPTY_HASH, MUST_NOT_RESOLVE));
        assertFalse(PhotoEvidencePolicy.hasPhoto("0", EMPTY_HASH.toUpperCase(Locale.ROOT), MUST_NOT_RESOLVE));
        assertFalse(PhotoEvidencePolicy.hasPhoto("0", " " + EMPTY_HASH + " ", MUST_NOT_RESOLVE));
    }

    @Test public void emptyHashIsCalculatedFromActualEmptyBytes() throws Exception {
        StringBuilder hash = new StringBuilder();
        for (byte b : MessageDigest.getInstance("MD5").digest(new byte[0])) {
            hash.append(String.format(Locale.ROOT, "%02x", b & 0xff));
        }
        assertFalse(PhotoEvidencePolicy.isUsableHash(hash.toString()));
    }

    @Test public void blankAndZeroHashesNeverTriggerLookup() {
        for (String hash : new String[]{null, "", " ", "0", " 0 "}) {
            assertFalse(PhotoEvidencePolicy.hasPhoto("0", hash, MUST_NOT_RESOLVE));
        }
    }

    @Test public void existingHashFormatsRemainSupported() {
        assertTrue(PhotoEvidencePolicy.isUsableHash(PHOTO_HASH));
        assertTrue(PhotoEvidencePolicy.isUsableHash("517209d9f68911eb546d268cdba0c9bae1cfc10e"));
    }

    @Test public void nullAndMissingFilesAreRejected() {
        assertFalse(PhotoEvidencePolicy.isReadableNonEmptyFile(null));
        assertFalse(PhotoEvidencePolicy.isReadableNonEmptyFile(new File(temporary.getRoot(), "missing.jpg")));
    }

    @Test public void directoriesAreRejected() {
        assertFalse(PhotoEvidencePolicy.isReadableNonEmptyFile(temporary.getRoot()));
    }

    @Test public void cameraPlaceholderIsRejected() throws Exception {
        assertFalse(PhotoEvidencePolicy.isReadableNonEmptyFile(temporary.newFile("camera.jpg")));
    }

    @Test public void nonEmptyFileStillRequiresImageDecodingByCaller() throws Exception {
        File file = temporary.newFile("photo.jpg");
        Files.write(file.toPath(), new byte[]{1, 2, 3});
        assertTrue(PhotoEvidencePolicy.isReadableNonEmptyFile(file));
    }
}
