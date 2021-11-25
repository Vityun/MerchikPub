package ua.com.merchik.merchik.retrofit;

import android.webkit.CookieManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Provides a synchronization point between the webview cookie store and okhttp3.OkHttpClient cookie store
 */
public final class MyCookieJar implements CookieJar {

    private CookieManager webviewCookieManager = CookieManager.getInstance();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
//        System.out.println("COOKIE_STREAM: " + cookies);
        String urlString = url.toString();

        for (Cookie cookie : cookies) {
            webviewCookieManager.setCookie(urlString, cookie.toString());
        }
    }

    private int count = 0;
    @NonNull
    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        System.out.println("TEST.ASYNC.COOKIE.enter: " + count++);
        String urlString = url.toString();
        String cookiesString = webviewCookieManager.getCookie(urlString);
        if (cookiesString != null && !cookiesString.isEmpty()) {
            //We can split on the ';' char as the cookie manager only returns cookies
            //that match the url and haven't expired, so the cookie attributes aren't included
            String[] cookieHeaders = cookiesString.split(";");
            List<Cookie> cookies = new ArrayList<>(cookieHeaders.length);

//            System.out.println("TEST.ASYNC.COOKIE.cookieHeaders: " + cookieHeaders.length);

            for (String header : cookieHeaders) {
                cookies.add(Cookie.parse(url, header));
            }

//            System.out.println("COOKIE2_STREAM: " + cookies);
            return cookies;
        }

        return Collections.emptyList();
    }




}