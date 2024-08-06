package ua.com.merchik.merchik.features.main.DBViewModels

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.join
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class WpDataDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {
    override val contextUI: ContextUI
        get() = ContextUI.DEFAULT

    override val table: KClass<out DataObjectUI>
        get() = AddressSDB::class

    override fun getItems(): List<ItemUI> {
        val wpDataDBUI = repository.getAllRealm(WpDataDB::class, contextUI)
        val themeDBUI = repository.getAllRealm(ThemeDB::class, contextUI)
        val addressSDBUI = repository.getAllRoom(AddressSDB::class, contextUI)
        return wpDataDBUI
            .join(themeDBUI, "theme_id = id: nm, comment")
            .join(addressSDBUI, "addr_id = addr_id: nm")

//        return addressSDBUI
    }
}