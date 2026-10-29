package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Test;

import java.util.Collections;

import ua.com.merchik.merchik.data.HashElements;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.WpDataServer;

import static org.junit.Assert.*;
import static ua.com.merchik.merchik.ServerExchange.RnoVisitDataPolicy.*;

public class RnoVisitDataPolicyTest {
    private static final long DAD2 = 1291026027512061443L;

    @Test public void ordinaryVisitsDoNotNeedPreparation() {
        assertFalse(needsPreparation(123, null));
        assertFalse(needsPreparation(123, " "));
    }

    @Test public void rnoWithoutHashMustNotBypassGate() {
        assertTrue(needsPreparation(14041, null));
    }

    @Test public void acceptedVisitKeepsPreparationRequirement() {
        assertTrue(needsPreparation(123, "access-key"));
    }

    @Test public void optionsRequestPairsExactDad2WithHash() {
        JsonObject request = request(Part.OPTIONS, DAD2, "access-key");
        assertEquals("plan", request.get("mod").getAsString());
        assertEquals("options_list", request.get("act").getAsString());
        assertEquals(Long.toString(DAD2), request.getAsJsonArray("code_dad2").get(0).getAsString());
        assertEquals("access-key", request.getAsJsonArray("code_dad2_hash").get(0).getAsString());
        assertEquals(1, request.getAsJsonArray("code_dad2_hash").size());
    }

    @Test public void rpRequestUsesFullVisitSnapshotNotGlobalVpi() {
        JsonObject request = request(Part.REPORT_PREPARE, DAD2, "access-key");
        assertEquals("report_prepare", request.get("mod").getAsString());
        assertEquals("list_data", request.get("act").getAsString());
        assertEquals(0, request.get("dt_change_from").getAsLong());
        assertFalse(request.has("date_from"));
        assertFalse(request.has("date_to"));
    }

    @Test public void productsUseExistingDad2ContractWithoutGlobalVpi() {
        JsonObject request = request(Part.PRODUCTS, DAD2, "access-key");
        assertEquals("data_list", request.get("mod").getAsString());
        assertEquals("tovar_list", request.get("act").getAsString());
        assertEquals(0, request.get("dt").getAsLong());
        assertFalse(request.has("code_dad2_hash"));
        assertEquals(Long.toString(DAD2), request.getAsJsonArray("code_dad2").get(0).getAsString());
    }

    @Test(expected = IllegalArgumentException.class) public void missingHashIsNotAnUnscopedRequest() {
        request(Part.OPTIONS, DAD2, " ");
    }

    @Test(expected = IllegalArgumentException.class) public void missingDad2IsNotAnUnscopedRequest() {
        request(Part.REPORT_PREPARE, 0, "access-key");
    }

    @Test public void scopeSeparatesUsersVisitsAndRotatedKeys() {
        String scope = dataScope(12, DAD2, "key-a");
        assertEquals(scope, dataScope(12, DAD2, "key-a"));
        assertNotEquals(scope, dataScope(13, DAD2, "key-a"));
        assertNotEquals(scope, dataScope(12, DAD2 + 1, "key-a"));
        assertNotEquals(scope, dataScope(12, DAD2, "key-b"));
        assertFalse(scope.contains("key-a"));
    }

    @Test public void requestedVisitKeySurvivesHashRotation() {
        assertEquals("12:dad2=" + DAD2, visitScope(12, DAD2));
    }

    @Test(expected = IllegalArgumentException.class) public void anonymousStateIsNotSaved() {
        visitScope(0, DAD2);
    }

    @Test public void existingRowsWithoutReceiptAreNotLoaded() {
        assertFalse(loaded(false, 0, 100));
    }

    @Test public void successfulEmptyResponseIsLoaded() {
        assertTrue(loaded(true, 0, 0));
        assertTrue(ready(true, 2, true, true, 0));
    }

    @Test public void lostRowsInvalidateReceipt() {
        assertFalse(loaded(true, 30, 29));
        assertFalse(loaded(true, 30, 0));
    }

    @Test public void localAdditionsDoNotDiscardLoadedState() {
        assertTrue(loaded(true, 30, 31));
    }

    @Test public void partialDownloadCannotUnlockWork() {
        assertFalse(ready(true, 20, false, true, 0));
        assertFalse(ready(false, 20, true, true, 0));
        assertFalse(ready(true, 20, true, false, 0));
    }

    @Test public void missingProductCardsCannotUnlockWork() {
        assertFalse(ready(true, 20, true, true, 1));
    }

    @Test public void emptyOptionsAreNotSuccessfulControl() {
        assertFalse(ready(true, 0, true, true, 0));
    }

    @Test public void rpResponseCannotContainAnotherVisit() {
        assertTrue(validReportRow(123L, Long.toString(DAD2), "321", DAD2));
        assertFalse(validReportRow(123L, Long.toString(DAD2 + 1), "321", DAD2));
    }

    @Test public void incompleteRpRowDoesNotMarkVisitLoaded() {
        assertFalse(validReportRow(null, Long.toString(DAD2), "321", DAD2));
        assertFalse(validReportRow(0L, Long.toString(DAD2), "321", DAD2));
        assertFalse(validReportRow(123L, Long.toString(DAD2), null, DAD2));
        assertFalse(validReportRow(123L, Long.toString(DAD2), " ", DAD2));
        assertFalse(validReportRow(123L, Long.toString(DAD2), "0", DAD2));
    }

    @Test public void responseMapAttachesHashToExactVisit() {
        String json = "{\"state\":true,\"list\":[{\"code_dad2\":" + DAD2
                + "},{\"code_dad2\":" + (DAD2 + 1) + "}],\"hash_elements\":{\"code_dad2\":{\""
                + DAD2 + "\":\"visit-key\"}}}";
        WpDataServer response = new Gson().fromJson(json, WpDataServer.class);
        assertEquals("visit-key", response.getList().get(0).getCode_dad2_hash());
        assertNull(response.getList().get(1).getCode_dad2_hash());
        assertEquals("visit-key", response.getList().get(0).getCode_dad2_hash());
    }

    @Test public void absentOrEmptyMapDoesNotEraseInlineHash() {
        WpDataDB row = new WpDataDB();
        row.setCode_dad2(DAD2);
        row.setCode_dad2_hash("inline-key");
        WpDataServer response = new WpDataServer();
        response.setList(Collections.singletonList(row));
        assertEquals("inline-key", response.getList().get(0).getCode_dad2_hash());
        HashElements hashes = new HashElements();
        hashes.setCodeDad2(Collections.singletonMap(Long.toString(DAD2), ""));
        response.setHashElements(hashes);
        assertEquals("inline-key", response.getList().get(0).getCode_dad2_hash());
    }

    @Test public void responseWithNullRowsDoesNotCrashHashMapping() {
        WpDataServer response = new WpDataServer();
        HashElements hashes = new HashElements();
        hashes.setCodeDad2(Collections.singletonMap(Long.toString(DAD2), "key"));
        response.setHashElements(hashes);
        assertNull(response.getList());
        response.setList(Collections.singletonList(null));
        assertNull(response.getList().get(0));
    }
}
