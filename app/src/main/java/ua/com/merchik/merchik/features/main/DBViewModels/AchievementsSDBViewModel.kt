package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.ServerExchange.PhotoDownload
import ua.com.merchik.merchik.ViewHolders.Clicks
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.SelectedMode
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.DialogCreateAchievement
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.dialogs.DialogData
import ua.com.merchik.merchik.dialogs.DialogData.DialogClickListener
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.RangeDate
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class AchievementsSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = AchievementsSDB::class

    private val scheduledAchievementPhotoIds = ConcurrentHashMap.newKeySet<String>()
    private val achievementPhotoDownloadsInProgress = ConcurrentHashMap.newKeySet<String>()
    private val achievementPhotoDownloadFailures = ConcurrentHashMap.newKeySet<String>()
    private val _achievementPhotoLoadingIds = MutableStateFlow<Set<String>>(emptySet())
    val achievementPhotoLoadingIds: StateFlow<Set<String>> =
        _achievementPhotoLoadingIds.asStateFlow()


    override fun getDefaultSortUserFields(): List<String>? {
        return listOf(
            "dt", "addr_id", "client_id", "theme_id", "user_id", "showcase_nm", "comment_txt"
        )
    }

    override fun getDefaultHideUserFields(): List<String> = listOf(
//        "group_header",
//        "id_res_image",
        "id",
        "ID",
        "dt_ut",
        "img_before_id",
        "img_before",
        "img_before_big",
        "img_after_id",
        "img_after",
        "img_after_big",
        "score",
        "score_who_nm",
        "score_dt",
        "adresa_nm",
        "adresa_addr",
        "adresa_tp",
        "spiskli_nm",
        "code_dad2",
        "sotr_fio",
        "comment_dt",
        "comment_user",
        "prem_reason",
        "prem_amount",
        "prem_amount_dt",
        "prem_sotr",
        "dvi",
        "confirm_state",
        "img_before_hash",
        "img_after_hash",
        "add_requirement_id",
        "manufacturer",
        "tovar_id",
        "dt_change",
        "error",
        "note",
        "currentVisit",
        "showcase_id"
    )

    override suspend fun getItems(): List<DataItemUI> {
        return when (contextUI) {
            ContextUI.ACHIEVEMENT -> getAchievementItems()
            else -> emptyList()
        }
    }

    override fun updateFilters() {
        if (contextUI != ContextUI.ACHIEVEMENT) {
            super.updateFilters()
            return
        }

        val wpDataDB = getAchievementWpDataOrNull()
        if (wpDataDB == null) {
            Globals.writeToMLOG(
                "ERROR",
                "AchievementsSDBViewModel/updateFilters",
                "wpDataDB is null, dataJson=$dataJson"
            )

            updateFilters(
                Filters(
                    searchText = "",
                    items = emptyList(),
                    rangeDataByKey = RangeDate(
                        key = ACHIEVEMENT_DATE_FILTER_KEY,
                        start = rangeDataStart.value,
                        end = rangeDataEnd.value,
                        enabled = true
                    )
                )
            )
            return
        }

        val defaultStart = achievementDefaultStartDate(wpDataDB)
        val defaultEnd = achievementDefaultEndDate(wpDataDB)
        val existingFilters = uiState.value.filters ?: filters
        val existingRange = existingFilters?.rangeDataByKey
            ?.takeIf { it.key.equals(ACHIEVEMENT_DATE_FILTER_KEY, ignoreCase = true) }

        val startDate = existingRange?.start ?: defaultStart
        val endDate = existingRange?.end ?: defaultEnd
        if (rangeDataStart.value != startDate) setStartDate(startDate)
        if (rangeDataEnd.value != endDate) setEndDate(endDate)

        val existingAddressFilter = existingFilters?.items
            ?.firstOrNull { it.leftField.equals(ACHIEVEMENT_ADDRESS_FILTER_KEY, ignoreCase = true) }
        val existingClientFilter = existingFilters?.items
            ?.firstOrNull { it.leftField.equals(ACHIEVEMENT_CLIENT_FILTER_KEY, ignoreCase = true) }

        val addressRaw = wpDataDB.addr_id
            .takeIf { it > 0 }
            ?.toString()
        val clientRaw = wpDataDB.client_id
            ?.takeIf { it.isNotBlank() }

        val addressFilter = ItemFilter(
            "Адреса",
            AddressSDB::class,
            AddressSDBViewModel::class,
            ModeUI.MULTI_SELECT,
            "Адреса",
            "Оберіть адресу",
            ACHIEVEMENT_ADDRESS_FILTER_KEY,
            "id",
            existingAddressFilter?.rightValuesRaw ?: listOfNotNull(addressRaw),
            existingAddressFilter?.rightValuesUI ?: listOfNotNull(
                wpDataDB.addr_txt?.takeIf { it.isNotBlank() } ?: addressRaw
            ),
            enabled = false
        )

        val clientFilter = ItemFilter(
            "Клієнт",
            CustomerSDB::class,
            CustomerSDBViewModel::class,
            ModeUI.MULTI_SELECT,
            "Клієнт",
            "Оберіть клієнта",
            ACHIEVEMENT_CLIENT_FILTER_KEY,
            "id",
            existingClientFilter?.rightValuesRaw ?: listOfNotNull(clientRaw),
            existingClientFilter?.rightValuesUI ?: listOfNotNull(
                wpDataDB.client_txt?.takeIf { it.isNotBlank() } ?: clientRaw
            ),
            enabled = false
        )

        updateFilters(
            Filters(
                searchText = existingFilters?.searchText.orEmpty(),
                items = mutableListOf(addressFilter, clientFilter),
                rangeDataByKey = RangeDate(
                    key = ACHIEVEMENT_DATE_FILTER_KEY,
                    start = startDate,
                    end = endDate,
                    enabled = true
                ),
                selectedMode = existingFilters?.selectedMode ?: SelectedMode.ALL
            )
        )
    }

    override fun onClickAdditionalContent() {
        super.onClickAdditionalContent()
        val dialogCreateAchievement = DialogCreateAchievement(context)
        val wpDataDB = getAchievementWpDataOrNull()
        wpDataDB?.let {
            dialogCreateAchievement.setData(wpDataDB)
            dialogCreateAchievement.setClose { dialogCreateAchievement.dismiss() }
            dialogCreateAchievement.setTitle("Створення нового Досягнення")
            dialogCreateAchievement.buttonPhotoTo()
            dialogCreateAchievement.buttonPhotoAfter()
            dialogCreateAchievement.show()

            val dialogData = DialogData(context)
            dialogData.setTitle("Створення досягнення")
            dialogData.setText(
                "У разі, якщо ви покращили розташування товару клієнта на вітрині, по зрівнянню з попереднім ДОСЯГНЕННЯМ, то введіть поточне Досягнення на підставі попереднього. " +
                        "Для цього, закрийте це віконце та натисніть довгим кліком на ПОПЕРЕДНЬОМУ Досягненні.Це суттєво збільшить шанс на отримання додаткової премії."
            )
            dialogData.setClose { dialogData.dismiss() }
            dialogData.show()
        }
    }

    private fun getAchievementItems(): List<DataItemUI> {
        val wpDataDB = getAchievementWpDataOrNull() ?: return emptyList()

        val dateFrom = rangeDataStart.value
            ?.atStartOfDay(ZoneId.systemDefault())
            ?.toEpochSecond()
            ?: achievementDefaultStartSeconds(wpDataDB)
        val dateTo = rangeDataEnd.value
            ?.atTime(LocalTime.MAX)
            ?.atZone(ZoneId.systemDefault())
            ?.toEpochSecond()
            ?: achievementDefaultEndSeconds(wpDataDB)

        val clientIds = selectedFilterValues(ACHIEVEMENT_CLIENT_FILTER_KEY)
        val addressIds = selectedFilterValues(ACHIEVEMENT_ADDRESS_FILTER_KEY)
            .mapNotNull { it.toIntOrNull() }

        val clientId = clientIds.singleOrNull()
        val addressId = addressIds.singleOrNull()

        val data = RoomManager.SQL_DB.achievementsDao()
            .getAchievementsListByFilters(dateFrom, dateTo, clientId, addressId, null)

        Log.e(
            "AchievementsViewModel",
            "ACHIEVEMENT: dateFrom=$dateFrom, dateTo=$dateTo, clientId=$clientId, addressId=$addressId, data=${data.size}"
        )

        scheduleMissingAchievementPhotos(data)

        return repository.toItemUIList(
            AchievementsSDB::class,
            data,
            contextUI,
            null
        )
            .map {
                val selected = FilteringDialogDataHolder.instance()
                    .filters
                    ?.items
                    ?.firstOrNull { it.clazz == table }
                    ?.rightValuesRaw
                    ?.contains(
                        (it.rawObj.firstOrNull { it is AchievementsSDB } as? AchievementsSDB)
                            ?.id
                            .toString()
                    )
                it.copy(selected = selected == true)
            }

    }

    fun getLoadingImageIndexes(itemUI: DataItemUI, loadingPhotoIds: Set<String>): Set<Int> {
        val achievement = itemUI.rawObj.firstOrNull { it is AchievementsSDB } as? AchievementsSDB
            ?: return emptySet()
        return achievement.loadingPhotoIndexes(loadingPhotoIds)
    }

    private fun scheduleMissingAchievementPhotos(data: List<AchievementsSDB>) {
        val photoIds = data
            .flatMap { achievement ->
                listOfNotNull(
                    achievement.imgBeforeId?.toString().normalizePhotoServerId(),
                    achievement.imgAfterId?.toString().normalizePhotoServerId()
                )
            }
            .distinct()
            .filter { scheduledAchievementPhotoIds.add(it) }

        if (photoIds.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            delay(ACHIEVEMENT_PHOTO_PREFETCH_DELAY_MS)
            for (photoServerId in photoIds) {
                if (!isActive) break
                requestAchievementPhotoIfNeeded(photoServerId)
                delay(ACHIEVEMENT_PHOTO_PREFETCH_STEP_DELAY_MS)
            }
        }
    }

    private fun requestAchievementPhotoIfNeeded(rawPhotoServerId: String?) {
        val photoServerId = rawPhotoServerId.normalizePhotoServerId() ?: return
        if (achievementPhotoDownloadFailures.contains(photoServerId)) return

        val localPhoto = getLocalStackPhotoByServerId(photoServerId)
        if (localPhoto?.hasFullLocalPhotoFile() == true) return
        if (!achievementPhotoDownloadsInProgress.add(photoServerId)) return
        updateAchievementPhotoLoadingState()

        scheduleAchievementPhotoDownloadTimeout(photoServerId)

        if (localPhoto != null) {
            if (localPhoto.getPhotoServerURL().isNullOrBlank()) {
                finishAchievementPhotoDownload(
                    photoServerId = photoServerId,
                    success = false,
                    error = "StackPhotoDB exists without photoServerURL"
                )
            } else {
                downloadAchievementPhotoFile(photoServerId, localPhoto)
            }
        } else {
            downloadAchievementPhotoInfoById(photoServerId)
        }
    }

    private fun downloadAchievementPhotoInfoById(photoServerId: String) {
        val request = PhotoTableRequest().apply {
            mod = "images_view"
            act = "list_image"
            nolimit = "1"
            id_list = photoServerId
        }
        val requestJson = Gson().toJson(request)
        Globals.writeToMLOG(
            "INFO",
            "AchievementsSDBViewModel/downloadAchievementPhotoInfoById/request",
            "photoServerId=$photoServerId, request=$requestJson"
        )

        PhotoDownload().getPhotoInfoAndSaveItToDB(
            request,
            object : Clicks.clickObjectAndStatus<StackPhotoDB> {
                override fun onSuccess(data: StackPhotoDB) {
                    Globals.writeToMLOG(
                        "INFO",
                        "AchievementsSDBViewModel/downloadAchievementPhotoInfoById/response",
                        "photoServerId=$photoServerId, stackPhoto=${Gson().toJson(data)}"
                    )
                    if (data.getPhotoServerURL().isNullOrBlank()) {
                        finishAchievementPhotoDownload(
                            photoServerId = photoServerId,
                            success = false,
                            error = "images_view.list_image returned empty photoServerURL"
                        )
                    } else {
                        downloadAchievementPhotoFile(photoServerId, data)
                    }
                }

                override fun onFailure(error: String) {
                    Globals.writeToMLOG(
                        "ERROR",
                        "AchievementsSDBViewModel/downloadAchievementPhotoInfoById/response",
                        "photoServerId=$photoServerId, request=$requestJson, error=$error"
                    )
                    finishAchievementPhotoDownload(photoServerId, success = false, error = error)
                }
            }
        )
    }

    private fun downloadAchievementPhotoFile(photoServerId: String, stackPhotoDB: StackPhotoDB) {
        PhotoDownload().downloadPhoto(
            true,
            stackPhotoDB,
            ACHIEVEMENT_PHOTO_FOLDER,
            object : PhotoDownload.downloadPhotoInterface {
                override fun onSuccess(data: StackPhotoDB) {
                    finishAchievementPhotoDownload(photoServerId, success = true)
                }

                override fun onFailure(s: String) {
                    finishAchievementPhotoDownload(photoServerId, success = false, error = s)
                }
            }
        )
    }

    private fun finishAchievementPhotoDownload(
        photoServerId: String,
        success: Boolean,
        error: String? = null
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            achievementPhotoDownloadsInProgress.remove(photoServerId)
            if (success) {
                achievementPhotoDownloadFailures.remove(photoServerId)
            } else {
                achievementPhotoDownloadFailures.add(photoServerId)
                Globals.writeToMLOG(
                    "ERROR",
                    "AchievementsSDBViewModel/downloadAchievementPhoto",
                    "photoServerId=$photoServerId, error=$error"
                )
            }
            updateAchievementPhotoLoadingState()
            if (success) updateContent()
        }
    }

    private fun scheduleAchievementPhotoDownloadTimeout(photoServerId: String) {
        viewModelScope.launch {
            delay(ACHIEVEMENT_PHOTO_DOWNLOAD_TIMEOUT_MS)
            if (achievementPhotoDownloadsInProgress.remove(photoServerId)) {
                achievementPhotoDownloadFailures.add(photoServerId)
                updateAchievementPhotoLoadingState()
                Globals.writeToMLOG(
                    "ERROR",
                    "AchievementsSDBViewModel/downloadAchievementPhoto",
                    "photoServerId=$photoServerId, error=timeout"
                )
            }
        }
    }

    private fun updateAchievementPhotoLoadingState() {
        _achievementPhotoLoadingIds.value = achievementPhotoDownloadsInProgress.toSet()
    }

    private fun getLocalStackPhotoByServerId(photoServerId: String): StackPhotoDB? {
        return runCatching {
            val realm = Realm.getDefaultInstance()
            try {
                realm.where(StackPhotoDB::class.java)
                    .equalTo("photoServerId", photoServerId)
                    .findFirst()
                    ?.let { realm.copyFromRealm(it) }
            } finally {
                realm.close()
            }
        }.onFailure { error ->
            Globals.writeToMLOG(
                "ERROR",
                "AchievementsSDBViewModel/getLocalStackPhotoByServerId",
                "photoServerId=$photoServerId, error=$error"
            )
        }.getOrNull()
    }

    private fun AchievementsSDB.loadingPhotoIndexes(loadingPhotoIds: Set<String>): Set<Int> {
        val indexes = mutableSetOf<Int>()
        imgBeforeId?.toString().normalizePhotoServerId()
            ?.takeIf { it in loadingPhotoIds }
            ?.let { indexes.add(0) }
        imgAfterId?.toString().normalizePhotoServerId()
            ?.takeIf { it in loadingPhotoIds }
            ?.let { indexes.add(1) }
        return indexes
    }

    private fun StackPhotoDB.hasFullLocalPhotoFile(): Boolean {
        val photoPath = getPhoto_num()
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it != "0" && !it.equals("null", ignoreCase = true) }
            ?: return false

        val isFullPhoto = getPhoto_size()?.equals("Full", ignoreCase = true) == true ||
                photoPath.contains("_Full", ignoreCase = true)

        return isFullPhoto && File(photoPath).exists()
    }

    private fun String?.normalizePhotoServerId(): String? {
        return this
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it != "0" && !it.equals("null", ignoreCase = true) }
    }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        val data = getAchievementWpDataOrNull()
        val achievement = itemUI.rawObj.firstOrNull { it is AchievementsSDB } as? AchievementsSDB
        if (achievement != null) {

            val stackPhotoDB = RealmManager.getPhotoByPhotoId(achievement.imgAfterId.toString())
            if (stackPhotoDB != null) {
                data?.let {

                    val dialogCreateAchievement = DialogCreateAchievement(context)
                    dialogCreateAchievement.setData(it)
                    dialogCreateAchievement.setClose { dialogCreateAchievement.dismiss() }
                    dialogCreateAchievement.setTitle("Створення Досягнення на основі створеного")
                    dialogCreateAchievement.setPhotoDo(stackPhotoDB)
                    dialogCreateAchievement.buttonPhotoAfter()
                    dialogCreateAchievement.show()
                }
            }
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        FilteringDialogDataHolder.instance().filters.apply {
            this?.let { filters ->
                filters.items = filters.items.map { itemFilter ->
                    if (itemFilter.clazz == table) {
                        val rightValuesRaw = mutableListOf<String>()
                        val rightValuesUI = mutableListOf<String>()
                        itemsUI.forEach {
                            (it.rawObj.firstOrNull() as? AchievementsSDB)?.let { achievement ->
                                achievement.id?.toString()?.let { id ->
                                    rightValuesRaw.add(id)
                                    rightValuesUI.add(
                                        achievement.commentTxt
                                            ?.takeIf { comment -> comment.isNotBlank() }
                                            ?: id
                                    )
                                }
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

    private fun getAchievementWpDataOrNull(): WpDataDB? {
        val codeDad2 = getCodeDad2FromDataJson() ?: return null
        return runCatching {
            RealmManager.INSTANCE.copyFromRealm(
                RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
            )
        }.onFailure { error ->
            Globals.writeToMLOG(
                "ERROR",
                "AchievementsSDBViewModel/getAchievementWpDataOrNull",
                "codeDad2=$codeDad2, error=$error"
            )
        }.getOrNull()
    }

    private fun getCodeDad2FromDataJson(): Long? {
        val raw = dataJson?.takeIf { it.isNotBlank() } ?: return null
        return runCatching {
            val element = JsonParser.parseString(raw)
            when {
                element.isJsonPrimitive -> element.asLongOrNull()
                element.isJsonObject -> element.asJsonObject.codeDad2OrNull()
                else -> null
            }
        }.getOrNull()?.takeIf { it > 0L }
    }

    private fun JsonObject.codeDad2OrNull(): Long? {
        return longOrNull("wpDataDBId")
            ?: longOrNull("codeDad2")
            ?: longOrNull("code_dad2")
            ?: get("nameValuePairs")
                ?.takeIf { it.isJsonObject }
                ?.asJsonObject
                ?.let { nested ->
                    nested.longOrNull("wpDataDBId")
                        ?: nested.longOrNull("codeDad2")
                        ?: nested.longOrNull("code_dad2")
                }
    }

    private fun JsonObject.longOrNull(key: String): Long? {
        return get(key)
            ?.takeIf { !it.isJsonNull }
            ?.asLongOrNull()
    }

    private fun JsonElement.asLongOrNull(): Long? {
        return runCatching {
            when {
                isJsonPrimitive -> asString.toLongOrNull()
                else -> null
            }
        }.getOrNull()
    }

    private fun selectedFilterValues(leftField: String): List<String> {
        val currentFilters = uiState.value.filters ?: filters
        return currentFilters
            ?.items
            ?.firstOrNull { it.leftField.equals(leftField, ignoreCase = true) }
            ?.rightValuesRaw
            ?.mapNotNull { it?.takeIf { value -> value.isNotBlank() } }
            .orEmpty()
    }

    private fun achievementDefaultStartSeconds(wpDataDB: WpDataDB): Long {
        return Clock.getDatePeriodLong(visitMillis(wpDataDB), -41) / 1000
    }

    private fun achievementDefaultEndSeconds(wpDataDB: WpDataDB): Long {
        return Clock.getDatePeriodLong(visitMillis(wpDataDB), 3) / 1000
    }

    private fun achievementDefaultStartDate(wpDataDB: WpDataDB): LocalDate {
        return achievementDefaultStartSeconds(wpDataDB).toLocalDateFromSeconds()
    }

    private fun achievementDefaultEndDate(wpDataDB: WpDataDB): LocalDate {
        return achievementDefaultEndSeconds(wpDataDB).toLocalDateFromSeconds()
    }

    private fun Long.toLocalDateFromSeconds(): LocalDate {
        return Instant.ofEpochSecond(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    private fun visitMillis(wpDataDB: WpDataDB): Long {
        return wpDataDB.dt?.time ?: System.currentTimeMillis()
    }

    companion object {
        private const val ACHIEVEMENT_DATE_FILTER_KEY = "dt_ut"
        private const val ACHIEVEMENT_ADDRESS_FILTER_KEY = "addr_id"
        private const val ACHIEVEMENT_CLIENT_FILTER_KEY = "client_id"
        private const val ACHIEVEMENT_PHOTO_FOLDER = "/Achievements"
        private const val ACHIEVEMENT_PHOTO_DOWNLOAD_TIMEOUT_MS = 45_000L
        private const val ACHIEVEMENT_PHOTO_PREFETCH_DELAY_MS = 350L
        private const val ACHIEVEMENT_PHOTO_PREFETCH_STEP_DELAY_MS = 80L
    }
}
