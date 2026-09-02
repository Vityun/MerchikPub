package ua.com.merchik.merchik.ServerExchange;

import android.util.Log;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ua.com.merchik.merchik.Globals;

public final class PhotoUrlUtils {
    private static final String TAG = "PhotoUrlUtils";
    private static final Set<String> LOGGED_INVALID_URLS =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    private PhotoUrlUtils() {
    }

    public static String prepareDownloadUrl(String rawUrl, boolean fullSize, String source, String photoServerId) {
        String url = normalize(rawUrl);
        String invalidReason = invalidReason(url);
        if (invalidReason != null) {
            logInvalidUrl(source, photoServerId, rawUrl, url, invalidReason);
            return null;
        }

        if (fullSize) {
            url = url.replace("thumb_", "");
            invalidReason = invalidReason(url);
            if (invalidReason != null) {
                logInvalidUrl(source, photoServerId, rawUrl, url, invalidReason + "_after_fullsize_normalize");
                return null;
            }
        }

        if (url.startsWith("photos/")) {
            return "/" + url;
        }

        return url;
    }

    public static boolean isSafeDownloadUrl(String rawUrl) {
        return invalidReason(normalize(rawUrl)) == null;
    }

    private static String normalize(String rawUrl) {
        if (rawUrl == null) return "";
        return rawUrl.trim().replace("\\/", "/");
    }

    private static String invalidReason(String url) {
        if (url == null || url.isEmpty()) return "empty";
        if (url.equals("/") || url.equals("//") || url.equals("/thumb_")) return "broken_root_or_thumb";

        String path = extractPath(url);
        if (path.isEmpty() || path.equals("/") || path.equals("//") || path.equals("/thumb_")) {
            return "broken_root_or_thumb";
        }

        if (path.contains("//")) {
            return "double_slash_in_path";
        }

        if (path.startsWith("/photos/") || path.startsWith("photos/")) {
            return null;
        }

        return "not_photo_path";
    }

    private static String extractPath(String url) {
        int schemeIndex = url.indexOf("://");
        String path = url;
        if (schemeIndex >= 0) {
            int pathStart = url.indexOf('/', schemeIndex + 3);
            path = pathStart >= 0 ? url.substring(pathStart) : "";
        }

        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }

        int hashIndex = path.indexOf('#');
        if (hashIndex >= 0) {
            path = path.substring(0, hashIndex);
        }

        return path;
    }

    private static void logInvalidUrl(String source, String photoServerId, String rawUrl, String preparedUrl, String reason) {
        String safeSource = source == null ? "unknown" : source;
        String safePhotoServerId = photoServerId == null ? "" : photoServerId;
        String safeRawUrl = rawUrl == null ? "null" : rawUrl;
        String key = safeSource + "|" + safePhotoServerId + "|" + safeRawUrl + "|" + reason;

        if (!LOGGED_INVALID_URLS.add(key)) return;

        String message = "source=" + safeSource
                + ", photoServerId=" + safePhotoServerId
                + ", reason=" + reason
                + ", rawUrl=" + safeRawUrl
                + ", preparedUrl=" + preparedUrl;

        Log.e(TAG, message);
        Globals.writeToMLOG("WARN", "PhotoUrlUtils/invalidPhotoUrl", message);
    }
}
