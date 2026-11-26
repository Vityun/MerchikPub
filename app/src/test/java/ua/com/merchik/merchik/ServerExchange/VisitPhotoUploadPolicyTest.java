package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

import static org.junit.Assert.*;
import static ua.com.merchik.merchik.ServerExchange.VisitUploadBatch.Kind.*;

public class VisitPhotoUploadPolicyTest {
    private static JsonObject json(String value) { return new JsonParser().parse(value).getAsJsonObject(); }

    private static StackPhotoDB photo(int id) {
        StackPhotoDB photo = new StackPhotoDB();
        photo.setId(id);
        photo.setPhoto_hash("hash-" + id);
        photo.setPhoto_num("/photos/" + id + ".jpg");
        photo.setCreate_time(100);
        return photo;
    }

    private VisitUploadBatch.Row warehouseComment(String local, String server, String text) {
        JsonObject payload = new JsonObject();
        payload.addProperty("element_id", local);
        payload.addProperty("id", server);
        payload.addProperty("text", text);
        VisitUploadBatch.Row row = new VisitUploadBatch.Row(PHOTO_COMMENT, local, payload);
        row.requiredWarehouseComment = true;
        return row;
    }

    @Test public void onlyWarehouseTypeAndEmptyWarehouseSamplesRequireConfirmation() {
        StackPhotoDB photo = photo(3907);
        photo.setPhoto_type(31);
        for (String sample : Arrays.asList("78", "94", "79", "", null)) {
            photo.setExample_id(sample);
            assertEquals("78".equals(sample) || "94".equals(sample), VisitPhotoUploadPolicy.requiresWarehouseComment(photo));
        }
        photo.setExample_id("78");
        photo.setPhoto_type(39);
        assertFalse(VisitPhotoUploadPolicy.requiresWarehouseComment(photo));
    }

    @Test public void bothCurrentCommentsNeedTheirOwnServerReceipt() {
        VisitUploadBatch batch = new VisitUploadBatch();
        String text = "Товару замовника на складі не знайшла";
        VisitUploadBatch.Row first = warehouseComment("3908", "60222135", text);
        VisitUploadBatch.Row missing = warehouseComment("3907", "", text);
        batch.merge(Arrays.asList(first, missing));
        Set<String> pending = Collections.singleton("3907");
        assertEquals(Collections.singletonList(first), batch.next(Collections.emptySet(), pending));
        JsonObject firstAck = json("{state:true,list:[{element_id:3908,state:true}]}");
        assertTrue(batch.acknowledge(first, VisitUploadBatch.accepted(first, firstAck)));
        first.applied = true;
        assertFalse(batch.allAcknowledged(pending));

        VisitUploadBatch.Row found = warehouseComment("3907", "60222141", text);
        batch.merge(Collections.singletonList(found));
        assertEquals(Collections.singletonList(found), batch.next());
        assertNull(VisitUploadBatch.accepted(found, firstAck));
        assertNull(VisitUploadBatch.accepted(found, json("{state:true,list:[{element_id:3907,state:false}]}")));
        JsonObject ack = VisitUploadBatch.accepted(found, json("{state:true,list:[{element_id:3907,state:true}]}"));
        assertNotNull(ack);
        VisitUploadBatch.Row edited = warehouseComment("3907", "60222141", "Виправлений коментар");
        batch.merge(Collections.singletonList(edited));
        assertFalse(batch.acknowledge(found, ack));
        assertFalse(batch.allAcknowledged());
        assertTrue(batch.acknowledge(edited, ack));
        edited.applied = true;
        assertTrue(batch.allAcknowledged());
        batch.merge(Collections.singletonList(warehouseComment("3907", "60222141", "1234567890")));
        assertTrue(batch.next().isEmpty());
        assertFalse(batch.allAcknowledged());
    }

    @Test public void recoveredHashDoesNotResendPreviouslyUploadedWarehouseFile() {
        StackPhotoDB photo = photo(3907);
        photo.setPhoto_type(31);
        photo.setExample_id("94");
        photo.setPhoto_hash("");
        VisitUploadBatch batch = new VisitUploadBatch();
        batch.merge(Collections.singletonList(warehouseComment("3907", "", "Valid warehouse comment")));
        assertFalse(VisitPhotoUploadPolicy.record(batch, photo, true, json("{state:false,list:[{state:false,error_type:'missing_geo_coord'}]}")));
        assertTrue(VisitPhotoUploadPolicy.record(batch, photo, true, json("{state:false,list:[{state:false,error_type:'photo_already_exist'}]}")));
        photo.setPhoto_hash("restored");
        assertTrue(batch.retryAfterConductFailure(1));
        assertTrue(VisitPhotoUploadPolicy.ready(batch, photo));
        assertFalse(batch.allAcknowledged(Collections.singleton("3907")));
    }

    @Test public void httpSuccessNeedsNeitherServerIdNorPerPhotoAcceptance() {
        VisitUploadBatch batch = new VisitUploadBatch();
        StackPhotoDB photo = photo(1);
        assertTrue(VisitPhotoUploadPolicy.record(batch, photo, true,
                json("{state:true,list:[{state:false,error:'Photo rejected',error_type:'missing_geo_coord'}]}")));
        assertNull(photo.getPhotoServerId());
        assertTrue(VisitPhotoUploadPolicy.ready(batch, photo));
    }

    @Test public void aggregateFalseWithPerPhotoErrorStillCountsAsHttpReceipt() {
        assertTrue(VisitPhotoUploadPolicy.acceptsResponse(true,
                json("{state:false,list:[{state:false,error:'Invalid photo'}]}")));
    }

    @Test public void successfulEmptyResponseIsEnoughWithoutExplicitGlobalError() {
        assertTrue(VisitPhotoUploadPolicy.acceptsResponse(true, null));
        assertTrue(VisitPhotoUploadPolicy.acceptsResponse(true, json("{}")));
        assertTrue(VisitPhotoUploadPolicy.acceptsResponse(true, json("{state:true,list:[]}")));
    }

    @Test public void httpErrorNeverCountsEvenWhenBodyClaimsSuccess() {
        VisitUploadBatch batch = new VisitUploadBatch();
        StackPhotoDB photo = photo(1);
        assertFalse(VisitPhotoUploadPolicy.record(batch, photo, false, json("{state:true,list:[{state:true}]}")));
        assertFalse(VisitPhotoUploadPolicy.ready(batch, photo));
    }

    @Test public void explicitGlobalErrorsDoNotReleaseConduct() {
        for (String body : Arrays.asList("{state:false,error:'Session expired'}",
                "{state:true,error:'Maintenance',list:[{state:true}]}",
                "{error_type:'not_authorized'}", "{error:{message:'Failed'}}",
                "{state:false}", "{state:false,list:[]}")) {
            assertFalse(body, VisitPhotoUploadPolicy.acceptsResponse(true, json(body)));
        }
    }

    @Test public void emptyErrorValuesAreNotGlobalErrors() {
        for (String error : Arrays.asList("null", "false", "0", "''")) {
            assertTrue(VisitPhotoUploadPolicy.acceptsResponse(true, json("{state:true,error:" + error + "}")));
        }
    }

    @Test public void transportFailureOrNoAttemptRemainsBlocking() {
        assertFalse(VisitPhotoUploadPolicy.allReady(new VisitUploadBatch(), Collections.singletonList(photo(1))));
    }

    @Test public void receiptSurvivesRestartWithoutChangingNormalPhotoState() {
        VisitUploadBatch batch = new VisitUploadBatch();
        batch.userId = 7;
        batch.dad2 = 123;
        StackPhotoDB photo = photo(1);
        photo.setComment("Pending comment");
        photo.setCommentUpload(true);
        photo.setDviUpload(true);
        photo.setMarkUpload(true);
        photo.setError(1);
        photo.setErrorTxt("Rejected by server");
        Gson gson = new Gson();
        String before = gson.toJson(photo);
        assertTrue(VisitPhotoUploadPolicy.record(batch, photo, true, json("{state:true}")));
        VisitUploadBatch restored = gson.fromJson(gson.toJson(batch), VisitUploadBatch.class);
        assertTrue(VisitPhotoUploadPolicy.ready(restored, photo));
        assertEquals(before, gson.toJson(photo));
        assertEquals(0, photo.getUpload_to_server());
        assertEquals(0, photo.getGet_on_server());
    }

    @Test public void existingSuccessfulUploadDoesNotWaitForServerId() {
        StackPhotoDB photo = photo(1);
        photo.setUpload_to_server(150);
        assertTrue(VisitPhotoUploadPolicy.ready(new VisitUploadBatch(), photo));
        assertEquals(Collections.singleton("1"), VisitPhotoUploadPolicy.pendingServerIds(
                new VisitUploadBatch(), Collections.singletonList(photo)));
    }

    @Test public void existingServerPhotoNeedsNoNewHttpReceipt() {
        StackPhotoDB photo = photo(1);
        photo.setPhotoServerId("99");
        assertTrue(VisitPhotoUploadPolicy.ready(new VisitUploadBatch(), photo));
        assertTrue(VisitPhotoUploadPolicy.pendingServerIds(new VisitUploadBatch(), Collections.singletonList(photo)).isEmpty());
        photo.setPhotoServerId(null);
        photo.setGet_on_server(150);
        assertTrue(VisitPhotoUploadPolicy.ready(new VisitUploadBatch(), photo));
    }

    @Test public void anotherPhotoAndReplacementCannotReuseReceipt() {
        VisitUploadBatch batch = new VisitUploadBatch();
        StackPhotoDB photo = photo(1);
        VisitPhotoUploadPolicy.record(batch, photo, true, null);
        assertFalse(VisitPhotoUploadPolicy.ready(batch, photo(2)));
        photo.setPhoto_hash("replacement");
        assertFalse(VisitPhotoUploadPolicy.ready(batch, photo));
        assertFalse(VisitPhotoUploadPolicy.ready(new VisitUploadBatch(), photo(1)));
    }

    @Test public void lateOldPhotoReceiptDoesNotReleaseReplacement() {
        VisitUploadBatch batch = new VisitUploadBatch();
        StackPhotoDB sent = photo(1);
        StackPhotoDB replaced = photo(1);
        replaced.setPhoto_hash("new-hash");
        VisitPhotoUploadPolicy.record(batch, sent, true, null);
        assertFalse(VisitPhotoUploadPolicy.ready(batch, replaced));
    }

    @Test public void photoWithoutHashUsesPathAndCreationTime() {
        VisitUploadBatch batch = new VisitUploadBatch();
        StackPhotoDB photo = photo(1);
        photo.setPhoto_hash(null);
        VisitPhotoUploadPolicy.record(batch, photo, true, null);
        assertTrue(VisitPhotoUploadPolicy.ready(batch, photo));
        photo.setCreate_time(101);
        assertFalse(VisitPhotoUploadPolicy.ready(batch, photo));
        photo.setCreate_time(100);
        photo.setPhoto_num("/photos/replaced.jpg");
        assertFalse(VisitPhotoUploadPolicy.ready(batch, photo));
    }

    @Test public void oldJournalsHaveNoAssumedPhotoReceipts() {
        VisitUploadBatch old = new Gson().fromJson("{userId:7,dad2:123,rows:{}}", VisitUploadBatch.class);
        assertFalse(VisitPhotoUploadPolicy.ready(old, photo(1)));
        old.photoHttpReceipts = null;
        assertFalse(VisitPhotoUploadPolicy.ready(old, photo(1)));
        assertTrue(VisitPhotoUploadPolicy.record(old, photo(1), true, null));
    }

    @Test public void everyNewPhotoRequiresItsOwnAttempt() {
        VisitUploadBatch batch = new VisitUploadBatch();
        StackPhotoDB first = photo(1), second = photo(2);
        VisitPhotoUploadPolicy.record(batch, first, true, null);
        assertTrue(VisitPhotoUploadPolicy.allReady(batch, Collections.singletonList(first)));
        assertFalse(VisitPhotoUploadPolicy.allReady(batch, Arrays.asList(first, second)));
        VisitPhotoUploadPolicy.record(batch, second, true, null);
        assertTrue(VisitPhotoUploadPolicy.allReady(batch, Arrays.asList(first, second)));
    }

    @Test public void missingIdentityCannotCreateReusableReceipt() {
        StackPhotoDB photo = photo(1);
        photo.setPhoto_hash(null);
        photo.setPhoto_num(null);
        assertFalse(VisitPhotoUploadPolicy.record(new VisitUploadBatch(), photo, true, null));
    }

    @Test public void metadataWithoutIdRemainsPendingButDoesNotBlockConduct() {
        VisitUploadBatch batch = new VisitUploadBatch();
        for (VisitUploadBatch.Kind kind : Arrays.asList(PHOTO_COMMENT, PHOTO_DVI, PHOTO_SCORE)) {
            batch.merge(Collections.singletonList(new VisitUploadBatch.Row(kind, "1", json("{element_id:'1',text:'pending'}"))));
        }
        StackPhotoDB photo = photo(1);
        VisitPhotoUploadPolicy.record(batch, photo, true, null);
        Set<String> pendingIds = VisitPhotoUploadPolicy.pendingServerIds(batch, Collections.singletonList(photo));
        assertTrue(batch.allAcknowledged(pendingIds));
        assertTrue(batch.next(Collections.emptySet(), pendingIds).isEmpty());
        assertFalse(batch.allAcknowledged());
        assertEquals(3, batch.rows.size());
        for (VisitUploadBatch.Row row : batch.rows.values()) {
            assertNull(row.acknowledgement);
            assertFalse(row.applied);
        }
    }

    @Test public void conductFailureRetriesPhotosOncePerNotification() {
        VisitUploadBatch batch = new VisitUploadBatch();
        StackPhotoDB photo = photo(1);
        VisitPhotoUploadPolicy.record(batch, photo, true, null);
        assertTrue(batch.retryAfterConductFailure(10));
        assertFalse(VisitPhotoUploadPolicy.ready(batch, photo));
        VisitPhotoUploadPolicy.record(batch, photo, true, null);
        assertFalse(batch.retryAfterConductFailure(10));
        assertTrue(VisitPhotoUploadPolicy.ready(batch, photo));
        assertTrue(batch.retryAfterConductFailure(11));
        assertFalse(VisitPhotoUploadPolicy.ready(batch, photo));
    }

    @Test public void metadataWithServerIdStillRequiresItsOwnAcknowledgement() {
        VisitUploadBatch batch = new VisitUploadBatch();
        for (VisitUploadBatch.Kind kind : Arrays.asList(PHOTO_COMMENT, PHOTO_DVI, PHOTO_SCORE)) {
            batch.merge(Collections.singletonList(new VisitUploadBatch.Row(kind, "1", json("{element_id:'1',id:'99'}"))));
        }
        assertFalse(batch.allAcknowledged(Collections.singleton("1")));
        assertFalse(batch.next(Collections.emptySet(), Collections.singleton("1")).isEmpty());
    }

    @Test public void photoReceiptNeverBypassesOtherQueues() {
        for (VisitUploadBatch.Kind kind : VisitUploadBatch.Kind.values()) {
            if (kind == PHOTO_COMMENT || kind == PHOTO_DVI || kind == PHOTO_SCORE) continue;
            VisitUploadBatch batch = new VisitUploadBatch();
            batch.merge(Collections.singletonList(new VisitUploadBatch.Row(kind, "1", json("{element_id:'1'}"))));
            assertFalse("Unconfirmed " + kind, batch.allAcknowledged(Collections.singleton("1")));
            assertEquals(1, batch.next(Collections.emptySet(), Collections.singleton("1")).size());
        }
    }

    @Test public void metadataForPhotoWithoutSuccessfulAttemptStillBlocks() {
        VisitUploadBatch batch = new VisitUploadBatch();
        batch.merge(Collections.singletonList(new VisitUploadBatch.Row(PHOTO_COMMENT, "2", json("{element_id:'2'}"))));
        assertFalse(batch.allAcknowledged(Collections.singleton("1")));
    }

    @Test public void arrivingServerIdMakesDeferredMetadataUploadable() {
        VisitUploadBatch batch = new VisitUploadBatch();
        batch.merge(Collections.singletonList(new VisitUploadBatch.Row(PHOTO_COMMENT, "1", json("{element_id:'1',text:'text'}"))));
        assertTrue(batch.allAcknowledged(Collections.singleton("1")));
        batch.merge(Collections.singletonList(new VisitUploadBatch.Row(PHOTO_COMMENT, "1", json("{element_id:'1',id:'99',text:'text'}"))));
        assertFalse(batch.allAcknowledged(Collections.emptySet()));
        assertEquals(1, batch.next(Collections.emptySet(), Collections.emptySet()).size());
    }
}
