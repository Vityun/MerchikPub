package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import ua.com.merchik.merchik.BuildConfig
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dialogs.features.DialogDismissListener
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder

private const val TAG = "PlayUpdateReminder"
private const val LAST_VERSION = "last_version"
private const val LAST_SHOWN_AT = "last_shown_at"
internal const val PLAY_UPDATE_REMINDER_INTERVAL_MS = 24 * 60 * 60 * 1000L

internal fun isPlayUpdateEnabled(packageName: String, isDebugBuild: Boolean): Boolean =
    packageName == "ua.com.merchik.merchik" && !isDebugBuild

internal fun shouldRemindAboutPlayUpdate(
    installedVersion: Int,
    availableVersion: Int,
    lastVersion: Int,
    lastShownAt: Long,
    now: Long
): Boolean = availableVersion > installedVersion &&
        (availableVersion != lastVersion || lastShownAt <= 0L || now < lastShownAt ||
                now - lastShownAt >= PLAY_UPDATE_REMINDER_INTERVAL_MS)

@Composable
internal fun PlayUpdateReminder() {
    val owner = LocalLifecycleOwner.current
    val activity = owner as? Activity ?: return
    if (!isPlayUpdateEnabled(activity.packageName, BuildConfig.DEBUG)) return
    val lifecycle = owner.lifecycle
    val manager = remember(activity) { AppUpdateManagerFactory.create(activity.applicationContext) }
    val preferences = remember(activity) {
        activity.getSharedPreferences("play_update_reminder", Context.MODE_PRIVATE)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) Log.d(TAG, "Update flow ended: ${result.resultCode}")
    }
    val immediate = remember { AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build() }
    fun startImmediateUpdate(info: AppUpdateInfo): Boolean = runCatching {
        manager.startUpdateFlowForResult(info, launcher, immediate)
    }.onFailure { Log.d(TAG, "Could not start Play update", it) }.getOrDefault(false)

    DisposableEffect(lifecycle, manager, launcher) {
        var attached = true
        var checking = false
        var notice: MessageDialogBuilder? = null
        val observer = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME || checking || notice != null) return@LifecycleEventObserver
            checking = true
            manager.appUpdateInfo.addOnSuccessListener { info ->
                checking = false
                if (!attached || activity.isFinishing || activity.isDestroyed ||
                    !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return@addOnSuccessListener
                when (info.updateAvailability()) {
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> startImmediateUpdate(info)
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        val version = info.availableVersionCode()
                        val now = System.currentTimeMillis()
                        if (shouldRemindAboutPlayUpdate(BuildConfig.VERSION_CODE, version,
                                preferences.getInt(LAST_VERSION, 0), preferences.getLong(LAST_SHOWN_AT, 0L), now)) {
                            preferences.edit().putInt(LAST_VERSION, version).putLong(LAST_SHOWN_AT, now).apply()
                            notice = showPlayUpdateNotice(activity, onDismiss = { notice = null }, onUpdate = {
                                if (!info.isUpdateTypeAllowed(immediate) || !startImmediateUpdate(info)) {
                                    openPlayListing(activity)
                                }
                            })
                        }
                    }
                    else -> Unit
                }
            }.addOnFailureListener { failure ->
                checking = false
                Log.d(TAG, "Play update check unavailable", failure)
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            attached = false
            lifecycle.removeObserver(observer)
            notice?.dismiss()
        }
    }
}

fun showPlayUpdateNoticePreview(activity: Activity): MessageDialogBuilder? {
    if (!isPlayUpdateEnabled(activity.packageName, BuildConfig.DEBUG)) return null
    if (activity.isFinishing || activity.isDestroyed) return null
    return showPlayUpdateNotice(activity, onDismiss = {}, onUpdate = {})
}

private fun showPlayUpdateNotice(
    activity: Activity,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
): MessageDialogBuilder {
    val appName = "Merchik"
    return MessageDialogBuilder(activity)
        .setTitle(activity.getString(R.string.play_update_title, appName))
        .setMessage(activity.getString(R.string.play_update_message, appName))
        .setCancelable(false)
        .setShowCloseButton(false)
        .setOnConfirmAction(activity.getString(R.string.play_update_action), onUpdate)
        .setOnCancelAction(activity.getString(R.string.play_update_later)) { }
        .setOnDismissListener(DialogDismissListener { onDismiss() })
        .also { it.show() }
}

private fun openPlayListing(context: Context) {
    val packageId = context.packageName
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageId"))
            .setPackage("com.android.vending"))
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageId")))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.play_update_store_error, Toast.LENGTH_SHORT).show()
        }
    }
}
