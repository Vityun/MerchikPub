package ua.com.merchik.merchik.features.main.Main

import android.content.Context
import android.util.Log
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.dataLayer.model.TextField
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt


@Composable
fun ItemRowCard(
    item: DataItemUI,
    uiState: StateUI,
    viewModel: MainViewModel,
    context: Context,
    visibilityColumName: Int,
    productCodeEditorState: ProductCodeEditorState
) {
    val rows = productCodeEditorState.rowsByItemId[item.stableId].orEmpty()
    val expanded = productCodeEditorState.expanded && rows.isNotEmpty()

    Box(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 12.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
    ) {
        ItemUI(
            item = item,
            visibilityColumName = visibilityColumName,
            settingsItemUI = uiState.settingsItems,
            contextUI = viewModel.modeUI,
            onClickItem = { viewModel.onClickItem(it, context) },
            onLongClickItem = { viewModel.onLongClickItem(it, context) },
            onClickItemImage = { viewModel.onClickItemImage(it, context) },
            onMultipleClickItemImage = { dataItem, index ->
                viewModel.onClickItemImage(dataItem, context, index)
            },
            onCheckItem = { checked, it ->
                viewModel.updateItemSelect(checked, it)
            },
            onClickProductCode = { clickedItem, fieldValue, action ->
                viewModel.onClickProductCode(
                    itemUI = clickedItem,
                    fieldValue = fieldValue,
                    action = action,
                    context = context
                )
            },
            onLongClickProductCode = { clickedItem, fieldValue, action ->
                viewModel.onLongClickProductCode(
                    itemUI = clickedItem,
                    fieldValue = fieldValue,
                    action = action,
                    context = context
                )
            },
            productCodeExpanded = expanded,
            productCodeRows = rows,
            onProductCodeMinus = { rowId ->
                viewModel.decreaseProductCodeValue(item.stableId, rowId)
            },
            onProductCodePlus = { rowId ->
                viewModel.increaseProductCodeValue(item.stableId, rowId)
            },
            onProductCodeValueChange = { rowId, value ->
                viewModel.updateProductCodeValue(item.stableId, rowId, value)
            },
            onProductCodeValue2Change = { rowId, value ->
                viewModel.updateProductCodeSecondValue(item.stableId, rowId, value)
            },
            onProductCodeTakePhoto = { clickedItem ->
                viewModel.onProductCodeTakePhoto(clickedItem, context)
            },
            onClickImageComment = { clickedItem, fieldValue ->
                viewModel.onClickImageComment(
                    itemUI = clickedItem,
                    fieldValue = fieldValue,
                    context = context
                )
            }
        )
    }
}


@Composable
fun GroupDeck(
    groupMeta: GroupMeta?,
    items: List<DataItemUI>,
    visibilityColumName: Int,
    settingsItems: List<SettingsItemUI>,
    viewModel: MainViewModel,
    context: Context,
    groupingFields: List<GroupingField>,
    level: Int,
    maxStackSize: Int = 5,
    stackOffset: Dp = 4.dp
) {

    val allSelected = remember(items) { items.isNotEmpty() && items.all { it.selected } }
    val uiState by viewModel.uiState.collectAsState()
    val productCodeEditorState by viewModel.productCodeEditorState.collectAsState()

    // stable key группы, чтобы rememberSaveable держал состояние именно этой группы
    val groupId = remember(groupMeta?.groupKey, level) {
        // groupKey лучше, чем title. Если groupMeta null — хотя бы уровень.
        "${level}:${groupMeta?.groupKey ?: "noKey"}"
    }

    // collapsedByDefault=true => свернуто => expanded=false
    val defaultExpanded = remember(groupingFields, level) {
        val collapsed = groupingFields.getOrNull(level)?.collapsedByDefault ?: true
        !collapsed
    }

    // стартовое состояние теперь учитывает collapsedByDefault при первой отрисовке
    var expanded by rememberSaveable(groupId) { mutableStateOf(defaultExpanded) }

    val deckExpandCommand = uiState.deckExpandCommand

    LaunchedEffect(deckExpandCommand?.version) {
        deckExpandCommand?.let { command ->
            expanded = command.expand
        }
    }

    if (items.isEmpty()) return

    val hasGroupTitle = groupMeta?.title?.isNotBlank() == true
    val isGroupHeaderEnabled = settingsItems.isGroupHeaderEnabled()

    val showGroupHeader = hasGroupTitle && isGroupHeaderEnabled

    val keepTopCardVisibleWhenExpanded = hasGroupTitle && !showGroupHeader

    LaunchedEffect(
        groupMeta?.groupKey,
        groupMeta?.title,
        level,
        expanded,
        hasGroupTitle,
        isGroupHeaderEnabled,
        showGroupHeader,
        keepTopCardVisibleWhenExpanded,
        settingsItems
    ) {
        Log.e(
            GROUP_DECK_DEBUG,
            """
        GroupDeck compose:
        level=$level
        groupKey=${groupMeta?.groupKey}
        groupTitle=${groupMeta?.title}
        expanded=$expanded

        hasGroupTitle=$hasGroupTitle
        isGroupHeaderEnabled=$isGroupHeaderEnabled
        showGroupHeader=$showGroupHeader
        keepTopCardVisibleWhenExpanded=$keepTopCardVisibleWhenExpanded

        enabledSettings=${settingsItems.enabledKeysDebug()}
        allSettings=${settingsItems.allKeysDebug()}
        """.trimIndent()
        )
    }

    val currentGroupingKey = remember(groupingFields, level) {
        groupingFields.getOrNull(level)?.key?.takeIf { it.isNotBlank() }
    }
    val forcedGroupTitleKey = remember(
        showGroupHeader,
        currentGroupingKey,
        groupMeta?.title
    ) {
        if (!showGroupHeader && !currentGroupingKey.isNullOrBlank() && !groupMeta?.title.isNullOrBlank()) {
            currentGroupingKey
        } else {
            null
        }
    }

    val stackSize = min(maxStackSize, items.size)
    val hasDeck = items.size > 1
    val count = items.size.coerceAtMost(4)

    val forceColumnNameForDeck = hasDeck && !expanded && forcedGroupTitleKey == null

    val topItemRaw = remember(
        items,
        forceColumnNameForDeck,
        forcedGroupTitleKey,
        groupMeta?.title
    ) {
        buildGroupSummaryItem(
            groupItems = items,
            forceColumnName = forceColumnNameForDeck,
            forcedVisibleKeys = forcedGroupTitleKey?.let { setOf(it) }.orEmpty(),
            forcedDisplayValues = if (forcedGroupTitleKey != null) {
                mapOf(forcedGroupTitleKey to groupMeta?.title.orEmpty())
            } else {
                emptyMap()
            }
        )
    }
    // 👇 сводная карточка БЕЗ кастомных UI-модификаторов
    val topItem = remember(topItemRaw, level, allSelected) {
        topItemRaw.copy(selected = allSelected)
            .witBackgroundUiModifiers(level)
    }

    val shadow: Shadow = Shadow(
        radius = 1.dp,
        spread = 1.dp,
        color = Color(0x401F1F1F),
        offset = DpOffset(x = 2.dp, y = 3.dp),
    )

    // небольшой подъём верхней карточки при раскрытии
    val topCardOffsetY by animateDpAsState(
        targetValue = if (expanded && hasDeck && keepTopCardVisibleWhenExpanded) (-6).dp else 0.dp,
        // ↳ смещаем вверх только в режиме, когда карта остаётся при expanded
        animationSpec = tween(250),
        label = "topCardOffset"
    )

    val cardBottomPadding by animateDpAsState(
        targetValue = if (expanded && hasDeck && keepTopCardVisibleWhenExpanded) 8.dp else (2 + count * 5).dp,
        animationSpec = tween(250),
        label = "cardBottomPadding"
    )

    val hasHiddenSelected = remember(items, stackSize) {
        items.drop(stackSize).any { it.selected }
    }

    val forcedTopCardKeys = remember(
        hasDeck,
        expanded,
        forcedGroupTitleKey
    ) {
        buildSet {
            if (hasDeck && !expanded) add("column_name")
            forcedGroupTitleKey?.let { add(it) }
        }
    }

    val topCardSettings = settingsItems.withForcedKeys(forcedTopCardKeys)

    val topCardVisibilityColumName = remember(
        visibilityColumName,
        forceColumnNameForDeck
    ) {
        if (forceColumnNameForDeck) {
            View.VISIBLE
        } else {
            visibilityColumName
        }
    }
    LaunchedEffect(
        groupMeta?.groupKey,
        expanded,
        hasDeck,
        forceColumnNameForDeck,
        forcedGroupTitleKey,
        forcedTopCardKeys,
        topCardVisibilityColumName
    ) {
        Log.e(
            GROUP_DECK_DEBUG,
            """
        GroupDeck top-card:
        level=$level
        groupKey=${groupMeta?.groupKey}
        hasDeck=$hasDeck
        expanded=$expanded
        forceColumnNameForDeck=$forceColumnNameForDeck
        forcedGroupTitleKey=$forcedGroupTitleKey
        forcedTopCardKeys=${forcedTopCardKeys.joinToString(", ").ifBlank { "<none>" }}
        topCardVisibilityColumName=$topCardVisibilityColumName
        """.trimIndent()
        )
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = stackOffset,
                end = stackOffset
            )
    ) {

        // --- Заголовок группы ---
        if (showGroupHeader) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (10 + 10 * level).dp, end = 10.dp)
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val groupTitle = if (level > 0) {
                    groupMeta!!.title.orEmpty()
                } else {
                    groupMeta!!.title.orEmpty()
                }

                Text(
                    text = "$groupTitle (всего ${items.size} элементов)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 1.dp, end = 8.dp)
                )
                Image(
                    painter =
                        if (expanded) painterResource(R.drawable.ic_arrow_up_1)
                        else painterResource(R.drawable.ic_arrow_down_1),
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 7.dp)
                        .align(Alignment.CenterVertically),
                    contentScale = ContentScale.Inside,
                    contentDescription = null
                )
            }
        }

        // --------- ВЕСЬ КОНТЕНТ ГРУППЫ (колода + список) ---------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .padding(top = 1.dp, bottom = 3.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                // ============= 1. СВЁРНУТАЯ КОЛОДА / ОДНА КАРТА =============
                // ЛОГИКА:
                //  - если НЕТ заголовка -> как раньше: карта/колода есть и при expanded = true
                //  - если ЕСТЬ заголовок -> карта/колода только когда expanded = false
                val showTopCardBlock = !expanded || keepTopCardVisibleWhenExpanded

                LaunchedEffect(
                    groupMeta?.groupKey,
                    expanded,
                    keepTopCardVisibleWhenExpanded,
                    showTopCardBlock
                ) {
                    Log.e(
                        GROUP_DECK_DEBUG,
                        """
        GroupDeck visibility:
        level=$level
        groupKey=${groupMeta?.groupKey}
        expanded=$expanded
        keepTopCardVisibleWhenExpanded=$keepTopCardVisibleWhenExpanded
        showTopCardBlock=$showTopCardBlock
        """.trimIndent()
                    )
                }

                if (hasDeck && showTopCardBlock) {
                    // кол-во подложек (слоёв)
                    val backCount = stackSize.coerceAtLeast(0)

                    // alphas[0] → слой index=1, alphas[1] → index=2, ...
                    val backAlphas = remember(backCount) {
                        mutableStateListOf<Float>().apply {
                            repeat(backCount) { add(1f) }
                        }
                    }
                    var firstRun by remember(groupId) { mutableStateOf(true) } // ✅ привязали к группе

                    // анимация последовательного исчезновения/появления подложек
                    LaunchedEffect(expanded, backCount) {
                        if (backCount <= 0) return@LaunchedEffect


                        if (firstRun) {
                            // если по умолчанию expanded=true и заголовка нет,
                            // подложки должны сразу быть скрыты, иначе будет “колода поверх раскрытого списка”.
                            val startAlpha = if (expanded && keepTopCardVisibleWhenExpanded) 0f else 1f
                            for (i in 0 until backCount) backAlphas[i] = startAlpha
                            firstRun = false
                            return@LaunchedEffect
                        }

                        if (expanded && keepTopCardVisibleWhenExpanded) {
                            for (i in backCount - 1 downTo 0) {
                                backAlphas[i] = 0f
                                delay(120)
                            }
                        } else if (!expanded && keepTopCardVisibleWhenExpanded) {
                            for (i in 0 until backCount) {
                                backAlphas[i] = 1f
                                delay(120)
                            }
                        } else {
                            for (i in 0 until backCount) backAlphas[i] = 1f
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = (10 - count * 2).dp,
                                end = (10 + count * 2).dp,
                                bottom = cardBottomPadding
                            )
                    ) {
                        // подложки (стек)
                        val selectedBackColor = colorResource(R.color.selected_item)
                        val normalBackColor = Color.White

// какие элементы отображаем как подложки
                        val deckItems = remember(items, stackSize) { items.take(stackSize) }

                        for (i in stackSize downTo 1) {
                            val idx0 = i - 1
                            val alpha = backAlphas.getOrNull(idx0) ?: 1f

                            val isSelected = deckItems.getOrNull(idx0)?.selected == true
                            val isBottomVisibleBack = i == stackSize

                            val bg = when {
                                isSelected -> selectedBackColor
                                hasHiddenSelected && isBottomVisibleBack -> Color(0xFFFFF59D)
                                else -> normalBackColor
                            }

                            DeckCardBack(
                                backgroundColor = bg,
                                index = i,
                                offsetStep = stackOffset,
                                alpha = alpha,
                                shadow = shadow
                            )
                        }

                        // верхняя агрегированная карточка
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .offset(y = topCardOffsetY)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    color = colorResource(R.color.TabE),
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            ItemUI(
                                item = topItem,
                                visibilityColumName = topCardVisibilityColumName,
                                settingsItemUI = topCardSettings,
                                showRoundCheckbox = !(viewModel.modeUI == ModeUI.ONE_SELECT && level == 0),
                                contextUI = viewModel.modeUI,
                                onClickItem = {
                                    // клик по карте действует как по заголовку
                                    expanded = !expanded
                                },
                                onLongClickItem = {
                                    if (items.size > 1) {
                                        viewModel.onLongClickItems(
                                            items = items,
                                            context = context,
                                            clickedItem = topItem
                                        )
                                    } else {
                                        viewModel.onLongClickItem(it, context)
                                    }
                                },
                                onClickItemImage = {
                                    viewModel.onClickItemImage(it, context)
                                },
                                onMultipleClickItemImage = { dataItem, indexImg ->
                                    viewModel.onClickItemImage(dataItem, context, indexImg)
                                },
                                onCheckItem = { checked, it ->
                                    if (viewModel.modeUI != ModeUI.ONE_SELECT)
                                        viewModel.updateItemsSelect(
                                            ids = items.map { it.stableId },
                                            checked = checked
                                        )
                                },
                                onClickProductCode = { clickedItem, fieldValue, action ->
                                    viewModel.onClickProductCode(
                                        itemUI = clickedItem,
                                        fieldValue = fieldValue,
                                        action = action,
                                        context = context
                                    )
                                },
                                onLongClickProductCode = { clickedItem, fieldValue, action ->
                                    viewModel.onLongClickProductCode(
                                        itemUI = clickedItem,
                                        fieldValue = fieldValue,
                                        action = action,
                                        context = context
                                    )
                                },
                            )
                        }
                    }
                } else if (!hasDeck && showTopCardBlock) {
                    // группа из одного элемента
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 10.dp,
                                end = 10.dp,
                                bottom = 6.dp,
                                top = 6.dp
                            )
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                    ) {
                        ItemUI(
                            item = topItem,
                            visibilityColumName = visibilityColumName,
                            settingsItemUI = settingsItems,
                            contextUI = viewModel.modeUI,
                            onClickItem = {
                                expanded = !expanded
                            },
                            onLongClickItem = {
                                viewModel.onLongClickItem(it, context)
                            },
                            onClickItemImage = {
                                viewModel.onClickItemImage(it, context)
                            },
                            onMultipleClickItemImage = { dataItem, indexImg ->
                                viewModel.onClickItemImage(dataItem, context, indexImg)
                            },
                            onCheckItem = { checked, it ->
                                viewModel.updateItemsSelect(
                                    ids = items.map { it.stableId },
                                    checked = checked
                                )
//                                viewModel.updateItemSelect(checked, it)
                            },
                            onClickProductCode = { clickedItem, fieldValue, action ->
                                viewModel.onClickProductCode(
                                    itemUI = clickedItem,
                                    fieldValue = fieldValue,
                                    action = action,
                                    context = context
                                )
                            },
                            onLongClickProductCode = { clickedItem, fieldValue, action ->
                                viewModel.onLongClickProductCode(
                                    itemUI = clickedItem,
                                    fieldValue = fieldValue,
                                    action = action,
                                    context = context
                                )
                            },
                        )
                    }
                }

                // ============= 2. РАСКРЫТЫЙ КОНТЕНТ (СПИСОК / СУБГРУППЫ) НИЖЕ =============
                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInVertically(
                        animationSpec = tween(1500),
                        initialOffsetY = { fullHeight -> -fullHeight }
                    ) + expandVertically(
                        animationSpec = tween(1500),
                        expandFrom = Alignment.Top
                    ),
                    exit = slideOutVertically(
                        animationSpec = tween(1500),
                        targetOffsetY = { fullHeight -> -fullHeight }
                    ) + shrinkVertically(
                        animationSpec = tween(1500),
                        shrinkTowards = Alignment.Top
                    )
                ) {
                    val nextLevel = level + 1
                    val nextGroupingKey = groupingFields.getOrNull(nextLevel)?.key

                    if (nextGroupingKey.isNullOrBlank()) {
                        // ❌ больше группировок нет — просто список карточек
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .padding(
                                            start = 10.dp, end = 10.dp,
                                            bottom = 6.dp, top = 6.dp
                                        )
                                        .shadow(4.dp, RoundedCornerShape(8.dp))
                                ) {
                                    val rows =
                                        productCodeEditorState.rowsByItemId[item.stableId].orEmpty()
                                    val expanded = productCodeEditorState.expanded

                                    ItemUI(
                                        item = item,
                                        visibilityColumName = visibilityColumName,
                                        settingsItemUI = uiState.settingsItems,
                                        contextUI = viewModel.modeUI,
                                        onClickItem = { viewModel.onClickItem(it, context) },
                                        onLongClickItem = {
                                            viewModel.onLongClickItem(
                                                it,
                                                context
                                            )
                                        },
                                        onClickItemImage = {
                                            viewModel.onClickItemImage(
                                                it,
                                                context
                                            )
                                        },
                                        onMultipleClickItemImage = { dataItem, index ->
                                            viewModel.onClickItemImage(dataItem, context, index)
                                        },
                                        onCheckItem = { checked, it ->
                                            viewModel.updateItemSelect(checked, it)
                                        },
                                        onClickProductCode = { clickedItem, fieldValue, action ->
                                            viewModel.onClickProductCode(
                                                itemUI = clickedItem,
                                                fieldValue = fieldValue,
                                                action = action,
                                                context = context
                                            )
                                        },
                                        onLongClickProductCode = { clickedItem, fieldValue, action ->
                                            viewModel.onLongClickProductCode(
                                                itemUI = clickedItem,
                                                fieldValue = fieldValue,
                                                action = action,
                                                context = context
                                            )
                                        },
                                        productCodeExpanded = expanded,
                                        productCodeRows = rows,
                                        onProductCodeMinus = { rowId ->
                                            viewModel.decreaseProductCodeValue(item.stableId, rowId)
                                        },
                                        onProductCodePlus = { rowId ->
                                            viewModel.increaseProductCodeValue(item.stableId, rowId)
                                        },
                                        onProductCodeValueChange = { rowId, value ->
                                            viewModel.updateProductCodeValue(
                                                item.stableId,
                                                rowId,
                                                value
                                            )
                                        },
                                        onProductCodeTakePhoto = { clickedItem ->
                                            viewModel.onProductCodeTakePhoto(clickedItem, context)
                                        },
                                        onClickImageComment = { clickedItem, fieldValue ->
                                            viewModel.onClickImageComment(
                                                itemUI = clickedItem,
                                                fieldValue = fieldValue,
                                                context = context
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // ✅ есть следующая группировка — строим СУБГРУППЫ
                        val subGroups = buildSubGroups(
                            items = items,
                            groupingKey = nextGroupingKey
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            subGroups.forEach { sub ->
                                val subMeta = if (groupMeta != null) {
                                    GroupMeta(
                                        groupKey = sub.key,
                                        title = sub.title,
                                        startIndex = 0,
                                        endIndexExclusive = sub.items.size
                                    )
                                } else null

                                GroupDeck(
                                    groupMeta = subMeta,
                                    items = sub.items,
                                    visibilityColumName = visibilityColumName,
                                    settingsItems = settingsItems,
                                    viewModel = viewModel,
                                    context = context,
                                    groupingFields = groupingFields,
                                    level = nextLevel,
                                    maxStackSize = maxStackSize,
                                    stackOffset = stackOffset
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun BoxScope.DeckCardBack(
    backgroundColor: Color,
    index: Int,
    offsetStep: Dp,
    alpha: Float = 1f,
    shadow: Shadow
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .matchParentSize()
            .offset(
                x = index * 2.dp,
                y = index * offsetStep
            )
            .graphicsLayer {
                this.alpha = alpha   // 👈 управляем видимостью всей подложки
            }
            .padding(top = offsetStep)
            .shadow(3.dp, shape)
//            .dropShadow(
//                shape = RoundedCornerShape(8.dp),
//                shadow = shadow
//            )
            .clip(shape)
            .border(1.dp, Color.LightGray, shape)
            .background(backgroundColor)
    )
}

private val GROUP_ALWAYS_HIDDEN_KEYS = setOf(
    "client_start_dt",
    "client_end_dt"
)

private fun aggregateNumberOrCount(rawValues: List<Any?>): Double? {
    var sum = 0.0
    var hasNumeric = false

    // уникальные непустые строковые значения (для случая, когда чисел нет вообще)
    val uniqueNonEmptyValues = mutableSetOf<String>()

    for (v in rawValues) {
        val original = v?.toString()?.trim() ?: continue
        if (original.isEmpty()) continue

        // учитываем оригинальное (нормализованное) значение для подсчёта уникальных
        uniqueNonEmptyValues += original

        // убираем пробелы/единицы измерения, приводим запятую к точке
        val cleaned = original
            .replace("\\s+".toRegex(), "")
            .replace("грн", "", ignoreCase = true)
            .replace("шт", "", ignoreCase = true)
            .replace("хв.", "", ignoreCase = true)
            .replace("хв", "", ignoreCase = true)
            .replace(',', '.')

        val n = cleaned.toDoubleOrNull()
        if (n != null) {
            hasNumeric = true
            sum += n
        }
    }

    return when {
        // есть хотя бы одно числовое значение -> возвращаем сумму
        hasNumeric -> sum

        // чисел нет, но есть строки -> возвращаем кол-во уникальных
        uniqueNonEmptyValues.isNotEmpty() -> uniqueNonEmptyValues.size.toDouble()

        // всё пустое
        else -> null
    }
}


/**
 * Собирает агрегированную карточку для колоды:
 * - спец. алгоритмы по ключам
 * - дефолт: одинаковые значения → показываем, разные/пустые → скрываем
 */

fun buildGroupSummaryItem(
    groupItems: List<DataItemUI>,
    forceColumnName: Boolean = false,
    forcedVisibleKeys: Set<String> = emptySet(),
    forcedDisplayValues: Map<String, String> = emptyMap()
): DataItemUI {
    require(groupItems.isNotEmpty())

    val first = groupItems.first()

    val visibleKeys = buildSet {
        addAll(first.fields.map { it.key.lowercase() })
        if (forceColumnName) add("column_name")
        addAll(forcedVisibleKeys.map { it.lowercase() })
    }

    val orderedKeys = first.rawFields.map { it.key }

    val summaryRawFields = mutableListOf<FieldValue>()
    val summaryFields = mutableListOf<FieldValue>()

    for (key in orderedKeys) {
        if (key in GROUP_ALWAYS_HIDDEN_KEYS) continue

        val perItemFields: List<FieldValue> = groupItems.mapNotNull { item ->
            item.rawFields.firstOrNull { it.key == key }
        }
        if (perItemFields.isEmpty()) continue

        val rawValues = perItemFields.map { it.value.rawValue }
        val displayValues = perItemFields.map { it.value.value.orEmpty().trim() }

        val aggregatedDisplay: String? = when (key) {
            "dt" -> {
                val inputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
                val outputFormatter =
                    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

                val dates: List<LocalDate> = displayValues.mapNotNull { s ->
                    val v = s.trim()
                    if (v.isEmpty()) return@mapNotNull null
                    runCatching { LocalDate.parse(v, inputFormatter) }.getOrNull()
                }

                if (dates.isEmpty()) null
                else {
                    val minDate = dates.minOrNull()!!
                    val maxDate = dates.maxOrNull()!!
                    val minStr = minDate.format(outputFormatter)
                    val maxStr = maxDate.format(outputFormatter)
                    if (minDate == maxDate) minStr else "C $minStr по $maxStr"
                }
            }

            "cash_ispolnitel" -> {
                val sum = aggregateNumberOrCount(rawValues)
                sum?.let {
                    if (it % 1.0 == 0.0) "${it.toLong()} грн"
                    else String.format(Locale.getDefault(), "%.0f грн", it)
                }
            }

            "addr_txt" -> {
                val distinct = displayValues
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()

                when {
                    distinct.isEmpty() -> null
                    distinct.size == 1 -> distinct.first()
                    else -> "${distinct.size} адреса"
                }
            }

            "client_txt" -> {
                val distinct = displayValues
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()

                when {
                    distinct.isEmpty() -> null
                    distinct.size == 1 -> distinct.first()
                    else -> "${distinct.size} клієнта"
                }
            }

            "duration" -> {
                val sum = aggregateNumberOrCount(rawValues)
                sum?.let { "${it.toLong()} хв." }
            }

            "user_txt" -> {
                val distinct = displayValues
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()

                when {
                    distinct.isEmpty() -> null
                    distinct.size == 1 -> distinct.first()
                    else -> "${distinct.size} виконавцiв"
                }
            }

            "sku" -> ""
            "option_code" -> ""

            "status" -> {
                val statuses: List<Int> = rawValues.mapNotNull { v ->
                    v?.toString()?.trim()?.toIntOrNull()
                }

                if (statuses.isEmpty()) {
                    null
                } else {
                    val total = statuses.size
                    val prov = statuses.count { it == 1 }
                    val notProv = total - prov

                    fun pct(count: Int): Int =
                        if (total == 0) 0 else ((count * 100.0 / total).roundToInt())

                    val provPct = pct(prov)
                    val notProvPct = pct(notProv)

                    "Пр: $total / Вр: $prov ($provPct%) / Нр: $notProv ($notProvPct%)"
                }
            }

            "expire_period" -> ""
            "barcode" -> ""
            "manufacturer_id" -> ""
            "nm" -> ""
            "group_id" -> ""
            "weight" -> ""
//                { "${displayValues.size} элементов" }

            else -> aggregateDefaultList(displayValues)
        }
        val forcedDisplay = forcedDisplayValues.entries
            .firstOrNull { it.key.equals(key, ignoreCase = true) }
            ?.value
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        val finalDisplay: String? =
            forcedDisplay ?: if (aggregatedDisplay != null) {
                aggregatedDisplay.ifEmpty { null }
            } else {
                val allEmpty = displayValues.all { it.isBlank() }
                if (allEmpty) {
                    null
                } else {
                    val distinct = displayValues.toSet()
                    if (distinct.size == 1) distinct.first()
                    else {
                        val uniqueUsers: Set<String> = displayValues
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .toSet()
                        if (uniqueUsers.isEmpty()) null
                        else uniqueUsers.joinToString(", ")
                    }
                }
            }

        if (finalDisplay == null) continue

        val template = perItemFields.first()

        val aggregatedField = FieldValue(
            key = key,
            field = if (key == "sortcol") {
                template.field.copy(
                    rawValue = "Всего:",
                    value = "Всего:"
                )
            } else {
                template.field
            },
            value = template.value.copy(
                rawValue = finalDisplay,
                value = finalDisplay
            )
        )

        summaryRawFields += aggregatedField

        if (key.lowercase() in visibleKeys) {
            summaryFields += aggregatedField
        }

    }

    val isTovarGroup = groupItems.firstOrNull()
        ?.rawObj
        ?.any { it is TovarDB } == true

    if (isTovarGroup) {
        val perItemFields: List<FieldValue> = groupItems.mapNotNull { item ->
            item.rawFields.firstOrNull { it.key == "column_name" }
        }
        val displayValues = perItemFields.map { it.value.value.orEmpty().trim() }

        val isTovarGroup = groupItems.firstOrNull()
            ?.rawObj
            ?.any { it is TovarDB } == true

        if (isTovarGroup) {
            val totalField = FieldValue(
                key = "tovar_total_count",
                field = TextField(
                    rawValue = "Всего:",
                    value = "Всего:"
                ),
                value = TextField(
                    rawValue = "${groupItems.size} элементов",
                    value = "${groupItems.size} элементов"
                )
            )

            summaryRawFields += totalField
            summaryFields += totalField
        }
    }
    val allRawObjects = groupItems.flatMap { it.rawObj }

    return first.copy(
        rawObj = allRawObjects,
        rawFields = summaryRawFields,
        fields = summaryFields
    )
}

private fun aggregateDefaultList(
    values: List<String?>,
    maxVisible: Int = 2
): String? {
    val distinct = values
        .mapNotNull { it?.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

    if (distinct.isEmpty()) return null

    return if (distinct.size <= maxVisible) {
        distinct.joinToString(", ")
    } else {
        val visible = distinct.take(maxVisible).joinToString(", ")
        val extraCount = distinct.size - maxVisible
        "$visible.. (усього ${distinct.size} елементів)"
    }
}

private data class LocalGroup(
    val key: String,
    val title: String?,
    val items: List<DataItemUI>
)

private fun buildSubGroups(
    items: List<DataItemUI>,
    groupingKey: String
): List<LocalGroup> {
    if (groupingKey.isBlank()) return emptyList()

    // группируем по значению нужного поля
    val map = LinkedHashMap<String, MutableList<DataItemUI>>()

    for (item in items) {
        val field = item.rawFields.firstOrNull {
            it.key.equals(groupingKey, ignoreCase = true)
        }

        val groupKey = field?.value?.value?.takeIf { it.isNotBlank() } ?: "—"

        val list = map.getOrPut(groupKey) { mutableListOf() }
        list += item
    }

    return map.map { (key, listItems) ->
        LocalGroup(
            key = key,
            title = key,          // при желании можно сделать красивый title
            items = listItems
        )
    }
}


private fun FieldValue.withoutModifiers(): FieldValue =
    copy(
        field = field.copy(modifierValue = null),
        value = value.copy(modifierValue = null)
    )

fun DataItemUI.withoutUiModifiers(): DataItemUI =
    copy(
        modifierContainer = null,                 // фон, шрифты, паддинги контейнера
        rawFields = rawFields.map { it.withoutModifiers() },
        fields = fields.map { it.withoutModifiers() }
    )


private fun FieldValue.witBackgroundModifiers(): FieldValue =
    copy(
        field = field.copy(modifierValue = null),
        value = value.copy(modifierValue = null)
    )

fun DataItemUI.witBackgroundUiModifiers(): DataItemUI =
    copy(
        modifierContainer = MerchModifier(
            background = Color(android.graphics.Color.parseColor("#D7D5D5"))
        ),
        rawFields = rawFields.map { it.withoutModifiers() },
        fields = fields.map { it.withoutModifiers() }
    )

fun DataItemUI.witBackgroundUiModifiers(level: Int = 0): DataItemUI =
    copy(
        modifierContainer = MerchModifier(
            background = groupBackgroundForLevel(level)
        ),
        rawFields = rawFields.map { it.withoutModifiers() },
        fields = fields.map { it.withoutModifiers() }
    )

private fun groupBackgroundForLevel(level: Int): Color {
    // базовый цвет для верхнего уровня
    val base = Color(android.graphics.Color.parseColor("#D7D5D5"))

    // чем глубже level, тем ближе к белому
    val factor = when {
        level <= 0 -> 0f       // самый тёмный (как сейчас)
        level == 1 -> 0.36f    // немного светлее
        level == 2 -> 0.70f
        else -> 0.22f          // ещё светлее для 3+ уровней
    }

    return lightenTowardsWhite(base, factor)
}

private fun lightenTowardsWhite(color: Color, factor: Float): Color {
    val f = factor.coerceIn(0f, 1f)
    return Color(
        red = color.red + (1f - color.red) * f,
        green = color.green + (1f - color.green) * f,
        blue = color.blue + (1f - color.blue) * f,
        alpha = color.alpha
    )
}


private fun List<SettingsItemUI>.withForcedKeys(keys: Set<String>): List<SettingsItemUI> {
    if (keys.isEmpty()) return this

    return map { item ->
        if (keys.any { forced -> item.key.equals(forced, ignoreCase = true) }) {
            item.copy(isEnabled = true)
        } else {
            item
        }
    }
}

private fun List<SettingsItemUI>.isGroupHeaderEnabled(): Boolean {
    return firstOrNull { it.key.equals("group_header", ignoreCase = true) }?.isEnabled ?: true
}

private const val GROUP_DECK_DEBUG = "GROUP_DECK_DEBUG"

private fun List<SettingsItemUI>.enabledKeysDebug(): String =
    filter { it.isEnabled }
        .joinToString(", ") { it.key }
        .ifBlank { "<none>" }

private fun List<SettingsItemUI>.allKeysDebug(): String =
    joinToString(" | ") { "${it.key}=${it.isEnabled}" }