package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import ua.com.merchik.merchik.Activities.DetailedReportActivity.OpinionDataHolder
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB
import ua.com.merchik.merchik.data.QuestionAnswerDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.dialogs.DialogData
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.launchFeaturesActivity
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class QuestionAnswerSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {


    override val table: KClass<out DataObjectUI>
        get() = QuestionAnswerDB::class

    override fun getDefaultHideUserFields(): List<String> {
        return ("ID, user_id").split(",")
    }

    override fun getDefaultGroupUserFields(): List<String> {
        return emptyList()
    }
    override fun getDefaultSortUserFields(): List<String>? {
        return "dt, id_quest, comment".split(",")
    }

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
            QuestionAnswerSDBViewModel::class,
            ModeUI.MULTI_SELECT,
            "Вид достижения",
            "Выберите характер достижения, которое Вы выполнили",
            "id",
            "id",
            data.map { it.id.toString() },
            data.map { it.nm },
            true
        )

//        filters = Filters(
//            searchText = "",
//            items = mutableListOf(
////                filterThemeDB
//            )
//        )
    }

    override suspend fun getItems(): List<DataItemUI> {
        Log.e("OpinionSDBViewModel","++++")
        return try
        {
            val data = RoomManager.SQL_DB.questionAnswerDao().all
            repository.toItemUIList(QuestionAnswerDB::class, data, contextUI, null)
                .map {
                    when (contextUI) {
                        ContextUI.ADD_THEME_QUESTION_ANSWER -> {
                            val selected = FilteringDialogDataHolder.instance()
                                .filters
                                ?.items
                                ?.firstOrNull { it.clazz == table }
                                ?.rightValuesRaw
                                ?.contains((it.rawObj.firstOrNull { it is QuestionAnswerDB } as? QuestionAnswerDB)?.id.toString())
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
            else -> {
                Log.e("OpinionSDBViewModel","onSelectedItemsUI -> empty")
            }
        }
    }

    override fun onClickAdditionalContent() {
        super.onClickAdditionalContent()
        launcher?.let {
            launchFeaturesActivity(
                launcher = it,
                context = context!!,
                viewModelClass = ThemeDBViewModel::class,
                dataJson = dataJson,
                modeUI = ModeUI.ONE_SELECT,
                contextUI = ContextUI.ADD_THEME_QUESTION_ANSWER,
                title = "Жалобы, Замечания, Предложени (Жилетка)",
                subTitle = "Выберите тему из списка. Благодаря анализу вашего мнения мы сможем улучшить работу нашего предприятия и тем самым увеличить ваши доходы.",
            )
        }

    }


}