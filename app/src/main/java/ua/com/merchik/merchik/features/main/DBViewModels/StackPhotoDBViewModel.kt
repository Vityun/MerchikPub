package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.Realm
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLog
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.MakePhoto.MakePhoto
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface.UploadPhotoReports
import ua.com.merchik.merchik.ServerExchange.PhotoDownload
import ua.com.merchik.merchik.ViewHolders.Clicks
import ua.com.merchik.merchik.WorkPlan
import ua.com.merchik.merchik.data.Database.Room.DynamicAchievementSDB
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest
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
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.math.abs
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
    private val DYNAMIC_ACHIEVEMENT_SELECT_ID_FILTER_KEY = "dynamic_achievement_select_id"
    private val stackPhotoDownloadsInProgress = ConcurrentHashMap.newKeySet<String>()
    private val stackPhotoDownloadFailures = ConcurrentHashMap.newKeySet<String>()
    private val _stackPhotoLoadingIds = MutableStateFlow<Set<String>>(emptySet())
    val stackPhotoLoadingIds: StateFlow<Set<String>> = _stackPhotoLoadingIds.asStateFlow()

    override val table: KClass<out DataObjectUI>
        get() = StackPhotoDB::class


    override fun onClickAdditionalContent() {
        openCamera(null) {
        }
    }


    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context) {
        val stackPhoto = clickedDataItemUI.rawObj
            .filterIsInstance<StackPhotoDB>()
            .firstOrNull()
        if (stackPhoto != null && !stackPhoto.hasLocalPhotoFile()) {
            requestStackPhotoIfNeeded(stackPhoto.photoServerId)
            Toast.makeText(context, "Фото завантажується", Toast.LENGTH_SHORT).show()
            return
        }

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
            ContextUI.STACK_PHOTO_DYNAMIC_ACHIEVEMENT -> {
                val selectId = getDynamicAchievementSelectIdFromDataJson()
                if (selectId == null) {
                    Globals.writeToMLOG(
                        "ERROR",
                        "StackPhotoDBViewModel.updateFilters",
                        "Не вдалося отримати select_id з dataJson: $dataJson"
                    )
                    return
                }

                val achievement = RoomManager.SQL_DB
                    .dynamicAchievementsDao()
                    .getBySelectId(selectId)
                    ?: RoomManager.SQL_DB
                        .dynamicAchievementsDao()
                        .getById(selectId)

                val achievementName = achievement
                    ?.nm
                    ?.takeIf { it.isNotBlank() }
                    ?: achievement
                        ?.selectName
                        ?.takeIf { it.isNotBlank() }
                    ?: selectId

                val filterDynamicAchievement = ItemFilter(
                    "Динамічне досягнення",
                    DynamicAchievementSDB::class,
                    DynamicAchievementSDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "Динамічне досягнення",
                    "Оберіть динамічне досягнення",
                    DYNAMIC_ACHIEVEMENT_SELECT_ID_FILTER_KEY,
                    "select_id",
                    emptyList(),
                    mutableListOf(achievementName),
                    false
                )

                filters = Filters(
                    rangeDataByKey = null,
                    items = mutableListOf(filterDynamicAchievement)
                )
            }

            ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT,
            ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT,
            ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT_YDERZHANIE,
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
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_169108,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_174213,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_172100,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_174878,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_175015 -> {

                var showcaseId: Int? = null

                /*
                 * Поддерживаем два формата dataJson:
                 *
                 * 1. Старый:
                 *    123456789
                 *
                 * 2. Новый:
                 *    {
                 *        "wpDataDBId": "123456789",
                 *        "showcaseId": 15
                 *    }
                 */
                val codeDad2 = try {
                    val dataJsonObject =
                        Gson().fromJson(dataJson, JsonObject::class.java)

                    val parsedCodeDad2 = dataJsonObject
                        .get("wpDataDBId")
                        ?.takeUnless { it.isJsonNull }
                        ?.asString
                        ?.toLongOrNull()
                        ?: throw IllegalArgumentException("wpDataDBId is missing")

                    dataJsonObject
                        .get("planogrammVizitShowcaseId")
                        ?.takeUnless { it.isJsonNull }
                        ?.asInt
                        ?.let { receivedPlanogrammId ->
                            planogrammId.value = receivedPlanogrammId
                        }

                    showcaseId = dataJsonObject
                        .get("showcaseId")
                        ?.takeUnless { it.isJsonNull }
                        ?.asString
                        ?.toIntOrNull()

                    parsedCodeDad2
                } catch (e: Exception) {
                    runCatching {
                        Gson().fromJson(dataJson, Long::class.java)
                    }.getOrNull()
                }

                if (codeDad2 == null) {
                    Toast.makeText(
                        context,
                        "Не вдалося визначити відвідування",
                        Toast.LENGTH_LONG
                    ).show()

                    Globals.writeToMLOG(
                        "ERROR",
                        "StackPhotoDBViewModel.updateFilters",
                        "Не вдалося отримати codeDad2 з dataJson: $dataJson"
                    )
                    return
                }

                val wpRow = RealmManager.getWorkPlanRowByCodeDad2(codeDad2)

                if (wpRow == null) {
                    Toast.makeText(
                        context,
                        "Дані застаріли і не можуть бути змінені у додатку",
                        Toast.LENGTH_LONG
                    ).show()

                    Globals.writeToMLOG(
                        "ERROR",
                        "StackPhotoDBViewModel.updateFilters",
                        "WpDataDB не знайдено. codeDad2: $codeDad2"
                    )
                    return
                }

                wpDataDB = RealmManager.INSTANCE.copyFromRealm(wpRow)

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

                    ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT,
                    ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT_YDERZHANIE -> 0

                    ContextUI.STACK_PHOTO_FROM_OPTION_158605 -> 40
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158 -> 4
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355 -> 5
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309 -> 39
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360 -> 31
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969 -> 10
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604 -> 41
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277 -> 28
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354 -> 42
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_169108 -> 47
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_172100 -> 48
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_174213 -> 49
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_174878 -> 50
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_175015 -> 51

                    else -> 0
                }

                val imagesType = PhotoTypeRealm.getPhotoTypeById(typePhotoId)
                val imagesTypeId = imagesType?.id ?: typePhotoId
                val imagesTypeName = imagesType?.nm ?: "Тип фото $typePhotoId"

                val filterImagesTypeListDB = ItemFilter(
                    "Тип фото",
                    ImagesTypeListDB::class,
                    ImagesTypeListDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "title",
                    "subTitle",
                    "photo_type",
                    "id",
                    mutableListOf(imagesTypeId.toString()),
                    mutableListOf(imagesTypeName),
                    true
                )

                val filterItems = mutableListOf(
                    filterWpDataDB,
                    filterImagesTypeListDB
                )


                showcaseId?.let { receivedShowcaseId ->
                    val showcase = RoomManager.SQL_DB
                        .showcaseDao()
                        .getById(receivedShowcaseId)

                    if (showcase != null) {
                        val filterShowcaseDB = ItemFilter(
                            "Вітрина",
                            ShowcaseSDB::class,
                            ShowcaseDBViewModel::class,
                            ModeUI.MULTI_SELECT,
                            "Вітрина",
                            "Оберіть поточну вітрину",

                            // Поле в StackPhotoDB
                            "showcase_id",

                            // Поле в ShowcaseSDB
                            "id",

                            mutableListOf(showcase.id.toString()),
                            mutableListOf(showcase.nm.orEmpty()),
                            true
                        )

                        filterItems.add(filterShowcaseDB)
                    } else {
                        Globals.writeToMLOG(
                            "ERROR",
                            "StackPhotoDBViewModel.updateFilters",
                            "ShowcaseSDB не знайдено. showcaseId: $receivedShowcaseId"
                        )
                    }
                }

                filters = Filters(
                    rangeDataByKey = null,
                    items = filterItems
                )
            }

            else -> Unit
        }
    }

    override suspend fun getItems(): List<DataItemUI> {
        return try {
            when (contextUI) {
                ContextUI.STACK_PHOTO_DYNAMIC_ACHIEVEMENT -> {
                    val selectId = getDynamicAchievementSelectIdFromDataJson()
                    if (selectId == null) {
                        Globals.writeToMLOG(
                            "ERROR",
                            "StackPhotoDBViewModel.getItems",
                            "Не вдалося отримати select_id з dataJson: $dataJson"
                        )
                        return emptyList()
                    }

                    val data = getDynamicAchievementStackPhotos(selectId)

                    repository.toItemUIList(
                        StackPhotoDB::class,
                        data,
                        contextUI,
                        null
                    )
                }

                ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT,
                ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT,
                ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT_YDERZHANIE,
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
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_169108,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_174213,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_172100,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_174878,
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_175015 -> {

                    var planogrammVizitShowcaseId = 0
                    var receivedShowcaseId: Int? = null

                    /*
                     * Поддерживаем:
                     *
                     * Старый формат:
                     * 123456789
                     *
                     * Формат планограммы:
                     * {
                     *     "wpDataDBId": "123456789",
                     *     "planogrammVizitShowcaseId": 3
                     * }
                     *
                     * Новый формат:
                     * {
                     *     "wpDataDBId": "123456789",
                     *     "showcaseId": 15
                     * }
                     */
                    val codeDad2 = try {
                        val dataJsonObject = Gson().fromJson(
                            dataJson,
                            JsonObject::class.java
                        )

                        val parsedCodeDad2 = dataJsonObject
                            .get("wpDataDBId")
                            ?.takeUnless { it.isJsonNull }
                            ?.asString
                            ?.toLongOrNull()
                            ?: throw IllegalArgumentException(
                                "wpDataDBId не передан"
                            )

                        planogrammVizitShowcaseId = dataJsonObject
                            .get("planogrammVizitShowcaseId")
                            ?.takeUnless { it.isJsonNull }
                            ?.asString
                            ?.toIntOrNull()
                            ?: 0

                        receivedShowcaseId = dataJsonObject
                            .get("showcaseId")
                            ?.takeUnless { it.isJsonNull }
                            ?.asString
                            ?.toIntOrNull()

                        parsedCodeDad2
                    } catch (e: Exception) {
                        Gson().fromJson(dataJson, Long::class.java)
                    }

                    Globals.writeToMLOG(
                        "INFO",
                        "StackPhotoDBViewModel.getItems",
                        "codeDad2: $codeDad2, " +
                                "planogrammVizitShowcaseId: $planogrammVizitShowcaseId, " +
                                "showcaseId: $receivedShowcaseId"
                    )

                    val typePhoto = when (contextUI) {
                        ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT,
                        ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT -> 14

                        ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT_YDERZHANIE,
                        ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT -> 0

                        ContextUI.STACK_PHOTO_FROM_OPTION_158605 -> 40
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158 -> 4
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355 -> 5
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309 -> 39
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360 -> 31
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969 -> 10
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604 -> 41
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277 -> 28
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354 -> 42
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_169108 -> 47
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_172100 -> 48
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_174213 -> 49
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_174878 -> 50
                        ContextUI.SAMPLE_PHOTO_FROM_OPTION_175015 -> 51

                        else -> 0
                    }

                    val data = RealmManager.INSTANCE.copyFromRealm(
                        StackPhotoRealm.getPhotosByDAD2(
                            codeDad2,
                            typePhoto
                        )
                    )

                    /*
                     * Если передана витрина, находим её и связываем
                     * с фотографией по ShowcaseSDB.photoId.
                     */
                    receivedShowcaseId?.let { showcaseId ->
                        val showcase = RoomManager.SQL_DB
                            .showcaseDao()
                            .getById(showcaseId)

                        if (showcase != null) {
                            val showcasePhotoId = showcase.photoId?.toString()

                            data.forEach { photo ->
                                val isShowcasePhoto =
                                    !showcasePhotoId.isNullOrBlank() &&
                                            photo.photoServerId == showcasePhotoId

                                if (isShowcasePhoto) {
                                    photo.showcaseId = showcase.id
                                    photo.showcaseName = showcase.nm.orEmpty()
                                }
                            }
                        } else {
                            Globals.writeToMLOG(
                                "ERROR",
                                "StackPhotoDBViewModel.getItems",
                                "ShowcaseSDB не знайдено. showcaseId: $showcaseId"
                            )
                        }
                    }

                    scheduleMissingStackPhotoFiles(data)
                    data.reverse()

                    repository
                        .toItemUIList(
                            StackPhotoDB::class,
                            data,
                            contextUI,
                            typePhoto
                        )
                        .map { item ->
                            val selectedPhotoId = when (contextUI) {
                                ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT_YDERZHANIE,
                                ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT -> {
                                    AchievementDataHolder.instance().photoToId
                                }

                                ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT -> {
                                    AchievementDataHolder.instance().photoAfterId
                                }

                                ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT -> {
                                    VizitShowcaseDataHolder
                                        .getInstance()[planogrammVizitShowcaseId]
                                        .photoDoId
                                }

                                else -> 0
                            }

                            val stackPhoto = item.rawObj
                                .filterIsInstance<StackPhotoDB>()
                                .firstOrNull()

                            val selected = when (contextUI) {
                                ContextUI.STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT -> {
                                    stackPhoto?.photoServerId ==
                                            selectedPhotoId.toString()
                                }

                                else -> {
                                    stackPhoto?.id == selectedPhotoId
                                }
                            }

                            item.copy(selected = selected)
                        }
                }

                else -> emptyList()
            }
        } catch (e: Exception) {
            Globals.writeToMLOG(
                "ERROR",
                "StackPhotoDBViewModel.getItems",
                "Exception: $e"
            )

            emptyList()
        }
    }

    private fun getDynamicAchievementSelectIdFromDataJson(): String? {
        val raw = dataJson?.trim() ?: return null
        if (raw.isEmpty() || raw == "0" || raw.equals("null", ignoreCase = true)) {
            return null
        }

        return runCatching {
            val jsonObject = Gson().fromJson(raw, JsonObject::class.java)
            jsonObject
                ?.get("select_id")
                ?.takeUnless { it.isJsonNull }
                ?.asString
                ?.trim()
                ?.takeIf { it.isNotEmpty() && it != "0" && !it.equals("null", ignoreCase = true) }
                ?: jsonObject
                    ?.get("selectId")
                    ?.takeUnless { it.isJsonNull }
                    ?.asString
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() && it != "0" && !it.equals("null", ignoreCase = true) }
                ?: raw.trim('"')
        }.getOrElse {
            raw.trim('"')
        }?.takeIf { it.isNotEmpty() && it != "0" && !it.equals("null", ignoreCase = true) }
    }

    private fun getDynamicAchievementStackPhotos(selectId: String): List<StackPhotoDB> {
        val achievement = runCatching {
            RoomManager.SQL_DB.dynamicAchievementsDao().getBySelectId(selectId)
        }.getOrNull()
        val achievementId = achievement
            ?.id
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it != selectId }

        val dynamicPhotos = runCatching {
            val bySelectId = RoomManager.SQL_DB.dynamicPhotoDao().getByDynamicAchievementId(selectId)
            if (bySelectId.isNotEmpty() || achievementId == null) {
                bySelectId
            } else {
                RoomManager.SQL_DB.dynamicPhotoDao().getByDynamicAchievementId(achievementId)
            }
        }.onFailure { error ->
            Globals.writeToMLOG(
                "ERROR",
                "StackPhotoDBViewModel.getDynamicAchievementStackPhotos",
                "selectId=$selectId, dynamic_photo read error=$error"
            )
        }.getOrDefault(emptyList())

        val photoIds = dynamicPhotos
            .mapNotNull { it.photoId.normalizePhotoServerId() }
            .distinct()

        if (photoIds.isEmpty()) {
            Globals.writeToMLOG(
                "INFO",
                "StackPhotoDBViewModel.getDynamicAchievementStackPhotos",
                "selectId=$selectId, dynamicPhotoRows=${dynamicPhotos.size}, photoIds=0"
            )
            return emptyList()
        }

        val photosByServerId = LinkedHashMap<String, StackPhotoDB>()
        val realm = Realm.getDefaultInstance()
        try {
            val realmPhotos = realm.where(StackPhotoDB::class.java)
                .`in`("photoServerId", photoIds.toTypedArray())
                .findAll()

            realm.copyFromRealm(realmPhotos).forEach { photo ->
                photo.photoServerId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { photosByServerId[it] = photo }
            }
        } catch (e: Exception) {
            Globals.writeToMLOG(
                "ERROR",
                "StackPhotoDBViewModel.getDynamicAchievementStackPhotos",
                "selectId=$selectId, StackPhotoDB read error=$e"
            )
        } finally {
            realm.close()
        }

        val result = photoIds.map { photoServerId ->
            val stackPhoto = photosByServerId[photoServerId]
            if (stackPhoto == null || !stackPhoto.hasLocalPhotoFile()) {
                requestStackPhotoIfNeeded(photoServerId)
            }

            stackPhoto ?: createLoadingStackPhotoPlaceholder(photoServerId)
        }
        val missingIds = photoIds.filterNot { photosByServerId.containsKey(it) }
        if (missingIds.isNotEmpty()) {
            Globals.writeToMLOG(
                "WARN",
                "StackPhotoDBViewModel.getDynamicAchievementStackPhotos",
                "selectId=$selectId, dynamicPhotoRows=${dynamicPhotos.size}, photoIds=${photoIds.size}, " +
                        "found=${photosByServerId.size}, missing=${missingIds.size}, missingIds=${missingIds.previewIds()}"
            )
        } else {
            Globals.writeToMLOG(
                "INFO",
                "StackPhotoDBViewModel.getDynamicAchievementStackPhotos",
                "selectId=$selectId, dynamicPhotoRows=${dynamicPhotos.size}, photoIds=${photoIds.size}, found=${photosByServerId.size}"
            )
        }

        return result
    }

    fun getLoadingImageIndexes(itemUI: DataItemUI, loadingPhotoIds: Set<String>): Set<Int> {
        val stackPhoto = itemUI.rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB
            ?: return emptySet()
        val photoServerId = stackPhoto.photoServerId.normalizePhotoServerId()
            ?: return emptySet()

        return if (photoServerId in loadingPhotoIds) setOf(0) else emptySet()
    }

    private fun scheduleMissingStackPhotoFiles(data: List<StackPhotoDB>) {
        data.forEach { stackPhoto ->
            if (!stackPhoto.hasLocalPhotoFile()) {
                requestStackPhotoIfNeeded(stackPhoto.photoServerId)
            }
        }
    }

    private fun requestStackPhotoIfNeeded(rawPhotoServerId: String?) {
        val photoServerId = rawPhotoServerId.normalizePhotoServerId() ?: return
        if (stackPhotoDownloadFailures.contains(photoServerId)) return

        val localPhoto = getLocalStackPhotoByServerId(photoServerId)
        if (localPhoto?.hasLocalPhotoFile() == true) return
        if (!stackPhotoDownloadsInProgress.add(photoServerId)) return
        updateStackPhotoLoadingState()

        scheduleStackPhotoDownloadTimeout(photoServerId)

        if (localPhoto != null) {
            if (localPhoto.photoServerURL.isNullOrBlank()) {
                finishStackPhotoDownload(
                    photoServerId = photoServerId,
                    success = false,
                    error = "StackPhotoDB exists without photoServerURL"
                )
            } else {
                downloadStackPhotoFile(photoServerId, localPhoto)
            }
            return
        }

        val request = PhotoTableRequest().apply {
            mod = "images_view"
            act = "list_image"
            nolimit = "1"
            id_list = photoServerId
        }

        PhotoDownload().getPhotoInfoAndSaveItToDB(
            request,
            object : Clicks.clickObjectAndStatus<StackPhotoDB> {
                override fun onSuccess(data: StackPhotoDB) {
                    if (data.photoServerURL.isNullOrBlank()) {
                        finishStackPhotoDownload(
                            photoServerId = photoServerId,
                            success = false,
                            error = "images_view.list_image returned empty photoServerURL"
                        )
                    } else {
                        downloadStackPhotoFile(photoServerId, data)
                    }
                }

                override fun onFailure(error: String) {
                    finishStackPhotoDownload(photoServerId, success = false, error = error)
                }
            }
        )
    }

    private fun downloadStackPhotoFile(photoServerId: String, stackPhotoDB: StackPhotoDB) {
        PhotoDownload().downloadPhoto(
            true,
            stackPhotoDB,
            STACK_PHOTO_DOWNLOAD_FOLDER,
            object : PhotoDownload.downloadPhotoInterface {
                override fun onSuccess(data: StackPhotoDB) {
                    finishStackPhotoDownload(photoServerId, success = true)
                }

                override fun onFailure(s: String) {
                    finishStackPhotoDownload(photoServerId, success = false, error = s)
                }
            }
        )
    }

    private fun finishStackPhotoDownload(
        photoServerId: String,
        success: Boolean,
        error: String? = null
    ) {
        viewModelScope.launch {
            stackPhotoDownloadsInProgress.remove(photoServerId)
            if (success) {
                stackPhotoDownloadFailures.remove(photoServerId)
            } else {
                stackPhotoDownloadFailures.add(photoServerId)
                Globals.writeToMLOG(
                    "ERROR",
                    "StackPhotoDBViewModel/downloadStackPhoto",
                    "photoServerId=$photoServerId, error=$error"
                )
            }
            updateStackPhotoLoadingState()
            if (success) updateContent()
        }
    }

    private fun scheduleStackPhotoDownloadTimeout(photoServerId: String) {
        viewModelScope.launch {
            delay(STACK_PHOTO_DOWNLOAD_TIMEOUT_MS)
            if (stackPhotoDownloadsInProgress.remove(photoServerId)) {
                stackPhotoDownloadFailures.add(photoServerId)
                updateStackPhotoLoadingState()
                Globals.writeToMLOG(
                    "ERROR",
                    "StackPhotoDBViewModel/downloadStackPhoto",
                    "photoServerId=$photoServerId, timeout=${STACK_PHOTO_DOWNLOAD_TIMEOUT_MS}ms"
                )
            }
        }
    }

    private fun updateStackPhotoLoadingState() {
        _stackPhotoLoadingIds.value = stackPhotoDownloadsInProgress.toSet()
    }

    private fun getLocalStackPhotoByServerId(photoServerId: String): StackPhotoDB? {
        val realm = Realm.getDefaultInstance()
        return try {
            realm.where(StackPhotoDB::class.java)
                .equalTo("photoServerId", photoServerId)
                .findFirst()
                ?.let { realm.copyFromRealm(it) }
        } catch (e: Exception) {
            Globals.writeToMLOG(
                "ERROR",
                "StackPhotoDBViewModel/getLocalStackPhotoByServerId",
                "photoServerId=$photoServerId, error=$e"
            )
            null
        } finally {
            realm.close()
        }
    }

    private fun createLoadingStackPhotoPlaceholder(photoServerId: String): StackPhotoDB {
        return StackPhotoDB().apply {
            id = createLoadingStackPhotoId(photoServerId)
            this.photoServerId = photoServerId
        }
    }

    private fun createLoadingStackPhotoId(photoServerId: String): Int {
        val hash = photoServerId.hashCode()
        val positiveHash = if (hash == Int.MIN_VALUE) Int.MAX_VALUE else abs(hash)
        return -(if (positiveHash == 0) 1 else positiveHash)
    }

    private fun StackPhotoDB.hasLocalPhotoFile(): Boolean {
        val path = photo_num?.takeIf { it.isNotBlank() } ?: return false
        return File(path).exists()
    }

    private fun String?.normalizePhotoServerId(): String? {
        return this
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it != "0" && !it.equals("null", ignoreCase = true) }
    }

    private fun List<String>.previewIds(limit: Int = 25): String {
        val preview = take(limit)
        val suffix = if (size > limit) "... +${size - limit}" else ""
        return "$preview$suffix"
    }

    private companion object {
        private const val STACK_PHOTO_DOWNLOAD_FOLDER = "/StackPhoto"
        private const val STACK_PHOTO_DOWNLOAD_TIMEOUT_MS = 45_000L
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        (itemsUI.first().rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB)?.let {
            when (contextUI) {
                ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT_YDERZHANIE,
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
                        ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT_YDERZHANIE,
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

    override fun getDefaultHideUserFields(): List<String> {
        return ("addr_id, approve, code_dad2, dviUpload, errorTime, dvi, upload_status, premiyaUpload, specialCol, " +
                "commentUpload, upload_time, upload_to_server, vpi, client_id, dt, photoServerURL, showcase_id, " +
                "time_event, tovar_id, user_id, photo_typeTxt, code_iza, example_id, example_img_id, planogram_id, " +
                "planogram_img_id, photoServerId, showcaseName, showcaseId, showcase_id, statusShowcase, mainOption," +
                "get_on_server, photo_hash").split(",")
    }

    override fun onLongClickItem(itemUI: DataItemUI, context: Context) {

        val stackPhotoDB: StackPhotoDB? =
            itemUI.rawObj.filterIsInstance<StackPhotoDB>().firstOrNull()
        stackPhotoDB?.let {
            PhotoLog().sendPhotoOnServer(context, it, object : UploadPhotoReports {
                override fun onSuccess(photoDB: StackPhotoDB, s: String) {
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("photoDB: ").append("{").append(photoDB.id).append("|")
                        .append(photoDB.getPhotoServerId()).append("}").append("s: ").append(s)

                    photoDB.setError(null)
                    photoDB.setUpload_to_server(System.currentTimeMillis())
                    RealmManager.stackPhotoSavePhoto(photoDB);
                    updateContent()

                    Globals.writeToMLOG("INFO", "долгий клик по фото/onSuccess", "" + stringBuilder)
                    Toast.makeText(context, "Фото вивантаженно.", Toast.LENGTH_SHORT).show()

                }

                override fun onFailure(photoDB: StackPhotoDB, error: String) {
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("photoDB: ").append("{").append(photoDB.id).append("|")
                        .append(photoDB.getPhotoServerId()).append("}").append("error: ")
                        .append(error)

                    Globals.writeToMLOG("INFO", "долгий клик по фото/onFailure", "" + stringBuilder)
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()

                }
            })
            Toast.makeText(context, "Починаю вивантаження фото.", Toast.LENGTH_SHORT).show()
        }
    }

}
