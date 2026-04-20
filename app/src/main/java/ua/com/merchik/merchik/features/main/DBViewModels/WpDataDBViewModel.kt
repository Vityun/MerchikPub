package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.HiddenMenuMode
import ua.com.merchik.merchik.dataLayer.LaunchOrigin
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.addrIdOrNull
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.model.ContextMenuActionEvent
import ua.com.merchik.merchik.dataLayer.model.ContextMenuActionIds
import ua.com.merchik.merchik.dataLayer.model.ContextMenuEntry
import ua.com.merchik.merchik.dataLayer.model.ContextMenuHeaderRow
import ua.com.merchik.merchik.dataLayer.model.ContextMenuHeaderUi
import ua.com.merchik.merchik.dataLayer.model.ContextMenuPayload
import ua.com.merchik.merchik.dataLayer.model.ContextMenuPresets
import ua.com.merchik.merchik.dataLayer.model.ContextMenuUiState
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.MenuLeading
import ua.com.merchik.merchik.dataLayer.model.SubmenuPresentation
import ua.com.merchik.merchik.dataLayer.model.rawAs
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.RangeDate
import ua.com.merchik.merchik.features.maps.data.mappers.WpSelectionDataHolder
import ua.com.merchik.merchik.features.maps.domain.filterByDistance
import ua.com.merchik.merchik.trecker
import javax.inject.Inject
import kotlin.random.Random
import kotlin.reflect.KClass

@HiltViewModel
class WpDataDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = WpDataDB::class
// РНО 14041

    override fun getDefaultGroupUserFields(): List<String> {
        return if (contextUI == ContextUI.WP_DATA)
            emptyList()
        else
            listOf(
                "dt",        // 1-й уровень группировки
                "addr_txt",  // 2-й уровень (если включишь вторую группировку)
                //            "client_txt" // 3-й уровень (опционально)
            )
    }

    override fun getDefaultSortUserFields(): List<String>? {
        return "dt, addr_txt, client_txt".split(",")
    }

    override fun getDefaultHideUserFields(): List<String>? {
        return if (contextUI == ContextUI.WP_DATA)
            "ID, user_txt, theme_id, client_start_dt, client_end_dt, sku, duration, doc_num_otchet, main_option_id, smeta, status".split(
                ","
            )
        else
            "ID, user_txt, theme_id, client_start_dt, client_end_dt, sku, duration, doc_num_otchet, main_option_id, smeta".split(
                ","
            )
    }


//    override fun onClickItem(itemUI: DataItemUI, context: Context) {
//        super.onClickItem(itemUI, context)
//
//        val wp = try {
//            val raw = itemUI.rawObj.firstOrNull()
//            Gson().fromJson(Gson().toJson(raw), WpDataDB::class.java)
//        } catch (e: Exception) {
//            null
//        } ?: return
//
//        when (contextUI) {
//            ContextUI.WP_DATA,
//            ContextUI.WP_DATA_IN_CONTAINER,
//            ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER -> {
//                viewModelScope.launch(Dispatchers.IO) {
//                    _events.emit(
//                        MainEvent.ShowLongClickContextMenu(
//                            menuState = LongClickContextMenuState(
//                                items = listOf(itemUI),
//                                clickedItem = itemUI,
//                                entries = buildWpLongClickEntries(listOf(itemUI)),
//                                headerWpDataDB = wp
//                            )
//                        )
//                    )
//                }
//            }
//
//            else -> {}
//        }
//    }

//    override fun onClickItem(itemUI: DataItemUI, context: Context) {
//        super.onClickItem(itemUI, context)
//        val wp = try {
//            val raw = itemUI.rawObj.firstOrNull()
//            Gson().fromJson(Gson().toJson(raw), WpDataDB::class.java)
//        } catch (e: Exception) {
//            null
//        } ?: return
//
//        when (contextUI) {
//
//            ContextUI.WP_DATA-> {
//                viewModelScope.launch {
//                    _events.emit(
//                        MainEvent.ShowContextMenu(
//                            menuState = ContextMenuState(
//                                wpDataDB = wp,
//                                item = itemUI,
//                                actions = listOf(
//                                    ContextMenuAction.OpenOrder,
//                                    ContextMenuAction.Close
//                                )
//                            )
//                        )
//                    )
//                }
//            }
//
//            ContextUI.WP_DATA_IN_CONTAINER -> {
//                viewModelScope.launch {
//                    _events.emit(
//                        MainEvent.ShowContextMenu(
//                            menuState = ContextMenuState(
//                                wpDataDB = wp,
//                                item = itemUI,
//                                actions = listOf(
//                                    ContextMenuAction.OpenVisit,
//                                    ContextMenuAction.Close
//                                )
//                            )
//                        )
//                    )
//                }
//            }
//
//            ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER -> {
//                viewModelScope.launch {
//                    _events.emit(
//                        MainEvent.ShowContextMenu(
//                            menuState = ContextMenuState(
//                                wpDataDB = wp,
//                                item = itemUI,
//                                actions = listOf(
//                                    ContextMenuAction.OpenOrder,
//                                    ContextMenuAction.Close
//                                )
//                            )
//                        )
//                    )
//                }
//            }
//
//            else -> {}
//        }
//
//    }

    override fun updateFilters() {
        val prev = uiState.value.filters ?: Filters()

        when (contextUI) {
            ContextUI.WP_DATA_IN_CONTAINER,
            ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER,
                -> {


                val rawData =
                    if (contextUI == ContextUI.WP_DATA_IN_CONTAINER) RealmManager.getAllWorkPlanWithOutRNO()
                    else RealmManager.getAllWorkPlanForRNO()


                val data: List<WpDataDB> = if (rawData.isNullOrEmpty()) {
                    emptyList()
                } else {
                    RealmManager.INSTANCE.copyFromRealm(rawData)
                }

//                if (contextUI == ContextUI.WP_DATA_IN_CONTAINER) {
//                    subTitle =
//                        CustomString.createTitleMsg(
//                            data,
//                            CustomString.TitleMode.MIX
//                        )
//                            .toString()
//                    subTitleLong =
//                        CustomString.createTitleMsg(
//                            data,
//                            CustomString.TitleMode.FULL
//                        )
//                            .toString()
//                }

                val dataUniqUser = data.distinctBy { it.user_id }
                    .let { list ->
                        if (contextUI == ContextUI.WP_DATA_IN_CONTAINER) {
                            list.filterNot { it.user_id == 14041 }
                        } else {
                            list
                        }
                    }
                val dataUniqAdress =
                    if (contextUI == ContextUI.WP_DATA_IN_CONTAINER) data.distinctBy { it.addr_id } else emptyList()
                val client =
                    if (contextUI == ContextUI.WP_DATA_IN_CONTAINER) RoomManager.SQL_DB.customerDao().all else emptyList()
                val filterUsersSDB = ItemFilter(
                    "Виконавець",
                    UsersSDB::class,
                    UsersSDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "Виконавець",
                    "Додати Виконавеця",
                    "user_txt",
                    "user_txt",
                    dataUniqUser.map { it.user_txt.toString() },
                    dataUniqUser.map { it.user_txt },
                    contextUI == ContextUI.WP_DATA_IN_CONTAINER
                )


                val filterAddressSDB = ItemFilter(
                    "Адреса",
                    AddressSDB::class,
                    AddressSDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "## ADRESS",
                    "## sub title",
                    "addr_txt",
                    "addr_txt",
                    dataUniqAdress.map { it.addr_txt.toString() },
                    dataUniqAdress.map { it.addr_txt },
                    enabled = true
                )

                val filterClientSDB = ItemFilter(
                    "Клієнт",
                    CustomerSDB::class,
                    CustomerSDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "Додати Клієнта",
                    "## sub title",
                    "client_txt",
                    "nm",
                    client.map { it.nm.toString() },
                    client.map { it.nm },
                    enabled = true
                )

                data.forEach { wpDataDB ->
                    // Определяем статус статуса
                    val statusComment = try {
                        if (wpDataDB.status == 1) {
                            "Роботу виконано (звiт проведено)"
                        } else {
                            if (wpDataDB.visit_start_dt > 0) {
                                if (wpDataDB.visit_end_dt > 0) {
                                    // Меняем статус с 0 на 2 для "Роботу виконано (звiт не проведено)"
                                    if (wpDataDB.status == 0) {
                                        wpDataDB.status = 2
                                    }
                                    "Роботу виконано (звiт не проведено)"
                                } else {
                                    // Меняем статус с 0 на 3 для "Робота виконується (звiт не проведено)"
                                    if (wpDataDB.status == 0) {
                                        wpDataDB.status = 3
                                    }
                                    "Робота виконується (звiт не проведено)"
                                }
                            } else {
                                "Робота не розпочата (звiт не проведено)"
                            }
                        }
                    } catch (e: Exception) {
                        "Дані відсутні"
                    }
                    // Устанавливаем статус комментарий
                    wpDataDB.statusComment = statusComment
                }

                val filterWPDataStatus = ItemFilter(
                    "Статус робiт",
                    WpDataDB::class,
                    WpDataDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "## STATUS",
                    "## sub title",
                    "status",
                    "status",
                    data.map { it.status.toString() }.distinct(),
                    data.map { it.statusComment }.distinct(),
                    enabled = true
                )

//                if (contextUI == ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER) {
//                    if (rangeDataEnd.value != null)
//                        setEndDate(LocalDate.now().plusDays(3))
//                }

                val newFilters = prev.copy(
                    items =
                        mutableListOf(
                            filterUsersSDB,
                            filterAddressSDB,
                            filterClientSDB,
                            filterWPDataStatus
                        ),
                    rangeDataByKey = RangeDate(
                        key = "dt",
                        start = rangeDataStart.value,
                        end = rangeDataEnd.value,
                        enabled = true
                    )
                )
                updateFilters(newFilters)
            }

            ContextUI.WP_DATA -> {


                val root = Gson().fromJson(dataJson, JsonObject::class.java)

                val addrId = (
                        root.get("addressId")
                            ?: root.getAsJsonObject("nameValuePairs")?.get("addressId")
                        ).let { el ->
                        if (el == null || el.isJsonNull) null else el.asInt
                    } ?: error("addressId missing in dataJson: $root")

                val data = RealmManager.getAllWorkPlanByAddressForRNO(addrId)


                val dataUniqUser = data.distinctBy { it.user_id }

//                subTitle =
//                    CustomString.createTitleMsg(
//                        data,
//                        CustomString.TitleMode.MIX
//                    )
//                        .toString()
//                subTitleLong =
//                    CustomString.createTitleMsg(
//                        data,
//                        CustomString.TitleMode.FULL
//                    )
//                        .toString()

                val filterUsersSDB = ItemFilter(
                    "Виконавець",
                    UsersSDB::class,
                    UsersSDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "## USER",
                    "## sub title",
                    "user_txt",
                    "user_txt",
                    dataUniqUser.map { it.user_txt.toString() },
                    dataUniqUser.map { it.user_txt },
                    false
                )

                val dataUniqAdress =
                    data.distinctBy { it.addr_id }
                val client =
                    data.distinctBy { it.client_id }
//                     RoomManager.SQL_DB.customerDao().all


                val filterAddressSDB = ItemFilter(
                    "Адреса",
                    AddressSDB::class,
                    AddressSDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "## ADRESS",
                    "## sub title",
                    "addr_txt",
                    "addr_txt",
                    dataUniqAdress.map { it.addr_txt.toString() },
                    dataUniqAdress.map { it.addr_txt },
                    enabled = false
                )

                val filterClientSDB = ItemFilter(
                    "Клієнт",
                    CustomerSDB::class,
                    CustomerSDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "Додати Клієнта",
                    "## sub title",
                    "client_txt",
                    "client_txt",
                    client.map { it.client_txt },
                    client.map { it.client_txt },
                    enabled = true
                )
                val newFilters = prev.copy(
                    items =
                        mutableListOf(
                            filterUsersSDB,
                            filterAddressSDB,
                            filterClientSDB,
//                            filterWPDataStatus
                        ),
//                    rangeDataByKey = null
                    rangeDataByKey = RangeDate(
                        key = "dt",
                        start = rangeDataStart.value,
                        end = rangeDataEnd.value,
                        enabled = true,
                    )
                )
                updateFilters(newFilters)
            }

            else -> {
                super.updateFilters()
            }
        }
    }

    override suspend fun getItems(): List<DataItemUI> {
        Log.e("!!!!!!TEST!!!!!!", "getItems: start")
        val raw: List<WpDataDB> = when (contextUI) {

            ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER -> {
                val data = RealmManager.getAllWorkPlanForRNO()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { RealmManager.INSTANCE.copyFromRealm(it) }
                    ?: emptyList()

                Globals.writeToMLOG(
                    "INFO",
                    "WpDataDBViewModel.getItems",
                    "ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER"
                )
                var location: Location? = null
                if (trecker.imHereGPS != null) {
                    location = trecker.imHereGPS
                } else if (trecker.imHereNET != null) {
                    location = trecker.imHereNET
                } else if (context != null) {
                    val client = LocationServices.getFusedLocationProviderClient(context!!)
                    val last = runCatching { client.lastLocation.await() }.getOrNull()
                    location = last
                }
                if (location != null)
                    filterByDistance(location, data, offsetDistanceMeters.value)
                else
                    data

            }

            ContextUI.WP_DATA -> {
                val root = Gson().fromJson(dataJson, JsonObject::class.java)

                val addrId = (
                        root.get("addressId")
                            ?: root.getAsJsonObject("nameValuePairs")?.get("addressId")
                        ).let { el ->
                        if (el == null || el.isJsonNull) null else el.asInt
                    } ?: error("addressId missing in dataJson: $root")

                val data = RealmManager.getAllWorkPlanByAddressForRNO(addrId)
                data
            }

            else -> {
                Globals.writeToMLOG(
                    "INFO",
                    "WpDataDBViewModel.getItems",
                    "ContextUI is not WP_DATA_ADDITIONAL_IN_CONTAINER"
                )
                RealmManager.getAllWorkPlanWithOutRNO()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { RealmManager.INSTANCE.copyFromRealm(it) }
                    ?: emptyList()
            }
        }

        raw.forEach { wpDataDB ->
            // Определяем статус статуса
            val statusComment = try {
                if (wpDataDB.status == 1) {
                    "Роботу виконано (звiт проведено)"
                } else {
                    if (wpDataDB.visit_start_dt > 0) {
                        if (wpDataDB.visit_end_dt > 0) {
                            // Меняем статус с 0 на 2 для "Роботу виконано (звiт не проведено)"
                            if (wpDataDB.status == 0) {
                                wpDataDB.status = 2
                            }
                            "Роботу виконано (звiт не проведено)"
                        } else {
                            // Меняем статус с 0 на 3 для "Робота виконується (звiт не проведено)"
                            if (wpDataDB.status == 0) {
                                wpDataDB.status = 3
                            }
                            "Робота виконується (звiт не проведено)"
                        }
                    } else {
                        "Робота не розпочата (звiт не проведено)"
                    }
                }
            } catch (e: Exception) {
                "Дані відсутні"
            }
            // Устанавливаем статус комментарий
            wpDataDB.statusComment = statusComment
        }

        val state = uiState.value
        var groupingKeys: List<String> =
            state.sortingFields
                .filter { it.group && !it.key.isNullOrBlank() }
                .map { it.key!! }
        if (groupingKeys.isEmpty()) {

        }

        // 3) Тяжёлое преобразование тоже на IO
        Globals.writeToMLOG("INFO", "WpDataDBViewModel.getItems", "raw size: ${raw.size}")
        val filter = FilteringDialogDataHolder.instance().filters

        return repository.toItemUIList(WpDataDB::class, raw, contextUI, 0, groupingKeys)
            .map {
                when (contextUI) {
                    ContextUI.WP_DATA -> {
                        val selected = Random.nextBoolean()
                        FilteringDialogDataHolder.instance()
                            .filters
//                        val selected = filter
                            ?.items
                            ?.firstOrNull { it.clazz == table }
                            ?.rightValuesRaw
                            ?.contains((it.rawObj.firstOrNull { it is WpDataDB } as? WpDataDB)?.code_dad2.toString())
                        it.copy(selected = true)
                    }

                    else -> {
                        val selected = filter
                            ?.items
                            ?.firstOrNull { it.clazz == table }
                            ?.rightValuesRaw
                            ?.contains((it.rawObj.firstOrNull { it is WpDataDB } as? WpDataDB)?.code_dad2.toString())
                        it.copy(selected = selected == true)
                    }
                }
            }

    }


    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {

        FilteringDialogDataHolder.instance().filters.apply {
            this?.let { filters ->
                filters.items = filters.items.map { itemFilter ->
                    if (itemFilter.clazz == table) {
                        val rightValuesRaw = mutableListOf<String>()
                        val rightValuesUI = mutableListOf<String>()
                        itemsUI.forEach {
                            (it.rawObj.firstOrNull() as? WpDataDB)?.let {
                                rightValuesRaw.add(it.code_dad2.toString())
                                rightValuesUI.add(it.code_dad2.toString())
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

        val selectedWp = itemsUI.mapNotNull { ui ->
            when (val ro = ui.rawObj) {
                is WpDataDB -> ro
                is List<*> -> ro.firstOrNull() as? WpDataDB
                else -> null
            }
        }.distinctBy { it.id }
        if (contextUI == ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER ||
            contextUI == ContextUI.WP_DATA
        ) {
            WpSelectionDataHolder.instance().set(selectedWp)
            showAdditionalEarningsDialog(selectedWp)
        }
        showServiceContextMenu(itemsUI)
    }

    private val hiddenMenuMode: HiddenMenuMode = HiddenMenuMode.INLINE

    private fun hiddenPresentation(): SubmenuPresentation =
        when (hiddenMenuMode) {
            HiddenMenuMode.INLINE -> SubmenuPresentation.INLINE_EXPAND
            HiddenMenuMode.OVERLAY -> SubmenuPresentation.OVERLAY
            HiddenMenuMode.REPLACE -> SubmenuPresentation.REPLACE
        }


//    private fun buildHeader(items: List<DataItemUI>): ContextMenuHeaderUi {
//        val item = items.first()
//
//        val wp = item.rawAs<WpDataDB>()
//        val theme = item.rawAs<ThemeDB>()
//
//        val clientText = item.fieldValueOrNull("client_txt")
//            ?: wp?.client_txt
//            ?: theme?.nm
//            ?: ""
//
//        val addressText = item.fieldValueOrNull("addr_txt")
//            ?: wp?.addr_txt
//            ?: ""
//
//        val rows = buildList {
//            if (clientText.isNotBlank()) {
//                add(
//                    ContextMenuHeaderRow(
//                        label = if (items.size > 1) "з ${items.size} елементiв" else "Клієнт",
//                        value = if (items.size > 1) "" else clientText
//                    )
//                )
//            }
//
//            if (addressText.isNotBlank()) {
//                add(
//                    ContextMenuHeaderRow(
//                        label = "За адресою",
//                        value = addressText
//                    )
//                )
//            }
//        }
//
//        return ContextMenuHeaderUi(
//            visible = rows.isNotEmpty(),
//            title = if (items.size > 1) {
//                "Виберіть дію для групи з ${items.size} вiзитiв"
//            } else {
//                "Виберіть дію для вiзиту"
//            },
//            rows = rows
//        )
//    }

    private fun buildHeader(items: List<DataItemUI>): ContextMenuHeaderUi {
        val groupingFields = uiState.value.groupingFields
            .filter { !it.key.isNullOrBlank() }
            .sortedBy { it.priority }

        val sortingFields = uiState.value.sortingFields
            .filter { !it.key.isNullOrBlank() }

        val groupingRows = buildList {
            groupingFields.forEach { groupingField ->
                val key = groupingField.key ?: return@forEach
                val sortingTitle = sortingFields
                    .firstOrNull { it.key == key }
                    ?.title

                var label = groupingField.title?.takeIf { it.isNotBlank() } ?: key
                if (label == "dt")
                    label = "дата"
                if (label == "addr_txt")
                    label = "адреса"
//                val label = sortingTitle?.takeIf { it.isNotBlank() }
//                    ?: groupingField.title?.takeIf { it.isNotBlank() }
//                    ?: "key"

                val values = items
                    .mapNotNull { it.fieldDisplayValueOrNull(key) }
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()


                if (values.isNotEmpty()) {
                    val value = when {
                        values.size == 1 -> values.first()
                        values.size <= 3 -> values.joinToString(", ")
                        else -> values.take(3).joinToString(", ") + " …"
                    }
                    add(
                        ContextMenuHeaderRow(
                            label = label,
                            value = value
                        )
                    )
                }
            }
        }

        val rows = groupingRows.ifEmpty {
            val item = items.first()

            val wp = item.rawAs<WpDataDB>()
            val theme = item.rawAs<ThemeDB>()

            val clientText = item.fieldDisplayValueOrNull("client_txt")
                ?: wp?.client_txt
                ?: theme?.nm
                ?: ""

            val addressText = item.fieldDisplayValueOrNull("addr_txt")
                ?: wp?.addr_txt
                ?: ""

            buildList {
                if (clientText.isNotBlank()) {
                    add(
                        ContextMenuHeaderRow(
                            label = if (items.size > 1) "з ${items.size} елементiв" else "Клієнт",
                            value = if (items.size > 1) "" else clientText
                        )
                    )
                }

                if (addressText.isNotBlank()) {
                    add(
                        ContextMenuHeaderRow(
                            label = "За адресою",
                            value = addressText
                        )
                    )
                }
            }
        }

        return ContextMenuHeaderUi(
            visible = rows.isNotEmpty(),
            title = if (items.size > 1) {
                "Виберіть дію для групи з ${items.size} вiзитiв"
            } else {
                "Виберіть дію для вiзиту"
            },
            rows = rows
        )
    }


    private fun buildEntries(payload: ContextMenuPayload): List<ContextMenuEntry> {
        val isActiveGrouped = uiState.value.groupingFields.isNotEmpty()

        val items = payload.selectedItems
        val first = items.first()

        val payloadIds = items.map { it.stableId }.toSet()
        val itemsTemp = uiState.value.items

        val allItems =
            filterAndSortDataItems(
                items = itemsTemp,
                filters = uiState.value.filters,
                sortingFields = uiState.value.sortingFields,
                groupingFields = uiState.value.groupingFields,
                rangeStart = rangeDataStart.value,
                rangeEnd = rangeDataEnd.value,
                searchText = uiState.value.filters?.searchText
            ).items


        val selectedInPayload = allItems.filter { it.stableId in payloadIds && it.selected }
        val unselectedInPayload = allItems.filter { it.stableId in payloadIds && !it.selected }

        val selectedGlobal = allItems.filter { it.selected }
        val unselectedGlobal = allItems.filter { !it.selected }

        val selectedInPayloadCount = selectedInPayload.size
        val unselectedInPayloadCount = unselectedInPayload.size

        val selectedGlobalCount = selectedGlobal.size
        val unselectedGlobalCount = unselectedGlobal.size

        val wp = first.rawAs<WpDataDB>()
        val isMulti = items.size > 1

        val canEdit = !isMulti && wp != null

        val markTitle = if (isMulti) {
            "Позначити ($unselectedInPayloadCount)"
        } else {
            "Позначити"
        }

        val unmarkTitle = if (isMulti) {
            "Зняти позначку ($selectedInPayloadCount)"
        } else {
            "Зняти позначку"
        }

        val markAllTitle = "Позначити усі ($unselectedGlobalCount)"
        val unmarkAllTitle = "Зняти всi позначки ($selectedGlobalCount)"

        val selectionEntries = listOf(
            ContextMenuPresets.Mark.toEntry(
                id = "selection_mark",
                title = markTitle,
                enabled = unselectedInPayloadCount > 0
            ),
            ContextMenuPresets.MarkAll.toEntry(
                id = "selection_mark_all",
                title = markAllTitle,
                enabled = unselectedGlobalCount > 0
            ),
            ContextMenuPresets.Unmark.toEntry(
                id = "selection_unmark",
                title = unmarkTitle,
                enabled = selectedInPayloadCount > 0
            ),
            ContextMenuPresets.UnmarkAll.toEntry(
                id = "selection_unmark_all",
                title = unmarkAllTitle,
                enabled = selectedGlobalCount > 0
            ),
            ContextMenuPresets.Invert.toEntry(
                id = "selection_invert",
                enabled = payloadIds.isNotEmpty()
            )
        )

        val serviceEntries = buildServiceEntries(payload)

        val listEntries = listOf(
            ContextMenuPresets.CollapseList.toEntry(
                id = "list_collapse"
            ),
            ContextMenuPresets.ExpandList.toEntry(
                id = "list_expand"
            )
        )

        return buildList {
            add(
                ContextMenuPresets.Edit.toEntry(
                    id = "root_edit",
                    title = "Змiнити/Вiдкрити",
                    enabled = canEdit
                )
            )

            add(ContextMenuEntry.Divider("divider_main_1"))

            add(
                ContextMenuEntry.Submenu(
                    id = "submenu_selection",
                    title = "Позначення элементiв",
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_multiple_select),
                    items = selectionEntries,
                    presentation = hiddenPresentation(),
                    expandedByDefault = false
                )
            )

            add(ContextMenuEntry.Divider("divider_main_3"))

            if (!isActiveGrouped) {
                add(
                    ContextMenuPresets.ListSettings.toEntry(
                        id = "list_settings"
                    )
                )
            } else {
                add(
                    ContextMenuEntry.Submenu(
                        id = "submenu_list",
                        title = "Групування",
                        leading = MenuLeading.DrawableIcon(R.drawable.ic_sort_down),
                        items = listEntries,
                        presentation = hiddenPresentation(),
                        expandedByDefault = false
                    )
                )
            }

            add(ContextMenuEntry.Divider("divider_main_2"))

            if (serviceEntries.isNotEmpty()) {
                add(
                    ContextMenuEntry.Submenu(
                        id = "submenu_service",
                        title = "Обрати дію",
                        leading = MenuLeading.DrawableIcon(R.drawable.ic_21),
                        items = buildServiceMenuEntries(payload),
                        presentation = SubmenuPresentation.OVERLAY,
                        expandedByDefault = false
                    )
                )
            } else {
                add(
                    ContextMenuPresets.AdditionalWorkAdd.toEntry()
                )
            }

            add(ContextMenuEntry.Divider("divider_main_4"))

            add(
                ContextMenuPresets.Close.toEntry(
                    id = "root_close"
                )
            )
        }
    }

    private fun buildServiceMenuEntries(payload: ContextMenuPayload): List<ContextMenuEntry> {
        val serviceEntries = buildServiceEntries(payload)

        if (serviceEntries.isEmpty()) {
            return listOf(
                ContextMenuPresets.Close.toEntry(
                    id = "service_root_close"
                )
            )
        }

        return buildList {
            addAll(serviceEntries)
            add(ContextMenuEntry.Divider("service_root_divider_close"))
            add(
                ContextMenuPresets.Close.toEntry(
                    id = "service_root_close"
                )
            )
        }
    }

    private fun buildServiceEntries(payload: ContextMenuPayload): List<ContextMenuEntry> {
        val items = payload.selectedItems
        val selectedItems = items.filter { it.selected }
        val selectedCount = selectedItems.size
        val hasSelectedInClickedCard = selectedCount > 0

        return when (contextUI) {
            ContextUI.WP_DATA_IN_CONTAINER -> listOf(
                ContextMenuPresets.RejectWork.toEntry(
                    id = "service_reject_work",
                    title = "Відмовитися від роботи ($selectedCount)",
                    enabled = hasSelectedInClickedCard,
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_hide)
                ),
                ContextMenuPresets.RejectClient.toEntry(
                    id = "service_reject_client",
                    title = "Відмовитися від клієнта",
                    enabled = hasSelectedInClickedCard,
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_hide)
                ),
                ContextMenuPresets.RejectAddress.toEntry(
                    id = "service_reject_address",
                    title = "Відмовитися від адреси",
                    enabled = hasSelectedInClickedCard,
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_hide)
                ),
                ContextMenuPresets.AskMoreMoney.toEntry(
                    id = "service_ask_more_money",
                    title = "Запросити більшу оплату",
                    enabled = hasSelectedInClickedCard,
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_money)
                )
            )

            ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER -> listOf(
                ContextMenuPresets.AdditionalWorkAdd.toEntry(
                    id = "service_apply_request",
                    title = "Подати заявку ($selectedCount)",
                    enabled = hasSelectedInClickedCard
                ),
                ContextMenuPresets.AdditionalWorkDellAddress.toEntry(
                    id = "service_hide_address",
                    title = "Не показувати адресу",
                    enabled = hasSelectedInClickedCard
                ),
                ContextMenuPresets.AdditionalWorkDellClient.toEntry(
                    id = "service_hide_client",
                    title = "Не показувати клієнта",
                    enabled = hasSelectedInClickedCard
                ),
                ContextMenuEntry.Divider("service_divider_2"),
                ContextMenuPresets.OpenSmsPlan.toEntry(
                    id = "service_sms_plan"
                )
            )

            else -> emptyList()
        }
    }

    override fun onContextMenuAction(event: ContextMenuActionEvent) {
        when (event.actionId) {
            ContextMenuActionIds.EDIT -> {
                val wp = event.payload.firstItem.rawAs<WpDataDB>() ?: return
                openDetailedReport(wp.id)
            }

            ContextMenuActionIds.OPEN_ORDER -> {
                val wp = event.payload.firstItem.rawAs<WpDataDB>() ?: return
                openDetailedReport(wp.id)
            }

            ContextMenuActionIds.OPEN_UFMD_SELECTOR -> {
                openUFMDSelector(
                    addressId = event.payload.firstItem.addrIdOrNull(),
                    origin = event.payload.origin
                )
            }

            ContextMenuActionIds.MARK -> {
                updateItemsSelect(
                    ids = event.payload.selectedItems.map { it.stableId },
                    checked = true
                )
            }

            ContextMenuActionIds.MARK_ALL -> {
                updateItemsSelect(
                    ids = uiState.value.items.map { it.stableId },
                    checked = true
                )
            }

            ContextMenuActionIds.UNMARK -> {
                updateItemsSelect(
                    ids = event.payload.selectedItems.map { it.stableId },
                    checked = false
                )
            }

            ContextMenuActionIds.UNMARK_ALL -> {
                updateItemsSelect(
                    ids = uiState.value.items.map { it.stableId },
                    checked = false
                )
            }

            ContextMenuActionIds.INVERT -> {
//                val targetIds = event.payload.items.map { it.stableId }.toSet()

                val selectedIds = uiState.value.items
                    .filter { it.selected }
                    .map { it.stableId }

                val unselectedIds = uiState.value.items
                    .filter { !it.selected }
                    .map { it.stableId }

                if (selectedIds.isNotEmpty()) {
                    updateItemsSelect(
                        ids = selectedIds,
                        checked = false
                    )
                }

                if (unselectedIds.isNotEmpty()) {
                    updateItemsSelect(
                        ids = unselectedIds,
                        checked = true
                    )
                }
            }

            ContextMenuActionIds.COLLAPSE_LIST -> {
                collapseAllDecks()
            }

            ContextMenuActionIds.EXPAND_LIST -> {
                expandAllDecks()
            }

            ContextMenuActionIds.OPEN_SMS_PLAN -> {
                openSmsPlanDirectory()
            }

            ContextMenuActionIds.LIST_SETTINGS -> {
                openSortingDialog()
            }

            ContextMenuActionIds.CLOSE -> {
                hideContextMenu()
            }

            ContextMenuActionIds.ADDITIONAL_ADD -> {
                openAdditionalWorkDialog()
            }
        }
    }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        super.onClickItem(itemUI, context)
        val wp = itemUI.rawAs<WpDataDB>() ?: return
        openDetailedReport(wp.id)
    }

    override fun onLongClickItem(itemUI: DataItemUI, context: Context) {
        showItemContextMenu(
            items = listOf(itemUI)
        )
    }

    override fun onLongClickItems(
        items: List<DataItemUI>,
        context: Context,
        clickedItem: DataItemUI
    ) {
        showItemContextMenu(
            items = items
        )
    }

    private fun showItemContextMenu(
        items: List<DataItemUI>,
        origin: LaunchOrigin? = null,
        deckId: String? = null
    ) {
        if (items.isEmpty()) return

        val payload = ContextMenuPayload(
            selectedItems = items,
            deckId = deckId,
            origin = origin
        )

        showContextMenu(
            ContextMenuUiState(
                payload = payload,
                header = buildHeader(items),
                entries = buildEntries(payload)
            )
        )
    }

    private fun DataItemUI.fieldDisplayValueOrNull(key: String): String? {
        return fields.firstOrNull { it.key.equals(key, ignoreCase = true) }
            ?.value
            ?.value
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: rawFields.firstOrNull { it.key.equals(key, ignoreCase = true) }
                ?.value
                ?.rawValue
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
    }

    private fun showServiceContextMenu(
        items: List<DataItemUI>,
        origin: LaunchOrigin? = null,
        deckId: String? = null
    ) {
        if (items.isEmpty()) return

        val payload = ContextMenuPayload(
            selectedItems = items,
            deckId = deckId,
            origin = origin
        )

        showContextMenu(
            ContextMenuUiState(
                payload = payload,
                header = buildHeader(items),
                entries = buildServiceMenuEntries(payload)
            )
        )
    }
}
