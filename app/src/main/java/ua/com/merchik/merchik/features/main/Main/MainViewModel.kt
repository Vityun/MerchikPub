package ua.com.merchik.merchik.features.main.Main

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Clock
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
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.database.room.factory.WPDataAdditionalFactory
import ua.com.merchik.merchik.dialogs.DialogFullPhoto
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenuState
import ua.com.merchik.merchik.features.main.componentsUI.MessageDialogData
import java.time.LocalDate
import kotlin.reflect.KClass

data class StateUI(
    val title: String? = null,
    val subTitle: String? = null,
    val idResImage: Int? = null,
    val itemsHeader: List<DataItemUI> = emptyList(),
    val items: List<DataItemUI> = emptyList(),
    val itemsFooter: List<DataItemUI> = emptyList(),
    val settingsItems: List<SettingsItemUI> = emptyList(),
    var sortingFields: List<SortingField> = emptyList(),
    val filters: Filters? = null,
    val lastUpdate: Long = 0
)

data class Filters(
    val title: String = "Фільтри",
    val subTitle: String? = "В этой форме Вы можете настроить фильтры для ограничения списка элементов",
    val rangeDataByKey: RangeDate? = null,
    val searchText: String,
    var items: List<ItemFilter>? = null
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
    var order: Int? = null
)

data class SettingsUI(
    val hideFields: List<String>? = null,
    val sortFields: List<SortingField>? = null,
    val sizeFonts: Int? = null
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
    var idResImage: Int? = null
    var modeUI: ModeUI = ModeUI.DEFAULT
    var contextUI: ContextUI = ContextUI.DEFAULT
    var launcher: ActivityResultLauncher<Intent>? = null

    abstract val table: KClass<out DataObjectUI>

    open fun getItemsHeader(): List<DataItemUI> = emptyList()

    open fun getItemsFooter(): List<DataItemUI> = emptyList()

    open fun updateFilters() {}

    abstract fun getItems(): List<DataItemUI>

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
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()


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
    }

    private fun loadPreferences() {
        _offsetSizeFonts.value = sharedPreferences.getFloat(APP_OFFSET_SIZE_FONTS, 0f)
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

    fun updateContent() {
        viewModelScope.launch {

            val list = getDefaultHideUserFields()
            val settingsItems = repository.getSettingsItemList(table, contextUI, list)
            val sortingFields = repository.getSortingFields(table, contextUI)
            updateFilters()

            _uiState.update {
                val title = title?.split(",")?.map { it.trim() }?.let {
                    it[0].toIntOrNull()?.let { intRes ->
                        context?.let { cont ->
                            getTranslateString(cont.getString(intRes), it[1].toLongOrNull())
                        }
                    }
                } ?: title

                it.copy(
                    title = title,
                    subTitle = subTitle,
                    idResImage = idResImage,
                    itemsHeader = getItemsHeader(),
                    items = getItems(),
                    itemsFooter = getItemsFooter(),
                    settingsItems = settingsItems,
                    sortingFields = sortingFields,
                    filters = filters,
                    lastUpdate = System.currentTimeMillis()
                )
            }
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

    /** Пользователь выбрал "Принять заказ (разово)" -> показать подтверждение */
    fun requestAcceptOneTime(wp: WpDataDB) {
        pending = PendingOp.AcceptOneTime(wp)
        viewModelScope.launch {
            _events.emit(
                MainEvent.ShowMessageDialog(
                    MessageDialogData(
                        message = "Выполнить текущую работу\n" +
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
                                "\n<font color='gray'>Клиенты:</font> ${WPDataAdditionalFactory.getUniqueClientIdsForAddr_TXT(wp.addr_id, wp.dt).toString().removeSurrounding("[", "]")}" +
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
                                "\n<font color='gray'>Клиенты:</font> ${WPDataAdditionalFactory.getUniqueClientIdsForAddr_TXT(wp.addr_id).toString().removeSurrounding("[", "]")}" +
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
                        val tablesLoadingUnloading = TablesLoadingUnloading()
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
                        val tablesLoadingUnloading = TablesLoadingUnloading()
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
                        val tablesLoadingUnloading = TablesLoadingUnloading()
                        tablesLoadingUnloading.uploadPlanBudget()

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
                        val tablesLoadingUnloading = TablesLoadingUnloading()
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


    override fun onCleared() {
        disposables.clear()
    }

    fun onChangeItemIndex(item: SettingsItemUI, offset: Int) {
//        repository.getSettingsItemList(table, contextUI)
    }
}