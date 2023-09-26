package ua.com.merchik.merchik.retrofit

import okhttp3.Interceptor
import java.util.concurrent.TimeUnit
import okhttp3.Response
import retrofit2.Invocation

class TimeoutInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val annotation = chain.getTimeoutAnnotation() ?: return chain.proceed(chain.request())

        val timeoutSec = timeoutSeconds(annotation.key) ?: return chain.proceed(chain.request())

        return chain
            .withConnectTimeout(timeoutSec, TimeUnit.SECONDS)
            .withWriteTimeout(timeoutSec, TimeUnit.SECONDS)
            .withReadTimeout(timeoutSec, TimeUnit.SECONDS)
            .proceed(chain.request())
    }

    private fun Interceptor.Chain.getTimeoutAnnotation() =
        request().tag(Invocation::class.java)?.method()?.getAnnotation(Timeout::class.java)

    private fun timeoutSeconds(key: String): Int? =
        when (key) {
            UPLOAD_PHOTO_KEY -> 120
            else -> null
        }
}