package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import android.text.Html
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONObject
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.common.VizitShowcase
import ua.com.merchik.merchik.dataLayer.common.VizitShowcaseDataHolder
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.AddressRealm
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm
import ua.com.merchik.merchik.database.realm.tables.ImagesTypeListRealm
import ua.com.merchik.merchik.database.realm.tables.UsersRealm
import ua.com.merchik.merchik.database.room.RoomManager.SQL_DB
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.DialogARMark.DialogARMark
import ua.com.merchik.merchik.dialogs.DialogFullPhoto
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.launchFeaturesActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class PlanogrammVizitShowcaseViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

//    val code_dad2 = 1110425031987057679
//    val wpDataDBId = 3796790965

    private val code_dad2 = mutableStateOf<Long>(0)

    override val table: KClass<out DataObjectUI>
        get() = PlanogrammVizitShowcaseSDB::class

    override fun onClickFullImage(stackPhotoDB: StackPhotoDB, comment: String?) {
        val dialogFullPhoto = DialogFullPhotoR(context)
        dialogFullPhoto.setPhoto(stackPhotoDB)
        comment?.let { dialogFullPhoto.setComment(it) }
        dialogFullPhoto.hideCamera()
        dialogFullPhoto.setClose { dialogFullPhoto.dismiss() }
        dialogFullPhoto.show()
    }


    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        val data =
            itemUI.rawObj.firstOrNull { it is PlanogrammVizitShowcaseSDB }?.let {
                it as PlanogrammVizitShowcaseSDB
            } ?: return

        val votes = SQL_DB.votesDao().getVote(
            code_dad2.value,
            data.planogram_photo_id.toLong(),
            5
        )

        val dialog = DialogARMark(context)

        dialog.setTitle("Оцінка планограми (" + data.planogram_id + ")")

        val bId = StringBuilder()
        val id = "<b>Ідентифікатор: </b>"
        bId.append(id)
        try {
            val id2 = String.format("%s", data.planogram_photo_id)
            bId.append(id2)
        } catch (e: Exception) {
            Log.e("!ExceptionQ", "> ${e.message}")
        }
        val endbId: CharSequence = Html.fromHtml(bId.toString())


        val bAddr = StringBuilder()
        val addr = "<b>Адреса: </b>"
        bAddr.append(addr)
        try {
            val addr2 = String.format("%s", AddressRealm.getAddressById(data.addr_id).nm)
            bAddr.append(addr2)
        } catch (e: Exception) {
            bAddr.append("Для всіх адрес")
        }
        val endbAddr: CharSequence = Html.fromHtml(bAddr.toString())


//                val bGrp = StringBuilder()
//                val grp = "<b>Мережа: </b>"
//                bGrp.append(grp)
//                try {
//                    Log.e("AdditionalRequirements", "Сеть: " + data.)
//                    if (data.getGrpId() == "0") {
//                        bGrp.append("Для всех сетей")
//                    } else {
//                        val grp2 = String.format("%s", data.getGrpId())
//                        bGrp.append(grp2)
//                    }
//                } catch (e: Exception) {
//                    bGrp.append("Для всех сетей")
//                }
//                val endbGrp: CharSequence = Html.fromHtml(bGrp.toString())


        val bNum = StringBuilder()
        val number = "<b>Номер: </b>"
        bNum.append(number)
        try {
            val number2 = String.format("%s", data.planogram_photo_id)
            bNum.append(number2)
        } catch (e: Exception) {
            Log.e("!ExceptionQ", "> ${e.message}")
        }
        val endbNum: CharSequence = Html.fromHtml(bNum.toString())

        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        val bStart = StringBuilder()
        val dStart = "<b>Дата створення: </b>"
        bStart.append(dStart)
        try {
//                Log.e("AdditionalRequirements", "Дата начала: " + data.getDtStart());
//                if (data.getDtStart().equals("0000-00-00")) {
//                    bStart.append("Не определена");
//                } else {
            if (data.dt == null)
                bStart.append("Не визначена");
            else {
                val formattedDate = sdf.format(data.dt)
                val dStart2 = String.format("%s", formattedDate)
                bStart.append(dStart2)
            }
        } catch (e: Exception) {
            bStart.append("Не визначена")
        }
        val endbStart: CharSequence = Html.fromHtml(bStart.toString())


        val bEnd = StringBuilder()
        val dEnd = "<b>Дата зміни: </b>"
        bEnd.append(dEnd)
        try {
//                Log.e("AdditionalRequirements", "Дата окончания: " + data.getDtEnd());
//                if (data.getDtEnd().equals("0000-00-00")) {
//                    bEnd.append("Не определена");
//                } else {
//                    val dEnd2 = String.format("%s", data.dtEnd)
//                    bEnd.append(dEnd2)
//                }
            if (votes.dt == null || votes.dt == 0L)
                bEnd.append("Не визначена");
            else {
                val formattedDate = sdf.format(votes.dtUpload)
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
            val author2 =
                String.format(
                    "%s",
                    UsersRealm.getUsersDBById(votes.voterId.toInt()).nm
                )
            bAuthor.append(author2)
        } catch (e: Exception) {
            bAuthor.append("Автор не визначений")
        }
        val endbAuthor: CharSequence = Html.fromHtml(bAuthor.toString())


        val bCust = StringBuilder()
        val customer = "<b>Клієнт: </b>"
        bCust.append(customer)
        try {
            val customer2 =
                String.format("%s", CustomerRealm.getCustomerById(data.client_id).nm)
            bCust.append(customer2)
        } catch (e: Exception) {
        }
        val endbCust: CharSequence = Html.fromHtml(bCust.toString())


        val bMark = StringBuilder()
        val mark = "<b>Оцінка: </b>"
        bMark.append(mark)
        try {
            val mark2 = String.format("%s", "")
            bMark.append(mark2)
        } catch (e: Exception) {
        }
        val endbMark: CharSequence = Html.fromHtml(bMark.toString())


        val bText = StringBuilder()
        val text = "<b>Коментар: </b>"
        bText.append(text)
        try {
            val text2: String = if (votes.comments.isNullOrEmpty())
                String.format("%s", data.comments)
            else
                String.format("%s", votes.comments)
            bText.append(text2)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val endbText: CharSequence = Html.fromHtml(bText.toString())


        dialog.setData(
            endbId,
            endbAddr,
//                    endbGrp,
            "",
            endbNum,
            endbStart,
            endbEnd,
            endbAuthor,
            endbCust,
            endbMark,
            endbText
        )

        itemUI.rawObj.firstOrNull { it is PlanogrammVizitShowcaseSDB }
            ?.let { elementDB ->
                elementDB as PlanogrammVizitShowcaseSDB

                val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                    RealmManager.getWorkPlanRowByCodeDad2(code_dad2.value)
                )


                val score = votes?.score ?: 0
                val text = votes?.comments

                dialog.setRatingBarPlanogrammVizitShowcase(
                    data,
                    wpDataDB,
                    score.toString().toFloat(),
                    text
                ) {
                    updateContent()
                }
            }

        dialog.setClose {
            dialog.dismiss()
            updateContent()
        }
        dialog.setLesson(context, true, 1234)
        dialog.setVideoLesson(context, true, 1235) {}

        dialog.show()
    }

    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context, index: Int) {
        when (index) {
            0 -> {
                dialog = DialogFullPhoto(context)
                val photoLogData = mutableListOf<StackPhotoDB>()
                var selectedIndex = -1
                val fieldsForCommentsImage = getFieldsForCommentsImage()
                val fieldsForCustomResult = getFieldsForCustomResult()
                val photoDBWithComments = HashMap<StackPhotoDB, String>()
                val photoDBWithRawObj = HashMap<StackPhotoDB, Any>()

                uiState.value.items.forEach { dataItemUI ->
                    val obj = dataItemUI.rawObj.firstOrNull() ?: return@forEach
                    val jsonObject = JSONObject(Gson().toJson(obj))

                    var comments = ""
                    fieldsForCommentsImage?.forEach { k ->
                        comments += "${jsonObject.opt(k)} \n\n"
                    }

                    if (clickedDataItemUI == dataItemUI) {
                        fieldsForCustomResult?.forEach { k ->
                            valueForCustomResult.value[k] = jsonObject.opt(k)
                        }
                    }

                    val imageFields = obj.getFieldsImageOnUI()
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    fun optStr(key: String): String =
                        jsonObject.opt(key)?.toString()?.trim().orEmpty()

                    // ✅ Берём ключ по index как было
                    val idKey = imageFields.getOrNull(index)
                    val photoId = idKey?.let { optStr(it) }.orEmpty()

                    var photo: StackPhotoDB? = null

                    // 1) По photoServerId (или любому id-полю)
                    if (photoId.isNotEmpty() && photoId != "0" && photoId != "null") {
                        photo = RealmManager.getPhotoById(null, photoId)
                    }

                    // 2) Fallback по hash, если id ещё нет или фото не найдено
                    if (photo == null) {
                        // если у некоторых моделей hash хранится в другом поле — добавь сюда альтернативы
                        val hash = optStr("photo_hash").ifEmpty { optStr("photo_do_hash") }
                        if (hash.length > 12 && hash != "0" && hash != "null") {
                            photo = RealmManager.getPhotoByHash(hash)
                        }
                    }

                    photo?.let { p ->
                        photoDBWithComments[p] = comments
                        photoDBWithRawObj[p] = obj
                        photoLogData.add(p)

                        if (clickedDataItemUI == dataItemUI) {
                            selectedIndex = photoLogData.size - 1
                        }
                    }
                }

                if (selectedIndex > -1) {
                    dialog?.setPhotos(
                        selectedIndex,
                        photoLogData,
                        { _, photoDB ->
                            onClickFullImage(photoDB, photoDBWithComments[photoDB])
                            dialog?.dismiss()
                            dialog = null
                        },
                        { updateContent() }
                    )

                    val id =
                        (clickedDataItemUI.rawObj.firstOrNull { it is PlanogrammVizitShowcaseSDB } as? PlanogrammVizitShowcaseSDB)
                            ?.planogram_photo_id

                    id?.let {
                        val vote = SQL_DB.votesDao().getVote(code_dad2.value, id.toLong(), 5)
                        dialog?.ratingType = DialogFullPhoto.RatingType.PLANOGRAM
                        dialog?.setVote(vote)
                        val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                            RealmManager.getWorkPlanRowByCodeDad2(code_dad2.value)
                        )
                        dialog?.setWpDataDB(wpDataDB)
                    }

                    dialog?.setClose {
                        dialog?.dismiss()
                        dialog = null
                        updateContent()
                    }
                    dialog?.show()
                }
            }

            1 -> {
                val id =
                    (clickedDataItemUI.rawObj.firstOrNull { it is PlanogrammVizitShowcaseSDB } as? PlanogrammVizitShowcaseSDB)?.id
                id?.let {
//                    VizitShowcaseDataHolder.instance().init()

                    val dataJson = JsonObject()
                    dataJson.addProperty("wpDataDBId", code_dad2.value.toString())
                    dataJson.addProperty("planogrammVizitShowcaseId", id)
//                    VizitShowcaseDataHolder.instance().planogrammVizitMap[id] = ""

                    launcher?.let {
                        launchFeaturesActivity(
                            launcher = it,
                            context = context,
                            viewModelClass = ShowcaseDBViewModel::class,
//                            dataJson = Gson().toJson(code_dad2),
                            dataJson = Gson().toJson(dataJson),
                            modeUI = ModeUI.ONE_SELECT,
                            contextUI = ContextUI.SHOWCASE,
                            title = "Зразок вітрини",
                            subTitle = "Виберіть вітрину згідно планограми"
                        )
                    }
                }

            }

            2 -> {
                val id =
                    (clickedDataItemUI.rawObj.firstOrNull { it is PlanogrammVizitShowcaseSDB } as? PlanogrammVizitShowcaseSDB)?.id
                id?.let {
                    val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
                    val optionDBID = dataJsonObject.get("optionDBId").asString
                    val dataJson = JsonObject()
                    dataJson.addProperty("wpDataDBId", code_dad2.value.toString())
                    dataJson.addProperty("planogrammVizitShowcaseId", id)
                    dataJson.addProperty("optionDBId", optionDBID)

                    launcher?.let {
                        launchFeaturesActivity(
                            launcher = it,
                            context = context,
                            viewModelClass = StackPhotoDBViewModel::class,
//                            dataJson = Gson().toJson(code_dad2),
                            dataJson = Gson().toJson(dataJson),
                            modeUI = ModeUI.ONE_SELECT,
                            contextUI = ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT,
                            title = "Перелік фото звітів",
                            subTitle = "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(14).nm
                        )
                    }
                }
            }

            3 -> {


                val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                    RealmManager.getWorkPlanRowByCodeDad2(code_dad2.value)
                )
                launcher?.let {
                    launchFeaturesActivity(
                        launcher = it,
                        context = context,
                        viewModelClass = AdditionalRequirementsDBViewModel::class,
                        dataJson = Gson().toJson(wpDataDB),
                        contextUI = ContextUI.ADD_REQUIREMENTS_FROM_OPTIONS,
                        title = "Доп. требования",
                        subTitle = "Список дополнительных требований, которые должен выполнить исполнитель во время посещения"
                    )
                }

            }

            else -> {

            }

        }
    }

    override fun updateFilters() {
        when (contextUI) {
            ContextUI.PLANOGRAMM_VIZIT_SHOWCASE,
            -> {

                try {

                    val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)

                    val codeDad2 = dataJsonObject.get("wpDataDBId").asString.toLong()
                    val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                    )


                    val filterWpDataDB = ItemFilter(
                        "Адреса",
                        AddressSDB::class,
                        AddressSDBViewModel::class,
                        ModeUI.MULTI_SELECT,
                        "title",
                        "subTitle",
                        "addr_id",
                        "addr_id",
                        mutableListOf(wpDataDB.addr_id.toString()),
                        mutableListOf(wpDataDB.addr_txt),
                        false
                    )


                    val filterImagesTypeListDB = ItemFilter(
                        "Клиент",
                        CustomerSDB::class,
                        CustomerSDBViewModel::class,
                        ModeUI.MULTI_SELECT,
                        "title",
                        "subTitle",
                        "client_id",
                        "client_id",
                        mutableListOf(wpDataDB.client_id),
                        mutableListOf(wpDataDB.client_txt),
                        true
                    )

                    filters = Filters(
                        rangeDataByKey = null,
                        searchText = "",
                        items = mutableListOf(
                            filterWpDataDB,
                            filterImagesTypeListDB
                        )
                    )

                } catch (e: Exception) {
                    Log.e("!!!!!", "err: ${e.message}")
                    Globals.writeToMLOG("ERROR", "PlanogrammVizitShowcaseViewModel.updateFilters","error: ${e.message}")
                }
            }

            else -> {}
        }

    }

    override suspend fun getItems(): List<DataItemUI> {
        val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)

//        val clientId = dataJsonObject.get("clientId").asString
//        val addressId = dataJsonObject.get("addressId").asInt
        code_dad2.value = dataJsonObject.get("wpDataDBId").asString.toLong()

        val colorUiContainer = dataJsonObject?.get("colorUiContainer")?.asInt ?: 0

//        val ttId = dataJsonObject.get("ttId").asString
        val base = SQL_DB.planogrammVizitShowcaseDao()
            .getByCodeDad2(code_dad2.value)
//            .getByClientIdAdressId(clientId, addressId)
//            .getByClientIdAdressIdUnique(clientId, addressId)
//            .getByClientIdAddressIdAndDad2(clientId, addressId, null)


        val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
            RealmManager.getWorkPlanRowByCodeDad2(code_dad2.value)
        )

        val dataHolder = VizitShowcaseDataHolder.getInstance()
        var showcaseList = dataHolder.getVizitShowcases()

        // Если список пустой — инициализируем
        if (showcaseList.isEmpty()) {
            base.forEach { item ->
                val newShowcase = VizitShowcase(
                    id = item.id,
                    showcaseId = item.showcase_id ?: 0,
                    showcasePhotoId = item.showcase_photo_id ?: 0,
                    photoDoId = item.photo_do_id ?: 0,
                    photoDoHash = item.photo_do_hash ?: ""
                )
                dataHolder.addVizitShowcase(newShowcase)
            }
            showcaseList = dataHolder.getVizitShowcases() // обновляем список после добавления
        }

        val newRecords = mutableListOf<PlanogrammVizitShowcaseSDB>()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val currentTimeFormatted = dateFormat.format(Date())

        base.forEach { item ->
            val modified = item.copy() // Создаем копию для изменений

            if (item.planogram_id == colorUiContainer)
                item.color = "FFC4C4"

            val score = SQL_DB.votesDao().getVote(
                code_dad2.value,
                item.planogram_photo_id.toLong(),
                5
            )?.score ?: 0
            item.score = score.toString()

            val showcaseData = showcaseList.find { it.id == item.id }

            showcaseData?.let {
                item.showcase_id = it.showcaseId
                item.showcase_photo_id = it.showcasePhotoId
                item.photo_do_id = it.photoDoId
                item.photo_do_hash = it.photoDoHash
            }
            // Если были изменения, добавляем новую запись
            if (!item.equals(modified)) {
                modified.showcase_id = item.showcase_id
                modified.showcase_photo_id = item.showcase_photo_id
                modified.photo_do_id = item.photo_do_id
                modified.photo_do_hash = item.photo_do_hash
                modified.uploadStatus = 1
                modified.dt_update = currentTimeFormatted
                modified.author_id = wpDataDB.user_id
                newRecords.add(modified)
            }
        }

        if (newRecords.isNotEmpty()) {
            SQL_DB.planogrammVizitShowcaseDao().insertAll(newRecords)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { Log.d("DB_INSERT", "++++") },
                    { error -> Globals.writeToMLOG("ERROR", "PlanogrammVizitShowcaseViewModel.getItems","Ошибка при сохранении: ${error.message}") }
                )
        }

        Globals.writeToMLOG("INFO", "PlanogrammVizitShowcaseViewModel.getItems","Result base size: ${base.size}")

        return repository.toItemUIList(PlanogrammVizitShowcaseSDB::class, base, contextUI, 5)
            .map {
                val selected = FilteringDialogDataHolder.instance()
                    .filters
                    ?.items
                    ?.firstOrNull { it.clazz == table }
                    ?.rightValuesRaw
                    ?.contains((it.rawObj.firstOrNull { it is PlanogrammVizitShowcaseSDB } as? PlanogrammVizitShowcaseSDB)?.planogram_id.toString())
                it.copy(selected = selected == true)
            }
    }
//        val data = repository.getAllRoom(table, contextUI, 5)
//            .map {
//                val selected = FilteringDialogDataHolder.instance()
//                    .filters
//                    ?.items
//                    ?.firstOrNull { it.clazz == table }
//                    ?.rightValuesRaw
//                    ?.contains((it.rawObj.firstOrNull { it is PlanogrammVizitShowcaseSDB } as? PlanogrammVizitShowcaseSDB)?.id.toString())
//                it.copy(selected = selected == true)
//            }
//        return data
//        return try
//        {
//            repository.toItemUIList(TradeMarkDB::class, data, contextUI, null)
//                .map {
//                    when (contextUI) {
//                        ContextUI.TRADE_MARK_FROM_ACHIEVEMENT -> {
//                            val selected = (it.rawObj.firstOrNull { it is TradeMarkDB } as? TradeMarkDB)?.id?.toInt() == AchievementDataHolder.instance().manufactureId
//                            it.copy(selected = selected)
//                        }
//                        ContextUI.DEFAULT -> {
//                            val selected = FilteringDialogDataHolder.instance()
//                                .filters
//                                ?.items
//                                ?.firstOrNull { it.clazz == table }
//                                ?.rightValuesRaw
//                                ?.contains((it.rawObj.firstOrNull { it is TradeMarkDB } as? TradeMarkDB)?.id.toString())
//                            it.copy(selected = selected == true)
//                        }
//                        else -> { it }
//                    }
//                }
//        } catch (e: Exception) {
//            emptyList()
//        }
//}


}