package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.MakePhoto.MakePhoto
import ua.com.merchik.merchik.WorkPlan
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.data.WPDataObj
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.common.VizitShowcaseDataHolder
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm
import ua.com.merchik.merchik.database.realm.tables.PhotoTypeRealm
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class StackPhotoDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    lateinit var wpDataDB: WpDataDB
    private val planogrammId = mutableStateOf(0)

    private val EXAMPLE_ID = "id_1c"
    private val EXAMPLE_IMG_ID = "photo_id"

    override val table: KClass<out DataObjectUI>
        get() = StackPhotoDB::class


    override fun onClickAdditionalContent() {
        openCamera(null) {
        }
    }


    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context) {
        super.onClickItemImage(clickedDataItemUI, context)
        dialog?.setCamera {
            openCamera(null) {
                dialog?.dismiss()
            }
        }
    }

    override fun onClickFullImage(stackPhotoDB: StackPhotoDB, comment: String?) {
        Log.e("onClickFullImage", "!!!!!!!!!")
        try {
            val dialogFullPhoto = DialogFullPhotoR(context)
            dialogFullPhoto.setPhoto(stackPhotoDB)
            comment?.let { dialogFullPhoto.setComment(it) }

            dialogFullPhoto.setClose { dialogFullPhoto.dismiss() }
            dialogFullPhoto.show()
        } catch (e: Exception) {
            Log.e("ShowcaseAdapter", "Exception e: $e")
        }
    }

    override fun updateFilters() {
        when (contextUI) {
            ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT,
            ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT,
            ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354,
            ContextUI.STACK_PHOTO_FROM_OPTION_158605,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_169108
            -> {

                try {
                    val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)

                    val codeDad2 = dataJsonObject.get("wpDataDBId").asString.toLong()
                    planogrammId.value = dataJsonObject.get("planogrammVizitShowcaseId").asInt
                    wpDataDB = RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                    )

                } catch (e: Exception) {
                    val codeDad2 = Gson().fromJson(dataJson, Long::class.java)
                    val wpRow = RealmManager.getWorkPlanRowByCodeDad2(codeDad2)

                    if (wpRow != null) {
                        wpDataDB = RealmManager.INSTANCE.copyFromRealm(wpRow)
                    } else {
                        // Обработка ситуации, когда запись не найдена
                        Toast.makeText(
                            context,
                            "Дані застаріли і не можуть бути змінені у додатку",
                            Toast.LENGTH_LONG
                        ).show()
                        Globals.writeToMLOG(
                            "ERROR", "StackPhotoDBViewModel.updateFilters",
                            "Exception e: $e"
                        )
                        return
                    }
                }

                val filterWpDataDB = ItemFilter(
                    "Відвідування",
                    WpDataDB::class,
                    WpDataDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "title",
                    "subTitle",
                    "code_dad2",
                    "code_dad2",
                    mutableListOf(wpDataDB.code_dad2.toString()),
                    mutableListOf(wpDataDB.code_dad2.toString()),
                    false
                )

                val typePhotoId = when (contextUI) {
                    ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT,
                    ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT -> 14

                    ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT -> 0
                    ContextUI.STACK_PHOTO_FROM_OPTION_158605 -> 40
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158 -> 4
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355 -> 5
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309 -> 39
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360 -> 31
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969 -> 10
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604 -> 41
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277 -> 26
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354 -> 42
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_169108 -> 47
                    else -> 0 // Резервное значение, не должно использоваться
                }

                val imagesType =
                    RealmManager.INSTANCE.copyFromRealm(PhotoTypeRealm.getPhotoTypeById(typePhotoId))
                val filterImagesTypeListDB = ItemFilter(
                    "Тип фото",
                    ImagesTypeListDB::class,
                    ImagesTypeListDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "title",
                    "subTitle",
                    "photo_type",
                    "id",
                    mutableListOf(imagesType.id.toString()),
                    mutableListOf(imagesType.nm),
                    true
                )

                filters = Filters(
                    rangeDataByKey = null,
                    items = mutableListOf(
                        filterWpDataDB,
                        filterImagesTypeListDB
                    )
                )
            }

            else -> {}
        }
    }

    override suspend fun getItems(): List<DataItemUI> {
        return try {
            when (contextUI) {
                ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT,
                ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT,
                ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354,
                ContextUI.STACK_PHOTO_FROM_OPTION_158605,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_169108
                -> {
                    var codeDad2: Long
                    var id: Int
                    try {
                        val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
                        codeDad2 = dataJsonObject.get("wpDataDBId").asString.toLong()
                        id = dataJsonObject.get("planogrammVizitShowcaseId").asInt

                    } catch (e: Exception) {
                        codeDad2 = Gson().fromJson(dataJson, Long::class.java)
                        id = 0
                    }
//                    val codeDad2 = Gson().fromJson(dataJson, Long::class.java)
//                    val wpDataDB = RealmManager.INSTANCE.copyFromRealm(
//                        RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
//                    )

                    val typePhoto = when (contextUI) {
                        ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT,
                        ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT -> 14

                        ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT -> 0
                        ContextUI.STACK_PHOTO_FROM_OPTION_158605 -> 40
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158 -> 4
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355 -> 5
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309 -> 39
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360 -> 31
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969 -> 10
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604 -> 41
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277 -> 26
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354 -> 42
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_169108 -> 47
                        else -> 0 // Резервное значение, не должно использоваться
                    }

                    val data = RealmManager.INSTANCE.copyFromRealm(
                        StackPhotoRealm.getPhotosByDAD2(
                            codeDad2,
                            typePhoto
                        )
                    )

                    data.reverse() //25.04.2025 добавил обратную сортировку

                    repository.toItemUIList(StackPhotoDB::class, data, contextUI, typePhoto)
                        .map {
                            val photoId = when (contextUI) {
                                ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT ->
                                    AchievementDataHolder.instance().photoToId

                                ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT -> AchievementDataHolder.instance().photoAfterId
                                ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT -> VizitShowcaseDataHolder.getInstance()[id].photoDoId

                                else -> 0
                            }
                            val selected = when (contextUI) {
                                ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT ->
                                    (it.rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB)?.photoServerId == photoId.toString()

                                else -> (it.rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB)?.id == photoId
                            }
                            it.copy(selected = selected)

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
                ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT -> {
                    AchievementDataHolder.instance().photoToId = it.id
                    AchievementDataHolder.instance().photoToURI = it.photo_num
                    AchievementDataHolder.instance().photoHashTo = it.photo_hash
                }

                ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT -> {
                    AchievementDataHolder.instance().photoAfterId = it.id
                    AchievementDataHolder.instance().photoAfterURI = it.photo_num
                    AchievementDataHolder.instance().photoHashAfter = it.photo_hash
                }

                ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT -> {
                    VizitShowcaseDataHolder.getInstance()[planogrammId.value].photoDoId =
                        it.photoServerId?.toIntOrNull() ?: 0
                    VizitShowcaseDataHolder.getInstance()[planogrammId.value].photoDoHash =
                        it.photo_hash ?: "0"

                    Log.e("!", "+")
                }

                else -> {}
            }

        }
    }

    private fun openCamera(stackPhotoDB: StackPhotoDB?, callback: () -> Unit) {
        when (contextUI) {
            ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT
            -> {
                val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
                val wpDataDB =
                    WpDataRealm.getWpDataRowByDad2Id(
                        dataJsonObject.get("wpDataDBId").asString.toLong()
                    )
                val optionDB =
                    RealmManager.INSTANCE.copyFromRealm(
                        OptionsRealm.getOptionById(
                            dataJsonObject.get(
                                "optionDBId"
                            ).asString
                        )
                    )
                if (wpDataDB != null) {
                    val typePhotoId = when (contextUI) {
                        ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT -> 14
                        else -> {
                            null
                        }
                    }


                    typePhotoId?.let { it ->
                        val workPlan = WorkPlan()
                        val wpDataObj: WPDataObj = workPlan.getKPS(wpDataDB.id)
                        wpDataObj.setPhotoType(it.toString())
                        val makePhoto = MakePhoto()
                        val custom: HashMap<String, Any?> = valueForCustomResult.value
                        // Проверяем и передаем example_id
                        custom[EXAMPLE_ID]?.let {
                            if (it.toString().isNotEmpty()) {
                                MakePhoto.example_id = it.toString()
                            }
                        }

                        // Проверяем и передаем example_img_id
                        custom[EXAMPLE_IMG_ID]?.let {
                            if (it.toString().isNotEmpty()) {
                                MakePhoto.example_img_id = it.toString()
                            }
                        }

                        makePhoto.pressedMakePhotoOldStyle<WpDataDB>(
                            context as Activity,
                            wpDataObj,
                            wpDataDB,
                            optionDB,
                            stackPhotoDB
                        )
                        callback.invoke()
                    }

                }
            }

            else -> {}
        }
    }
}