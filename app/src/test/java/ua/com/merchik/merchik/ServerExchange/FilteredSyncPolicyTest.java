package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonObject;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import ua.com.merchik.merchik.ServerExchange.TablesExchange.FilteredSyncPolicy;
import ua.com.merchik.merchik.data.Database.Room.ShelfSizeSDB;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShelfSizeResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.TranslatesResponse;
import ua.com.merchik.merchik.data.Translation.SiteTranslationsList;

import static org.junit.Assert.*;

public class FilteredSyncPolicyTest {
    @Test public void shelfRequestKeepsDateRangeAndAddsUnixSeconds() {
        JsonObject request = FilteredSyncPolicy.shelfRequest(LocalDate.of(2026, 10, 29), 1793263380L);
        assertEquals("data_list", request.get("mod").getAsString());
        assertEquals("shelf_size", request.get("act").getAsString());
        assertEquals("2026-10-09", request.get("dt_from").getAsString());
        assertEquals("2026-10-30", request.get("dt_to").getAsString());
        assertEquals("1793263380", request.get("dt_change_from").getAsString());
        assertFalse(request.has("dt"));
    }

    @Test public void translationsUseDtAndNotDtChangeFrom() {
        JsonObject request = FilteredSyncPolicy.translationRequest(2, 1793263380L);
        assertEquals("translation", request.get("mod").getAsString());
        assertEquals("translation_list", request.get("act").getAsString());
        assertEquals("2", request.get("lang_id").getAsString());
        assertEquals("1793263380", request.get("dt").getAsString());
        assertFalse(request.has("dt_change_from"));
    }

    @Test public void firstRequestsContainZero() {
        assertEquals(0, FilteredSyncPolicy.translationRequest(2, 0).get("dt").getAsLong());
        assertEquals(0, FilteredSyncPolicy.shelfRequest(LocalDate.of(2026, 10, 29), 0).get("dt_change_from").getAsLong());
    }

    @Test public void negativeRequestTimesBecomeZero() {
        assertEquals(0, FilteredSyncPolicy.translationRequest(2, -10).get("dt").getAsLong());
    }

    @Test public void dateRangeChangesAcrossDayAndMonthBoundaries() {
        assertEquals("2026-10-09/2026-10-30", FilteredSyncPolicy.shelfScope(LocalDate.of(2026, 10, 29)));
        assertEquals("2026-10-10/2026-10-31", FilteredSyncPolicy.shelfScope(LocalDate.of(2026, 10, 30)));
        assertEquals("2026-09-11/2026-10-02", FilteredSyncPolicy.shelfScope(LocalDate.of(2026, 10, 1)));
    }

    @Test public void shelfDatesUseKyivCalendarIncludingDst() {
        assertEquals(LocalDate.of(2026, 10, 25), FilteredSyncPolicy.shelfDate(Instant.parse("2026-10-24T21:30:00Z").getEpochSecond()));
        assertEquals(LocalDate.of(2026, 10, 26), FilteredSyncPolicy.shelfDate(Instant.parse("2026-10-25T22:30:00Z").getEpochSecond()));
    }

    @Test public void differentLanguagesAndAccountsHaveDifferentScopes() {
        assertNotEquals(FilteredSyncPolicy.translationScope(1), FilteredSyncPolicy.translationScope(2));
        assertNotEquals(FilteredSyncPolicy.accountScope(10, "2"), FilteredSyncPolicy.accountScope(11, "2"));
    }

    @Test(expected = IllegalArgumentException.class) public void invalidLanguageIsRejected() {
        FilteredSyncPolicy.translationRequest(0, 0);
    }

    @Test public void missingScopeOrMissingDataRestartsAtZero() {
        assertEquals(0, FilteredSyncPolicy.requestWatermark(null, 2000L, true));
        assertEquals(0, FilteredSyncPolicy.requestWatermark(cursor(1000, 5), 2000L, false));
    }

    @Test public void validCursorIsSentUnchanged() {
        assertEquals(1000, FilteredSyncPolicy.requestWatermark(cursor(1000, 5), 2000L, true));
    }

    @Test public void futureCursorIsCorrectedByTrustedClock() {
        assertEquals(1880, FilteredSyncPolicy.requestWatermark(cursor(9000, 5), 2000L, true));
    }

    @Test public void successUsesStartTimeWithOverlap() {
        SynchronizationCursorSDB saved = FilteredSyncPolicy.savedCursor("resource", "scope", 1000, 2000L, 9);
        assertEquals(1880, saved.vpi);
        assertEquals(9, saved.itemCount);
        assertEquals("resource", saved.resource);
        assertEquals("scope", saved.scope);
    }

    @Test public void unknownTrustedClockDoesNotAdvanceVpi() {
        assertEquals(1000, FilteredSyncPolicy.savedCursor("r", "s", 1000, null, 0).vpi);
        assertEquals(0, FilteredSyncPolicy.savedCursor("r", "s", 0, null, 0).vpi);
    }

    @Test public void translationsRequireBothStoresToMatchLastSavedCount() {
        assertTrue(FilteredSyncPolicy.translationsReady(cursor(1000, 5), 5, 5));
        assertFalse(FilteredSyncPolicy.translationsReady(cursor(1000, 5), 0, 5));
        assertFalse(FilteredSyncPolicy.translationsReady(cursor(1000, 5), 5, 0));
        assertFalse(FilteredSyncPolicy.translationsReady(cursor(1000, 5), 5, 6));
    }

    @Test public void confirmedEmptyLanguageDoesNotCauseRepeatedFullDownloads() {
        assertTrue(FilteredSyncPolicy.translationsReady(cursor(1000, 0), 0, 0));
        assertFalse(FilteredSyncPolicy.translationsReady(null, 0, 0));
    }

    @Test public void emptySuccessfulResponsesAreAccepted() {
        assertTrue(FilteredSyncPolicy.shelfRows(shelves()).isEmpty());
        assertTrue(FilteredSyncPolicy.translationRows(translations(), 2).isEmpty());
    }

    @Test(expected = IllegalStateException.class) public void missingShelfBodyIsRejected() {
        FilteredSyncPolicy.shelfRows(null);
    }

    @Test(expected = IllegalStateException.class) public void missingTranslationBodyIsRejected() {
        FilteredSyncPolicy.translationRows(null, 2);
    }

    @Test(expected = IllegalStateException.class) public void falseStateIsRejected() {
        ShelfSizeResponse response = shelves();
        response.state = false;
        FilteredSyncPolicy.shelfRows(response);
    }

    @Test(expected = IllegalStateException.class) public void missingListIsRejected() {
        TranslatesResponse response = translations();
        response.list = null;
        FilteredSyncPolicy.translationRows(response, 2);
    }

    @Test(expected = IllegalStateException.class) public void serverErrorIsRejected() {
        TranslatesResponse response = translations();
        response.error = "Denied";
        FilteredSyncPolicy.translationRows(response, 2);
    }

    @Test(expected = IllegalStateException.class) public void missingShelfIdIsRejected() {
        FilteredSyncPolicy.shelfRows(shelves(new ShelfSizeSDB()));
    }

    @Test(expected = IllegalStateException.class) public void duplicateShelfIdIsRejected() {
        ShelfSizeSDB row = new ShelfSizeSDB(); row.id = 4;
        FilteredSyncPolicy.shelfRows(shelves(row, row));
    }

    @Test(expected = IllegalStateException.class) public void nullTranslationIsRejected() {
        FilteredSyncPolicy.translationRows(translations((TranslatesSDB) null), 2);
    }

    @Test(expected = IllegalStateException.class) public void duplicateTranslationIdIsRejected() {
        TranslatesSDB row = translation("4");
        FilteredSyncPolicy.translationRows(translations(row, row), 2);
    }

    @Test(expected = IllegalStateException.class) public void wrongLanguageIsRejected() {
        TranslatesSDB row = translation("4"); row.langId = "1";
        FilteredSyncPolicy.translationRows(translations(row), 2);
    }

    @Test public void missingLanguageIsFilledFromRequest() {
        TranslatesSDB row = translation("4");
        FilteredSyncPolicy.translationRows(translations(row), 2);
        assertEquals("2", row.langId);
    }

    @Test public void allTranslationFieldsReachRealm() {
        TranslatesSDB row = translation("4");
        row.num1c = "5"; row.internalName = "control"; row.langId = "2";
        row.defaultValue = "default"; row.nm = "name"; row.title = "title";
        row.scriptMod = "mod"; row.scriptAct = "act"; row.url = "url";
        row.platformId = "6"; row.dtUpdate = "1793263380";
        SiteTranslationsList copy = FilteredSyncPolicy.realmTranslations(Collections.singletonList(row)).get(0);
        assertEquals(row.id, copy.getID()); assertEquals(row.num1c, copy.getNum1c());
        assertEquals(row.internalName, copy.getInternalName()); assertEquals(row.langId, copy.getLangId());
        assertEquals(row.defaultValue, copy.getDefaultValue()); assertEquals(row.nm, copy.getNm());
        assertEquals(row.title, copy.getTitle()); assertEquals(row.scriptMod, copy.getScriptMod());
        assertEquals(row.scriptAct, copy.getScriptAct()); assertEquals(row.url, copy.getUrl());
        assertEquals(row.platformId, copy.getPlatformId()); assertEquals(row.dtUpdate, copy.getDtUpdate());
    }

    @Test public void nullTranslationFieldsAreNotReplacedWithOldValues() {
        SiteTranslationsList copy = FilteredSyncPolicy.realmTranslations(Collections.singletonList(translation("4"))).get(0);
        assertNull(copy.getNm()); assertNull(copy.getTitle());
    }

    private static SynchronizationCursorSDB cursor(long vpi, long count) {
        return FilteredSyncPolicy.savedCursor("r", "s", vpi, null, count);
    }

    private static TranslatesSDB translation(String id) {
        TranslatesSDB row = new TranslatesSDB(); row.id = id; return row;
    }

    private static ShelfSizeResponse shelves(ShelfSizeSDB... rows) {
        ShelfSizeResponse response = new ShelfSizeResponse();
        response.state = true; response.list = Arrays.asList(rows); return response;
    }

    private static TranslatesResponse translations(TranslatesSDB... rows) {
        TranslatesResponse response = new TranslatesResponse();
        response.state = true; response.list = Arrays.asList(rows); return response;
    }
}
