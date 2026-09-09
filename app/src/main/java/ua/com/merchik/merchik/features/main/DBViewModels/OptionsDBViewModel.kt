package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.Activities.DetailedReportActivity.OpinionDataHolder
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB
import ua.com.merchik.merchik.data.QuestionAnswerDB
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.SettingsUI
import ua.com.merchik.merchik.features.main.Main.launchFeaturesActivity
import java.util.Calendar
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.WorkPlan
import ua.com.merchik.merchik.Options.Options
import ua.com.merchik.merchik.Options.OptionControl
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading
import ua.com.merchik.merchik.ViewHolders.Clicks
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.SelectedMode
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.features.LoadingDialogWithPercent
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel
import ua.com.merchik.merchik.features.main.options.OptionItemState
import ua.com.merchik.merchik.features.main.options.OptionsDisplayMode
import ua.com.merchik.merchik.features.main.options.OptionsRowFactory


@HiltViewModel
class OptionsDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    private val _optionRows = MutableStateFlow<List<OptionItemState>>(emptyList())
    val optionRows = _optionRows.asStateFlow()
    private val _optionsLoading = MutableStateFlow(false)
    val optionsLoading = _optionsLoading.asStateFlow()
    private val _optionsError = MutableStateFlow<String?>(null)
    val optionsError = _optionsError.asStateFlow()

    data class OptionScrollRequest(val id: String, val optionId: String, val sequence: Long)
    private val _optionScroll = MutableStateFlow<OptionScrollRequest?>(null)
    val optionScroll = _optionScroll.asStateFlow()
    private var scrollSequence = 0L
    private var visitDad2 = 0L
    override val settingsVisitId: Long?
        get() = visitDad2.takeIf { contextUI == ContextUI.OPTIONS_IN_CONTAINER && it > 0 }
    private var optionsJob: Job? = null
    private var refreshPending = false
    private var recheckPending = false
    private var galleryClick: Clicks.click? = null
    private var onVisitReloaded: ((WpDataDB) -> Unit)? = null
    private var rowFactory: OptionsRowFactory<WpDataDB>? = null
    private var hostGeneration = 0L
    private var visibleOptions: List<OptionsDB> = emptyList()
    var onConductReport: (() -> Unit)? = null

    data class ReportButtonState(
        val plan: String,
        val fact: String,
        val iconRes: Int,
        val tintRes: Int
    )
    private val _reportButton = MutableStateFlow<ReportButtonState?>(null)
    val reportButton = _reportButton.asStateFlow()

    fun conductReport() {
        if (context != null && !_optionsLoading.value && _reportButton.value != null) {
            onConductReport?.invoke()
        }
    }

    fun updateReportButton(wp: WpDataDB) {
        val (icon, tint) = when {
            wp.setStatus == 1 -> R.drawable.ic_question_circle_regular to R.color.colorInetYellow
            wp.status == 1 -> R.drawable.ic_check to R.color.greenCol
            (wp.dt?.time ?: Long.MAX_VALUE) < System.currentTimeMillis() ->
                R.drawable.ic_exclamation_mark_in_a_circle to R.color.red_error
            else -> R.drawable.ic_check to R.color.shadow
        }
        _reportButton.value = ReportButtonState(
            wp.cash_ispolnitel.toString(), wp.cash_fact.toString(), icon, tint
        )
    }

    fun attachOptions(
        hostContext: Context,
        wp: WpDataDB,
        gallery: Clicks.click,
        onReloaded: (WpDataDB) -> Unit
    ) {
        if (visitDad2 != wp.code_dad2) {
            detachOptions()
            _optionRows.value = emptyList()
            _optionScroll.value = null
        }
        visitDad2 = wp.code_dad2
        context = hostContext
        contextUI = ContextUI.OPTIONS_IN_CONTAINER
        modeUI = ModeUI.DEFAULT
        typeWindow = "container"
        title = null
        subTitle = null
        dataJson = Gson().toJson(visitDad2)
        galleryClick = gallery
        onVisitReloaded = onReloaded
        updateReportButton(wp)
        refreshOptions(true)
    }

    override fun updateContent() {
        if (contextUI == ContextUI.OPTIONS_IN_CONTAINER) refreshOptions(true)
        else super.updateContent()
    }

    fun refreshOptions(recheck: Boolean) {
        if (context == null || visitDad2 <= 0 || galleryClick == null) return
        recheckPending = recheckPending || recheck
        if (optionsJob?.isActive == true) {
            refreshPending = true
            return
        }
        val generation = hostGeneration
        optionsJob = viewModelScope.launch {
            _optionsLoading.value = true
            try {
                do {
                    // Coalesce attach/onResume notifications and let the host draw first.
                    delay(16)
                    refreshPending = false
                    val shouldCheck = recheckPending
                    recheckPending = false
                    loadOptionRows(shouldCheck)
                } while (refreshPending)
            } catch (timeout: TimeoutCancellationException) {
                _optionsError.value = "Перевищено час очікування відповіді сервера. Повторіть синхронізацію."
                Globals.writeToMLOG("ERROR", "OptionsDBViewModel/download", "dad2=$visitDad2, timeout")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Globals.writeToMLOG("ERROR", "OptionsDBViewModel/refresh", "dad2=$visitDad2, error=$error")
                _optionsError.value = error.message ?: error.toString()
            } finally {
                if (hostGeneration == generation) _optionsLoading.value = false
            }
        }
    }

    private suspend fun loadOptionRows(recheck: Boolean) {
        val host = context ?: return
        val gallery = galleryClick ?: return
        var wp = WpDataRealm.getWpDataRowByDad2Id(visitDad2)
            ?: error("Відвідування не знайдено в локальній базі")
        val workPlan = WorkPlan()
        fun readButtons() = workPlan.getOptionButtons2(workPlan.getWpOpchetId(wp), wp.id)
        var buttons = readButtons()
        val downloaded = buttons.isEmpty()
        if (downloaded) {
            downloadVisitOptions(host, visitDad2)
            buttons = readButtons()
            if (buttons.isEmpty()) error("Сервер не повернув опції до цього відвідування")
        }
        // These controls use the main-thread Realm instance and may show dialogs.
        // Do not move them to Dispatchers.IO with managed Realm objects.
        if (recheck || downloaded) {
            val controls = Options()
            for (button in buttons) {
                val previousSignal = button.isSignal
                try {
                    controls.optionControl(host, wp, button, null, Options.NNKMode.NULL,
                        object : OptionControl.UnlockCodeResultListener {
                            override fun onUnlockCodeSuccess() = Unit
                            override fun onUnlockCodeFailure() = Unit
                        })
                } catch (error: Exception) {
                    // A failed control must not discard the entire options list.
                    button.isSignal = previousSignal
                    Globals.writeToMLOG("ERROR", "OptionsDBViewModel/control",
                        "dad2=$visitDad2, option=${button.optionId}, error=$error")
                }
            }
        }
        wp = WpDataRealm.getWpDataRowByDad2Id(visitDad2) ?: return
        val allOptions = RealmManager.INSTANCE.copyFromRealm(
            OptionsRealm.getOptionsByDAD2(visitDad2.toString())
        )
        // Match the legacy fragment: check every button in query order, then hide/sort for display.
        val visibleButtons = buttons.filter { it.optionId != "80976" }.sortedBy { it.so ?: 0 }
        val translations = RoomManager.SQL_DB.siteObjectsDao()
            .getObjectsById(visibleButtons.mapNotNull { it.optionId?.toIntOrNull() })
        val generation = hostGeneration
        val factory = OptionsRowFactory(host, wp, visibleButtons, allOptions, gallery,
            { hostGeneration == generation && context === host }) {
            refreshOptions(false)
        }
        val rows = ArrayList<OptionItemState>(visibleButtons.size)
        for (button in visibleButtons) {
            val translation = translations.firstOrNull { it.additionalId.toString() == button.optionId }
            rows.add(factory.create(button, translation))
            delay(1)
        }
        rowFactory = factory
        visibleOptions = visibleButtons
        _optionRows.value = rows
        updateReportButton(wp)
        onVisitReloaded?.invoke(wp)
        // Publish the same buttons to MainUI's search/filter/sort/selection pipeline.
        // Controls are calculated above, never from the rendering/filtering callbacks.
        super.updateContent()
    }

    private suspend fun downloadVisitOptions(host: Context, dad2: Long) {
        val progress = ProgressViewModel(1)
        val dialog = (host as? Activity)?.let { LoadingDialogWithPercent(it, progress) }
        dialog?.show()
        progress.onNextEvent("Завантажую опції до цього відвідування", 2000)
        try {
            withTimeout(60_000) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    TablesLoadingUnloading().downloadOptionsByDAD2(dad2, object : Clicks.click {
                        override fun <T> click(data: T) {
                            if (!continuation.isActive) return
                            if (data is List<*>) continuation.resume(Unit)
                            else continuation.resumeWithException(IllegalStateException(data.toString()))
                        }
                    })
                }
            }
        } finally {
            progress.onCompleted()
        }
    }

    fun scrollToOption(option: OptionsDB) {
        // A failed control must remain reachable even when search/selection hid its row.
        uiState.value.filters?.let {
            updateFilters(it.copy(searchText = "", selectedMode = SelectedMode.ALL))
        }
        expandAllDecks()
        _optionScroll.value = OptionScrollRequest(option.getID(), option.optionId, ++scrollSequence)
    }

    enum class OptionClickTarget { ROW, COUNTER, SECONDARY_COUNTER, SIGNAL }

    fun onOptionClick(id: String, target: OptionClickTarget, view: android.view.View) {
        if (context == null || _optionsLoading.value) return
        val row = _optionRows.value.firstOrNull { it.id == id } ?: return
        try {
            val action = when (target) {
                OptionClickTarget.ROW -> row.onClick
                OptionClickTarget.COUNTER -> row.counter.onClick
                OptionClickTarget.SECONDARY_COUNTER -> row.secondaryCounter.onClick
                OptionClickTarget.SIGNAL -> row.signal.onClick
            }
            action?.onClick(view)
        } catch (error: Exception) {
            Globals.writeToMLOG("ERROR", "OptionsDBViewModel/click", "option=${row.optionId}, target=$target, error=$error")
        }
    }

    fun onOptionLongClick(id: String, view: android.view.View) {
        if (context == null || _optionsLoading.value) return
        try {
            _optionRows.value.firstOrNull { it.id == id }?.onLongClick?.onLongClick(view)
        } catch (error: Exception) {
            Globals.writeToMLOG("ERROR", "OptionsDBViewModel/longClick", "id=$id, error=$error")
        }
    }

    fun clearOptionsError() { _optionsError.value = null }

    fun detachOptions() {
        hostGeneration++
        optionsJob?.cancel()
        optionsJob = null
        rowFactory?.detach()
        rowFactory = null
        _optionsLoading.value = false
        _optionRows.value = emptyList()
        _optionsError.value = null
        visibleOptions = emptyList()
        _reportButton.value = null
        onConductReport = null
        galleryClick = null
        onVisitReloaded = null
        refreshPending = false
        recheckPending = false
        context = null
    }


    override val table: KClass<out DataObjectUI>
        get() = OptionsDB::class

    override fun getDefaultHideUserFields(): List<String> {
        return (
                "option_control_id, is_signal, sum_premiya, amount, amount_min, option_control_txt, " +
                        "amount_max, option_control_descr, column_name, option_descr"
                ).split(",").map { it.trim() }    }

    override fun getDefaultGroupUserFields(): List<String> {
        return emptyList()
    }

    override fun getDefaultSortUserFields(): List<String>? {
        // The source buttons are already ordered by so; user sorting is applied on top.
        if (contextUI == ContextUI.OPTIONS_IN_CONTAINER) return emptyList()
        return "sum_penalty".split(",")
    }

    override fun updateFilters() {
        if (contextUI == ContextUI.OPTIONS_IN_CONTAINER) {
            val dad2 = visitDad2.toString()
            val previous = uiState.value.filters?.takeIf { state ->
                state.items.any { it.leftField == "code_dad2" && it.rightValuesRaw == listOf(dad2) }
            } ?: Filters()
            filters = previous.copy(
                items = listOf(ItemFilter(
                    title = "Відвідування",
                    clazz = OptionsDB::class,
                    clazzViewModel = OptionsDBViewModel::class,
                    modeUI = ModeUI.MULTI_SELECT,
                    titleContext = "Відвідування",
                    subTitleContext = "Опції поточного відвідування",
                    leftField = "code_dad2",
                    rightField = "code_dad2",
                    rightValuesRaw = listOf(dad2),
                    rightValuesUI = listOf(dad2),
                    enabled = false
                )),
                rangeDataByKey = null
            )
            return
        }
        Log.e("OpinionSDBViewModel", "++++")
        try {

            val codeDad2 = Gson().fromJson(dataJson, Long::class.java)

            val data = RealmManager.getOptionsByDad2(codeDad2).take(1)

            val filterThemeDB = ItemFilter(
                "dad2",
                OptionsDB::class,
                OptionsDBViewModel::class,
                ModeUI.MULTI_SELECT,
                "Тема",
                "Выберите характер достижения, которое Вы выполнили",
                "code_dad2",
                "code_dad2",
                data.map { it.codeDad2 },
                data.map { it.codeDad2 },
                false
            )

            filters = Filters(
                searchText = "",
                items = mutableListOf(
                    filterThemeDB
                )
            )
        } catch (e: Exception) {
        }
    }

    fun settingsDisplayMode(): OptionsDisplayMode =
        if (contextUI == ContextUI.OPTIONS_IN_CONTAINER) {
            repository.getSettingsUI(table.java, contextUI, settingsVisitId)?.optionsDisplayMode
                ?: OptionsDisplayMode.ALL
        } else {
            OptionsDisplayMode.ALL
        }

    fun saveDisplayFilters(applyToAllVisits: Boolean, displayMode: OptionsDisplayMode) {
        val dad2 = settingsVisitId ?: return
        val saved = repository.getSettingsUI(table.java, contextUI, dad2) ?: SettingsUI(
            hideFields = repository.getSettingsItemList(
                table, contextUI, getDefaultHideUserFields(), modeUI, dad2
            ).filterNot { it.isEnabled }.map { it.key }
        )
        repository.saveSettingsUI(
            table,
            saved.copy(optionsDisplayMode = displayMode),
            contextUI,
            dad2,
            applyToAllVisits
        )
    }

    override suspend fun getItems(): List<DataItemUI> {
        if (contextUI == ContextUI.OPTIONS_IN_CONTAINER) {
            val rowsById = _optionRows.value.associateBy { it.id }
            val displayMode = settingsDisplayMode()
            val redSignalColor = ContextCompat.getColor(
                context ?: getApplication<Application>(), R.color.red_error
            )
            // Only restrict presentation: every option has already been calculated in loadOptionRows.
            val displayedOptions = visibleOptions.filter { option ->
                val row = rowsById[option.getID()]
                when (displayMode) {
                    OptionsDisplayMode.ALL -> true
                    OptionsDisplayMode.ACTIVE -> row != null &&
                        row.backgroundRes != R.drawable.button_bg_inactive
                    OptionsDisplayMode.VIOLATIONS -> row != null &&
                        row.signal.visibility == View.VISIBLE && row.signal.tint == redSignalColor
                }
            }
            return repository.toItemUIList(
                OptionsDB::class, displayedOptions, contextUI, null,
                settingsVisitId = settingsVisitId
            )
                .map { item ->
                    val id = (item.rawObj.firstOrNull() as? OptionsDB)?.getID()
                    val translatedTitle = rowsById[id]?.title?.text?.toString()
                    item.copy(fields = item.fields.map { field ->
                        if (field.key == "option_txt" && translatedTitle != null)
                            field.copy(value = field.value.copy(value = translatedTitle))
                        else field
                    })
                }
        }
        Log.e("OpinionSDBViewModel", "++++")

        return try {
            val codeDad2 = Gson().fromJson(dataJson, Long::class.java)
            Log.e("OpinionSDBViewModel", "codeDad2: $codeDad2")

            val data = RealmManager.getOptionsByDad2(codeDad2)
//                .filter { !it.optionControlTxt.isNullOrBlank() }
                .onEach { option ->
                    if (option.sumPenalty != "0.00")
                        option.timeColor = "FFC4C4"
                }
                ?: return emptyList()



            repository.toItemUIList(
                OptionsDB::class,
                data,
                contextUI,
                null
            ).map { item ->
                when (contextUI) {
                    ContextUI.ADD_THEME_QUESTION_ANSWER -> {
                        val selected = FilteringDialogDataHolder.instance()
                            .filters
                            ?.items
                            ?.firstOrNull { it.clazz == table }
                            ?.rightValuesRaw
                            ?.contains(
                                (item.rawObj.firstOrNull { it is QuestionAnswerDB }
                                        as? QuestionAnswerDB)
                                    ?.id
                                    .toString()
                            )

                        item.copy(selected = selected == true)
                    }

                    else -> item
                }
            }
        } catch (e: Exception) {
            Log.e(
                "OpinionSDBViewModel",
                "getItems -> Exception: ${e.message}",
                e
            )
            emptyList()
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        Log.e("ADD_OPINION_FROM_DETAIL", "onSelectedItemsUI")
        when (contextUI) {
            ContextUI.ADD_OPINION_FROM_DETAILED_REPORT -> {
                Log.e(
                    "ADD_OPINION_FROM_DETAIL",
                    "onSelectedItemsUI -> ADD_OPINION_FROM_DETAILED_REPORT"
                )

                (itemsUI.first().rawObj.firstOrNull { it is OpinionSDB } as? OpinionSDB)?.let {
                    Log.e(
                        "ADD_OPINION_FROM_DETAIL",
                        "onSelectedItemsUI -> itemsUI.first().rawObj.firstOrNull { it is ThemeDB } as? ThemeDB)?.let"
                    )
                    OpinionDataHolder.instance().opinionID = it.id
                    OpinionDataHolder.instance().opinionName = it.nm
                    Log.e(
                        "ADD_OPINION_FROM_DETAIL",
                        "opinionName: ${OpinionDataHolder.instance().opinionID}"
                    )
                }
            }

            ContextUI.DEFAULT -> {
                FilteringDialogDataHolder.instance().filters.apply {
                    this?.let { filters ->
                        filters.items = filters.items.map { itemFilter ->
                            if (itemFilter.clazz == table) {
                                val rightValuesRaw = mutableListOf<String>()
                                val rightValuesUI = mutableListOf<String>()
                                itemsUI.forEach {
                                    (it.rawObj.firstOrNull() as? ThemeDB)?.let {
                                        rightValuesRaw.add(it.id)
                                        rightValuesUI.add(it.nm)
                                    }
                                }
                                itemFilter.copy(
                                    rightValuesRaw = rightValuesRaw,
                                    rightValuesUI = rightValuesUI
                                )
                            } else {
                                itemFilter
                            }
                        }
                    }
                }
            }

            else -> {
                Log.e("OpinionSDBViewModel", "onSelectedItemsUI -> empty")
            }
        }
    }

    override fun onClickAdditionalContent() {
        if (contextUI == ContextUI.OPTIONS_IN_CONTAINER) {
            context?.let {
                android.widget.Toast.makeText(it, "Додавання опцій недоступне", android.widget.Toast.LENGTH_SHORT).show()
            }
            return
        }
        super.onClickAdditionalContent()
        launcher?.let {
            launchFeaturesActivity(
                launcher = it,
                context = context!!,
                viewModelClass = ThemeDBViewModel::class,
                dataJson = dataJson,
                modeUI = ModeUI.ONE_SELECT,
                contextUI = ContextUI.ADD_THEME_QUESTION_ANSWER,
                title = "Жалобы, Замечания, Предложени (Жилетка)",
                subTitle = "Выберите тему из списка. Благодаря анализу вашего мнения мы сможем улучшить работу нашего предприятия и тем самым увеличить ваши доходы.",
            )
        }

    }


    private fun isToday(seconds: Long?): Boolean {
        if (seconds == null || seconds <= 0L) return false

        val today = Calendar.getInstance()

        val created = Calendar.getInstance().apply {
            timeInMillis = seconds * 1000L
        }

        return today.get(Calendar.YEAR) == created.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == created.get(Calendar.DAY_OF_YEAR)
    }
}
