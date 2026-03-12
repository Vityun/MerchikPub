package ua.com.merchik.merchik.Utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

private const val INTERNET_CHANNEL_ID = "internet_state_channel"
private const val INTERNET_NOTIFICATION_ID = 1001

fun showInternetStateNotification(context: Context, isConnected: Boolean) {
    val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            INTERNET_CHANNEL_ID,
            "Internet state",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) return
    }

    val title = if (isConnected) "Інтернет увімкнено" else "Інтернет вимкнено"
    val text = if (isConnected) {
        "З'єднання з інтернетом з'явилося"
    } else {
        "З'єднання з інтернетом втрачено"
    }

    val notification = NotificationCompat.Builder(context, INTERNET_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_notify_sync)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context)
        .notify(INTERNET_NOTIFICATION_ID, notification)
}