package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
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
class CustomerSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = CustomerSDB::class

    override suspend fun getItems(): List<DataItemUI> {
        val data = repository.getAllRoom(CustomerSDB::class, contextUI, null)
            .map {
                val selected = FilteringDialogDataHolder.instance()
                    .filters
                    ?.items
                    ?.firstOrNull { it.clazz == table }
                    ?.rightValuesRaw
                    ?.contains((it.rawObj.firstOrNull { it is CustomerSDB } as? CustomerSDB)?.id.toString())
                it.copy(selected = selected == true)
            }
        return data
    }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        FilteringDialogDataHolder.instance().filters.apply {
            this?.let {filters ->
                filters.items = filters.items?.map { itemFilter ->
                    if (itemFilter.clazz == table) {
                        val rightValuesRaw = mutableListOf<String>()
                        val rightValuesUI = mutableListOf<String>()
                        itemsUI.forEach {
                            (it.rawObj.firstOrNull() as? CustomerSDB)?.let {
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
}