package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
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


    override fun updateFilters() {
        when (contextUI) {
            ContextUI.WP_DATA_IN_CONTAINER -> {

                val data = RealmManager.getAllWorkPlan()?.takeIf { it.isNotEmpty() }
                    ?.let { RealmManager.INSTANCE.copyFromRealm(it) } ?: emptyList()

                val dataUniqUser = data.distinctBy { it.user_id }
                val dataUniqAdress = data.distinctBy { it.addr_id }

                val filterUsersSDB = ItemFilter(
                    "Доп. фильтр",
                    UsersSDB::class,
                    UsersSDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "## USER",
                    "## sub title",
                    "user_txt",
                    "user_txt",
                    dataUniqUser.map { it.user_txt },
                    dataUniqUser.map { it.user_txt },
                    true
                )

                val filterAddressSDB = ItemFilter(
                    "Доп. фильтр",
                    AddressSDB::class,
                    AddressSDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "## ADRESS",
                    "## sub title",
                    "addr_txt",
                    "addr_txt",
                    dataUniqAdress.map { it.addr_txt },
                    dataUniqAdress.map { it.addr_txt },
                    true
                )

                filters = Filters(
                    searchText = "",
                    items = mutableListOf(
                        filterUsersSDB,
                        filterAddressSDB

                    )
                )
            }

            else -> {
                super.updateFilters()
            }
        }
    }

    override fun getItems(): List<DataItemUI> {
        val wpDataDBUI = repository.getAllRealm(WpDataDB::class, contextUI, null)
            .map {
                val selected = FilteringDialogDataHolder.instance()
                    .filters
                    ?.items
                    ?.firstOrNull { it.clazz == table }
                    ?.rightValuesRaw
                    ?.contains((it.rawObj.firstOrNull { it is WpDataDB } as? WpDataDB)?.code_dad2.toString())
                it.copy(selected = selected == true)
            }

//        val themeDBUI = repository.getAllRealm(ThemeDB::class, contextUI, null)
//        val addressSDBUI = repository.getAllRoom(AddressSDB::class, contextUI, null)
//        return wpDataDBUI
//            .join(themeDBUI, "theme_id = id: nm, comment")
//            .join(addressSDBUI, "addr_id = addr_id: nm")

        return wpDataDBUI
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