package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.ServerExchange.TablesExchange.SiteObjectsSyncPolicy;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SiteObjectsResponse;

import static org.junit.Assert.*;

public class SiteObjectsSyncPolicyTest {
    @Test public void requestUsesExistingEndpointAndUnixSeconds() {
        JsonObject request = SiteObjectsSyncPolicy.request(2, 1793263380L);
        assertEquals("site_objects", request.get("mod").getAsString());
        assertEquals("list", request.get("act").getAsString());
        assertEquals("2", request.get("lang_id").getAsString());
        assertEquals("1793263380", request.get("dt_change_from").getAsString());
        assertEquals(4, request.size());
    }

    @Test public void initialRequestIncludesZeroWatermark() {
        assertEquals("0", SiteObjectsSyncPolicy.request(2, 0).get("dt_change_from").getAsString());
    }

    @Test public void negativeWatermarkCannotReachServer() {
        assertEquals("0", SiteObjectsSyncPolicy.request(2, -100).get("dt_change_from").getAsString());
    }

    @Test(expected = IllegalArgumentException.class) public void invalidLanguageIsRejected() {
        SiteObjectsSyncPolicy.request(0, 0);
    }

    @Test public void bothStoresReadyUsesSavedWatermark() {
        assertEquals(1700000000L, SiteObjectsSyncPolicy.requestWatermark(1700000000L, 1793263500L, true, true));
    }

    @Test public void emptyOrWrongLanguageRoomRequiresFullDownload() {
        assertEquals(0, SiteObjectsSyncPolicy.requestWatermark(1700000000L, 1793263500L, false, true));
    }

    @Test public void emptyOrWrongLanguageRealmRequiresFullDownload() {
        assertEquals(0, SiteObjectsSyncPolicy.requestWatermark(1700000000L, 1793263500L, true, false));
    }

    @Test public void bothStoresMissingRequireFullDownload() {
        assertEquals(0, SiteObjectsSyncPolicy.requestWatermark(1700000000L, null, false, false));
    }

    @Test public void successUsesRequestStartMinusTwoMinutes() {
        assertEquals(1793263380L, SiteObjectsSyncPolicy.savedWatermark(1700000000L, 1793263500L));
    }

    @Test public void unavailableTrustedClockKeepsWatermark() {
        assertEquals(1700000000L, SiteObjectsSyncPolicy.savedWatermark(1700000000L, null));
        assertEquals(0, SiteObjectsSyncPolicy.savedWatermark(0, null));
    }

    @Test public void futureWatermarkIsCorrected() {
        assertEquals(1793263380L, SiteObjectsSyncPolicy.requestWatermark(1999999999L, 1793263500L, true, true));
    }

    @Test public void negativeAndEarlyTimeCannotProduceNegativeVpi() {
        assertEquals(0, SiteObjectsSyncPolicy.requestWatermark(-1, null, true, true));
        assertEquals(0, SiteObjectsSyncPolicy.savedWatermark(0, 10L));
    }

    @Test public void emptySuccessfulDeltaIsAccepted() {
        assertTrue(SiteObjectsSyncPolicy.rows(response(), 2, false).isEmpty());
    }

    @Test(expected = IllegalStateException.class) public void emptyFullResponseCannotClearDictionaries() {
        SiteObjectsSyncPolicy.rows(response(), 2, true);
    }

    @Test(expected = IllegalStateException.class) public void nullBodyIsRejected() {
        SiteObjectsSyncPolicy.rows(null, 2, false);
    }

    @Test(expected = IllegalStateException.class) public void missingListIsRejected() {
        SiteObjectsResponse response = response();
        response.objectSQLList = null;
        SiteObjectsSyncPolicy.rows(response, 2, false);
    }

    @Test(expected = IllegalStateException.class) public void falseStateIsRejected() {
        SiteObjectsResponse response = response();
        response.state = false;
        SiteObjectsSyncPolicy.rows(response, 2, false);
    }

    @Test(expected = IllegalStateException.class) public void missingStateIsRejected() {
        SiteObjectsResponse response = response();
        response.state = null;
        SiteObjectsSyncPolicy.rows(response, 2, false);
    }

    @Test(expected = IllegalStateException.class) public void serverErrorIsRejected() {
        SiteObjectsResponse response = response();
        response.error = "no access";
        SiteObjectsSyncPolicy.rows(response, 2, false);
    }

    @Test public void blankErrorIsNotFailure() {
        SiteObjectsResponse response = response();
        response.error = " ";
        assertTrue(SiteObjectsSyncPolicy.rows(response, 2, false).isEmpty());
    }

    @Test(expected = IllegalStateException.class) public void nullRowIsRejected() {
        SiteObjectsSyncPolicy.rows(response((SiteObjectsSDB) null), 2, false);
    }

    @Test(expected = IllegalStateException.class) public void missingIdIsRejected() {
        SiteObjectsSyncPolicy.rows(response(new SiteObjectsSDB()), 2, false);
    }

    @Test(expected = IllegalStateException.class) public void duplicateIdsAreRejected() {
        SiteObjectsSyncPolicy.rows(response(row(7), row(7)), 2, false);
    }

    @Test public void languageIsStampedEvenWhenNotReturnedByServer() {
        SiteObjectsSDB row = row(7);
        SiteObjectsSyncPolicy.rows(response(row), 3, true);
        assertEquals("3", row.langId);
    }

    @Test public void invalidBatchDoesNotPartiallyChangeLanguage() {
        SiteObjectsSDB row = row(7);
        try {
            SiteObjectsSyncPolicy.rows(response(row, null), 3, true);
            fail("Expected rejection");
        } catch (IllegalStateException expected) {
            assertNull(row.langId);
        }
    }

    @Test public void sameResponsePreservesAllSharedFieldsInRealmAndAdditionalIdInRoom() {
        String json = "{\"state\":true,\"object_list\":[{\"ID\":45,\"additional_id\":901,"
                + "\"ID_1c\":\"abc\",\"nm\":\"name\",\"comments\":\"comment\","
                + "\"script_mod\":\"mod\",\"script_act\":\"act\",\"lesson_id\":\"12\","
                + "\"platform_id\":\"3\",\"object_type\":\"4\",\"dt_change\":\"1793263380\","
                + "\"author_id\":\"17\",\"nm_translation\":\"translated\","
                + "\"comments_translation\":\"translated comment\"}]}";
        List<SiteObjectsSDB> rows = SiteObjectsSyncPolicy.rows(new Gson().fromJson(json, SiteObjectsResponse.class), 2, true);
        SiteObjectsDB copy = SiteObjectsSyncPolicy.realmRows(rows).get(0);
        assertEquals(45, copy.getID());
        assertEquals("abc", copy.getID1c());
        assertEquals("name", copy.getNm());
        assertEquals("comment", copy.getComments());
        assertEquals("mod", copy.getScriptMod());
        assertEquals("act", copy.getScriptAct());
        assertEquals("12", copy.getLessonId());
        assertEquals("3", copy.getPlatformId());
        assertEquals("4", copy.getObjectType());
        assertEquals("1793263380", copy.getDtChange());
        assertEquals("17", copy.getAuthorId());
        assertEquals("translated", copy.getNmTranslation());
        assertEquals("translated comment", copy.getCommentsTranslation());
        assertEquals("2", copy.getLangId());
        assertEquals(Integer.valueOf(901), rows.get(0).additionalId);
    }

    @Test public void nullTranslationsArePreservedToClearPreviousValues() {
        SiteObjectsDB copy = SiteObjectsSyncPolicy.realmRows(Collections.singletonList(row(7))).get(0);
        assertNull(copy.getNmTranslation());
        assertNull(copy.getCommentsTranslation());
    }

    @Test public void emptyDeltaHasNoRealmWrites() {
        assertTrue(SiteObjectsSyncPolicy.realmRows(Collections.emptyList()).isEmpty());
    }

    private static SiteObjectsSDB row(int id) {
        SiteObjectsSDB row = new SiteObjectsSDB();
        row.id = id;
        return row;
    }

    private static SiteObjectsResponse response(SiteObjectsSDB... rows) {
        SiteObjectsResponse response = new SiteObjectsResponse();
        response.state = true;
        response.objectSQLList = Arrays.asList(rows);
        return response;
    }
}
