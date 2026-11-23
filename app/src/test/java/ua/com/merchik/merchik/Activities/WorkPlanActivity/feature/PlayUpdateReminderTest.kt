package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PlayUpdateReminderTest {
    private val shownAt = 1_795_421_000_000L

    @Test fun sameOrOlderVersionIsNeverOffered() {
        assertFalse(shouldRemindAboutPlayUpdate(100, 100, 0, 0, shownAt))
        assertFalse(shouldRemindAboutPlayUpdate(100, 99, 0, 0, shownAt))
    }

    @Test fun aNewerVersionIsOfferedOnFirstCheck() {
        assertTrue(shouldRemindAboutPlayUpdate(100, 101, 0, 0, shownAt))
    }

    @Test fun repeatedResumesDoNotShowTheSameVersionBeforeTwentyFourHours() {
        val almostADay = shownAt + PLAY_UPDATE_REMINDER_INTERVAL_MS - 1
        assertFalse(shouldRemindAboutPlayUpdate(100, 101, 101, shownAt, shownAt))
        assertFalse(shouldRemindAboutPlayUpdate(100, 101, 101, shownAt, almostADay))
        assertTrue(shouldRemindAboutPlayUpdate(100, 101, 101, shownAt, almostADay + 1))
    }

    @Test fun aDifferentNewVersionIsNotSuppressedByThePreviousReminder() {
        assertTrue(shouldRemindAboutPlayUpdate(100, 102, 101, shownAt, shownAt + 1))
    }

    @Test fun aClockCorrectionDoesNotSuppressTheReminderIndefinitely() {
        assertTrue(shouldRemindAboutPlayUpdate(100, 101, 101, shownAt, shownAt - 1))
    }

    @Test fun onlyTheProductionReleaseChecksForPlayUpdates() {
        assertTrue(isPlayUpdateEnabled("ua.com.merchik.merchik", isDebugBuild = false))
        assertFalse(isPlayUpdateEnabled("ua.com.merchik.merchik", isDebugBuild = true))
        assertFalse(isPlayUpdateEnabled("ua.com.merchik.merchik_test", isDebugBuild = false))
        assertFalse(isPlayUpdateEnabled("ua.com.merchik.merchik_test", isDebugBuild = true))
        assertFalse(isPlayUpdateEnabled("ua.com.merchik.other", isDebugBuild = false))
    }

    @Test fun bothNoticePathsRequireProductionAndUseTheStandardBuilder() {
        val path = "src/main/java/ua/com/merchik/merchik/Activities/WorkPlanActivity/feature/PlayUpdateReminder.kt"
        val source = File("app", path).takeIf { it.isFile } ?: File(path)
        val code = source.readText()
        val guard = "if (!isPlayUpdateEnabled(activity.packageName, BuildConfig.DEBUG)) return"
        val automatic = code.substringAfter("fun PlayUpdateReminder()")
            .substringBefore("fun showPlayUpdateNoticePreview(")
        assertTrue(automatic.indexOf(guard) in 0 until automatic.indexOf("AppUpdateManagerFactory.create("))
        assertTrue(code.contains("notice = showPlayUpdateNotice(activity,"))
        val preview = code.substringAfter("fun showPlayUpdateNoticePreview(")
            .substringBefore("private fun showPlayUpdateNotice(")
        assertTrue(preview.indexOf("$guard null") in 0 until preview.indexOf("return showPlayUpdateNotice("))
        assertTrue(code.contains("return MessageDialogBuilder(activity)"))
        val notice = code.substringAfter("private fun showPlayUpdateNotice(")
            .substringBefore("private fun openPlayListing(")
        assertTrue(notice.contains(".setCancelable(false)"))
        assertTrue(notice.contains(".setShowCloseButton(false)"))
        assertTrue(notice.contains(".setOnConfirmAction("))
        assertTrue(notice.contains(".setOnCancelAction("))
    }
}
