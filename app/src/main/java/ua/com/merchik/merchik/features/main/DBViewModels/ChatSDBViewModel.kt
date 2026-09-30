package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ua.com.merchik.merchik.Activities.ReferencesActivity.ReferencesActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatGrpSDB
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatListItem
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatSDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.toItemUI
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.ItemFilterChoice
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject

data class ChatConversation(val chat: ChatGrpSDB, val messages: List<ChatSDB>)

@HiltViewModel
class ChatSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {
    override val table = ChatListItem::class
    private var rows: List<ChatListItem> = emptyList()
    private var listSubscription: Disposable? = null
    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount = _unreadCount.asStateFlow()

    init {
        contextUI = ContextUI.CHATS_IN_CONTAINER
        typeWindow = "container"
    }

    fun startObserving() {
        if (listSubscription?.isDisposed == false) return
        _loading.value = true
        _error.value = null
        listSubscription = RoomManager.SQL_DB.chatGrpDao().observeChatList()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ updated ->
                rows = updated
                _unreadCount.value = rows.sumOf { it.kolUnread.toLong() }
                _loading.value = false
                super.updateContent()
            }, { failure ->
                _loading.value = false
                _error.value = getApplication<Application>().getString(R.string.chats_load_error)
                Globals.writeToMLOG("ERROR", "ChatSDBViewModel/list", failure.toString())
            })
    }

    fun stopObserving() {
        listSubscription?.dispose()
        listSubscription = null
    }

    override fun updateContent() {
        super.updateContent()
        stopObserving()
        startObserving()
    }

    override fun getDefaultHideUserFields() = listOf("id", "dt")
    override fun getDefaultSortUserFields() = listOf("dt")
    override fun getHideSortUserFields() = listOf("readState")

    override suspend fun getItems(): List<DataItemUI> =
        rows.map { it.toItemUI(nameUIRepository, null, null) }

    override fun updateFilters() {
        val previous = uiState.value.filters ?: Filters()
        val displayFilter = ItemFilter(
            title = "Стан повідомлень", clazz = table, modeUI = ModeUI.ONE_SELECT,
            titleContext = "Стан повідомлень", subTitleContext = "",
            leftField = "readState", rightField = "readState",
            rightValuesRaw = emptyList(), rightValuesUI = emptyList(), enabled = true,
            choices = listOf(
                ItemFilterChoice("all", "Усі", emptyList()),
                ItemFilterChoice("unread", "Є непрочитані", listOf("1")),
                ItemFilterChoice("read", "Усі прочитані", listOf("0"))
            ), defaultChoiceKey = "all"
        )
        filters = previous.copy(items = listOf(
            previous.items.firstOrNull { it.key == displayFilter.key } ?: displayFilter.clearValues()
        ))
    }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        val chat = itemUI.rawObj.filterIsInstance<ChatListItem>().firstOrNull() ?: return
        (context as? ReferencesActivity)?.openChat(chat.id)
    }

    override fun onClickAdditionalContent() {
        context?.let { Toast.makeText(it, R.string.chat_creation_unavailable, Toast.LENGTH_SHORT).show() }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        val host = context ?: return
        if (itemsUI.size == 1) onClickItem(itemsUI.single(), host)
        else Toast.makeText(host, R.string.chat_select_one, Toast.LENGTH_SHORT).show()
    }

    // History is loaded only when the conversation is opened, not during list binding.
    fun loadConversation(chatId: Int): Single<ChatConversation> = Single.zip(
        RoomManager.SQL_DB.chatGrpDao().getById(chatId),
        RoomManager.SQL_DB.chatDao().getAllById(chatId)
    ) { chat, messages -> ChatConversation(chat, messages) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError { Globals.writeToMLOG("ERROR", "ChatSDBViewModel/conversation", "chatId=$chatId, error=$it") }

    override fun onCleared() {
        stopObserving()
        super.onCleared()
    }
}
