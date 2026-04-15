import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.MenuLeading
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus

//package ua.com.merchik.merchik.features.main.componentsUI
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import androidx.annotation.DrawableRes
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.ColumnScope
//import androidx.compose.foundation.layout.IntrinsicSize
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.widthIn
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Divider
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.Stable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.input.pointer.motionEventSpy
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.platform.LocalView
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.compose.LocalLifecycleOwner
//import androidx.lifecycle.repeatOnLifecycle
//import com.google.gson.Gson
//import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity
//import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
//import ua.com.merchik.merchik.Globals
//import ua.com.merchik.merchik.data.RealmModels.WpDataDB
//import ua.com.merchik.merchik.dataLayer.ContextUI
//import ua.com.merchik.merchik.dataLayer.LaunchOrigin
//import ua.com.merchik.merchik.dataLayer.MainEvent
//import ua.com.merchik.merchik.dataLayer.ModeUI
//import ua.com.merchik.merchik.dataLayer.iconResOrNull
//import ua.com.merchik.merchik.dataLayer.model.DataItemUI
//import ua.com.merchik.merchik.database.room.factory.WPDataAdditionalFactory
//import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
//import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
//import ua.com.merchik.merchik.features.main.DBViewModels.SMSPlanSDBViewModel
//import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
//import ua.com.merchik.merchik.features.main.Main.CardItemsUI
//import ua.com.merchik.merchik.features.main.Main.MainViewModel
//import ua.com.merchik.merchik.features.main.Main.launchFeaturesActivity
//import ua.com.merchik.merchik.features.maps.data.mappers.WpSelectionDataHolder
//import java.time.format.DateTimeFormatter
//import java.util.Locale
//import kotlin.collections.map
//
//
//@Stable
//data class ContextMenuController(
//    val open: (wp: WpDataDB, actions: List<ContextMenuAction>) -> Unit,
//    val openState: (ContextMenuState) -> Unit,
//    val close: () -> Unit
//)
//
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

//
//data class ContextMenuState(
//    val wpDataDB: WpDataDB,
//    val item: DataItemUI? = null,
//    val actions: List<ContextMenuAction> = emptyList(),
////    val entries: List<ContextMenuEntry> = emptyList(),
//    val origin: LaunchOrigin? = null
//)
//
sealed interface LongClickContextMenuEntry {
    data class Item(
        val title: String,
        val action: LongClickMenuAction? = null,
        val enabled: Boolean = true,
        val leading: MenuLeading = MenuLeading.None
    ) : LongClickContextMenuEntry

    data object Divider : LongClickContextMenuEntry
}
//
//data class LongClickContextMenuState(
//    val items: List<DataItemUI>,
//    val clickedItem: DataItemUI,
//    val entries: List<LongClickContextMenuEntry>,
//    val origin: LaunchOrigin? = null,
//    val deckId: String? = null,
//    val headerWpDataDB: WpDataDB? = null
//) {
//    val itemUI: DataItemUI get() = clickedItem
//    val isMulti: Boolean get() = items.size > 1
//}
//
sealed class LongClickMenuAction {
    data object Add : LongClickMenuAction()
    data object Edit : LongClickMenuAction()
    data object Delete : LongClickMenuAction()

    data object Mark : LongClickMenuAction()
    data object MarkAll : LongClickMenuAction()
    data object Unmark : LongClickMenuAction()
    data object UnmarkAll : LongClickMenuAction()
    data object Invert : LongClickMenuAction()

//    data object CollapseDeck : LongClickMenuAction()
//    data object ExpandDeck : LongClickMenuAction()

    data object CollapseList : LongClickMenuAction()
    data object ExpandList : LongClickMenuAction()

    data object Accept : LongClickMenuAction()
    data object Reject : LongClickMenuAction()

    data object Close : LongClickMenuAction()

}
//
////sealed interface MenuLeading {
////    data object None : MenuLeading
////
////    /** взять иконку из action.iconResOrNull() */
////    data object ActionIcon : MenuLeading
////
////    data class Checkbox(val checked: Boolean) : MenuLeading
////    data class Text(val value: String) : MenuLeading
////
////    data class DrawableIcon(@DrawableRes val resId: Int) : MenuLeading
////}
//
//data class ContextMenuResult(
//    val action: ContextMenuAction,
//    val wpDataDB: WpDataDB?
//)
//
//
///** Новое меню по long click */
//data class LongClickContextMenuResult(
//    val action: LongClickMenuAction?,
//    val items: List<DataItemUI>,
//    val clickedItem: DataItemUI,
//    val deckId: String? = null
//) {
//    val itemUI: DataItemUI get() = clickedItem
//    val isMulti: Boolean get() = items.size > 1
//}
//
data class MessageDialogData(
    val title: String = "Додатковий заробіток",
    val subTitle: String? = "Базовий мерчендайзинг",
    val message: String,
    val status: DialogStatus,
    val positivText: String? = null,
    val cancelText: String? = null,
    val showButton: Boolean = true,
    val isCancelable: Boolean = true,
    val filterLogic: Boolean = false,
    val onButtonOkClicked: (() -> Unit)? = null,
    val onButtonCancelClicked: (() -> Unit)? = null,
    val onTextLinkClick: ((String) -> Unit)? = null
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
        ContextMenuAction("Змінити")

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
//
//
//@Composable
//fun rememberContextMenuHost(
//    viewModel: MainViewModel,
//    context: Context,
//    closeMapsDialogAnimated: () -> Unit
//): ContextMenuController {
//    var selectedItem by remember { mutableStateOf<ContextMenuState?>(null) }
//    var selectedLongClickItem by remember { mutableStateOf<LongClickContextMenuState?>(null) }
//
//    val focusManager = LocalFocusManager.current
//
//    var showMessageDialog by remember { mutableStateOf<MessageDialogData?>(null) }
//    var showCardItemsDialog by remember { mutableStateOf<CardItemsData?>(null) }
//
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val uiState by viewModel.uiState.collectAsState()
//
//    val formatterDDmmYYYY = DateTimeFormatter
//        .ofPattern("dd MMM yyyy")
//        .withLocale(Locale.getDefault())
//
//    fun openUFMDSelector(wp: WpDataDB, origin: LaunchOrigin?) {
//        viewModel.launcher?.let { launcher ->
//            WpSelectionDataHolder.instance().init()
//
//            launchFeaturesActivity(
//                launcher = launcher,
//                context = context,
//                viewModelClass = WpDataDBViewModel::class,
//                dataJson = Gson().toJson(
//                    mapOf("addressId" to wp.addr_id)
//                ),
//                modeUI = ModeUI.MULTI_SELECT,
//                contextUI = ContextUI.WP_DATA,
//                title = "Додатковий заробіток",
//                origin = origin
//            )
//        }
//    }
//
//    LaunchedEffect(lifecycleOwner) {
//        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
//            viewModel.events.collect { event ->
//                when (event) {
//                    is MainEvent.ShowContextMenu -> {
//                        selectedItem = event.menuState
//                    }
//
//                    is MainEvent.ShowLongClickContextMenu -> {
//                        selectedLongClickItem = event.menuState
//                    }
//
//                    is MainEvent.ShowMessageDialog -> {
//                        Log.d("MainUI", "Received ShowMessageDialog: ${event.data}")
//                        Globals.writeToMLOG(
//                            "INFO",
//                            "MainUI.rememberContextMenuHost",
//                            "Received ShowMessageDialog: ${event.data}"
//                        )
//                        showMessageDialog = event.data
//                    }
//
//                    is MainEvent.ShowCardItemsDialog -> {
//                        showCardItemsDialog = event.cardItemsData
//                    }
//
//                    is MainEvent.ShowLoading -> {
//                        showMessageDialog = MessageDialogData(
//                            subTitle = "Чекаємо на відповідь від сервера",
//                            status = DialogStatus.LOADING,
//                            message = " ",
//                            showButton = false,
//                            isCancelable = false
//                        )
//                    }
//
//                    is MainEvent.LoadingCompleted -> {
//                        showMessageDialog = null
//                    }
//
//                    is MainEvent.LoadingCanceled -> {
//                        showMessageDialog = null
//                    }
//
//                    is MainEvent.JumpToVizitAndCloseMaps -> {
//                        closeMapsDialogAnimated()
//                        viewModel.performPendingK()
//                    }
//
//                    is MainEvent.OpenUFMDWPDataSelector -> {
//                        openUFMDSelector(event.wp, event.origin)
//                    }
//
//                    is MainEvent.HideMessageDialog -> {
//                        showMessageDialog = null
//                    }
//                }
//            }
//        }
//    }
//
//    /** Старое контекстное меню */
//    selectedItem?.let { state ->
//        LegacyContextMenuDialog(
//            visible = true,
//            wpDataDB = state.wpDataDB,
//            actions = state.actions,
//            onDismiss = { selectedItem = null },
//            onActionClick = { result ->
//                focusManager.clearFocus(force = true)
//
//                when (result.action) {
//                    is ContextMenuAction.ShowAllVizitInAdress -> {
//                        val wp = result.wpDataDB ?: run {
//                            selectedItem = null
//                            return@LegacyContextMenuDialog
//                        }
//
//                        val periodText = viewModel.uiState.value.filters?.let { f ->
//                            val range = f.rangeDataByKey
//                            val start = range?.start?.format(formatterDDmmYYYY) ?: "?"
//                            val end = range?.end?.format(formatterDDmmYYYY) ?: "?"
//                            "$start по $end"
//                        } ?: "не визначено"
//
//                        viewModel.requestJumpToAddressVisits(wp, periodText)
//                        selectedItem = null
//                    }
//
//                    is ContextMenuAction.AcceptOrder -> {
//                        selectedItem = result.wpDataDB?.let {
//                            ContextMenuState(
//                                wpDataDB = it,
//                                actions = listOf(
//                                    ContextMenuAction.ConfirmAcceptOneTime,
//                                    ContextMenuAction.ConfirmAcceptInfinite,
//                                    ContextMenuAction.Close
//                                ),
//                                origin = state.origin
//                            )
//                        }
//                    }
//
//                    is ContextMenuAction.AcceptAllAtAddress -> {
//                        selectedItem = result.wpDataDB?.let {
//                            ContextMenuState(
//                                wpDataDB = it,
//                                actions = listOf(
//                                    ContextMenuAction.ConfirmAllAcceptOneTime,
//                                    ContextMenuAction.ConfirmAllAcceptInfinite,
//                                    ContextMenuAction.Close
//                                ),
//                                origin = state.origin
//                            )
//                        }
//                    }
//
//                    is ContextMenuAction.ConfirmAcceptOneTime -> {
//                        result.wpDataDB?.let { viewModel.requestAcceptOneTime(it) }
//                        selectedItem = null
//                    }
//
//                    is ContextMenuAction.ConfirmAcceptInfinite -> {
//                        result.wpDataDB?.let { viewModel.requestAcceptInfinite(it) }
//                        selectedItem = null
//                    }
//
//                    is ContextMenuAction.ConfirmAllAcceptOneTime -> {
//                        result.wpDataDB?.let { viewModel.requestAcceptAllWorkOneTime(it) }
//                        selectedItem = null
//                    }
//
//                    is ContextMenuAction.ConfirmAllAcceptInfinite -> {
//                        result.wpDataDB?.let { viewModel.requestAcceptAllWorkInfinite(it) }
//                        selectedItem = null
//                    }
//
//                    is ContextMenuAction.OpenVisit -> {
//                        result.wpDataDB?.let {
//                            val intent = Intent(context, DetailedReportActivity::class.java)
//                            intent.putExtra("WpDataDB_ID", it.id)
//                            context.startActivity(intent)
//                        }
//                        selectedItem = null
//                    }
//
//                    ContextMenuAction.OpenOrder -> {
//                        result.wpDataDB?.let {
//                            val intent = Intent(context, DetailedReportActivity::class.java)
//                            intent.putExtra("WpDataDB_ID", it.id)
//                            context.startActivity(intent)
//                        }
//                        selectedItem = null
//                    }
//
//                    is ContextMenuAction.OpenSMSPlanDirectory -> {
//                        val intent = Intent(context, FeaturesActivity::class.java)
//                        val bundle = Bundle().apply {
//                            putString("viewModel", SMSPlanSDBViewModel::class.java.canonicalName)
//                            putString("contextUI", ContextUI.SMS_PLAN_DEFAULT.toString())
//                            putString("modeUI", ModeUI.MULTI_SELECT.toString())
//                            putString("title", "Заявки")
//                            putString("subTitle", "## subTitle")
//                        }
//                        intent.putExtras(bundle)
//                        context.startActivity(intent)
//                        selectedItem = null
//                    }
//
//                    is ContextMenuAction.OpenUFMDWPDataSelector -> {
//                        result.wpDataDB?.let { openUFMDSelector(it, state.origin) }
//                        selectedItem = null
//                    }
//
//                    is ContextMenuAction.Close -> {
//                        selectedItem = null
//                    }
//
//                    else -> {
//                        selectedItem = null
//                    }
//                }
//            }
//        )
//    }
//
//    /** Новое контекстное меню по long click */
//    selectedLongClickItem?.let { state ->
//        LongClickContextMenuDialog(
//            visible = true,
//            state = state,
//            onDismiss = { selectedLongClickItem = null },
//            onActionClick = { result ->
//                focusManager.clearFocus(force = true)
//
//                when (result.action) {
//                    LongClickMenuAction.Add -> {
//                        val wp = result.items
//                            .flatMap { it.rawObj }
//                            .firstOrNull { it is WpDataDB } as? WpDataDB
//
//                        wp?.let { openUFMDSelector(it, state.origin) }
//                        selectedLongClickItem = null
//                    }
//
//                    LongClickMenuAction.Edit -> {
//                        (result.items.first().rawObj.first() as WpDataDB)?.let {
//                            val intent = Intent(context, DetailedReportActivity::class.java)
//                            intent.putExtra("WpDataDB_ID", it.id)
//                            context.startActivity(intent)
//                        }
//                        selectedLongClickItem = null
//                    }
//
//                    LongClickMenuAction.Delete -> {
//                        selectedLongClickItem = null
//                    }
//
//                    LongClickMenuAction.Mark -> {
//                        viewModel.updateItemsSelect(
//                            ids = result.items.map { it.stableId },
//                            checked = true
//                        )
//                        selectedLongClickItem = null
//                    }
//
//                    LongClickMenuAction.MarkAll -> {
//                        viewModel.updateItemsSelect(
//                            ids = uiState.items.map { it.stableId },
//                            checked = true
//                        )
//                        selectedLongClickItem = null
//                    }
//
//                    LongClickMenuAction.Unmark -> {
//                        viewModel.updateItemsSelect(
//                            ids = result.items.map { it.stableId },
//                            checked = false
//                        )
//                        selectedLongClickItem = null
//                    }
//
//                    LongClickMenuAction.UnmarkAll -> {
//                        viewModel.updateItemsSelect(
//                            ids = uiState.items.map { it.stableId },
//                            checked = false
//                        )
//                        selectedLongClickItem = null
//                    }
//
//                    LongClickMenuAction.Invert -> {
//                        val resultIds = result.items.map { it.stableId }.toSet()
//
//                        val selectedIds = uiState.items
//                            .filter { it.stableId in resultIds && it.selected }
//                            .map { it.stableId }
//
//                        val unselectedIds = uiState.items
//                            .filter { it.stableId in resultIds && !it.selected }
//                            .map { it.stableId }
//
//                        if (selectedIds.isNotEmpty()) {
//                            viewModel.updateItemsSelect(ids = selectedIds, checked = false)
//                        }
//
//                        if (unselectedIds.isNotEmpty()) {
//                            viewModel.updateItemsSelect(ids = unselectedIds, checked = true)
//                        }
//
//                        selectedLongClickItem = null
//                    }
//
//                    LongClickMenuAction.CollapseList -> {
//                        viewModel.collapseAllDecks()
//                        selectedLongClickItem = null
//                    }
//
//                    LongClickMenuAction.ExpandList -> {
//                        viewModel.expandAllDecks()
//                        selectedLongClickItem = null
//                    }
//
//                    LongClickMenuAction.Accept -> {
//                        selectedLongClickItem = null
//                    }
//
//                    LongClickMenuAction.Reject -> {
//                        selectedLongClickItem = null
//                    }
//
//                    LongClickMenuAction.Close -> {
//                            selectedLongClickItem = null
//                        }
//
//                    null -> {
//                        selectedLongClickItem = null
//                    }
//                }
//            }
//        )
//    }
//
//    val open: (WpDataDB, List<ContextMenuAction>) -> Unit = { wp, actions ->
//        selectedItem = ContextMenuState(
//            wpDataDB = wp,
//            actions = actions
//        )
//    }
//
//    val openState: (ContextMenuState) -> Unit = { state ->
//        selectedItem = state
//    }
//
//    val close: () -> Unit = {
//        selectedItem = null
//        selectedLongClickItem = null
//    }
//
//    showCardItemsDialog?.let {
//        CardItemsUI(
//            title = it.title,
//            item = it.dateItemUI,
//            viewModel = viewModel,
//            onDismiss = {
//                showCardItemsDialog = null
//                viewModel.cancelPending()
//            }
//        )
//    }
//
//    showMessageDialog?.let { d ->
//        MessageDialog(
//            title = d.title,
//            subTitle = d.subTitle,
//            message = d.message,
//            status = d.status,
//            onDismiss = {
//                showMessageDialog = null
//                viewModel.cancelPending()
//            },
//            okButtonName = d.positivText ?: "Ok",
//            cancelButtonName = d.cancelText ?: "",
//            onConfirmAction = if (d.showButton) {
//                {
//                    d.onButtonOkClicked?.invoke()
//                    showMessageDialog = null
//                    if (d.filterLogic) {
//                        closeMapsDialogAnimated()
//                        viewModel.performPendingK()
//                    } else {
//                        viewModel.performPending()
//                    }
//                }
//            } else null,
//            onCancelAction = if (d.status == DialogStatus.NORMAL) {
//                {
//                    showMessageDialog = null
//                    if (d.filterLogic) {
//                        closeMapsDialogAnimated()
//                        viewModel.cancelPendingK()
//                    } else {
//                        viewModel.cancelPending()
//                    }
//                }
//            } else if (d.cancelText != null) {
//                { }
//            } else null,
//            onTextLinkClick = {
//                d.onTextLinkClick?.invoke(it)
//            }
//        )
//    }
//
//    return remember(open, openState, close) {
//        ContextMenuController(
//            open = open,
//            openState = openState,
//            close = close
//        )
//    }
//}
//
//@Composable
//private fun ContextMenuContainer(
//    visible: Boolean,
//    onDismiss: () -> Unit,
//    header: (@Composable () -> Unit)? = null,
//    content: @Composable ColumnScope.() -> Unit
//) {
//    if (!visible) return
//
//    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
//
//    Dialog(onDismissRequest = onDismiss) {
//        Column(
//            modifier = Modifier
//                .widthIn(min = 220.dp, max = screenWidth * 0.9f)
//                .background(Color.White, RoundedCornerShape(8.dp))
//        ) {
//            if (header != null) {
//                header()
//
//                HorizontalDivider(
//                    color = colorResource(R.color.background_item_filter),
//                    thickness = 1.dp
//                )
//            }
//
//            content()
//        }
//    }
//}
//
//@Composable
//fun LongClickContextMenuDialog(
//    visible: Boolean,
//    state: LongClickContextMenuState,
//    onDismiss: () -> Unit,
//    onActionClick: (LongClickContextMenuResult) -> Unit
//) {
//    if (!visible) return
//
//    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
//
//    Dialog(onDismissRequest = onDismiss) {
//        Column(
//            modifier = Modifier
//                .widthIn(min = 220.dp, max = screenWidth * 0.9f)
//                .background(Color.White, RoundedCornerShape(8.dp))
//        ) {
//            state.headerWpDataDB?.let { wpDataDB ->
//                Column(
//                    modifier = Modifier
//                        .background(
//                            color = colorResource(R.color.background_item_filter),
//                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
//                        )
//                        .fillMaxWidth()
//                        .padding(horizontal = 12.dp, vertical = 12.dp)
//                ) {
//                    Row {
//                        Text(
//                            text = "Виберіть дію для клієнта: ",
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.DarkGray,
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                        Text(
//                            text = wpDataDB.client_txt,
//                            fontWeight = FontWeight.SemiBold,
//                            modifier = Modifier.padding(start = 2.dp),
//                            color = Color.Black,
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                    }
//
//                    Spacer(Modifier.height(2.dp))
//
//                    Row {
//                        Text(
//                            text = "За адресою: ",
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.DarkGray,
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                        Text(
//                            text = wpDataDB.addr_txt,
//                            fontWeight = FontWeight.SemiBold,
//                            modifier = Modifier.padding(start = 2.dp),
//                            color = Color.Black,
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                    }
//                }
//
//                HorizontalDivider(
//                    color = colorResource(R.color.background_item_filter),
//                    thickness = 1.dp
//                )
//            }
//
//            state.entries.forEach { entry ->
//                when (entry) {
//                    LongClickContextMenuEntry.Divider -> {
//                        HorizontalDivider(
//                            color = colorResource(R.color.background_item_filter),
//                            thickness = 1.dp
//                        )
//                    }
//
//                    is LongClickContextMenuEntry.Item -> {
//                        LongClickContextMenuRow(
//                            state = state,
//                            item = entry,
//                            onActionClick = onActionClick
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun LongClickContextMenuRow(
//    state: LongClickContextMenuState,
//    item: LongClickContextMenuEntry.Item,
//    onActionClick: (LongClickContextMenuResult) -> Unit
//) {
//    val textColor = if (item.enabled) {
//        Color.Black.copy(alpha = 0.95f)
//    } else {
//        Color.Black.copy(alpha = 0.45f)
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .then(
//                if (item.enabled && item.action != null) {
//                    Modifier.clickable {
//                        onActionClick(
//                            LongClickContextMenuResult(
//                                action = item.action,
//                                items = state.items,
//                                clickedItem = state.clickedItem
//                            )
//                        )
//                    }
//                } else {
//                    Modifier
//                }
//            )
//            .padding(horizontal = 12.dp, vertical = 12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Box(
//            modifier = Modifier.width(20.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            MenuLeadingView(
//                leading = item.leading,
//                tint = textColor
//            )
//        }
//
//        Spacer(Modifier.width(12.dp))
//
//        Text(
//            text = item.title,
//            style = MaterialTheme.typography.bodyLarge,
//            color = textColor,
//            modifier = Modifier.weight(1f)
//        )
//    }
//}
//
//@Composable
//private fun MenuLeadingView(
//    leading: MenuLeading,
//    tint: Color
//) {
//    when (leading) {
//        MenuLeading.None -> {
//            Spacer(Modifier.size(16.dp))
//        }
//
//        is MenuLeading.DrawableIcon -> {
//            Icon(
//                painter = painterResource(id = leading.resId),
//                contentDescription = null,
//                modifier = Modifier.size(16.dp),
//                tint = tint
//            )
//        }
//
//        is MenuLeading.Text -> {
//            Text(
//                text = leading.value,
//                color = tint,
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = FontWeight.SemiBold
//            )
//        }
//
//        is MenuLeading.Checkbox -> {
//            Box(
//                modifier = Modifier
//                    .size(14.dp)
//                    .border(1.dp, tint, RoundedCornerShape(2.dp)),
//                contentAlignment = Alignment.Center
//            ) {
//                if (leading.checked) {
//                    Text(
//                        text = "✓",
//                        color = tint,
//                        fontSize = 10.sp,
//                        lineHeight = 10.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//        }
//
//        else -> {}
//    }
//}
//
//
//@Composable
//fun LegacyContextMenuDialog(
//    visible: Boolean,
//    wpDataDB: WpDataDB,
//    actions: List<ContextMenuAction>,
//    onDismiss: () -> Unit,
//    onActionClick: (ContextMenuResult) -> Unit
//) {
//    val isMultiClient = actions.contains(ContextMenuAction.ConfirmAllAcceptOneTime) ||
//            actions.contains(ContextMenuAction.ConfirmAllAcceptInfinite)
//
//    val hasOneTime = ContextMenuAction.ConfirmAllAcceptOneTime in actions
//    val hasInfinite = ContextMenuAction.ConfirmAllAcceptInfinite in actions
//
//    val clients: List<String> = when {
//        hasOneTime -> WPDataAdditionalFactory.getUniqueClientIdsForAddr_TXT(
//            wpDataDB.addr_id,
//            wpDataDB.dt
//        )
//
//        hasInfinite -> WPDataAdditionalFactory.getUniqueClientIdsForAddr_TXT(
//            wpDataDB.addr_id
//        )
//
//        else -> emptyList()
//    }
//
//    ContextMenuContainer(
//        visible = visible,
//        onDismiss = onDismiss,
//        header = {
//            Column(
//                modifier = Modifier
//                    .background(
//                        color = colorResource(R.color.background_item_filter),
//                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
//                    )
//                    .fillMaxWidth()
//                    .padding(horizontal = 12.dp, vertical = 12.dp)
//            ) {
//                Row {
//                    Text(
//                        text = if (!isMultiClient) {
//                            "Виберіть дію для клієнта: "
//                        } else {
//                            "Виберіть дію для клієнтів: "
//                        },
//                        fontWeight = FontWeight.SemiBold,
//                        color = Color.DarkGray,
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                    Text(
//                        text = if (!isMultiClient) {
//                            wpDataDB.client_txt
//                        } else {
//                            clients.joinToString(", ")
//                        },
//                        fontWeight = FontWeight.SemiBold,
//                        modifier = Modifier.padding(start = 2.dp),
//                        color = Color.Black,
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }
//
//                Spacer(Modifier.height(2.dp))
//
//                Row {
//                    Text(
//                        text = "За адресою: ",
//                        fontWeight = FontWeight.SemiBold,
//                        color = Color.DarkGray,
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                    Text(
//                        text = wpDataDB.addr_txt,
//                        fontWeight = FontWeight.SemiBold,
//                        modifier = Modifier.padding(start = 2.dp),
//                        color = Color.Black,
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }
//            }
//        }
//    ) {
//        actions.forEachIndexed { index, action ->
//            val title = if (!isMultiClient) {
//                action.title
//            } else {
//                String.format(action.title, "${clients.size} ")
//            }
//
//            LegacyContextMenuRow(
//                wpDataDB = wpDataDB,
//                action = action,
//                title = title,
//                onActionClick = onActionClick
//            )
//
//            if (index < actions.lastIndex) {
//                HorizontalDivider(
//                    color = colorResource(R.color.background_item_filter),
//                    thickness = 1.dp
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun LegacyContextMenuRow(
//    wpDataDB: WpDataDB,
//    action: ContextMenuAction,
//    title: String,
//    onActionClick: (ContextMenuResult) -> Unit
//) {
//    val textColor = Color.Black.copy(alpha = 0.95f)
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable {
//                onActionClick(ContextMenuResult(action, wpDataDB))
//            }
//            .padding(horizontal = 12.dp, vertical = 12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Box(
//            modifier = Modifier.width(20.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            MenuLeadingView(
//                leading = action.toLegacyMenuLeading(),
//                tint = textColor
//            )
//        }
//
//        Spacer(Modifier.width(12.dp))
//
//        Text(
//            text = title,
//            style = MaterialTheme.typography.bodyLarge,
//            color = textColor,
//            modifier = Modifier.weight(1f)
//        )
//    }
//}
//
//private fun ContextMenuAction.toLegacyMenuLeading(): MenuLeading =
//    iconResOrNull()?.let { MenuLeading.DrawableIcon(it) } ?: MenuLeading.None
