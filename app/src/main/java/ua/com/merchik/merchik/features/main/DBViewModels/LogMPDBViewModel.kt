package ua.com.merchik.merchik.features.main.DBViewModels

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.RealmModels.LogDB
import ua.com.merchik.merchik.data.RealmModels.LogMPDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.UsersDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.join
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.features.main.Filters
import ua.com.merchik.merchik.features.main.MainViewModel
import ua.com.merchik.merchik.features.main.RangeDate
import java.time.LocalDate
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class LogMPDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {

    override val title: String
        get() = "Історія місцеположення"

    override val contextUI: ContextUI
        get() = ContextUI.DEFAULT

    override val table: KClass<out DataObjectUI>
        get() = LogMPDB::class

    override fun getItems(): List<ItemUI> {
        val logMPDBUI = repository.getAllRealm(LogMPDB::class, contextUI)
        return logMPDBUI
    }


    override fun getFilters(): Filters {
        return Filters(
            RangeDate("CoordTime", LocalDate.now(), LocalDate.now()),
            ""
        )
    }
}