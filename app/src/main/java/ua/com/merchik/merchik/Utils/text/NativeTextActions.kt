package ua.com.merchik.merchik.Utils.text

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.Utils.text.NativeTextAction.Kind
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogFullPhoto
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR

/** Shared by text links and regular controls. Resolve records on tap, never while rendering text. */
object NativeTextActions {
    @JvmStatic
    fun perform(context: Context, action: NativeTextAction) {
        val activity = findActivity(context)
        if (activity != null && (activity.isFinishing || activity.isDestroyed)) return
        try {
            when (action.kind) {
                Kind.COPY_UNLOCK_CODE -> copyUnlockCode(context, action.value)
                Kind.VISIT_BY_NUMBER, Kind.VISIT_BY_DAD2 -> openVisit(context, action)
                Kind.PHOTO_BY_SERVER_ID -> openPhoto(context, action.value)
                Kind.TASK_BY_ID, Kind.RECLAMATION_BY_ID,
                Kind.TASK_BY_NUMBER, Kind.RECLAMATION_BY_NUMBER -> openTask(context, action)
                Kind.DIAL -> startActivity(context, Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", action.value, null)))
            }
        } catch (error: Exception) {
            showError(context, action, error)
        }
    }

    private fun copyUnlockCode(context: Context, code: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboard == null) {
            Toast.makeText(context, R.string.chat_unlock_code_copy_error, Toast.LENGTH_SHORT).show()
            return
        }
        clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.chat_unlock_code), code))
        Toast.makeText(context, R.string.chat_unlock_code_copied, Toast.LENGTH_SHORT).show()
    }

    private fun openVisit(context: Context, action: NativeTextAction) {
        val wp = if (action.kind == Kind.VISIT_BY_DAD2) {
            WpDataRealm.getWpDataRowByDad2Id(action.value.toLong())
        } else {
            WpDataRealm.getWpDataRowByDocNumOtchet(action.value)
        }
        if (wp == null) {
            missing(context, R.string.native_text_visit_not_found, action.value)
            return
        }
        startActivity(context, Intent(context, DetailedReportActivity::class.java).apply {
            putExtra("WpDataDB_ID", wp.id)
        })
    }

    private fun openPhoto(context: Context, serverId: String) {
        val activity = requireNotNull(findActivity(context)) { "Photo dialog requires an activity" }
        val photo = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(serverId)
        if (photo == null) {
            missing(context, R.string.native_text_photo_not_found, serverId)
            return
        }
        val dialog = DialogFullPhoto(activity)
        dialog.setPhotos(0, listOf(photo), { _, selected ->
            val fullSize = DialogFullPhotoR(activity)
            fullSize.setPhoto(selected)
            selected.comment?.let(fullSize::setComment)
            fullSize.setClose { fullSize.dismiss() }
            fullSize.show()
        }, {})
        dialog.setClose { dialog.dismiss() }
        // The existing dismiss hook also releases the slideshow timer.
        dialog.setOnDismissListener {}
        dialog.show()
    }

    private fun openTask(context: Context, action: NativeTextAction) {
        val activity = requireNotNull(findActivity(context)) { "Task link requires an activity" }
        val owner = activity as? LifecycleOwner ?: error("Task link requires a lifecycle owner")
        val taskType = if (action.kind == Kind.TASK_BY_ID || action.kind == Kind.TASK_BY_NUMBER) 1 else 0
        val byNumber = action.kind == Kind.TASK_BY_NUMBER || action.kind == Kind.RECLAMATION_BY_NUMBER
        owner.lifecycleScope.launch {
            try {
                val task = withContext(Dispatchers.IO) {
                    val dao = RoomManager.SQL_DB.tarDao()
                    if (byNumber) dao.getByDocumentNumber(action.value, taskType)
                    else dao.getById(action.value.toInt())?.takeIf { it.tp == taskType }
                }
                if (activity.isFinishing || activity.isDestroyed) return@launch
                if (task == null) {
                    missing(context, R.string.native_text_task_not_found, action.value)
                    return@launch
                }
                startActivity(context, Intent(context, TARActivity::class.java).apply {
                    putExtra("TAR_type", taskType)
                    putExtra(TARActivity.EXTRA_OPEN_TAR_ID, task.id.toInt())
                })
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                showError(context, action, error)
            }
        }
    }

    private fun startActivity(context: Context, intent: Intent) {
        if (findActivity(context) == null) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun findActivity(context: Context): Activity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is Activity) return current
            val base = current.baseContext
            if (base === current) break
            current = base
        }
        return null
    }

    private fun missing(context: Context, message: Int, value: String) {
        Toast.makeText(context, context.getString(message, value), Toast.LENGTH_LONG).show()
    }

    private fun showError(context: Context, action: NativeTextAction, error: Exception) {
        // Do not log the full message or the unlock code.
        Globals.writeToMLOG("ERROR", "NativeTextActions", "kind=${action.kind}, exception=$error")
        val message = if (action.kind == Kind.COPY_UNLOCK_CODE) R.string.chat_unlock_code_copy_error
            else R.string.native_text_action_error
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
