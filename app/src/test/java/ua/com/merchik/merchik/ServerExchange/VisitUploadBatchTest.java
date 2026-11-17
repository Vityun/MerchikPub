package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;
import static ua.com.merchik.merchik.ServerExchange.VisitUploadBatch.Kind.*;

public class VisitUploadBatchTest {
    @Test public void failureNotificationRequeuesOnlyConfirmedUpdates() {
        for (VisitUploadBatch.Kind kind : Arrays.asList(WP, RP, PHOTO_COMMENT, PHOTO_DVI, PHOTO_SCORE,
                VOTE, AR, TAR, EKL, PAUSE, PLANOGRAM)) {
            VisitUploadBatch batch = new VisitUploadBatch();
            VisitUploadBatch.Row row = row(kind, "1");
            batch.merge(Collections.singletonList(row));
            batch.acknowledge(row, json("{state:true}"));
            row.applied = true;
            batch.complete = true;
            batch.retryAfter = Long.MAX_VALUE;
            assertTrue(batch.retryAfterConductFailure(14214942));
            assertFalse(batch.complete);
            assertEquals(0, batch.retryAfter);
            assertEquals(1, batch.next().size());
            assertFalse(row.applied);
        }
    }

    @Test public void recoveryDoesNotDuplicateConfirmedCreatesAndAppendOnlyData() {
        for (VisitUploadBatch.Kind kind : Arrays.asList(ACHIEVEMENT, TAR_CREATE, TAR_COMMENT,
                PLAN_BUDGET, ANSWER, LOCATION, LOG)) {
            VisitUploadBatch batch = new VisitUploadBatch();
            VisitUploadBatch.Row row = row(kind, "1");
            batch.merge(Collections.singletonList(row));
            batch.acknowledge(row, json("{state:true,id:123}"));
            row.applied = true;
            batch.retryAfterConductFailure(14214942);
            assertTrue(batch.next().isEmpty());
            assertTrue(batch.allAcknowledged());
        }
    }

    @Test public void recoveryPreservesEveryUnconfirmedKind() {
        for (VisitUploadBatch.Kind kind : VisitUploadBatch.Kind.values()) {
            VisitUploadBatch batch = new VisitUploadBatch();
            batch.merge(Collections.singletonList(row(kind, "1")));
            batch.retryAfterConductFailure(14214942);
            assertEquals(1, batch.next().size());
            assertFalse(batch.allAcknowledged());
        }
    }

    @Test public void duplicateMessageAfterRestartDoesNotResetRetryOrAcknowledgements() {
        VisitUploadBatch batch = new VisitUploadBatch();
        VisitUploadBatch.Row row = row(RP, "1");
        batch.merge(Collections.singletonList(row));
        batch.retryAfterConductFailure(14214942);
        batch.acknowledge(row, json("{state:true}"));
        row.applied = true;
        batch.complete = true;
        batch.retryAfter = 12345;
        VisitUploadBatch loaded = new Gson().fromJson(new Gson().toJson(batch), VisitUploadBatch.class);
        assertFalse(loaded.retryAfterConductFailure(14214942));
        assertEquals(12345, loaded.retryAfter);
        assertTrue(loaded.complete);
        assertTrue(loaded.allAcknowledged());
        assertTrue(loaded.next().isEmpty());
        assertTrue(loaded.retryAfterConductFailure(14214943));
        assertEquals(1, loaded.next().size());
    }

    @Test public void crashBeforeLocalAcknowledgementDoesNotDiscardServerReceipt() {
        VisitUploadBatch batch = new VisitUploadBatch();
        VisitUploadBatch.Row row = row(RP, "1");
        batch.merge(Collections.singletonList(row));
        batch.acknowledge(row, json("{state:true}"));
        batch.retryAfterConductFailure(14214942);
        assertNotNull(row.acknowledgement);
        assertFalse(row.applied);
        assertFalse(batch.allAcknowledged());
    }

    @Test public void legacyUploadJournalSupportsRecoveryAndKeepsNewerEdits() {
        VisitUploadBatch batch = new Gson().fromJson("{\"userId\":176053,\"dad2\":123,\"rows\":{}}", VisitUploadBatch.class);
        assertNotNull(batch.recoveryMessages);
        batch.merge(Collections.singletonList(row(WP, "1")));
        batch.merge(Collections.singletonList(new VisitUploadBatch.Row(WP, "1", json("{value:2}"))));
        batch.retryAfterConductFailure(14214942);
        assertEquals(2, batch.next().get(0).data.get("value").getAsInt());
    }

    private static JsonObject json(String value) { return JsonParser.parseString(value).getAsJsonObject(); }
    private static VisitUploadBatch.Row row(VisitUploadBatch.Kind kind, String id) {
        return new VisitUploadBatch.Row(kind, id, json("{\"element_id\":\"" + id + "\",\"value\":1}"));
    }
    @Test public void emptyBodyDoesNotAcknowledgeAnything() {
        for (VisitUploadBatch.Kind kind : VisitUploadBatch.Kind.values()) {
            assertNull(VisitUploadBatch.accepted(row(kind, "1"), null));
            assertNull(VisitUploadBatch.accepted(row(kind, "1"), json("{}")));
        }
    }
    @Test public void topLevelRejectionWinsOverSuccessfulRows() {
        assertNull(VisitUploadBatch.accepted(row(RP, "1"), json("{state:false,data:[{element_id:1,state:true}]}")));
        assertNull(VisitUploadBatch.accepted(row(RP, "1"), json("{state:true,error:'denied',data:[{element_id:1,state:true}]}")));
    }
    @Test public void partialResponseAcknowledgesOnlyMatchingRows() {
        JsonObject body = json("{state:true,data:[{element_id:1,state:true},{element_id:2,state:false}]} ");
        assertNotNull(VisitUploadBatch.accepted(row(RP, "1"), body));
        assertNull(VisitUploadBatch.accepted(row(RP, "2"), body));
        assertNull(VisitUploadBatch.accepted(row(RP, "3"), body));
    }
    @Test public void duplicateOrConflictingAcknowledgementsAreRejected() {
        assertNull(VisitUploadBatch.accepted(row(VOTE, "1"), json("{state:true,list:[{element_id:1,state:true},{element_id:1,state:false}]}")));
        assertNull(VisitUploadBatch.accepted(row(VOTE, "1"), json("{state:true,list:[{element_id:1,state:true},{element_id:1,state:true}]}")));
    }
    @Test public void photoQueuesRequireExplicitItemAcceptance() {
        for (VisitUploadBatch.Kind kind : Arrays.asList(PHOTO_COMMENT, PHOTO_SCORE, PHOTO_DVI, VOTE)) {
            assertNull(VisitUploadBatch.accepted(row(kind, "1"), json("{state:true,list:[{element_id:1,state:false}]}")));
            assertNull(VisitUploadBatch.accepted(row(kind, "1"), json("{state:true,list:[{element_id:1}]}")));
            assertNotNull(VisitUploadBatch.accepted(row(kind, "1"), json("{state:true,list:[{element_id:1,state:true}]}")));
        }
    }
    @Test public void workPlanPerRowErrorCannotAuthorizeConduct() {
        assertNull(VisitUploadBatch.accepted(row(WP, "7"), json("{state:true,plan:[{element_id:7,error:'bad time'}]}")));
        assertNull(VisitUploadBatch.accepted(row(WP, "7"), json("{state:true,plan:[{element_id:8,data:{dt_update:true}}]}")));
        assertNotNull(VisitUploadBatch.accepted(row(WP, "7"), json("{state:true,plan:[{element_id:7,data:{dt_update:false}}]}")));
    }
    @Test public void singleRowNoChangeAcknowledgementIsSupported() {
        assertNotNull(VisitUploadBatch.accepted(row(WP, "7"), json("{state:true}")));
        assertNotNull(VisitUploadBatch.accepted(row(WP, "7"), json("{state:true,plan:[]}")));
        assertNull(VisitUploadBatch.accepted(row(WP, "7"), json("{state:true,error:'retry'}")));
    }
    @Test public void unexpectedResultEnvelopeCannotBeMistakenForNoChange() {
        for (VisitUploadBatch.Kind kind : Arrays.asList(WP, TAR, PAUSE)) {
            assertNull(VisitUploadBatch.accepted(row(kind, "7"), json("{state:true,data:[{element_id:7,state:false}]}")));
            assertNull(VisitUploadBatch.accepted(row(kind, "7"), json("{state:true,result:{error:'bad'}}")));
        }
    }
    @Test public void structuredErrorsAlsoKeepRowsPending() {
        assertNull(VisitUploadBatch.accepted(row(WP, "7"), json("{state:true,error:{message:'invalid'}}")));
        assertNull(VisitUploadBatch.accepted(row(RP, "7"), json("{state:true,data:[{element_id:7,state:true,error:['bad']}]}")));
    }
    @Test public void locationErrorWithGeoIdStillRemainsPending() {
        assertNull(VisitUploadBatch.accepted(row(LOCATION, "7"), json("{state:true,geo_result:{'7':{state:false,geo_id:333}}}")));
        assertNotNull(VisitUploadBatch.accepted(row(LOCATION, "7"), json("{state:true,geo_result:{'7':{state:true,geo_id:333}}}")));
    }
    @Test public void planogramsUseKeyedResults() {
        JsonObject body = json("{state:true,list:{'7':true,'8':false}}");
        assertNotNull(VisitUploadBatch.accepted(row(PLANOGRAM, "7"), body));
        assertNull(VisitUploadBatch.accepted(row(PLANOGRAM, "8"), body));
        assertNull(VisitUploadBatch.accepted(row(PLANOGRAM, "9"), body));
    }
    @Test public void returnedServerIdIsRequiredForCreates() {
        for (VisitUploadBatch.Kind kind : Arrays.asList(ACHIEVEMENT, PLAN_BUDGET, TAR_CREATE)) {
            String field = kind == PLAN_BUDGET ? "data" : "list";
            assertNull(VisitUploadBatch.accepted(row(kind, "7"), json("{state:true," + field + ":[{element_id:7,state:true}]}")));
            assertNotNull(VisitUploadBatch.accepted(row(kind, "7"), json("{state:true," + field + ":[{element_id:7,state:true,id:123}]}")));
        }
    }
    @Test public void additionalRequirementUsesItemIdNotLocalPrimaryKey() {
        VisitUploadBatch.Row row = new VisitUploadBatch.Row(AR, "local-user-99", json("{element_id:21,score:5}"));
        assertNotNull(VisitUploadBatch.accepted(row, json("{state:true,list:[{element_id:21,state:true}]}")));
        assertNotNull(VisitUploadBatch.accepted(row, json("{list:[{element_id:21,state:true}]}")));
        assertNull(VisitUploadBatch.accepted(row, json("{state:false,list:[{element_id:21,state:true}]}")));
        assertNull(VisitUploadBatch.accepted(row, json("{state:true,list:[{element_id:21}]}")));
    }
    @Test public void answersUseTheirExistingOptionalStateContract() {
        assertNotNull(VisitUploadBatch.accepted(row(ANSWER, "1794565326123"), json("{state:true,list:[{element_id:'1794565326123'}]}")));
        assertNull(VisitUploadBatch.accepted(row(ANSWER, "1794565326123"), json("{state:true,list:[{element_id:'1794565326123',error:'denied'}]}")));
    }
    @Test public void eklUsesReturnedId() {
        assertNotNull(VisitUploadBatch.accepted(row(EKL, "7"), json("{state:true,list:[{id:7,state:true}]}")));
        assertNull(VisitUploadBatch.accepted(row(EKL, "7"), json("{state:true,list:[{id:7,state:false}]}")));
    }
    @Test public void positionalTarCommentIsOnlyAcceptedForSingleRow() {
        assertNotNull(VisitUploadBatch.accepted(row(TAR_COMMENT, "temp"), json("{state:true,list:[{state:true,info:{comment_id:123}}]}")));
        assertNull(VisitUploadBatch.accepted(row(TAR_COMMENT, "temp"), json("{state:true,list:[{state:true},{state:true}]}")));
    }
    @Test public void oldAcknowledgementCannotClearNewerPayload() {
        VisitUploadBatch batch = new VisitUploadBatch();
        VisitUploadBatch.Row old = row(WP, "1");
        batch.merge(Collections.singletonList(old));
        batch.merge(Collections.singletonList(new VisitUploadBatch.Row(WP, "1", json("{element_id:'1',value:2}"))));
        assertFalse(batch.acknowledge(old, json("{state:true}")));
        assertEquals(1, batch.next().size());
    }
    @Test public void clearingDirtyFlagsElsewhereDoesNotDiscardPendingSnapshot() {
        VisitUploadBatch batch = new VisitUploadBatch();
        batch.merge(Collections.singletonList(row(VOTE, "1")));
        batch.merge(Collections.emptyList());
        assertEquals(1, batch.next().size());
    }
    @Test public void acknowledgedUnchangedPayloadIsNotSentAgain() {
        VisitUploadBatch batch = new VisitUploadBatch();
        VisitUploadBatch.Row row = row(VOTE, "1");
        batch.merge(Collections.singletonList(row));
        assertTrue(batch.acknowledge(row, json("{state:true}")));
        assertFalse(batch.merge(Collections.singletonList(row(VOTE, "1"))));
        assertTrue(batch.next().isEmpty());
    }
    @Test public void restartRetainsFailedRowsAndAcceptedRowsSeparately() {
        VisitUploadBatch batch = new VisitUploadBatch();
        batch.dad2 = 1161126028847061792L;
        batch.userId = 123;
        VisitUploadBatch.Row accepted = row(RP, "1");
        batch.merge(Arrays.asList(accepted, row(RP, "2")));
        batch.acknowledge(accepted, json("{state:true}"));
        VisitUploadBatch restored = new Gson().fromJson(new Gson().toJson(batch), VisitUploadBatch.class);
        assertEquals(batch.dad2, restored.dad2);
        assertEquals(123, restored.userId);
        assertEquals("2", restored.next().get(0).id);
        assertFalse(restored.rows.get("RP:1").applied);
    }
    @Test public void requestContainsOnlyUploadEndpointAndSelectedRows() {
        JsonObject request = VisitUploadBatch.request(Collections.singletonList(row(PLANOGRAM, "7")));
        assertEquals("vizit_showcase_save", request.get("act").getAsString());
        assertTrue(request.has("list"));
        assertFalse(request.has("dt_change_from"));
        request = VisitUploadBatch.request(Collections.singletonList(row(RP, "7")));
        assertEquals("set_report_data", request.get("act").getAsString());
        assertEquals(1, request.getAsJsonArray("data").size());
    }
    @Test public void batchBoundariesDoNotMixEndpointsOrPositionalResponses() {
        VisitUploadBatch batch = new VisitUploadBatch();
        batch.merge(Arrays.asList(row(TAR_COMMENT, "1"), row(TAR_COMMENT, "2"), row(VOTE, "3")));
        assertEquals(1, batch.next().size());
        batch = new VisitUploadBatch();
        batch.merge(Arrays.asList(row(RP, "1"), row(RP, "2"), row(VOTE, "3")));
        assertEquals(2, batch.next().size());
    }
    @Test public void logUsesKeyedServerReceiptAndRejectsExplicitFailure() {
        assertNotNull(VisitUploadBatch.accepted(row(LOG, "7"), json("{log:{'7':{id:888}}}")));
        assertNotNull(VisitUploadBatch.accepted(row(LOG, "7"), json("{log:{'7':{dt:1794565326}}}")));
        assertNull(VisitUploadBatch.accepted(row(LOG, "7"), json("{log:{'7':{error:'denied'}}}")));
        assertNull(VisitUploadBatch.accepted(row(LOG, "7"), json("{log:{'7':{id:888,state:false}}}")));
        assertNull(VisitUploadBatch.accepted(row(LOG, "7"), json("{state:false,log:{'7':{id:888}}}")));
        assertNull(VisitUploadBatch.accepted(row(LOG, "7"), json("{log:{'8':{id:888}}}")));
    }
    @Test public void failedQueueDoesNotBlockIndependentUploadsOrAllowCompletion() {
        VisitUploadBatch batch = new VisitUploadBatch();
        VisitUploadBatch.Row photo = row(PHOTO_COMMENT, "1");
        VisitUploadBatch.Row work = row(WP, "2");
        batch.merge(Arrays.asList(photo, work));
        assertEquals(work, batch.next(Collections.singleton(PHOTO_COMMENT)).get(0));
        assertTrue(batch.acknowledge(work, json("{state:true}")));
        work.applied = true;
        assertTrue(batch.next(Collections.singleton(PHOTO_COMMENT)).isEmpty());
        assertFalse(batch.allAcknowledged());
        assertEquals(photo, batch.next().get(0));
    }
    @Test public void receiptMustBeAppliedBeforeConductMayProceed() {
        VisitUploadBatch batch = new VisitUploadBatch();
        VisitUploadBatch.Row row = row(RP, "1");
        batch.merge(Collections.singletonList(row));
        batch.acknowledge(row, json("{state:true}"));
        assertFalse(batch.allAcknowledged());
        row.applied = true;
        assertTrue(batch.allAcknowledged());
        batch.complete = true;
        batch.merge(Collections.singletonList(new VisitUploadBatch.Row(RP, "1", json("{element_id:1,value:2}"))));
        assertFalse(batch.complete);
        assertFalse(batch.allAcknowledged());
    }
    @Test public void batchesAreBoundedAndRemainderStaysQueued() {
        VisitUploadBatch batch = new VisitUploadBatch();
        for (int i = 1; i <= 101; i++) batch.merge(Collections.singletonList(row(RP, String.valueOf(i))));
        assertEquals(100, batch.next().size());
        for (VisitUploadBatch.Row row : batch.next()) batch.acknowledge(row, json("{state:true}"));
        assertEquals("101", batch.next().get(0).id);
        assertEquals(101, batch.rows.size());
    }
    @Test public void createdTaskRekeysOnlyItsOwnPendingUpdatesAndComments() {
        VisitUploadBatch batch = new VisitUploadBatch();
        batch.merge(Arrays.asList(row(TAR, "1"),
                new VisitUploadBatch.Row(TAR_COMMENT, "temp", json("{id:'1',comment:'text'}")),
                new VisitUploadBatch.Row(TAR_COMMENT, "other", json("{id:'2',comment:'other'}"))));
        batch.reconcileCreatedTask("1", "99", 1161126028847061792L);
        assertFalse(batch.rows.containsKey("TAR:1"));
        assertEquals(99, batch.rows.get("TAR:99").data.get("element_id").getAsInt());
        assertEquals(1161126028847061792L, batch.rows.get("TAR:99").data.get("code_dad2").getAsLong());
        assertEquals("99", batch.rows.get("TAR_COMMENT:temp").data.get("id").getAsString());
        assertEquals("2", batch.rows.get("TAR_COMMENT:other").data.get("id").getAsString());
        assertTrue(batch.acknowledge(batch.rows.get("TAR:99"), json("{state:true}")));
    }
    @Test public void freshlyCollectedTaskEditWinsOverOldLocalIdSnapshot() {
        VisitUploadBatch batch = new VisitUploadBatch();
        batch.merge(Arrays.asList(row(TAR, "1"), new VisitUploadBatch.Row(TAR, "99", json("{element_id:99,value:2}"))));
        batch.reconcileCreatedTask("1", "99", 100L);
        assertEquals(1, batch.rows.size());
        assertEquals(2, batch.rows.get("TAR:99").data.get("value").getAsInt());
    }
    @Test public void ordinaryUploaderAssignedCommentIdIsReused() {
        VisitUploadBatch batch = new VisitUploadBatch();
        VisitUploadBatch.Row pending = new VisitUploadBatch.Row(TAR_COMMENT, "temp", json("{id:5,dt:'123',comment:'text'}"));
        pending.authorId = "7";
        batch.merge(Collections.singletonList(pending));
        batch.reconcileCommentId(pending, "99");
        assertFalse(batch.rows.containsKey("TAR_COMMENT:temp"));
        assertEquals("99", batch.next().get(0).data.get("comment_id").getAsString());
        assertEquals("7", new Gson().fromJson(new Gson().toJson(batch), VisitUploadBatch.class)
                .rows.get("TAR_COMMENT:99").authorId);
    }
    @Test public void rekeyingCommentDoesNotOverwriteNewerTextCollectedUnderServerId() {
        VisitUploadBatch batch = new VisitUploadBatch();
        VisitUploadBatch.Row old = new VisitUploadBatch.Row(TAR_COMMENT, "temp", json("{id:5,dt:'123',comment:'old'}"));
        batch.merge(Arrays.asList(old, new VisitUploadBatch.Row(TAR_COMMENT, "99",
                json("{id:5,dt:'123',comment_id:'99',comment:'new'}"))));
        batch.reconcileCommentId(old, "99");
        assertEquals(1, batch.rows.size());
        assertEquals("new", batch.next().get(0).data.get("comment").getAsString());
    }
    @Test public void newerMarkSupersedesPendingSnapshotWithDifferentLocalId() {
        VisitUploadBatch batch = new VisitUploadBatch();
        VisitUploadBatch.Row old = new VisitUploadBatch.Row(AR, "old", json("{element_id:21,tp_id:'0',score:1}"));
        batch.merge(Collections.singletonList(old));
        batch.merge(Collections.singletonList(new VisitUploadBatch.Row(AR, "new", json("{element_id:21,tp_id:'0',score:5}"))));
        assertEquals(1, batch.rows.size());
        assertEquals("new", batch.next().get(0).id);
        assertFalse(batch.acknowledge(old, json("{state:true}")));
    }
    @Test public void differentMarkTypesCannotShareAnAmbiguousResponseBatch() {
        VisitUploadBatch batch = new VisitUploadBatch();
        batch.merge(Arrays.asList(new VisitUploadBatch.Row(AR, "1", json("{element_id:21,tp_id:'0',score:1}")),
                new VisitUploadBatch.Row(AR, "2", json("{element_id:21,tp_id:'1',score:5}"))));
        assertEquals(2, batch.rows.size());
        assertEquals(1, batch.next().size());
        VisitUploadBatch.Row first = batch.next().get(0);
        batch.acknowledge(first, json("{state:true}"));
        assertEquals("2", batch.next().get(0).id);
    }
    @Test public void previouslyTrackedRowMustBeResnapshottedEvenAfterDirtyFlagClears() {
        VisitUploadBatch batch = new VisitUploadBatch();
        batch.merge(Collections.singletonList(row(PHOTO_SCORE, "7")));
        assertTrue(batch.tracks(PHOTO_SCORE, 7));
        assertFalse(batch.tracks(PHOTO_COMMENT, 7));
        VisitUploadBatch.Row updated = new VisitUploadBatch.Row(PHOTO_SCORE, 7, json("{element_id:'7',value:5}"));
        batch.merge(Collections.singletonList(updated));
        assertEquals(updated.data, batch.next().get(0).data);
    }
}
