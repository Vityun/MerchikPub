package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.*;

public class VisitReportBackgroundRefreshWiringTest {
    private static final String ACTIVITY = "Activities/DetailedReportActivity/DetailedReportActivity.java";

    @Test public void freshnessCannotPutCachedVisitBehindPreparationScreen() throws IOException {
        String activity = source(ACTIVITY);
        String opening = between(activity, "protected void onCreate(", "private void initializeReport()");
        assertFalse(opening.contains("VisitReportRefresh"));
        assertTrue(opening.contains("!RnoVisitDataExchange.isReady(wpDataDB)"));
        assertTrue(opening.contains("VisitReportDataSync.hasMissingProducts(wpDataDB.getCode_dad2())"));
        assertTrue(opening.contains("initializeReport();"));

        String preparation = between(activity, "private void showVisitPreparation(", "private void prepareVisitData(");
        assertFalse(preparation.contains("VisitReportRefresh"));
        assertTrue(preparation.contains("prepareVisitData(refresh, progress, message, retry);"));
    }

    @Test public void requestIsPostedAfterReportInitialization() throws IOException {
        String initialization = between(source(ACTIVITY), "private void initializeReport()", "private void refreshVisitInBackground()");
        assertTrue(initialization.indexOf("refreshVisitInBackground();") > initialization.indexOf("reportInitialized = true;"));
        String scheduling = between(source("ServerExchange/VisitReportRefresh.java"),
                "public static Subscription refreshForVisit(", "private static void start(");
        assertTrue(scheduling.contains("MAIN.post(() -> start(dad2, subscription));"));
        assertTrue(scheduling.contains("return subscription;"));
    }

    @Test public void backgroundCallbackDoesNotRecreateOrBlockScreen() throws IOException {
        String activity = source(ACTIVITY);
        String background = between(activity, "private void refreshVisitInBackground()", "private void showVisitPreparation(");
        assertFalse(background.contains("setContentView("));
        assertFalse(background.contains("initializeReport("));
        assertFalse(background.contains("setActivityData("));
        assertFalse(background.contains("Dialog"));
        assertFalse(background.contains("showVisitPreparation("));
        assertTrue(background.contains("VisitReportDataSync.repairForVisit(dad2, updated)"));
        assertTrue(background.contains("detailedReportViewModel.reportDataUpdated(missingProducts)"));
        assertTrue(background.contains("!isFinishing() && !isDestroyed()"));
        assertTrue(background.contains("visitUserId == Globals.getCurrentUserId()"));
        assertTrue(background.contains("Objects.equals(session, Globals.getCurrentSessionId())"));
        assertTrue(between(activity, "protected void onDestroy()", "protected void onResume()").contains("visitRefresh.cancel()"));
    }

    @Test public void visibleProductsObserveRefreshWithoutReplacingVisitState() throws IOException {
        String fragment = source("Activities/DetailedReportActivity/DetailedReportTovarsFrag.java");
        assertTrue(fragment.contains("getReportDataUpdated().observe(getViewLifecycleOwner()"));
        assertTrue(fragment.contains("products.refreshReportPrepareOnTabResume()"));
        assertTrue(fragment.contains("products.refreshAfterVisitDataLoaded()"));
    }

    @Test public void lateResponseStillChecksVisitBeforeMerge() throws IOException {
        String refresh = source("ServerExchange/VisitReportRefresh.java");
        assertTrue(refresh.contains("if (!notStarted(visit)) throw new IllegalStateException"));
        assertTrue(refresh.contains("this::requireCurrent, merged -> applied = merged"));
        assertTrue(refresh.contains("listener.complete(error == null && applied)"));
        assertTrue(refresh.contains("if (applied && count > 0)"));
        String download = source("ServerExchange/ReportPrepareSync.java");
        String response = between(download.substring(download.indexOf("JsonObject snapshot = request.deepCopy();")),
                "@Override public void onResponse(", "@Override public void onFailure(");
        assertTrue(response.indexOf("requireCurrent.run();") < response.indexOf("ReportPrepareRealm.reconcileUnstartedVisit"));
    }

    private static String between(String source, String start, String end) {
        int first = source.indexOf(start);
        assertTrue("Missing start: " + start, first >= 0);
        int last = source.indexOf(end, first + start.length());
        assertTrue("Missing end: " + end, last > first);
        return source.substring(first, last);
    }

    private static String source(String file) throws IOException {
        Path root = Path.of("app/src/main/java/ua/com/merchik/merchik");
        if (!Files.isDirectory(root)) root = Path.of("src/main/java/ua/com/merchik/merchik");
        return new String(Files.readAllBytes(root.resolve(file)), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
