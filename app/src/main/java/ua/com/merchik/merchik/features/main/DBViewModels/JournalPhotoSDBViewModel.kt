package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLog
import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogAdapter
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TarPhotoDataHolder
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface.UploadPhotoReports
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.PhotoTypeRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR
import ua.com.merchik.merchik.dialogs.features.InfoDialogBuilder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.RangeDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class JournalPhotoSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {


    override val table: KClass<out DataObjectUI>
        get() = StackPhotoDB::class

    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context) {
        super.onClickItemImage(clickedDataItemUI, context)
    }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        super.onClickItem(itemUI, context)

        val obj = itemUI.rawObj.firstOrNull() ?: return
        val imageFields = obj.getFieldsImageOnUI()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val jsonObject = JSONObject(Gson().toJson(obj))

        fun optStr(key: String): String =
            jsonObject.opt(key)?.toString()?.trim().orEmpty()

        // 1) Пытаемся найти по serverId (или любому первому ключу из getFieldsImageOnUI)
        val idKey = imageFields.getOrNull(0)
        val photoId = idKey?.let { optStr(it) }.orEmpty()

        var photoLogData: StackPhotoDB? = null

        if (photoId.isNotEmpty() && photoId != "0" && photoId != "null") {
            // как у тебя, только безопаснее
            photoLogData = RealmManager.getPhotoById(null, photoId)
        }

        // 2) Если не найдено — пробуем по hash (твой кейс: photo_hash всегда есть)
        if (photoLogData == null) {
            val hash = optStr("photo_hash") // <-- ключ хэша
            if (hash.length > 12 && hash != "0" && hash != "null") {
                photoLogData = RealmManager.getPhotoByHash(hash)
            }
        }

        InfoDialogBuilder(context)
            .setTitle("Детальна інформація")
            .setMessage(PhotoLogAdapter.photoData(photoLogData))
            .show()
    }

    override fun onClickFullImage(stackPhotoDB: StackPhotoDB, comment: String?) {
        try {
            val dialogFullPhoto = DialogFullPhotoR(context)
            dialogFullPhoto.setPhoto(stackPhotoDB)

            // Pika
            comment?.let { dialogFullPhoto.setComment(it) }


            dialogFullPhoto.setClose { dialogFullPhoto.dismiss() }
            dialogFullPhoto.show()
        } catch (e: Exception) {
            Log.e("ShowcaseAdapter", "Exception e: $e")
        }
    }

    override fun updateFilters() {
        try {
            val zirParams = getZirFilterParamsOrNull()

            val typePhoto = if (zirParams != null) {
                RealmManager.INSTANCE.copyFromRealm(PhotoTypeRealm.getPhotoType())
                    .filter { it.id.toString().toIntOrNull() in ZIR_PHOTO_TYPES }
            } else {
                RealmManager.INSTANCE.copyFromRealm(PhotoTypeRealm.getPhotoType())
            }

            val clients = if (zirParams != null) {
                val client = RoomManager.SQL_DB.customerDao()
                    .getById(zirParams.clientId)

                if (client != null) listOf(client) else emptyList()
            } else {
                RoomManager.SQL_DB.customerDao().all
            }

            val addresses = if (zirParams != null) {
                val address = RoomManager.SQL_DB.addressDao()
                    .getById(zirParams.addrId.toInt())

                if (address != null) listOf(address) else emptyList()
            } else {
                RoomManager.SQL_DB.addressDao().all
            }

            val typePhotoFilter = ItemFilter(
                "Тип фото",
                ImagesTypeListDB::class,
                ImagesTypeListDBViewModel::class,
                ModeUI.MULTI_SELECT,
                "title",
                "subTitle",
                "photo_type",
                "id",
                typePhoto.map { it.id.toString() },
                typePhoto.map { it.nm },
                true
            )

            val clientFilter = ItemFilter(
                "Клієнт",
                CustomerSDB::class,
                CustomerSDBViewModel::class,
                ModeUI.MULTI_SELECT,
                "Додати Клієнта",
                "## sub title",
                "client_id",
                "id",
                clients.map { it.id },
                clients.map { it.nm },
                enabled = true
            )

            val adressFilter = ItemFilter(
                "Адреса",
                AddressSDB::class,
                AddressSDBViewModel::class,
                ModeUI.MULTI_SELECT,
                "## ADRESS",
                "## sub title",
                "addr_id",
                "id",
                addresses.map { it.id.toString() },
                addresses.map { it.nm },
                enabled = true
            )

            val users = RoomManager.SQL_DB.usersDao().all2

            val userFilter = ItemFilter(
                "Виконавець",
                UsersSDB::class,
                UsersSDBViewModel::class,
                ModeUI.MULTI_SELECT,
                "## USER",
                "## sub title",
                "user_id",
                "id",
                users.map { it.id.toString() },
                users.map { it.fio },
                contextUI == ContextUI.WP_DATA_IN_CONTAINER
            )

            filters = Filters(
                searchText = "",
                items = mutableListOf(
                    typePhotoFilter,
                    clientFilter,
                    adressFilter,
                    userFilter
                ),
                rangeDataByKey = RangeDate(
                    key = "----",
                    start = rangeDataStart.value,
                    end = rangeDataEnd.value,
                    enabled = true
                )
            )
        } catch (e: Exception) {
            Log.e("StackPhotoViewModel", "updateFilters error: ${e.message}", e)
        }
    }

    override fun getDefaultHideUserFields(): List<String> {
        return ("approve, code_dad2, dviUpload, errorTime, dvi, upload_status, " +
                "get_on_server, id, markUpload, object_id, photo_hash, photo_num, " +
                "premiyaUpload, specialCol, comment, commentUpload, upload_time, upload_to_server, vpi, " +
                "dt, photoServerURL, showcase_id, time_event, tovar_id, photo_typeTxt, code_iza, " +
                "example_id, example_img_id, planogram_id, planogram_img_id, gp, premiya, photoServerId," +
                "img_src_id").split(",")
    }

    override fun getDefaultSortUserFields(): List<String>? {
        return "dt".split(",")
    }

    override suspend fun getItems(): List<DataItemUI> {
        val startMillis: Long = rangeDataStart.value
            ?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
            ?: Long.MIN_VALUE

        val endMillis: Long = rangeDataEnd.value
            ?.atTime(LocalTime.MAX)
            ?.atZone(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
            ?: Long.MAX_VALUE

        Log.e("%%%%%%%%%%%%%%%%%%", "start: $startMillis | end: $endMillis")

        val data = RealmManager.INSTANCE.copyFromRealm(
            RealmManager.getStackPhotoWithDate(
                startMillis / 1000, endMillis / 1000
            )
        )
        return repository.toItemUIList(SamplePhotoSDB::class, data, contextUI, null)
    }


    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        when (contextUI) {
            ContextUI.ADD_STACK_PHOTO_TO_ZIR -> {
                (itemsUI.first().rawObj.firstOrNull { it is StackPhotoDB } as? StackPhotoDB)?.let {
                    TarPhotoDataHolder.instance().photoToId = it.id
                }
            }
            else -> {}
        }
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

    private data class ZirFilterParams(
        val addrId: String,
        val clientId: String
    )

    private fun getZirFilterParamsOrNull(): ZirFilterParams? {
        if (contextUI != ContextUI.ADD_STACK_PHOTO_TO_ZIR) return null

        val jsonString = dataJson
            .takeIf { it!!.isNotBlank() }
            ?: return null

        Log.e("ZIR_FILTER", "dataJson=$jsonString")

        val json = runCatching {
            JSONObject(jsonString)
        }.getOrNull() ?: return null

        val addrId = json.optString("addrId")
            .takeIf { it.isNotBlank() }
            ?: return null

        val clientId = json.optString("clientId")
            .takeIf { it.isNotBlank() }
            ?: return null

        return ZirFilterParams(
            addrId = addrId,
            clientId = clientId
        )
    }

    private val ZIR_PHOTO_TYPES = listOf(
        4,
        0,
        10,
        14,
        28,
        31,
        36,
        39,
        40,
        41,
        42,
        45,
        46,
        47,
        48
    )
}