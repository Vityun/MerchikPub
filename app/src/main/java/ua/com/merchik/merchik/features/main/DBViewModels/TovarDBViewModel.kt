package ua.com.merchik.merchik.features.main.DBViewModels

import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class TovarDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = TovarDB::class

    override fun getItems(): List<DataItemUI> {
//        val clientId = Gson().fromJson(dataJson, JsonObject::class.java).get("clientId").asString
        val codeDad2 = Gson().fromJson(dataJson, JsonObject::class.java).get("codeDad2").asLong
        val test = RealmManager.getTovarListFromReportPrepareByDad2Copy(codeDad2)
//        val test = TovarRealm.getByCliIds(arrayOf(clientId))
            .filter { it is DataObjectUI }
            .map { it as DataObjectUI }
        return repository.toItemUIList(TovarDB::class, test, contextUI, 18)
            .map {
                val selected = (it.rawObj.firstOrNull { it is TovarDB } as? TovarDB)?.getiD()?.toIntOrNull() == AchievementDataHolder.instance().tovarId
                it.copy(selected = selected)
            }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        (itemsUI.first().rawObj.firstOrNull { it is TovarDB } as? TovarDB)?.let {
            AchievementDataHolder.instance().tovarId = it.getiD().toInt()
            AchievementDataHolder.instance().tovarName = it.nm
        }
    }

}