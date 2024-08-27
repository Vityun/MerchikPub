package ua.com.merchik.merchik.features.main.DBViewModels

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class UsersSDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = UsersSDB::class

    override fun getItems(): List<DataItemUI> {
        return repository.getAllRoom(table, contextUI, null)
            .map {
                val selected = FilteringDialogDataHolder.instance()
                    .filters
                    ?.items
                    ?.firstOrNull { it.clazz == table }
                    ?.rightValuesRaw
                    ?.contains((it.rawObj.firstOrNull { it is UsersSDB } as? UsersSDB)?.id.toString())
                it.copy(selected = selected == true)
            }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        FilteringDialogDataHolder.instance().filters.apply {
            this?.let {
                this?.let {filters ->
                    filters.items = filters.items?.map { itemFilter ->
                        if (itemFilter.clazz == table) {
                            val rightValuesRaw = mutableListOf<String>()
                            val rightValuesUI = mutableListOf<String>()
                            itemsUI.forEach {
                                (it.rawObj.firstOrNull() as? UsersSDB)?.let {
                                    rightValuesRaw.add(it.id.toString())
                                    rightValuesUI.add(it.fio)
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
}