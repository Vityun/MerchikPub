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
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
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

    override fun getDefaultHideUserFields(): List<String>? {
//        return when (contextUI) {
//            ContextUI.SHOWCASE,
//            ContextUI.SHOWCASE_FROM_ACHIEVEMENT ->
                return listOf(
                    "column_name",
                    "group_header",

                    "id", "photoServerId", "dt", "object_id", "user_id", "addr_id", "client_id", "theme_id", "tovar_id", "time_event", "vpi", "create_time",
                    "upload_to_server", "get_on_server", "code_dad2", "photo_num", "photo_hash", "photo_type", "photo_size", "photo_user_id", "photo_group_id",
                    "doc_id", "comment", "gp", "upload_time", "upload_status", "status", "error", "errorTime", "errorTxt", "userTxt", "customerTxt", "addressTxt",
                    "photo_typeTxt", "approve", "dvi", "mark", "premiya", "photoServerURL", "dviUpload", "commentUpload", "markUpload", "premiyaUpload", "img_src_id",
                    "showcase_id", "code_iza", "planogram_id", "planogram_img_id", "example_id", "example_img_id", "specialCol"
                )
//                ("column_name, group_header").split(",")


//            else -> null
//        }
    }


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


    override fun getItemsFooter(): List<DataItemUI> {
        return when (contextUI) {
            ContextUI.SHOWCASE_FROM_ACHIEVEMENT -> {
                val data = StackPhotoDB::class.java.newInstance()
                data.comment = "Це досягнення не відноситься до жодної з пропозицій замовника"
                data.id = -999
                data.photoServerId = "-999"
                data.photo_hash = "-999"
                data.showcaseId = 0
                data.specialCol = -1
                data.showcaseName = "Створити фото без зазначення вітрини"
                repository.toItemUIList(
                    StackPhotoDB::class,
                    listOf(data),
                    contextUI,
                    null
                )
            }

            else -> {
                emptyList()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override suspend fun getItems(): List<DataItemUI> {
        return try {
            when (contextUI) {
                ContextUI.SHOWCASE,
                ContextUI.SHOWCASE_FROM_ACHIEVEMENT -> {

                    val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
                    val codeDad2 = dataJsonObject["wpDataDBId"].asString.toLong()

                    val planogrammVizitShowcaseId =
                        dataJsonObject
                            .takeIf { it.has("planogrammVizitShowcaseId") }
                            ?.get("planogrammVizitShowcaseId")
                            ?.asInt
                            ?: 0

                    val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                    ) ?: return emptyList()

                    val showcaseTypes = listOf(0, 1, 2)

                    val showcaseDataList = RoomManager.SQL_DB
                        .showcaseDao()
                        .getByDocTP(
                            wpDataDB.client_id,
                            wpDataDB.addr_id,
                            showcaseTypes
                        )

                    /*
                     * Сопоставляем photoId фотографии с соответствующей витриной.
                     *
                     * Ключ приводим к String, потому что photoServerId в StackPhotoDB
                     * ниже также сравнивается как строка.
                     */
                    val showcaseByPhotoId = showcaseDataList
                        .mapNotNull { showcase ->
                            showcase.photoId
                                ?.toString()
                                ?.takeIf { it.isNotBlank() }
                                ?.let { photoId ->
                                    photoId to showcase
                                }
                        }
                        .toMap()

                    val photoIds = showcaseByPhotoId.keys.toTypedArray()

                    val photos = RealmManager.INSTANCE.copyFromRealm(
                        StackPhotoRealm.getByIds2(photoIds)
                    ).onEach { photo ->

                        val showcase = showcaseByPhotoId[
                            photo.photoServerId?.toString()
                        ]

                        photo.showcaseId = showcase?.id ?: 0
                        photo.showcaseName = showcase?.nm.orEmpty()
                        photo.specialCol = -1
                    }

                    val selectedPhotoId = VizitShowcaseDataHolder
                        .getInstance()[planogrammVizitShowcaseId]
                        .showcasePhotoId
                        .toString()

                    repository
                        .toItemUIList(
                            StackPhotoDB::class,
                            photos,
                            contextUI,
                            0
                        )
                        .map { item ->

                            val stackPhoto = item.rawObj
                                .filterIsInstance<StackPhotoDB>()
                                .firstOrNull()

                            item.copy(
                                selected = stackPhoto
                                    ?.photoServerId
                                    ?.toString() == selectedPhotoId
                            )
                        }
                }
                ContextUI.SHOWCASE_COMPLETED_CHECK -> {
                    val dataJsonObject = Gson().fromJson(
                        dataJson,
                        JsonObject::class.java
                    )

                    val codeDad2 = dataJsonObject
                        .get("wpDataDBId")
                        .asString
                        .toLong()

                    val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                    ) ?: return emptyList()

                    val showcaseTypes = listOf(0, 1, 2)

                    val showcaseDataList = RoomManager.SQL_DB
                        .showcaseDao()
                        .getByDocTP(
                            wpDataDB.client_id,
                            wpDataDB.addr_id,
                            showcaseTypes
                        )

                    /*
                     * Связываем ID фотографии с витриной.
                     */
                    val showcaseByPhotoId = showcaseDataList
                        .mapNotNull { showcase ->
                            showcase.photoId
                                ?.toString()
                                ?.takeIf { it.isNotBlank() && it != "0" }
                                ?.let { photoId ->
                                    photoId to showcase
                                }
                        }
                        .toMap()

                    val photoIds = showcaseByPhotoId
                        .keys
                        .toTypedArray()

                    /*
                     * Получаем фотографии витрин и записываем в каждую:
                     * showcaseId и showcaseName.
                     *
                     * specialCol здесь не меняем.
                     */
                    val data: List<StackPhotoDB> =
                        RealmManager.INSTANCE.copyFromRealm(
                            StackPhotoRealm.getByIds2(photoIds)
                        ).onEach { photo ->
                            val showcase = showcaseByPhotoId[
                                photo.photoServerId?.toString()
                            ]

                            photo.showcaseId = showcase?.id ?: 0
                            photo.showcaseName = showcase?.nm.orEmpty()
                        }

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

                    val uniqueExampleIds = mutableSetOf<String>()

                    for (stackPhotoDB in listOfStackPhotoCOMPLETED) {
                        val showcaseIdStack = stackPhotoDB.showcase_id

                        if (
                            showcaseIdStack.isNullOrEmpty() ||
                            showcaseIdStack == "0"
                        ) {
                            continue
                        }

                        val isShowcaseIdPresent = showcaseDataList.any { showcase ->
                            showcase.id.toString() == showcaseIdStack
                        }

                        if (!isShowcaseIdPresent) {
                            continue
                        }

                        val exampleId = stackPhotoDB.example_img_id

                        if (
                            exampleId.isNullOrEmpty() ||
                            !uniqueExampleIds.add(exampleId)
                        ) {
                            continue
                        }

                        val dataItem = data.find { photo ->
                            photo.photoServerId == exampleId
                        }

                        if (dataItem != null) {
                            dataItem.specialCol = 1
                        }

                        /*
                         * Сохраняем существующую логику цветов:
                         * 1 — фотография найдена;
                         * 2 — остальные фотографии.
                         */
                        data.forEach { photo ->
                            if (photo.specialCol == 0) {
                                photo.specialCol = 2
                            }
                        }
                    }

                    repository
                        .toItemUIList(
                            StackPhotoDB::class,
                            data,
                            contextUI,
                            0
                        )
                        .map { item ->
                            val stackPhoto = item.rawObj
                                .filterIsInstance<StackPhotoDB>()
                                .firstOrNull()

                            val selected = FilteringDialogDataHolder
                                .instance()
                                .filters
                                ?.items
                                ?.firstOrNull { filter ->
                                    filter.clazz == table
                                }
                                ?.rightValuesRaw
                                ?.contains(stackPhoto?.id?.toString())

                            item.copy(
                                selected = selected == true
                            )
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
                }

                ContextUI.SHOWCASE_FROM_ACHIEVEMENT -> {
                    AchievementDataHolder.instance().showcaseId = it.showcaseId
                    AchievementDataHolder.instance().showcaseName = "№${it.showcaseId} ${it.showcaseName}"
                }

                else -> {}
            }

        }
    }

}