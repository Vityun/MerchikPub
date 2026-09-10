package ua.com.merchik.merchik.features.main.Main

import android.app.Activity
import android.text.Spanned
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import ua.com.merchik.merchik.R
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.componentsUI.RoundCheckbox
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.toAnnotatedString
import ua.com.merchik.merchik.dialogs.features.indicator.LineSpinFadeLoaderIndicator
import ua.com.merchik.merchik.features.main.DBViewModels.OptionsDBViewModel
import ua.com.merchik.merchik.features.main.options.OptionItemState

/** Dedicated option rows inside the shared MainUI list/filter/grouping shell. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OptionsItemsUI(
    modifier: Modifier,
    viewModel: OptionsDBViewModel,
    dataItems: List<DataItemUI>,
    groups: List<GroupMeta>,
    listState: LazyListState
) {
    val rows by viewModel.optionRows.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val loading by viewModel.optionsLoading.collectAsState()
    val error by viewModel.optionsError.collectAsState()
    val scrollRequest by viewModel.optionScroll.collectAsState()
    val hostView = LocalView.current
    val context = LocalContext.current
    val activity = context as? Activity
    val rowsById = remember(rows) { rows.associateBy { it.id } }
    val visibleRows = remember(dataItems, rowsById) {
        dataItems.mapNotNull { item -> rowsById[(item.rawObj.firstOrNull() as? OptionsDB)?.getID()] }
    }
    var handledScroll by rememberSaveable { mutableStateOf(0L) }
    var highlightedId by remember { mutableStateOf<String?>(null) }
    var highlightSequence by remember { mutableStateOf(0L) }

    LaunchedEffect(scrollRequest, visibleRows.map { it.id }, groups) {
        val request = scrollRequest ?: return@LaunchedEffect
        if (handledScroll == request.sequence) return@LaunchedEffect
        val index = visibleRows.indexOfFirst { it.id == request.id }.takeIf { it >= 0 }
            ?: visibleRows.indexOfFirst { it.controlId == request.optionId }.takeIf { it >= 0 }
            ?: visibleRows.indexOfFirst { it.optionIdValue == request.optionId }.takeIf { it >= 0 }
            ?: return@LaunchedEffect
        val groupIndex = groups.indexOfFirst { index in it.startIndex until it.endIndexExclusive }
        listState.animateScrollToItem(if (groupIndex >= 0) groupIndex else index)
        highlightedId = visibleRows[index].id
        highlightSequence = request.sequence
        handledScroll = request.sequence
    }

    LaunchedEffect(error) {
        val message = error ?: return@LaunchedEffect
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            MessageDialogBuilder(activity)
                .setTitle("Відсутні дані щодо цього відвідування")
                .setStatus(DialogStatus.ERROR)
                .setSubTitle(message)
                .setMessage("На даний момент немає даних для відображення. Можливо вони ще не завантаженi з боку сервера. Зачекайте завершення обміну даними з сервером. Якщо завантаження не відбулося, знайдіть місце з кращим інтернет-з'єднанням, натисніть 'Синхронізація' і дочекайтеся завершення процесу. Якщо це не допомогло, зверніться до керівника.")
                .setOnConfirmAction { }
                .show()
            viewModel.clearOptionsError()
        }
    }

    val itemContent: @Composable (DataItemUI) -> Unit = { item ->
        val row = rowsById[(item.rawObj.firstOrNull() as? OptionsDB)?.getID()]
        if (row != null) {
            val bringIntoView = remember { BringIntoViewRequester() }
            LaunchedEffect(highlightedId, highlightSequence) {
                if (highlightedId == row.id && highlightSequence > 0) {
                    // Let the existing deck expansion finish before revealing its inner row.
                    if (groups.isNotEmpty()) delay(1600)
                    bringIntoView.bringIntoView()
                }
            }
            Box(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                .bringIntoViewRequester(bringIntoView)) {
                OptionItemUI(
                    row = row,
                    enabled = !loading,
                    selected = item.selected,
                    showSelection = viewModel.modeUI == ModeUI.FILTER_SELECT ||
                        viewModel.modeUI == ModeUI.MULTI_SELECT || viewModel.modeUI == ModeUI.ONE_SELECT,
                    onCheckedChange = { if (!loading) viewModel.updateItemSelect(it, item) },
                    pulseSequence = if (highlightedId == row.id) highlightSequence else 0L,
                    onClick = { target -> viewModel.onOptionClick(row.id, target, hostView) },
                    onLongClick = { viewModel.onOptionLongClick(row.id, hostView) }
                )
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            if (groups.isEmpty()) {
                items(dataItems, key = { it.stableId }) { itemContent(it) }
            } else {
                items(groups, key = { "${it.groupKey}_${it.startIndex}" }) { group ->
                    GroupDeck(
                        groupMeta = group,
                        items = dataItems.subList(group.startIndex, group.endIndexExclusive),
                        visibilityColumName = if (uiState.settingsItems.any { it.key == "column_name" && it.isEnabled }) View.VISIBLE else View.GONE,
                        settingsItems = uiState.settingsItems,
                        viewModel = viewModel,
                        context = context,
                        groupingFields = uiState.groupingFields,
                        level = 0,
                        itemContent = itemContent
                    )
                }
            }
        }
        if (loading) {
            LineSpinFadeLoaderIndicator(
                color = Color.Gray,
                modifier = Modifier.align(if (rows.isEmpty()) Alignment.Center else Alignment.TopCenter)
                    .size(36.dp), radius = 12f, elementHeight = 5f, penThickness = 3f
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OptionItemUI(
    row: OptionItemState,
    enabled: Boolean,
    selected: Boolean,
    showSelection: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    pulseSequence: Long,
    onClick: (OptionsDBViewModel.OptionClickTarget) -> Unit,
    onLongClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pulseSequence) {
        scale.snapTo(1f)
        if (pulseSequence > 0) repeat(16) {
            scale.animateTo(1.025f, tween(425))
            scale.animateTo(1f, tween(425))
        }
    }
    val inactive = row.backgroundRes == R.drawable.button_bg_inactive
    val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier.fillMaxWidth().graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .shadow(2.dp, shape, clip = false)
            .clip(shape).background(if (inactive) Color(0xFFDBDBDB) else Color(0xFFAAAAAA))
            .combinedClickable(enabled = enabled,
                onClick = { onClick(OptionsDBViewModel.OptionClickTarget.ROW) },
                onLongClick = onLongClick)
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 66.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showSelection) {
                RoundCheckbox(aroundColor = Color.Transparent, checked = selected,
                    onCheckedChange = onCheckedChange)
            }
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OptionText(row.title, Modifier, null, 0.dp,
                    textStyle = TextStyle(fontWeight = FontWeight.SemiBold))
                OptionText(row.optionId, Modifier, null, 0.dp)
            }
            Column(Modifier.padding(start = 8.dp, end = 8.dp).widthIn(max = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                OptionText(row.counter, Modifier, if (enabled && row.counter.onClick != null) {
                    { onClick(OptionsDBViewModel.OptionClickTarget.COUNTER) }
                } else null)
                OptionText(row.secondaryCounter, Modifier, if (enabled && row.secondaryCounter.onClick != null) {
                    { onClick(OptionsDBViewModel.OptionClickTarget.SECONDARY_COUNTER) }
                } else null)
            }
            Image(
                painter = painterResource(row.signal.iconRes),
                contentDescription = "Перевірити статус: ${row.title.text}",
                colorFilter = ColorFilter.tint(Color(row.signal.tint)),
                modifier = Modifier.padding(vertical = 8.dp).size(50.dp)
                    .alpha(if (row.signal.visibility == View.VISIBLE) 1f else 0f)
                    .clickable(enabled = enabled && row.signal.visibility == View.VISIBLE) {
                        onClick(OptionsDBViewModel.OptionClickTarget.SIGNAL)
                    }
            )
        }
    }
}

@Composable
fun OptionsReportButton(viewModel: OptionsDBViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.reportButton.collectAsState()
    val loading by viewModel.optionsLoading.collectAsState()
    val shape = RoundedCornerShape(8.dp)
    val buttonColor = colorResource(R.color.blue)
    Button(
        onClick = viewModel::conductReport,
        enabled = !loading && state != null,
        modifier = modifier.fillMaxWidth().shadow(4.dp, shape),
        shape = shape,
        contentPadding = PaddingValues(horizontal = 8.dp),
        // Recalculation blocks repeat submits without fading the button into the background.
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color.White,
            disabledContainerColor = buttonColor,
            disabledContentColor = Color.White
        )
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 66.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = viewModel.getTranslateString(stringResource(R.string.complete_work)),
                modifier = Modifier.weight(1f).padding(vertical = 16.dp,
                    horizontal = 16.dp),
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp).widthIn(max = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("План: ${state?.plan ?: "0"} грн", fontSize = 13.sp, lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("Факт: ${state?.fact ?: "0"} грн", fontSize = 13.sp, lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                state?.let {
                    Image(painterResource(it.iconRes), contentDescription = null,
                        modifier = Modifier.size(24.dp), colorFilter = ColorFilter.tint(colorResource(it.tintRes)))
                }
            }
        }
    }
}

@Composable
private fun OptionText(
    part: OptionItemState.TextPart,
    modifier: Modifier,
    onClick: (() -> Unit)?,
    padding: Dp = 8.dp,
    textStyle: TextStyle? = null
) {
    if (part.visibility == View.GONE) return
    val annotated = remember(part.text) {
        (part.text as? Spanned)?.toAnnotatedString() ?: AnnotatedString(part.text.toString())
    }
    Text(
        text = annotated,
        color = Color(0xFF424242),
        fontSize = 14.sp,
        lineHeight = 18.sp,
        style = textStyle ?: TextStyle.Default,
        fontWeight = textStyle?.fontWeight ?: if (part.bold) FontWeight.Bold else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.alpha(if (part.visibility == View.VISIBLE) 1f else 0f)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding)
    )
}
