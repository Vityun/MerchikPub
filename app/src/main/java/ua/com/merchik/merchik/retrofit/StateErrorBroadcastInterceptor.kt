package ua.com.merchik.merchik.retrofit

import okhttp3.Interceptor
import okhttp3.Response
import ua.com.merchik.merchik.Globals


class StateErrorBroadcastInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body ?: return response
        val isJson = body.contentType()?.subtype?.contains("json", true) == true
        if (!isJson) return response

        try {
            val peeked = response.peekBody(1_000_000)
            val text = peeked.string()
            val json = org.json.JSONObject(text)
            val state = when (val v = json.opt("state")) {
                is Boolean -> v
                is Number  -> v.toInt() != 0
                is String  -> v.equals("true", true) || v == "1"
                else       -> null
            }
            val error_old_app = when (val v = json.opt("error_old_app")) {
                is Boolean -> v
                is Number  -> v.toInt() != 0
                is String  -> v.equals("true", true) || v == "1"
                else       -> null
            }
            if (state == false && error_old_app == true) {
                json.optString("error")
                    .takeIf { it.isNotBlank() }
                    ?.let(GlobalErrors::emit)
            }
        } catch (e: Exception) {
//            Globals.writeToMLOG("ERROR","StateErrorBroadcastInterceptor.intercept", "Exception: ${e.message}")
        }

        return response
    }
}