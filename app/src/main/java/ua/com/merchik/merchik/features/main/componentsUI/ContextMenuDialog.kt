package ua.com.merchik.merchik.features.main.componentsUI

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface.ExchangeResponseInterface
import ua.com.merchik.merchik.ServerExchange.TablesExchange.OptionsExchange
import ua.com.merchik.merchik.ServerExchange.TablesExchange.ReportPrepareExchange
import ua.com.merchik.merchik.ViewHolders.Clicks.clickVoid
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.MainEvent
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.iconResOrNull
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm
import ua.com.merchik.merchik.database.room.factory.WPDataAdditionalFactory
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.DBViewModels.SMSPlanSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.Main.CardItemsUI
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.launchFeaturesActivity
import java.time.format.DateTimeFormatter
import java.util.Locale


@Stable
data class ContextMenuController(
    val open: (wp: WpDataDB, actions: List<ContextMenuAction>) -> Unit,
    val close: () -> Unit
)

@Stable
class DialogCloseController {
    private var closeFn: (() -> Unit)? = null

    fun bind(close: () -> Unit) {
        closeFn = close
    }

    fun unbind() {
        closeFn = null
    }

    fun close() {
        closeFn?.invoke()
    }
}

@Composable
fun rememberDialogCloseController(): DialogCloseController =
    remember { DialogCloseController() }


data class ContextMenuState(
    val actions: List<ContextMenuAction>,
    val wpDataDB: WpDataDB              // держим выбранный объект тут
)


data class ContextMenuResult(
    val action: ContextMenuAction,
    val wpDataDB: WpDataDB?
)

data class MessageDialogData(
    val title: String = "Додатковий заробіток",
    val subTitle: String? = "Базовий мерчендайзинг",
    val message: String,
    val status: DialogStatus,
    val positivText: String? = null,
    val showButton: Boolean = true,
    val isCancelable: Boolean = true,
    val filterLogic: Boolean = false
)

data class CardItemsData(
    val dateItemUI: DataItemUI,
    val title: String
)

sealed class ContextMenuAction(val title: String) {

    data object OpenUFMDWPDataSelector :
        ContextMenuAction("-")

    data object ShowAllVizitInAdress :
        ContextMenuAction("Показати всі відвідування за цією адресою")

    data object AcceptOrder :
        ContextMenuAction("Прийняти це замовлення")

    data object AcceptAllAtAddress :
        ContextMenuAction("Прийняти всіх клієнтів за цією адресою")

    data object RejectOrder :
        ContextMenuAction("Відмовитися від цього замовлення")

    data object RejectAddress :
        ContextMenuAction("Відмовитися від цієї адреси")

    data object RejectClient :
        ContextMenuAction("Відмовитися від цього клієнта")

    data object RejectByType :
        ContextMenuAction("Відмовитися від цього типу робіт")

    data object OpenVisit :
        ContextMenuAction("Відкрити відвідування")

    data object OpenOrder :
        ContextMenuAction("Інформація про це замовлення")

    data object AskMoreMoney :
        ContextMenuAction("Запросити більшу оплату")

    data object Feedback :
        ContextMenuAction("Зворотний зв’язок")

    data object ConfirmAcceptOneTime :
        ContextMenuAction("Виконати один раз")

    data object ConfirmAcceptInfinite :
        ContextMenuAction("Виконувати регулярно")

    data object ConfirmAllAcceptOneTime :
        ContextMenuAction("Виконати один раз усі роботи по %s клієнта за цією адресою")

    data object ConfirmAllAcceptInfinite :
        ContextMenuAction("Виконувати регулярно всі роботи по %s клієнта за цією адресою")

    data object OpenSMSPlanDirectory :
        ContextMenuAction("Переглянути журнал заявок")

    data object Close :
        ContextMenuAction("Закрити")

}


/** Хост: держит состояние и рисует диалог. Возвращает контроллер с open/close. */
@Composable
fun rememberContextMenuHost(
    viewModel: MainViewModel,
    context: Context,
    closeMapsDialogAnimated: () -> Unit
): ContextMenuController {
    var selectedItem by remember { mutableStateOf<ContextMenuState?>(null) }
    val focusManager = LocalFocusManager.current

    var showMessageDialog by remember { mutableStateOf<MessageDialogData?>(null) }
    var showCardItemsDialog by remember { mutableStateOf<CardItemsData?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    val formatterDDmmYYYY = DateTimeFormatter
        .ofPattern("dd MMM yyyy")
        .withLocale(Locale.getDefault())

    LaunchedEffect(lifecycleOwner) {
        // жизненный цикл: подписываемся когда owner в STARTED, отписываемся при stop
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is MainEvent.ShowContextMenu -> selectedItem = event.menuState
                    is MainEvent.ShowMessageDialog -> {
                        // debug log
                        Log.d("MainUI", "Received ShowMessageDialog: ${event.data}")
                        Globals.writeToMLOG(
                            "INFO",
                            "MainUI.rememberContextMenuHost",
                            "Received ShowMessageDialog: ${event.data}"
                        )
                        showMessageDialog = event.data
                    }

                    is MainEvent.ShowCardItemsDialog -> showCardItemsDialog = event.cardItemsData

                    is MainEvent.ShowLoading -> {

                        showMessageDialog = MessageDialogData(
                            subTitle = "Чекаємо на відповідь від сервера",
                            status = DialogStatus.LOADING,
                            message = " ",
                            showButton = false,
                            isCancelable = false
                        )
                    }

                    is MainEvent.LoadingCompleted -> {
                        showMessageDialog = null
                    }

                    is MainEvent.LoadingCanceled -> {
                        showMessageDialog = null
                    }


                    is MainEvent.JumpToVizitAndCloseMaps -> {
                        closeMapsDialogAnimated()
                        viewModel.performPendingK()
                    }

                    is MainEvent.OpenUFMDWPDataSelector -> {


                        viewModel.launcher?.let {
                            launchFeaturesActivity(
                                launcher = it,
                                context = context,
                                viewModelClass = WpDataDBViewModel::class,
                                dataJson = Gson().toJson(
                                    mapOf("addressId" to event.wp.addr_id)
                                ),
                                modeUI = ModeUI.MULTI_SELECT,
                                contextUI = ContextUI.WP_DATA,
                                title = "Додатковий заробіток",
                                subTitle = viewModel.getTranslateString(
                                    "Этот раздел предназначен для внештатных исполнителей. В нем отображаются работы которые может взять на исполнение любой пользователь. Для этого кликните по интересующему вас визиту и выберите из контекстного меню нужный вам",
                                    9070
                                )
                            )
                        }

                    }


                }
            }
        }
    }


    // Рендер самого диалога один раз, когда selectedItem != null
    selectedItem?.let { state ->
        ContextMenuDialog(
            visible = true,
            wpDataDB = state.wpDataDB,
            actions = state.actions,
            onDismiss = { selectedItem = null },
            onActionClick = { result ->
                focusManager.clearFocus(force = true)
                when (result.action) {
                    is ContextMenuAction.ShowAllVizitInAdress -> {

                        val wp = result.wpDataDB ?: run {
                            selectedItem = null
                            return@ContextMenuDialog
                        }

                        // periodText должен быть известен MainUI/MapsDialog.
                        // Самый простой путь: хранить periodText в MainViewModel.state,
                        // или передать в openContextMenu как параметр.
                        // Я покажу вариант через ViewModel.state (самый чистый).
                        val periodText = viewModel.uiState.value.filters?.let { f ->
                            val range = f.rangeDataByKey
                            val start = range?.start?.format(formatterDDmmYYYY) ?: "?"
                            val end = range?.end?.format(formatterDDmmYYYY) ?: "?"
                            "$start по $end"
                        } ?: "не визначено"

                        viewModel.requestJumpToAddressVisits(wp, periodText)
                        selectedItem = null
                    }

                    is ContextMenuAction.AcceptOrder -> {
                        selectedItem = result.wpDataDB?.let {
                            ContextMenuState(
                                actions = listOf(
                                    ContextMenuAction.ConfirmAcceptOneTime,
                                    ContextMenuAction.ConfirmAcceptInfinite,
                                    ContextMenuAction.Close
                                ),
                                wpDataDB = it
                            )
                        }
                    }

                    is ContextMenuAction.AcceptAllAtAddress -> {
                        selectedItem = result.wpDataDB?.let {
                            ContextMenuState(
                                actions = listOf(
                                    ContextMenuAction.ConfirmAllAcceptOneTime,
                                    ContextMenuAction.ConfirmAllAcceptInfinite,
                                    ContextMenuAction.Close
                                ),
                                wpDataDB = it
                            )
                        }
                    }

                    is ContextMenuAction.ConfirmAcceptOneTime -> {
                        result.wpDataDB?.let { viewModel.requestAcceptOneTime(it) }
                        selectedItem = null
                    }

                    is ContextMenuAction.ConfirmAcceptInfinite -> {
                        result.wpDataDB?.let { viewModel.requestAcceptInfinite(it) }
                        selectedItem = null
                    }

                    is ContextMenuAction.ConfirmAllAcceptOneTime -> {
                        result.wpDataDB?.let { viewModel.requestAcceptAllWorkOneTime(it) }
                        selectedItem = null
                    }

                    is ContextMenuAction.ConfirmAllAcceptInfinite -> {
                        result.wpDataDB?.let { viewModel.requestAcceptAllWorkInfinite(it) }
                        selectedItem = null
                    }

                    is ContextMenuAction.OpenVisit -> {
                        result.wpDataDB?.let {
                            val intent = Intent(context, DetailedReportActivity::class.java)
                            intent.putExtra("WpDataDB_ID", it.id)
                            context.startActivity(intent)
                        }
                        selectedItem = null
                    }

                    ContextMenuAction.OpenOrder -> {
                        result.wpDataDB?.let {
                            val intent = Intent(context, DetailedReportActivity::class.java)
                            intent.putExtra("WpDataDB_ID", it.id)
                            context.startActivity(intent)
                        }
                        selectedItem = null
                    }

                    is ContextMenuAction.OpenSMSPlanDirectory -> {
                        val intent = Intent(context, FeaturesActivity::class.java)
                        val bundle = Bundle().apply {
                            putString("viewModel", SMSPlanSDBViewModel::class.java.canonicalName)
                            putString("contextUI", ContextUI.SMS_PLAN_DEFAULT.toString())
                            putString("modeUI", ModeUI.MULTI_SELECT.toString())
                            putString("title", "Заявки")
                            putString("subTitle", "## subTitle")
                        }
                        intent.putExtras(bundle)
                        // launcher.launch(intent) // если используешь launcher — передай сюда как зависимость
                        context.startActivity(intent)
                        selectedItem = null
                    }

                    is ContextMenuAction.Close -> selectedItem = null

                    else -> {
                        // твой ShowMessageDialog
                        // showMessageDialog = ...
                        selectedItem = null
                    }
                }
            }
        )
    }

    // Сам контроллер
    val open: (WpDataDB, List<ContextMenuAction>) -> Unit = { wp, actions ->
        selectedItem = ContextMenuState(actions = actions, wpDataDB = wp)
    }
    val close: () -> Unit = { selectedItem = null }

    showCardItemsDialog?.let {
        CardItemsUI(
            title = it.title,
            item = it.dateItemUI,
            viewModel = viewModel,
            onDismiss = {
                showCardItemsDialog = null
                viewModel.cancelPending()
            }
        )
    }
    showMessageDialog?.let { d ->
        MessageDialog(
            title = d.title,
            subTitle = d.subTitle,
            message = d.message,
            status = d.status,
            onDismiss = {
                showMessageDialog = null
                viewModel.cancelPending()
            },
            okButtonName = d.positivText ?: "Ok",
            onConfirmAction = if (d.showButton) {
                {
                    showMessageDialog = null
                    if (d.filterLogic) {
                        closeMapsDialogAnimated()
                        viewModel.performPendingK()
                    } else
                        viewModel.performPending()
                }
            } else null,
            onCancelAction = if (d.status == DialogStatus.NORMAL) {
                {
                    showMessageDialog = null
                    if (d.filterLogic) {
                        closeMapsDialogAnimated()
                        viewModel.cancelPendingK()
                    } else
                        viewModel.cancelPending()
                }
            } else null,
            onCloseClick = {

            }
        )
    }

    return remember { ContextMenuController(open, close) }
}

@Composable
fun ContextMenuDialog(
    visible: Boolean,
    wpDataDB: WpDataDB,
    actions: List<ContextMenuAction>,
    onDismiss: () -> Unit,
    onActionClick: (ContextMenuResult) -> Unit
) {
    if (!visible) return

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(screenWidth * 0.9f)
                .background(Color.White, shape = RoundedCornerShape(6.dp))
                .width(IntrinsicSize.Max)
        ) {
            // Header
            val isMultiClient = actions.contains(ContextMenuAction.ConfirmAllAcceptOneTime)
                    || actions.contains(ContextMenuAction.ConfirmAllAcceptInfinite)

            val hasOneTime = ContextMenuAction.ConfirmAllAcceptOneTime in actions
            val hasInfinite = ContextMenuAction.ConfirmAllAcceptInfinite in actions

            val clients: List<String> = when {
                hasOneTime ->
                    WPDataAdditionalFactory.getUniqueClientIdsForAddr_TXT(
                        wpDataDB.addr_id,
                        wpDataDB.dt
                    )

                hasInfinite ->
                    WPDataAdditionalFactory.getUniqueClientIdsForAddr_TXT(
                        wpDataDB.addr_id
                    )

                else -> emptyList()
            }

            Column(
                modifier = Modifier
                    .background(
                        Color(0xFFB1B1B1),
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                    )
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (!isMultiClient) "Виберіть дію для клієнта:" else "Виберіть дію для клієнтів:",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                )
                Text(
                    text = if (!isMultiClient) wpDataDB.client_txt else clients.toString()
                        .removeSurrounding("[", "]"),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "за адресою: ${wpDataDB.addr_txt}",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                )
            }

            Divider(color = Color.Gray, thickness = 1.dp)

            actions.forEach { action ->
                val iconRes = action.iconResOrNull()
                val textColor =
                    if (iconRes == null)
                        Color.Black.copy(alpha = 0.55f) // светло-серый
                    else
                        Color.Black.copy(alpha = 0.95f)

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onActionClick(ContextMenuResult(action, wpDataDB))
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (iconRes != null) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = action.title,
                                modifier = Modifier.size(20.dp),
                                tint = Color.Black.copy(alpha = 0.95f)
                            )
                        } else {
                            Spacer(Modifier.size(20.dp)) // чтобы текст всех строк был на одном уровне
                        }

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = if (!isMultiClient) action.title else String.format(
                                action.title,
                                "${clients.size} "
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Divider(color = Color.Gray, thickness = 1.dp)
                }
            }

        }
    }

}


private fun optionDownload(codeDad2: Long, datePremiumDownloadFormat: String, click: clickVoid) {
    // Опції
    val optionsExchange =
        OptionsExchange(datePremiumDownloadFormat, datePremiumDownloadFormat, codeDad2.toString())
    optionsExchange.downloadOptions(object : ExchangeResponseInterface {
        override fun <T> onSuccess(data: List<T>) {
            if (data.isNotEmpty()) {
                RealmManager.saveDownloadedOptions(data as List<OptionsDB>)
            }
            click.click()
        }

        override fun onFailure(error: String) {
            click.click()
        }
    })
}

private fun reportDownload(codeDad2: Long, datePremiumDownloadFormat: String, click: clickVoid) {

    val reportPrepareExchange = ReportPrepareExchange(
        datePremiumDownloadFormat,
        datePremiumDownloadFormat,
        codeDad2.toString()
    )
    reportPrepareExchange.downloadReportPrepare(object : ExchangeResponseInterface {
        override fun <T> onSuccess(data: List<T>) {
            if (data.isNotEmpty()) {
                ReportPrepareRealm.setAll(data as List<ReportPrepareDB>)
            }

            click.click()
        }

        override fun onFailure(error: String) {
            click.click()
        }
    })
}