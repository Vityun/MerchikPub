package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class SiteObjectsDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = SiteObjectsDB::class

    override fun getItems(): List<DataItemUI> {
        return repository.getAllRealm(SiteObjectsDB::class, contextUI, null)
    }
}