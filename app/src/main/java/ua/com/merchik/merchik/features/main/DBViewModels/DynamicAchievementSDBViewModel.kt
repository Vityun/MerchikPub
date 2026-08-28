package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.ServerExchange.PhotoDownload
import ua.com.merchik.merchik.ServerExchange.TablesExchange.DynamicAchievementsExchange
import ua.com.merchik.merchik.ViewHolders.Clicks
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.DynamicAchievementSDB
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
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class DynamicAchievementSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = DynamicAchievementSDB::class

    private val preloadedVisitKeys = ConcurrentHashMap.newKeySet<String>()
    private val scheduledRackPhotoIds = ConcurrentHashMap.newKeySet<String>()
    private val rackPhotoDownloadsInProgress = ConcurrentHashMap.newKeySet<String>()
    private val rackPhotoDownloadFailures = ConcurrentHashMap.newKeySet<String>()

    override fun getDefaultSortUserFields(): List<String> = DYNAMIC_ACHIEVEMENT_VISIBLE_FIELDS

    override fun getDefaultHideUserFields(): List<String> = listOf(
        "ID",
        "id_res_image",
        "select_id",
        "select_name",
        "rack_photo_id",
        "rack_photo",
        "rack_photo_big",
        "client_id",
        "client_nm",
        "isp",
        "isp_nm",
        "addr_id",
        "addr_tp",
        "addr_city",
        "addr_addr",
        "addr_nomer_tt",
        "rack_id",
        "rack_form_nm",
        "tovar_grp_id",
        "tovar_grp_nm",
        "date_from",
        "theme_id",
        "planogram_id",
        "planogram_nm",
        "manufacturer_id",
        "manufacturer_nm",
        "tovar_id",
        "ative",
        "active",
        "dvi",
        "about",
        "author_id",
        "author_fio",
        "achieve_photo_count__title"
    )

    override fun updateFilters() {
        if (contextUI != ContextUI.DYNAMIC_ACHIEVEMENT) {
            super.updateFilters()
            return
        }

        val wpDataDB = getDynamicAchievementWpDataOrNull()
        if (wpDataDB == null) {
            Globals.writeToMLOG(
                "ERROR",
                "DynamicAchievementSDBViewModel/updateFilters",
                "wpDataDB is null, dataJson=$dataJson"
            )
            updateFilters(Filters(searchText = "", items = emptyList()))
            return
        }

        val existingFilters = uiState.value.filters ?: filters
        val existingAddressFilter = existingFilters?.items
            ?.firstOrNull { it.leftField.equals(DYNAMIC_ACHIEVEMENT_ADDRESS_FILTER_KEY, ignoreCase = true) }
        val existingClientFilter = existingFilters?.items
            ?.firstOrNull { it.leftField.equals(DYNAMIC_ACHIEVEMENT_CLIENT_FILTER_KEY, ignoreCase = true) }

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
            DYNAMIC_ACHIEVEMENT_ADDRESS_FILTER_KEY,
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
            DYNAMIC_ACHIEVEMENT_CLIENT_FILTER_KEY,
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
                items = mutableListOf(
//                    addressFilter,
                    clientFilter),
                selectedMode = existingFilters?.selectedMode ?: SelectedMode.ALL
            )
        )
    }

    override suspend fun getItems(): List<DataItemUI> {
        val wpDataDB = getDynamicAchievementWpDataOrNull()
        val clientId = selectedFilterValues(DYNAMIC_ACHIEVEMENT_CLIENT_FILTER_KEY)
            .singleOrNull()
            ?: wpDataDB?.client_id?.takeIf { it.isNotBlank() }
        val addressId = selectedFilterValues(DYNAMIC_ACHIEVEMENT_ADDRESS_FILTER_KEY)
            .singleOrNull()
            ?: wpDataDB?.addr_id?.takeIf { it > 0 }?.toString()

//        val data = withContext(Dispatchers.IO) {
//            if (contextUI == ContextUI.DYNAMIC_ACHIEVEMENT && wpDataDB != null) {
//                preloadDynamicAchievementsForVisitIfNeeded(clientId, addressId)
//            }
//
//            if (!clientId.isNullOrBlank() || !addressId.isNullOrBlank()) {
//                RoomManager.SQL_DB.dynamicAchievementsDao()
//                    .getByClientAndAddress(clientId, addressId)
//            } else {
//                RoomManager.SQL_DB.dynamicAchievementsDao().all
//            }
//        }

        val data = RoomManager.SQL_DB.dynamicAchievementsDao().all
        Log.e(
            "DynamicAchievementVM",
            "items: contextUI=$contextUI, clientId=$clientId, addressId=$addressId, data=${data.size}"
        )

        scheduleMissingRackPhotos(data)

        return repository.toItemUIList(
            DynamicAchievementSDB::class,
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
                        (it.rawObj.firstOrNull { raw -> raw is DynamicAchievementSDB } as? DynamicAchievementSDB)
                            ?.id
                            .toString()
                    )
                it.copy(selected = selected == true)
            }
    }

    private fun preloadDynamicAchievementsForVisitIfNeeded(clientId: String?, addressId: String?) {
        if (clientId.isNullOrBlank() || addressId.isNullOrBlank()) return

        val key = "$clientId|$addressId"
        if (!preloadedVisitKeys.add(key)) return

        runCatching {
            DynamicAchievementsExchange().downloadDynamicAchievementsForVisitSync(clientId, addressId)
        }.onSuccess { savedRows ->
            Globals.writeToMLOG(
                "INFO",
                "DynamicAchievementSDBViewModel/preloadDynamicAchievementsForVisit",
                "clientId=$clientId, addressId=$addressId, savedRows=$savedRows"
            )
        }.onFailure { error ->
            preloadedVisitKeys.remove(key)
            Globals.writeToMLOG(
                "ERROR",
                "DynamicAchievementSDBViewModel/preloadDynamicAchievementsForVisit",
                "clientId=$clientId, addressId=$addressId, error=$error"
            )
        }
    }

    private fun scheduleMissingRackPhotos(data: List<DynamicAchievementSDB>) {
        val photoIds = data
            .mapNotNull { item -> item.rackPhotoId.normalizePhotoServerId() }
            .distinct()
            .filter { scheduledRackPhotoIds.add(it) }

        if (photoIds.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            delay(DYNAMIC_ACHIEVEMENT_PHOTO_PREFETCH_DELAY_MS)
            for (photoServerId in photoIds) {
                if (!isActive) break
                requestRackPhotoIfNeeded(photoServerId)
                delay(DYNAMIC_ACHIEVEMENT_PHOTO_PREFETCH_STEP_DELAY_MS)
            }
        }
    }

    private fun requestRackPhotoIfNeeded(rawPhotoServerId: String?) {
        val photoServerId = rawPhotoServerId.normalizePhotoServerId() ?: return
        if (rackPhotoDownloadFailures.contains(photoServerId)) return

        val localPhoto = getLocalStackPhotoByServerId(photoServerId)
        if (localPhoto?.hasFullLocalPhotoFile() == true) return
        if (!rackPhotoDownloadsInProgress.add(photoServerId)) return

        scheduleRackPhotoDownloadTimeout(photoServerId)

        if (localPhoto != null && !localPhoto.getPhotoServerURL().isNullOrBlank()) {
            downloadRackPhotoFile(photoServerId, localPhoto)
        } else {
            downloadRackPhotoInfoById(photoServerId)
        }
    }

    private fun downloadRackPhotoInfoById(photoServerId: String) {
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
                    if (data.getPhotoServerURL().isNullOrBlank()) {
                        finishRackPhotoDownload(
                            photoServerId = photoServerId,
                            success = false,
                            error = "images_view.list_image returned empty photoServerURL"
                        )
                    } else {
                        downloadRackPhotoFile(photoServerId, data)
                    }
                }

                override fun onFailure(error: String) {
                    finishRackPhotoDownload(photoServerId, success = false, error = error)
                }
            }
        )
    }

    private fun downloadRackPhotoFile(photoServerId: String, stackPhotoDB: StackPhotoDB) {
        PhotoDownload().downloadPhoto(
            true,
            stackPhotoDB,
            DYNAMIC_ACHIEVEMENT_PHOTO_FOLDER,
            object : PhotoDownload.downloadPhotoInterface {
                override fun onSuccess(data: StackPhotoDB) {
                    finishRackPhotoDownload(photoServerId, success = true)
                }

                override fun onFailure(s: String) {
                    finishRackPhotoDownload(photoServerId, success = false, error = s)
                }
            }
        )
    }

    private fun finishRackPhotoDownload(
        photoServerId: String,
        success: Boolean,
        error: String? = null
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            rackPhotoDownloadsInProgress.remove(photoServerId)
            if (success) {
                rackPhotoDownloadFailures.remove(photoServerId)
                updateContent()
            } else {
                rackPhotoDownloadFailures.add(photoServerId)
                Globals.writeToMLOG(
                    "ERROR",
                    "DynamicAchievementSDBViewModel/downloadRackPhoto",
                    "photoServerId=$photoServerId, error=$error"
                )
            }
        }
    }

    private fun scheduleRackPhotoDownloadTimeout(photoServerId: String) {
        viewModelScope.launch {
            delay(DYNAMIC_ACHIEVEMENT_PHOTO_DOWNLOAD_TIMEOUT_MS)
            if (rackPhotoDownloadsInProgress.remove(photoServerId)) {
                rackPhotoDownloadFailures.add(photoServerId)
                Globals.writeToMLOG(
                    "ERROR",
                    "DynamicAchievementSDBViewModel/downloadRackPhoto",
                    "photoServerId=$photoServerId, error=timeout"
                )
            }
        }
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
                "DynamicAchievementSDBViewModel/getLocalStackPhotoByServerId",
                "photoServerId=$photoServerId, error=$error"
            )
        }.getOrNull()
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

    private fun getDynamicAchievementWpDataOrNull(): WpDataDB? {
        val codeDad2 = getCodeDad2FromDataJson() ?: return null
        return runCatching {
            val wpData = RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                ?: return@runCatching null

            RealmManager.INSTANCE.copyFromRealm(wpData)
        }.onFailure { error ->
            Globals.writeToMLOG(
                "ERROR",
                "DynamicAchievementSDBViewModel/getDynamicAchievementWpDataOrNull",
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

    companion object {
        private val DYNAMIC_ACHIEVEMENT_VISIBLE_FIELDS = listOf(
            "rack_nm",
            "nm",
            "achieve_photo_count",
            "theme_nm",
            "tovar_nm",
            "date_to",
            "dt_update"
        )
        private const val DYNAMIC_ACHIEVEMENT_ADDRESS_FILTER_KEY = "addr_id"
        private const val DYNAMIC_ACHIEVEMENT_CLIENT_FILTER_KEY = "client_id"
        private const val DYNAMIC_ACHIEVEMENT_PHOTO_FOLDER = "/DynamicAchievements"
        private const val DYNAMIC_ACHIEVEMENT_PHOTO_DOWNLOAD_TIMEOUT_MS = 45_000L
        private const val DYNAMIC_ACHIEVEMENT_PHOTO_PREFETCH_DELAY_MS = 350L
        private const val DYNAMIC_ACHIEVEMENT_PHOTO_PREFETCH_STEP_DELAY_MS = 80L
    }
}
