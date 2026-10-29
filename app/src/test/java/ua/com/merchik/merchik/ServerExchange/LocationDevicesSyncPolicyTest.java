package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;
import java.util.Collections;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.LocationDevicesSyncPolicy;
import ua.com.merchik.merchik.data.Database.Room.LocationDevices;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.LocationDevicesResponse;
import static org.junit.Assert.*;

public class LocationDevicesSyncPolicyTest {
    private LocationDevicesResponse response() {
        LocationDevicesResponse result = new LocationDevicesResponse();
        result.state = true;
        result.list = Collections.emptyList();
        return result;
    }
    @Test public void emptySuccessfulDeltaCanCommitItsCursor() {
        assertTrue(LocationDevicesSyncPolicy.rows(response()).isEmpty());
        assertEquals(880, LocationDevicesSyncPolicy.savedWatermark(100, 1000L));
    }
    @Test(expected = IllegalStateException.class) public void falseStateIsNotAnEmptySuccess() {
        LocationDevicesResponse response = response(); response.state = false;
        LocationDevicesSyncPolicy.rows(response);
    }
    @Test(expected = IllegalStateException.class) public void errorOverridesTrueState() {
        LocationDevicesResponse response = response(); response.error = "SQL error";
        LocationDevicesSyncPolicy.rows(response);
    }
    @Test(expected = IllegalStateException.class) public void nullResponseCannotAdvanceCursor() {
        LocationDevicesSyncPolicy.rows(null);
    }
    @Test(expected = IllegalStateException.class) public void invalidRowRejectedBeforeTransaction() {
        LocationDevicesResponse response = response();
        LocationDevices row = new LocationDevices(); row.id = 0L;
        response.list = Collections.singletonList(row);
        LocationDevicesSyncPolicy.rows(response);
    }
    @Test public void validRowsKeepIdentityAndValues() {
        LocationDevicesResponse response = response();
        LocationDevices row = new LocationDevices(); row.id = 1L; row.mac = "test";
        response.list = Collections.singletonList(row);
        assertSame(row, LocationDevicesSyncPolicy.rows(response).get(0));
    }
    @Test public void longDownloadUsesStartNotCompletionForCursor() {
        long start = 1793288868L;
        assertEquals(start - 120, LocationDevicesSyncPolicy.savedWatermark(0, start));
    }
    @Test public void unknownTrustedTimeKeepsPreviousCursor() {
        assertEquals(100, LocationDevicesSyncPolicy.savedWatermark(100, null));
        assertEquals(100, LocationDevicesSyncPolicy.requestWatermark(100, null));
    }
    @Test public void futureWatermarkIsClampedBeforeRequest() {
        assertEquals(880, LocationDevicesSyncPolicy.requestWatermark(9000, 1000L));
        assertEquals(0, LocationDevicesSyncPolicy.requestWatermark(-1, 1000L));
    }
}
