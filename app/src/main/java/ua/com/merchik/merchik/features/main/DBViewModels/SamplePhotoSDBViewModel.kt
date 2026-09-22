package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites
import ua.com.merchik.merchik.MakePhoto.MakePhoto
import ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery
import ua.com.merchik.merchik.MakePhoto.ProductPhotoCapture
import ua.com.merchik.merchik.Utils.PhotoPickerUtils
import ua.com.merchik.merchik.WorkPlan
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.data.WPDataObj
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.ImageDisplayMode
import ua.com.merchik.merchik.dataLayer.model.rawAs
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm
import ua.com.merchik.merchik.database.realm.tables.PhotoTypeRealm
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm
import ua.com.merchik.merchik.database.realm.tables.TovarRealm
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.SamplePhotoPreview
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.SettingsUI
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class SamplePhotoSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    private val EXAMPLE_ID = "id_1c"
    private val EXAMPLE_IMG_ID = "photo_id"
    private val isProductCapture: Boolean
        get() = contextUI == ContextUI.SAMPLE_PHOTO_FOR_PRODUCT ||
            (contextUI == ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158 &&
                (dataJsonString("tovarId")?.toLongOrNull() ?: 0L) > 0L)
    private val isProductGallery: Boolean
        get() = contextUI == ContextUI.SAMPLE_PHOTO_FOR_PRODUCT_GALLERY
    private var initialDisplayModeApplied = false
    private val warehouseAvailabilityKey = "warehouse_product_available"
    private val warehouseExceptionSampleIds = listOf(78, 94)
    private val warehouseNoStockExcludedSampleIds = listOf(34, 36, 39, 41, 42, 75, 80, 83, 84, 87)
    val warehouseProductAvailable = savedStateHandle.getStateFlow<Boolean?>(warehouseAvailabilityKey, null)

    init {
        viewModelScope.launch {
            dataItems
                .map { items -> items.mapNotNull { it.rawAs<SamplePhotoSDB>()?.id } }
                .distinctUntilChanged()
                .collect { ids ->
                    if (uiState.value.lastUpdate != 0L) {
                        Log.e(
                            "SamplePhotoFilter",
                            "displayedSamples: contextUI=$contextUI, warehouseProductAvailable=${warehouseProductAvailable.value}, " +
                                "count=${ids.size}, ids=$ids"
                        )
                    }
                }
        }
    }

    override val table: KClass<out DataObjectUI>
        get() = SamplePhotoSDB::class

    override fun updateContent() {
        modeUI = ModeUI.ONE_SELECT
        if (!initialDisplayModeApplied) {
            val settings = repository.getSettingsUI(table.java, contextUI, settingsVisitId)
                ?: SettingsUI(hideFields = getDefaultHideUserFields())
            repository.saveSettingsUI(
                table,
                settings.copy(imageDisplayMode = ImageDisplayMode.TWO_COLUMNS),
                contextUI,
                settingsVisitId
            )
            initialDisplayModeApplied = true
        }
        super.updateContent()
    }

    override fun getFieldsForCommentsImage(): List<String>? {
        return "nm, about".split(",").map { it.trim() }
    }

    override fun getFieldsForCustomResult(): List<String>? {
        return "$EXAMPLE_ID, $EXAMPLE_IMG_ID".split(",").map { it.trim() }
    }

    override fun updateFilters() {

        try {

            val typePhotoId = resolvePhotoTypeId()

            val itemsFilter = mutableListOf<ItemFilter>()

            typePhotoId?.let {
                val imagesType = PhotoTypeRealm.getPhotoTypeById(it)

                val imagesTypeId = imagesType?.id ?: it
                val imagesTypeName = imagesType?.nm ?: "Тип фото $it"
                val filterImagesTypeListDB = ItemFilter(
                    "Тип фото",
                    ImagesTypeListDB::class,
                    ImagesTypeListDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "title",
                    "subTitle",
                    "photo_tp",
                    "id",
                    mutableListOf(imagesTypeId.toString()),
                    mutableListOf(imagesTypeName),
                    !isProductCapture && !isProductGallery
                )
                itemsFilter.add(filterImagesTypeListDB)
            }

            try {
//                AddressSDB addr = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
//                TradeMarkDB tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(String.valueOf(addr.tpId));
//                groupText.setText(tradeMarkDB.getNm());

                val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
                val tradeMarkId = dataJsonObject.get("tradeMarkDBId").asString
                val tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(tradeMarkId.toString())

                val isProductPhoto = isProductCapture || isProductGallery
                val tradeMarkIds = listOf(tradeMarkId, "0").distinct()

                val filterTradeMarkDB = ItemFilter(
                    "Мережа",
                    TradeMarkDB::class,
                    TradeMarkDBViewModel::class,
                    ModeUI.MULTI_SELECT,
                    "Мережа",
                    "subTitle",
                    "grp_id",
                    "iD",
                    if (isProductPhoto) tradeMarkIds
                    else mutableListOf(tradeMarkDB.id.toString(), "0"),
                    if (isProductPhoto) tradeMarkIds.map {
                        if (it == "0") "Все не указанные" else tradeMarkDB?.nm ?: "Мережа $it"
                    } else mutableListOf(tradeMarkDB.nm, "Все не указанные"),
                    !isProductPhoto
                )
                itemsFilter.add(filterTradeMarkDB)
            } catch (e: Exception) {
            }

            buildWarehouseSampleFilter()?.let { itemsFilter.add(it) }

            filters = Filters(
                rangeDataByKey = null,
                searchText = "",
                items = itemsFilter
            )
        } catch (e: Exception) {
            Log.e("!", "error: ${e.message}")
        }
    }

    override fun getDefaultHideUserFields(): List<String>? {
        return "abbr, grp_id, ID, photo_id, photo_tp, column_name, showcaseName, showcaseId, statusShowcase, mainOption".split(",")
    }

    override suspend fun getItems(): List<DataItemUI> {
        if (isProductCapture || isProductGallery) {
            val photoType = resolvePhotoTypeId() ?: return emptyList()
            val tradeMarkId = dataJsonInt("tradeMarkDBId") ?: 0
            val samples = withContext(Dispatchers.IO) {
                ProductPhotoCapture.getSamples(photoType, tradeMarkId)
            }
            return repository.toItemUIList(SamplePhotoSDB::class, samples, contextUI, 35)
        }
        val data = RoomManager.SQL_DB.samplePhotoDao().getPhotoLogActive(1)
        // Оновлюємо назви виключених зразків з актуального набору для відображення.
        buildWarehouseSampleFilter(data)?.let { warehouseFilter ->
            val currentFilters = filters ?: Filters()
            filters = currentFilters.copy(
                items = currentFilters.items.filterNot { it.key == warehouseFilter.key } + warehouseFilter
            )
        }
        val typePhoto = 35
        return repository.toItemUIList(SamplePhotoSDB::class, data, contextUI, typePhoto)
    }

    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context) {
        onClickItemImage(clickedDataItemUI, context, 0)
    }

    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context, index: Int) {
        val sample = clickedDataItemUI.rawAs<SamplePhotoSDB>() ?: return
        if (isProductCapture || isProductGallery) {
            this.context = context
            try {
                SamplePhotoPreview.showSample(context, sample, galleryAction = isProductGallery) { onStarted ->
                    if (isProductGallery) openProductGallery(onStarted)
                    else takeProductPhoto(sample, onStarted)
                }
            } catch (e: Exception) {
                Log.e("SamplePhotoSDBViewModel", "Cannot open product sample ${sample.id}", e)
                Toast.makeText(context, "Не вдалося відкрити зразок фото", Toast.LENGTH_SHORT).show()
            }
            return
        }
        val photo = prepareSamplePhoto(sample, context, index) ?: return
        val comment = listOfNotNull(sample.nm, sample.about)
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
        onClickFullImage(photo, comment, sample.id)
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        val photoContext = context ?: return
        val sample = itemsUI.singleOrNull()?.rawAs<SamplePhotoSDB>()
        if (sample == null) {
            Toast.makeText(photoContext, "Оберіть один зразок фото", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            if (isProductGallery) {
                openProductGallery {}
                return
            }
            if (isProductCapture) {
                takeProductPhoto(sample) {}
                return
            }
            val photo = prepareSamplePhoto(sample, photoContext, 0) ?: return
            openCamera(photo) {}
        } catch (e: Exception) {
            Log.e("SamplePhotoSDBViewModel", "Cannot take photo for sample ${sample.id}", e)
            Toast.makeText(photoContext, "Не вдалося відкрити камеру", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onClickFullImage(stackPhotoDB: StackPhotoDB, comment: String?) {
        onClickFullImage(stackPhotoDB, comment, null)
    }

    private fun onClickFullImage(stackPhotoDB: StackPhotoDB, comment: String?, sampleId: Int?) {
        val photoContext = context ?: return
        try {
            SamplePhotoPreview.show(
                photoContext, stackPhotoDB, comment, sampleId,
                galleryAction = isProductGallery
            ) { onStarted ->
                if (isProductGallery) openProductGallery(onStarted)
                else openCamera(stackPhotoDB, onStarted)
            }
        } catch (e: Exception) {
            Log.e("SamplePhotoSDBViewModel", "Cannot open sample photo", e)
        }
    }

    private fun openProductGallery(onStarted: () -> Unit) {
        val activity = context as? Activity ?: return
        if (activity.isFinishing || activity.isDestroyed) return
        try {
            val visitId = dataJsonString("wpDataDBId")?.toLongOrNull()
                ?: error("Visit ID is missing")
            val visit = WpDataRealm.getWpDataRowById(visitId)
                ?: error("Visit not found: $visitId")
            val tovarId = dataJsonString("tovarId")
                ?.takeIf { (it.toLongOrNull() ?: 0L) > 0L }
                ?: error("Product ID is missing")
            MakePhotoFromGalery.MakePhotoFromGaleryWpDataDB = RealmManager.INSTANCE.copyFromRealm(visit)
            MakePhotoFromGalery.tovarId = tovarId
            MakePhotoFromGalery.photoType = 4
            activity.startActivityForResult(
                PhotoPickerUtils.createSingleImageChooser(),
                MakePhoto.PICK_GALLERY_IMAGE_REQUEST
            )
            onStarted()
        } catch (e: Exception) {
            Log.e("SamplePhotoSDBViewModel", "Cannot open product gallery", e)
            Toast.makeText(activity, "Не вдалося відкрити галерею для цього товару", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takeProductPhoto(sample: SamplePhotoSDB, onStarted: () -> Unit) {
        val activity = context as? Activity ?: return
        try {
            val data = Gson().fromJson(dataJson, JsonObject::class.java)
            val visitId = data.get("wpDataDBId").asString.toLong()
            val optionId = data.get("optionDBId").asString
            val tovarId = data.get("tovarId").asString
            val photoType = resolvePhotoTypeId() ?: error("Photo type is missing")
            if (ProductPhotoCapture.takePhoto(activity, visitId, optionId, photoType, tovarId, sample)) {
                onStarted()
            }
        } catch (e: Exception) {
            Log.e("SamplePhotoSDBViewModel", "Invalid product capture parameters", e)
            Toast.makeText(activity, "Не вдалося відкрити камеру для цього товару", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera(stackPhotoDB: StackPhotoDB?, callback: () -> Unit) {
        val dataJsonObject = Gson().fromJson(dataJson, JsonObject::class.java)
        val wpDataDB =
            RealmManager.INSTANCE.copyFromRealm(WpDataRealm.getWpDataRowById(dataJsonObject.get("wpDataDBId").asString.toLong()))
//        val id = dataJsonObject.get("optionDBId").asString
        val optionDB =
            RealmManager.INSTANCE.copyFromRealm(OptionsRealm.getOptionById(dataJsonObject.get("optionDBId").asString))
        if (wpDataDB != null && optionDB != null) {
            val typePhotoId = resolvePhotoTypeId()

            when (contextUI) {
                ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158 -> {
                    val req = if (stackPhotoDB == null) {
                        TovarRequisites()
                    } else {
                        var reportPrepareDB: ReportPrepareDB? = null
                        val tovarDB = TovarRealm.getById(stackPhotoDB.tovar_id)
                        if (tovarDB != null)
                            reportPrepareDB = ReportPrepareRealm.getReportPrepareByTov(
                                wpDataDB.code_dad2.toString(),
                                stackPhotoDB.tovar_id
                            )

                        if (tovarDB == null || reportPrepareDB == null)
                            TovarRequisites()
                        else
                            TovarRequisites(tovarDB, reportPrepareDB)
                    }

                    MakePhoto.photoType = typePhotoId.toString()
                    req
                        .createDialog(
                            context,
                            wpDataDB,
                            optionDB
                        ) {}
                        .show()

                    callback.invoke()
                }

                else -> {
                    typePhotoId?.let {
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

                        MakePhoto.photoType = typePhotoId.toString()

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
        }
    }

    private fun resolvePhotoTypeId(): Int? {
        return when (contextUI) {
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158 -> 4
            ContextUI.SAMPLE_PHOTO_FOR_PRODUCT_GALLERY -> 4
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355 -> 5
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360 -> 31
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969 -> 10
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_135809 -> 14
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309 -> 39
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604 -> 41
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277 -> 28
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354 -> 42
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_169108 -> 47
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_172100 -> 48
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_174213 -> 49
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_174878 -> 50
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_GENERIC -> dataJsonInt("photoType")
            ContextUI.SAMPLE_PHOTO_FOR_PRODUCT -> dataJsonInt("photoType")
            else -> dataJsonInt("photoType")
        }
    }

    private fun dataJsonInt(key: String): Int? = dataJsonString(key)?.toIntOrNull()

    private fun dataJsonString(key: String): String? {
        return runCatching {
            val root = Gson().fromJson(dataJson, JsonObject::class.java)
            val value = root?.get(key)?.takeIf { !it.isJsonNull } ?: return@runCatching null
            value.asString.trim()
        }.getOrNull()
    }

    fun getWarehouseAvailabilityQuestion(): String {
        val clientName = try {
            val root = Gson().fromJson(dataJson, JsonObject::class.java)
            val visitId = root?.get("wpDataDBId")?.takeIf { !it.isJsonNull }
                ?.asString?.toLongOrNull()
            val visit = visitId?.let { WpDataRealm.getWpDataRowById(it) }
            visit?.client_txt?.takeIf { it.isNotBlank() }
                ?: visit?.client_id?.let { CustomerRealm.getCustomerById(it)?.nm }
                    ?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.e("SamplePhotoSDBViewModel", "Cannot resolve customer for warehouse question", e)
            null
        }
        val customer = clientName?.let { "заказчика «$it»" } ?: "заказчика"
        return "Есть ли на складе  данной торговой точки какой либо товар $customer?"
    }

    fun setWarehouseProductAvailable(available: Boolean) {
        if (contextUI != ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360 ||
            warehouseProductAvailable.value != null
        ) return

        savedStateHandle[warehouseAvailabilityKey] = available
        updateFilters()
        filters?.let { updateFilters(it) }
    }

    private fun buildWarehouseSampleFilter(samples: List<SamplePhotoSDB>? = null): ItemFilter? {
        if (contextUI != ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360) return null
        val available = warehouseProductAvailable.value ?: return null
        val samplesById = (samples ?: RoomManager.SQL_DB.samplePhotoDao().getPhotoLogActive(1))
            .associateBy { it.id }
        val excludedIds = if (available) {
            warehouseExceptionSampleIds
        } else {
            warehouseNoStockExcludedSampleIds
        }

        return ItemFilter(
            title = "Виключити зразки",
            clazz = SamplePhotoSDB::class,
            modeUI = ModeUI.ONE_SELECT,
            titleContext = "Зразок",
            subTitleContext = "",
            leftField = "ID",
            rightField = "ID",
            rightValuesRaw = excludedIds.map { it.toString() },
            rightValuesUI = excludedIds.map { id ->
                samplesById[id]?.nm?.takeIf { it.isNotBlank() } ?: "Зразок №$id"
            },
            enabled = true,
            excludeMode = true
        )
    }

    private fun prepareSamplePhoto(sample: SamplePhotoSDB, context: Context, index: Int): StackPhotoDB? {
        val photo = resolvePhotoDbForItem(sample, index)
        if (photo == null) {
            Log.e("SamplePhotoSDBViewModel", "Photo not found: sampleId=${sample.id}, photoId=${sample.photoId}")
            Toast.makeText(context, "Фото зразка ще не завантажено", Toast.LENGTH_SHORT).show()
            return null
        }

        this.context = context
        valueForCustomResult.value[EXAMPLE_ID] = sample.id1c ?: 0
        valueForCustomResult.value[EXAMPLE_IMG_ID] = sample.photoId ?: 0
        return photo
    }
}
