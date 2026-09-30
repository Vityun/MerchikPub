package ua.com.merchik.merchik.features.main.Main

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatListItem
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.dialogs.features.indicator.LineSpinFadeLoaderIndicator
import ua.com.merchik.merchik.features.main.DBViewModels.ChatSDBViewModel
import ua.com.merchik.merchik.features.main.chat.ChatDateFormatter
import ua.com.merchik.merchik.features.main.componentsUI.CounterBadge
import ua.com.merchik.merchik.features.main.componentsUI.RoundCheckbox
import java.time.LocalDate

@Composable
fun ChatItemsUI(
    viewModel: ChatSDBViewModel,
    dataItems: List<DataItemUI>,
    groups: List<GroupMeta>,
    listState: LazyListState
) {
    val uiState by viewModel.uiState.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val fontOffset by viewModel.offsetSizeFonts.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val today by produceState(LocalDate.now(), lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                value = LocalDate.now()
                delay(60_000)
            }
        }
    }
    val showSelection = viewModel.modeUI in listOf(ModeUI.FILTER_SELECT, ModeUI.MULTI_SELECT, ModeUI.ONE_SELECT)
    val itemContent: @Composable (DataItemUI) -> Unit = { item ->
        ChatItemUI(item, uiState.settingsItems, fontOffset, showSelection, today,
            onClick = {
                focusManager.clearFocus()
                viewModel.onClickItem(item, context)
            },
            onCheckedChange = { viewModel.updateItemSelect(it, item) })
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            error?.let { message ->
                Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(message, color = colorResource(R.color.red_error))
                    TextButton(onClick = viewModel::updateContent) { Text(stringResource(R.string.chats_retry)) }
                }
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = listState, contentPadding = PaddingValues(vertical = 2.dp)) {
                if (groups.isEmpty()) {
                    items(dataItems, key = { it.stableId }) { itemContent(it) }
                } else {
                    items(groups, key = { "${it.groupKey}_${it.startIndex}" }) { group ->
                        GroupDeck(
                            groupMeta = group,
                            items = dataItems.subList(group.startIndex, group.endIndexExclusive),
                            visibilityColumName = if (uiState.settingsItems.any { it.key == "column_name" && it.isEnabled }) View.VISIBLE else View.GONE,
                            settingsItems = uiState.settingsItems, viewModel = viewModel,
                            context = context, groupingFields = uiState.groupingFields,
                            level = 0, itemContent = itemContent
                        )
                    }
                }
            }
        }
        if (loading) {
            LineSpinFadeLoaderIndicator(color = Color.Gray,
                modifier = Modifier.align(Alignment.Center).size(36.dp),
                radius = 12f, elementHeight = 5f, penThickness = 3f)
        } else if (error == null && dataItems.isEmpty()) {
            Text(stringResource(R.string.chats_empty), Modifier.align(Alignment.Center).padding(16.dp))
        }
    }
}

@Composable
fun ChatToolbarTitle(viewModel: ChatSDBViewModel) {
    val unreadCount by viewModel.unreadCount.collectAsState()
    Row(
        Modifier.fillMaxWidth().heightIn(min = 45.dp).padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(stringResource(R.string.chat_toolbar_title), Modifier.padding(top = 8.dp),
                fontSize = 18.sp, color = Color.Black)
            if (unreadCount > 0) {
                CounterBadge(unreadCount, Modifier.padding(start = 3.dp), maxCount = null,
                    background = Color.LightGray,
                    borderAndTextColor = Color.DarkGray)
            }
        }
    }
}

@Composable
private fun ChatItemUI(
    item: DataItemUI,
    settings: List<SettingsItemUI>,
    fontOffset: Float,
    showSelection: Boolean,
    today: LocalDate,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val chat = item.rawObj.filterIsInstance<ChatListItem>().firstOrNull() ?: return
    val visibleKeys = settings.filter { it.isEnabled }.map { it.key }.toSet()
    val showLabels = "column_name" in visibleKeys
    val shape = RoundedCornerShape(8.dp)
    Column(
        Modifier.padding(horizontal = 8.dp, vertical = 6.dp).fillMaxWidth()
            .shadow(4.dp, shape).clip(shape).border(1.dp, Color.LightGray, shape)
            .background(if (item.selected) colorResource(R.color.selected_item) else Color.White)
            .clickable(onClick = onClick).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (showSelection) {
                RoundCheckbox(aroundColor = Color.Transparent, checked = item.selected, onCheckedChange = onCheckedChange)
            }
//            Image(painterResource(if (chat.kolUnread > 0) R.drawable.ic_email else R.drawable.ic_email_open),
//                contentDescription = stringResource(if (chat.kolUnread > 0) R.string.chat_unread else R.string.chat_read),
//                modifier = Modifier.size(28.dp))
            if ("nm" in visibleKeys) {
                Text(chat.nm, Modifier.weight(1f), fontSize = (16 + fontOffset).sp,
                    fontWeight = if (chat.kolUnread > 0) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (chat.kolUnread > 0 && "kolUnread" in visibleKeys) {
                CounterBadge(chat.kolUnread, Modifier.align(Alignment.Top), maxCount = null,
                    background = colorResource(R.color.selected_item),
                    borderAndTextColor = Color.DarkGray)
            }
        }
        item.fields.filter { it.key in visibleKeys && it.key in listOf("id", "dt") }
            .forEach { field ->
                Text(if (showLabels) "${field.field.value} ${field.value.value}" else field.value.value,
                    fontSize = (14 + fontOffset).sp, color = Color.DarkGray,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        val showMessage = "lastMsg" in visibleKeys && chat.lastMsg.isNotBlank()
        val timestamp = if ("lastUpdate" in visibleKeys) {
            ChatDateFormatter.format(chat.lastUpdate, today = today)
        } else ""
        if (showMessage || timestamp.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showMessage) {
                    Text(chat.lastMsg, Modifier.weight(1f),
                        fontSize = (14 + fontOffset).sp, color = Color.DarkGray,
                        maxLines = 4, overflow = TextOverflow.Ellipsis)
                } else {
                    Spacer(Modifier.weight(1f))
                }
                if (timestamp.isNotEmpty()) {
                    Text(timestamp, fontSize = (12 + fontOffset).sp, color = Color.Gray,
                        maxLines = 1, softWrap = false)
                }
            }
        }
    }
}
