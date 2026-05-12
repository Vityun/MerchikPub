package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.ViewHolders.ErrorSelectionDataHolder
import ua.com.merchik.merchik.data.RealmModels.ErrorDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass


@HiltViewModel
class ErrorDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override fun getDefaultHideUserFields(): List<String>? {
        return "ID".split(",")
    }

    override val table: KClass<out DataObjectUI>
        get() = ErrorDB::class

    override fun updateFilters() {
        filters = Filters(
            rangeDataByKey = null,
            items = mutableListOf()
        )
    }

    override suspend fun getItems(): List<DataItemUI> {
        return try {
            val data = RealmManager.getAllErrorDbNotZero()

            repository.toItemUIList(ErrorDB::class, data, contextUI, null)
                .map { item ->
                    when (contextUI) {
                        ContextUI.DEFAULT -> {
                            val selected = FilteringDialogDataHolder.instance()
                                .filters
                                ?.items
                                ?.firstOrNull { it.clazz == table }
                                ?.rightValuesRaw
                                ?.contains(
                                    (item.rawObj.firstOrNull { it is ErrorDB } as? ErrorDB)?.id.toString()
                                )

                            item.copy(selected = selected == true)
                        }

                        ContextUI.ERROR_FROM_TEXT_EDITOR -> {
                            val selectedId = ErrorSelectionDataHolder.selectedId
                            val currentId =
                                (item.rawObj.firstOrNull { it is ErrorDB } as? ErrorDB)?.id?.toString()

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
            ContextUI.DEFAULT -> {
                FilteringDialogDataHolder.instance().filters?.let { filters ->
                    filters.items = filters.items.map { itemFilter ->
                        if (itemFilter.clazz == table) {
                            val rightValuesRaw = mutableListOf<String?>()
                            val rightValuesUI = mutableListOf<String?>()

                            itemsUI.forEach { item ->
                                (item.rawObj.firstOrNull() as? ErrorDB)?.let { error ->
                                    rightValuesRaw.add(error.id.toString())
                                    rightValuesUI.add(error.nm)
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

            ContextUI.ERROR_FROM_TEXT_EDITOR -> {
                val error = itemsUI.firstOrNull()?.rawObj?.firstOrNull() as? ErrorDB
                ErrorSelectionDataHolder.set(
                    id = error?.id,
                    name = error?.nm
                )
            }

            else -> Unit
        }
    }
}