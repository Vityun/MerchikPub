package ua.com.merchik.merchik.features.main.DBViewModels


import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.ViewHolders.AkciyaSelectionDataHolder
import ua.com.merchik.merchik.data.RealmModels.PromoDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.PromoRealm
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass


@HiltViewModel
class AkciyaDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override fun getDefaultHideUserFields(): List<String>? {
        return "ID".split(",")
    }

    override val table: KClass<out DataObjectUI>
        get() = PromoDB::class

    override fun updateFilters() {
        filters = Filters(
            rangeDataByKey = null,
            items = mutableListOf()
        )
    }

    override suspend fun getItems(): List<DataItemUI> {
        return try {
            val data = PromoRealm.getAllPromoDBList()

            repository.toItemUIList(PromoDB::class, data, contextUI, null)
                .map { item ->
                    when (contextUI) {
                        ContextUI.AKCIYA_FROM_TEXT_EDITOR -> {
                            val selectedId = AkciyaSelectionDataHolder.selectedId
                            val currentId =
                                (item.rawObj.firstOrNull { it is PromoDB } as? PromoDB)?.id

                            item.copy(selected = selectedId == currentId)
                        }

                        else -> item
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        when (contextUI) {
            ContextUI.AKCIYA_FROM_TEXT_EDITOR -> {
                val akciya = itemsUI.firstOrNull()?.rawObj?.firstOrNull() as? PromoDB
                AkciyaSelectionDataHolder.set(
                    id = akciya?.id,
                    name = akciya?.nm
                )
            }

            else -> Unit
        }
    }
}