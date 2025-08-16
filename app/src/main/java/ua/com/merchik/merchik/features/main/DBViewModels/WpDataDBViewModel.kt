package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.Utils.CustomString
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainEvent
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.RangeDate
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenuAction
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenuState
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
                    if (contextUI == ContextUI.WP_DATA_IN_CONTAINER) RealmManager.getAllWorkPlan()
                    else RealmManager.getAllWorkPlanForRNO()

                val data: List<WpDataDB> = if (rawData.isNullOrEmpty()) {
                    emptyList()
                } else {
                    if (contextUI == ContextUI.WP_DATA_IN_CONTAINER)
                        subTitle =
                            CustomString.createTitleMsg(rawData, CustomString.TitleMode.SHORT)
                                .toString()
                    RealmManager.INSTANCE.copyFromRealm(rawData)
                }

                val dataUniqUser = data.distinctBy { it.user_id }
                    .let { list ->
                        if (contextUI == ContextUI.WP_DATA_IN_CONTAINER) {
                            list.filterNot { it.user_id == 14041 }
                        } else {
                            list
                        }
                    }
                val dataUniqAdress = data.distinctBy { it.addr_id }
                val client = CustomerRealm.getAll()
                val filterUsersSDB = ItemFilter(
                    "Виконавець",
                    UsersSDB::class,
                    UsersSDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "## USER",
                    "## sub title",
                    "user_txt",
                    "user_txt",
                    dataUniqUser.map { it.user_txt },
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
                    dataUniqAdress.map { it.addr_txt },
                    dataUniqAdress.map { it.addr_txt },
                    enabled = true
                )

                val filterClientSDB = ItemFilter(
                    "Клиент",
                    CustomerSDB::class,
                    CustomerSDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "## ADRESS",
                    "## sub title",
                    "client_id",
                    "client_txt",
                    client.map { it.id.toString() },
                    client.map { it.nm },
                    enabled = true
                )


                filters = Filters(
                    searchText = "",
                    items =
                        mutableListOf(
                            filterUsersSDB,
                            filterAddressSDB,
                            filterClientSDB
                        ),
                    rangeDataByKey = RangeDate(
                        key = "dt",
                        start = rangeDataStart.value,
                        end = rangeDataEnd.value,
                        enabled = true
                    )
                )
            }

            else -> {
                super.updateFilters()
            }
        }
    }

    override fun getItems(): List<DataItemUI> {
        launcher
        return try {
            when (contextUI) {
                ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER
                    -> {
                    val data = RealmManager.getAllWorkPlanForRNO()?.takeIf { it.isNotEmpty() }
                        ?.let { RealmManager.INSTANCE.copyFromRealm(it) } ?: emptyList()
                    val res = repository.toItemUIList(WpDataDB::class, data, contextUI, 0)
//                        .map {
//                            val selected = FilteringDialogDataHolder.instance()
//                                .filters
//                                ?.items
//                                ?.firstOrNull { it.clazz == table }
//                                ?.rightValuesRaw
//                                ?.contains((it.rawObj.firstOrNull { it is WpDataDB } as? WpDataDB)?.code_dad2.toString())
//                            it.copy(selected = selected == true)
//                        }
                    res

                }

                else -> {
                    val res = repository.getAllRealm(WpDataDB::class, contextUI, null)
//                        .map {
//                            val selected = FilteringDialogDataHolder.instance()
//                                .filters
//                                ?.items
//                                ?.firstOrNull { it.clazz == table }
//                                ?.rightValuesRaw
//                                ?.contains((it.rawObj.firstOrNull { it is WpDataDB } as? WpDataDB)?.code_dad2.toString())
//                            it.copy(selected = selected == true)
//                        }
//                    res
//                    val data = RealmManager.getAllWorkPlanWithOutRNO()?.takeIf { it.isNotEmpty() }
//                        ?.let { RealmManager.INSTANCE.copyFromRealm(it) } ?: emptyList()
//                    val res = repository.toItemUIList(WpDataDB::class, data, contextUI, 0)
//                        .map {
//                            val selected = FilteringDialogDataHolder.instance()
//                                .filters
//                                ?.items
//                                ?.firstOrNull { it.clazz == table }
//                                ?.rightValuesRaw
//                                ?.contains((it.rawObj.firstOrNull { it is WpDataDB } as? WpDataDB)?.code_dad2.toString())
//                            it.copy(selected = selected == true)
//                        }
                    res
                }

            }

        } catch (e: Exception) {
            repository.getAllRealm(WpDataDB::class, contextUI, null)
                .map {
                    val selected = FilteringDialogDataHolder.instance()
                        .filters
                        ?.items
                        ?.firstOrNull { it.clazz == table }
                        ?.rightValuesRaw
                        ?.contains((it.rawObj.firstOrNull { it is WpDataDB } as? WpDataDB)?.code_dad2.toString())
                    it.copy(selected = selected == true)
                }
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        FilteringDialogDataHolder.instance().filters.apply {
            this?.let { filters ->
                filters.items = filters.items?.map { itemFilter ->
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