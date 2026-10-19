package ua.com.merchik.merchik.data.RealmModels;

import org.junit.Test;

import java.time.Instant;

import ua.com.merchik.merchik.ServerExchange.StackPhotoSyncPolicy;
import ua.com.merchik.merchik.Utils.PhotoDateUtils;

import static org.junit.Assert.*;

public class StackPhotoDateTest {
    @Test
    public void pendingCameraRecordAlreadyHasDateBeforeActivityResult() {
        StackPhotoDB photo = pendingPhoto("2026-10-19");
        assertEquals(Long.valueOf(Instant.parse("2026-10-18T21:00:00Z").getEpochSecond()), photo.getDt());
        assertEquals("2026-10-19", PhotoDateUtils.uploadDate(photo.getDt(), photo.getTime_event()));
        assertEquals(0, photo.getUpload_to_server());
        assertEquals(0, photo.getGet_on_server());
    }

    @Test
    public void pendingTaskRecordNormalizesLegacyMilliseconds() {
        long seconds = Instant.parse("2026-10-19T08:38:50Z").getEpochSecond();
        StackPhotoDB photo = pendingPhoto(String.valueOf(seconds * 1000));
        assertEquals(Long.valueOf(seconds), photo.getDt());
        assertEquals("2026-10-19", PhotoDateUtils.uploadDate(photo.getDt(), photo.getTime_event()));
    }

    @Test
    public void oldRecordWithNullDtRemainsUploadableWithoutChangingComment() {
        StackPhotoDB photo = pendingPhoto("2026-10-19");
        photo.setDt(null);
        StackPhotoSyncPolicy.editComment(photo, "Local comment", 200);
        assertEquals("2026-10-19", PhotoDateUtils.uploadDate(photo.getDt(), photo.getTime_event()));
        assertNull(photo.getDt());
        assertEquals("Local comment", photo.getComment());
        assertTrue(photo.isCommentUpload());
        assertEquals(0, photo.getUpload_to_server());
    }

    @Test
    public void invalidDateFailureKeepsPhotoAndCommentPending() {
        StackPhotoDB sent = pendingPhoto(null);
        StackPhotoDB current = pendingPhoto(null);
        StackPhotoSyncPolicy.editComment(current, "Local comment", 200);
        try {
            PhotoDateUtils.uploadDate(sent.getDt(), sent.getTime_event());
            fail("Invalid metadata must not be uploaded");
        } catch (IllegalArgumentException error) {
            assertTrue(StackPhotoSyncPolicy.uploadFailed(current, sent, error.getMessage(), 300));
        }
        assertNull(current.getDt());
        assertEquals(2787, current.getId());
        assertEquals("camera.jpg", current.getPhoto_num());
        assertEquals("Local comment", current.getComment());
        assertTrue(current.isCommentUpload());
        assertEquals(0, current.getUpload_to_server());
        assertEquals(0, current.getGet_on_server());
        assertNotNull(current.getErrorTxt());
    }

    private static StackPhotoDB pendingPhoto(String timeEvent) {
        return new StackPhotoDB(2787, null, 0, 1, 2, "3", 4, timeEvent,
                100, 100, 0, 0, 1191026036770061512L,
                "camera.jpg", "local-hash", 31, "1", "0", "0", "", "", 0, 0,
                false, "", "", "");
    }
}
