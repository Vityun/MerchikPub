package ua.com.merchik.merchik.features.main.Main

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.DialogCreateAchievement
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.DBViewModels.AchievementsSDBViewModel
import ua.com.merchik.merchik.features.main.componentsUI.QuestionAnswerDialog
import ua.com.merchik.merchik.features.main.componentsUI.QuestionAnswerDialogUiState

internal const val EXTRA_ONE_TIME_PAYMENT_INCREASE = "one_time_payment_increase"
internal const val ONE_TIME_PAYMENT_ACHIEVEMENT_THEME = 1378

internal enum class OneTimePaymentIncreaseLayer {
    QUESTION_ANSWER, CREATE_ACHIEVEMENT, ACHIEVEMENTS;

    val hasAchievementDialog: Boolean get() = this != ACHIEVEMENTS

    fun dismissQuestionAnswer(): OneTimePaymentIncreaseLayer =
        if (this == QUESTION_ANSWER) CREATE_ACHIEVEMENT else this
}

internal fun openOneTimePaymentIncrease(activity: Activity, dataJson: String?) {
    val codeDad2 = Gson().fromJson(dataJson, Long::class.java)
    require(codeDad2 != null && codeDad2 > 0) { "Не указан визит для повышения оплаты" }
    val intent = Intent(activity, FeaturesActivity::class.java).apply {
        putExtra("viewModel", AchievementsSDBViewModel::class.java.canonicalName)
        putExtra("contextUI", ContextUI.ACHIEVEMENT.name)
        putExtra("modeUI", ModeUI.DEFAULT.name)
        // Both the achievements list and QuestionAnswerDialog accept the visit ID.
        putExtra("dataJson", Gson().toJson(codeDad2))
        putExtra("title", "Досягнення")
        putExtra("subTitle", "Представлені досягнення за обраною адресою, клієнтом та періодом")
        putExtra(EXTRA_ONE_TIME_PAYMENT_INCREASE, true)
        // Replace the theme picker, retaining its caller (the question/answer list).
        addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
    }
    activity.startActivity(intent)
    activity.finish()
}

@Composable
internal fun OneTimePaymentIncreaseDialogs(viewModel: AchievementsSDBViewModel) {
    var layer by rememberSaveable(viewModel.dataJson) {
        mutableStateOf(OneTimePaymentIncreaseLayer.QUESTION_ANSWER)
    }
    var achievementShown by remember(viewModel) { mutableStateOf(false) }
    var openingError by remember { mutableStateOf<String?>(null) }

    if (layer.hasAchievementDialog) {
        DisposableEffect(viewModel, viewModel.context) {
            var dialog: DialogCreateAchievement? = null
            try {
                dialog = viewModel.createAchievementDialog(ONE_TIME_PAYMENT_ACHIEVEMENT_THEME)
                    ?: error("Відвідування не знайдено. Виконайте обмін даними та повторіть спробу.")
                dialog.setOnDismissListener {
                    achievementShown = false
                    layer = OneTimePaymentIncreaseLayer.ACHIEVEMENTS
                    viewModel.updateContent()
                }
                // The upper Compose window must be added after the legacy dialog window.
                dialog.setOnShowListener { achievementShown = true }
                dialog.show()
            } catch (error: Exception) {
                layer = OneTimePaymentIncreaseLayer.ACHIEVEMENTS
                openingError = error.message ?: "Не удалось открыть создание достижения"
                Globals.writeToMLOG("ERROR", "OneTimePaymentIncrease", "dataJson=${viewModel.dataJson}, error=$error")
            }
            onDispose {
                dialog?.setOnShowListener(null)
                dialog?.setOnDismissListener(null)
                dialog?.dismiss()
                achievementShown = false
            }
        }
    }

    if (achievementShown && layer == OneTimePaymentIncreaseLayer.QUESTION_ANSWER) {
        val theme = remember { ThemeRealm.getThemeById("610") }
        QuestionAnswerDialog(
            viewModel = viewModel,
            state = QuestionAnswerDialogUiState(isOneTimePaymentIncrease = true),
            themeTitle = theme?.nm ?: "Повышение оплаты",
            themeComment = theme?.comment.orEmpty(),
            hint = "Введіть значення",
            onDismiss = { layer = layer.dismissQuestionAnswer() },
            onSave = { layer = layer.dismissQuestionAnswer() }
        )
    }

    openingError?.let { message ->
        MessageDialog(
            title = "Досягнення",
            status = DialogStatus.ERROR,
            message = message,
            okButtonName = "ОК",
            onDismiss = { openingError = null },
            onConfirmAction = { openingError = null }
        )
    }
}
