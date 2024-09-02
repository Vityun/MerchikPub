package ua.com.merchik.merchik.features.main.DBViewModels

import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.RealmResults
import org.json.JSONObject
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.data.RealmModels.UsersDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm
import ua.com.merchik.merchik.database.realm.tables.TovarRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
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

    override fun updateFilters() {
        val codeDad2 = Gson().fromJson(dataJson, JSONObject::class.java).getString("codeDad2").toLong()
        val data = RealmManager.getTovarListFromReportPrepareByDad2Copy(codeDad2)
        val filterTovarDB = ItemFilter(
            "Доп. фильтр",
            TovarDB::class,
            TovarDBViewModel::class,
            ModeUI.MULTI_SELECT,
            "Товар",
            "subTitle",
            "iD",
            "iD",
            data.map { it.getiD() },
            data.map { it.nm },
            false
        )

        val clientId = Gson().fromJson(dataJson, JSONObject::class.java).getString("clientId")
        val client = CustomerRealm.getCustomerById(clientId)
        val filterCustomerSDB = ItemFilter(
            "Клиент",
            CustomerSDB::class,
            CustomerSDBViewModel::class,
            ModeUI.MULTI_SELECT,
            "title",
            "subTitle",
            "client_id",
            "id",
            mutableListOf(client.id),
            mutableListOf(client.nm),
            false
        )

        filters = Filters(
            rangeDataByKey = null,
            searchText = "",
            items = mutableListOf(
                filterTovarDB,
                filterCustomerSDB
            )
        )

    }

    override fun getItems(): List<DataItemUI> {
        return try {
            val codeDad2 = Gson().fromJson(dataJson, JSONObject::class.java).getString("codeDad2").toLong()
            val data = RealmManager.getTovarListFromReportPrepareByDad2Copy(codeDad2)
            repository.toItemUIList(TovarDB::class, data, contextUI, null)
                .map {
                    when (contextUI) {
                        ContextUI.THEME_FROM_ACHIEVEMENT -> {
                            val selected = (it.rawObj.firstOrNull { it is TovarDB } as? TovarDB)?.getiD()?.toIntOrNull() == AchievementDataHolder.instance().themeId
                            it.copy(selected = selected)
                        }
                        ContextUI.DEFAULT -> {
                            val selected = FilteringDialogDataHolder.instance()
                                .filters
                                ?.items
                                ?.firstOrNull { it.clazz == table }
                                ?.rightValuesRaw
                                ?.contains((it.rawObj.firstOrNull { it is TovarDB } as? TovarDB)?.getiD().toString())
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
                (itemsUI.first().rawObj.firstOrNull { it is TovarDB } as? TovarDB)?.let {
                    AchievementDataHolder.instance().tovarId = it.getiD().toInt()
                    AchievementDataHolder.instance().tovarName = it.nm
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
                                    (it.rawObj.firstOrNull() as? TovarDB)?.let {
                                        rightValuesRaw.add(it.getiD())
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