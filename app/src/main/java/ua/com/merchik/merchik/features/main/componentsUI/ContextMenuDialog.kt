package ua.com.merchik.merchik.features.main.componentsUI

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.iconResOrNull
import ua.com.merchik.merchik.database.room.factory.WPDataAdditionalFactory
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus


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
