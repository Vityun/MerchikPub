package ua.com.merchik.merchik.features.main.componentsUI

import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.QuestionAnswerDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogData
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.launchFeaturesActivity
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class QuestionAnswerDialogUiState(
    val address: String = "",
    val client: String = "",
    val dad2: String = "",

    val avgWorkDurationPlan: String = "",
    val avgWorkDurationFact: String = "",

    val avgUpOnShowcasePlan: String = "",
    val avgUpOnShowcaseFact: String = "",

    val wantReceivePlan: String = "",
    val wantReceiveFact: String = "",

    val visitsPerWeekPlan: String = "",

    val totalUpOnShowcase: Int = 0,
    val reportPrepareCount: Int = 0,

    val comment: String = ""
)

private val QuestionAnswerCorner = 8.dp
private val QuestionAnswerBorderWidth = 1.dp
private val QuestionAnswerFieldMinHeight = 42.dp
private val QuestionAnswerInnerPadding = 3.dp
private val QuestionAnswerValueColumnWidth = 76.dp
private val QuestionAnswerSuffixColumnWidth = 56.dp

private const val QUESTION_PAY_INCREASE_ID = 610

@Composable
fun QuestionAnswerDialog(
    viewModel: MainViewModel,
    state: QuestionAnswerDialogUiState,
    themeTitle: String,
    themeComment: String,
    hint: String,
    onDismiss: () -> Unit,
    onSave: (QuestionAnswerDialogUiState) -> Unit
) {

    val codeDad2 = Gson().fromJson(viewModel.dataJson, Long::class.java)
    val wpdata = RealmManager.getWorkPlanRowByCodeDad2(codeDad2)


    val calculatedData = remember(wpdata?.code_dad2) {
        calculateQuestionAnswerData(
            wpdata = wpdata,
            periodDays = 30
        )
    }

    val factPeriodTooltip = remember(calculatedData) {
        "Дані отримані з ${calculatedData.visitsCount} відвідувань по даному Адресу/Клієнту " +
                "в період з ${calculatedData.periodFromText} по ${calculatedData.periodToText}"
    }

    val context = LocalContext.current

    var maxLinesSubTitle by remember { mutableStateOf(1) }

    var showToolTip by remember { mutableStateOf(false) }
    var showSavedMessage by remember { mutableStateOf(false) }

    var recentComplaintDateSeconds by remember { mutableStateOf<Long?>(null) }
    var isCheckingRecentComplaint by remember { mutableStateOf(false) }

    fun formatComplaintDate(seconds: Long): String {
        return SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        ).format(Date(seconds * 1000L))
    }

    var avgWorkDurationPlan by remember(state.avgWorkDurationPlan) {
        mutableStateOf(state.avgWorkDurationPlan)
    }
//    var avgWorkDurationFact by remember(state.avgWorkDurationFact) {
//        mutableStateOf(state.avgWorkDurationFact)
//    }
    var avgUpOnShowcasePlan by remember(state.avgUpOnShowcasePlan) {
        mutableStateOf(state.avgUpOnShowcasePlan)
    }
//    var avgUpOnShowcaseFact by remember(state.avgUpOnShowcaseFact) {
//        mutableStateOf(state.avgUpOnShowcaseFact)
//    }
    var wantReceivePlan by remember(state.wantReceivePlan) {
        mutableStateOf(state.wantReceivePlan)
    }
//    var wantReceiveFact by remember(state.wantReceiveFact) {
//        mutableStateOf(state.wantReceiveFact)
//    }
    var comment by remember(state.comment) {
        mutableStateOf(state.comment)
    }

    var visitsPerWeekPlan by remember(state.visitsPerWeekPlan) {
        mutableStateOf(state.visitsPerWeekPlan)
    }

    val avgWorkDurationFact = calculatedData.avgWorkDurationFact
    val avgUpOnShowcaseFact = calculatedData.avgUpOnShowcaseFact
    val wantReceiveFact = calculatedData.wantReceiveFact
    val avgCashPenaltyFact = calculatedData.avgCashPenaltyFact

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val commentBringRequester = remember { BringIntoViewRequester() }

    var showValidationErrors by remember { mutableStateOf(false) }
    var validationShakeTrigger by remember { mutableIntStateOf(0) }

    val statementTemplate = remember {
        viewModel.getTranslateString(
            "Я, {USER}, зазвичай витрачаю на роботу з цим клієнтом у цiй ТТ {DURATION_FACT} хвилин, " +
                    "при цьому, зазвичай, за вiзит піднімаю на вітрину приблизно {UP_FACT} одиниць товару і отримую на руки близько {CASH_FACT} грн/вiзит, " +
                    "при цьому втрачаю на сигналах за опціями {CASH_PENALTY_FACT} грн/вiзит. " +
                    "Вважаю, що оплата недостатня. Хочу, щоб вона становила {DESIRED_CASH} грн/вiзит. " +
                    "Або збільшити кількість візитів, зараз в середньому {VISITS_PER_WEEK_FACT} вiзитiв на тиждень, хочу щоб становила {VISITS_PER_WEEK} вiзитiв в тиждень. " +
                    "Крім того, хочу зазначити:",
            100500
        )
    }

    fun openValueDialog(
        title: String,
        text: String,
        currentValue: String,
        onValueChanged: (String) -> Unit
    ) {
        val dialog = DialogData(context)
        dialog.setTitle(title)
        dialog.setText(text)
        dialog.setOperation(
            DialogData.Operations.DecimalNumber,
            currentValue.ifBlank { hint },
            null,
            null
        )

        dialog.setOk("Зберегти") {
            val value = dialog.operationResult?.trim().orEmpty()
            onValueChanged(value)
            dialog.dismiss()
        }

        dialog.setClose {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun saveComplaintNow() {
        coroutineScope.launch {
            runCatching {
                saveQuestionAnswerToDb(
                    wpdata = wpdata,
                    themeTitle = themeTitle,
                    statementTemplate = statementTemplate,

                    avgWorkDurationFact = avgWorkDurationFact,
                    avgUpOnShowcaseFact = avgUpOnShowcaseFact,
                    wantReceiveFact = wantReceiveFact,
                    avgCashPenaltyFact = avgCashPenaltyFact,
                    visitsPerWeekFact = calculatedData.visitsPerWeekFact,

                    desiredCash = wantReceivePlan,
                    desiredVisitsPerWeek = visitsPerWeekPlan,

                    totalUpOnShowcase = calculatedData.totalUpOnShowcase,
                    reportPrepareCount = calculatedData.reportPrepareCount,

                    comment = comment
                )
            }.onSuccess {
                showSavedMessage = true
            }.onFailure { error ->
                Globals.writeToMLOG(
                    "ERROR",
                    "QuestionAnswerDialog/saveQuestionAnswerToDb",
                    "Exception: $error"
                )

                Toast.makeText(
                    context,
                    "Помилка збереження",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogView = LocalView.current

        SideEffect {
            (dialogView.parent as? DialogWindowProvider)
                ?.window
                ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxSize()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 40.dp,
                        bottom = 40.dp
                    )
                    .imePadding()
                    .background(Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.End)
                ) {
                    ImageButton(
                        id = R.drawable.ic_question_1,
                        shape = CircleShape,
                        colorImage = ColorFilter.tint(Color.Gray),
                        sizeButton = 40.dp,
                        sizeImage = 22.dp,
                        modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                        onClick = { showToolTip = true }
                    )

                    ImageButton(
                        id = R.drawable.ic_letter_x,
                        shape = CircleShape,
                        colorImage = ColorFilter.tint(Color.Gray),
                        sizeButton = 40.dp,
                        sizeImage = 25.dp,
                        modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                        onClick = { onDismiss.invoke() }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = themeTitle,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            Text(
                                text = themeComment,
                                maxLines = maxLinesSubTitle,
                                overflow = TextOverflow.Ellipsis,
                                color = if ((viewModel.typeWindow ?: "").equals(
                                        "container",
                                        true
                                    )
                                ) Color.DarkGray else Color.Black,
                                textDecoration = if (maxLinesSubTitle == 1) TextDecoration.Underline else null,
                                modifier = Modifier
                                    .padding(start = 2.dp, end = 2.dp)
                                    .clickable {
                                        maxLinesSubTitle =
                                            if (maxLinesSubTitle == 1) 99 else 1
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.padding(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(color = Color.White)
                        ) {

                            Column(
                                modifier = Modifier
                                    .padding(7.dp)
                                    .verticalScroll(scrollState)
                            ) {
                                QuestionAnswerFramedReadOnlyField(
                                    title = "Адрес",
                                    value = wpdata.addr_txt,
                                    tooltipText = "Это текущий адрес. Редактировать его в этом диалоге нельзя."
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                QuestionAnswerFramedReadOnlyField(
                                    title = "Клиент",
                                    value = wpdata.client_txt,
                                    tooltipText = "Это текущий клиент. Редактировать его в этом диалоге нельзя."
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                QuestionAnswerFramedReadOnlyField(
                                    title = "ДАД2",
                                    value = wpdata.code_dad2.toString(),
                                    tooltipText = "Это текущий ДАД2. Редактировать его в этом диалоге нельзя."
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                val visitsPerWeekTooltip = remember(calculatedData) {
                                    "Дані отримані з ${calculatedData.visitsCount} відвідувань по даному Адресу/Клієнту " +
                                            "в період з ${calculatedData.periodFromText} по ${calculatedData.periodToText}. " +
                                            "Показник розраховано як середню кількість візитів на тиждень."
                                }

                                val cashPenaltyTooltip = remember(calculatedData) {
                                    "Це середнє зниження за опціями. $factPeriodTooltip."
                                }

                                val upTooltip = remember(calculatedData) {
                                    "Це середня кількість піднятого товару. $factPeriodTooltip. " +
                                            "Всего за этот период было поднято ${calculatedData.totalUpOnShowcase} единиц товара, согласно поданным вами отчетов"
                                }

                                QuestionAnswerStatementBlock(
                                    viewModel = viewModel,
                                    statementTemplate = statementTemplate,
                                    userName = wpdata.user_txt,
                                    dad2 = wpdata.code_dad2,
                                    durationFact = avgWorkDurationFact,
                                    durationTooltip = "Це середня тривалість робіт. $factPeriodTooltip",
                                    upFact = avgUpOnShowcaseFact,
                                    upTooltip = upTooltip,
                                    cashFact = wantReceiveFact,
                                    cashTooltip = "Це значення взято з поточних робіт.",
                                    cashPenaltyFact = avgCashPenaltyFact,
                                    cashPenaltyTooltip = cashPenaltyTooltip,
                                    visitsPerWeekFact = calculatedData.visitsPerWeekFact,
                                    visitsPerWeekFactTooltip = visitsPerWeekTooltip,
                                    desiredCash = wantReceivePlan,
                                    visitsPerWeek = visitsPerWeekPlan,
                                    showValidationErrors = showValidationErrors,
                                    validationShakeTrigger = validationShakeTrigger,
                                    comment = comment,
                                    onDesiredCashClick = {
                                        openValueDialog(
                                            title = "Бажана оплата",
                                            text = "Вкажіть суму, яку ви вважаєте справедливою за виконання робіт по цьому клієнту/адресi (грн/вiзит).",
                                            currentValue = wantReceivePlan,
                                            onValueChanged = {
                                                wantReceivePlan = it
                                                if (it.isNotBlank() && visitsPerWeekPlan.isNotBlank()) {
                                                    showValidationErrors = false
                                                }
                                            }
                                        )
                                    },
                                    onVisitsPerWeekClick = {
                                        openValueDialog(
                                            title = "Кількість візитів",
                                            text = "Вкажіть бажану кількість візитів на тиждень.",
                                            currentValue = visitsPerWeekPlan,
                                            onValueChanged = {
                                                visitsPerWeekPlan = it
                                                if (it.isNotBlank() && wantReceivePlan.isNotBlank()) {
                                                    showValidationErrors = false
                                                }
                                            }
                                        )
                                    },
                                    onCommentChange = { comment = it },
                                    commentModifier = Modifier
                                        .bringIntoViewRequester(commentBringRequester)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                coroutineScope.launch {
                                                    delay(250L)
                                                    commentBringRequester.bringIntoView()
                                                }
                                            }
                                        }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        Row {
                            Button(
                                onClick = { onDismiss.invoke() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.blue)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 12.dp, end = 6.dp)
                            ) {
                                Text("Закрыть")
                            }

                            Button(
                                onClick = {
                                    val hasEmptyRequiredFields =
                                        wantReceivePlan.isBlank() || visitsPerWeekPlan.isBlank()

                                    if (hasEmptyRequiredFields) {
                                        showValidationErrors = true
                                        validationShakeTrigger++

                                        Toast.makeText(
                                            context,
                                            "Заполните обязательные поля",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        return@Button
                                    }

                                    if (isCheckingRecentComplaint) {
                                        return@Button
                                    }

                                    coroutineScope.launch {
                                        isCheckingRecentComplaint = true

                                        val complaintKey = ComplaintKey(
                                            themeId = QUESTION_PAY_INCREASE_ID,
                                            clientId = wpdata.client_id.orEmpty(),
                                            addressId = wpdata.addr_id.toString()
                                        )

                                        val lastComplaintDate = runCatching {
                                            findRecentComplaintDate(complaintKey)
                                        }.onFailure { error ->
                                            Globals.writeToMLOG(
                                                "ERROR",
                                                "QuestionAnswerDialog/findRecentComplaintDate",
                                                "Exception: $error"
                                            )
                                        }.getOrNull()

                                        isCheckingRecentComplaint = false

                                        if (lastComplaintDate != null) {
                                            recentComplaintDateSeconds = lastComplaintDate
                                        } else {
                                            saveComplaintNow()
                                        }
                                    }
//                                    coroutineScope.launch {
//                                        runCatching {
//                                            saveQuestionAnswerToDb(
//                                                wpdata = wpdata,
//                                                themeTitle = themeTitle,
//                                                statementTemplate = statementTemplate,
//
//                                                avgWorkDurationFact = avgWorkDurationFact,
//                                                avgUpOnShowcaseFact = avgUpOnShowcaseFact,
//                                                wantReceiveFact = wantReceiveFact,
//                                                avgCashPenaltyFact = avgCashPenaltyFact,
//                                                visitsPerWeekFact = calculatedData.visitsPerWeekFact,
//
//                                                desiredCash = wantReceivePlan,
//                                                desiredVisitsPerWeek = visitsPerWeekPlan,
//
//                                                totalUpOnShowcase = calculatedData.totalUpOnShowcase,
//                                                reportPrepareCount = calculatedData.reportPrepareCount,
//
//                                                comment = comment
//                                            )
//                                        }.onSuccess {
//                                            showSavedMessage = true
//                                        }.onFailure { error ->
//                                            Globals.writeToMLOG(
//                                                "ERROR",
//                                                "QuestionAnswerDialog/saveQuestionAnswerToDb",
//                                                "Exception: $error"
//                                            )
//
//                                            Toast.makeText(
//                                                context,
//                                                "Помилка збереження",
//                                                Toast.LENGTH_LONG
//                                            ).show()
//                                        }
//                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.orange)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 12.dp, start = 6.dp)
                            ) {
                                Text("Сохранить")
                            }
                        }

                    }
                }
            }
        }
    }

    recentComplaintDateSeconds?.let { createdAtSeconds ->
        MessageDialog(
            title = "Жалобы, Замечания, Предложения",
            subTitle = "Схожа скарга вже була",
            status = DialogStatus.ALERT,
            message =
                "За темою Хочу збільшення розміру оплати по цiй ТТ звернення вже було створено " +
                        "${formatComplaintDate(createdAtSeconds)}. " +
                        "Створити нову скаргу?",
            okButtonName = "Так",
            cancelButtonName = "Ні",
            onDismiss = {
                recentComplaintDateSeconds = null
            },
            onCancelAction = {
                recentComplaintDateSeconds = null
            },
            onConfirmAction = {
                recentComplaintDateSeconds = null
                saveComplaintNow()
            }
        )
    }

    if (showToolTip) {
        MessageDialog(
            title = themeTitle,
            status = DialogStatus.ALERT,
            message = themeComment,
            okButtonName = "Ок",
            onDismiss = { showToolTip = false },
            onConfirmAction = { showToolTip = false }
        )
    }

    if (showSavedMessage) {
        MessageDialog(
            title = "Отзыв подан",
            subTitle = "Спасибо!",
            status = DialogStatus.NORMAL,
            message = "Благодаря анализу вашего мнения мы сможем улучшить работу нашего предприятия и тем самым увеличить ваши доходы!",
            okButtonName = "Ок",
            onDismiss = {
                showSavedMessage = false
                onDismiss.invoke()
            },
            onConfirmAction = {
                showSavedMessage = false
                onDismiss.invoke()
            }
        )
    }
}


@Composable
private fun QuestionAnswerReadOnlyBox(
    value: String,
    modifier: Modifier = Modifier,
    innerFillMaxWidth: Boolean = false
) {
    Box(
        modifier = modifier
            .heightIn(min = QuestionAnswerFieldMinHeight)
            .border(
                BorderStroke(
                    QuestionAnswerBorderWidth,
                    colorResource(id = R.color.borderContextMenu)
                ),
                RoundedCornerShape(QuestionAnswerCorner)
            )
            .padding(QuestionAnswerInnerPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .then(
                    if (innerFillMaxWidth) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.wrapContentWidth()
                    }
                )
                .padding(start = 2.dp, end = 2.dp)
                .background(
                    color = colorResource(id = R.color.background_item_filter),
                    shape = RoundedCornerShape(QuestionAnswerCorner)
                )
                .border(
                    BorderStroke(
                        QuestionAnswerBorderWidth,
                        colorResource(id = R.color.borderContextMenu)
                    ),
                    RoundedCornerShape(QuestionAnswerCorner)
                )
        ) {
            Text(
                modifier = Modifier
                    .then(
                        if (innerFillMaxWidth) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.wrapContentWidth()
                        }
                    )
                    .padding(
                        start = 7.dp,
                        top = 4.dp,
                        bottom = 4.dp,
                        end = 7.dp
                    ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                textAlign = TextAlign.Center,
                text = value.ifBlank { "0" }
            )
        }
    }
}

@Composable
private fun QuestionAnswerGroupBox(
    title: String = "Среднее значения за визит",
    onTitleClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = colorResource(id = R.color.borderContextMenu)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        QuestionAnswerBorderWidth,
                        borderColor
                    ),
                    RoundedCornerShape(QuestionAnswerCorner)
                )
                .padding(
                    top = 16.dp,
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 10.dp
                )
        ) {
            content()
        }

        Text(
            text = title,
            color = Color.Gray,
            fontSize = 13.sp,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 12.dp, y = (-8).dp)
                .background(Color.White)
                .padding(horizontal = 6.dp)
                .padding(bottom = 2.dp)
                .clickable { onTitleClick() },
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )

        )
    }
}

@Composable
private fun QuestionAnswerGroupHeader(
    showFact: Boolean
) {
    val planWidth by animateDpAsState(
        targetValue = if (showFact) {
            QuestionAnswerValueColumnWidth
        } else {
            QuestionAnswerValueColumnWidth + QuestionAnswerValueColumnWidth
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "planHeaderWidth"
    )

    val factWidth by animateDpAsState(
        targetValue = if (showFact) QuestionAnswerValueColumnWidth else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "factHeaderWidth"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.width(planWidth),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "План",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(QuestionAnswerSuffixColumnWidth))

        Box(
            modifier = Modifier
                .width(factWidth)
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            if (factWidth > 1.dp) {
                Text(
                    text = "Факт",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .width(QuestionAnswerValueColumnWidth)
                        .graphicsLayer {
                            val progress = (factWidth / QuestionAnswerValueColumnWidth)
                                .coerceIn(0f, 1f)
                            translationX = size.width * (1f - progress)
                            alpha = progress
                        }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun QuestionAnswerMetricRow(
    label: String,
    planValue: String,
    planSuffix: String,
    factValue: String,
    factTooltipText: String,
    showFact: Boolean,
    onPlanClick: () -> Unit
) {
    val planWidth by animateDpAsState(
        targetValue = if (showFact) {
            QuestionAnswerValueColumnWidth
        } else {
            QuestionAnswerValueColumnWidth + QuestionAnswerValueColumnWidth
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "planWidth"
    )

    val factWidth by animateDpAsState(
        targetValue = if (showFact) QuestionAnswerValueColumnWidth else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "factWidth"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Tooltip(
            text = "Натисніть, щоб ввести або переглянути значення"
        ) {
            QuestionAnswerSmallClickableField(
                value = planValue,
                onClick = onPlanClick,
                modifier = Modifier.width(planWidth)
            )
        }

        Text(
            text = "($planSuffix)",
            modifier = Modifier
                .width(QuestionAnswerSuffixColumnWidth)
                .padding(start = 4.dp),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Box(
            modifier = Modifier
                .width(factWidth)
                .clipToBounds()
        ) {
            if (factWidth > 1.dp) {
                Box(
                    modifier = Modifier
                        .width(QuestionAnswerValueColumnWidth)
                        .graphicsLayer {
                            val progress = (factWidth / QuestionAnswerValueColumnWidth)
                                .coerceIn(0f, 1f)
                            translationX = size.width * (1f - progress)
                            alpha = progress
                        }
                ) {
                    Tooltip(text = factTooltipText) {
                        QuestionAnswerSmallReadOnlyField(
                            value = factValue,
                            modifier = Modifier.width(QuestionAnswerValueColumnWidth)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionAnswerFramedReadOnlyField(
    title: String,
    value: String,
    tooltipText: String
) {
    Tooltip(text = tooltipText) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            QuestionAnswerBorderWidth,
                            colorResource(id = R.color.borderContextMenu)
                        ),
                        RoundedCornerShape(QuestionAnswerCorner)
                    )
                    .padding(
                        top = 12.dp,
                        start = 6.dp,
                        end = 6.dp,
                        bottom = 6.dp
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                QuestionAnswerReadOnlyValueChip(
                    value = value
                )
            }

            Text(
                text = title,
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 13.sp,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 12.dp, y = (-8).dp)
                    .background(Color.White)
                    .padding(horizontal = 6.dp, vertical = 0.dp)
            )
        }
    }
}

@Composable
private fun QuestionAnswerSmallReadOnlyField(
    value: String,
    modifier: Modifier = Modifier
) {
    QuestionAnswerReadOnlyBox(
        value = value,
        modifier = modifier.height(38.dp),
        innerFillMaxWidth = true
    )
}

@Composable
private fun QuestionAnswerSmallClickableField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .border(
                BorderStroke(
                    QuestionAnswerBorderWidth,
                    colorResource(id = if (value.isNotEmpty()) R.color.borderContextMenu else R.color.red_error)
                ),
                RoundedCornerShape(QuestionAnswerCorner)
            )
            .background(Color.White, RoundedCornerShape(QuestionAnswerCorner))
            .clickable { onClick() }
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = value,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            fontSize = 13.sp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionAnswerStatementBlock(
    viewModel: MainViewModel,
    statementTemplate: String,
    userName: String,
    dad2: Long,
    durationFact: String,
    durationTooltip: String,
    upFact: String,
    upTooltip: String,
    cashFact: String,
    cashTooltip: String,
    cashPenaltyFact: String,
    cashPenaltyTooltip: String,
    visitsPerWeekFact: String,
    visitsPerWeekFactTooltip: String,
    desiredCash: String,
    visitsPerWeek: String,
    showValidationErrors: Boolean,
    validationShakeTrigger: Int,
    comment: String,
    onDesiredCashClick: () -> Unit,
    onVisitsPerWeekClick: () -> Unit,
    onCommentChange: (String) -> Unit,
    commentModifier: Modifier = Modifier
) {
    QuestionAnswerGroupBox(
        title = "Заява",
        onTitleClick = {}
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            QuestionAnswerStatementTemplateText(
                viewModel = viewModel,
                dad2 = dad2,
                template = statementTemplate,
                inserts = listOf(
                    QuestionAnswerStatementInsert.Text(
                        key = "{USER}",
                        value = userName.ifBlank { "0" }
                    ),

                    QuestionAnswerStatementInsert.ReadOnly(
                        key = "{DURATION_FACT}",
                        value = durationFact,
                        tooltip = durationTooltip
                    ),

                    QuestionAnswerStatementInsert.ReadOnly(
                        key = "{UP_FACT}",
                        value = upFact,
                        tooltip = upTooltip
                    ),

                    QuestionAnswerStatementInsert.ReadOnly(
                        key = "{CASH_FACT}",
                        value = cashFact,
                        tooltip = cashTooltip
                    ),

                    QuestionAnswerStatementInsert.ReadOnly(
                        key = "{CASH_PENALTY_FACT}",
                        value = cashPenaltyFact,
                        tooltip = cashPenaltyTooltip
                    ),

                    QuestionAnswerStatementInsert.Editable(
                        key = "{DESIRED_CASH}",
                        value = desiredCash,
                        placeholder = "вкажіть",
                        onClick = onDesiredCashClick,
                        isError = showValidationErrors && desiredCash.isBlank(),
                        shakeTrigger = validationShakeTrigger
                    ),

                    QuestionAnswerStatementInsert.ReadOnly(
                        key = "{VISITS_PER_WEEK_FACT}",
                        value = visitsPerWeekFact,
                        tooltip = visitsPerWeekFactTooltip
                    ),

                    QuestionAnswerStatementInsert.Editable(
                        key = "{VISITS_PER_WEEK}",
                        value = visitsPerWeek,
                        placeholder = "вкажіть",
                        onClick = onVisitsPerWeekClick,
                        isError = showValidationErrors && visitsPerWeek.isBlank(),
                        shakeTrigger = validationShakeTrigger
                    )
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            QuestionAnswerCommentInput(
                value = comment,
                onValueChange = onCommentChange,
                modifier = commentModifier,
                placeholder = "Тут додайте усі фактори, котрі суттєво впливають на тривалість виконання робіт. " +
                        "Складнощі з: (планограмами, персоналом, складом, фасуванням, і т.д.) та вкажіть свої аргументи щодо збільшення кількості візитів, або вартості."

            )
        }
    }
}

private sealed class QuestionAnswerStatementInsert(
    open val key: String
) {
    data class Text(
        override val key: String,
        val value: String
    ) : QuestionAnswerStatementInsert(key)

    data class ReadOnly(
        override val key: String,
        val value: String,
        val tooltip: String
    ) : QuestionAnswerStatementInsert(key)

    data class Editable(
        override val key: String,
        val value: String,
        val placeholder: String,
        val onClick: () -> Unit,
        val isError: Boolean = false,
        val shakeTrigger: Int = 0
    ) : QuestionAnswerStatementInsert(key)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionAnswerStatementTemplateText(
    viewModel: MainViewModel,
    template: String,
    dad2: Long,
    inserts: List<QuestionAnswerStatementInsert>
) {

    val context = LocalContext.current

    val annotatedText = remember(template, inserts) {
        buildQuestionAnswerStatementText(
            template = template,
            inserts = inserts
        )
    }

    val inlineContent = remember(inserts) {
        inserts
            .filterNot { it is QuestionAnswerStatementInsert.Text }
            .associate { insert ->
                insert.key to InlineTextContent(
                    placeholder = Placeholder(
                        width = insert.inlineWidth(),
                        height = 30.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    when (insert) {
                        is QuestionAnswerStatementInsert.ReadOnly -> {
                            TooltipWithDetails(
                                text = insert.tooltip,
                                onDetailsClick = {
                                    viewModel.launcher?.let { launcher ->
                                        launchFeaturesActivity(
                                            launcher = launcher,
                                            context = context,
                                            viewModelClass = WpDataDBViewModel::class,
                                            dataJson = Gson().toJson(
                                                mapOf("codeDad2" to dad2)
                                            ),
                                            modeUI = ModeUI.DEFAULT,
                                            contextUI = ContextUI.WP_DATA_ADDRESS_CLIENT,
                                            title = "План робiт",
                                            subTitle = "В текущей форме отображены визиты, на основании которых рассчитаны средние показатели работ (длительность, количество поднятого товара и пр.).",
                                            origin = null
                                        )
                                    }

                                }
                            ) {
                                QuestionAnswerInlineReadOnlyChip(
                                    value = insert.value,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        is QuestionAnswerStatementInsert.Editable -> {
                            QuestionAnswerInlineEditableChip(
                                value = insert.value,
                                placeholder = insert.placeholder,
                                onClick = insert.onClick,
                                isError = insert.isError,
                                shakeTrigger = insert.shakeTrigger,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        is QuestionAnswerStatementInsert.Text -> Unit
                    }
                }
            }
    }

    BasicText(
        text = annotatedText,
        inlineContent = inlineContent,
        style = LocalTextStyle.current.copy(
            color = Color.Black,
            fontSize = 14.sp,
            lineHeight = 34.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        ),
        modifier = Modifier.fillMaxWidth()
    )
}


@Composable
private fun QuestionAnswerReadOnlyValueChip(
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentWidth()
            .background(
                color = colorResource(id = R.color.background_item_filter),
                shape = RoundedCornerShape(QuestionAnswerCorner)
            )
            .border(
                BorderStroke(
                    QuestionAnswerBorderWidth,
                    colorResource(id = R.color.borderContextMenu)
                ),
                RoundedCornerShape(QuestionAnswerCorner)
            )
    ) {
        Text(
            modifier = Modifier.padding(
                start = 7.dp,
                top = 4.dp,
                bottom = 4.dp,
                end = 7.dp
            ),
            text = value.ifBlank { "0" },
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun QuestionAnswerInlineEditableChip(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    shakeTrigger: Int = 0
) {
    val isEmpty = value.isBlank()
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0 && isError) {
            shakeOffset.snapTo(0f)

            listOf(
                -10f,
                10f,
                -8f,
                8f,
                -5f,
                5f,
                -2f,
                2f,
                0f
            ).forEach { target ->
                shakeOffset.animateTo(
                    targetValue = target,
                    animationSpec = tween(durationMillis = 45)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = shakeOffset.value
            }
            .border(
                BorderStroke(
                    QuestionAnswerBorderWidth,
                    colorResource(
                        id = if (isError || isEmpty) {
                            R.color.red_error
                        } else {
                            R.color.borderContextMenu
                        }
                    )
                ),
                RoundedCornerShape(QuestionAnswerCorner)
            )
            .background(Color.White, RoundedCornerShape(QuestionAnswerCorner))
            .clickable { onClick() }
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.ifBlank { placeholder },
            color = if (isEmpty) Color.Gray else Color.Black,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuestionAnswerTextPart(
    text: String
) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = Color.Black
    )
}

@Composable
private fun QuestionAnswerInlineReadOnlyChip(
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                BorderStroke(
                    QuestionAnswerBorderWidth,
                    colorResource(id = R.color.borderContextMenu)
                ),
                RoundedCornerShape(QuestionAnswerCorner)
            )
            .background(
                colorResource(id = R.color.background_item_filter),
                RoundedCornerShape(QuestionAnswerCorner)
            )
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.ifBlank { "0" },
            color = colorResource(id = R.color.centerColor),
            textDecoration = TextDecoration.Underline,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
private fun QuestionAnswerCommentInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Зазначте усі фактори, котрі суттєво впливають на тривалість виконання робіт. " +
            "Складнощі з: (планограмами, персоналом, складом, фасуванням, і т.д.) та вкажіть свої побажання щодо збільшення кількості візитів, або вартості."
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        minLines = 3,
        maxLines = 6,
        textStyle = LocalTextStyle.current.copy(
            color = Color.Black,
            fontSize = 16.sp
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .border(
                BorderStroke(
                    1.dp,
                    colorResource(id = if (value.isNotEmpty()) R.color.borderContextMenu else R.color.red_error)
//                            colorResource(id = R.color.borderContextMenu)
                ),
                RoundedCornerShape(QuestionAnswerCorner)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopStart
            ) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }

                innerTextField()
            }
        }
    )
}


private data class QuestionAnswerStatementPart(
    val text: String,
    val isPlaceholder: Boolean
)

private fun buildQuestionAnswerStatementText(
    template: String,
    inserts: List<QuestionAnswerStatementInsert>
): AnnotatedString {
    val insertsMap = inserts.associateBy { it.key }
    val keys = inserts.map { it.key }

    return buildAnnotatedString {
        var cursor = 0

        while (cursor < template.length) {
            val next = keys
                .mapNotNull { key ->
                    val index = template.indexOf(key, startIndex = cursor)
                    if (index >= 0) key to index else null
                }
                .minByOrNull { it.second }

            if (next == null) {
                append(template.substring(cursor))
                break
            }

            val key = next.first
            val index = next.second

            if (index > cursor) {
                append(template.substring(cursor, index))
            }

            when (val insert = insertsMap[key]) {
                is QuestionAnswerStatementInsert.Text -> {
                    append(insert.value)
                }

                is QuestionAnswerStatementInsert.ReadOnly -> {
                    appendInlineContent(
                        id = insert.key,
                        alternateText = insert.value.ifBlank { "0" }
                    )
                }

                is QuestionAnswerStatementInsert.Editable -> {
                    appendInlineContent(
                        id = insert.key,
                        alternateText = insert.value.ifBlank { insert.placeholder }
                    )
                }

                null -> {
                    append(key)
                }
            }

            cursor = index + key.length
        }
    }
}

private fun splitQuestionAnswerTemplate(
    template: String,
    keys: List<String>
): List<QuestionAnswerStatementPart> {
    if (template.isBlank()) return emptyList()

    val result = mutableListOf<QuestionAnswerStatementPart>()
    var cursor = 0

    while (cursor < template.length) {
        val next = keys
            .mapNotNull { key ->
                val index = template.indexOf(key, startIndex = cursor)
                if (index >= 0) key to index else null
            }
            .minByOrNull { it.second }

        if (next == null) {
            val tail = template.substring(cursor)
            if (tail.isNotBlank()) {
                result += QuestionAnswerStatementPart(tail.trim(), false)
            }
            break
        }

        val key = next.first
        val index = next.second

        if (index > cursor) {
            val text = template.substring(cursor, index)
            if (text.isNotBlank()) {
                result += QuestionAnswerStatementPart(text.trim(), false)
            }
        }

        result += QuestionAnswerStatementPart(key, true)

        cursor = index + key.length
    }

    return result
}

private suspend fun saveQuestionAnswerToDb(
    wpdata: WpDataDB,
    themeTitle: String,
    statementTemplate: String,

    avgWorkDurationFact: String,
    avgUpOnShowcaseFact: String,
    wantReceiveFact: String,
    avgCashPenaltyFact: String,
    visitsPerWeekFact: String,

    desiredCash: String,
    desiredVisitsPerWeek: String,

    totalUpOnShowcase: Int,
    reportPrepareCount: Int,

    comment: String
) {
//    withContext(Dispatchers.IO) {
    val nowSeconds = System.currentTimeMillis() / 1000L

    val answerText = buildQuestionAnswerSavedText(
        template = statementTemplate,
        userName = wpdata.user_txt,
        durationFact = avgWorkDurationFact,
        upFact = avgUpOnShowcaseFact,
        cashFact = wantReceiveFact,
        cashPenaltyFact = avgCashPenaltyFact,
        visitsPerWeekFact = visitsPerWeekFact,
        desiredCash = desiredCash,
        desiredVisitsPerWeek = desiredVisitsPerWeek,
        comment = comment
    )

    val avgAnswerText = buildString {
        append("avgWorkDurationFact=").append(avgWorkDurationFact)
        append("; avgUpOnShowcaseFact=").append(avgUpOnShowcaseFact)
        append("; wantReceiveFact=").append(wantReceiveFact)
        append("; visitsPerWeekFact=").append(visitsPerWeekFact)
        append("; desiredCash=").append(desiredCash.ifBlank { "0" })
        append("; desiredVisitsPerWeek=").append(desiredVisitsPerWeek.ifBlank { "0" })
        append("; totalUpOnShowcase=").append(totalUpOnShowcase)
        append("; reportPrepareCount=").append(reportPrepareCount)
    }

    val questionAnswer = QuestionAnswerDB().apply {
        id = System.currentTimeMillis()

        userId = Globals.userId.toLong()

        question = themeTitle
        answer = desiredCash
        dt = nowSeconds

        avgAnswer = desiredVisitsPerWeek

        /*
         * Если type_user нужен строго по старой логике —
         * тут можно будет заменить на нужный код.
         */
        typeUser = null

        idQuest = QUESTION_PAY_INCREASE_ID
        idQuestCom = QUESTION_PAY_INCREASE_ID

        this.comment = answerText

        /*
         * ДокНом берем из wpdata.doc_num.
         */
        objectId = wpdata.code_dad2.toString()
        objectStr = wpdata.doc_num_otchet.orEmpty()

        adrId = wpdata.addr_id.toString()
        kliId = wpdata.client_id.orEmpty()

        objectDate = wpdata.dt?.time?.div(1000L) ?: 0L

        /*
         * Привязываем запись к текущему ДАД2.
         */
        elementId = ""

        optionId = ""
        mnenieId = ""
    }

    RoomManager.SQL_DB
        .questionAnswerDao()
        .insert(questionAnswer)
//    }
}

private fun buildQuestionAnswerSavedText(
    template: String,
    userName: String,
    durationFact: String,
    upFact: String,
    cashFact: String,
    cashPenaltyFact: String,
    visitsPerWeekFact: String,
    desiredCash: String,
    desiredVisitsPerWeek: String,
    comment: String
): String {
    val result = template
        .replace("{USER}", userName.ifBlank { "0" })
        .replace("{DURATION_FACT}", durationFact.ifBlank { "0" })
        .replace("{UP_FACT}", upFact.ifBlank { "0" })
        .replace("{CASH_FACT}", cashFact.ifBlank { "0" })
        .replace("{CASH_PENALTY_FACT}", cashPenaltyFact.ifBlank { "0" })
        .replace("{VISITS_PER_WEEK_FACT}", visitsPerWeekFact.ifBlank { "0" })
        .replace("{DESIRED_CASH}", desiredCash.ifBlank { "не вказано" })
        .replace("{VISITS_PER_WEEK}", desiredVisitsPerWeek.ifBlank { "не вказано" })

    return if (comment.isBlank()) {
        result
    } else {
        "$result\n\n$comment"
    }
}

private data class QuestionAnswerCalculatedData(
    val periodFromText: String = "",
    val periodToText: String = "",
    val visitsCount: Int = 0,

    val avgWorkDurationFact: String = "0",
    val avgUpOnShowcaseFact: String = "0",
    val wantReceiveFact: String = "0",
    val avgCashPenaltyFact: String = "0",

    val visitsPerWeekFact: String = "0",

    val totalUpOnShowcase: Int = 0,
    val reportPrepareCount: Int = 0
)

private fun calculateQuestionAnswerData(
    wpdata: WpDataDB?,
    periodDays: Int = 30
): QuestionAnswerCalculatedData {
    if (wpdata == null) return QuestionAnswerCalculatedData()

    return try {
        val documentDateMs = wpdata.dt?.time ?: System.currentTimeMillis()
        val periodFromMs = documentDateMs - periodDays * 24L * 60L * 60L * 1000L

        val periodFromDate = Date(periodFromMs)
        val periodToDate = Date(documentDateMs)

        val periodFromText = formatQuestionAnswerDate(periodFromDate)
        val periodToText = formatQuestionAnswerDate(periodToDate)

        val visits = RealmManager.INSTANCE
            .where(WpDataDB::class.java)
            .equalTo("addr_id", wpdata.addr_id)
            .equalTo("client_id", wpdata.client_id)
            .greaterThanOrEqualTo("dt", periodFromDate)
            .lessThanOrEqualTo("dt", periodToDate)
            .findAll()
            .let { RealmManager.INSTANCE.copyFromRealm(it) }
            .filter { visit ->
                visit.code_dad2 > 0L
            }

        val workDurationMinutes = visits.mapNotNull { visit ->
            calculateVisitDurationMinutes(
                start = visit.visit_start_dt,
                end = visit.client_end_dt
            )
        }

        val avgWorkDurationFact = if (workDurationMinutes.isNotEmpty()) {
            formatQuestionAnswerNumber(workDurationMinutes.average())
        } else {
            "0"
        }

        val dad2List = visits
            .map { it.code_dad2 }
            .filter { it > 0L }
            .distinct()

        val reportPrepareList = if (dad2List.isNotEmpty()) {
            ReportPrepareRealm.getReportPrepareUpMoreThanZeroByDad2List_LIST(dad2List)
        } else {
            emptyList()
        }

        val reportPrepareByDad2 = reportPrepareList
            .groupBy { it.codeDad2?.toLongOrNull() }

        val upByVisitList = dad2List.map { dad2 ->
            val rowsForVisit = reportPrepareByDad2[dad2].orEmpty()

            rowsForVisit.sumOf { reportPrepare ->
                reportPrepare.up.toQuestionAnswerIntOrZero()
            }
        }

        val totalUpOnShowcase = upByVisitList.sum()

        val avgUpOnShowcaseFact = if (upByVisitList.isNotEmpty()) {
            formatQuestionAnswerIntNumber(upByVisitList.average())
        } else {
            "-"
        }

        val periodDaysFact = periodDays.coerceAtLeast(1)

        val visitsPerWeekFact = if (visits.isNotEmpty()) {
            formatQuestionAnswerVisitsPerWeek(
                visits.size * 7.0 / periodDaysFact
            )
        } else {
            "0"
        }

        val avgCashPenaltyFact = if (visits.isNotEmpty()) {
            formatQuestionAnswerWholeNumber(
                visits.map { it.cash_penalty }.average()
            )
        } else {
            "0"
        }

        QuestionAnswerCalculatedData(
            periodFromText = periodFromText,
            periodToText = periodToText,
            visitsCount = visits.size,
            avgWorkDurationFact = avgWorkDurationFact,
            avgUpOnShowcaseFact = avgUpOnShowcaseFact,
            wantReceiveFact = formatQuestionAnswerNumber(wpdata.cash_ispolnitel),
            visitsPerWeekFact = visitsPerWeekFact,
            totalUpOnShowcase = totalUpOnShowcase,
            avgCashPenaltyFact = avgCashPenaltyFact,
            reportPrepareCount = reportPrepareList.size
        )

    } catch (e: Exception) {
        Globals.writeToMLOG(
            "ERROR",
            "QuestionAnswerDialog/calculateQuestionAnswerData",
            "Exception e: $e"
        )

        QuestionAnswerCalculatedData()
    }
}

private fun calculateVisitDurationMinutes(
    start: Long,
    end: Long
): Double? {
    if (start <= 0L || end <= 0L || end <= start) return null

    val diff = end - start

    /*
     * В большинстве мест такие даты у тебя выглядят как unix seconds.
     * Но на всякий случай оставляем защиту, если вдруг придут milliseconds.
     */
    return if (diff > 5L * 24L * 60L * 60L) {
        diff / 1000.0 / 60.0
    } else {
        diff / 60.0
    }
}

private fun formatQuestionAnswerDate(date: Date): String {
    return runCatching {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
    }.getOrDefault("")
}

private fun formatQuestionAnswerNumber(value: Double): String {
    return formatQuestionAnswerWholeNumber(value)
}

private fun formatQuestionAnswerIntNumber(value: Double): String {
    return formatQuestionAnswerWholeNumber(value)
}

private fun formatQuestionAnswerVisitsPerWeek(value: Double): String {
    return formatQuestionAnswerWholeNumber(value)
}

private fun formatQuestionAnswerWholeNumber(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"

    return BigDecimal
        .valueOf(value)
        .setScale(0, RoundingMode.HALF_UP)
        .toPlainString()
}

private fun QuestionAnswerStatementInsert.inlineWidth(): TextUnit {
    return when (this) {
        is QuestionAnswerStatementInsert.ReadOnly -> {
            val text = value.ifBlank { "0" }
            val width = text.length * 9 + 28
            width.coerceIn(44, 96).sp
        }

        is QuestionAnswerStatementInsert.Editable -> {
            val text = value.ifBlank { placeholder }
            val width = text.length * 9 + 34
            width.coerceIn(90, 130).sp
        }

        is QuestionAnswerStatementInsert.Text -> {
            1.sp
        }
    }
}


private fun String?.toQuestionAnswerIntOrZero(): Int {
    if (this.isNullOrBlank()) return 0

    val normalized = this
        .trim()
        .replace(',', '.')

    return normalized.toIntOrNull()
        ?: normalized.toDoubleOrNull()?.toInt()
        ?: 0
}


private const val COMPLAINT_REPEAT_WINDOW_SECONDS = 7L * 24L * 60L * 60L

private data class ComplaintKey(
    val themeId: Int,
    val clientId: String,
    val addressId: String
)

private suspend fun findRecentComplaintDate(
    key: ComplaintKey,
    nowSeconds: Long = System.currentTimeMillis() / 1000L
): Long? = withContext(Dispatchers.IO) {
    if (
        key.themeId <= 0 ||
        key.clientId.isBlank() ||
        key.addressId.isBlank()
    ) {
        return@withContext null
    }

    RoomManager.SQL_DB
        .questionAnswerDao()
        .findLastComplaintDate(
            key.themeId,
            key.clientId,
            key.addressId,
            nowSeconds - COMPLAINT_REPEAT_WINDOW_SECONDS
        )
}
