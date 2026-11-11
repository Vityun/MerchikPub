package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonObject;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ua.com.merchik.merchik.data.synchronization.StartupPolicy;

import static org.junit.Assert.*;

public class ProductCardRetryPolicyTest {
    private static Set<String> ids(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    private static Set<String> attempt(Set<String> missing, Map<String, Long> stored) {
        Set<String> selected = ProductCardRetryPolicy.eligible(missing, stored);
        for (String id : selected) stored.put(id, ProductCardRetryPolicy.nextAttempt(stored.getOrDefault(id, 0L)));
        return selected;
    }

    @Test public void missingCardsAllowPlanAfterBatchAndOnlyFiveTargetedRetries() {
        Set<String> missing = ids("133901", "157629");
        Map<String, Long> stored = new HashMap<>();
        assertTrue(ProductCardRetryPolicy.needsBatch(false, false));
        boolean downloaded = true;
        assertTrue(StartupPolicy.areRequiredTablesReady(true, true, true, true, true, true, true, downloaded));
        for (int i = 1; i <= 5; i++) {
            assertFalse(ProductCardRetryPolicy.needsBatch(false, downloaded));
            assertEquals(missing, attempt(missing, stored));
            assertEquals(Long.valueOf(i), stored.get("133901"));
            assertEquals(Long.valueOf(i), stored.get("157629"));
        }
        for (int i = 0; i < 100; i++) assertTrue(attempt(missing, stored).isEmpty());
    }

    @Test public void emptyResponsesAndNetworkErrorsDoNotResetAttempts() {
        Map<String, Long> stored = new HashMap<>();
        Set<String> missing = ids("133901");
        // Count before dispatch: an empty body, HTTP error or interrupted request uses its slot.
        for (int i = 0; i < 5; i++) assertEquals(missing, attempt(missing, stored));
        assertTrue(attempt(missing, stored).isEmpty());
    }

    @Test public void persistedAttemptsSurviveRestartAndVisitScopeChanges() {
        Map<String, Long> persisted = new HashMap<>();
        for (int i = 0; i < 5; i++) attempt(ids("133901"), persisted);
        Map<String, Long> afterRestart = new HashMap<>(persisted);
        assertTrue(ProductCardRetryPolicy.eligible(ids("133901"), afterRestart).isEmpty());
        assertEquals("229169:133901", ProductCardRetryPolicy.scope(229169, "133901"));
        assertNotEquals(ProductCardRetryPolicy.scope(229169, "133901"), ProductCardRetryPolicy.scope(7, "133901"));
    }

    @Test public void successfulCardsLeaveRetryRequestsAndRemainingOnesKeepTheirBudget() {
        Map<String, Long> stored = new HashMap<>();
        assertEquals(ids("158427", "133901", "157629"), attempt(ids("158427", "133901", "157629"), stored));
        for (int i = 0; i < 4; i++) assertEquals(ids("133901", "157629"), attempt(ids("133901", "157629"), stored));
        assertEquals(Long.valueOf(1), stored.get("158427"));
        assertTrue(attempt(ids("133901", "157629"), stored).isEmpty());
    }

    @Test public void newMissingProductDoesNotRestartExhaustedProducts() {
        Map<String, Long> stored = new HashMap<>();
        stored.put("133901", 5L);
        assertEquals(ids("175194"), attempt(ids("133901", "175194"), stored));
        assertEquals(Long.valueOf(5), stored.get("133901"));
        assertEquals(Long.valueOf(1), stored.get("175194"));
    }

    @Test public void targetedRequestHasOnlyMissingIdsAndNoGlobalWatermarkOrVisitFilter() {
        JsonObject request = ProductCardRetryPolicy.request(ids("157629", "133901", "133901"));
        assertEquals("data_list", request.get("mod").getAsString());
        assertEquals("tovar_list", request.get("act").getAsString());
        assertEquals("[\"133901\",\"157629\"]", request.get("id").toString());
        assertEquals(3, request.entrySet().size());
    }

    @Test public void openingVisitCanRequestExhaustedProductWithoutResettingAutomaticBudget() {
        Map<String, Long> stored = new HashMap<>();
        stored.put("133901", 5L);
        assertTrue(ProductCardRetryPolicy.eligible(ids("133901"), stored).isEmpty());
        assertEquals("[\"133901\"]", ProductCardRetryPolicy.request(ids("133901")).get("id").toString());
        assertTrue(ProductCardRetryPolicy.eligible(ids("133901"), stored).isEmpty());
    }

    @Test public void regularExchangeMayRefreshBatchWithoutRestartingTargetedBudget() {
        assertTrue(ProductCardRetryPolicy.needsBatch(true, true));
        assertEquals(1794398489L, VisitReportDownloadPolicy.requestVpi(true, 1794398489L));
        assertTrue(ProductCardRetryPolicy.eligible(ids("133901"), Collections.singletonMap("133901", 5L)).isEmpty());
    }

    @Test public void emptyAndFailedBatchKeepVpi() {
        assertEquals(123, VisitReportDownloadPolicy.nextVpi(123, 1000, 0, true));
        assertEquals(123, VisitReportDownloadPolicy.nextVpi(123, 1000, 432, false));
        assertEquals(0, VisitReportDownloadPolicy.nextVpi(0, 1000, 0, true));
    }

    @Test public void nonemptyProcessedBatchAdvancesVpiEvenWithUnresolvedReferences() {
        assertEquals(880, VisitReportDownloadPolicy.nextVpi(123, 1000, 432, true));
    }

    @Test public void noAttemptForCompleteProductDirectory() {
        assertTrue(ProductCardRetryPolicy.eligible(Collections.emptySet(), new HashMap<>()).isEmpty());
    }

    @Test(expected = IllegalArgumentException.class) public void noEmptyTargetedRequest() {
        ProductCardRetryPolicy.request(Collections.emptySet());
    }

    @Test public void countersAreBounded() {
        assertEquals(1, ProductCardRetryPolicy.nextAttempt(-1));
        assertEquals(5, ProductCardRetryPolicy.nextAttempt(5));
        assertEquals(5, ProductCardRetryPolicy.nextAttempt(100));
        assertEquals(5, ProductCardRetryPolicy.nextAttempt(Long.MAX_VALUE));
    }
}
