package ua.com.merchik.merchik.features.main.DBViewModels

import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class ThemeDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = ThemeDB::class

    override fun getItems(): List<DataItemUI> {
        return try
        {
            when (contextUI) {
                ContextUI.THEME_FROM_ACHIEVEMENT -> {
                    val dataJson: Array<String> = Gson().fromJson(dataJson, object : TypeToken<Array<String>>() {}.type)

                    val data = ThemeRealm.getThemeByIds(dataJson)

                    repository.toItemUIList(ThemeDB::class, data, contextUI, null)
                        .map {
                            val selected = (it.rawObj.firstOrNull { it is ThemeDB } as? ThemeDB)?.id?.toInt() == AchievementDataHolder.instance().themeId
                            it.copy(selected = selected)
                        }
                }
                else -> { emptyList() }
            }

        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        (itemsUI.first().rawObj.firstOrNull { it is ThemeDB } as? ThemeDB)?.let {
            AchievementDataHolder.instance().themeId = it.id.toInt()
            AchievementDataHolder.instance().themeName = it.nm
        }
    }

}