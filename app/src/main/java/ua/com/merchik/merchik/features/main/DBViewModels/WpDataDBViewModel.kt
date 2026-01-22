package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.Utils.CustomString
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainEvent
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.RangeDate
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenuAction
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenuState
import ua.com.merchik.merchik.features.maps.domain.filterByDistance
import ua.com.merchik.merchik.trecker
import javax.inject.Inject
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

    override fun getDefaultGroupUserFields(): List<String> =
        listOf(
            "dt",        // 1-й уровень группировки
            "addr_txt",  // 2-й уровень (если включишь вторую группировку)
//            "client_txt" // 3-й уровень (опционально)
        )

    override fun getDefaultSortUserFields(): List<String>? {
        return "dt, addr_txt, client_txt".split(",")
    }

    override fun getDefaultHideUserFields(): List<String>? {
        return "ID, user_txt, theme_id, client_start_dt, client_end_dt, sku, duration, doc_num_otchet, main_option_id, smeta".split(
            ","
        )
    }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        super.onClickItem(itemUI, context)
        val wp = try {
            val raw = itemUI.rawObj.firstOrNull()
            Gson().fromJson(Gson().toJson(raw), WpDataDB::class.java)
        } catch (e: Exception) {
            null
        } ?: return

        when (contextUI) {
            ContextUI.WP_DATA_IN_CONTAINER -> {
                viewModelScope.launch {
                    _events.emit(
                        MainEvent.ShowContextMenu(
                            menuState = ContextMenuState(
                                wpDataDB = wp,
                                actions = listOf(
                                    ContextMenuAction.OpenVisit,
                                    ContextMenuAction.Close
                                )
                            )
                        )
                    )
                }
            }

            ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER -> {
                viewModelScope.launch {
                    _events.emit(
                        MainEvent.ShowContextMenu(
                            menuState = ContextMenuState(
                                wpDataDB = wp,
                                actions = listOf(
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
                            )
                        )
                    )
                }
            }

            else -> {}
        }

    }

    override fun updateFilters() {
        when (contextUI) {
            ContextUI.WP_DATA_IN_CONTAINER,
            ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER
                -> {


                val rawData =
                    if (contextUI == ContextUI.WP_DATA_IN_CONTAINER) RealmManager.getAllWorkPlanWithOutRNO()
                    else RealmManager.getAllWorkPlanForRNO()


                val data: List<WpDataDB> = if (rawData.isNullOrEmpty()) {
                    emptyList()
                } else {
                    RealmManager.INSTANCE.copyFromRealm(rawData)
                }

                if (contextUI == ContextUI.WP_DATA_IN_CONTAINER) {
                    subTitle =
                        CustomString.createTitleMsg(
                            data,
                            CustomString.TitleMode.MIX
                        )
                            .toString()
                    subTitleLong =
                        CustomString.createTitleMsg(
                            data,
                            CustomString.TitleMode.FULL
                        )
                            .toString()
                }

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
                    "## USER",
                    "## sub title",
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
                    "Клиент",
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

                filters = Filters(
                    items =
                        mutableListOf(
                            filterUsersSDB,
                            filterAddressSDB,
                            filterClientSDB,
                            filterWPDataStatus
                        ),
//                    rangeDataByKey = null
                    rangeDataByKey = RangeDate(
                        key = "dt",
                        start = rangeDataStart.value,
                        end = rangeDataEnd.value,
                        enabled = true
                    )
                )
//                filters = null
            }

            else -> {
                super.updateFilters()
            }
        }
    }


//    override suspend fun getItemsHeader(): List<DataItemUI> {
//        if (getItems().isEmpty()) {
//            val wpDataDB = SamplePhotoSDB()
//            wpDataDB.nm = "test"
//            return repository.toItemUIList(
//                SamplePhotoSDB::class,
//                mutableListOf(wpDataDB),
//                contextUI,
//                null
//            )
//        } else return emptyList()
//    }


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
        return repository.toItemUIList(WpDataDB::class, raw, contextUI, 0, groupingKeys)

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
    }
}