package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okio.Buffer;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.CatalogSyncPolicy.Table;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammAddressSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammTypeSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShowcaseResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammTypeResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammVizitShowcaseResponse;
import ua.com.merchik.merchik.retrofit.RetrofitInterface;

import static org.junit.Assert.*;

public class CatalogSyncPolicyTest {
    private static final long NOW = 1795070000L;
    private static final List<Long> IDS = Arrays.asList(42L, 3L, 42L, null, 0L, -1L, 3000000000L);

    @Test public void everyCatalogUsesItsOwnExclusionParameterAndNoVpi() {
        assertEquals(7, Table.values().length);
        for (Table table : Table.values()) {
            JsonObject json = CatalogSyncPolicy.request(table, table.baseRequest(), IDS, false);
            String key = table == Table.SHOWCASES ? "id_exclude" : "id_exclude_old";
            assertEquals(table.mod, json.get("mod").getAsString());
            assertEquals(table.act, json.get("act").getAsString());
            assertEquals("1", json.get("nolimit").getAsString());
            assertEquals("[3,42,3000000000]", json.getAsJsonArray(key).toString());
            assertFalse(json.has("dt_change_from"));
            assertEquals(4, json.size());
        }
    }

    @Test public void fullRefreshRemovesBothExclusionsAndLegacyVpi() {
        for (Table table : Table.values()) {
            JsonObject base = table.baseRequest();
            base.addProperty("dt_change_from", NOW);
            base.add("id_exclude", new JsonArray());
            base.add("id_exclude_old", new JsonArray());
            JsonObject request = CatalogSyncPolicy.request(table, base, IDS, true);
            assertEquals(table.baseRequest(), request);
            assertTrue(base.has("dt_change_from"));
        }
    }

    @Test public void exclusionsAlsoStripLegacyVpiAndKeepFilters() {
        JsonObject base = Table.SHOWCASES.baseRequest();
        base.addProperty("active_only", "1");
        base.addProperty("dt_change_from", NOW);
        base.add("id_exclude_old", new JsonArray());
        JsonArray addresses = new JsonArray();
        addresses.add("123");
        base.add("addr_id", addresses);
        JsonObject request = CatalogSyncPolicy.request(Table.SHOWCASES, base, IDS, false);
        assertFalse(request.has("dt_change_from"));
        assertFalse(request.has("id_exclude_old"));
        assertEquals("1", request.get("active_only").getAsString());
        assertEquals(addresses, request.get("addr_id"));
        request.getAsJsonArray("addr_id").add("456");
        assertEquals(1, addresses.size());
    }

    @Test public void emptyLocalTableHasNoExclusions() {
        for (Table table : Table.values()) {
            assertEquals(table.baseRequest(), CatalogSyncPolicy.request(table, table.baseRequest(),
                    Collections.emptyList(), false));
        }
    }

    @Test public void firstRunAlwaysRefreshesPreviouslyLoadedRows() {
        assertTrue(CatalogSyncPolicy.fullDue(NOW, null, 250));
    }

    @Test public void nextFullRefreshIsDueAtExactly24Hours() {
        SynchronizationCursorSDB receipt = receipt(NOW, 250);
        assertFalse(CatalogSyncPolicy.fullDue(NOW, receipt, 250));
        assertFalse(CatalogSyncPolicy.fullDue(NOW + 3600, receipt, 250));
        assertFalse(CatalogSyncPolicy.fullDue(NOW + 86399, receipt, 250));
        assertTrue(CatalogSyncPolicy.fullDue(NOW + 86400, receipt, 250));
        assertTrue(CatalogSyncPolicy.fullDue(NOW + 172800, receipt, 250));
    }

    @Test public void localDataLossForcesFullRefreshBeforeDailyDeadline() {
        assertTrue(CatalogSyncPolicy.fullDue(NOW + 1, receipt(NOW, 250), 249));
        assertTrue(CatalogSyncPolicy.fullDue(NOW + 1, receipt(NOW, 250), 0));
        assertFalse(CatalogSyncPolicy.fullDue(NOW + 1, receipt(NOW, 250), 251));
    }

    @Test public void clockRollbackOrInvalidReceiptForcesFullRefresh() {
        assertTrue(CatalogSyncPolicy.fullDue(NOW - 1, receipt(NOW, 10), 10));
        assertTrue(CatalogSyncPolicy.fullDue(NOW, receipt(0, 10), 10));
    }

    @Test public void dailyReceiptsAreSeparateFromEveryOldWatermark() {
        Set<String> keys = new HashSet<>();
        for (Table table : Table.values()) {
            assertTrue(keys.add(table.receiptResource()));
            assertNotEquals(table.resource(), table.receiptResource());
            assertTrue(table.receiptResource().startsWith("catalog_full_refresh/"));
        }
    }

    @Test public void accountsAndShowcaseFiltersHaveIndependentDailyReceipts() {
        JsonObject base = Table.SHOWCASES.baseRequest();
        assertEquals(CatalogSyncPolicy.scope(7, base), CatalogSyncPolicy.scope(7, base.deepCopy()));
        assertNotEquals(CatalogSyncPolicy.scope(7, base), CatalogSyncPolicy.scope(8, base));
        JsonObject changed = base.deepCopy();
        changed.addProperty("addr_id", "123");
        assertNotEquals(CatalogSyncPolicy.scope(7, base), CatalogSyncPolicy.scope(7, changed));
    }

    @Test public void partialDeltaNeverPostponesDailyFullRefresh() {
        assertNull(CatalogSyncPolicy.fullReceipt(Table.PLANOGRAMS, "scope", false, NOW, 20, 250));
    }

    @Test public void emptyFullResponseDoesNotCreateFirstReceiptOrAdvanceExistingOne() {
        assertNull(CatalogSyncPolicy.fullReceipt(Table.PLANOGRAMS, "scope", true, NOW, 0, 0));
        assertNull(CatalogSyncPolicy.fullReceipt(Table.PLANOGRAMS, "scope", true, NOW, 0, 250));
    }

    @Test public void invalidCompletionTimeDoesNotSaveReceipt() {
        assertNull(CatalogSyncPolicy.fullReceipt(Table.PLANOGRAMS, "scope", true, 0, 20, 250));
    }

    @Test public void nonemptyReplayCountsAsSuccessfulFullRefresh() {
        SynchronizationCursorSDB receipt = CatalogSyncPolicy.fullReceipt(Table.PLANOGRAMS, "scope", true,
                NOW, 250, 250);
        assertEquals(NOW, receipt.vpi);
        assertEquals(250, receipt.itemCount);
        assertEquals("scope", receipt.scope);
        assertEquals(Table.PLANOGRAMS.receiptResource(), receipt.resource);
    }

    @Test public void rackExcludesSelectIdsNotLocalPrimaryKeys() {
        List<Long> ids = CatalogSyncPolicy.showcaseIds(Arrays.asList(
                showcase(1, "65753", 84006), showcase(2, "58543", 84006), showcase(3, "65753", 84006)));
        assertEquals(Arrays.asList(58543L, 65753L), ids);
    }

    @Test public void rackBackfillsMissingMetadataEvenForSharedSelectId() {
        assertEquals(Collections.singletonList(58543L), CatalogSyncPolicy.showcaseIds(Arrays.asList(
                showcase(1, "65753", 84006), showcase(2, "65753", null), showcase(3, "58543", 84006))));
    }

    @Test public void rackKeepsExistingSpecialExceptionsAndIgnoresInvalidIds() {
        assertEquals(Collections.singletonList(33L), CatalogSyncPolicy.showcaseIds(Arrays.asList(
                showcase(1, "444", 1), showcase(2, "1028", 1), showcase(3, "0", 1),
                showcase(4, "-2", 1), showcase(5, "bad", 1), showcase(6, null, 1),
                showcase(7, " 33 ", 1))));
    }

    @Test public void relatedCatalogsExcludeParentPlanogramsNotLinkIds() {
        List<PlanogrammAddressSDB> rows = Arrays.asList(address(1260, 1), address(1261, 1), address(1262, 5));
        List<Long> ids = CatalogSyncPolicy.exclusionIds(rows, row -> row.planogramId, row -> true);
        assertEquals(Arrays.asList(1L, 5L), ids);
        for (Table table : Arrays.asList(Table.ADDRESSES, Table.GROUPS, Table.IMAGES, Table.TYPES)) {
            assertEquals("planogram_id", table.exclusionIdField());
            assertEquals("[1,5]", CatalogSyncPolicy.request(table, table.baseRequest(), ids, false)
                    .getAsJsonArray("id_exclude_old").toString());
        }
        assertEquals("ID", Table.PLANOGRAMS.exclusionIdField());
        assertEquals("ID", Table.VISITS.exclusionIdField());
        assertEquals("select_id", Table.SHOWCASES.exclusionIdField());
    }

    @Test public void typeLinksConvertStringParentIdToNumber() {
        PlanogrammTypeSDB row = new PlanogrammTypeSDB(19, " 5 ", null, null, null);
        assertEquals(Collections.singletonList(5L), CatalogSyncPolicy.exclusionIds(
                Collections.singletonList(row), PlanogrammTypeSDB::getPlanogram_id, item -> true));
    }

    @Test public void invalidExclusionKeysAreNotSent() {
        assertEquals(Collections.singletonList(5L), CatalogSyncPolicy.exclusionIds(
                Arrays.asList(null, "", "0", "-1", "bad", "9223372036854775808", " 5 ", "5"),
                value -> value, value -> true));
    }

    @Test public void pendingRowsAreNotExcludedEvenWhenKeyIsShared() {
        assertEquals(Collections.singletonList(5L), CatalogSyncPolicy.exclusionIds(
                Arrays.asList(address(1, 1), address(2, 1), address(3, 5)),
                row -> row.planogramId, row -> row.id != 2));
        assertEquals(Collections.singletonList(6L), CatalogSyncPolicy.exclusionIds(
                Arrays.asList(visit(5, 1, "pending"), visit(6, 0, "downloaded")),
                row -> row.id, row -> !Integer.valueOf(1).equals(row.uploadStatus)));
    }

    @Test public void downloadedRowsStillValidateTheirOwnPrimaryIds() {
        List<PlanogrammAddressSDB> rows = Arrays.asList(address(1260, 1), address(1261, 1));
        assertEquals(rows, CatalogSyncPolicy.rows(true, null, rows, row -> row.id));
        assertThrows(IllegalStateException.class, () -> CatalogSyncPolicy.rows(true, null,
                Arrays.asList(address(1260, 1), address(1260, 5)), row -> row.id));
    }

    @Test public void missingLinkForSameParentForcesFullRefresh() {
        List<PlanogrammAddressSDB> rows = Arrays.asList(address(1260, 1), address(1261, 1));
        SynchronizationCursorSDB receipt = CatalogSyncPolicy.fullReceipt(Table.ADDRESSES, "scope", true,
                NOW, rows.size(), rows.size());
        List<PlanogrammAddressSDB> remaining = Collections.singletonList(rows.get(0));
        assertEquals(CatalogSyncPolicy.exclusionIds(rows, row -> row.planogramId, row -> true),
                CatalogSyncPolicy.exclusionIds(remaining, row -> row.planogramId, row -> true));
        assertTrue(CatalogSyncPolicy.fullDue(NOW + 1, receipt, remaining.size()));
    }

    @Test public void expectedEmptyExclusionResponseDoesNotSaveReceipt() {
        for (List<Integer> list : Arrays.<List<Integer>>asList(null, Collections.emptyList())) {
            List<Integer> rows = CatalogSyncPolicy.rows(false, "За заданими умовами список відсутній", list, id -> id);
            assertTrue(rows.isEmpty());
            assertNull(CatalogSyncPolicy.fullReceipt(Table.PLANOGRAMS, "scope", true, NOW, rows.size(), 551));
        }
    }

    @Test public void otherServerErrorsAreStillRejected() {
        for (String error : Arrays.asList(null, "", "Unauthorized", "failed")) {
            assertThrows(IllegalStateException.class, () -> CatalogSyncPolicy.rows(false, error,
                    Collections.<Integer>emptyList(), id -> id));
        }
        assertThrows(IllegalStateException.class, () -> CatalogSyncPolicy.rows(false,
                "За заданими умовами список відсутній", Collections.singletonList(1), id -> id));
        assertThrows(IllegalStateException.class, () -> CatalogSyncPolicy.rows(true,
                "failed", Collections.singletonList(1), id -> id));
    }

    @Test public void downloadedVisitRowsCannotOverwritePendingLocalChanges() {
        PlanogrammVizitShowcaseSDB pending = visit(5, 1, "local edit");
        PlanogrammVizitShowcaseSDB dirtyNewRow = visit(6, 1, "local new");
        PlanogrammVizitShowcaseSDB serverOld = visit(5, null, "server old");
        PlanogrammVizitShowcaseSDB serverNew = visit(7, null, "server new");
        List<PlanogrammVizitShowcaseSDB> saved = CatalogSyncPolicy.visitRowsToSave(
                Arrays.asList(serverOld, serverNew), Collections.emptyList(), Arrays.asList(pending, dirtyNewRow));
        assertEquals(Collections.singletonList(serverNew), saved);
        assertEquals(Integer.valueOf(0), serverNew.uploadStatus);
        assertEquals(Integer.valueOf(1), pending.uploadStatus);
        assertEquals("local edit", pending.comments);
        assertEquals("local new", dirtyNewRow.comments);
        assertNull(serverOld.uploadStatus);
    }

    @Test public void acknowledgedVisitRowsCanBeRefreshed() {
        PlanogrammVizitShowcaseSDB server = visit(5, null, "updated");
        assertEquals(Collections.singletonList(server), CatalogSyncPolicy.visitRowsToSave(
                Collections.singletonList(server), Collections.singletonList(visit(5, 0, "old")),
                Collections.singletonList(visit(5, 0, "old"))));
        assertEquals(Integer.valueOf(0), server.uploadStatus);
    }

    @Test public void uploadAcknowledgedDuringDownloadDoesNotAllowStaleResponseToOverwriteIt() {
        PlanogrammVizitShowcaseSDB server = visit(5, null, "old");
        assertTrue(CatalogSyncPolicy.visitRowsToSave(Collections.singletonList(server),
                Collections.singletonList(visit(5, 1, "new")),
                Collections.singletonList(visit(5, 0, "new"))).isEmpty());
    }

    @Test public void retrofitSerializesCatalogBodiesWithoutExecutingNetworkCalls() throws Exception {
        RetrofitInterface api = new Retrofit.Builder().baseUrl("https://example.invalid/")
                .addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitInterface.class);
        for (Table table : Arrays.asList(Table.SHOWCASES, Table.PLANOGRAMS, Table.ADDRESSES, Table.GROUPS, Table.IMAGES)) {
            for (boolean full : new boolean[]{true, false}) {
                JsonObject json = CatalogSyncPolicy.request(table, table.baseRequest(), IDS, full);
                Call<?> call;
                switch (table) {
                    case SHOWCASES: call = api.SHOWCASE_UPLOAD("application/json", json); break;
                    case PLANOGRAMS: call = api.Planogramm_RESPONSE("application/json", json); break;
                    case ADDRESSES: call = api.Planogramm_ADDRESS_RESPONSE("application/json", json); break;
                    case GROUPS: call = api.Planogramm_GROUP_RESPONSE("application/json", json); break;
                    case IMAGES: call = api.Planogramm_IMAGES_RESPONSE("application/json", json); break;
                    default: throw new AssertionError(table);
                }
                assertEquals("POST", call.request().method());
                Buffer body = new Buffer();
                call.request().body().writeTo(body);
                assertEquals(json, new Gson().fromJson(body.readUtf8(), JsonObject.class));
                assertFalse(call.isExecuted());
            }
        }
    }

    @Test public void rxCatalogEndpointsSerializeRequestsAndParseOwnRowIdsWithoutNetwork() {
        JsonObject[] sent = new JsonObject[1];
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain -> {
            Buffer buffer = new Buffer();
            chain.request().body().writeTo(buffer);
            sent[0] = new Gson().fromJson(buffer.readUtf8(), JsonObject.class);
            return new okhttp3.Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                    .code(200).message("OK").body(ResponseBody.create(null,
                            "{\"state\":true,\"list\":[{\"ID\":42,\"planogram_id\":1104}]}"))
                    .build();
        }).build();
        RetrofitInterface api = new Retrofit.Builder().baseUrl("https://example.invalid/").client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.createSynchronous())
                .build().create(RetrofitInterface.class);
        for (boolean full : new boolean[]{true, false}) {
            JsonObject types = CatalogSyncPolicy.request(Table.TYPES, Table.TYPES.baseRequest(), IDS, full);
            PlanogrammTypeResponse typeRows = api.Planogramm_TYPE_RESPONSE("application/json", types).blockingGet();
            assertEquals(types, sent[0]);
            assertEquals(Integer.valueOf(42), ReferenceSyncPolicy.rows(typeRows.state, typeRows.error,
                    typeRows.list, row -> row.getID()).get(0).getID());
            JsonObject visits = CatalogSyncPolicy.request(Table.VISITS, Table.VISITS.baseRequest(), IDS, full);
            PlanogrammVizitShowcaseResponse visitRows = api.PLANOGRAMM_VIZIT_SHOWCASE_RESPONSE("application/json", visits).blockingGet();
            assertEquals(visits, sent[0]);
            assertEquals(Integer.valueOf(42), ReferenceSyncPolicy.rows(visitRows.state, visitRows.error,
                    visitRows.list, row -> row.id).get(0).id);
        }
    }

    @Test public void serverErrorsAreNotLostInShowcaseAndVisitDtos() {
        String json = "{\"state\":true,\"error\":\"failed\",\"list\":[{\"ID\":42}]}";
        ShowcaseResponse showcases = new Gson().fromJson(json, ShowcaseResponse.class);
        PlanogrammVizitShowcaseResponse visits = new Gson().fromJson(json, PlanogrammVizitShowcaseResponse.class);
        assertEquals("failed", showcases.error);
        assertEquals("failed", visits.error);
        assertThrows(IllegalStateException.class,
                () -> ReferenceSyncPolicy.rows(showcases.state, showcases.error, showcases.list, row -> row.id));
        assertThrows(IllegalStateException.class,
                () -> ReferenceSyncPolicy.rows(visits.state, visits.error, visits.list, row -> row.id));
    }

    private static SynchronizationCursorSDB receipt(long time, int count) {
        SynchronizationCursorSDB result = new SynchronizationCursorSDB();
        result.vpi = time;
        result.itemCount = count;
        return result;
    }

    private static ShowcaseSDB showcase(int id, String selectId, Integer mainOptionId) {
        ShowcaseSDB row = new ShowcaseSDB();
        row.id = id;
        row.selectId = selectId;
        row.mainOptionId = mainOptionId;
        return row;
    }

    private static PlanogrammVizitShowcaseSDB visit(int id, Integer uploadStatus, String comments) {
        PlanogrammVizitShowcaseSDB row = new PlanogrammVizitShowcaseSDB();
        row.id = id;
        row.uploadStatus = uploadStatus;
        row.comments = comments;
        return row;
    }

    private static PlanogrammAddressSDB address(int id, int planogramId) {
        PlanogrammAddressSDB row = new PlanogrammAddressSDB();
        row.id = id;
        row.planogramId = planogramId;
        return row;
    }
}
