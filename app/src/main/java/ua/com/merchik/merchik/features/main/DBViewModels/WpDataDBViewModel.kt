package ua.com.merchik.merchik.features.main.DBViewModels

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.join
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class WpDataDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = WpDataDB::class

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
            this?.let {filters ->
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