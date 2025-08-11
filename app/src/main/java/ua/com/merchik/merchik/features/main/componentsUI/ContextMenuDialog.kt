package ua.com.merchik.merchik.features.main.componentsUI

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import org.json.JSONObject
import ua.com.merchik.merchik.dataLayer.model.DataItemUI



data class ContextMenuState(
    val item: DataItemUI,
    val actions: List<ContextMenuAction>
)


data class ContextMenuResult(
    val action: ContextMenuAction,
    val clientTxt: String?,
    val addrTxt: String?,
    val codeDad2: Long?,
    val id: Long?
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
    data object ConfirmAcceptInfinite : ContextMenuAction("Выполнять регулярно")
    data object Close : ContextMenuAction("Закрыть")
}


@Composable
fun ContextMenuDialog(
    visible: Boolean,
    item: DataItemUI?,
    actions: List<ContextMenuAction>,
    onDismiss: () -> Unit,
    onActionClick: (ContextMenuResult) -> Unit
) {
    if (visible) {
        // Извлечение client_txt и addr_txt из rawObj[0]
        var clientTxt: String? = null
        var addrTxt: String? = null
        var codeDad2: Long? = null
        var id: Long? = null

        try {
            val raw = item?.rawObj?.firstOrNull()
            if (raw != null) {
                val jsonObject = JSONObject(Gson().toJson(raw))
                clientTxt = jsonObject.optString("client_txt").takeIf { it.isNotBlank() }
                addrTxt = jsonObject.optString("addr_txt").takeIf { it.isNotBlank() }
                codeDad2 = jsonObject.optLong("code_dad2").takeIf { it != 0L }
                id = jsonObject.optLong("ID").takeIf { it != 0L }
            }
        } catch (e: Exception) {
            Log.e("ContextMenuDialog", "JSON parse error", e)
        }

        Dialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(6.dp))
                    .width(IntrinsicSize.Max)
            ) {

                // 🔹 Header
                Column(
                    modifier = Modifier

                        .background(
                            Color(0xFFB1B1B1),
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                        )
                ) {
                    Text(
                        text = "Выберите действие для клиента:",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                    )
                    clientTxt?.let {
                        Text(
                            text = it,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 1.dp)
                        )
                    }

                    addrTxt?.let {
                        Text(
                            text = "по адресу: $it",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 1.dp, bottom = 16.dp)
                        )
                    }
                }
                Divider(color = Color.Gray, thickness = 1.dp)

                // Меню
//                val actions = listOf(
//                    ContextMenuAction.AcceptOrder,
//                    ContextMenuAction.AcceptAllAtAddress,
//                    ContextMenuAction.RejectOrder,
//                    ContextMenuAction.RejectAddress,
//                    ContextMenuAction.RejectClient,
//                    ContextMenuAction.RejectByType,
//                    ContextMenuAction.OpenOrder,
//                    ContextMenuAction.AskMoreMoney,
//                    ContextMenuAction.Feedback,
//                    ContextMenuAction.Close
//                )

                actions.forEach { action ->
                    Column {
                        Text(
                            text = action.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onActionClick(
                                        ContextMenuResult(
                                            action = action,
                                            clientTxt = clientTxt,
                                            addrTxt = addrTxt,
                                            codeDad2 = codeDad2,
                                            id = id
                                        )
                                    )
                                }
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                        Divider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            }
        }
    }
}
