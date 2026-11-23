@file:JvmName("ChatConversationContent")

package ua.com.merchik.merchik.features.main.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.Utils.text.NativeTextActions
import ua.com.merchik.merchik.Utils.text.NativeTextLinks
import ua.com.merchik.merchik.Utils.text.NativeTextParser
import ua.com.merchik.merchik.data.synchronization.ChatReadPolicy
import ua.com.merchik.merchik.dialogs.features.indicator.LineSpinFadeLoaderIndicator
import ua.com.merchik.merchik.features.main.DBViewModels.ChatSDBViewModel

fun setContentChatConversation(
    view: ComposeView,
    viewModel: ChatConversationViewModel,
    chats: ChatSDBViewModel,
    onBack: Runnable
) {
    view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    view.setContent {
        MerchikTheme {
            val fontOffset by chats.offsetSizeFonts.collectAsState()
            ChatConversationScreen(viewModel, fontOffset, onBack::run)
        }
    }
}

private fun visibleMessageIds(listState: LazyListState): List<Int> {
    val layout = listState.layoutInfo
    return layout.visibleItemsInfo.filter {
        ChatReadPolicy.isVisible(it.offset, it.size, layout.viewportStartOffset, layout.viewportEndOffset)
    }.mapNotNull { it.key as? Int }
}

@OptIn(FlowPreview::class)
@Composable
private fun ChatConversationScreen(viewModel: ChatConversationViewModel, fontOffset: Float, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val title = remember(state.title) { NativeTextParser.parse(state.title).text }
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val windowInfo = LocalWindowInfo.current
    LaunchedEffect(viewModel, listState, lifecycleOwner, windowInfo) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            snapshotFlow {
                if (windowInfo.isWindowFocused && !state.loading && !state.loadError) visibleMessageIds(listState)
                else emptyList()
            }
                .distinctUntilChanged()
                .debounce(500)
                .collect { viewModel.markVisibleRead(it) }
        }
    }
    Column(Modifier.fillMaxSize().background(colorResource(R.color.colorToolbar))) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.chat_back), tint = Color.Black)
            }
            Text(title.ifBlank { stringResource(R.string.chat_toolbar_title) },
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                fontSize = (18 + fontOffset).sp, color = Color.Black,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (state.loadError || state.readError) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(if (state.loadError) R.string.chat_load_error else R.string.chat_read_error),
                    color = colorResource(R.color.red_error))
                TextButton(onClick = {
                    if (state.loadError) viewModel.retry() else viewModel.markVisibleRead(visibleMessageIds(listState))
                }) { Text(stringResource(R.string.chats_retry)) }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(Modifier.fillMaxSize(), state = listState, contentPadding = PaddingValues(vertical = 2.dp)) {
                items(state.messages, key = { it.id }) { message -> ChatMessage(message, fontOffset) }
            }
            if (state.loading) {
                LineSpinFadeLoaderIndicator(color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center).size(36.dp),
                    radius = 12f, elementHeight = 5f, penThickness = 3f)
            } else if (!state.loadError && state.messages.isEmpty()) {
                Text(stringResource(R.string.chat_messages_empty),
                    modifier = Modifier.align(Alignment.Center).padding(16.dp), color = Color.DarkGray)
            }
        }
    }
}

@Composable
private fun ChatMessage(message: ChatMessageItem, fontOffset: Float) {
    val context = LocalContext.current
    val text = remember(message.text, context) {
        NativeTextLinks.toAnnotatedString(NativeTextParser.parse(message.text)) {
            NativeTextActions.perform(context, it)
        }
    }
    val read = message.readAt > 0
    val shape = RoundedCornerShape(8.dp)
    Column(
        Modifier.padding(horizontal = 8.dp, vertical = 6.dp).fillMaxWidth()
            .shadow(4.dp, shape).clip(shape).border(1.dp, Color.LightGray, shape)
            .background(Color.White).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SelectionContainer {
            Text(text, fontSize = (16 + fontOffset).sp,
                color = if (read) Color.DarkGray else Color.Black,
                fontWeight = if (read) FontWeight.Normal else FontWeight.Medium)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(if (read) R.drawable.ic_email_open else R.drawable.ic_email),
                contentDescription = stringResource(if (read) R.string.chat_message_read else R.string.chat_message_unread),
                modifier = Modifier.size(18.dp))
            Text(stringResource(R.string.chat_message_id, message.id), Modifier.weight(1f),
                fontSize = (12 + fontOffset).sp, color = Color.DarkGray)
            Text(ChatDateFormatter.format(message.timestamp), fontSize = (12 + fontOffset).sp,
                color = Color.Gray, maxLines = 1, softWrap = false)
        }
    }
}
