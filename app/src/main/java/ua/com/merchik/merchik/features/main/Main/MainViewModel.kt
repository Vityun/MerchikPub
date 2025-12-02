package ua.com.merchik.merchik.features.main.Main

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.Globals.APP_OFFSET_SIZE_FONTS
import ua.com.merchik.merchik.Globals.APP_PREFERENCES
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
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
    var sortingFields: List<SortingField> = emptyList(),
    val groupingFields: List<GroupingField> = emptyList(),
    val filters: Filters? = null,
    val lastUpdate: Long = 0
)

data class Filters(
    val title: String = "Фільтри",
    val subTitle: String? = "В этой форме Вы можете настроить фильтры для ограничения списка элементов",
    val rangeDataByKey: RangeDate? = null,
    val searchText: String = "",
    var items: List<ItemFilter> = emptyList()
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
    val key: String,                // по какому полю группируем (как в FieldValue.key)
    val title: String? = null,      // заголовок для UI (например "По дате", "По магазину")
    val priority: Int = 0,          // порядок применения, если группировок несколько
    val collapsedByDefault: Boolean = false // свернута ли группа по умолчанию
)

data class GroupMeta(
    val groupKey: String,           // ключ группы (например "2025-11-19" или "Київ")
    val title: String? = null,      // что показывать в заголовке группы
    val startIndex: Int,            // индекс первого элемента группы в result.items
    val endIndexExclusive: Int      // индекс после последнего (как в subList)
)

sealed interface MainEvent {
    data class ShowContextMenu(val menuState: ContextMenuState) : MainEvent
    data class ShowMessageDialog(val data: MessageDialogData) : MainEvent
}


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

    private val _valueForCustomResult = MutableStateFlow(HashMap<String, Any?>())
    val valueForCustomResult: StateFlow<HashMap<String, Any?>> = _valueForCustomResult

    private val _scrollToHash = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val scrollToHash: SharedFlow<Long> = _scrollToHash

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


    var filters: Filters? = null

    protected var dialog: DialogFullPhoto? = null


    open fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context) {
        onClickItemImage(clickedDataItemUI, context, 0) // Делегируем вызов новому методу
    }

    open fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context, index: Int) {
        if (index == 0) {
            dialog = DialogFullPhoto(context)
            val photoLogData = mutableListOf<StackPhotoDB>()
            var selectedIndex = -1
            val fieldsForCommentsImage = getFieldsForCommentsImage()
            val fieldsForCustomResult = getFieldsForCustomResult()
            val photoDBWithComments = HashMap<StackPhotoDB, String>()
            val photoDBWithRawObj = HashMap<StackPhotoDB, Any>()
            _uiState.value.items.map { dataItemUI ->
                val jsonObject = JSONObject(Gson().toJson(dataItemUI.rawObj[0]))
                var comments = ""
                fieldsForCommentsImage?.forEach {
                    comments += "${jsonObject.get(it)} \n\n"
                }
                if (clickedDataItemUI == dataItemUI) {
                    fieldsForCustomResult?.forEach {
                        _valueForCustomResult.value[it] = jsonObject.get(it)
                    }
                }
                val imageFields = dataItemUI.rawObj[0].getFieldsImageOnUI().split(",")
                imageFields.getOrNull(index)?.takeIf { it.isNotEmpty() }?.let { fieldKey ->
                    RealmManager.getPhotoById(null, jsonObject.get(fieldKey.trim()).toString())
                        ?.let { photo ->
                            photoDBWithComments[photo] = comments
                            photoDBWithRawObj[photo] = dataItemUI.rawObj[0]
                            photoLogData.add(photo)

                            if (clickedDataItemUI == dataItemUI) {
                                selectedIndex = photoLogData.size - 1
                            }
                        }
                }
//            dataItemUI.rawObj[0].getFieldsImageOnUI().split(",").forEach {
//                if (it.isNotEmpty()) {
//                    RealmManager.getPhotoById( null, jsonObject.get(it.trim()).toString())
//                        ?.let {
//                            photoDBWithComments[it] = comments
//                            photoDBWithRawObj[it] = dataItemUI.rawObj[0]
//                            photoLogData.add(it)
//                            if (clickedDataItemUI == dataItemUI) selectedIndex = photoLogData.count() - 1
//                        }
//                }
//            }
            }

            if (selectedIndex > -1) {
                dialog?.setPhotos(
                    selectedIndex, photoLogData,
                    { _, photoDB ->
                        onClickFullImage(photoDB, photoDBWithComments[photoDB])
                        dialog?.dismiss()
                        dialog = null
                    },
                    { }
                )

                dialog?.setClose {
                    dialog?.dismiss()
                    dialog = null
                }
                dialog?.show()
            }

        }
    }

    private val _uiState = MutableStateFlow(StateUI())
    val uiState
            : StateFlow<StateUI>
        get() = _uiState.asStateFlow()


    private val _rangeDataStart = MutableStateFlow<LocalDate?>(LocalDate.now())
    val rangeDataStart
            : StateFlow<LocalDate?>
        get() = _rangeDataStart.asStateFlow()


    private val _rangeDataEnd = MutableStateFlow<LocalDate?>(LocalDate.now().plusDays(7))
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


    init {
        loadPreferences()
        observeSourcesForItems()
    }

    private fun loadPreferences() {
        _offsetSizeFonts.value = sharedPreferences.getFloat(APP_OFFSET_SIZE_FONTS, 0f)
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

    private fun recomputeDataItems(uiState: StateUI, rangeStart: LocalDate?, rangeEnd: LocalDate?) {
        viewModelScope.launch {
            val combined = withContext(Dispatchers.Default) {
                val header = uiState.itemsHeader
                val footer = uiState.itemsFooter

                val result: FilterAndSortResult = try {
                    filterAndSortDataItems(
                        items = uiState.items,
                        filters = uiState.filters,
                        sortingFields = uiState.sortingFields,
                        groupingFields = uiState.groupingFields, // новый список в StateUI
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
                        emptyList(),
                        isActiveFiltered = false,
                        isActiveSorted = false,
                        isActiveGrouped = false
                    ) // fallback
                }

                // Собираем итоговый immutable список
                buildList {
                    addAll(header)
                    addAll(result.items)
                    addAll(footer)
                }
            }

            // обновляем state только если изменилось (чтобы не триггерить лишний раз)
            if (_dataItems.value != combined) {
                _dataItems.value = combined
            }
        }
    }


    fun updateOffsetSizeFonts(offsetSizeFont: Float) {
        viewModelScope.launch {
            sharedPreferences.edit().putFloat(APP_OFFSET_SIZE_FONTS, offsetSizeFont).apply()
            _offsetSizeFonts.value = offsetSizeFont
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

    fun updateSorting(newSortingField: SortingField?, position: Int) {
//        viewModelScope.launch {
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
//        }
    }

    fun updateContent() {
        viewModelScope.launch {

            val list = getDefaultHideUserFields()
            val settingsItems = repository.getSettingsItemList(table, contextUI, list)

            val defaultSort = getDefaultSortUserFields()

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
                            collapsedByDefault = false
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
                Globals.writeToMLOG(
                    "INFO",
                    "MainViewModel.updateContent",
                    "getItems() size: ${dataItemUIS.size}"
                )

                val finalItems = if (groupingKeys.isEmpty()) {
                    dataItemUIS
                } else {
                    dataItemUIS.map { it.withGroupingOnTop(groupingKeys) }
                }

                old.copy(
                    title = titleResolved,
                    subTitle = subTitle,
                    subTitleLong = subTitleLong,
                    idResImage = idResImage,
                    items = finalItems,
                    itemsHeader = getItemsHeader(),
                    itemsFooter = getItemsFooter(),
                    settingsItems = settingsItems,
                    sortingFields = sortingFields,
                    groupingFields = groupingFields,
                    filters = filters,
                    lastUpdate = System.currentTimeMillis()
                )
            }
        }
    }

    // ViewModel
    private val _flyRequests = MutableSharedFlow<Long>(replay = 0, extraBufferCapacity = 10)
    val flyRequests = _flyRequests.asSharedFlow()

    fun requestFlyByStableId(stableId: Long) {
        viewModelScope.launch {
            _flyRequests.emit(stableId)
        }
    }

    fun updateFilters(filters: Filters) {
        this.filters = filters
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    filters = filters,
                    lastUpdate = System.currentTimeMillis()
                )
            }
        }
    }

    fun updateItemSelect(checked: Boolean, itemUI: DataItemUI) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    itemsHeader = it.itemsHeader.map { oldItemUI ->
                        oldItemUI.copy(
                            selected =
                                if (itemUI === oldItemUI) checked
                                else if (modeUI == ModeUI.ONE_SELECT) false else oldItemUI.selected
                        )
                    },
                    items = it.items.map { oldItemUI ->
                        oldItemUI.copy(
                            selected =
                                if (itemUI === oldItemUI) checked
                                else if (modeUI == ModeUI.ONE_SELECT) false else oldItemUI.selected
                        )
                    },
                    itemsFooter = it.itemsFooter.map { oldItemUI ->
                        oldItemUI.copy(
                            selected =
                                if (itemUI === oldItemUI) checked
                                else if (modeUI == ModeUI.ONE_SELECT) false else oldItemUI.selected
                        )
                    },
                )
            }
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
//                        message = "Выполнить текущую работу\n" +
//                                "<font color='gray'>Посещение от</font> ${
//                                    Clock.getHumanTime_dd_MMMM(
//                                        wp.dt.time
//                                    )
//                                }" +
//                                "\n<font color='gray'>Клиент:</font> ${wp.client_txt}" +
//                                "\n<font color='gray'>Адрес:</font> ${wp.addr_txt}" +
//                                "\n<font color='gray'>Премiя (план):</font> ${wp.cash_ispolnitel} грн." +
//                                "\n<font color='gray'>СКЮ (количество товарных позиций):</font> ${wp.sku_plan}" +
//                                "\n<font color='gray'>Середній час роботи:</font> ${wp.duration} хв",
                        message = String.format(
                            "Выполнить текущую работу<br>" +
                                    "<font color='gray'>Посещение от</font> %s" +
                                    "<br><font color='gray'>Клиент:</font> %s" +
                                    "<br><font color='gray'>Адрес:</font> %s" +
                                    "<br><font color='gray'>Премiя (план):</font> %s грн." +
                                    "<br><font color='gray'>СКЮ (количество товарных позиций):</font> %s" +
                                    "<br><font color='gray'>Середній час роботи:</font> %s хв",
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
                        message = "Выполнять все всегда работы этого клиента по этому адресу\n" +
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
                        message = "Выполнить текущие работы за сегодня\n" +
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
                        message = "Выполнять все всегда работы доступные по этому адресу\n" +
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


    private fun doAcceptOneTime(wp: WpDataDB) {
        val dad2 = wp.code_dad2
        val dao = RoomManager.SQL_DB.wpDataAdditionalDao()
        val d = dao.getByCodeDad2(dad2)
            .subscribeOn(Schedulers.io())
            .flatMap { list ->
                if (list.isEmpty()) {
                    dao.insert(WPDataAdditionalFactory.blankWithDad2(wp)).andThen(Single.just(true))
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
                            "INFO", "MainViewModel.doAcceptOneTime",
                            "Updated: +"
                        )

                        _events.emit(
                            MainEvent.ShowMessageDialog(
                                MessageDialogData(
                                    message = if (inserted) "Заявка на выполнение работ создана и передана куратору, в течении нескольких минут вы получите ответ. Если ответ будет положительный это посещение будет перенесено в план работ"
                                    else "Запрос на работы по этому посещению уже подан, как только куратор даст ответ вы получите уведомление",
                                    status = if (inserted) DialogStatus.NORMAL else DialogStatus.ALERT,
                                )
                            )
                        )
                    }
                },
                { _ ->
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

    fun openContextMenu(wp: WpDataDB, contextUI: ContextUI) {
        val actions = buildActions(contextUI)
        viewModelScope.launch {
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

            else -> emptyList()
        }


    override fun onCleared() {
        disposables.clear()
    }

    fun onChangeItemIndex(item: SettingsItemUI, offset: Int) {
//        repository.getSettingsItemList(table, contextUI)
    }
}