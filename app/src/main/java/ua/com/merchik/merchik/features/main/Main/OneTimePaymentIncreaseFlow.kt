package ua.com.merchik.merchik.features.main.Main

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.Options.OvertimeOfferManager
import ua.com.merchik.merchik.Utils.WorkTimeRules
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.DialogCreateAchievement
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.DBViewModels.AchievementsSDBViewModel
import ua.com.merchik.merchik.features.main.componentsUI.QuestionAnswerDialog
import ua.com.merchik.merchik.features.main.componentsUI.QuestionAnswerDialogUiState
import ua.com.merchik.merchik.features.main.componentsUI.QuestionAnswerPaymentSnapshot
import ua.com.merchik.merchik.features.main.componentsUI.calculateQuestionAnswerPaymentSnapshot

internal const val EXTRA_ONE_TIME_PAYMENT_INCREASE = "one_time_payment_increase"
internal const val ONE_TIME_PAYMENT_ACHIEVEMENT_THEME = 1378

internal enum class OneTimePaymentIncreaseLayer {
    QUESTION_ANSWER, CREATE_ACHIEVEMENT, ACHIEVEMENTS;

    val hasAchievementDialog: Boolean get() = this != ACHIEVEMENTS

    fun dismissQuestionAnswer(): OneTimePaymentIncreaseLayer =
        if (this == QUESTION_ANSWER) CREATE_ACHIEVEMENT else this
}

internal fun canRequestOneTimePaymentIncrease(
    visitStart: Long,
    clientStart: Long,
    visitEnd: Long,
    clientEnd: Long
): Boolean = runCatching {
    WorkTimeRules.normalize(visitStart, clientStart, visitEnd, clientEnd).end > 0L
}.getOrDefault(false)

private fun loadOneTimePaymentIncreaseVisit(dataJson: String?): WpDataDB {
    val codeDad2 = Gson().fromJson(dataJson, Long::class.java)
    require(codeDad2 != null && codeDad2 > 0) { "Не указан визит для повышения оплаты" }
    return requireNotNull(RealmManager.getWorkPlanRowByCodeDad2(codeDad2)) {
        "Відвідування не знайдено. Виконайте обмін даними та повторіть спробу."
    }
}

private fun WpDataDB.canRequestOneTimePaymentIncrease(): Boolean =
    canRequestOneTimePaymentIncrease(visit_start_dt, client_start_dt, visit_end_dt, client_end_dt)

private fun prepareOneTimePaymentIncrease(
    dataJson: String?,
    activity: Activity? = null
): QuestionAnswerPaymentSnapshot? {
    val visit = loadOneTimePaymentIncreaseVisit(dataJson)
    if (!visit.canRequestOneTimePaymentIncrease()) return null
    val snapshot = calculateQuestionAnswerPaymentSnapshot(visit, isOneTimePaymentIncrease = true)
    val offerJson = activity?.intent?.getStringExtra(OvertimeOfferManager.EXTRA_OFFER)
        ?: return snapshot
    val offer = Gson().fromJson(offerJson, OvertimeOfferManager.Offer::class.java)
    require(OvertimeOfferManager.matches(offer, visit) && offer.baselineSeconds?.signum() == 1) {
        "Дані відвідування змінилися. Відкрийте заявку повторно з відвідування."
    }
    return snapshot.copy(
        calculatedData = snapshot.calculatedData.copy(
            avgWorkDurationFact = offer.baselineText(),
            avgWorkDurationMinutes = offer.baselineMinutes()
        ),
        isPlannedDuration = offer.isPlanned,
        isServerDuration = !offer.isPlanned
    )
}

internal fun openOneTimePaymentIncrease(activity: Activity, dataJson: String?): Boolean {
    val visit = loadOneTimePaymentIncreaseVisit(dataJson)
    if (!visit.canRequestOneTimePaymentIncrease()) return false

    val intent = Intent(activity, FeaturesActivity::class.java).apply {
        putExtra("viewModel", AchievementsSDBViewModel::class.java.canonicalName)
        putExtra("contextUI", ContextUI.ACHIEVEMENT.name)
        putExtra("modeUI", ModeUI.DEFAULT.name)
        // Both the achievements list and QuestionAnswerDialog accept the visit ID.
        putExtra("dataJson", Gson().toJson(visit.code_dad2))
        putExtra("title", "Досягнення")
        putExtra("subTitle", "Представлені досягнення за обраною адресою, клієнтом та періодом")
        putExtra(EXTRA_ONE_TIME_PAYMENT_INCREASE, true)
        // Replace the theme picker, retaining its caller (the question/answer list).
        addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
    }
    activity.startActivity(intent)
    activity.finish()
    return true
}

@Composable
internal fun UnfinishedOneTimePaymentIncreaseDialog(onDismiss: () -> Unit) {
    MessageDialog(
        title = "Повышение оплаты",
        subTitle = RoomManager.SQL_DB.themeDao().getById("1378").nm ?: "",
        status = DialogStatus.ALERT,
        message = "Нельзя просить РАЗОВОЕ повышение оплаты, если работа по этому визиту ещё не завершена. " +
                "Сначала завершите работу.",
        okButtonName = "ОК",
        onDismiss = onDismiss,
        onConfirmAction = onDismiss
    )
}

@Composable
internal fun OneTimePaymentIncreaseEntry(
    viewModel: MainViewModel,
    themeTitle: String,
    themeComment: String,
    onDismiss: () -> Unit
) {
    val preparation = remember(viewModel.dataJson) {
        runCatching { prepareOneTimePaymentIncrease(viewModel.dataJson) }
    }
    val snapshot = preparation.getOrNull()
    var openingError by remember { mutableStateOf<String?>(null) }
    var showUnfinishedVisitMessage by rememberSaveable(viewModel.dataJson) { mutableStateOf(false) }

    when {
        preparation.isFailure || openingError != null -> MessageDialog(
            title = "Повышение оплаты",
            status = DialogStatus.ERROR,
            message = openingError ?: preparation.exceptionOrNull()?.message
                ?: "Не удалось открыть форму повышения оплаты.",
            okButtonName = "ОК",
            onDismiss = onDismiss,
            onConfirmAction = onDismiss
        )
        snapshot == null || showUnfinishedVisitMessage -> UnfinishedOneTimePaymentIncreaseDialog(onDismiss)
        snapshot.isDurationBelowAverage -> QuestionAnswerDialog(
            viewModel = viewModel,
            state = QuestionAnswerDialogUiState(isOneTimePaymentIncrease = true),
            themeTitle = themeTitle,
            themeComment = themeComment,
            hint = "Введіть значення",
            onDismiss = onDismiss,
            onSave = { _: String -> },
            preparedSnapshot = snapshot
        )
        else -> LaunchedEffect(viewModel) {
            try {
                val activity = viewModel.context as? Activity ?: error("Не найден экран выбора темы")
                if (openOneTimePaymentIncrease(activity, viewModel.dataJson)) {
                    onDismiss()
                } else {
                    showUnfinishedVisitMessage = true
                }
            } catch (error: Exception) {
                openingError = error.message ?: "Не удалось открыть достижения. Повторите попытку."
                Globals.writeToMLOG("ERROR", "OneTimePaymentIncrease/open", "error=$error")
            }
        }
    }
}

@Composable
internal fun OneTimePaymentIncreaseDialogs(viewModel: AchievementsSDBViewModel) {
    var layer by rememberSaveable(viewModel.dataJson) {
        mutableStateOf(OneTimePaymentIncreaseLayer.QUESTION_ANSWER)
    }
    var achievementShown by remember(viewModel) { mutableStateOf(false) }
    var achievementDialog by remember(viewModel) { mutableStateOf<DialogCreateAchievement?>(null) }
    var openingError by remember { mutableStateOf<String?>(null) }
    var showUnfinishedVisitMessage by rememberSaveable(viewModel.dataJson) { mutableStateOf(false) }
    val preparation = remember(viewModel.dataJson) {
        runCatching {
            prepareOneTimePaymentIncrease(viewModel.dataJson, viewModel.context as? Activity)
        }
    }
    val preparedSnapshot = preparation.getOrNull()
    var rejectedFormClosed by rememberSaveable(viewModel.dataJson) { mutableStateOf(false) }

    if (preparation.isFailure) {
        MessageDialog(
            title = "Перепрацювання",
            status = DialogStatus.ALERT,
            message = preparation.exceptionOrNull()?.message ?: "Не вдалося підготувати заявку.",
            okButtonName = "ОК",
            onDismiss = { (viewModel.context as? Activity)?.finish() },
            onConfirmAction = { (viewModel.context as? Activity)?.finish() }
        )
        return
    }

    // A restored screen can receive updated work times; do not recreate an achievement in that case.
    if (preparedSnapshot?.isDurationBelowAverage == true) {
        if (!rejectedFormClosed) {
            val theme = remember { ThemeRealm.getThemeById("610") }
            QuestionAnswerDialog(
                viewModel = viewModel,
                state = QuestionAnswerDialogUiState(isOneTimePaymentIncrease = true),
                themeTitle = theme?.nm ?: "Повышение оплаты",
                themeComment = theme?.comment.orEmpty(),
                hint = "Введіть значення",
                onDismiss = {
                    rejectedFormClosed = true
                    (viewModel.context as? Activity)?.finish()
                },
                onSave = { _: String -> },
                preparedSnapshot = preparedSnapshot
            )
        }
        return
    }

    if (layer.hasAchievementDialog) {
        DisposableEffect(viewModel, viewModel.context) {
            var dialog: DialogCreateAchievement? = null
            try {
                // Recheck on activity restoration before creating either of the input dialogs.
                val visit = loadOneTimePaymentIncreaseVisit(viewModel.dataJson)
                if (!visit.canRequestOneTimePaymentIncrease()) {
                    showUnfinishedVisitMessage = true
                    layer = OneTimePaymentIncreaseLayer.ACHIEVEMENTS
                } else {
                    dialog = viewModel.createAchievementDialog(ONE_TIME_PAYMENT_ACHIEVEMENT_THEME)
                        ?: error("Відвідування не знайдено. Виконайте обмін даними та повторіть спробу.")
                    dialog.setRequirementClient(
                        1,
                        "Це досягнення не відноситься до жодної з пропозицій замовника"
                    )
                    achievementDialog = dialog
                    dialog.setOnDismissListener {
                        achievementShown = false
                        achievementDialog = null
                        layer = OneTimePaymentIncreaseLayer.ACHIEVEMENTS
                        viewModel.updateContent()
                    }
                    // The upper Compose window must be added after the legacy dialog window.
                    dialog.setOnShowListener { achievementShown = true }
                    dialog.show()
                }
            } catch (error: Exception) {
                layer = OneTimePaymentIncreaseLayer.ACHIEVEMENTS
                openingError = error.message ?: "Не удалось открыть создание достижения"
                Globals.writeToMLOG("ERROR", "OneTimePaymentIncrease", "dataJson=${viewModel.dataJson}, error=$error")
            }
            onDispose {
                dialog?.setOnShowListener(null)
                dialog?.setOnDismissListener(null)
                dialog?.dismiss()
                achievementDialog = null
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
            onSave = { statement: String ->
                val dialog = achievementDialog
                if (dialog == null) {
                    openingError = "Діалог створення досягнення вже закрито. Текст не перенесено."
                } else {
                    runCatching {
                        dialog.setComment(statement)
                    }.onSuccess {
                        layer = layer.dismissQuestionAnswer()
                    }.onFailure {
                        openingError = "Не вдалося перенести текст у досягнення. Повторіть спробу."
                    }
                }
            },
            preparedSnapshot = preparedSnapshot
        )
    }

    if (showUnfinishedVisitMessage) {
        UnfinishedOneTimePaymentIncreaseDialog { showUnfinishedVisitMessage = false }
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
