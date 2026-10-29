package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.FilteredSyncPolicy;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ModImagesView;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ModImagesViewList;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest;
import ua.com.merchik.merchik.retrofit.RetrofitInterface;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static org.junit.Assert.*;
import static ua.com.merchik.merchik.ServerExchange.PhotoMetadataPolicy.*;

public class PhotoMetadataPolicyTest {
    private final Gson gson = new Gson();
    private JsonObject json(String text) { return gson.fromJson(text, JsonObject.class); }
    private ModImagesViewList row() {
        return gson.fromJson("{\"ID\":\"42\",\"dt\":1000,\"dt_upload\":\"1200\",\"merchik_id\":\"7\","
                + "\"addr_id\":\"9\",\"client_id\":\"9382\",\"photo_tp\":\"4\",\"dvi\":\"1\","
                + "\"code_dad2\":\"1211026021586061512\",\"img_hash\":\"hash\",\"tovar_id\":\"123\","
                + "\"showcase_id\":\"5\",\"code_iza\":\"iza\",\"example_id\":\"78\",\"example_img_id\":\"80\","
                + "\"planogram_id\":\"6\",\"planogram_img_id\":\"8\",\"photo_url\":\"https://example/photo\","
                + "\"comments\":\"server text\"}", ModImagesViewList.class);
    }
    private StackPhotoDB local() {
        StackPhotoDB photo = new StackPhotoDB();
        photo.setId(17);
        photo.setPhotoServerId("42");
        photo.setPhoto_num("/local/file.jpg");
        photo.setComment("local text");
        photo.setDvi(0);
        photo.setVpi(100);
        return photo;
    }
    private ModImagesView response(ModImagesViewList... items) {
        ModImagesView response = new ModImagesView();
        response.setState(true); response.setList(Arrays.asList(items)); return response;
    }

    @Test public void cursorIsSecondsAndNeverDtUpload() {
        JsonObject request = request(json("{\"dt_upload\":\"123\"}"), 1793263380L);
        assertEquals("1793263380", request.get("dt_change_from").getAsString());
        assertFalse(request.has("dt_upload"));
        assertEquals("images_view", request.get("mod").getAsString());
        assertEquals("list_image", request.get("act").getAsString());
    }
    @Test public void dtoSerializesNewCursor() {
        PhotoTableRequest request = new PhotoTableRequest(); request.dt_change_from = "123";
        assertEquals("123", gson.toJsonTree(request).getAsJsonObject().get("dt_change_from").getAsString());
        assertFalse(gson.toJson(request).contains("dt_upload"));
    }
    @Test public void scopeIgnoresVpiAndPropertyOrder() {
        assertEquals(scope(7, json("{\"photo_type\":\"10\",\"date_from\":\"2026-10-01\",\"dt_change_from\":\"5\"}")),
                scope(7, json("{\"date_from\":\"2026-10-01\",\"photo_type\":\"10\",\"dt_change_from\":\"8\"}")));
    }
    @Test public void scopesSeparateUsersTypesAndDateWindows() {
        JsonObject first = json("{\"date_from\":\"2026-10-01\",\"photo_type\":\"10\"}");
        assertNotEquals(scope(7, first), scope(8, first));
        assertNotEquals(scope(7, first), scope(7, json("{\"date_from\":\"2026-10-01\",\"photo_type\":\"31\"}")));
        assertNotEquals(scope(7, first), scope(7, json("{\"date_from\":\"2026-10-20\",\"photo_type\":\"10\"}")));
    }
    @Test public void targetedIdsAreNormalized() {
        assertEquals("41,42", filters(json("{\"id_list\":\"42,41,42\"}")).get("id_list").getAsString());
    }
    @Test(expected = IllegalArgumentException.class) public void emptyIdDoesNotBecomeUnfilteredDownload() { filters(json("{\"id_list\":\"\"}")); }
    @Test public void successfulEmptyDeltaAdvancesAtStartTime() {
        assertTrue(rows(response(), new JsonObject()).isEmpty());
        SynchronizationCursorSDB saved = FilteredSyncPolicy.savedCursor(RESOURCE, "scope", 100, 1000L, 0);
        assertEquals(880, saved.vpi);
    }
    @Test public void unknownClockKeepsCursor() { assertEquals(100, FilteredSyncPolicy.savedCursor(RESOURCE, "scope", 100, null, 0).vpi); }
    @Test(expected = IllegalStateException.class) public void failedStateRejected() { ModImagesView r = response(); r.setState(false); rows(r, new JsonObject()); }
    @Test(expected = IllegalStateException.class) public void sqlErrorWithTrueStateAndEmptyListIsRejected() {
        ModImagesView response = gson.fromJson("{\"state\":true,\"list\":[],\"error\":\"SQLSTATE: real_comment_dt ambiguous\"}", ModImagesView.class);
        rows(response, new JsonObject());
    }
    @Test(expected = IllegalStateException.class) public void errorRejectsNonemptyResponseToo() {
        ModImagesView response = gson.fromJson("{\"state\":true,\"list\":[{\"ID\":\"42\"}],\"error\":\"partial result\"}", ModImagesView.class);
        rows(response, new JsonObject());
    }
    @Test public void emptyErrorAllowsSuccessfulEmptyDelta() {
        ModImagesView response = gson.fromJson("{\"state\":true,\"list\":[],\"error\":\"  \"}", ModImagesView.class);
        assertTrue(rows(response, new JsonObject()).isEmpty());
    }
    @Test public void oldUnvalidatedCursorsAreNotReused() {
        assertNotEquals("images_view/list_image/metadata-v2", RESOURCE);
    }
    @Test(expected = IllegalStateException.class) public void nullListRejected() { ModImagesView r = response(); r.setList(null); rows(r, new JsonObject()); }
    @Test(expected = IllegalStateException.class) public void paginatedResponseCannotAdvanceVpi() { ModImagesView r = response(row()); r.setTotalPages(2); rows(r, new JsonObject()); }
    @Test public void identicalDuplicatesSavedOnce() { assertEquals(1, rows(response(row(), row()), new JsonObject()).size()); }
    @Test(expected = IllegalArgumentException.class) public void conflictingDuplicatesRejected() { ModImagesViewList changed = row(); changed.setComments("different"); rows(response(row(), changed), new JsonObject()); }
    @Test(expected = IllegalArgumentException.class) public void invalidNumberRejectedBeforeSave() { ModImagesViewList r = row(); r.setDvi("oops"); uniqueRows(Collections.singletonList(r)); }
    @Test(expected = IllegalArgumentException.class) public void nullRowRejected() { uniqueRows(Collections.singletonList(null)); }
    @Test(expected = IllegalArgumentException.class) public void zeroServerIdRejected() { ModImagesViewList r = row(); r.setID("0"); uniqueRows(Collections.singletonList(r)); }
    @Test(expected = IllegalStateException.class) public void missingRequestedIdRejected() { rows(response(row()), json("{\"id_list\":\"42,43\"}")); }
    @Test(expected = IllegalStateException.class) public void unexpectedIdRejected() { rows(response(row()), json("{\"id_list\":\"43\"}")); }
    @Test public void allRequestedIdsAccepted() { assertEquals(1, rows(response(row()), json("{\"id_list\":\"42\"}")).size()); }
    @Test public void newPhotoContainsFullMetadataWithoutFile() {
        StackPhotoDB p = new StackPhotoDB(); merge(p, row(), true, 200);
        assertEquals(1211026021586061512L, p.code_dad2); assertEquals("hash", p.photo_hash);
        assertEquals("123", p.tovar_id); assertEquals("5", p.showcase_id); assertEquals("iza", p.getCode_iza());
        assertEquals("78", p.example_id); assertEquals("80", p.example_img_id);
        assertEquals("6", p.planogram_id); assertEquals("8", p.planogram_img_id);
        assertEquals("server text", p.comment); assertNull(p.photo_num); assertEquals(0, p.vpi);
        assertEquals(1000000, p.create_time); assertEquals(1200000, p.get_on_server);
    }
    @Test public void existingIncompletePhotoIsBackfilled() { StackPhotoDB p = local(); merge(p, row(), false, 200); assertEquals(1211026021586061512L, p.code_dad2); assertEquals("78", p.example_id); assertEquals("hash", p.photo_hash); }
    @Test public void serverCanChangeSyncedCommentAndDvi() { StackPhotoDB p = local(); merge(p, row(), false, 200); assertEquals("server text", p.comment); assertEquals(Integer.valueOf(1), p.dvi); }
    @Test public void serverCanClearSyncedComment() { StackPhotoDB p = local(); ModImagesViewList r = row(); r.setComments(""); merge(p, r, false, 200); assertEquals("", p.comment); }
    @Test public void pendingLocalCommentWins() { StackPhotoDB p = local(); p.commentUpload = true; merge(p, row(), false, 200); assertEquals("local text", p.comment); assertTrue(p.commentUpload); }
    @Test public void pendingClearedCommentIsNotResurrected() { StackPhotoDB p = local(); p.comment = ""; p.commentUpload = true; merge(p, row(), false, 200); assertEquals("", p.comment); assertTrue(p.commentUpload); }
    @Test public void pendingDviWins() { StackPhotoDB p = local(); p.dviUpload = true; merge(p, row(), false, 200); assertEquals(Integer.valueOf(0), p.dvi); assertTrue(p.dviUpload); }
    @Test public void editAcknowledgedDuringDownloadStillWins() { StackPhotoDB p = local(); p.vpi = 200; merge(p, row(), false, 200); assertEquals("local text", p.comment); assertEquals(Integer.valueOf(0), p.dvi); }
    @Test public void fileRatingAndFlagsAreNeverOverwritten() {
        StackPhotoDB p = local(); p.mark = "8"; p.markUpload = true; p.premiya = "20"; p.premiyaUpload = true;
        p.upload_to_server = 5000; p.get_on_server = 6000; merge(p, row(), false, 200);
        assertEquals(17, p.getId()); assertEquals("/local/file.jpg", p.photo_num); assertEquals("8", p.mark);
        assertEquals("20", p.premiya); assertTrue(p.markUpload); assertTrue(p.premiyaUpload);
        assertEquals(5000, p.upload_to_server); assertEquals(6000, p.get_on_server); assertEquals(100, p.vpi);
    }
    @Test public void absentOptionalFieldsDoNotClearExistingValues() {
        StackPhotoDB p = local(); ModImagesViewList sparse = new ModImagesViewList(); sparse.setID("42");
        merge(p, sparse, false, 200); assertEquals("local text", p.comment); assertEquals(Integer.valueOf(0), p.dvi);
    }
    @Test public void hashFallbackRequiresVisitUserAndProduct() {
        StackPhotoDB p = new StackPhotoDB(); merge(p, row(), true, 200); p.photoServerId = null;
        assertTrue(matchesUnassignedLocal(p, row())); p.tovar_id = "other"; assertFalse(matchesUnassignedLocal(p, row()));
        p.tovar_id = "123"; p.code_dad2++; assertFalse(matchesUnassignedLocal(p, row()));
    }
    @Test public void hashCannotReassignDifferentServerId() { StackPhotoDB p = new StackPhotoDB(); merge(p, row(), true, 200); p.photoServerId = "43"; assertFalse(matchesHashResponse(p, row())); }
    @Test public void hashRejectsDifferentOwner() { StackPhotoDB p = new StackPhotoDB(); merge(p, row(), true, 200); p.user_id = 8; assertFalse(matchesHashResponse(p, row())); }
    @Test public void hashQueryHasZeroVpiAndNoOldCursorWithoutExecutingNetwork() {
        RetrofitInterface api = new Retrofit.Builder().baseUrl("https://example.invalid/")
                .addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitInterface.class);
        retrofit2.Call<ModImagesView> call = api.PHOTO_METADATA_BY_HASH("images_view", "list_image", "1", "0", Collections.singletonList("hash"));
        assertEquals("0", call.request().url().queryParameter("dt_change_from"));
        assertNull(call.request().url().queryParameter("dt_upload")); assertFalse(call.isExecuted());
    }
}
