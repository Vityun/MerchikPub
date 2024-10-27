package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class TradeMarkDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = TradeMarkDB::class

    override fun updateFilters() {
        val data = when(contextUI) {
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
                TradeMarkRealm.getTradeMarkByIds(ids)
            }
            else -> { TradeMarkRealm.getAll() }
        }

        val filterTradeMarDB = ItemFilter(
            "Доп. фильтр",
            TradeMarkDB::class,
            TradeMarkDBViewModel::class,
            ModeUI.MULTI_SELECT,
            "Торгова марка",
            "subTitle",
            "iD",
            "iD",
            data.map { it.id },
            data.map { it.nm },
            true
        )


        filters = Filters(
            rangeDataByKey = null,
            searchText = "",
            items = mutableListOf(
                filterTradeMarDB,
            )
        )

    }

    override fun getItems(): List<DataItemUI> {
        return try
        {
            val data = repository.getAllRealmDataObjectUI(TradeMarkDB::class)

            repository.toItemUIList(TradeMarkDB::class, data, contextUI, null)
                .map {
                    when (contextUI) {
                        ContextUI.TRADE_MARK_FROM_ACHIEVEMENT -> {
                            val selected = (it.rawObj.firstOrNull { it is TradeMarkDB } as? TradeMarkDB)?.id?.toInt() == AchievementDataHolder.instance().manufactureId
                            it.copy(selected = selected)
                        }
                        ContextUI.DEFAULT -> {
                            val selected = FilteringDialogDataHolder.instance()
                                .filters
                                ?.items
                                ?.firstOrNull { it.clazz == table }
                                ?.rightValuesRaw
                                ?.contains((it.rawObj.firstOrNull { it is TradeMarkDB } as? TradeMarkDB)?.id.toString())
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
            ContextUI.TRADE_MARK_FROM_ACHIEVEMENT -> {
                (itemsUI.first().rawObj.firstOrNull { it is TradeMarkDB } as? TradeMarkDB)?.let {
                    AchievementDataHolder.instance().manufactureId = it.id.toInt()
                    AchievementDataHolder.instance().manufactureName = it.nm
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
                                    (it.rawObj.firstOrNull() as? TradeMarkDB)?.let {
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