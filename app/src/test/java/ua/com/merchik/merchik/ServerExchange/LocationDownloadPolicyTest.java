package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.merchik.merchik.ServerExchange.TablesExchange.FilteredSyncPolicy;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.LocationDownloadPolicy;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RetrofitResponse.Location.LocationList;

import static org.junit.Assert.*;

public class LocationDownloadPolicyTest {
    private static final long NOW = 1793263380L;

    private LocationList location(long id) {
        LocationList row = new LocationList();
        row.id = id;
        row.dtDevice = NOW;
        row.sourceId = 2;
        row.lat = 50.51f;
        row.lon = 30.77f;
        row.altitude = 120;
        row.speed = 1.5f;
        row.accuracy = 8;
        row.locationIsFake = 1;
        return row;
    }

    private SynchronizationCursorSDB cursor(long count) {
        return FilteredSyncPolicy.savedCursor("location/list", "17:all", 0, NOW, count);
    }

    @Test public void initialDownloadRequiresFullFetch() {
        assertFalse(LocationDownloadPolicy.cacheReady(null, 100));
    }

    @Test public void uploadConfirmationsDoNotResetDownloadCursor() {
        SynchronizationCursorSDB cursor = cursor(10);
        assertTrue(LocationDownloadPolicy.cacheReady(cursor, 10));
        assertTrue(LocationDownloadPolicy.cacheReady(cursor, 12));
        assertEquals(NOW - 120, FilteredSyncPolicy.requestWatermark(cursor, NOW + 600,
                LocationDownloadPolicy.cacheReady(cursor, 12)));
    }

    @Test public void lostServerRowsCauseFullFetch() {
        assertFalse(LocationDownloadPolicy.cacheReady(cursor(10), 9));
        assertFalse(LocationDownloadPolicy.cacheReady(cursor(10), 0));
    }

    @Test public void confirmedEmptyCacheCanUseVpi() {
        assertTrue(LocationDownloadPolicy.cacheReady(cursor(0), 0));
        assertTrue(LocationDownloadPolicy.cacheReady(cursor(0), 1));
    }

    @Test public void emptyDeltaDoesNotCreateRows() {
        assertTrue(LocationDownloadPolicy.missingRows(Collections.emptyList(), Collections.emptySet(), 10, NOW).isEmpty());
    }

    @Test public void knownServerIdsAreNotRewrittenOrAllocatedAgain() {
        LocationList known = new LocationList();
        known.id = 55L;
        Set<Long> ids = new HashSet<>(Collections.singletonList(55L));
        List<LogMPDB> additions = LocationDownloadPolicy.missingRows(Arrays.asList(known, location(56)), ids, 80, NOW);
        assertEquals(1, additions.size());
        assertEquals(81, additions.get(0).id);
        assertEquals(56L, additions.get(0).serverId);
        assertEquals(Collections.singleton(55L), ids);
    }

    @Test public void mappingUsesLocalIdsAndPreservesServerIdsAboveIntegerRange() {
        LogMPDB row = LocationDownloadPolicy.missingRows(Collections.singletonList(location(3000000001L)),
                Collections.emptySet(), 100, NOW + 20).get(0);
        assertEquals(101, row.id);
        assertEquals(3000000001L, row.serverId);
        assertEquals(Integer.valueOf(2), row.provider);
        assertEquals(50.51, row.CoordX, 0.00001);
        assertEquals(30.77, row.CoordY, 0.00001);
        assertEquals(120, row.CoordAltitude, 0);
        assertEquals(1.5, row.CoordSpeed, 0);
        assertEquals(8, row.CoordAccuracy, 0);
        assertTrue(row.mocking);
        assertEquals(NOW * 1000L, row.CoordTime);
        assertEquals(NOW + 20, row.vpi);
        assertEquals(0, row.codeDad2);
        assertEquals(0, row.address);
        assertNull(row.gp);
    }

    @Test public void repeatedBatchIsIdempotent() {
        LocationList row = location(55);
        List<LogMPDB> first = LocationDownloadPolicy.missingRows(Arrays.asList(row, row), Collections.emptySet(), 10, NOW);
        assertEquals(1, first.size());
        assertTrue(LocationDownloadPolicy.missingRows(Collections.singletonList(row),
                Collections.singleton(first.get(0).serverId), first.get(0).id, NOW).isEmpty());
    }

    @Test public void distinctServerEventsAtSameCoordinatesRemainDistinct() {
        List<LogMPDB> rows = LocationDownloadPolicy.missingRows(Arrays.asList(location(55), location(56)),
                Collections.emptySet(), 10, NOW);
        assertEquals(2, rows.size());
        assertEquals(11, rows.get(0).id);
        assertEquals(12, rows.get(1).id);
    }

    @Test public void missingMockFlagRetainsPreviousDefault() {
        LocationList row = location(55);
        row.locationIsFake = null;
        assertFalse(LocationDownloadPolicy.missingRows(Collections.singletonList(row), Collections.emptySet(), 0, NOW).get(0).mocking);
    }

    @Test(expected = IllegalStateException.class) public void missingCoordinateRejectsBatch() {
        LocationList invalid = location(56);
        invalid.lat = null;
        LocationDownloadPolicy.missingRows(Arrays.asList(location(55), invalid), Collections.emptySet(), 0, NOW);
    }

    @Test(expected = IllegalStateException.class) public void missingTimeRejectsBatch() {
        LocationList invalid = location(55);
        invalid.dtDevice = null;
        LocationDownloadPolicy.missingRows(Collections.singletonList(invalid), Collections.emptySet(), 0, NOW);
    }

    @Test(expected = IllegalStateException.class) public void missingServerIdRejectsBatch() {
        LocationList invalid = location(55);
        invalid.id = null;
        LocationDownloadPolicy.missingRows(Collections.singletonList(invalid), Collections.emptySet(), 0, NOW);
    }

    @Test(expected = ArithmeticException.class) public void localIdOverflowDoesNotOverwriteAnExistingRow() {
        LocationDownloadPolicy.missingRows(Collections.singletonList(location(55)), Collections.emptySet(), Integer.MAX_VALUE, NOW);
    }

    @Test(expected = ArithmeticException.class) public void timestampOverflowRejected() {
        LocationList invalid = location(55);
        invalid.dtDevice = Long.MAX_VALUE;
        LocationDownloadPolicy.missingRows(Collections.singletonList(invalid), Collections.emptySet(), 0, NOW);
    }
}
