package ua.com.merchik.merchik.data.synchronization;

public final class StartupPolicy {
    public static final String EXTRA_OFFLINE_LOGIN = "offline_login";
    public static final long LOADING_TIMEOUT_MS = 45_000L;

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
                                                  boolean siteAvailable, boolean themeAvailable) {
        // Saved flags cannot replace missing dictionaries or locally seeded SiteObjects rows.
        return wpLoaded && siteLoaded && optionsLoaded && themeLoaded && siteAvailable && themeAvailable;
    }

    public static boolean isTemporaryHttpFailure(int code) {
        return code == 408 || code == 429 || code >= 500;
    }
}
