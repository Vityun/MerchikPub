package ua.com.merchik.merchik.features.main.componentsUI

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.iconResOrNull
import ua.com.merchik.merchik.database.room.factory.WPDataAdditionalFactory
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.DBViewModels.SMSPlanSDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainEvent
import ua.com.merchik.merchik.features.main.Main.MainViewModel


@Stable
data class ContextMenuController(
    val open: (wp: WpDataDB, actions: List<ContextMenuAction>) -> Unit,
    val close: () -> Unit
)


data class ContextMenuState(
    val actions: List<ContextMenuAction>,
    val wpDataDB: WpDataDB               // держим выбранный объект тут
)


data class ContextMenuResult(
    val action: ContextMenuAction,
    val wpDataDB: WpDataDB?
)

data class MessageDialogData(
    val title: String = "Дополнительный заработок",
    val subTitle: String? = "Базовий мерчендайзинг",
    val message: String,
    val status: DialogStatus,
    val positivText: String? = null
)

sealed class ContextMenuAction(val title: String) {
    data object AcceptOrder : ContextMenuAction("Принять этот заказ")
    data object AcceptAllAtAddress : ContextMenuAction("Принять всех клиентов этого адреса")
    data object RejectOrder : ContextMenuAction("Отказаться от этого заказа")
    data object RejectAddress : ContextMenuAction("Отказаться от этого адреса")
    data object RejectClient : ContextMenuAction("Отказаться от этого клиента")
    data object RejectByType : ContextMenuAction("Отказаться от такого типа работ")
    data object OpenVisit : ContextMenuAction("Открыть посещение")
    data object OpenOrder : ContextMenuAction("Информация об этом заказе")
    data object AskMoreMoney : ContextMenuAction("Попросить больше денег")
    data object Feedback : ContextMenuAction("Обратная связь")
    data object ConfirmAcceptOneTime : ContextMenuAction("Выполнить один раз")
    data object ConfirmAcceptInfinite : ContextMenuAction( "Выполнять регулярно")
    data object ConfirmAllAcceptOneTime : ContextMenuAction("Выполнить один раз все работы по %sклиента этому адресу")
    data object ConfirmAllAcceptInfinite : ContextMenuAction( "Выполнять регулярно все работы по %sклиента этому адресу")
    data object OpenSMSPlanDirectory : ContextMenuAction("Просмотреть заявки")
    data object Close : ContextMenuAction("Закрыть")
}



/** Хост: держит состояние и рисует диалог. Возвращает контроллер с open/close. */
@Composable
fun rememberContextMenuHost(
    viewModel: MainViewModel,
    context: Context
): ContextMenuController {
    var selectedItem by remember { mutableStateOf<ContextMenuState?>(null) }
    val focusManager = LocalFocusManager.current

    var showMessageDialog by remember { mutableStateOf<MessageDialogData?>(null) }

    // Подписка на события (как было)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MainEvent.ShowContextMenu -> selectedItem = event.menuState
                is MainEvent.ShowMessageDialog -> { /* твоя логика показ диалога */
                    showMessageDialog = event.data
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
                    is ContextMenuAction.OpenVisit,
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
            onConfirmAction = {
                showMessageDialog = null
                viewModel.performPending()
            },
            onCancelAction = if (d.status == DialogStatus.NORMAL) {
                {
                    showMessageDialog = null
                    viewModel.cancelPending()
                }
            } else null
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

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
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
                modifier = Modifier.background(
                    Color(0xFFB1B1B1),
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (!isMultiClient) "Выберите действие для клиента:" else "Выберите действие для клиентов:",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                )
                Text(
                    text = if (!isMultiClient) wpDataDB.client_txt else clients.toString().removeSurrounding("[", "]"),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "по адресу: ${wpDataDB.addr_txt}",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                )
            }

            Divider(color = Color.Gray, thickness = 1.dp)

            actions.forEach { action ->
                val iconRes = action.iconResOrNull()
                val textColor =
                    if (iconRes == null)
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.45f) // светло-серый
                    else
                        MaterialTheme.colorScheme.surface

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
                                tint = MaterialTheme.colorScheme.surface
                            )
                        } else {
                            Spacer(Modifier.size(20.dp)) // чтобы текст всех строк был на одном уровне
                        }

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = if (!isMultiClient) action.title else String.format(action.title,"${clients.size} "),
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
