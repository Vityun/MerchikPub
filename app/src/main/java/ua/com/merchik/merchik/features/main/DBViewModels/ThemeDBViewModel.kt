package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.ViewHolders.Clicks.click
import ua.com.merchik.merchik.ViewHolders.TextViewClickAdapter
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.dialogs.DialogData
import ua.com.merchik.merchik.dialogs.DialogData.DialogClickListener
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

    private val defaultAchievementThemeIds = arrayOf("595", "1252", "1251", "1378")

    override fun getDefaultHideUserFields(): List<String>? {
        return "ID, comment, column_name".split(",")
    }

    override fun updateFilters() {
        val data = when(contextUI) {
            ContextUI.THEME_FROM_ACHIEVEMENT-> {
                loadThemesByIdsSafe(parseAchievementThemeIds())
            }
            else -> { emptyList() }
        }

        val filterThemeDB = ItemFilter(
            "Доп. фильтр",
            ThemeDB::class,
            ThemeDBViewModel::class,
            ModeUI.MULTI_SELECT,
            "Вид достижения",
            "Выберите характер достижения, которое Вы выполнили",
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

    override suspend fun getItems(): List<DataItemUI> {
        return try
        {
            val data = loadThemesSafe(oprosOnly = contextUI == ContextUI.ADD_THEME_QUESTION_ANSWER)
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
            Globals.writeToMLOG("ERROR", "ThemeDBViewModel/getItems", "Exception: $e, contextUI=$contextUI")
            emptyList()
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        when (contextUI) {
            ContextUI.THEME_FROM_ACHIEVEMENT -> {
                val theme = itemsUI.firstOrNull()?.rawObj?.firstOrNull { it is ThemeDB } as? ThemeDB
                val themeId = theme?.id?.trim()?.toIntOrNull()

                if (theme == null || themeId == null) {
                    val message = "Не удалось выбрать тему достижения. Данные справочника тем повреждены или не загружены."
                    Log.e("ThemeDBViewModel", "THEME_FROM_ACHIEVEMENT invalid theme=$theme, items=${itemsUI.size}")
                    Globals.writeToMLOG(
                        "ERROR",
                        "ThemeDBViewModel/onSelectedItemsUI",
                        "Invalid theme for achievement. themeId=${theme?.id}, themeName=${theme?.nm}, items=${itemsUI.size}"
                    )
                    Toast.makeText(getApplication<Application>(), message, Toast.LENGTH_LONG).show()
                    return
                }

                AchievementDataHolder.instance().themeId = themeId
                AchievementDataHolder.instance().themeName = theme.nm
            }
            ContextUI.DEFAULT -> {
                FilteringDialogDataHolder.instance().filters.apply {
                    this?.let {filters ->
                        filters.items = filters.items.map { itemFilter ->
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

    private fun parseAchievementThemeIds(): Array<String> {
        val json = dataJson
        if (json.isNullOrBlank()) {
            Globals.writeToMLOG(
                "ERROR",
                "ThemeDBViewModel/parseAchievementThemeIds",
                "dataJson is empty. Fallback to default theme ids."
            )
            return defaultAchievementThemeIds
        }

        return try {
            Gson().fromJson(json, Array<String>::class.java)
                ?.mapNotNull { it?.trim()?.takeIf(String::isNotBlank) }
                ?.distinct()
                ?.toTypedArray()
                ?.takeIf { it.isNotEmpty() }
                ?: defaultAchievementThemeIds
        } catch (e: Exception) {
            Globals.writeToMLOG(
                "ERROR",
                "ThemeDBViewModel/parseAchievementThemeIds",
                "Exception: $e, dataJson=$json"
            )
            defaultAchievementThemeIds
        }
    }

    private fun loadThemesSafe(oprosOnly: Boolean): List<ThemeDB> {
        return try {
            if (oprosOnly) {
                ThemeRealm.getAllOpros()
            } else {
                ThemeRealm.getAll()
            }
        } catch (e: Exception) {
            Globals.writeToMLOG("ERROR", "ThemeDBViewModel/loadThemesSafe", "Exception: $e")
            emptyList()
        }
    }

    private fun loadThemesByIdsSafe(ids: Array<String>): List<ThemeDB> {
        return try {
            ThemeRealm.getThemeByIds(ids)
        } catch (e: Exception) {
            Globals.writeToMLOG(
                "ERROR",
                "ThemeDBViewModel/loadThemesByIdsSafe",
                "Exception: $e, ids=${ids.joinToString()}"
            )
            emptyList()
        }
    }
}
