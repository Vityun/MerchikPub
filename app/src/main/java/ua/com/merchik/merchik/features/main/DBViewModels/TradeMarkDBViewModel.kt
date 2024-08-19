package ua.com.merchik.merchik.features.main.DBViewModels

import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class TradeMarkDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = TradeMarkDB::class

    override fun getItems(): List<DataItemUI> {
        return try
        {
            when (contextUI) {
                ContextUI.TRADE_MARK_FROM_ACHIEVEMENT -> {
                    val codeDad2 = Gson().fromJson(dataJson, Long::class.java)

                    val tovarDBList = RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.getTovarListFromReportPrepareByDad2(codeDad2)
                    )

                    val ids = arrayOfNulls<String>(tovarDBList.size)
                    var j = 0
                    for (item in tovarDBList) {
                        ids[j++] = item.manufacturerId
                    }

                    val data = TradeMarkRealm.getTradeMarkByIds(ids)

                    repository.toItemUIList(TradeMarkDB::class, data, contextUI, null)
                        .map {
                            val selected = (it.rawObj.firstOrNull { it is TradeMarkDB } as? TradeMarkDB)?.id?.toInt() == AchievementDataHolder.instance().manufactureId
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
        (itemsUI.first().rawObj.firstOrNull { it is TradeMarkDB } as? TradeMarkDB)?.let {
            AchievementDataHolder.instance().manufactureId = it.id.toInt()
            AchievementDataHolder.instance().manufactureName = it.nm
        }
    }

}