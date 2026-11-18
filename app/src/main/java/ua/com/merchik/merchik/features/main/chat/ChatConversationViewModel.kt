package ua.com.merchik.merchik.features.main.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.ServerExchange.workmager.ChatReadWorker
import ua.com.merchik.merchik.database.room.RoomManager

data class ChatMessageItem(val id: Int, val text: String, val timestamp: Long, val readAt: Long)

data class ChatConversationState(
    val title: String = "",
    val messages: List<ChatMessageItem> = emptyList(),
    val loading: Boolean = true,
    val loadError: Boolean = false,
    val readError: Boolean = false
)

class ChatConversationViewModel(application: Application) : AndroidViewModel(application) {
    private val db = RoomManager.SQL_DB
    private val userId = Globals.getCurrentUserId()
    private val session = Globals.getCurrentSessionId()
    private var chatId = 0
    private var history: Disposable? = null
    private val reads = CompositeDisposable()
    private val inFlight = mutableSetOf<Int>()
    private val _state = MutableStateFlow(ChatConversationState())
    val state = _state.asStateFlow()

    private fun requireAccount() {
        check(userId > 0 && userId == Globals.getCurrentUserId() && db === RoomManager.SQL_DB
            && !session.isNullOrEmpty() && session == Globals.getCurrentSessionId())
    }

    fun open(id: Int) {
        if (chatId == id && history?.isDisposed == false) return
        chatId = id
        retry()
    }

    fun retry() {
        history?.dispose()
        _state.value = _state.value.copy(loading = true, loadError = false)
        history = db.chatGrpDao().getById(chatId)
            .doOnSuccess { requireAccount() }
            .flatMapPublisher { chat ->
                db.chatDao().observeConversation(chatId).map { messages ->
                    requireAccount()
                    ChatConversationState(title = chat.nm.orEmpty(), loading = false,
                        messages = messages.map { row ->
                            ChatMessageItem(row.id, row.msg.orEmpty(), row.dt ?: 0L, row.dtRead ?: 0L)
                        })
                }
            }
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ _state.value = it.copy(readError = _state.value.readError) }, { error ->
                _state.value = _state.value.copy(loading = false, loadError = true)
                Globals.writeToMLOG("ERROR", "ChatConversation/load", "chatId=$chatId, error=$error")
            })
    }

    fun markVisibleRead(ids: List<Int>) {
        if (_state.value.loadError) return
        val unread = _state.value.messages.filter { it.id in ids && it.readAt <= 0 && it.id !in inFlight }
        _state.value = _state.value.copy(readError = false)
        if (unread.isEmpty()) return
        unread.forEach { message ->
            inFlight.add(message.id)
            val conversationId = chatId
            reads.add(Completable.fromAction {
                requireAccount()
                // Persist the retry job before acknowledging the read locally.
                ChatReadWorker.enqueue(getApplication(), userId, message.id).result.get()
                requireAccount()
                db.chatDao().markRead(conversationId, message.id, System.currentTimeMillis() / 1000)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ inFlight.remove(message.id) }, { error ->
                    inFlight.remove(message.id)
                    _state.value = _state.value.copy(readError = true)
                    Globals.writeToMLOG("ERROR", "ChatConversation/read", "messageId=${message.id}, error=$error")
                }))
        }
    }

    override fun onCleared() {
        history?.dispose()
        reads.dispose()
        super.onCleared()
    }
}
