package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Test;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.merchik.merchik.ServerExchange.TablesExchange.FilteredSyncPolicy;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.ReferenceSyncPolicy;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.ReferenceSyncPolicy.Table;
import ua.com.merchik.merchik.data.Database.Room.ArticleSDB;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHints;
import ua.com.merchik.merchik.data.RealmModels.ArticleDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.QuestionAnswerResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.AdditionalMaterialsGroupsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.Location.LocationResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ArticleResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.StandartResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammAddressResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammGroupResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammImagesResponse;
import ua.com.merchik.merchik.retrofit.RetrofitInterface;

import static org.junit.Assert.*;

public class ReferenceSyncPolicyTest {
    private static final long NOW = 1793263380L;
    private final Gson gson = new Gson();

    @Test public void planogramImagesRequest() { request(Table.PLANOGRAM_IMAGES, "planogram", "img_list", true); }
    @Test public void standardsRequest() { request(Table.STANDART, "standart", "list", false); }
    @Test public void planogramAddressesRequest() { request(Table.PLANOGRAM_ADDRESSES, "planogram", "addr_list", true); }
    @Test public void articlesRequest() { request(Table.ARTICLES, "data_list", "tovar_vendor_code_list", false); }
    @Test public void planogramGroupsRequest() { request(Table.PLANOGRAM_GROUPS, "planogram", "group_list", true); }
    @Test public void lessonsRequest() { request(Table.LESSONS, "lesson", "list", false); }
    @Test public void questionsRequest() { request(Table.QUESTIONS, "quest_data", "list", false); }
    @Test public void locationsRequest() { request(Table.LOCATIONS, "location", "list", false); }
    @Test public void additionalMaterialGroupsRequest() { request(Table.ADDITIONAL_MATERIAL_GROUPS, "additional_materials", "group_list", false); }
    @Test public void samplePhotosRequest() { request(Table.SAMPLE_PHOTOS, "images_view", "list_example", false); }

    private void request(Table table, String mod, String act, boolean noLimit) {
        JsonObject request = ReferenceSyncPolicy.request(table, NOW);
        assertEquals(mod, request.get("mod").getAsString());
        assertEquals(act, request.get("act").getAsString());
        assertEquals(NOW, request.get("dt_change_from").getAsLong());
        assertEquals(10, request.get("dt_change_from").getAsString().length());
        assertEquals(noLimit, request.has("nolimit"));
        if (noLimit) assertEquals("1", request.get("nolimit").getAsString());
        assertEquals(noLimit ? 4 : 3, request.size());
    }

    @Test public void retrofitSerializesAllRequestBodiesWithoutNetwork() throws Exception {
        RetrofitInterface api = new Retrofit.Builder().baseUrl("https://example.invalid/")
                .addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitInterface.class);
        for (Table table : Table.values()) {
            JsonObject json = ReferenceSyncPolicy.request(table, NOW);
            Call<?> call;
            switch (table) {
                case PLANOGRAM_IMAGES: call = api.Planogramm_IMAGES_RESPONSE("application/json", json); break;
                case STANDART: call = api.GET_TABLE_STANDART("application/json", json); break;
                case PLANOGRAM_ADDRESSES: call = api.Planogramm_ADDRESS_RESPONSE("application/json", json); break;
                case ARTICLES: call = api.ARTICLE_DOWNLOAD("application/json", json); break;
                case PLANOGRAM_GROUPS: call = api.Planogramm_GROUP_RESPONSE("application/json", json); break;
                case LESSONS: call = api.DOWNLOAD_VIDEO_LESSONS("application/json", json); break;
                case QUESTIONS: call = api.GET_QUESTION_LIST("application/json", json); break;
                case LOCATIONS: call = api.LOCATION_RESPONSE_CALL("application/json", json); break;
                case ADDITIONAL_MATERIAL_GROUPS: call = api.AdditionalMaterialsGroupsResponse_JSON_UPLOAD("application/json", json); break;
                case SAMPLE_PHOTOS: call = api.GET_SAMPLE_PHOTO("application/json", json); break;
                default: throw new AssertionError(table);
            }
            assertEquals("POST", call.request().method());
            Buffer buffer = new Buffer();
            call.request().body().writeTo(buffer);
            assertEquals(json, gson.fromJson(buffer.readUtf8(), JsonObject.class));
            assertFalse(call.isExecuted());
        }
    }

    @Test public void eachEndpointHasIndependentCursorKey() {
        Set<String> resources = new HashSet<>();
        for (Table table : Table.values()) assertTrue(resources.add(table.resource()));
        assertEquals(10, resources.size());
        assertTrue(resources.contains("quest_data/list"));
        assertFalse(resources.contains("question_answer")); // Upload stores a local ID, not seconds.
    }

    @Test public void initialRequestsIncludeZero() {
        for (Table table : Table.values()) {
            assertEquals("0", ReferenceSyncPolicy.request(table, 0).get("dt_change_from").getAsString());
            assertEquals("0", ReferenceSyncPolicy.request(table, -1).get("dt_change_from").getAsString());
        }
    }

    @Test public void cursorUsesRequestStartNotResponseCompletion() {
        for (Table table : Table.values()) {
            SynchronizationCursorSDB cursor = FilteredSyncPolicy.savedCursor(table.resource(), "7:all", 0, NOW, 3);
            assertEquals(NOW - 120, cursor.vpi);
            assertEquals(NOW - 120, FilteredSyncPolicy.requestWatermark(cursor, NOW + 600, true));
            assertEquals(0, FilteredSyncPolicy.requestWatermark(cursor, NOW + 600, false));
        }
    }

    @Test public void unknownTrustedTimeDoesNotAdvanceCursor() {
        assertEquals(100, FilteredSyncPolicy.savedCursor("lesson/list", "7:all", 100, null, 1).vpi);
    }

    @Test public void accountsHaveSeparateScopes() {
        assertNotEquals(FilteredSyncPolicy.accountScope(1, "all"), FilteredSyncPolicy.accountScope(2, "all"));
    }

    @Test public void emptySuccessfulDeltaIsAccepted() {
        assertTrue(ReferenceSyncPolicy.rows(true, null, Collections.<Long>emptyList(), id -> id).isEmpty());
    }

    @Test public void validRowsAreNotFilteredOrReordered() {
        List<Long> rows = Arrays.asList(20L, 1L, 3000000000L);
        assertSame(rows, ReferenceSyncPolicy.rows(true, "", rows, id -> id));
    }

    @Test(expected = IllegalStateException.class) public void falseStateRejected() {
        ReferenceSyncPolicy.rows(false, null, Collections.<Long>emptyList(), id -> id);
    }
    @Test(expected = IllegalStateException.class) public void missingStateRejected() {
        ReferenceSyncPolicy.rows(null, null, Collections.<Long>emptyList(), id -> id);
    }
    @Test(expected = IllegalStateException.class) public void serverErrorRejectedEvenWithTrueState() {
        ReferenceSyncPolicy.rows(true, "error", Arrays.asList(1L), id -> id);
    }
    @Test(expected = IllegalStateException.class) public void missingListRejected() {
        ReferenceSyncPolicy.<Long>rows(true, null, null, id -> id);
    }
    @Test(expected = IllegalStateException.class) public void nullRowRejected() {
        ReferenceSyncPolicy.rows(true, null, Arrays.asList(1L, null), id -> id);
    }
    @Test(expected = IllegalStateException.class) public void missingIdRejected() {
        ReferenceSyncPolicy.rows(true, null, Arrays.asList(new ArticleSDB()), row -> row.id);
    }
    @Test(expected = IllegalStateException.class) public void zeroIdRejected() {
        ReferenceSyncPolicy.rows(true, null, Arrays.asList(0L), id -> id);
    }
    @Test(expected = IllegalStateException.class) public void negativeIdRejected() {
        ReferenceSyncPolicy.rows(true, null, Arrays.asList(-1L), id -> id);
    }
    @Test(expected = IllegalStateException.class) public void duplicateIdRejected() {
        ReferenceSyncPolicy.rows(true, null, Arrays.asList(1L, 1L), id -> id);
    }

    @Test public void typedServerResponsesKeepPrimaryKeys() {
        String json = "{\"state\":true,\"list\":[{\"ID\":42}]}";
        PlanogrammAddressResponse addresses = gson.fromJson(json, PlanogrammAddressResponse.class);
        assertEquals(42, ReferenceSyncPolicy.rows(addresses.state, addresses.error, addresses.list, r -> r.id).get(0).id.intValue());
        PlanogrammGroupResponse groups = gson.fromJson(json, PlanogrammGroupResponse.class);
        assertEquals(42, ReferenceSyncPolicy.rows(groups.state, groups.error, groups.list, r -> r.id).get(0).id.intValue());
        PlanogrammImagesResponse images = gson.fromJson(json, PlanogrammImagesResponse.class);
        assertEquals(42, ReferenceSyncPolicy.rows(images.state, images.error, images.list, r -> r.id).get(0).id.intValue());
        StandartResponse standards = gson.fromJson(json, StandartResponse.class);
        assertEquals(42, ReferenceSyncPolicy.rows(standards.state, standards.error, standards.list, r -> r.id).get(0).id.intValue());
        ArticleResponse articles = gson.fromJson(json, ArticleResponse.class);
        assertEquals(42, ReferenceSyncPolicy.rows(articles.state, articles.error, articles.list, r -> r.id).get(0).id.intValue());
        SiteHints lessons = gson.fromJson(json, SiteHints.class);
        assertEquals(42, ReferenceSyncPolicy.rows(lessons.getState(), lessons.error, lessons.getList(), r -> r.getID()).get(0).getID());
        QuestionAnswerResponse questions = gson.fromJson(json, QuestionAnswerResponse.class);
        assertEquals(42L, ReferenceSyncPolicy.rows(questions.getState(), questions.getError(), questions.getList(), r -> r.getId()).get(0).getId());
        AdditionalMaterialsGroupsResponse materials = gson.fromJson(json, AdditionalMaterialsGroupsResponse.class);
        assertEquals(42, ReferenceSyncPolicy.rows(materials.state, materials.error, materials.list, r -> r.id).get(0).id.intValue());
        LocationResponse locations = gson.fromJson(json, LocationResponse.class);
        assertEquals(42L, ReferenceSyncPolicy.rows(locations.state, locations.error, locations.list, r -> r.id).get(0).id.longValue());
    }

    @Test public void articleMappingPreservesAllRealmFields() {
        ArticleSDB row = gson.fromJson("{\"ID\":12,\"vendor_code\":\"A01\",\"tovar_id\":31,\"addr_tp_id\":6,\"dt_update\":1793263380}", ArticleSDB.class);
        JsonObject copy = gson.toJsonTree(ArticleDB.fromRoom(row)).getAsJsonObject();
        assertEquals("12", copy.get("iD").getAsString());
        assertEquals("A01", copy.get("vendorCode").getAsString());
        assertEquals("31", copy.get("tovarId").getAsString());
        assertEquals("6", copy.get("addrTpId").getAsString());
        assertEquals("1793263380", copy.get("dtUpdate").getAsString());
    }

    @Test public void articleMappingPreservesNulls() {
        ArticleSDB row = new ArticleSDB();
        row.id = 12;
        assertEquals("{\"iD\":\"12\"}", gson.toJson(ArticleDB.fromRoom(row)));
    }

    @Test public void lessonsAndArticlesExposeServerErrors() {
        String json = "{\"state\":true,\"list\":[],\"error\":\"failed\"}";
        assertEquals("failed", gson.fromJson(json, SiteHints.class).error);
        assertEquals("failed", gson.fromJson(json, ArticleResponse.class).error);
        assertEquals("failed", gson.fromJson(json, AdditionalMaterialsGroupsResponse.class).error);
        assertEquals("failed", gson.fromJson(json, LocationResponse.class).error);
    }
}
