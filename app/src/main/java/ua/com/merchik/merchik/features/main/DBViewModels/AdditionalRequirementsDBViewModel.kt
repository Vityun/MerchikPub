package ua.com.merchik.merchik.features.main.DBViewModels

import android.content.Context
import android.text.Html
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM
import ua.com.merchik.merchik.database.realm.tables.AddressRealm
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm
import ua.com.merchik.merchik.database.realm.tables.UsersRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.DialogARMark.DialogARMark
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class AdditionalRequirementsDBViewModel @Inject constructor(
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(repository, nameUIRepository, savedStateHandle) {

    override val contextUI: ContextUI
        get() = ContextUI.DEFAULT

    override val table: KClass<out DataObjectUI>
        get() = AdditionalRequirementsDB::class

    override fun getItems(): List<ItemUI> {
        return try {
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

            repository.toItemUIList(AdditionalRequirementsDB::class, data, contextUI).map { itemUI ->
                itemUI.rawObj.firstOrNull { it is AdditionalRequirementsDB }?.let { elementDB ->
                    elementDB as AdditionalRequirementsDB

                    val wpDataDB = Gson().fromJson(dataJson, WpDataDB::class.java)

                    val dateDocumentLong =
                        Clock.dateConvertToLong(Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000))
                    val dateFrom = Clock.getDatePeriodLong(dateDocumentLong, -15) / 1000
                    AdditionalRequirementsMarkRealm.getMark(
                        dateFrom,
                        elementDB.getId(),
                        Globals.userId.toString()
                    )?.let { itemUI.copy(rawObj = listOf(elementDB, it)) }
                        ?: itemUI.copy(rawObj = listOf(elementDB, AdditionalRequirementsMarkDB()))
                } ?: itemUI.copy()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun onClickItem(itemUI: ItemUI, context: Context) {

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


        val bStart = StringBuilder()
        val dStart = "<b>Дата начала: </b>"
        bStart.append(dStart)
        try {
//                Log.e("AdditionalRequirements", "Дата начала: " + data.getDtStart());
//                if (data.getDtStart().equals("0000-00-00")) {
//                    bStart.append("Не определена");
//                } else {
            val dStart2 = String.format("%s", data.dtStart)
            bStart.append(dStart2)
//                }
        } catch (e: Exception) {
            bStart.append("Не определена")
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
            val dEnd2 = String.format("%s", data.dtEnd)
            bEnd.append(dEnd2)
//                }
        } catch (e: Exception) {
            bEnd.append("Не определена")
        }
        val endbEnd: CharSequence = Html.fromHtml(bEnd.toString())

        val bAuthor = StringBuilder()
        val author = "<b>Автор: </b>"
        bAuthor.append(author)
        try {
            Log.e("AdditionalRequirements", "Автор: " + data.getAuthorId())
            val author2 =
                String.format("%s", UsersRealm.getUsersDBById(data.getAuthorId().toInt()).nm)
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

            val dateDocumentLong =
                Clock.dateConvertToLong(Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000))
            val dateFrom = Clock.getDatePeriodLong(dateDocumentLong, -15) / 1000
            val score = AdditionalRequirementsMarkRealm.getMark(
                dateFrom,
                elementDB.getId(),
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
}