package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.Activities.DetailedReportActivity.OpinionDataHolder
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class OpinionSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {


    override val table: KClass<out DataObjectUI>
        get() = OpinionSDB::class

//    override fun getDefaultHideUserFields(): List<String>? {
//        Log.e("OpinionSDBViewModel","getDefaultHideUserFields +")
//        return when(contextUI) {
//            ContextUI.ADD_OPINION_FROM_DETAILED_REPORT -> {
////                "dt_change".split(",")
//                val list = mutableListOf("dt_change", "grp_id")
//                Log.e("OpinionSDBViewModel","getDefaultHideUserFields: $list")
//                list
//            }
//            else -> null
//        }
//    }

    override fun updateFilters() {
        Log.e("OpinionSDBViewModel","++++")
        val data = when(contextUI) {
            ContextUI.ADD_OPINION_FROM_DETAILED_REPORT -> {
                val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
                val themeID = dataJsonObject.get("themeID").asInt

//                val themeID: Int = Gson().fromJson(dataJson, Int::class.java)
                val listOpinionTheme = RoomManager.SQL_DB.opinionThemeDao().getByTheme(themeID)
                val mnenieIds = listOpinionTheme.map { it.mnenieId }.toMutableList()
                RoomManager.SQL_DB.opinionDao().getOpinionByIds(mnenieIds)
            }
            else -> {
                Log.e("OpinionSDBViewModel","updateFilters ----")
                emptyList() }
        }

        val filterThemeDB = ItemFilter(
            "Доп. фильтр",
            OpinionSDB::class,
            OpinionSDBViewModel::class,
            ModeUI.MULTI_SELECT,
            "Вид достижения",
            "Выберите характер достижения, которое Вы выполнили",
            "id",
            "id",
            data.map { it.id.toString() },
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
        Log.e("OpinionSDBViewModel","++++")
        return try
        {
            val data = RoomManager.SQL_DB.opinionDao().all
            repository.toItemUIList(OpinionSDB::class, data, contextUI, null)
                .map {
                    when (contextUI) {
                        ContextUI.ADD_OPINION_FROM_DETAILED_REPORT -> {
                            val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
                            val opinionID = dataJsonObject.get("opinionID").asInt

                            val selected = (it.rawObj.firstOrNull { it is OpinionSDB } as? OpinionSDB)?.id == opinionID
                            it.copy(selected = selected)
                        }
                        ContextUI.DEFAULT -> {
                            val selected = FilteringDialogDataHolder.instance()
                                .filters
                                ?.items
                                ?.firstOrNull { it.clazz == table }
                                ?.rightValuesRaw
                                ?.contains((it.rawObj.firstOrNull { it is OpinionSDB } as? OpinionSDB)?.id.toString())
                            it.copy(selected = selected == true)
                        }
                        else -> { it }
                    }

                }
        } catch (e: Exception) {
            Log.e("OpinionSDBViewModel","getItems -> Exception: ${e.message}")
            emptyList()
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        Log.e("ADD_OPINION_FROM_DETAIL", "onSelectedItemsUI")
        when (contextUI) {
            ContextUI.ADD_OPINION_FROM_DETAILED_REPORT -> {
                Log.e("ADD_OPINION_FROM_DETAIL", "onSelectedItemsUI -> ADD_OPINION_FROM_DETAILED_REPORT")

                (itemsUI.first().rawObj.firstOrNull { it is OpinionSDB } as? OpinionSDB)?.let {
                    Log.e("ADD_OPINION_FROM_DETAIL", "onSelectedItemsUI -> itemsUI.first().rawObj.firstOrNull { it is ThemeDB } as? ThemeDB)?.let")
                    OpinionDataHolder.instance().opinionID = it.id
                    OpinionDataHolder.instance().opinionName = it.nm
                    Log.e("ADD_OPINION_FROM_DETAIL", "opinionName: ${OpinionDataHolder.instance().opinionID}")
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
            else -> {
                Log.e("OpinionSDBViewModel","onSelectedItemsUI -> empty")
            }
        }
    }

}