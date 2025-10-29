package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import android.text.Html
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.join
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM
import ua.com.merchik.merchik.database.realm.tables.AddressRealm
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm
import ua.com.merchik.merchik.database.realm.tables.UsersRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.DialogARMark.DialogARMark
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class AdditionalRequirementsDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override fun getDefaultHideUserFields(): List<String>? {
        return when (contextUI) {
            ContextUI.ADD_REQUIREMENTS_FROM_ACHIEVEMENT,
            ContextUI.ADD_REQUIREMENTS_FROM_OPTIONS ->
                ("addr_id, author_id, disable_score, dt_change, exam_id, grp_id, hide_client, " +
                        "hide_user, ID, not_approve, options_id, showcase_tp_id, site_id, summ, theme_id," +
                        "tovar_id, user_id, option_id, client_id, color").split(",")

            else -> null
        }
    }

    override fun updateFilters() {
        when (contextUI) {
            ContextUI.ADD_REQUIREMENTS_FROM_ACHIEVEMENT -> {
                val clientId = Gson().fromJson(dataJson, String::class.java)
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
                    true
                )

                val theme = ThemeRealm.getThemeById("1253")
                val filterThemeDB = ItemFilter(
                    "Тема",
                    ThemeDB::class,
                    ThemeDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "title",
                    "subTitle",
                    "theme_id",
                    "id",
                    mutableListOf(theme.id),
                    mutableListOf(theme.nm),
                    true
                )

                filters = Filters(
                    searchText = "",
                    items = mutableListOf(
                        filterCustomerSDB,
                        filterThemeDB
                    )
                )
            }

            ContextUI.ADD_REQUIREMENTS_FROM_OPTIONS -> {
                val wpDataDB = Gson().fromJson(dataJson, WpDataDB::class.java)

                var ttCategory: Int? = null
                val addressSDB = RoomManager.SQL_DB.addressDao().getById(wpDataDB.addr_id)
                if (addressSDB != null) {
                    ttCategory = addressSDB.ttId
                }

                val data = AdditionalRequirementsRealm.getData3(
                    wpDataDB,
                    AdditionalRequirementsModENUM.HIDE_FOR_USER,
                    ttCategory,
                    null,
                    0
                )

                val filterAdditionalRequirementsDB = ItemFilter(
                    "Доп. фильтр",
                    AdditionalRequirementsDB::class,
                    AdditionalRequirementsDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "title",
                    "subTitle",
                    "id",
                    "id",
                    data.map { it.id.toString() },
                    data.map { it.notes },
                    true
                )

                filters = Filters(
                    searchText = "",
                    items = mutableListOf(
                        filterAdditionalRequirementsDB,
                    )
                )
            }

            else -> {}
        }
    }

    override val table: KClass<out DataObjectUI>
        get() = AdditionalRequirementsDB::class

    override suspend fun getItemsHeader(): List<DataItemUI> {
        return when (contextUI) {
            ContextUI.ADD_REQUIREMENTS_FROM_ACHIEVEMENT -> {
                val data = AdditionalRequirementsDB::class.java.newInstance()
                data.notes = "Це досягнення не відноситься до жодної з пропозицій замовника"
                data.id = 1
                repository.toItemUIList(
                    AdditionalRequirementsDB::class,
                    listOf(data),
                    contextUI,
                    null
                )
                    .map {
                        val selected =
                            (it.rawObj.firstOrNull { it is AdditionalRequirementsDB } as? AdditionalRequirementsDB)?.id == AchievementDataHolder.instance().requirementClientId
                        it.copy(selected = selected)
                    }
            }

            else -> {
                emptyList()
            }
        }

    }

    override suspend fun getItems(): List<DataItemUI> {
        return try {
            when (contextUI) {
                ContextUI.ADD_REQUIREMENTS_FROM_ACHIEVEMENT -> {
                    val clientId = Gson().fromJson(dataJson, String::class.java)
                    val data = AdditionalRequirementsRealm.getADByClientAll(clientId, "1253")
                    repository.toItemUIList(AdditionalRequirementsDB::class, data, contextUI, null)
                        .map {
                            val selected =
                                (it.rawObj.firstOrNull { it is AdditionalRequirementsDB } as? AdditionalRequirementsDB)?.id == AchievementDataHolder.instance().requirementClientId
                            it.copy(selected = selected)
                        }
                }

                ContextUI.ADD_REQUIREMENTS_FROM_OPTIONS -> {
                    val wpDataDB = Gson().fromJson(dataJson, WpDataDB::class.java)

                    var ttCategory: Int? = null
                    val addressSDB = RoomManager.SQL_DB.addressDao().getById(wpDataDB.addr_id)
                    if (addressSDB != null) {
                        ttCategory = addressSDB.ttId
                    }

                    val data = AdditionalRequirementsRealm.getData3(
                        wpDataDB,
                        AdditionalRequirementsModENUM.HIDE_FOR_USER,
                        ttCategory,
                        null,
                        1
                    )

                    val themeDBUI = repository.getAllRealm(ThemeDB::class, contextUI, null)
                    val customerSDBUI = repository.getAllRoom(CustomerSDB::class, contextUI, null)
                    val usersSDBBUI = repository.getAllRoom(UsersSDB::class, contextUI, null)

                    val result = repository.toItemUIList(
                        AdditionalRequirementsDB::class,
                        data,
                        contextUI,
                        null
                    ).map { itemUI ->
                        itemUI.rawObj.firstOrNull { it is AdditionalRequirementsDB }
                            ?.let { elementDB ->
                                elementDB as AdditionalRequirementsDB

                                val dateDocumentLong =
                                    wpDataDB.dt.time / 1000

                                val dateFrom = Clock.getDatePeriodLong(dateDocumentLong, -15) / 1000
                                AdditionalRequirementsMarkRealm.getMark(
                                    dateFrom,
                                    elementDB.id,
                                    Globals.userId.toString()
                                )?.let { itemUI.copy(rawObj = listOf(elementDB, it)) }
                                    ?: itemUI.copy(
                                        rawObj = listOf(
                                            elementDB,
                                            AdditionalRequirementsMarkDB()
                                        )
                                    )
                            } ?: itemUI.copy()
                    }
                        .join(themeDBUI, "theme_id = id: nm")
                        .join(customerSDBUI, "client_id = client_id: nm")
                        .join(usersSDBBUI, "author_id = user_id: fio")

                    result
                }

                else -> {
                    emptyList()
                }
            }

        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        when (contextUI) {
            ContextUI.ADD_REQUIREMENTS_FROM_OPTIONS -> {
                val data = itemUI.rawObj.firstOrNull { it is AdditionalRequirementsDB }?.let {
                    it as AdditionalRequirementsDB
                } ?: return

                val dialog = DialogARMark(context)

                dialog.setTitle("Доп. Требование (" + data.getId() + "/" + data.getSiteId() + ")")

                val bId = StringBuilder()
                val id = "<b>Идентификатор: </b>"
                bId.append(id)
                try {
                    val id2 = String.format("%s", data.getId())
                    bId.append(id2)
                } catch (e: Exception) {
                }
                val endbId: CharSequence = Html.fromHtml(bId.toString())


                val bAddr = StringBuilder()
                val addr = "<b>Адрес: </b>"
                bAddr.append(addr)
                try {
                    val addr2 = String.format("%s", AddressRealm.getAddressById(data.getId()).nm)
                    bAddr.append(addr2)
                } catch (e: Exception) {
                    bAddr.append("Для всех адресов")
                }
                val endbAddr: CharSequence = Html.fromHtml(bAddr.toString())


                val bGrp = StringBuilder()
                val grp = "<b>Сеть: </b>"
                bGrp.append(grp)
                try {
                    Log.e("AdditionalRequirements", "Сеть: " + data.getGrpId())
                    if (data.getGrpId() == "0") {
                        bGrp.append("Для всех сетей")
                    } else {
                        val grp2 = String.format("%s", data.getGrpId())
                        bGrp.append(grp2)
                    }
                } catch (e: Exception) {
                    bGrp.append("Для всех сетей")
                }
                val endbGrp: CharSequence = Html.fromHtml(bGrp.toString())


                val bNum = StringBuilder()
                val number = "<b>Номер: </b>"
                bNum.append(number)
                try {
                    val number2 = String.format("%s", data.getSiteId())
                    bNum.append(number2)
                } catch (e: Exception) {
                }
                val endbNum: CharSequence = Html.fromHtml(bNum.toString())

                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

                val bStart = StringBuilder()
                val dStart = "<b>Дата начала: </b>"
                bStart.append(dStart)
                try {
//                Log.e("AdditionalRequirements", "Дата начала: " + data.getDtStart());
//                if (data.getDtStart().equals("0000-00-00")) {
//                    bStart.append("Не определена");
//                } else {
                    if (data.dtStart == null)
                        bStart.append("Не визначена");
                    else {
                        val formattedDate = sdf.format(data.dtStart)
                        val dStart2 = String.format("%s", formattedDate)
                        bStart.append(dStart2)
                    }
                } catch (e: Exception) {
                    bStart.append("Не визначена")
                }
                val endbStart: CharSequence = Html.fromHtml(bStart.toString())


                val bEnd = StringBuilder()
                val dEnd = "<b>Дата окончания: </b>"
                bEnd.append(dEnd)
                try {
//                Log.e("AdditionalRequirements", "Дата окончания: " + data.getDtEnd());
//                if (data.getDtEnd().equals("0000-00-00")) {
//                    bEnd.append("Не определена");
//                } else {
//                    val dEnd2 = String.format("%s", data.dtEnd)
//                    bEnd.append(dEnd2)
//                }
                    if (data.dtEnd == null)
                        bEnd.append("Не визначена");
                    else {
                        val formattedDate = sdf.format(data.dtEnd)
                        val dStart2 = String.format("%s", formattedDate)
                        bEnd.append(dStart2)
                    }
                } catch (e: Exception) {
                    bEnd.append("Не визначена")
                }
                val endbEnd: CharSequence = Html.fromHtml(bEnd.toString())

                val bAuthor = StringBuilder()
                val author = "<b>Автор: </b>"
                bAuthor.append(author)
                try {
                    Log.e("AdditionalRequirements", "Автор: " + data.authorId)
                    val author2 =
                        String.format(
                            "%s",
                            UsersRealm.getUsersDBById(data.authorId.toInt()).nm
                        )
                    bAuthor.append(author2)
                } catch (e: Exception) {
                    bAuthor.append("Автор не определён")
                }
                val endbAuthor: CharSequence = Html.fromHtml(bAuthor.toString())


                val bCust = StringBuilder()
                val customer = "<b>Клиент: </b>"
                bCust.append(customer)
                try {
                    val customer2 =
                        String.format("%s", CustomerRealm.getCustomerById(data.getClientId()).nm)
                    bCust.append(customer2)
                } catch (e: Exception) {
                }
                val endbCust: CharSequence = Html.fromHtml(bCust.toString())


                val bMark = StringBuilder()
                val mark = "<b>Оценка: </b>"
                bMark.append(mark)
                try {
                    val mark2 = String.format("%s", "")
                    bMark.append(mark2)
                } catch (e: Exception) {
                }
                val endbMark: CharSequence = Html.fromHtml(bMark.toString())


                val bText = StringBuilder()
                val text = "<b>Текст: </b>"
                bText.append(text)
                try {
                    val text2 = String.format("%s", data.getNotes())
                    bText.append(text2)
                } catch (e: Exception) {
                }


                /*Добавляю Товары*/
//            try {
//                bText.append("\n").append("<b>Товар: </b>");
//                bText.append(createTovarText(tovarDB, tradeMarkDB));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
                val endbText: CharSequence = Html.fromHtml(bText.toString())


                dialog.setData(
                    endbId,
                    endbAddr,
                    endbGrp,
                    endbNum,
                    endbStart,
                    endbEnd,
                    endbAuthor,
                    endbCust,
                    endbMark,
                    endbText
                )


                itemUI.rawObj.firstOrNull { it is AdditionalRequirementsDB }?.let { elementDB ->
                    elementDB as AdditionalRequirementsDB

                    val wpDataDB = Gson().fromJson(dataJson, WpDataDB::class.java)

                    val dateDocumentLong = wpDataDB.dt.time

                    val dateFrom = Clock.getDatePeriodLong(dateDocumentLong, -15) / 1000
                    val score = AdditionalRequirementsMarkRealm.getMark(
                        dateFrom,
                        elementDB.id,
                        Globals.userId.toString()
                    )?.score ?: 0

                    dialog.setRatingBarAR(data, score.toString().toFloat()) {
                        updateContent()
                    }
                }


                dialog.setClose { dialog.dismiss() }
                dialog.setLesson(context, true, 1234)
                dialog.setVideoLesson(context, true, 1235) {}

                dialog.show()
            }

            else -> {}
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        (itemsUI.first().rawObj.firstOrNull { it is AdditionalRequirementsDB } as? AdditionalRequirementsDB)?.let {
            AchievementDataHolder.instance().requirementClientId = it.id
            AchievementDataHolder.instance().requirementClientName = it.notes
        }
    }
}