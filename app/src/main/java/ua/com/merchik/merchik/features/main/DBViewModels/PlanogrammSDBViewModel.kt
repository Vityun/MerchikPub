package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.MakePhoto.MakePhoto
import ua.com.merchik.merchik.MakePhoto.PhotoReferenceSelection
import ua.com.merchik.merchik.MakePhoto.PlanogramPhotoSelection
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammJOINSDB
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.model.ContextMenuActionEvent
import ua.com.merchik.merchik.dataLayer.model.ContextMenuActionIds
import ua.com.merchik.merchik.dataLayer.model.ContextMenuEntry
import ua.com.merchik.merchik.dataLayer.model.ContextMenuHeaderRow
import ua.com.merchik.merchik.dataLayer.model.ContextMenuHeaderUi
import ua.com.merchik.merchik.dataLayer.model.ContextMenuPayload
import ua.com.merchik.merchik.dataLayer.model.ContextMenuPresets
import ua.com.merchik.merchik.dataLayer.model.ContextMenuUiState
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.MenuLeading
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.dataLayer.model.rawAs
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm
import ua.com.merchik.merchik.dialogs.DialogFullPhoto
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import java.lang.ref.WeakReference
import java.util.IdentityHashMap
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class PlanogrammSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    private val isPhotoCapture: Boolean
        get() = contextUI == ContextUI.PLANOGRAM_MAKE_PHOTO

    private var menuContext: WeakReference<Context>? = null
    private val previewPlanograms = IdentityHashMap<StackPhotoDB, Int>()
    private var selectionContext: PlanogramPhotoSelection? = null

    override val table: KClass<out DataObjectUI>
        get() = PlanogrammSDB::class

    override fun getDefaultHideUserFields(): List<String> = listOf(
        "column_name", "group_header", "isp_id", "isp_txt", "client_id", "client_txt",
        "img_id", "photo", "photo_id", "photo_big", "dt_start", "dt_end",
        "author_id", "authorTxt", "dtUpdate"
    )

    override fun getHideSortUserFields(): List<String> = listOf("comments", "planogrammPhoto")

    override fun getItemsFooter(): List<DataItemUI> = if (isPhotoCapture) {
        repository.toItemUIList(table, listOf(PlanogrammSDB().apply {
            id = WITHOUT_PLANOGRAM_ID
            nm = "Створити фото без зазначення планограми"
            photoId = 0L
        }), contextUI, null)
    } else emptyList()

    override fun updateFilters() {
        selectionContext = null
        filters = null
        if (!isPhotoCapture) return

        try {
            val visit = currentVisit()
            val selection = PlanogramPhotoSelection.forVisit(visit)
            val now = System.currentTimeMillis() / 1000
            val date = selection.date
            val networkId = selection.networkId?.toString().orEmpty()
            val networkName = networkId.takeIf { it.isNotBlank() }
                ?.let { TradeMarkRealm.getTradeMarkRowById(it)?.nm }
                ?.takeIf { it.isNotBlank() }
                ?: if (networkId.isBlank()) "Не визначена" else "Мережа $networkId"

            filters = Filters(
                subTitle = "Планограми поточного відвідування",
                items = listOf(
                    planogramSelectionFilter("Адреса", AddressSDB::class, "addr_id",
                        visit.addr_id.toString(), visit.addr_txt.orEmpty()),
                    planogramSelectionFilter("Клієнт", CustomerSDB::class, "client_id",
                        visit.client_id.orEmpty(), visit.client_txt.orEmpty()),
                    planogramSelectionFilter("Мережа", TradeMarkDB::class, "tp_id", networkId, networkName),
                    planogramSelectionFilter("Чинні на дату", PlanogrammSDB::class, "date", date,
                        Clock.getHumanTimeSecPattern(now, "dd.MM.yyyy"))
                )
            )
            selectionContext = selection
        } catch (e: Exception) {
            Globals.writeToMLOG("ERROR", "PlanogrammSDBViewModel/updateFilters", "dataJson=$dataJson, error=$e")
        }
    }

    override suspend fun getItems(): List<DataItemUI> {
        if (!isPhotoCapture) return repository.getAllRoom(table, contextUI, null)

        return try {
            val selection = selectionContext ?: run {
                updateFilters()
                selectionContext
            } ?: return emptyList()
            val visit = selection.visit
            val photoType = captureData().text("photoType").toInt()
            // Keep the old selector's client/address/network and current-date restrictions.
            val plans = selection.getPlans()
            val counts = StackPhotoRealm.getPhotosByDAD2(visit.code_dad2, photoType)
                .groupingBy { it.planogram_id?.toIntOrNull() }.eachCount()
            val rows = plans.map { it.toPlanogramItem(counts[it.id] ?: 0) }
                .sortedBy { it.planogrammPhoto }
            repository.toItemUIList(table, rows, contextUI, null)
                .withPlanogramSelectionFilters(filters?.items.orEmpty())
        } catch (e: Exception) {
            Globals.writeToMLOG("ERROR", "PlanogrammSDBViewModel/getItems", "dataJson=$dataJson, error=$e")
            emptyList()
        }
    }

    override fun shouldOpenContextMenuOnCardClick(): Boolean = true

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        if (!isPhotoCapture) {
            super.onSelectedItemsUI(itemsUI)
            return
        }
        val photoContext = context ?: return
        val item = itemsUI.singleOrNull() ?: run {
            Toast.makeText(photoContext, "Оберіть одну планограму.", Toast.LENGTH_SHORT).show()
            return
        }
        makePlanogramPhoto(item, photoContext)
    }

    private fun makePlanogramPhoto(item: DataItemUI, context: Context): Boolean {
        if (!isPhotoCapture) return false
        val activity = context as? Activity ?: return false
        val plan = item.rawAs<PlanogrammSDB>() ?: return false
        return try {
            if (PhotoReferenceSelection.enabled(dataJson)) {
                return PhotoReferenceSelection.complete(context, dataJson, PhotoReferenceSelection.PLANOGRAM, plan.id)
            }
            val data = captureData()
            val visit = currentVisit()
            val photoType = data.text("photoType").toInt()
            require(photoType >= 0) { "Invalid photo type" }
            val optionId = data.text("optionDbId")
            val option = optionId.takeIf { it.isNotBlank() }
                ?.let { OptionsRealm.getOptionById(it) }
                ?.let { RealmManager.INSTANCE.copyFromRealm(it) }
            check(optionId.isBlank() || option != null) { "Option $optionId not found" }

            MakePhoto.photoType = photoType.toString()
            MakePhoto.photoCustomerGroup = data.text("photoCustomerGroup")
            MakePhoto.example_id = data.text("exampleId")
            MakePhoto.tovarId = data.text("tovarId")
            MakePhoto.showcase_id = data.text("showcaseId")
            MakePhoto.img_src_id = data.text("imgSrcId")
            MakePhoto.example_img_id = data.text("exampleImgId")
            MakePhoto.planogram_id = if (plan.id == WITHOUT_PLANOGRAM_ID) "0" else plan.id.toString()
            MakePhoto.planogram_img_id = if (plan.id == WITHOUT_PLANOGRAM_ID) "0" else (plan.photoId ?: 0L).toString()
            Toast.makeText(context, if (plan.id == WITHOUT_PLANOGRAM_ID) {
                "Фото без зазначення планограми"
            } else "Обрана планограма: ${plan.nm} (${plan.id})", Toast.LENGTH_LONG).show()
            MakePhoto().makePhotoForPlanogramm(
                activity, visit, option, data.get("chooseCustomerGroup")?.asBoolean ?: true
            )
            true
        } catch (e: Exception) {
            Globals.writeToMLOG("ERROR", "PlanogrammSDBViewModel/makePhoto", "dataJson=$dataJson, error=$e")
            Toast.makeText(context, "Не вдалося розпочати виготовлення фото. Відкрийте список повторно з опції.", Toast.LENGTH_LONG).show()
            false
        }
    }

    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context, index: Int) {
        val plan = clickedDataItemUI.rawAs<PlanogrammSDB>() ?: return
        if (plan.id == WITHOUT_PLANOGRAM_ID) {
            makePlanogramPhoto(clickedDataItemUI, context)
            return
        }
        val visit = if (isPhotoCapture) {
            try {
                currentVisit()
            } catch (e: Exception) {
                Globals.writeToMLOG("ERROR", "PlanogrammSDBViewModel/preview", "dataJson=$dataJson, error=$e")
                Toast.makeText(context, "Відвідування не знайдено. Відкрийте список повторно з опції.", Toast.LENGTH_LONG).show()
                return
            }
        } else null
        previewPlanograms.clear()
        if (resolvePhotoDbForItem(plan, index) == null) {
            Toast.makeText(context, "Фото планограми ще не завантажено.", Toast.LENGTH_LONG).show()
            return
        }
        super.onClickItemImage(clickedDataItemUI, context, index)
        val photoDialog = dialog ?: return
        visit?.let { photoDialog.setWpDataDB(it) }
        photoDialog.setRatingType(DialogFullPhoto.RatingType.PLANOGRAM)
        photoDialog.setRating()
        photoDialog.setDvi()
        photoDialog.setClose {
            photoDialog.dismiss()
            if (dialog === photoDialog) dialog = null
            previewPlanograms.clear()
            updateContent()
        }
        val selectCurrent: () -> Unit = action@{
            val planId = previewPlanograms[photoDialog.currentPhoto] ?: return@action
            val currentItem = uiState.value.items.firstOrNull {
                it.rawAs<PlanogrammSDB>()?.id == planId
            } ?: return@action
            if (makePlanogramPhoto(currentItem, context)) {
                photoDialog.dismiss()
                if (dialog === photoDialog) dialog = null
                previewPlanograms.clear()
            }
        }
        if (isPhotoCapture) {
            if (PhotoReferenceSelection.enabled(dataJson) && PhotoReferenceSelection.gallery(dataJson)) {
                photoDialog.setGallery { selectCurrent() }
            } else {
                photoDialog.setCamera { selectCurrent() }
            }
        }
    }

    override fun resolvePhotoDbForItem(obj: Any, index: Int): StackPhotoDB? {
        val plan = obj as? PlanogrammSDB ?: return null
        val photoId = plan.photoId?.takeIf { it > 0 } ?: return null
        // Keep plan identity separately: never replace IDs or metadata of the real photo row.
        return StackPhotoRealm.stackPhotoDBGetPhotoBySiteId2(photoId.toString())
            ?.takeIf { it.id > 0 }
            ?.also { previewPlanograms[it] = plan.id }
    }

    override fun getFieldsForCommentsImage(): List<String> = listOf("nm", "comments")

    override fun onClickFullImage(stackPhotoDB: StackPhotoDB, comment: String?) {
        val photoDialog = DialogFullPhotoR(context)
        photoDialog.setPhoto(stackPhotoDB)
        comment?.let { photoDialog.setComment(it) }
        photoDialog.hideCamera()
        photoDialog.setClose {
            photoDialog.dismiss()
            updateContent()
        }
        photoDialog.show()
    }

    override fun onClickItem(itemUI: DataItemUI, context: Context) = showPlanogramMenu(itemUI, context)

    override fun onLongClickItem(itemUI: DataItemUI, context: Context) = showPlanogramMenu(itemUI, context)

    override fun onLongClickItems(items: List<DataItemUI>, context: Context, clickedItem: DataItemUI) =
        showPlanogramMenu(clickedItem, context)

    private fun showPlanogramMenu(item: DataItemUI, context: Context) {
        val plan = item.rawAs<PlanogrammSDB>() ?: return
        menuContext = WeakReference(context)
        showContextMenu(ContextMenuUiState(
            payload = ContextMenuPayload(selectedItems = listOf(item)),
            header = ContextMenuHeaderUi(
                visible = true, title = "Оберіть дію для планограми",
                rows = listOf(ContextMenuHeaderRow(label = "Назва", value = plan.nm.orEmpty()))
            ),
            entries = listOf(
                ContextMenuEntry.Action(
                    id = "planogram_photo", actionId = PHOTO_ACTION,
                    title = if (isPhotoCapture) "Виготовити фото" else "Відкрити",
                    leading = MenuLeading.DrawableIcon(if (isPhotoCapture) android.R.drawable.ic_menu_camera else R.drawable.ic_eye)
                ),
                ContextMenuEntry.Action(
                    id = "planogram_select",
                    actionId = if (item.selected) ContextMenuActionIds.UNMARK else ContextMenuActionIds.MARK,
                    title = if (item.selected) "Зняти позначення" else "Позначити",
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_multiple_select)
                ),
                ContextMenuPresets.ListSettings.toEntry(id = "planogram_settings"),
                ContextMenuPresets.Close.toEntry(id = "planogram_close")
            )
        ))
    }

    override fun onContextMenuAction(event: ContextMenuActionEvent) {
        when (event.actionId) {
            PHOTO_ACTION -> {
                val photoContext = menuContext?.get()
                hideContextMenu()
                menuContext = null
                if (photoContext != null) {
                    if (isPhotoCapture) makePlanogramPhoto(event.payload.firstItem, photoContext)
                    else onClickItemImage(event.payload.firstItem, photoContext)
                }
            }
            ContextMenuActionIds.MARK, ContextMenuActionIds.UNMARK -> updateItemsSelect(
                ids = event.payload.selectedItems.map { it.stableId },
                checked = event.actionId == ContextMenuActionIds.MARK
            )
            ContextMenuActionIds.LIST_SETTINGS -> openSortingDialog()
            ContextMenuActionIds.CLOSE -> {
                hideContextMenu()
                menuContext = null
            }
            else -> super.onContextMenuAction(event)
        }
    }

    override fun onContextMenuDismissed() {
        menuContext = null
    }

    private fun captureData(): JsonObject = JsonParser.parseString(dataJson).asJsonObject

    private fun currentVisit(): WpDataDB {
        val dad2 = captureData().text("wpDataDBId").toLong()
        return requireNotNull(RealmManager.getWorkPlanRowByCodeDad2Detached(dad2)) {
            "Visit $dad2 not found"
        }
    }

    private fun JsonObject.text(key: String): String = get(key)?.takeUnless { it.isJsonNull }?.asString.orEmpty()

    private companion object {
        const val WITHOUT_PLANOGRAM_ID = -999
        const val PHOTO_ACTION = "planogram_photo"
    }
}

internal fun planogramSelectionFilter(
    title: String,
    clazz: KClass<out DataObjectUI>,
    field: String,
    value: String,
    displayValue: String
): ItemFilter = ItemFilter(
    title = title,
    clazz = clazz,
    modeUI = ModeUI.ONE_SELECT,
    titleContext = title,
    subTitleContext = "",
    leftField = "planogram_selection_$field",
    rightField = field,
    rightValuesRaw = listOf(value),
    rightValuesUI = listOf(displayValue.ifBlank { value }),
    enabled = false
)

internal fun List<DataItemUI>.withPlanogramSelectionFilters(filters: List<ItemFilter>): List<DataItemUI> {
    // SQL already handles unassigned plans and date intervals. These fields describe its
    // selection context, not a second equality restriction on the plan's own client/address.
    val contextFields = filters.map { filter ->
        FieldValue(
            key = filter.leftField,
            field = TextField(rawValue = filter.leftField, value = filter.title),
            value = TextField(rawValue = filter.rightValuesRaw.single().orEmpty(), value = "")
        )
    }
    val contextKeys = contextFields.map { it.key }.toSet()
    return map { item ->
        item.copy(rawFields = item.rawFields.filterNot { it.key in contextKeys } + contextFields)
    }
}

internal fun PlanogrammJOINSDB.toPlanogramItem(photoCount: Int): PlanogrammSDB = PlanogrammSDB().also {
    it.id = id
    it.nm = planogrammName
    it.comments = planogrammComment
    it.clientId = planogrammClientId?.toString()
    it.clientTxt = planogrammClientTxt
    it.dtStart = planogrammDtStart
    it.dtEnd = planogrammDtEnd
    it.photoId = planogrammPhotoId?.toLong()
    it.planogrammPhoto = photoCount
}
