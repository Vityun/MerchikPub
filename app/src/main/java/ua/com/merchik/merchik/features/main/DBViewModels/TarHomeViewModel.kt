package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.ServerExchange.PhotoDownload
import ua.com.merchik.merchik.ViewHolders.Clicks
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.toItemUI
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.realm.tables.TasksAndReclamationsRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.ItemFilterChoice
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.tar.TarListItem
import javax.inject.Inject

@HiltViewModel
class TarHomeViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    private val state: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, state) {
    override val table = TarListItem::class
    private var type = 0
    private var tarId = 0
    private var rows = emptyList<TarListItem>()
    private var subscription: Disposable? = null
    private val requestedPhotos = mutableSetOf<Int>()
    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _heading = MutableStateFlow(tarHomeHeading(type, rows))
    val heading = _heading.asStateFlow()
    var onOpen: Globals.TARInterface? = null
    var onCreateTask: Runnable? = null

    fun configure(type: Int, tarId: Int) {
        this.type = type
        this.tarId = tarId
        title = null
        _heading.value = tarHomeHeading(type, rows)
        contextUI = if (type == 1) ContextUI.TASKS_IN_CONTAINER else ContextUI.RECLAMATIONS_IN_CONTAINER
        typeWindow = "container"
        modeUI = ModeUI.DEFAULT
    }

    fun startObserving() {
        if (subscription?.isDisposed == false) return
        _loading.value = true
        _error.value = null
        subscription = RoomManager.SQL_DB.tarDao()
            .observeHome(Globals.userId, type, Clock.getDateLong(-30).time / 1000, tarId)
            .subscribeOn(Schedulers.io())
            .map { list -> list.map(TarListItem::from) }
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ updated ->
                rows = updated
                _loading.value = false
                rebuild()
                loadPhotos()
                if (tarId > 0 && state.get<Boolean>("opened") != true && rows.size == 1 && onOpen != null) {
                    state["opened"] = true
                    open(tarId)
                }
            }, { failure ->
                _loading.value = false
                _error.value = "Не вдалося завантажити список ЗІР"
                Globals.writeToMLOG("ERROR", "TarHome/list", failure.toString())
            })
    }

    fun stopObserving() {
        subscription?.dispose()
        subscription = null
    }

    override fun updateContent() {
        requestedPhotos.clear()
        stopObserving()
        startObserving()
    }

    private fun rebuild() {
        _heading.value = tarHomeHeading(type, rows)
        super.updateContent()
    }

    override fun getDefaultHideUserFields() = listOf("id")
    override fun getDefaultSortUserFields() = listOf("dt")

    override suspend fun getItems(): List<DataItemUI> {
        val app = getApplication<Application>()
        val red = Color(ContextCompat.getColor(app, R.color.red_error))
        val green = Color(ContextCompat.getColor(app, R.color.green_default))
        return rows.map { row ->
            val item = row.toItemUI(nameUIRepository, null, null)
            val statusColor = when {
                row.tp == 0 && row.state > 0 -> green
                row.state == 0 -> red
                row.state == 1 -> green
                else -> Color.Gray
            }
            item.copy(
                images = photo(row)?.photo_num?.takeIf { it.isNotBlank() }?.let { listOf(it) } ?: item.images,
                fields = item.fields.map { field ->
                    if (field.key == "state" || field.key == "amount") field.copy(
                        value = field.value.copy(modifierValue = MerchModifier(
                            textColor = if (field.key == "amount") red else statusColor
                        ))
                    ) else field
                },
                modifierContainer = MerchModifier(background = if (row.state == 0)
                    Color(ContextCompat.getColor(app, R.color.yellow)) else Color.White)
            )
        }
    }

    override fun updateFilters() {
        filters = tarHomeFilters(type, tarId, uiState.value.filters ?: Filters())
    }

    override fun onClickAdditionalContent() { onCreateTask?.run() }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        itemUI.rawObj.filterIsInstance<TarListItem>().firstOrNull()?.let { open(it.id) }
    }

    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context, index: Int) =
        onClickItem(clickedDataItemUI, context)

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        if (itemsUI.size == 1) context?.let { onClickItem(itemsUI.single(), it) }
        else context?.let { Toast.makeText(it, "Оберіть один запис ЗІР", Toast.LENGTH_SHORT).show() }
    }

    private fun open(id: Int) {
        val row = RoomManager.SQL_DB.tarDao().getById(id)
        if (row != null) onOpen?.onSuccess(row)
        else {
            context?.let { Toast.makeText(it, "Запис ЗІР більше недоступний", Toast.LENGTH_SHORT).show() }
            updateContent()
        }
    }

    private fun photo(row: TarListItem): StackPhotoDB? =
        row.photoHash.takeIf { it.isNotBlank() }?.let { StackPhotoRealm.getByHash(it) }
            ?: row.photo.takeIf { it > 0 }?.let {
                StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(it.toString()) ?: StackPhotoRealm.getById(it)
            }

    private fun loadPhotos() {
        val candidates = rows.filter { it.photo > 0 && it.photo !in requestedPhotos }
        candidates.forEach { requestedPhotos.add(it.photo) }
        val missing = candidates.filter { photo(it) == null }
        candidates.filter { photo(it) != null }.forEach(::downloadPhoto)
        if (missing.isEmpty()) return
        val request = PhotoTableRequest().apply {
            mod = "images_view"
            act = "list_image"
            nolimit = "1"
            id_list = missing.map { it.photo }.distinct().joinToString(",")
        }
        PhotoDownload().getPhotoInfoAndSaveItToDB(request, object : Clicks.clickObjectAndStatus<StackPhotoDB> {
            override fun onSuccess(data: StackPhotoDB) {
                viewModelScope.launch {
                    missing.forEach(::downloadPhoto)
                    rebuild()
                }
            }
            override fun onFailure(error: String) {
                Globals.writeToMLOG("ERROR", "TarHome/photoMetadata", error)
            }
        })
    }

    private fun downloadPhoto(row: TarListItem) {
        val photo = photo(row) ?: return
        if (!photo.photo_num.isNullOrBlank()) return
        PhotoDownload().downloadPhoto(false, photo, "/TAR", object : PhotoDownload.downloadPhotoInterface {
            override fun onSuccess(data: StackPhotoDB) { viewModelScope.launch { rebuild() } }
            override fun onFailure(error: String) {
                Globals.writeToMLOG("ERROR", "TarHome/photo", error)
            }
        })
    }

    override fun onCleared() {
        stopObserving()
        super.onCleared()
    }
}

data class TarHomeHeading(val title: String, val activeCount: Int)

internal fun tarHomeHeading(type: Int, rows: List<TarListItem>) = TarHomeHeading(
    title = if (type == 1) "Завдання" else "Рекламації",
    activeCount = rows.count { it.state == 0 }
)

internal fun tarHomeFilters(type: Int, tarId: Int, previous: Filters): Filters {
    val status = ItemFilter(
        title = "Статус", clazz = TarListItem::class, modeUI = ModeUI.ONE_SELECT,
        titleContext = "Статус", subTitleContext = "", leftField = "state", rightField = "state",
        rightValuesRaw = emptyList(), rightValuesUI = emptyList(), enabled = true,
        choices = listOf(ItemFilterChoice("all", "Усі", emptyList())) + (0..3).map {
            ItemFilterChoice(it.toString(), TasksAndReclamationsRealm.getStatusTxt(type, it))
        }, defaultChoiceKey = if (tarId > 0) "all" else "0"
    ).clearValues()
    val customer = ItemFilter(
        "Клієнт", CustomerSDB::class, CustomerSDBViewModel::class, ModeUI.MULTI_SELECT,
        "Клієнт", "", "client", "id", emptyList(), emptyList(), true
    )
    val address = ItemFilter(
        "Адреса", AddressSDB::class, AddressSDBViewModel::class, ModeUI.MULTI_SELECT,
        "Адреса", "", "addr", "id", emptyList(), emptyList(), true
    )
    return previous.copy(items = listOf(status, customer, address).map { filter ->
        previous.items.firstOrNull { it.key == filter.key } ?: filter
    })
}
