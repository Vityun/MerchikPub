package ua.com.merchik.merchik.features.main.DBViewModels

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class TovarDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {

    override val contextUI: ContextUI
        get() = ContextUI.ONE_SELECT

    override val table: KClass<out DataObjectUI>
        get() = TovarDB::class

    override fun getItems(): List<ItemUI> {
        return repository.getAllRealm(TovarDB::class, contextUI)
            .map {
                val selected = (it.rawObj.firstOrNull { it is TovarDB } as? TovarDB)?.getiD()?.toIntOrNull() == AchievementDataHolder.instance().tovarId
                it.copy(selected = selected)
            }
    }

    override fun onSelectedItemsUI(itemsUI: List<ItemUI>) {
        (itemsUI.first().rawObj.firstOrNull { it is TovarDB } as? TovarDB)?.let {
            AchievementDataHolder.instance().tovarId = it.getiD().toInt()
            AchievementDataHolder.instance().tovarName = it.nm
        }
    }

}