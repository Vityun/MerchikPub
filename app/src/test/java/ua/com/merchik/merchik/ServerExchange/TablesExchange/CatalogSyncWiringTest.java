package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import org.junit.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.*;

public class CatalogSyncWiringTest {
    private static final String EXCHANGE = "ServerExchange/TablesExchange/";
    private static final String DAO = "database/room/DaoInterfaces/";

    @Test public void allLegacyDownloadEntryPointsUseCatalogProtocol() throws IOException {
        String planograms = source(EXCHANGE + "PlanogrammTableExchange.java");
        for (String entry : new String[]{"planograms(click)", "addresses(click)", "groups(click)", "images(click)",
                "types(null)", "types(click)", "visits(null)"}) {
            assertTrue(entry, planograms.contains("CatalogDataExchange." + entry));
        }
        assertFalse(planograms.contains("dt_change_from"));
        assertFalse(planograms.contains("setVpi"));
        assertTrue(source(EXCHANGE + "ShowcaseExchange.java").contains("CatalogDataExchange.showcases("));
        String references = source(EXCHANGE + "ReferenceDataExchange.java");
        for (String table : new String[]{"addresses", "groups", "images"}) {
            assertTrue(references.contains("CatalogDataExchange." + table + "(callback)"));
        }
        assertFalse(source(EXCHANGE + "ReferenceSyncPolicy.java").contains("PLANOGRAM_"));
    }

    @Test public void relatedTablesDeriveParentExclusionsFromTheirOwnDownloadedRows() throws IOException {
        String catalog = source(EXCHANGE + "CatalogDataExchange.java");
        for (String dao : new String[]{"planogrammDao", "planogrammAddressDao", "planogrammGroupDao",
                "planogrammImagesDao", "planogrammVizitShowcaseDao", "showcaseDao"}) {
            assertTrue(catalog.contains("return db." + dao + "().getAll();"));
        }
        assertTrue(catalog.contains("return db.planogrammTypeDao().getAllPlanogramsm();"));
        for (String table : new String[]{"ADDRESSES", "GROUPS", "IMAGES"}) {
            assertTrue(catalog.contains("(Table." + table + ", row -> row.id, row -> row.planogramId)"));
        }
        assertTrue(catalog.contains("(Table.TYPES, PlanogrammTypeSDB::getID, PlanogrammTypeSDB::getPlanogram_id)"));
        assertTrue(catalog.contains("CatalogSyncPolicy.exclusionIds(local, exclusionId, this::downloaded)"));
        assertTrue(source(DAO + "PlanogrammAddressDao.java").contains(
                "@Query(\"SELECT * FROM planogramm_address\")\n    List<PlanogrammAddressSDB> getAll();"));
    }

    @Test public void existingPlanogramIdsAreReplacedIncludingTovarGrp() throws IOException {
        String catalog = source(EXCHANGE + "CatalogDataExchange.java");
        String method = between(catalog, "public static void planograms(", "public static void addresses(");
        assertTrue(method.contains("return result;"));
        assertTrue(method.contains("db.planogrammDao().upsertAll(rows)"));
        assertFalse(method.contains("contains("));
        assertFalse(method.contains("removeIf("));
        assertFalse(method.contains("tovarGrp ="));
        assertTrue(source(DAO + "PlanogrammDao.java").contains(
                "@Insert(onConflict = OnConflictStrategy.REPLACE)\n    void upsertAll("));
    }

    @Test public void receiptIsSavedAtomicallyOnlyAfterNonemptyData() throws IOException {
        String download = between(source(EXCHANGE + "CatalogDataExchange.java"),
                "private static <T> List<T> download(", "private static <B> B body(");
        assertTrue(download.contains("get(context.table.receiptResource(), context.scope)"));
        assertTrue(download.contains("int knownRows = dataset.downloadedCount(dataset.beforeRequest);"));
        assertTrue(download.contains("receipt, knownRows)"));
        assertTrue(download.contains("rows.size(), dataset.downloadedCount(dataset.local(db))"));
        assertTrue(download.indexOf("if (rows.isEmpty())") < download.indexOf("db.runInTransaction("));
        assertTrue(between(download, "if (rows.isEmpty())", "db.runInTransaction(").contains("return rows;"));
        String transaction = between(download, "db.runInTransaction(", "log(context, \"saved:");
        assertTrue(transaction.indexOf("dataset.save(db, rows);") < transaction.indexOf("CatalogSyncPolicy.fullReceipt("));
        assertTrue(transaction.contains("if (saved != null) db.synchronizationCursorDao().save(saved);"));
        assertTrue(transaction.indexOf("context.requireCurrent();") < transaction.indexOf("dataset.save(db, rows);"));
        assertTrue(transaction.lastIndexOf("context.requireCurrent();") > transaction.indexOf(".save(saved)"));
        assertFalse(download.contains("setVpi("));
    }

    @Test public void failedResponsesCannotReachPersistence() throws IOException {
        String catalog = source(EXCHANGE + "CatalogDataExchange.java");
        assertTrue(catalog.contains("!response.isSuccessful() || response.body() == null"));
        assertTrue(catalog.contains("return rows(body.state, body.error, body.list, id)"));
        String context = catalog.substring(catalog.indexOf("private static final class RequestContext"));
        assertTrue(context.contains("db == RoomManager.SQL_DB"));
        assertTrue(context.contains("user == Globals.getCurrentUserId()"));
        assertTrue(context.contains("Objects.equals(session, Globals.getCurrentSessionId())"));
        assertTrue(catalog.contains("if (callback == null || !context.isCurrent()) return;"));
        assertTrue(catalog.contains("request = (Single<List<T>>) IN_FLIGHT.get(context);"));
    }

    @Test public void pendingVisitEditsAreRereadInsideSaveAndNotExcluded() throws IOException {
        String visits = between(source(EXCHANGE + "CatalogDataExchange.java"),
                "public static void visits(", "private static <T> void start(");
        assertTrue(visits.contains("return !Integer.valueOf(1).equals(row.uploadStatus);"));
        assertTrue(visits.contains("upsertAll(CatalogSyncPolicy.visitRowsToSave(rows, beforeRequest, local(db)))"));
        String download = between(source(EXCHANGE + "CatalogDataExchange.java"),
                "private static <T> List<T> download(", "private static <B> B body(");
        assertTrue(download.indexOf("dataset.beforeRequest = dataset.local(db);") < download.indexOf("dataset.load(request)"));
        assertFalse(visits.contains("delete"));
    }

    @Test public void showcaseDataIsCommittedOnceBeforePhotoCallback() throws IOException {
        String exchange = source("ServerExchange/Exchange.java");
        assertFalse(exchange.contains("showcaseDao().insertAll("));
        assertTrue(exchange.contains("showcaseExchange.downloadShowcasePhoto((List<ShowcaseSDB>) data)"));
        String catalog = source(EXCHANGE + "CatalogDataExchange.java");
        assertTrue(catalog.contains("void save(AppDatabase db, List<ShowcaseSDB> rows) { db.showcaseDao().upsertAll(rows); }"));
    }

    private static String between(String source, String first, String last) {
        int start = source.indexOf(first);
        assertTrue("Missing " + first, start >= 0);
        int end = source.indexOf(last, start + first.length());
        assertTrue("Missing " + last, end > start);
        return source.substring(start, end);
    }

    private static String source(String path) throws IOException {
        Path root = Path.of("app/src/main/java/ua/com/merchik/merchik");
        if (!Files.isDirectory(root)) root = Path.of("src/main/java/ua/com/merchik/merchik");
        return Files.readString(root.resolve(path), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
