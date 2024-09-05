package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB
import ua.com.merchik.merchik.data.RealmModels.LogDB
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class ReportPrepareDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = ReportPrepareDB::class

    override fun getItems(): List<DataItemUI> {
        return repository.getAllRealm(ReportPrepareDB::class, contextUI, null)
            .map {
                val selected = FilteringDialogDataHolder.instance()
                    .filters
                    ?.items
                    ?.firstOrNull { it.clazz == table }
                    ?.rightValuesRaw
                    ?.contains((it.rawObj.firstOrNull { it is ReportPrepareDB } as? ReportPrepareDB)?.id.toString())
                it.copy(selected = selected == true)
            }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        FilteringDialogDataHolder.instance().filters.apply {
            this?.let {filters ->
                filters.items = filters.items?.map { itemFilter ->
                    if (itemFilter.clazz == table) {
                        val rightValuesRaw = mutableListOf<String>()
                        val rightValuesUI = mutableListOf<String>()
                        itemsUI.forEach {
                            (it.rawObj.firstOrNull() as? ReportPrepareDB)?.let {
                                rightValuesRaw.add(it.id.toString())
                                rightValuesUI.add(it.otchetNum)
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