package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.common.VizitShowcaseDataHolder
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class ShowcaseDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    private val planogrammId = mutableStateOf(0)

    override val table: KClass<out DataObjectUI>
        get() = StackPhotoDB::class

    override fun onClickFullImage(stackPhotoDB: StackPhotoDB, comment: String?) {
        val dialogFullPhoto = DialogFullPhotoR(context)
        dialogFullPhoto.setPhoto(stackPhotoDB)
        comment?.let { dialogFullPhoto.setComment(it) }
        dialogFullPhoto.hideCamera()
        dialogFullPhoto.setClose { dialogFullPhoto.dismiss() }
        dialogFullPhoto.show()
    }

    override fun updateFilters() {
//        when (contextUI) {
//            ContextUI.SHOWCASE,
//            -> {

//                val codeDad2 = Gson().fromJson(dataJson, Long::class.java)
//                val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
//                    RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
//                )

        try {

            val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)

            val codeDad2 = dataJsonObject.get("wpDataDBId").asString.toLong()

            planogrammId.value = if (dataJsonObject.has("planogrammVizitShowcaseId"))
                dataJsonObject["planogrammVizitShowcaseId"].asInt
            else 0

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
        }
//            }

//            else -> {}
//        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getItems(): List<DataItemUI> {
        return try {
            when (contextUI) {
                ContextUI.SHOWCASE
                    -> {
//                    val codeDad2 = Gson().fromJson(dataJson, Long::class.java)
                    val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
                    val codeDad2 = dataJsonObject.get("wpDataDBId").asString.toLong()
                    val id = if (dataJsonObject.has("planogrammVizitShowcaseId"))
                        dataJsonObject["planogrammVizitShowcaseId"].asInt
                    else 0

                    val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                    )
                    val list = mutableListOf(0, 1, 2)
                    val showcaseDataList = RoomManager.SQL_DB.showcaseDao().getByDocTP(
                        wpDataDB!!.client_id, wpDataDB.addr_id, list
                    )

                    val ids = showcaseDataList.map { it.photoId?.toString() }.toTypedArray()

                    val data: List<StackPhotoDB> =
                        RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getByIds2(ids))

                    repository.toItemUIList(StackPhotoDB::class, data, contextUI, 0)
                        .map {
                            val photoId: String =
                                VizitShowcaseDataHolder.getInstance()[id].showcasePhotoId.toString()
                            val justId =
                                (it.rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB)?.photoServerId.toString()
                            val selected =
                                justId == photoId
                            it.copy(selected = selected)
                        }
                }

                ContextUI.SHOWCASE_COMPLETED_CHECK
                    -> {
                    val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
                    val codeDad2 = dataJsonObject.get("wpDataDBId").asString.toLong()

                    val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                    )
                    val list = mutableListOf(0, 1, 2)
                    val showcaseDataList = RoomManager.SQL_DB.showcaseDao().getByDocTP(
                        wpDataDB!!.client_id, wpDataDB.addr_id, list
                    )

                    val ids = showcaseDataList.map { it.photoId?.toString() }.toTypedArray()

                    val data: List<StackPhotoDB> =
                        RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getByIds2(ids))

                    val listOfStackPhotoCOMPLETED = buildList {
                        addAll(
                            RealmManager.INSTANCE.copyFromRealm(
                                StackPhotoRealm.getPhotosByDAD2(
                                    codeDad2,
                                    0
                                )
                            )
                        )
                        addAll(
                            RealmManager.INSTANCE.copyFromRealm(
                                StackPhotoRealm.getPhotosByDAD2(
                                    codeDad2,
                                    45
                                )
                            )
                        )
                    }


                    val uniqueExampleIds: MutableSet<String> = HashSet()
//                    val dataList: MutableList<StackPhotoDB> = ArrayList()

//                    for (stackPhotoDB in listOfStackPhotoCOMPLETED) {
//                        val showcaseIdStack = stackPhotoDB.showcase_id
//                        if (showcaseIdStack != null && showcaseIdStack.isNotEmpty() && showcaseIdStack != "0") {
//                            val isShowcaseIdPresent = listOfStackPhotoCOMPLETED.any { photo ->
//                                photo.showcase_id?.let { id ->
//                                    showcaseDataList.any { showcase -> showcase.id.toString() == id }
//                                } ?: false
//                            }
//                            53920528
//                            if (isShowcaseIdPresent) {
//                                val exampleId = stackPhotoDB.getExample_img_id()
//                                if (exampleId != null && exampleId.isNotEmpty()  && uniqueExampleIds.add(exampleId)
//                                ) {
//                                }
//                            }
//                        }
//                    }

                    for (stackPhotoDB in listOfStackPhotoCOMPLETED) {
                        val showcaseIdStack = stackPhotoDB.showcase_id
                        if (!showcaseIdStack.isNullOrEmpty() && showcaseIdStack != "0") {

                            val isShowcaseIdPresent =
                                showcaseDataList.any { it.id.toString() == showcaseIdStack }

                            if (isShowcaseIdPresent) {
                                val exampleId = stackPhotoDB.getExample_img_id()
                                if (!exampleId.isNullOrEmpty() && uniqueExampleIds.add(exampleId)) {

                                    // Найдём соответствующий элемент в списке `data`
//                                    data.find { it.photoServerId == exampleId }?.let { dataItem ->
//                                        dataItem.specialCol = 1
//                                    }
                                    val dataItem = data.find { it.photoServerId == exampleId }

                                    if (dataItem != null) {
                                        dataItem.specialCol = 1
                                    }
                                    // Пройтись по всему списку и отметить 2 для всех без совпадения
                                    data.forEach { photo ->
                                        if (photo.specialCol == 0) {
                                            photo.specialCol = 2
                                        }
                                    }

                                }
                            }
                        }
                    }


                    repository.toItemUIList(StackPhotoDB::class, data, contextUI, 0)
                        .map {
                            val selected = FilteringDialogDataHolder.instance()
                                .filters
                                ?.items
                                ?.firstOrNull { it.clazz == table }
                                ?.rightValuesRaw
                                ?.contains((it.rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB)?.id.toString())
                            it.copy(selected = selected == true)
                        }
                }

                else -> {
                    emptyList()
                }
            }

        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        (itemsUI.first().rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB)?.let {
            when (contextUI) {
                ContextUI.SHOWCASE -> {
                    val dataHolder = VizitShowcaseDataHolder.getInstance()
                    dataHolder[planogrammId.value].showcaseId = it.showcase_id?.toIntOrNull() ?: 0
                    dataHolder[planogrammId.value].showcasePhotoId =
                        it.photoServerId?.toIntOrNull() ?: 0


//                    VizitShowcaseDataHolder.instance().planogrammVizitMap[planogrammId.value] =
//                        it.photoServerId
//                    it.getShowcase_id()
//                    VizitShowcaseDataHolder.instance().photoId = it.id
//                    VizitShowcaseDataHolder.instance().photoServerId = it.photoServerId
                }

                else -> {}
            }

        }
    }

}