package ua.com.merchik.merchik.features.main.Main


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.MainEvent
import ua.com.merchik.merchik.dataLayer.model.ContextMenuActionEvent
import ua.com.merchik.merchik.dataLayer.model.ContextMenuEntry
import ua.com.merchik.merchik.dataLayer.model.ContextMenuHeaderUi
import ua.com.merchik.merchik.dataLayer.model.ContextMenuPayload
import ua.com.merchik.merchik.dataLayer.model.ContextMenuUiState
import ua.com.merchik.merchik.dataLayer.model.MenuLeading
import ua.com.merchik.merchik.dataLayer.model.SubmenuPresentation


@Composable
fun rememberContextMenuHost(
    viewModel: MainViewModel,
    onOpenSortingDialog: () -> Unit = {},
    onOpenAdditionalWorkDialog: () -> Unit = {}
) {
    var menuState by remember { mutableStateOf<ContextMenuUiState?>(null) }
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is MainEvent.ShowContextMenu -> {
                        menuState = event.state
                    }

                    MainEvent.HideContextMenu -> {
                        menuState = null
                    }

                    MainEvent.OpenSortingDialog -> {
                        onOpenSortingDialog()
                    }

                    MainEvent.OpenAdditionalWorkDialog -> {
                        onOpenAdditionalWorkDialog()
                    }
                    else -> Unit
                }
            }
        }
    }

    menuState?.let { state ->
        UniversalContextMenuDialog(
            visible = true,
            state = state,
            onDismiss = {
                menuState = null
                viewModel.onContextMenuDismissed()
            },
            onActionClick = { actionEvent, closeMenu ->
                focusManager.clearFocus(force = true)
                viewModel.onContextMenuAction(actionEvent)

                if (closeMenu) {
                    menuState = null
                }
            }
        )
    }
}

@Composable
private fun ContextMenuContainer(
    visible: Boolean,
    onDismiss: () -> Unit,
    header: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(min = 220.dp, max = 420.dp)
//                .animateContentSize(
//                    animationSpec = spring(
//                        dampingRatio = Spring.DampingRatioMediumBouncy,
//                        stiffness = Spring.StiffnessLow
//                    )
//                )
                .background(Color.White, RoundedCornerShape(8.dp))
        ) {
            if (header != null) {
                header()
                HorizontalDivider(
                    color = colorResource(R.color.background_item_filter),
                    thickness = 1.dp
                )
            }

            content()
        }
    }
}

@Composable
private fun ContextMenuHeaderView(header: ContextMenuHeaderUi) {
    if (!header.visible) return

    Column(
        modifier = Modifier
            .background(
                color = colorResource(R.color.background_item_filter),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            )
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        header.title?.let {
            Text(
                text = it,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (header.title != null && header.rows.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
        }

        header.rows.forEachIndexed { index, row ->
            Row {
                Text(
                    text = "${row.label}: ",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = row.value,
                    fontWeight = if (row.valueHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (index < header.rows.lastIndex) {
                Spacer(Modifier.height(2.dp))
            }
        }
    }
}

@Composable
private fun MenuRowDivider(
    nestingLevel: Int = 0
) {
    HorizontalDivider(
        modifier = Modifier.padding(start = (44 + nestingLevel * 16).dp),
        color = colorResource(R.color.transparent),
        thickness = 1.dp
    )
}

@Composable
private fun MenuSectionDivider() {
    HorizontalDivider(
        color = Color(0xFFBDBDBD),
        thickness = 1.dp
    )
}

@Composable
private fun MenuLeadingView(
    leading: MenuLeading,
    tint: Color
) {
    when (leading) {
        MenuLeading.None -> {
            Spacer(Modifier.size(16.dp))
        }

        is MenuLeading.DrawableIcon -> {
            Icon(
                painter = painterResource(id = leading.resId),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = tint
            )
        }

        is MenuLeading.Text -> {
            Text(
                text = leading.value,
                color = tint,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        is MenuLeading.Checkbox -> {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .border(1.dp, tint, RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (leading.checked) {
                    Text(
                        text = "✓",
                        color = tint,
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SubmenuArrow(
    expanded: Boolean,
    enabled: Boolean
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "submenu_arrow_rotation"
    )

    val tint = if (enabled) {
        Color.Black.copy(alpha = 0.75f)
    } else {
        Color.Black.copy(alpha = 0.35f)
    }

    Icon(
        imageVector = Icons.Filled.KeyboardArrowRight,
        contentDescription = null,
        modifier = Modifier
            .size(18.dp)
            .rotate(rotation),
        tint = tint
    )
}


@Composable
private fun ContextMenuRow(
    title: String,
    leading: MenuLeading,
    enabled: Boolean,
    nestingLevel: Int = 0,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val textColor = if (enabled) {
        Color.Black.copy(alpha = 0.95f)
    } else {
        Color.Black.copy(alpha = 0.45f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enabled && onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(
                start = 12.dp + (nestingLevel * 16).dp,
                end = 12.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(20.dp),
            contentAlignment = Alignment.Center
        ) {
            MenuLeadingView(
                leading = leading,
                tint = textColor
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Immutable
private data class ContextMenuPage(
    val id: String,
    val header: ContextMenuHeaderUi?,
    val entries: List<ContextMenuEntry>
)

@Composable
private fun ContextMenuEntriesContent(
    entries: List<ContextMenuEntry>,
    payload: ContextMenuPayload,
    currentHeader: ContextMenuHeaderUi?,
    nestingLevel: Int = 0,
    onActionClick: (ContextMenuActionEvent, closeMenu: Boolean) -> Unit,
    onOpenOverlay: (ContextMenuUiState, Rect?) -> Unit,
    onPushPage: (ContextMenuPage) -> Unit
)
{
    val expandedMap = remember(entries) { mutableStateMapOf<String, Boolean>() }

    entries
        .filter {
            when (it) {
                is ContextMenuEntry.Action -> it.visible
                is ContextMenuEntry.Submenu -> it.visible
                is ContextMenuEntry.Divider -> true
            }
        }
        .forEachIndexed { index, entry ->
            when (entry) {
                is ContextMenuEntry.Divider -> {
                    MenuSectionDivider()
                }

                is ContextMenuEntry.Action -> {
                    ContextMenuRow(
                        title = entry.title,
                        leading = entry.leading,
                        enabled = entry.enabled,
                        nestingLevel = nestingLevel,
                        onClick = {
                            onActionClick(
                                ContextMenuActionEvent(
                                    actionId = entry.actionId,
                                    payload = payload
                                ),
                                entry.closeMenuOnClick
                            )
                        }
                    )
                }

                is ContextMenuEntry.Submenu -> {
                    val expanded = expandedMap[entry.id] ?: entry.expandedByDefault
                    var rowBounds by remember(entry.id) { mutableStateOf<Rect?>(null) }

                    ContextMenuRow(
                        title = entry.title,
                        leading = entry.leading,
                        enabled = entry.enabled,
                        nestingLevel = nestingLevel,
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            rowBounds = coordinates.boundsInParent()
                        },
                        trailing = {
                            SubmenuArrow(
                                expanded = entry.presentation == SubmenuPresentation.INLINE_EXPAND && expanded,
                                enabled = entry.enabled
                            )
                        },
                        onClick = {
                            when (entry.presentation) {
                                SubmenuPresentation.INLINE_EXPAND -> {
                                    expandedMap[entry.id] = !expanded
                                }

                                SubmenuPresentation.OVERLAY -> {
                                    onOpenOverlay(
                                        ContextMenuUiState(
                                            payload = payload,
                                            header = entry.headerOverride ?: currentHeader,
                                            entries = entry.items
                                        ),
                                        rowBounds
                                    )
                                }

                                SubmenuPresentation.REPLACE -> {
                                    onPushPage(
                                        ContextMenuPage(
                                            id = entry.id,
                                            header = entry.headerOverride ?: currentHeader,
                                            entries = entry.items
                                        )
                                    )
                                }
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = entry.presentation == SubmenuPresentation.INLINE_EXPAND && expanded,
                        enter = fadeIn(animationSpec = tween(140)) +
                                expandVertically(
                                    expandFrom = Alignment.Top,
                                    animationSpec = tween(180)
                                ),
                        exit = fadeOut(animationSpec = tween(90)) +
                                shrinkVertically(
                                    shrinkTowards = Alignment.Top,
                                    animationSpec = tween(140)
                                )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MenuRowDivider(nestingLevel = nestingLevel + 1)

                            ContextMenuEntriesContent(
                                entries = entry.items,
                                payload = payload,
                                currentHeader = entry.headerOverride ?: currentHeader,
                                nestingLevel = nestingLevel + 1,
                                onActionClick = onActionClick,
                                onOpenOverlay = onOpenOverlay,
                                onPushPage = onPushPage
                            )
                        }
                    }
                }
            }

            val shouldDrawDivider =
                index < entries.lastIndex &&
                        entry !is ContextMenuEntry.Divider &&
                        entries[index + 1] !is ContextMenuEntry.Divider

            if (shouldDrawDivider) {
                MenuRowDivider(nestingLevel = nestingLevel)
            }
        }
}

@Composable
fun UniversalContextMenuDialog(
    visible: Boolean,
    state: ContextMenuUiState,
    onDismiss: () -> Unit,
    onActionClick: (ContextMenuActionEvent, closeMenu: Boolean) -> Unit
) {
    if (!visible) return

    var overlayState by remember(state) { mutableStateOf<ContextMenuUiState?>(null) }
    var overlayOrigin by remember(state) { mutableStateOf(TransformOrigin.Center) }
    var baseCardBounds by remember(state) { mutableStateOf<Rect?>(null) }

    var pageStack by remember(state) {
        mutableStateOf(
            listOf(
                ContextMenuPage(
                    id = "root",
                    header = state.header,
                    entries = state.entries
                )
            )
        )
    }

    val currentPage = pageStack.last()

    Dialog(
        onDismissRequest = {
            if (overlayState != null) {
                overlayState = null
            } else if (pageStack.size > 1) {
                pageStack = pageStack.dropLast(1)
            } else {
                onDismiss()
            }
        }
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 220.dp, max = 420.dp)
        ) {
            ContextMenuCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .onGloballyPositioned { coordinates ->
                        baseCardBounds = coordinates.boundsInParent()
                    },
                dimmed = overlayState != null,
                header = {
                    currentPage.header?.let {
                        ContextMenuHeaderView(it)
                    }
                }
            ) {
                if (pageStack.size > 1) {
                    ContextMenuRow(
                        title = "Назад",
                        leading = MenuLeading.None,
                        enabled = true,
                        onClick = {
                            pageStack = pageStack.dropLast(1)
                        }
                    )

                    HorizontalDivider(
                        color = colorResource(R.color.background_item_filter),
                        thickness = 1.dp
                    )
                }

                ContextMenuEntriesContent(
                    entries = currentPage.entries,
                    payload = state.payload,
                    currentHeader = currentPage.header,
                    onActionClick = onActionClick,
                    onOpenOverlay = { overlay, clickedRowBounds ->
                        overlayState = overlay

                        val card = baseCardBounds
                        overlayOrigin = if (card != null && clickedRowBounds != null && card.width > 0f && card.height > 0f) {
                            val fx = ((clickedRowBounds.center.x - card.left) / card.width)
                                .coerceIn(0.15f, 0.85f)
                            val fy = ((clickedRowBounds.center.y - card.top) / card.height)
                                .coerceIn(0.10f, 0.90f)
                            TransformOrigin(fx, fy)
                        } else {
                            TransformOrigin.Center
                        }
                    },
                    onPushPage = { nextPage -> pageStack = pageStack + nextPage }
                )
            }

            if (overlayState != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                )
            }

            AnimatedVisibility(
                visible = overlayState != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .zIndex(1f),
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 220,
                        delayMillis = 40
                    )
                ) + scaleIn(
                    initialScale = 0.15f,
                    transformOrigin = overlayOrigin,
                    animationSpec = tween(
                        durationMillis = 766,
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 120)
                ) + scaleOut(
                    targetScale = 0.92f,
                    transformOrigin = overlayOrigin,
                    animationSpec = tween(
                        durationMillis = 180,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                overlayState?.let { overlay ->
                    ContextMenuCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        header = null
                    ) {
                        ContextMenuEntriesContent(
                            entries = overlay.entries,
                            payload = overlay.payload,
                            currentHeader = null,
                            onActionClick = onActionClick,
                            onOpenOverlay = { nextOverlay, _ ->
                                overlayState = nextOverlay
                            },
                            onPushPage = { _ -> }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ContextMenuCard(
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    dimmed: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = if (dimmed) 0.72f else 1f
                    scaleX = if (dimmed) 0.985f else 1f
                    scaleY = if (dimmed) 0.985f else 1f
                }
                .background(Color.White, RoundedCornerShape(8.dp))
        ) {
            if (header != null) {
                header()
                HorizontalDivider(
                    color = colorResource(R.color.background_item_filter),
                    thickness = 1.dp
                )
            }

            content()
        }

        if (dimmed) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.06f))
            )
        }
    }
}