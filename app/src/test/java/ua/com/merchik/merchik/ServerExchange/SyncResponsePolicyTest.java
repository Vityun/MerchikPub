package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import static org.junit.Assert.*;

public class SyncResponsePolicyTest {
    @Test public void emptySuccessIsNotAnError() {
        assertTrue(SyncResponsePolicy.requireList(true, null, Collections.emptyList()).isEmpty());
    }
    @Test(expected = IllegalStateException.class) public void noDataTextDoesNotAuthorizeFalseState() {
        SyncResponsePolicy.requireList(false, "No data", Collections.emptyList());
    }
    @Test(expected = IllegalStateException.class) public void sqlErrorOverridesTrueState() {
        SyncResponsePolicy.requireList(true, "Query failed", Collections.emptyList());
    }
    @Test public void temporaryHttpFailuresRetry() {
        for (int status : new int[]{408, 429, 500, 502, 503, 504}) {
            assertTrue(SyncResponsePolicy.shouldRetry(new SyncResponsePolicy.HttpFailure(status), 0));
        }
    }
    @Test public void permanentHttpFailuresDoNotRetry() {
        for (int status : new int[]{400, 401, 403, 404, 422}) {
            assertFalse(SyncResponsePolicy.shouldRetry(new SyncResponsePolicy.HttpFailure(status), 0));
        }
    }
    @Test public void networkFailureRetriesRemainBounded() {
        Throwable timeout = new SocketTimeoutException("timeout");
        assertTrue(SyncResponsePolicy.shouldRetry(timeout, 0));
        assertTrue(SyncResponsePolicy.shouldRetry(timeout, 1));
        assertFalse(SyncResponsePolicy.shouldRetry(timeout, 2));
    }
    @Test public void wrappedNetworkFailureCanRetry() {
        assertTrue(SyncResponsePolicy.shouldRetry(new RuntimeException(new IOException("reset")), 0));
    }
    @Test public void invalidResponseOrDatabaseFailureDoesNotRetry() {
        assertFalse(SyncResponsePolicy.shouldRetry(new IllegalStateException("state=false"), 0));
        assertFalse(SyncResponsePolicy.shouldRetry(new IllegalArgumentException("bad row"), 0));
    }
    @Test public void malformedJsonAndCertificateErrorsDoNotRetry() {
        assertFalse(SyncResponsePolicy.shouldRetry(new com.google.gson.stream.MalformedJsonException("bad JSON"), 0));
        assertFalse(SyncResponsePolicy.shouldRetry(new com.google.gson.JsonSyntaxException(new IOException()), 0));
        assertFalse(SyncResponsePolicy.shouldRetry(new javax.net.ssl.SSLHandshakeException("certificate"), 0));
    }
    @Test public void diagnosticsAreBoundedAndSingleLine() {
        String text = SyncResponsePolicy.shortError("bad\nresponse\r\t" + "x".repeat(1000));
        assertEquals(259, text.length());
        assertFalse(text.contains("\n"));
        assertFalse(text.contains("\r"));
        assertTrue(text.endsWith("..."));
    }
    @Test public void traceDistinguishesUnchangedCursorFromEmptySuccess() {
        String failed = SyncExchangeTrace.format("chat/list", 100, null, false, "No data", 0, 20,
                new IllegalStateException("invalid"));
        assertTrue(failed.contains("savedVpi=unchanged"));
        assertTrue(failed.contains("state=false"));
        String saved = SyncExchangeTrace.format("chat/list", 100, 200L, true, null, 0, 20, null);
        assertTrue(saved.contains("rows=0"));
        assertTrue(saved.contains("savedVpi=200"));
        assertFalse(saved.contains("failure="));
    }
}
