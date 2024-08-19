package ua.com.merchik.merchik.features.main.DBViewModels

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
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
    }
}