package ua.com.merchik.merchik.Activities

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.GsonBuilder
import org.json.JSONObject
import ua.com.merchik.merchik.Activities.WorkPlanActivity.WPDataActivity
import ua.com.merchik.merchik.R
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale



class FcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d("~~~~FCM~~~~", "newToken=$token")
        getSharedPreferences(packageName, MODE_PRIVATE).edit()
            .putString("fcm_token", token)
            .putBoolean("fcm_token_isSend", false)
            .apply()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(m: RemoteMessage) {
        // 1) Единый JSON-лог
        logRemoteMessageAsJson(m)

        val data = m.data

        // 2) Заголовок/текст (приоритет: data -> notification -> дефолт)
        val title = data["title"] ?: m.notification?.title ?: getString(R.string.app_name)
        val body  = data["body"]  ?: m.notification?.body  ?: ""

        // 3) Канал (type / channel / channel_id из notification)
        val fromServerChannelId = m.notification?.channelId ?: data["channel_id"]
        val typeOrChannel = data["type"] ?: data["channel"] ?: fromServerChannelId
        val channelId = resolveChannelId(typeOrChannel)

        // 4) Замена уведомлений парой (tag,id)
        val tag = data["tag"] ?: m.notification?.tag ?: channelId
        val stableId = (data["id"] ?: channelId).hashCode()

        // 5) Intent при тапе
        val contentIntent = buildContentIntent(data)
        val subText = labelForChannel(channelId)

        // 6) Построение уведомления
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.merchik_small) // лучше монохромный drawable-икон
            .setContentTitle(title)
            .setContentText(body)
            .setSubText(subText)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(groupKeyFor(channelId))

        // Большая картинка из data.image (по желанию)
        data["image"]?.let { url ->
            loadBitmap(url)?.let { bmp ->
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bmp)
                        .setSummaryText(body)
                )
            }
        }

        // 7) Показ
        val nm = NotificationManagerCompat.from(this)
        if (!nm.areNotificationsEnabled()) {
            Log.w("FCM", "Notifications are disabled by user")
        }
        nm.notify(tag, stableId, builder.build())

        // 8) Групповой summary по каналу
        showOrUpdateGroupSummary(nm, channelId)
    }

    // ---------- ЛОГИ ----------

    private fun logRemoteMessageAsJson(m: RemoteMessage) {
        try {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(m.data)
            Log.e("!!!FCM!!!", "Received message: $jsonString")

            val root = JSONObject()
                .put("from", m.from)
                .put("messageId", m.messageId)
                .put("collapseKey", m.collapseKey)
                .put("sentTime", m.sentTime)
                .put("ttl", m.ttl)
                .put("priority", m.priority)
                .put("originalPriority", m.originalPriority)

            // data
            val dataObj = JSONObject()
            for ((k, v) in m.data) dataObj.put(k, v)
            root.put("data", dataObj)

            // notification
            m.notification?.let { n ->
                val notif = JSONObject()
                    .put("title", n.title)
                    .put("body", n.body)
                    .put("titleLocKey", n.titleLocalizationKey)
                    .put("titleLocArgs", n.titleLocalizationArgs?.joinToString())
                    .put("bodyLocKey", n.bodyLocalizationKey)
                    .put("bodyLocArgs", n.bodyLocalizationArgs?.joinToString())
                    .put("icon", n.icon)
                    .put("channelId", n.channelId)
                    .put("color", n.color)
                    .put("sound", n.sound)
                    .put("tag", n.tag)
                    .put("clickAction", n.clickAction)
                    .put("ticker", n.ticker)
                    .put("sticky", n.sticky)
                    .put("localOnly", n.localOnly)
                    .put("notificationPriority", n.notificationPriority)
                    .put("defaultSound", n.defaultSound)
                    .put("defaultVibrateSettings", n.defaultVibrateSettings)
                    .put("defaultLightSettings", n.defaultLightSettings)
                    .put("eventTime", n.eventTime)
                    .put("visibility", n.visibility)
                    .put("notificationCount", n.notificationCount)
                    .put("imageUrl", n.imageUrl?.toString())

                n.vibrateTimings?.let { notif.put("vibrateTimings", it.joinToString()) }
                n.lightSettings?.let { notif.put("lightSettings", it.contentToString()) }

                root.put("notification", notif)
            }

            Log.d("FCM_JSON", root.toString(2))
        } catch (e: Exception) {
            Log.e("FCM_JSON", "Failed to build JSON log", e)
        }
    }

    // ---------- HELPERS ----------

    // Сопоставление server "type"/"channel" -> реальный id канала
    private fun resolveChannelId(typeOrId: String?): String {
        if (typeOrId.isNullOrBlank()) return getString(R.string.default_channel)
        return when (typeOrId.lowercase(Locale.ROOT)) {
            "chat", getString(R.string.channel_chat) -> getString(R.string.channel_chat)
            "task", getString(R.string.channel_task) -> getString(R.string.channel_task)
            "system", getString(R.string.channel_system) -> getString(R.string.channel_system)
            "update", getString(R.string.channel_update) -> getString(R.string.channel_update)
            "reclamation", "reclam", getString(R.string.channel_reclamation) ->
                getString(R.string.channel_reclamation)
            "work_confirm", "confirm", getString(R.string.channel_work_confirm) ->
                getString(R.string.channel_work_confirm)
            "work_update", "workupd", getString(R.string.channel_work_update) ->
                getString(R.string.channel_work_update)
            getString(R.string.default_channel) -> getString(R.string.default_channel)
            else -> getString(R.string.default_channel) // фолбек
        }
    }

    private fun groupKeyFor(channelId: String) = "grp_$channelId"

    // 1) Короткий текстовый лейбл по channelId
    private fun labelForChannel(channelId: String): String = when (channelId) {
        getString(R.string.channel_chat)          -> "Чат"
        getString(R.string.channel_task)          -> "Задача"
        getString(R.string.channel_system)        -> "Система"
        getString(R.string.channel_update)        -> "Оновлення"
        getString(R.string.channel_reclamation)   -> "Рекламація"
        getString(R.string.channel_work_confirm)  -> "Підтвердження"
        getString(R.string.channel_work_update)   -> "Зміни в роботі"
        else                                      -> "Повідомлення"
    }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showOrUpdateGroupSummary(nm: NotificationManagerCompat, channelId: String) {
        val summaryTag = "summary_$channelId"
        val summaryId = summaryTag.hashCode()
        val groupKey = groupKeyFor(channelId)

        val childCount = if (Build.VERSION.SDK_INT >= 23) {
            val sysNm = getSystemService(NotificationManager::class.java)
            sysNm.activeNotifications.count {
                it.notification.group == groupKey &&
                        (it.notification.flags and Notification.FLAG_GROUP_SUMMARY) == 0
            }
        } else 0

        // Если детей нет — удалим summary, чтобы не оставалась пустышка
        if (childCount <= 0) {
            nm.cancel(summaryTag, summaryId)
            return
        }

        val summaryText = resources.getQuantityString(
            R.plurals.new_notifications_count, childCount, childCount
        )

        val subText = labelForChannel(channelId)

        val summary = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.merchik_small) // лучше монохромная иконка из drawable
            .setContentTitle(resolveChannelName(channelId))
            .setContentText(summaryText)
            .setSubText(subText)
            .setStyle(NotificationCompat.InboxStyle())
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setOnlyAlertOnce(true) // не дёргать звук при каждом апдейте summary
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY) // звук только на summary
            .setNumber(childCount) // опционально: счетчик на бейдже
            .build()

        nm.notify(summaryTag, summaryId, summary)
    }


    private fun resolveChannelName(channelId: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.getNotificationChannel(channelId)?.name?.toString()
                ?: getString(R.string.app_name)
        } else {
            getString(R.string.app_name)
        }
    }

    private fun buildContentIntent(data: Map<String, String>): PendingIntent {
        val intent = Intent(this, WPDataActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            for ((k, v) in data) putExtra(k, v)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        return PendingIntent.getActivity(this, 0, intent, flags)
    }

    private fun loadBitmap(url: String): Bitmap? = try {
        (URL(url).openConnection() as HttpURLConnection).run {
            connectTimeout = 5000
            readTimeout = 5000
            inputStream.use { BitmapFactory.decodeStream(it) }
        }
    } catch (_: Exception) { null }
}