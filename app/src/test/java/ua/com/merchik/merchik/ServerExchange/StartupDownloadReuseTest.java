package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;
import static org.junit.Assert.*;

public class StartupDownloadReuseTest {
    private final StartupDownloadReuse<String> reuse = new StartupDownloadReuse<>();

    @Test public void successfulStartupCanOnlyBeReusedOnce() {
        long ticket = reuse.begin("account/session/language/db");
        reuse.succeeded(ticket, 1000);
        assertTrue(reuse.consume("account/session/language/db", 2000));
        assertFalse(reuse.consume("account/session/language/db", 2001));
    }
    @Test public void failedStartupMustBeRequestedAgain() {
        reuse.begin("one");
        assertFalse(reuse.consume("one", 2000));
    }
    @Test public void manualExchangeInvalidatesStartupResult() {
        long ticket = reuse.begin("one");
        reuse.succeeded(ticket, 1000);
        reuse.clear();
        assertFalse(reuse.consume("one", 1001));
    }
    @Test public void manualExchangeAlsoInvalidatesLateCompletion() {
        long ticket = reuse.begin("one");
        reuse.clear();
        reuse.succeeded(ticket, 1000);
        assertFalse(reuse.consume("one", 1001));
    }
    @Test public void joiningPendingStartupDoesNotSkipNextExchange() {
        long ticket = reuse.begin("one");
        assertFalse(reuse.consume("one", 999));
        reuse.succeeded(ticket, 1000);
        assertFalse(reuse.consume("one", 1001));
    }
    @Test public void changedAccountLanguageSessionOrDatabaseCannotReuseResult() {
        long ticket = reuse.begin("one");
        reuse.succeeded(ticket, 1000);
        assertFalse(reuse.consume("two", 1001));
    }
    @Test public void expiredStartupIsNotARegularExchangeCache() {
        long ticket = reuse.begin("one");
        reuse.succeeded(ticket, 1000);
        assertFalse(reuse.consume("one", 301000));
    }
    @Test public void staleCompletionCannotCompleteNewStartup() {
        long old = reuse.begin("one");
        reuse.begin("two");
        reuse.succeeded(old, 1000);
        assertFalse(reuse.consume("two", 1001));
    }
}
