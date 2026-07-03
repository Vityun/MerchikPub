package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.Activities.DetailedReportActivity.OpinionDataHolder
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.Options.Controls.OptionControlQuestionAnswer
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB
import ua.com.merchik.merchik.data.QuestionAnswerDB
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.launchFeaturesActivity
import java.util.Arrays
import java.util.Calendar
import javax.inject.Inject
import kotlin.reflect.KClass


@HiltViewModel
class OptionsDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {


    override val table: KClass<out DataObjectUI>
        get() = OptionsDB::class

    override fun getDefaultHideUserFields(): List<String> {
        return ("ID, adr_id, kli_id").split(",")
    }

    override fun getDefaultGroupUserFields(): List<String> {
        return emptyList()
    }

    override fun getDefaultSortUserFields(): List<String>? {
        return "dt, id_quest, comment".split(",")
    }

//    override fun updateFilters() {
//        Log.e("OpinionSDBViewModel", "++++")
//        val data = when (contextUI) {
//
//            ContextUI.QUESTION_ANSWER_INFO -> {
//                val themeList = arrayOf("6", "600", "607", "610", "612", "412")
//                ThemeRealm.getThemeByIds(themeList)
//            }
//
//            else -> {
//                Log.e("OpinionSDBViewModel", "updateFilters ----")
//                emptyList()
//            }
//        }
//
//        val filterThemeDB = ItemFilter(
//            "Тема",
//            ThemeDB::class,
//            ThemeDBViewModel::class,
//            ModeUI.MULTI_SELECT,
//            "Тема",
//            "Выберите характер достижения, которое Вы выполнили",
//            "id_quest",
//            "question",
//            data.map { it.id.toString() },
//            data.map { it.nm },
//            false
//        )
//
//        filters = Filters(
//            searchText = "",
//            items = mutableListOf(
//                filterThemeDB
//            )
//        )
//    }

    override suspend fun getItems(): List<DataItemUI> {
        Log.e("OpinionSDBViewModel", "++++")

        return try {
            val codeDad2 = Gson().fromJson(dataJson, Long::class.java)
            Log.e("OpinionSDBViewModel", "codeDad2: $codeDad2")

            val data = RealmManager.getOptionsByDad2(codeDad2)
                .onEach { option ->
                    if (option.isSignal != "0")
                        option.timeColor = "FFC4C4"

                }
                ?: return emptyList()

//                    data


            repository.toItemUIList(
                OptionsDB::class,
                data,
                contextUI,
                null
            ).map { item ->
                when (contextUI) {
                    ContextUI.ADD_THEME_QUESTION_ANSWER -> {
                        val selected = FilteringDialogDataHolder.instance()
                            .filters
                            ?.items
                            ?.firstOrNull { it.clazz == table }
                            ?.rightValuesRaw
                            ?.contains(
                                (item.rawObj.firstOrNull { it is QuestionAnswerDB }
                                        as? QuestionAnswerDB)
                                    ?.id
                                    .toString()
                            )

                        item.copy(selected = selected == true)
                    }

                    else -> item
                }
            }
        } catch (e: Exception) {
            Log.e(
                "OpinionSDBViewModel",
                "getItems -> Exception: ${e.message}",
                e
            )
            emptyList()
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        Log.e("ADD_OPINION_FROM_DETAIL", "onSelectedItemsUI")
        when (contextUI) {
            ContextUI.ADD_OPINION_FROM_DETAILED_REPORT -> {
                Log.e(
                    "ADD_OPINION_FROM_DETAIL",
                    "onSelectedItemsUI -> ADD_OPINION_FROM_DETAILED_REPORT"
                )

                (itemsUI.first().rawObj.firstOrNull { it is OpinionSDB } as? OpinionSDB)?.let {
                    Log.e(
                        "ADD_OPINION_FROM_DETAIL",
                        "onSelectedItemsUI -> itemsUI.first().rawObj.firstOrNull { it is ThemeDB } as? ThemeDB)?.let"
                    )
                    OpinionDataHolder.instance().opinionID = it.id
                    OpinionDataHolder.instance().opinionName = it.nm
                    Log.e(
                        "ADD_OPINION_FROM_DETAIL",
                        "opinionName: ${OpinionDataHolder.instance().opinionID}"
                    )
                }
            }

            ContextUI.DEFAULT -> {
                FilteringDialogDataHolder.instance().filters.apply {
                    this?.let { filters ->
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
                Log.e("OpinionSDBViewModel", "onSelectedItemsUI -> empty")
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


    private fun isToday(seconds: Long?): Boolean {
        if (seconds == null || seconds <= 0L) return false

        val today = Calendar.getInstance()

        val created = Calendar.getInstance().apply {
            timeInMillis = seconds * 1000L
        }

        return today.get(Calendar.YEAR) == created.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == created.get(Calendar.DAY_OF_YEAR)
    }
}