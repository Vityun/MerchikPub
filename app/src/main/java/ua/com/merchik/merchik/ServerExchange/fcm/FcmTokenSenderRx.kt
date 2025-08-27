package ua.com.merchik.merchik.ServerExchange.fcm

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData
import ua.com.merchik.merchik.retrofit.RetrofitBuilder


object FcmTokenSenderRx {

    fun sendIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val token = prefs.getString("fcm_token", null)
        val alreadySent = prefs.getBoolean("fcm_token_isSend", false)

        if (token.isNullOrBlank()) {
            Log.w("~~~~FCM~~~~", "sendIfNeeded: token is null/blank")
            return
        }
        if (alreadySent) {
            Log.d("~~~~FCM~~~~", "sendIfNeeded: already sent, skip")
            return
        }

        val data: StandartData<*> = StandartData<Any?>()
        data.mod = "auth"
        data.act = "fcm_token_add"
        data.token = token

        val json = Gson().toJson(data)
        val body = Gson().fromJson(json, JsonObject::class.java)

        RetrofitBuilder.getRetrofitInterface()
            .FCM_TOKEN_UPLOAD_RX(RetrofitBuilder.contentType, body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                val bodyStr = resp.toString()
                if (resp.state)
                    prefs.edit().putBoolean("fcm_token_isSend",true).apply()
                Log.e("~~~~FCM~~~~", "sendFCM success: body=$bodyStr")

            }, { e ->
                Log.e("~~~~FCM~~~~", "sendFCM error: ${e.message}", e)
            })
    }
}
