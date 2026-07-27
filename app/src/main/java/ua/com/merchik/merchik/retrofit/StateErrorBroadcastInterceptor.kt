package ua.com.merchik.merchik.retrofit

import okhttp3.Interceptor
import okhttp3.Response


class StateErrorBroadcastInterceptor : Interceptor {
    private companion object {
        const val MAX_ERROR_BODY_BYTES = 64L * 1024L

        val STATE_FALSE = Regex(
            """"state"\s*:\s*(false|0|"false"|"0")""",
            RegexOption.IGNORE_CASE
        )
        val ERROR_OLD_APP_TRUE = Regex(
            """"error_old_app"\s*:\s*(true|1|"true"|"1")""",
            RegexOption.IGNORE_CASE
        )
        val ERROR_TEXT = Regex(
            """"error"\s*:\s*"((?:\\.|[^"\\])*)"""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body ?: return response
        val isJson = body.contentType()?.subtype?.contains("json", true) == true
        if (!isJson) return response

        try {
            val contentLength = body.contentLength()
            if (contentLength > MAX_ERROR_BODY_BYTES) return response

            val peeked = response.peekBody(MAX_ERROR_BODY_BYTES)
            val text = peeked.string()
            if (!text.trimStart().startsWith("{")) return response
            if (!STATE_FALSE.containsMatchIn(text)) return response
            if (!ERROR_OLD_APP_TRUE.containsMatchIn(text)) return response

            extractError(text)
                ?.takeIf { it.isNotBlank() }
                ?.let(GlobalErrors::emit)
        } catch (e: Exception) {
//            Globals.writeToMLOG("ERROR","StateErrorBroadcastInterceptor.intercept", "Exception: ${e.message}")
        }

        return response
    }

    private fun extractError(text: String): String? {
        val escaped = ERROR_TEXT.find(text)?.groupValues?.getOrNull(1) ?: return null
        return unescapeJsonString(escaped)
    }

    private fun unescapeJsonString(value: String): String {
        val result = StringBuilder(value.length)
        var i = 0

        while (i < value.length) {
            val char = value[i]
            if (char != '\\' || i == value.lastIndex) {
                result.append(char)
                i++
                continue
            }

            val next = value[++i]
            when (next) {
                '"', '\\', '/' -> result.append(next)
                'b' -> result.append('\b')
                'f' -> result.append('\u000C')
                'n' -> result.append('\n')
                'r' -> result.append('\r')
                't' -> result.append('\t')
                'u' -> {
                    if (i + 4 < value.length) {
                        val hex = value.substring(i + 1, i + 5)
                        val code = hex.toIntOrNull(16)
                        if (code != null) {
                            result.append(code.toChar())
                            i += 4
                        } else {
                            result.append("\\u").append(hex)
                            i += 4
                        }
                    } else {
                        result.append("\\u")
                    }
                }
                else -> result.append(next)
            }
            i++
        }

        return result.toString()
    }
}
