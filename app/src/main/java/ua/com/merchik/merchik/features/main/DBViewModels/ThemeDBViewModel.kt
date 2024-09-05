package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class ThemeDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = ThemeDB::class

    override fun updateFilters() {
        val data = when(contextUI) {
            ContextUI.THEME_FROM_ACHIEVEMENT -> {
                val themeIDs: Array<String> = Gson().fromJson(dataJson, object : TypeToken<Array<String>>() {}.type)
                ThemeRealm.getThemeByIds(themeIDs)
            }
            else -> { emptyList() }
        }

        val filterThemeDB = ItemFilter(
            "Доп. фильтр",
            ThemeDB::class,
            ThemeDBViewModel::class,
            ModeUI.MULTI_SELECT,
            "Tемы",
            "subTitle",
            "id",
            "id",
            data.map { it.id },
            data.map { it.nm },
            true
        )

        filters = Filters(
            searchText = "",
            items = mutableListOf(
                filterThemeDB
            )
        )
    }

    override fun getItems(): List<DataItemUI> {
        return try
        {
            val data = ThemeRealm.getAll()
            repository.toItemUIList(ThemeDB::class, data, contextUI, null)
                .map {
                    when (contextUI) {
                        ContextUI.THEME_FROM_ACHIEVEMENT -> {
                            val selected = (it.rawObj.firstOrNull { it is ThemeDB } as? ThemeDB)?.id?.toIntOrNull() == AchievementDataHolder.instance().themeId
                            it.copy(selected = selected)
                        }
                        ContextUI.DEFAULT -> {
                            val selected = FilteringDialogDataHolder.instance()
                                .filters
                                ?.items
                                ?.firstOrNull { it.clazz == table }
                                ?.rightValuesRaw
                                ?.contains((it.rawObj.firstOrNull { it is ThemeDB } as? ThemeDB)?.id.toString())
                            it.copy(selected = selected == true)
                        }
                        else -> { it }
                    }

                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        when (contextUI) {
            ContextUI.THEME_FROM_ACHIEVEMENT -> {
                (itemsUI.first().rawObj.firstOrNull { it is ThemeDB } as? ThemeDB)?.let {
                    AchievementDataHolder.instance().themeId = it.id.toInt()
                    AchievementDataHolder.instance().themeName = it.nm
                }
            }
            ContextUI.DEFAULT -> {
                FilteringDialogDataHolder.instance().filters.apply {
                    this?.let {filters ->
                        filters.items = filters.items?.map { itemFilter ->
                            if (itemFilter.clazz == table) {
                                val rightValuesRaw = mutableListOf<String>()
                                val rightValuesUI = mutableListOf<String>()
                                itemsUI.forEach {
                                    (it.rawObj.firstOrNull() as? ThemeDB)?.let {
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
            else -> {}
        }
    }
}