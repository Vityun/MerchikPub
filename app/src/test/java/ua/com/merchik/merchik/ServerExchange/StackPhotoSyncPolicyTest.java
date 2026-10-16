package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.PhotoInfoResponseList;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoInformationData;

import static org.junit.Assert.*;
import static ua.com.merchik.merchik.ServerExchange.StackPhotoSyncPolicy.*;

public class StackPhotoSyncPolicyTest {
    @Test
    public void uploadAckPreservesCommentEnteredWhileFileWasUploading() throws Exception {
        StackPhotoDB sent = photo();
        StackPhotoDB current = photo();
        editComment(current, "New warehouse comment", 200);
        Map<String, Object> before = fields(current);
        assertTrue(uploadSucceeded(current, sent, 300));
        assertEquals(300, current.getUpload_to_server());
        assertNull(current.getError());
        assertOnlyChanged(before, current, "upload_to_server", "error");
    }

    @Test
    public void failedUploadPreservesCommentAndItsPendingFlag() throws Exception {
        StackPhotoDB sent = photo();
        StackPhotoDB current = photo();
        editComment(current, "Saved during request", 200);
        Map<String, Object> before = fields(current);
        assertTrue(uploadFailed(current, sent, "timeout", 300));
        assertEquals("timeout", current.getErrorTxt());
        assertOnlyChanged(before, current, "error", "errorTime", "errorTxt");
    }

    @Test
    public void serverIdResponseOnlyChangesServerFields() throws Exception {
        StackPhotoDB sent = photo();
        StackPhotoDB current = photo();
        editComment(current, "Saved before server ID", 200);
        current.setUpload_to_server(250);
        Map<String, Object> before = fields(current);
        assertTrue(receivedServerDetails(current, sent, "59663971", "https://example/photo", 100));
        assertEquals("59663971", current.getPhotoServerId());
        assertOnlyChanged(before, current, "photoServerId", "photoServerURL", "get_on_server");
    }

    @Test
    public void uploadAckAfterServerIdDoesNotEraseServerId() {
        StackPhotoDB sent = photo();
        StackPhotoDB current = photo();
        assertTrue(receivedServerDetails(current, sent, "59663971", "url", 100));
        editComment(current, "Local text", 200);
        assertTrue(uploadSucceeded(current, sent, 300));
        assertEquals("59663971", current.getPhotoServerId());
        assertEquals("Local text", current.getComment());
        assertTrue(current.isCommentUpload());
    }

    @Test
    public void serverIdAfterUploadAckDoesNotResetUploadTime() {
        StackPhotoDB sent = photo();
        StackPhotoDB current = photo();
        assertTrue(uploadSucceeded(current, sent, 300));
        assertTrue(receivedServerDetails(current, sent, "59663971", "url", 100));
        assertEquals(300, current.getUpload_to_server());
    }

    @Test
    public void commentWaitsForServerIdAndIsAcknowledgedOnlyAfterSeparateUpload() {
        StackPhotoDB sent = photo();
        StackPhotoDB current = photo();
        editComment(current, "Warehouse is empty", 200);
        assertTrue(uploadSucceeded(current, sent, 300));
        assertTrue(current.isCommentUpload());
        assertTrue(receivedServerDetails(current, sent, "59663971", "url", 100));
        assertTrue(current.isCommentUpload());
        PhotoInformationData request = request(current);
        assertTrue(commentAccepted(request, Collections.singletonList(result(7, true))));
        assertTrue(acknowledgeComment(current, request));
        assertFalse(current.isCommentUpload());
        assertEquals("Warehouse is empty", current.getComment());
    }

    @Test
    public void commentEditPreservesAlreadyReceivedServerFields() throws Exception {
        StackPhotoDB current = photo();
        current.setPhotoServerId("59663971");
        current.setPhotoServerURL("url");
        current.setUpload_to_server(200);
        current.setGet_on_server(100);
        Map<String, Object> before = fields(current);
        editComment(current, "New text", 300);
        assertTrue(current.isCommentUpload());
        assertOnlyChanged(before, current, "comment", "commentUpload", "vpi");
    }

    @Test
    public void commentAckCannotClearNewerText() throws Exception {
        StackPhotoDB current = pendingPhoto();
        PhotoInformationData sent = request(current);
        editComment(current, "Updated again", 200);
        Map<String, Object> before = fields(current);
        assertFalse(acknowledgeComment(current, sent));
        assertOnlyChanged(before, current);
        assertTrue(current.isCommentUpload());
    }

    @Test
    public void matchingCommentAckOnlyClearsPendingFlag() throws Exception {
        StackPhotoDB current = pendingPhoto();
        PhotoInformationData sent = request(current);
        Map<String, Object> before = fields(current);
        assertTrue(acknowledgeComment(current, sent));
        assertOnlyChanged(before, current, "commentUpload");
    }

    @Test
    public void editingAgainAfterAckQueuesCommentAgain() {
        StackPhotoDB current = pendingPhoto();
        assertTrue(acknowledgeComment(current, request(current)));
        editComment(current, "Updated after ACK", 300);
        assertTrue(current.isCommentUpload());
    }

    @Test
    public void rejectedCommentIsNotAccepted() {
        StackPhotoDB current = pendingPhoto();
        assertFalse(commentAccepted(request(current), Collections.singletonList(result(7, false))));
        assertTrue(current.isCommentUpload());
        assertEquals("Pending text", current.getComment());
    }

    @Test
    public void missingUnknownOrMalformedResultCannotAcknowledgeComment() {
        PhotoInformationData sent = request(pendingPhoto());
        assertFalse(commentAccepted(sent, null));
        assertFalse(commentAccepted(sent, Collections.emptyList()));
        assertFalse(commentAccepted(sent, Arrays.asList(null, result(null, true), result(8, true))));
    }

    @Test
    public void conflictingResultsDoNotAcknowledgeComment() {
        PhotoInformationData sent = request(pendingPhoto());
        assertFalse(commentAccepted(sent, Arrays.asList(result(7, true), result(7, false))));
        assertFalse(commentAccepted(sent, Arrays.asList(result(7, false), result(7, true))));
    }

    @Test
    public void partialResponseOnlyAcceptsMatchingLocalId() {
        PhotoInformationData sent = request(pendingPhoto());
        assertTrue(commentAccepted(sent, Arrays.asList(result(8, false), result(7, true))));
        sent.element_id = "8";
        assertFalse(commentAccepted(sent, Collections.singletonList(result(7, true))));
    }

    @Test
    public void deletedPhotoIsNotRecreatedByCallbacks() {
        StackPhotoDB sent = photo();
        assertFalse(uploadSucceeded(null, sent, 200));
        assertFalse(uploadFailed(null, sent, "error", 200));
        assertFalse(receivedServerDetails(null, sent, "59663971", "url", 200));
        assertFalse(acknowledgeComment(null, request(pendingPhoto())));
    }

    @Test
    public void reusedLocalIdWithDifferentHashRejectsOldResponse() throws Exception {
        StackPhotoDB sent = photo();
        StackPhotoDB current = photo();
        current.setPhoto_hash("other-hash");
        Map<String, Object> before = fields(current);
        assertFalse(uploadSucceeded(current, sent, 200));
        assertFalse(uploadFailed(current, sent, "error", 200));
        assertFalse(receivedServerDetails(current, sent, "59663971", "url", 200));
        assertOnlyChanged(before, current);
    }

    @Test
    public void sameHashDoesNotAllowUpdatingAnotherLocalId() {
        StackPhotoDB current = photo();
        current.setId(8);
        assertFalse(samePhoto(current, photo()));
    }

    @Test
    public void photosWithoutHashUsePathAndCreationTime() {
        StackPhotoDB current = photo();
        StackPhotoDB snapshot = photo();
        snapshot.setPhoto_hash(null);
        current.setPhoto_hash(null);
        assertTrue(samePhoto(current, snapshot));
        current.setCreate_time(101);
        assertFalse(samePhoto(current, snapshot));
        current.setCreate_time(100);
        current.setPhoto_num("other.jpg");
        assertFalse(samePhoto(current, snapshot));
    }

    @Test
    public void serverOnlyPhotoIdentityWorksWithoutLocalFileOrHash() {
        StackPhotoDB current = pendingPhoto();
        StackPhotoDB snapshot = pendingPhoto();
        current.setPhoto_hash(null);
        snapshot.setPhoto_hash(null);
        current.setPhoto_num(null);
        snapshot.setPhoto_num(null);
        assertTrue(samePhoto(current, snapshot));
        current.setPhotoServerId("different");
        assertFalse(samePhoto(current, snapshot));
    }

    @Test
    public void conflictingServerIdDoesNotReplaceKnownIdentity() throws Exception {
        StackPhotoDB current = pendingPhoto();
        Map<String, Object> before = fields(current);
        assertFalse(receivedServerDetails(current, photo(), "59663980", "other", 200));
        assertOnlyChanged(before, current);
    }

    @Test
    public void emptyServerUrlDoesNotClearExistingUrl() {
        StackPhotoDB current = photo();
        current.setPhotoServerURL("existing-url");
        assertTrue(receivedServerDetails(current, photo(), "59663971", "", 100));
        assertEquals("existing-url", current.getPhotoServerURL());
        assertTrue(receivedServerDetails(current, photo(), "59663971", null, 100));
        assertEquals("existing-url", current.getPhotoServerURL());
    }

    @Test
    public void invalidServerIdsAreIgnored() {
        for (String id : Arrays.asList(null, "", "0")) {
            assertFalse(receivedServerDetails(photo(), photo(), id, "url", 100));
        }
    }

    @Test
    public void lateFailureDoesNotUndoSuccessfulUpload() throws Exception {
        StackPhotoDB current = photo();
        StackPhotoDB sent = photo();
        assertTrue(uploadSucceeded(current, sent, 200));
        Map<String, Object> before = fields(current);
        assertFalse(uploadFailed(current, sent, "late timeout", 300));
        assertOnlyChanged(before, current);
    }

    @Test
    public void uploadTimeDoesNotGoBackwards() {
        StackPhotoDB current = photo();
        current.setUpload_to_server(300);
        assertTrue(uploadSucceeded(current, photo(), 200));
        assertEquals(300, current.getUpload_to_server());
    }

    @Test
    public void commentAckChecksServerAndLocalIds() {
        StackPhotoDB current = pendingPhoto();
        PhotoInformationData sent = request(current);
        sent.id = "59663980";
        assertFalse(acknowledgeComment(current, sent));
        sent.id = current.getPhotoServerId();
        sent.element_id = "8";
        assertFalse(acknowledgeComment(current, sent));
    }

    @Test
    public void commentAckNeedsPendingFlagAndValidServerId() {
        StackPhotoDB current = pendingPhoto();
        PhotoInformationData sent = request(current);
        current.setCommentUpload(false);
        assertFalse(acknowledgeComment(current, sent));
        current.setCommentUpload(true);
        for (String id : Arrays.asList(null, "", "0")) {
            current.setPhotoServerId(id);
            sent.id = id;
            assertFalse(acknowledgeComment(current, sent));
        }
    }

    private static StackPhotoDB photo() {
        StackPhotoDB photo = new StackPhotoDB();
        photo.setId(7);
        photo.setPhoto_hash("local-hash");
        photo.setPhoto_num("local.jpg");
        photo.setCreate_time(100);
        photo.setDt(100L);
        photo.setVpi(100);
        photo.setCode_dad2(1300926020162061344L);
        photo.setPhoto_type(31);
        photo.setExample_id("78");
        photo.setComment("");
        photo.setDvi(1);
        photo.setDviUpload(true);
        photo.setMark("8");
        photo.setMarkUpload(true);
        photo.setError(1);
        photo.setErrorTxt("old error");
        return photo;
    }

    private static StackPhotoDB pendingPhoto() {
        StackPhotoDB photo = photo();
        photo.setPhotoServerId("59663971");
        editComment(photo, "Pending text", 100);
        return photo;
    }

    private static PhotoInformationData request(StackPhotoDB photo) {
        PhotoInformationData sent = new PhotoInformationData();
        sent.element_id = String.valueOf(photo.getId());
        sent.id = photo.getPhotoServerId();
        sent.text = photo.getComment();
        return sent;
    }

    private static PhotoInfoResponseList result(Integer id, boolean accepted) {
        PhotoInfoResponseList result = new PhotoInfoResponseList();
        result.elementId = id;
        result.state = accepted;
        result.error = accepted ? null : "Rejected";
        return result;
    }

    private static Map<String, Object> fields(StackPhotoDB photo) throws Exception {
        Map<String, Object> result = new HashMap<>();
        for (Field field : StackPhotoDB.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            result.put(field.getName(), field.get(photo));
        }
        return result;
    }

    private static void assertOnlyChanged(Map<String, Object> before, StackPhotoDB after,
                                          String... allowed) throws Exception {
        Map<String, Object> actual = fields(after);
        for (String key : allowed) {
            before.remove(key);
            actual.remove(key);
        }
        assertEquals(before, actual);
    }
}
