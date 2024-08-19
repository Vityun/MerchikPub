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
        get() = AddressSDB::class

    override fun getItems(): List<DataItemUI> {
        val wpDataDBUI = repository.getAllRealm(WpDataDB::class, contextUI, null)
        val themeDBUI = repository.getAllRealm(ThemeDB::class, contextUI, null)
        val addressSDBUI = repository.getAllRoom(AddressSDB::class, contextUI, null)
        return wpDataDBUI
            .join(themeDBUI, "theme_id = id: nm, comment")
            .join(addressSDBUI, "addr_id = addr_id: nm")

//        return addressSDBUI
    }
}