package ua.com.merchik.merchik.features.main.Main

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers.InlinePriceDrafts
import ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers.PriceSaveGuard
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.features.main.DBViewModels.TovarDBViewModel

private data class InlinePriceEditorBinding(
    val viewModel: TovarDBViewModel,
    val drafts: InlinePriceDrafts,
    val tooltipEnabled: Boolean
)

private val LocalInlinePriceEditor = staticCompositionLocalOf<InlinePriceEditorBinding?> { null }

private tailrec fun Context.priceEditorActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> if (baseContext !== this) baseContext.priceEditorActivity() else null
    else -> null
}

@Composable
internal fun InlinePriceEditorHost(viewModel: MainViewModel, context: Context, content: @Composable () -> Unit) {
    if (viewModel !is TovarDBViewModel || viewModel.contextUI != ContextUI.TOVAR_FROM_TOVAR_TABS) {
        content()
        return
    }
    val drafts = viewModel.getInlinePriceDrafts()
    val owner = LocalLifecycleOwner.current
    val activity = context.priceEditorActivity() as? ComponentActivity
    var active by remember { mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    var dialogVisible by remember { mutableStateOf(false) }
    val focusRequest by drafts.focusRequest.collectAsState()
    LaunchedEffect(focusRequest, active, dialogVisible) {
        if (active && !dialogVisible) focusRequest?.let { viewModel.requestScrollToVisit(it.itemId) }
    }

    DisposableEffect(drafts, owner, activity) {
        val handler = Handler(Looper.getMainLooper())
        var dialog: MessageDialogBuilder? = null
        val showPending = Runnable {
            // Fragment ON_PAUSE also means switching tabs. Only the activity decides
            // whether a message can be shown now or must wait for foregrounding.
            if (activity != null && !activity.isFinishing && !activity.isDestroyed &&
                activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog?.isShowing() != true
            ) {
                val error = drafts.nextError()
                if (error != null) {
                    val prices = error.draft.prices
                    val onDismiss: () -> Unit = {
                        dialogVisible = false
                        drafts.requestNotification()
                    }
                    val builder = if (error.draft.invalidPrice) {
                        PriceSaveGuard.priceErrorDialog(activity, prices.product, prices.price, prices.priceMin)
                    } else if (error.draft.invalidDiscount) {
                        PriceSaveGuard.discountDialog(activity, prices.product, prices.discountCheck(),
                            { drafts.approveDiscount(error) }, { drafts.refuseDiscount(error) }, onDismiss)
                    } else {
                        MessageDialogBuilder(activity)
                            .setTitle("Изменения не сохранены")
                            .setStatus(DialogStatus.ERROR)
                            .setMessage(if (error.draft.value.isBlank()) "Введите корректное значение цены."
                                else "Не удалось сохранить цену. Обновите список товаров и повторите попытку. Черновик сохранён.")
                            .setOnConfirmAction("Закрыть") { }
                    }
                    drafts.markShown(error)
                    dialog = builder
                    dialogVisible = true
                    if (!error.draft.invalidDiscount) builder.setOnDismissListener(onDismiss)
                    builder.show()
                }
            }
        }
        fun scheduleNotification() {
            handler.removeCallbacks(showPending)
            // Focus loss can arrive just before ON_PAUSE when the app is minimized.
            handler.postDelayed(showPending, 150)
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    active = false
                    drafts.finishAll()
                    scheduleNotification()
                }
                Lifecycle.Event.ON_RESUME -> {
                    active = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
                    drafts.revalidate()
                    scheduleNotification()
                }
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        if (activity !== owner) activity?.lifecycle?.addObserver(observer)
        val notifications = activity?.lifecycleScope?.launch {
            drafts.notificationVersion.collect { scheduleNotification() }
        }
        onDispose {
            drafts.finishAll()
            notifications?.cancel()
            owner.lifecycle.removeObserver(observer)
            if (activity !== owner) activity?.lifecycle?.removeObserver(observer)
            scheduleNotification()
        }
    }
    CompositionLocalProvider(LocalInlinePriceEditor provides InlinePriceEditorBinding(viewModel, drafts, active && !dialogVisible)) {
        content()
    }
}

@Composable
internal fun ProductPriceEditorRow(itemId: Long, row: ProductCodeEditorRowUi, onValueChange: (String) -> Unit) {
    val binding = LocalInlinePriceEditor.current
    val field = remember(binding?.drafts, itemId, row.rowId) { binding?.viewModel?.inlinePriceField(itemId, row) }
    if (binding == null || field == null) {
        DecimalNumberEditorRow(row.title, row.value, onValueChange)
        return
    }
    val drafts by binding.drafts.drafts.collectAsState()
    val draft = drafts[field]
    val focusRequest by binding.drafts.focusRequest.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(focusRequest, binding.tooltipEnabled) {
        if (focusRequest == field && binding.tooltipEnabled) {
            focusRequester.requestFocus()
            keyboard?.show()
            binding.drafts.focusRestored(field)
        }
    }
    DisposableEffect(binding.drafts, field) {
        onDispose { if (focused) binding.drafts.finish(field) }
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(row.title, Modifier.weight(1f), maxLines = 3)
        DecimalNumberInput(
            value = draft?.value ?: row.value,
            onValueChange = {
                binding.viewModel.onInlinePriceInput(field)
                binding.drafts.change(field, it)
            },
            controlled = true,
            modifier = Modifier.focusRequester(focusRequester),
            errorMessage = if (draft?.invalidPrice == true) {
                if (field.beforePromotion) {
                    "АКЦИОННАЯ цена (${draft.prices.price} грн) не может быть выше чем цена ДО начала акции"
                } else {
                    "АКЦИОННАЯ цена не может быть выше чем цена ДО начала акции (${draft.prices.priceMin} грн)"
                }
            } else if (draft?.invalidDiscount == true) {
                "Проверьте цену ДО начала акции и текущую"
            } else null,
            tooltipEnabled = binding.tooltipEnabled,
            onFocusChange = { hasFocus ->
                if (focused && !hasFocus) binding.drafts.finish(field)
                focused = hasFocus
            },
            onDone = {
                binding.drafts.finish(field)
                focusManager.clearFocus()
                keyboard?.hide()
            }
        )
    }
}
