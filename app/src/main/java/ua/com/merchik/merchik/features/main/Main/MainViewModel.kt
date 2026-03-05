package ua.com.merchik.merchik.features.main.Main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.Globals.APP_OFFSET_DISTANCE_METERS
import ua.com.merchik.merchik.Globals.APP_OFFSET_SIZE_FONTS
import ua.com.merchik.merchik.Globals.APP_PREFERENCES
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading
import ua.com.merchik.merchik.data.Database.Room.WPDataAdditional
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainEvent
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.PendingAction
import ua.com.merchik.merchik.dataLayer.SelectedMode
import ua.com.merchik.merchik.dataLayer.addrIdOrNull
import ua.com.merchik.merchik.dataLayer.common.FilterAndSortResult
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.dataLayer.withContainerBackground
import ua.com.merchik.merchik.dataLayer.withGroupingOnTop
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.database.room.factory.WPDataAdditionalFactory
import ua.com.merchik.merchik.dialogs.DialogFullPhoto
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenuAction
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenuState
import ua.com.merchik.merchik.features.main.componentsUI.MessageDialogData
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass

data class StateUI(
    val title: String? = null,
    val subTitle: String? = null,
    val subTitleLong: String? = null,
    val idResImage: Int? = null,
    val itemsHeader: List<DataItemUI> = emptyList(),
    val items: List<DataItemUI> = emptyList(),
    val itemsFooter: List<DataItemUI> = emptyList(),
    val settingsItems: List<SettingsItemUI> = emptyList(),
    val settingsItemsForCard: List<SettingsItemUI> = emptyList(),
    var sortingFields: List<SortingField> = emptyList(),
    val groupingFields: List<GroupingField> = emptyList(),
    val filters: Filters? = null,
    val lastUpdate: Long = 0
)

data class Filters(
    val title: String = "Фільтри",
    val subTitle: String? = "У цій формі Ви можете налаштувати фільтри для обмеження списку елементів",
    val rangeDataByKey: RangeDate? = null,
    val searchText: String = "",
    var items: List<ItemFilter> = emptyList(),
    val selectedMode: SelectedMode = SelectedMode.ALL
)

data class ItemFilter(
    val title: String,
    val clazz: KClass<out DataObjectUI>,
    val clazzViewModel: KClass<out MainViewModel>,
    val modeUI: ModeUI,
    val titleContext: String,
    val subTitleContext: String,
    val leftField: String,
    val rightField: String,
    val rightValuesRaw: List<String?>,
    val rightValuesUI: List<String?>,
    val enabled: Boolean,
    val excludeMode: Boolean = false
) {
    fun onSelect(activity: Activity) {
        val intent = Intent(activity, FeaturesActivity::class.java)
        val bundle = Bundle()
        bundle.putString("viewModel", clazzViewModel.java.canonicalName)
        bundle.putString("modeUI", modeUI.toString())
        bundle.putString("title", titleContext)
        bundle.putString("subTitle", subTitleContext)
        intent.putExtras(bundle)
        ActivityCompat.startActivityForResult(
            activity,
            intent,
            DetailedReportActivity.NEED_UPDATE_UI_REQUEST,
            null
        )
    }
}

data class RangeDate(
    val key: String? = null,
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    val enabled: Boolean
)

data class SortingField(
    val key: String? = null,
    val title: String? = null,
    var order: Int? = null, //порядо сортировки 1 по алфавиту
    var group: Boolean = false // Группировать по этому полю или нет
)

data class SettingsUI(
    val hideFields: List<String>? = null,
    val sortFields: List<SortingField>? = null,
    val groupFields: List<GroupingField>? = null,
    val sizeFonts: Int? = null
)

data class GroupingField(
    val key: String? = null,         // по какому полю группируем (как в FieldValue.key)
    val title: String? = null,      // заголовок для UI (например "По дате", "По магазину")
    val priority: Int = 0,          // порядок применения, если группировок несколько
    val collapsedByDefault: Boolean = true // свернута ли группа по умолчанию
)

data class GroupMeta(
    val groupKey: String,           // ключ группы (например "2025-11-19" или "Київ")
    val title: String? = null,      // что показывать в заголовке группы
    val startIndex: Int,            // индекс первого элемента группы в result.items
    val endIndexExclusive: Int      // индекс после последнего (как в subList)
)


abstract class MainViewModel(
    application: Application,
    val repository: MainRepository,
    val nameUIRepository: NameUIRepository,
    protected val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)

    private val _offsetSizeFonts = MutableStateFlow(0.0f)
    val offsetSizeFonts: StateFlow<Float> = _offsetSizeFonts

    private val _offsetDistanceMeters = MutableStateFlow(5_000.0f)
    val offsetDistanceMeters: StateFlow<Float> = _offsetDistanceMeters

    private val _valueForCustomResult = MutableStateFlow(HashMap<String, Any?>())
    val valueForCustomResult: StateFlow<HashMap<String, Any?>> = _valueForCustomResult

    private val _scrollToHash = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val scrollToHash: SharedFlow<Long> = _scrollToHash

    private val _expandGroup = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val expandGroup: SharedFlow<String> = _expandGroup


    // внутреннее «ожидаемое действие» после подтверждения диалога
    private sealed interface PendingOp {
        data class AcceptOneTime(val wp: WpDataDB) : PendingOp
        data class AcceptInfinite(val wp: WpDataDB) : PendingOp
        data class AcceptAllClientOneAddressInfinite(val wp: WpDataDB) : PendingOp
        data class AcceptAllClientOneAddressOneTime(val wp: WpDataDB) : PendingOp
    }

    private var pending: PendingOp? = null


    var context: Context? = null
    var dataJson: String? = null
    var title: String? = null
    var typeWindow: String? = null
    var subTitle: String? = null
    var subTitleLong: String? = null
    var idResImage: Int? = null
    var modeUI: ModeUI = ModeUI.DEFAULT
    var contextUI: ContextUI = ContextUI.DEFAULT
    var launcher: ActivityResultLauncher<Intent>? = null

    abstract val table: KClass<out DataObjectUI>

    open suspend fun getItemsHeader(): List<DataItemUI> = emptyList()

    open fun getItemsFooter(): List<DataItemUI> = emptyList()

    open fun updateFilters() {}

    abstract suspend fun getItems(): List<DataItemUI>

    open fun onClickAdditionalContent() {}

    open fun onClickItem(itemUI: DataItemUI, context: Context) {}

    open fun onLongClickItem(itemUI: DataItemUI, context: Context) {}

    open fun onClickFullImage(stackPhotoDB: StackPhotoDB, comment: String?) {}

    open fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {}

    open fun getFieldsForCommentsImage(): List<String>? {
        return null
    }

    open fun getFieldsForCustomResult(): List<String>? {
        return null
    }

    open fun getDefaultHideUserFields(): List<String>? {
        return null
    }

    open fun getDefaultSortUserFields(): List<String>? {
        return null
    }

    open fun getDefaultGroupUserFields(): List<String> = emptyList()

    open fun getDefaultHideFieldsForCards(): List<String>? {
        return null
    }

    var filters: Filters? = null

    protected var dialog: DialogFullPhoto? = null


    open fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context) {
        onClickItemImage(clickedDataItemUI, context, 0) // Делегируем вызов новому методу
    }

    open fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context, index: Int) {
        if (index != 0) return

        dialog = DialogFullPhoto(context)

        val photoLogData = mutableListOf<StackPhotoDB>()
        var selectedIndex = -1

        val fieldsForCommentsImage = getFieldsForCommentsImage()
        val fieldsForCustomResult = getFieldsForCustomResult()

        val photoDBWithComments = HashMap<StackPhotoDB, String>()
        val photoDBWithRawObj = HashMap<StackPhotoDB, Any>()

        _uiState.value.items.forEach { dataItemUI ->
            val obj = dataItemUI.rawObj.firstOrNull() ?: return@forEach
            val jsonObject = JSONObject(Gson().toJson(obj))

            var comments = ""
            fieldsForCommentsImage?.forEach { k ->
                comments += "${jsonObject.opt(k)} \n\n"
            }

            if (clickedDataItemUI == dataItemUI) {
                fieldsForCustomResult?.forEach { k ->
                    _valueForCustomResult.value[k] = jsonObject.opt(k)
                }
            }

            val photo = resolvePhotoDbForItem(obj, index) ?: return@forEach

            photoDBWithComments[photo] = comments
            photoDBWithRawObj[photo] = obj
            photoLogData.add(photo)

            if (clickedDataItemUI == dataItemUI) {
                selectedIndex = photoLogData.size - 1
            }
        }

        if (selectedIndex > -1) {
            Log.e("!!!!!!!!!!!","+++++++++++++++")
            dialog?.setPhotos(
                selectedIndex,
                photoLogData,
                { _, photoDB ->
                    onClickFullImage(photoDB, photoDBWithComments[photoDB])
                    dialog?.dismiss()
                    dialog = null
                },
                { /* updateContent если надо */ }
            )

            dialog?.setClose {
                dialog?.dismiss()
                dialog = null
            }

            dialog?.show()
        }
    }

    private fun resolvePhotoDbForItem(obj: Any, index: Int): StackPhotoDB? {
        // если это уже StackPhotoDB (частый кейс для журнала фото) — просто вернём его
        if (obj is StackPhotoDB) return obj

        val json = JSONObject(Gson().toJson(obj))

        val imageKeys = (obj as? DataObjectUI)?.getFieldsImageOnUI()
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()

        // 1) основной id по индексу
        val idKey = imageKeys.getOrNull(index)
        val photoId = idKey?.let { json.opt(it)?.toString()?.trim() }.orEmpty()

        var photo: StackPhotoDB? = null
        if (photoId.isNotEmpty() && photoId != "0" && photoId != "null") {
            photo = RealmManager.getPhotoById(null, photoId)
        }

        // 2) fallback по hash (если id ещё нет)
        if (photo == null) {
            val hash = json.opt("photo_hash")?.toString()?.trim().orEmpty()
            if (hash.length > 12 && hash != "0" && hash != "null") {
                photo = RealmManager.getPhotoByHash(hash)
            }
        }

        return photo
    }


    private val _uiState = MutableStateFlow(StateUI())
    val uiState
            : StateFlow<StateUI>
        get() = _uiState.asStateFlow()


    private val _rangeDataStart = MutableStateFlow<LocalDate?>(LocalDate.now().minusDays(1))
    val rangeDataStart
            : StateFlow<LocalDate?>
        get() = _rangeDataStart.asStateFlow()


    private val _rangeDataEnd = MutableStateFlow<LocalDate?>(LocalDate.now().plusDays(4))
    val rangeDataEnd
            : StateFlow<LocalDate?>
        get() = _rangeDataEnd.asStateFlow()

    private val _showMenuDialog = MutableStateFlow(false)
    val showMenuDialog
            : StateFlow<Boolean>
        get() = _showMenuDialog.asStateFlow()


    protected val _events = MutableSharedFlow<MainEvent>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    private val _dataItems = MutableStateFlow<List<DataItemUI>>(emptyList())
    val dataItems: StateFlow<List<DataItemUI>> = _dataItems.asStateFlow()

    private val _groups = MutableStateFlow<List<GroupMeta>>(emptyList())
    val groups: StateFlow<List<GroupMeta>> get() = _groups


    private val _kostilDialog = MutableStateFlow<Boolean>(false)
    val kostilDialog: StateFlow<Boolean> get() = _kostilDialog

    fun requestExpandGroup(groupId: String) {
        _expandGroup.tryEmit(groupId)
    }

    fun requestScrollToVisit(itemHash: Long) {
        _scrollToHash.tryEmit(itemHash)
    }

    fun setStartDate(date: LocalDate) {
        _rangeDataStart.value = date

        // обнови filters, если используешь копию
        val current = _uiState.value.filters
        if (current != null) {
            val updated = current.copy(
                rangeDataByKey = current.rangeDataByKey?.copy(start = date)
            )
            _uiState.value = _uiState.value.copy(filters = updated)
        }
    }

    fun setEndDate(date: LocalDate) {
        _rangeDataEnd.value = date

        val current = _uiState.value.filters
        if (current != null) {
            val updated = current.copy(
                rangeDataByKey = current.rangeDataByKey?.copy(end = date)
            )
            _uiState.value = _uiState.value.copy(filters = updated)
        }
    }

    fun showKostilDialog() {
        _kostilDialog.value = true
    }

    fun hideKostilDialog() {
        _kostilDialog.value = false
    }

    private var planBudgetPollingJob: Job? = null
    private var loadingUnloading: TablesLoadingUnloading = TablesLoadingUnloading()


    init {
        loadPreferences()
        observeSourcesForItems()
    }

    /** Запустить polling (если уже запущен — ничего не делает) */
    fun startPlanBudgetPolling(context: Context) {
        if (planBudgetPollingJob?.isActive == true) return

        planBudgetPollingJob = viewModelScope.launch(Dispatchers.IO) {
            // можно сразу сделать первый запрос без ожидания
            while (isActive) {
                val startedAt = System.currentTimeMillis()

                try {
                    // защита от зависаний (по желанию)
                    withTimeout(15_000) {
                        loadingUnloading.donwloadPlanBudgetForConfirmDecision(
                            context as Activity,
                            kotlinx.coroutines.Runnable {
                                updateContent()
                            }
                        )
                    }
                } catch (ce: CancellationException) {
                    throw ce
                } catch (t: Throwable) {
                    Log.e("PlanBudgetPolling", "poll error", t)
                }

                // держим период ~10с с учётом времени запроса
                val elapsed = System.currentTimeMillis() - startedAt
                val delayMs = (10_000 - elapsed).coerceAtLeast(0)
                delay(delayMs)
            }
        }
    }

    fun startPlanBudgetPollingSecond(wpDataAdditional: List<WPDataAdditional>) {
        viewModelScope.launch(Dispatchers.IO) {
            tablesLoadingUnloading.donwloadPlanBudgetForConfirmDecision(context as Activity, wpDataAdditional) {
                updateContent()
            }
        }
    }


        private fun loadPreferences() {
        _offsetSizeFonts.value = sharedPreferences.getFloat(APP_OFFSET_SIZE_FONTS, 0f)
        _offsetDistanceMeters.value = sharedPreferences.getFloat(APP_OFFSET_DISTANCE_METERS, 5_000f)
    }

    private fun observeSourcesForItems() {
        viewModelScope.launch {
            // комбинируем источники: uiState, range start/end
            combine(
                _uiState,
                _rangeDataStart,
                _rangeDataEnd
            ) { uiState, start, end ->
                Triple(uiState, start, end)
            }.collect { (uiState, start, end) ->
                // делаем пересчёт в фоне
                recomputeDataItems(uiState, start, end)
            }
        }
    }

    private fun recomputeDataItems(
        uiState: StateUI,
        rangeStart: LocalDate?,
        rangeEnd: LocalDate?
    ) {
        viewModelScope.launch {
            val (combined, shiftedGroups) = withContext(Dispatchers.Default) {
                val header = uiState.itemsHeader
                val footer = uiState.itemsFooter

                val result: FilterAndSortResult = try {
                    filterAndSortDataItems(
                        items = uiState.items,
                        filters = uiState.filters,
                        sortingFields = uiState.sortingFields,
                        groupingFields = uiState.groupingFields,
                        rangeStart = rangeStart,
                        rangeEnd = rangeEnd,
                        searchText = uiState.filters?.searchText
                    )
                } catch (e: Throwable) {
                    Globals.writeToMLOG(
                        "ERROR",
                        "MainViewModel.recomputeDataItems",
                        "filterAndSortDataItems failed: $e"
                    )
                    FilterAndSortResult(
                        items = emptyList(),
                        groups = emptyList(),
                        isActiveFiltered = false,
                        isActiveSorted = false,
                        isActiveGrouped = false
                    )
                }

                // 👉 сдвигаем индексы групп на размер header
                val headerSize = header.size
                val groupsWithOffset: List<GroupMeta> = result.groups.map { g ->
                    g.copy(
                        startIndex = g.startIndex + headerSize,
                        endIndexExclusive = g.endIndexExclusive + headerSize
                    )
                }

                // Собираем итоговый список для UI
                val combinedList = buildList {
                    addAll(header)
                    addAll(result.items)
                    addAll(footer)
                }

                combinedList to groupsWithOffset
            }

            // обновляем элементы
            if (_dataItems.value != combined) {
                _dataItems.value = combined
            }

            // обновляем группы (для GroupDeck / отрисовки)
            if (_groups.value != shiftedGroups) {
                _groups.value = shiftedGroups
            }
        }
    }


    fun updateOffsetSizeFonts(offsetSizeFont: Float) {
        viewModelScope.launch {
            sharedPreferences.edit().putFloat(APP_OFFSET_SIZE_FONTS, offsetSizeFont).apply()
            _offsetSizeFonts.value = offsetSizeFont
        }
    }

    fun updateOffsetDistanceMeters(offsetDistanceMeters: Float) {
        viewModelScope.launch {
            sharedPreferences.edit().putFloat(APP_OFFSET_DISTANCE_METERS, offsetDistanceMeters)
                .apply()
            _offsetDistanceMeters.value = offsetDistanceMeters
        }
    }

    fun getTranslateString(text: String, translateId: Long? = null) =
        nameUIRepository.getTranslateString(text, translateId)

    fun saveSettings() {
        viewModelScope.launch {
            repository.saveSettingsUI(
                table,
                SettingsUI(
                    hideFields = uiState.value.settingsItems.filter { !it.isEnabled }
                        .map { it.key },
                    sortFields = uiState.value.sortingFields.filter { it.key != null }
                        .map { it.copy(title = null) }
                ),
                contextUI)
        }
    }

//    fun updateSearch(text: String) {
//        _uiState.update {
//            it.copy(
//                filters = Filters(
//                    searchText = text
//                )
//            )
//        }
//    }

    fun updateSorting(newSortingField: SortingField?, position: Int) {
        viewModelScope.launch {
            val newSortingFields = mutableListOf<SortingField>()
            newSortingFields.addAll(_uiState.value.sortingFields)
            newSortingField?.let {
                if (position < newSortingFields.size) newSortingFields[position] = it
                else newSortingFields.add(position, it)
            } ?: run {
                if (position < newSortingFields.size) newSortingFields[position] =
                    SortingField()
            }
            _uiState.update {
                it.copy(
                    sortingFields = newSortingFields
                )
            }
        }
    }

    fun updateGrouping(newGroupingField: GroupingField?, position: Int) {
        viewModelScope.launch {
            val newGroupingFields = mutableListOf<GroupingField>()
            newGroupingFields.addAll(_uiState.value.groupingFields)
            newGroupingField?.let {
                if (position < newGroupingFields.size) newGroupingFields[position] = it
                else newGroupingFields.add(position, it)
            } ?: run {
                if (position < newGroupingFields.size) newGroupingFields[position] =
                    GroupingField()
            }
            _uiState.update {
                it.copy(
                    groupingFields = newGroupingFields
                )
            }
        }
    }


    fun updateContent() {
        viewModelScope.launch {
            Log.e("FILTERS_APPLY",
                "search=${filters?.searchText}, itemsSelected=${
                    filters?.items?.sumOf { it.rightValuesRaw.size } ?: 0
                }"
            )

            val list = getDefaultHideUserFields()
            val settingsItems = repository.getSettingsItemList(table, contextUI, list, modeUI)

            val defaultSort = getDefaultSortUserFields()

            val itemsHeader = getItemsHeader()
            val itemsFooter = getItemsFooter()

            val hideFieldsForCards = getDefaultHideFieldsForCards()
            val settingsForCardsItems =
                repository.getSettingsItemList(table, contextUI, hideFieldsForCards, modeUI)

            // 1) Берём сортировки
            val sortingFieldsFromRepo = repository.getSortingFields(table, contextUI, defaultSort)

            // 2) Дефолтные ключи группировки
            val defaultGroupKeys: List<String> = getDefaultGroupUserFields()
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            // 3) Есть ли вообще сохранённые сортировки у пользователя в БД?
            val hasUserSorting: Boolean = repository.hasUserSorting(table, contextUI)

            // 4) Есть ли включённая группировка в том, что пришло из репозитория?
            val hasUserGrouping: Boolean = sortingFieldsFromRepo.any {
                it.group && !it.key.isNullOrBlank()
            }

            // 5) Применяем дефолтную группировку
            val sortingFields: List<SortingField> =
                if (!hasUserSorting && !hasUserGrouping && defaultGroupKeys.isNotEmpty()) {
                    // 👆 Только если НЕТ пользовательских сортировок (первый запуск)
                    sortingFieldsFromRepo.map { sf ->
                        val k = sf.key
                        if (!k.isNullOrBlank() &&
                            defaultGroupKeys.any { def -> def.equals(k, ignoreCase = true) }
                        ) {
                            sf.copy(group = true)
                        } else {
                            sf
                        }
                    }
                } else {
                    // Есть пользовательские настройки -> уважаем их, ничего не навязываем
                    sortingFieldsFromRepo
                }

            // дальше твой код без изменений
            val groupingFields: List<GroupingField> = sortingFields
                .mapIndexedNotNull { index, sf ->
                    sf.takeIf { it.group && !it.key.isNullOrBlank() }?.let {
                        GroupingField(
                            key = it.key!!,
                            title = it.title,
                            priority = index,
                            collapsedByDefault = true
                        )
                    }
                }

            updateFilters()

            val groupingKeys: List<String> = sortingFields
                .filter { it.group && !it.key.isNullOrBlank() }
                .map { it.key!! }

            _uiState.update { old ->
                val titleResolved = title?.split(",")?.map { it.trim() }?.let {
                    it[0].toIntOrNull()?.let { intRes ->
                        context?.let { cont ->
                            getTranslateString(
                                cont.getString(intRes),
                                it[1].toLongOrNull()
                            )
                        }
                    }
                } ?: title

                val dataItemUIS = getItems()

                val selectedIds: Set<Long> = buildSet {
                    addAll(old.items.filter { it.selected }.map { it.stableId })
                    addAll(old.itemsHeader.filter { it.selected }.map { it.stableId })
                    addAll(old.itemsFooter.filter { it.selected }.map { it.stableId })
                }

                val dataWithRestoredSelected =
                    if (selectedIds.isEmpty()) {
                        dataItemUIS
                    } else {
                        dataItemUIS.map { item ->
                            item.copy(selected = item.stableId in selectedIds)
                        }
                    }

                val selectedMode = old.filters?.selectedMode ?: SelectedMode.ALL
                val shouldApply = (modeUI == ModeUI.FILTER_SELECT || modeUI == ModeUI.MULTI_SELECT)

                val filteredSelectedItems = if (!shouldApply) {
                    dataWithRestoredSelected
                } else {
                    when (selectedMode) {
                        SelectedMode.ALL -> dataWithRestoredSelected
                        SelectedMode.ONLY_SELECTED -> dataWithRestoredSelected.filter { it.selected }
                        SelectedMode.ONLY_UNSELECTED -> dataWithRestoredSelected.filter { !it.selected }
                    }
                }


                val finalItems = if (groupingKeys.isEmpty()) {
                    filteredSelectedItems
                } else {
                    filteredSelectedItems.map { it.withGroupingOnTop(groupingKeys) }
                }

                android.util.Log.e(
                    "TEST_BAG1",
                    "value ${RoomManager.SQL_DB.siteObjectsDao().all.size}"
                )

                finalItems.forEach {
                    android.util.Log.e("TEST_BAG", "value ${it.fields.first().field}")
                }

                old.copy(
                    title = titleResolved,
                    subTitle = subTitle,
                    subTitleLong = subTitleLong,
                    idResImage = idResImage,
                    items = finalItems,
                    itemsHeader = itemsHeader,
                    itemsFooter = itemsFooter,
                    settingsItems = settingsItems,
                    settingsItemsForCard = settingsForCardsItems,
                    sortingFields = sortingFields,
                    groupingFields = groupingFields,
                    filters = filters,
                    lastUpdate = System.currentTimeMillis()
                )
            }
        }
    }

    fun updateSubtitle(short: String?, long: String?) {
        _uiState.update { it.copy(subTitle = short, subTitleLong = long) }
    }


    // ViewModel
    private val _flyRequests = MutableSharedFlow<Long>(replay = 0, extraBufferCapacity = 10)
    val flyRequests = _flyRequests.asSharedFlow()

    fun requestFlyByStableId(stableId: Long) {
        viewModelScope.launch {
            _flyRequests.emit(stableId)
        }
    }

    fun updateFilters(filter: Filters) {
        this.filters = filter
//        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    filters = filter,
                    lastUpdate = System.currentTimeMillis()
                )
            }
//        }
    }

    fun updateItemsSelect(ids: List<Long>, checked: Boolean) {
        val set = ids.toHashSet()

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    itemsHeader = state.itemsHeader.map { old ->
                        old.copy(
                            selected =
                                if (old.stableId in set) checked
                                else if (modeUI == ModeUI.ONE_SELECT) false else old.selected
                        )
                    },
                    items = state.items.map { old ->
                        old.copy(
                            selected =
                                if (old.stableId in set) checked
                                else if (modeUI == ModeUI.ONE_SELECT) false else old.selected
                        )
                    },
                    itemsFooter = state.itemsFooter.map { old ->
                        old.copy(
                            selected =
                                if (old.stableId in set) checked
                                else if (modeUI == ModeUI.ONE_SELECT) false else old.selected
                        )
                    },
                )
            }
        }
    }


    fun updateItemSelect(checked: Boolean, itemUI: DataItemUI) {
        val targetId = itemUI.stableId

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    itemsHeader = state.itemsHeader.map { old ->
                        old.copy(
                            selected =
                                if (old.stableId == targetId) checked
                                else if (modeUI == ModeUI.ONE_SELECT) false else old.selected
                        )
                    },
                    items = state.items.map { old ->
                        old.copy(
                            selected =
                                if (old.stableId == targetId) checked
                                else if (modeUI == ModeUI.ONE_SELECT) false else old.selected
                        )
                    },
                    itemsFooter = state.itemsFooter.map { old ->
                        old.copy(
                            selected =
                                if (old.stableId == targetId) checked
                                else if (modeUI == ModeUI.ONE_SELECT) false else old.selected
                        )
                    },
                )
            }
        }
    }

    fun updateSelectedModeFilter(mode: SelectedMode) {
        _uiState.update { state ->
            val f = state.filters ?: Filters()
            state.copy(filters = f.copy(selectedMode = mode))
        }
    }

    /** Универсальный массовый апдейт */
    fun highlightWhere(predicate: (DataItemUI) -> Boolean, color: Color?) {
        _uiState.update { st ->
            st.copy(items = st.items.map { if (predicate(it)) it.withContainerBackground(color) else it })
        }
    }

    /** Подкрасить все элементы с данным addr_id */
    fun highlightByAddrId(addrId: String, color: Color) {
        val target = addrId.trim()
        highlightWhere(
            predicate = { it.addrIdOrNull() == target },
            color = color
        )
    }

    /** Подкрасить элемент по ID */
    fun highlightBId(id: Long, color: Color) {
        highlightWhere(
            predicate = { it.stableId == id },
            color = color
        )
    }

    /** Пользователь выбрал "Принять заказ (разово)" -> показать подтверждение */
    fun requestAcceptOneTime(wp: WpDataDB) {
        pending = PendingOp.AcceptOneTime(wp)
        viewModelScope.launch {
            _events.emit(
                MainEvent.ShowMessageDialog(
                    MessageDialogData(

                        message = String.format(
                            "\"Виконати поточну роботу\n" +
                                    "\"<font color='gray'>Відвідування від</font> %s\n" +
                                    "\"<br><font color='gray'>Клієнт:</font> %s\n" +
                                    "\"<br><font color='gray'>Адреса:</font> %s\n" +
                                    "\"<br><font color='gray'>Премія (план):</font> %s грн.\n" +
                                    "\"<br><font color='gray'>СКЮ (кількість товарних позицій):</font> %s\n" +
                                    "\"<br><font color='gray'>Середній час роботи:</font> %s хв\n",
                            Clock.getHumanTime_dd_MMMM(wp.dt.time),
                            wp.client_txt,
                            wp.addr_txt,
                            wp.cash_ispolnitel,
                            wp.sku,
                            wp.duration
                        ),
                        status = DialogStatus.NORMAL
                    )
                )
            )
        }
    }

    /** Пользователь выбрал "Принять заказ (постоянно)" -> показать подтверждение */
    fun requestAcceptInfinite(wp: WpDataDB) {
        pending = PendingOp.AcceptInfinite(wp)
        viewModelScope.launch {
            _events.emit(
                MainEvent.ShowMessageDialog(
                    MessageDialogData(
                        message = "Виконувати всі роботи цього клієнта за цією адресою\n" +
                                "<font color='gray'>Посещение от</font> ${
                                    Clock.getHumanTime_dd_MMMM(
                                        wp.dt.time
                                    )
                                }" +
                                "\n<font color='gray'>Клиент:</font> ${wp.client_txt}" +
                                "\n<font color='gray'>Адрес:</font> ${wp.addr_txt}",
                        status = DialogStatus.NORMAL
                    )
                )
            )
        }
    }

    /** Пользователь выбрал "Принять заказ всех клиентов (разово)" -> показать подтверждение */
    fun requestAcceptAllWorkOneTime(wp: WpDataDB) {
        pending = PendingOp.AcceptAllClientOneAddressOneTime(wp)
        viewModelScope.launch {
            _events.emit(
                MainEvent.ShowMessageDialog(
                    MessageDialogData(
                        message = "Виконати поточні роботи за сьогодні\n" +
                                "<font color='gray'>Посещение от</font> ${
                                    Clock.getHumanTime_dd_MMMM(
                                        wp.dt.time
                                    )
                                }" +
                                "\n<font color='gray'>Клиенты:</font> ${
                                    WPDataAdditionalFactory.getUniqueClientIdsForAddr_TXT(
                                        wp.addr_id,
                                        wp.dt
                                    ).toString().removeSurrounding("[", "]")
                                }" +
                                "\n<font color='gray'>Адрес:</font> ${wp.addr_txt}",
                        status = DialogStatus.NORMAL
                    )
                )
            )
        }
    }

    /** Пользователь выбрал "Принять заказ всех клиентов (постоянно)" -> показать подтверждение */
    fun requestAcceptAllWorkInfinite(wp: WpDataDB) {
        pending = PendingOp.AcceptAllClientOneAddressInfinite(wp)
        viewModelScope.launch {
            _events.emit(
                MainEvent.ShowMessageDialog(
                    MessageDialogData(
                        message = "Виконувати всі роботи доступні за цією адресою\n" +
                                "<font color='gray'>Посещение от</font> ${
                                    Clock.getHumanTime_dd_MMMM(
                                        wp.dt.time
                                    )
                                }" +
                                "\n<font color='gray'>Клиенты:</font> ${
                                    WPDataAdditionalFactory.getUniqueClientIdsForAddr_TXT(
                                        wp.addr_id
                                    ).toString().removeSurrounding("[", "]")
                                }" +
                                "\n<font color='gray'>Адрес:</font> ${wp.addr_txt}",
                        status = DialogStatus.NORMAL
                    )
                )
            )
        }
    }

    /** Обе кнопки диалога вызывают это действие */
    fun performPending() {
        val op = pending ?: return
        when (op) {
            is PendingOp.AcceptOneTime -> doAcceptOneTime(op.wp)
            is PendingOp.AcceptInfinite -> doAcceptInfinite(op.wp)
            is PendingOp.AcceptAllClientOneAddressOneTime -> doAcceptAllClientOneAddressOneTime(op.wp)
            is PendingOp.AcceptAllClientOneAddressInfinite -> doAcceptAllClientOneAddressInfinite(op.wp)
        }
    }

    fun cancelPending() {
        pending = null
    }

    private val tablesLoadingUnloading = TablesLoadingUnloading()

    private fun doAcceptAllClientOneAddressInfinite(wp: WpDataDB) {
        val dao = RoomManager.SQL_DB.wpDataAdditionalDao()

        val d = dao.getByAddr(wp.addr_id)
            .subscribeOn(Schedulers.io())
            .flatMap { list ->
                if (list.isEmpty()) {
//                    ###############
//                    поменять на всех клиентов. В цикле перебрать по адресам
                    dao.insertAll(WPDataAdditionalFactory.withAllClientForAddressInfinite(wp))
                        .andThen(Single.just(true))
                } else Single.just(false)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { inserted ->
                    pending = null
                    viewModelScope.launch {
                        // временный костыль
                        tablesLoadingUnloading.uploadPlanBudget()

                        _events.emit(
                            MainEvent.ShowMessageDialog(
                                MessageDialogData(
                                    message = if (inserted) "Заявка на выполнение работ создана и передана куратору, в течении нескольких минут вы получите ответ. Если ответ будет положительный это посещение будет перенесено в план работ"
                                    else "Запрос на работы по этому посещению уже подан, как только куратор даст ответ вы получите уведомление",
                                    status = if (inserted) DialogStatus.NORMAL else DialogStatus.ALERT
                                )
                            )
                        )
                    }
                },
                { e ->
                    pending = null
                    viewModelScope.launch {
                        _events.emit(
                            MainEvent.ShowMessageDialog(
                                MessageDialogData(
                                    message = "Ваша заявка создана, однако, для того чтобы ее подтвердить выполните обмен с сервером",
                                    status = DialogStatus.ALERT,
                                    positivText = "Синхронизация"
                                )
                            )
                        )
                    }
                }
            )
        disposables.add(d)
    }

    private fun doAcceptAllClientOneAddressOneTime(wp: WpDataDB) {
        val dao = RoomManager.SQL_DB.wpDataAdditionalDao()

        val d = dao.getByAddr(wp.addr_id)
            .subscribeOn(Schedulers.io())
            .flatMap { list ->
                if (list.isEmpty()) {
//                    ###############
//                    поменять на всех клиентов. В цикле перебрать по адресам
                    dao.insertAll(WPDataAdditionalFactory.withAllClientForAddressOneTime(wp))
                        .andThen(Single.just(true))
                } else Single.just(false)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { inserted ->
                    pending = null
                    viewModelScope.launch {
                        // временный костыль
                        tablesLoadingUnloading.uploadPlanBudget()

                        _events.emit(
                            MainEvent.ShowMessageDialog(
                                MessageDialogData(
                                    message = if (inserted) "Заявка на выполнение работ создана и передана куратору, в течении нескольких минут вы получите ответ. Если ответ будет положительный это посещение будет перенесено в план работ"
                                    else "Запрос на работы по этому посещению уже подан, как только куратор даст ответ вы получите уведомление",
                                    status = if (inserted) DialogStatus.NORMAL else DialogStatus.ALERT
                                )
                            )
                        )
                    }
                },
                { e ->
                    pending = null
                    viewModelScope.launch {
                        _events.emit(
                            MainEvent.ShowMessageDialog(
                                MessageDialogData(
                                    message = "Ваша заявка создана, однако, для того чтобы ее подтвердить выполните обмен с сервером",
                                    status = DialogStatus.ALERT,
                                    positivText = "Синхронизация"
                                )
                            )
                        )
                    }
                }
            )
        disposables.add(d)
    }

     fun doAcceptOneTime(wp: WpDataDB) {
        val dad2 = wp.code_dad2
        val dao = RoomManager.SQL_DB.wpDataAdditionalDao()

        val d = dao.getByCodeDad2(dad2)
            .subscribeOn(Schedulers.io())
            .flatMap { list ->
                if (list.isEmpty()) {
                    dao.insert(WPDataAdditionalFactory.blankWithDad2(wp))
                        .andThen(Single.just(true))
                } else {
                    Single.just(false)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { inserted ->
                    pending = null

                    if (!inserted) {
                        // было и остаётся: заявка уже подана
                        viewModelScope.launch {
                            _events.emit(
                                MainEvent.ShowMessageDialog(
                                    MessageDialogData(
                                        message = "Запит на роботи з цього відвідування вже подано, як тільки куратор дасть відповідь ви отримаєте повідомлення",
                                        status = DialogStatus.ALERT,
                                    )
                                )
                            )
                        }
                        return@subscribe
                    }

                    // новая логика: вместо диалога показываем лоадер и реально ждём ответ
                    viewModelScope.launch {
                        _events.emit(
                            MainEvent.ShowLoading(
                                "Чекаємо на відповідь від сервера",
                                28_700L
                            )
                        )
                    }

                    // 1) обмен
                    val uploadDisp = tablesLoadingUnloading
                        .uploadPlanBudgetRx()  // новая Rx-версия (ниже)
                        .timeout(35, java.util.concurrent.TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { _ ->
                                // 2) ожидание решения в БД до 28.7 сек
                                viewModelScope.launch {
                                    val result = waitDecisionByDad2(dad2, timeoutMs = 28_700L)

                                    when (result) {
                                        DecisionResult.APPROVED -> {
                                            _events.emit(MainEvent.LoadingCompleted)
                                            // если нужно — можно короткий toast/snack без диалога
                                            val wpDataAdditional = withContext(Dispatchers.IO) {
                                                RoomManager.SQL_DB.wpDataAdditionalDao()
                                                    .getByCodeDad2Sync(dad2)
                                            }

                                            context?.let { startPlanBudgetPollingSecond(wpDataAdditional) }

//                                            _events.emit(
//                                                MainEvent.ShowMessageDialog(
//                                                    MessageDialogData(
//                                                        subTitle = "Відповідь від сервера",
//                                                        message = wpDataAdditional.first().comment.takeIf { it.isNotBlank() }
//                                                            ?: "Ваша заявка создана, однако, для того чтобы ее подтвердить выполните обмен с сервером",
//                                                        status = DialogStatus.NORMAL
//                                                    )
//                                                )
//                                            )

                                        }

                                        DecisionResult.DECLINED -> {
                                            _events.emit(MainEvent.LoadingCanceled)

                                            val comment = withContext(Dispatchers.IO) {
                                                RoomManager.SQL_DB.wpDataAdditionalDao()
                                                    .getLastCommentByDad2Sync(dad2)
                                            }

                                            _events.emit(
                                                MainEvent.ShowMessageDialog(
                                                    MessageDialogData(
                                                        subTitle = "Відповідь від сервера",
                                                        message = comment?.takeIf { it.isNotBlank() }
                                                            ?: "Заявка відхилена.",
                                                        status = DialogStatus.ALERT
                                                    )
                                                )
                                            )
                                        }

                                        DecisionResult.PENDING_TIMEOUT -> {
                                            // ответа пока нет — но заявка отправлена
                                            _events.emit(MainEvent.LoadingCompleted)
                                            // при желании можно показать мягкое сообщение:
                                            // _events.emit(MainEvent.ShowMessageDialog(...))
                                        }
                                    }
                                }
                            },
                            { err ->
                                viewModelScope.launch {
                                    _events.emit(MainEvent.LoadingCanceled)
                                    _events.emit(
                                        MainEvent.ShowMessageDialog(
                                            MessageDialogData(
                                                message = "Ваша заявка створена, однак, для того, щоб її підтвердити виконайте обмін із сервером",
                                                status = DialogStatus.ALERT,
                                                positivText = "Синхронізація"
                                            )
                                        )
                                    )
                                }
                            }
                        )

                    disposables.add(uploadDisp)
                },
                { _ ->
                    pending = null
                    viewModelScope.launch {
                        _events.emit(
                            MainEvent.ShowMessageDialog(
                                MessageDialogData(
                                    message = "Ваша заявка створена, однак, для того, щоб її підтвердити виконайте обмін із сервером",
                                    status = DialogStatus.ALERT,
                                    positivText = "Синхронізація"
                                )
                            )
                        )
                    }
                }
            )

        disposables.add(d)
    }

    private enum class DecisionResult { APPROVED, DECLINED, PENDING_TIMEOUT }

    private suspend fun waitDecisionByDad2(dad2: Long, timeoutMs: Long): DecisionResult {
        val dao = RoomManager.SQL_DB.wpDataAdditionalDao()

        val res: DecisionResult? = kotlinx.coroutines.withTimeoutOrNull(timeoutMs) {
            var out: DecisionResult? = null

            while (out == null) {
                when (dao.getLastActionByDad2(dad2) ?: 0) {
                    1 -> out = DecisionResult.APPROVED
                    2 -> out = DecisionResult.DECLINED
                    else -> kotlinx.coroutines.delay(1200)
                }
            }
            out // <-- последняя строка лямбды, точно DecisionResult
        }
        return res ?: DecisionResult.PENDING_TIMEOUT
    }


//    private fun doAcceptOneTime(wp: WpDataDB) {
//        val dad2 = wp.code_dad2
//        val dao = RoomManager.SQL_DB.wpDataAdditionalDao()
//        val d = dao.getByCodeDad2(dad2)
//            .subscribeOn(Schedulers.io())
//            .flatMap { list ->
//                if (list.isEmpty()) {
//                    dao.insert(WPDataAdditionalFactory.blankWithDad2(wp)).andThen(Single.just(true))
//                } else Single.just(false)
//            }
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(
//                { inserted ->
//                    pending = null
//                    viewModelScope.launch {
//// временный костыль
//                        tablesLoadingUnloading.uploadPlanBudget()
//
//                        Globals.writeToMLOG(
//                            "INFO", "MainViewModel.doAcceptOneTime",
//                            "Updated: +"
//                        )
//
//                        _events.emit(
//                            MainEvent.ShowMessageDialog(
//                                MessageDialogData(
//                                    message = if (inserted) "Заявка на выполнение работ создана и передана куратору, в течении нескольких минут вы получите ответ. Если ответ будет положительный это посещение будет перенесено в план работ"
//                                    else "Запрос на работы по этому посещению уже подан, как только куратор даст ответ вы получите уведомление",
//                                    status = if (inserted) DialogStatus.NORMAL else DialogStatus.ALERT,
//                                )
//                            )
//                        )
//                    }
//                },
//                { _ ->
//                    pending = null
//                    viewModelScope.launch {
//                        _events.emit(
//                            MainEvent.ShowMessageDialog(
//                                MessageDialogData(
//                                    message = "Ваша заявка создана, однако, для того чтобы ее подтвердить выполните обмен с сервером",
//                                    status = DialogStatus.ALERT,
//                                    positivText = "Синхронизация"
//                                )
//                            )
//                        )
//                    }
//                }
//            )
//        disposables.add(d)
//    }

    private fun doAcceptInfinite(wp: WpDataDB) {
        val dao = RoomManager.SQL_DB.wpDataAdditionalDao()

        val d = dao.getByClientAndAddr(wp.client_id.toInt(), wp.addr_id)
            .subscribeOn(Schedulers.io())
            .flatMap { list ->
                if (list.isEmpty()) {
                    dao.insert(WPDataAdditionalFactory.withClientAndAddress(wp))
                        .andThen(Single.just(true))
                } else Single.just(false)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { inserted ->
                    pending = null
                    viewModelScope.launch {
                        // временный костыль
                        tablesLoadingUnloading.uploadPlanBudget()

                        Globals.writeToMLOG(
                            "INFO", "MainViewModel.doAcceptInfinite",
                            "Updated: +"
                        )
                        _events.emit(
                            MainEvent.ShowMessageDialog(
                                MessageDialogData(
                                    message = if (inserted) "Заявка на виконання робіт створена та передана куратору, протягом декількох хвилин ви отримаєте відповідь. Якщо відповідь буде позитивною, це відвідування буде перенесено до плану робіт."
                                    else "Запит на роботи з цього відвідування вже подано, як тільки куратор дасть відповідь ви отримаєте повідомлення",
                                    status = if (inserted) DialogStatus.NORMAL else DialogStatus.ALERT
                                )
                            )
                        )
                    }
                },
                { e ->
                    pending = null
                    viewModelScope.launch {
                        _events.emit(
                            MainEvent.ShowMessageDialog(
                                MessageDialogData(
                                    message = "Ваша заявка створена, однак, для того, щоб її підтвердити виконайте обмін із сервером",
                                    status = DialogStatus.ALERT,
                                    positivText = "Синхронізація"
                                )
                            )
                        )
                    }
                }
            )
        disposables.add(d)
    }

    fun openContextMenu(wp: WpDataDB, contextUI: ContextUI) {
        viewModelScope.launch {
            if (contextUI == ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER_MULT) {
                // ✅ вместо меню — открываем новый экран
                _events.emit(MainEvent.OpenUFMDWPDataSelector(wp))
                return@launch
            }

            val actions = buildActions(contextUI)
            _events.emit(
                MainEvent.ShowContextMenu(
                    menuState = ContextMenuState(
                        wpDataDB = wp,
                        actions = actions
                    )
                )
            )
        }
    }

    private fun buildActions(contextUI: ContextUI): List<ContextMenuAction> =
        when (contextUI) {
            ContextUI.WP_DATA_IN_CONTAINER -> listOf(
                ContextMenuAction.OpenVisit,
                ContextMenuAction.Close
            )

            ContextUI.WP_DATA_IN_CONTAINER_MULT -> listOf(
                ContextMenuAction.ShowAllVizitInAdress,
                ContextMenuAction.Close
            )

            ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER -> listOf(
                ContextMenuAction.AcceptOrder,
                ContextMenuAction.AcceptAllAtAddress,
                ContextMenuAction.RejectOrder,
                ContextMenuAction.RejectAddress,
                ContextMenuAction.RejectClient,
                ContextMenuAction.RejectByType,
                ContextMenuAction.OpenOrder,
                ContextMenuAction.OpenSMSPlanDirectory,
                ContextMenuAction.AskMoreMoney,
                ContextMenuAction.Feedback,
                ContextMenuAction.Close
            )

            ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER_MULT -> listOf(
                ContextMenuAction.ShowAllVizitInAdress,
                ContextMenuAction.AcceptAllAtAddress,
//                ContextMenuAction.RejectOrder,
                ContextMenuAction.RejectAddress,
//                ContextMenuAction.RejectClient,
                ContextMenuAction.RejectByType,
//                ContextMenuAction.OpenOrder,
                ContextMenuAction.OpenSMSPlanDirectory,
                ContextMenuAction.AskMoreMoney,
                ContextMenuAction.Feedback,
                ContextMenuAction.Close
            )

            else -> emptyList()
        }


    override fun onCleared() {
        disposables.clear()
    }

    fun onChangeItemIndex(item: SettingsItemUI, offset: Int) {
//        repository.getSettingsItemList(table, contextUI)
    }

    private var pendingAction: PendingAction? = null

    fun requestJumpToAddressVisits(wp: WpDataDB, periodText: String) {
        // готовим pending
        pendingAction = PendingAction.JumpToAddressVisits(
            addrText = wp.addr_txt ?: "",
            addrTitle = wp.addr_txt,
            periodText = periodText
        )

        viewModelScope.launch {

            performPendingK()
            _events.emit(
                MainEvent.JumpToVizitAndCloseMaps
//                MainEvent.ShowMessageDialog(
//                    MessageDialogData(
//                        title = "Перейти к посещенням",
//                        status = DialogStatus.NORMAL,
//                        subTitle = wp.addr_txt,
//                        message = "Показать все работы по этому адресу за период с $periodText?",
//                        filterLogic = true
//                    )
//                )
            )
        }
    }

    fun performPendingK() {
        when (val p = pendingAction) {
            is PendingAction.JumpToAddressVisits -> {
                pendingAction = null

                // 1) sorting/grouping как раньше
                val sortingFieldAdr =
                    SortingField("addr_txt", getTranslateString("Адреса", 1101), 1)
                val groupingFieldAdr = GroupingField(
                    key = "addr_txt",
                    title = getTranslateString("Адреса", 1101),
                    priority = 1,
                    collapsedByDefault = false
                )
                val sortingFieldDate = SortingField("dt", getTranslateString("Дата", 1100), 1)
                val groupingFieldDate = GroupingField(
                    key = "dt",
                    title = getTranslateString("Дата", 1100),
                    priority = 1,
                    collapsedByDefault = false
                )

                viewModelScope.launch {
                    updateSorting(sortingFieldAdr, 0)
                    updateSorting(sortingFieldDate, 1)
                    updateGrouping(groupingFieldAdr, 0)
                    updateGrouping(groupingFieldDate, 1)

                    val updated = (_uiState.value.filters?.copy(searchText = p.addrText) ?: Filters(
                        searchText = p.addrText
                    ))
                    updateFilters(updated)

                    // 2) твои “переходные” штуки
                    // showToolTipKostil = true — это было локально в MapsDialog,
                    // поэтому делаем отдельный эвент, чтобы UI показал это.
//                    _events.emit(MainEvent.ShowCardItemsDialog(/* если надо */)) // или отдельный MainEvent.ShowKostil
                    showKostilDialog()
                }
            }

            null -> Unit
            else -> {
                pendingAction = null
            } // на всякий
        }
    }

    fun cancelPendingK() {
        pendingAction = null
    }

}