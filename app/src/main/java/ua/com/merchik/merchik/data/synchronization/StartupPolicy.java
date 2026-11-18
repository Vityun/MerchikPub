package ua.com.merchik.merchik.data.synchronization;

import java.util.EnumSet;
import java.util.Objects;

public final class StartupPolicy {
    public static final String EXTRA_OFFLINE_LOGIN = "offline_login";
    public static final long LOADING_TIMEOUT_MS = 45_000L;
    public static final long RETRY_INTERVAL_MS = 30_000L;

    private StartupPolicy() { }

    public static boolean isOffline(boolean offlineLogin, boolean networkConnected,
                                    boolean serverStatusKnown, boolean serverAvailable) {
        return offlineLogin || !networkConnected || (serverStatusKnown && !serverAvailable);
    }

    public static boolean shouldShowLoading(boolean dataReady, boolean offline) {
        return !dataReady && !offline;
    }

    public static boolean areRequiredTablesReady(boolean wpLoaded, boolean siteLoaded,
                                                  boolean optionsLoaded, boolean themeLoaded,
                                                  boolean siteAvailable, boolean themeAvailable,
                                                  boolean reportPrepareLoaded, boolean productsDownloaded) {
        // Saved flags cannot replace missing dictionaries or locally seeded SiteObjects rows.
        // Product batch completion, not completeness of every RP reference, unlocks the plan.
        return wpLoaded && siteLoaded && optionsLoaded && themeLoaded && siteAvailable && themeAvailable
                && reportPrepareLoaded && productsDownloaded;
    }

    public static boolean isTemporaryHttpFailure(int code) {
        return code == 408 || code == 429 || code >= 500;
    }

    public enum InitialTable { PLAN, OPTIONS, SITE_OBJECTS, THEMES }

    public static EnumSet<InitialTable> missingTables(boolean usesVisits, boolean wpLoaded,
                                                     boolean optionsLoaded, boolean siteAvailable,
                                                     boolean themeAvailable) {
        EnumSet<InitialTable> missing = EnumSet.noneOf(InitialTable.class);
        if (usesVisits && !wpLoaded) missing.add(InitialTable.PLAN);
        if (usesVisits && !optionsLoaded) missing.add(InitialTable.OPTIONS);
        if (!siteAvailable) missing.add(InitialTable.SITE_OBJECTS);
        if (!themeAvailable) missing.add(InitialTable.THEMES);
        return missing;
    }

    /** In-memory scheduling only; never a download watermark or a readiness flag. */
    public static final class RetryGate {
        private Object scope;
        private long lastAttempt = -1;

        public boolean tryStart(Object currentScope, long elapsedMs) {
            if (!Objects.equals(scope, currentScope)) {
                scope = currentScope;
                lastAttempt = -1;
            }
            if (lastAttempt >= 0 && elapsedMs >= lastAttempt
                    && elapsedMs - lastAttempt < RETRY_INTERVAL_MS) return false;
            lastAttempt = elapsedMs;
            return true;
        }
    }
}
