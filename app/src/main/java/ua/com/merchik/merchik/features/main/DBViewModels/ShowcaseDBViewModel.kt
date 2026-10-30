package ua.com.merchik.merchik.features.main.DBViewModels

import MessageDialogData
import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.Realm
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.MakePhoto.MakePhoto
import ua.com.merchik.merchik.MakePhoto.PhotoReferenceSelection
import ua.com.merchik.merchik.MakePhoto.PhotoReferenceContext
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB
import ua.com.merchik.merchik.data.RealmModels.LogDB
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainEvent
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.common.VizitShowcaseDataHolder
import ua.com.merchik.merchik.dataLayer.model.ContextMenuActionEvent
import ua.com.merchik.merchik.dataLayer.model.ContextMenuActionIds
import ua.com.merchik.merchik.dataLayer.model.ContextMenuEntry
import ua.com.merchik.merchik.dataLayer.model.ContextMenuHeaderRow
import ua.com.merchik.merchik.dataLayer.model.ContextMenuHeaderUi
import ua.com.merchik.merchik.dataLayer.model.ContextMenuPayload
import ua.com.merchik.merchik.dataLayer.model.ContextMenuPresets
import ua.com.merchik.merchik.dataLayer.model.ContextMenuUiState
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.MenuLeading
import ua.com.merchik.merchik.dataLayer.model.SubmenuPresentation
import ua.com.merchik.merchik.dataLayer.model.rawAs
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.dialogs.DialogData
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import java.io.File
import java.lang.ref.WeakReference
import java.util.Calendar
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class ShowcaseDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    private var contextForPhotoAction: WeakReference<Context>? = null
    private val loggedShowcaseDiagnostics = mutableSetOf<String>()

    private val isPhotoCapture: Boolean
        get() = contextUI == ContextUI.SHOWCASE_MAKE_PHOTO
    private val isReferenceSelection: Boolean
        get() = PhotoReferenceSelection.enabled(dataJson)

    private val usesCompletedCheckFilters: Boolean
        get() = contextUI == ContextUI.SHOWCASE_COMPLETED_CHECK || isPhotoCapture

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
                    "doc_id", "comment", "gp", "upload_time", "upload_status", "error", "errorTime", "errorTxt", "userTxt", "customerTxt", "addressTxt",
                    "photo_typeTxt", "dvi", "mark", "premiya", "photoServerURL", "dviUpload", "commentUpload", "markUpload", "premiyaUpload", "img_src_id",
                    "showcase_id", "approve", "status", "code_iza", "planogram_id", "planogram_img_id", "example_id", "example_img_id", "specialCol", "mainOption"
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
        dialogFullPhoto.setClose {
            dialogFullPhoto.dismiss()
            if (isPhotoCapture) updateContent()
        }
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
            planogrammId.value = getPlanogrammVizitShowcaseIdOrDefault()

            val reference = getReferenceContext("updateFilters") ?: return

            val filterWpDataDB = ItemFilter(
                "Адреса",
                AddressSDB::class,
                AddressSDBViewModel::class,
                ModeUI.MULTI_SELECT,
                "title",
                "subTitle",
                "addr_id",
                "addr_id",
                mutableListOf(reference.addressId.toString()),
                mutableListOf(reference.addressName),
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
                mutableListOf(reference.clientId),
                mutableListOf(reference.clientName),
                !isReferenceSelection
            )

            val mainOptionFilter = if (usesCompletedCheckFilters && taskReferenceContext() == null) {
                getCurrentWpDataOrLog("updateFilters")?.let { buildMainOptionFilter(it) }
            } else {
                null
            }

            filters = Filters(
                rangeDataByKey = null,
                items = mutableListOf(
                    filterWpDataDB,
                    filterImagesTypeListDB
                ).apply {
                    mainOptionFilter?.let { add(it) }
                }
            )

        } catch (e: Exception) {
            logShowcaseDiagnosticOnce(
                key = "updateFilters_exception_${dataJson.previewForLog()}",
                level = "ERROR",
                stage = "updateFilters",
                message = "Cannot build filters. contextUI=$contextUI, dataJson=${dataJson.previewForLog()}",
                error = e
            )
        }
    }


    override fun getItemsFooter(): List<DataItemUI> {
        return when (contextUI) {
            ContextUI.SHOWCASE_FROM_ACHIEVEMENT,
            ContextUI.SHOWCASE_MAKE_PHOTO -> {
                if (contextUI == ContextUI.SHOWCASE_MAKE_PHOTO) {
                    getReferenceContext("getItemsFooter") ?: return emptyList()
                } else {
                    val wpDataDB = getCurrentWpDataOrLog("getItemsFooter") ?: return emptyList()
                    val showcaseDataList = getShowcaseDataList(wpDataDB, "getItemsFooter")
                    if (showcaseDataList.isNotEmpty() && !workedWithClientLessThanOptionalPeriod(wpDataDB)) {
                        return emptyList()
                    }
                }

                // сама пустая витрина
                val data = StackPhotoDB::class.java.newInstance()
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
                    val planogrammVizitShowcaseId = getPlanogrammVizitShowcaseIdOrDefault()
                    val wpDataDB = getCurrentWpDataOrLog("getItems/$contextUI") ?: return emptyList()
                    val showcaseDataList = getShowcaseDataList(wpDataDB, "getItems/$contextUI")

                    val photos = buildShowcasePhotos(showcaseDataList, wpDataDB).onEach {
                        it.specialCol = -1
                    }
                    val selectedShowcaseId = if (contextUI == ContextUI.SHOWCASE_FROM_ACHIEVEMENT) {
                        AchievementDataHolder.instance().showcaseId ?: 0
                    } else {
                        VizitShowcaseDataHolder.getInstance()[planogrammVizitShowcaseId].showcaseId
                    }

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
                                selected = selectedShowcaseId > 0 && stackPhoto?.showcaseId == selectedShowcaseId,
                                images = showcaseImages(stackPhoto)
                            )
                        }
                }
                ContextUI.SHOWCASE_COMPLETED_CHECK,
                ContextUI.SHOWCASE_MAKE_PHOTO -> {
                    val reference = getReferenceContext("getItems/$contextUI") ?: return emptyList()
                    val codeDad2 = reference.codeDad2
                    val showcaseDataList = getShowcaseDataList(reference, "getItems/$contextUI")

                    val data = buildShowcasePhotos(showcaseDataList, reference)

                    val completedPhotoTypes = if (isPhotoCapture) {
                        listOfNotNull(getCapturePhotoType())
                    } else {
                        listOf(0, 45)
                    }
                    val listOfStackPhotoCOMPLETED = completedPhotoTypes.flatMap { photoType ->
                        loadCompletedPhotos(
                            codeDad2 = codeDad2,
                            photoType = photoType,
                            stage = "getItems/$contextUI"
                        )
                    }

                    ShowcasePhotoRows.markCompleted(data, listOfStackPhotoCOMPLETED)

                    val displayData = if (isPhotoCapture) {
                        val photoCounts = listOfStackPhotoCOMPLETED.groupingBy { it.showcase_id }.eachCount()
                        data.sortedBy { photoCounts[it.showcaseId.toString()] ?: 0 }
                    } else {
                        data
                    }

                    repository
                        .toItemUIList(
                            StackPhotoDB::class,
                            displayData,
                            contextUI,
                            0
                        )
                        .map { item ->
                            val stackPhoto = item.rawObj
                                .filterIsInstance<StackPhotoDB>()
                                .firstOrNull()

                            // mainOption is an int in StackPhotoDB: keep an unknown value
                            // distinct from a real option 0 in the raw fields used by ItemFilter.
                            val mainOptionId = showcaseDataList
                                .firstOrNull { it.id == stackPhoto?.showcaseId }?.mainOptionId

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
                                selected = selected == true,
                                images = showcaseImages(stackPhoto),
                                rawFields = item.rawFields.map { field ->
                                    if (field.key == "mainOption" && mainOptionId == null)
                                        field.copy(value = field.value.copy(rawValue = ""))
                                    else field
                                }
                            )
                        }
                }
                else -> {
                    emptyList()
                }
            }

        } catch (e: Exception) {
            logShowcaseDiagnosticOnce(
                key = "getItems_exception_${contextUI}_${dataJson.previewForLog()}",
                level = "ERROR",
                stage = "getItems",
                message = "Cannot build showcase items. contextUI=$contextUI, dataJson=${dataJson.previewForLog()}",
                error = e
            )
            emptyList()
        }
    }

    override fun shouldOpenContextMenuOnCardClick(): Boolean = true

    private fun makeShowcasePhoto(
        clickedDataItemUI: DataItemUI,
        context: Context,
        onPhotoFlowStarted: () -> Unit = {}
    ) {
        if (!isPhotoCapture) return

        try {
            val activity = context as? Activity ?: return
            val photo = clickedDataItemUI.rawAs<StackPhotoDB>() ?: return
            val showcaseId = photo.showcaseId
            val showcase = if (showcaseId == 0 && photo.id == -999) {
                ShowcaseSDB().apply {
                    id = 0
                    nm = photo.showcaseName
                }
            } else {
                RoomManager.SQL_DB.showcaseDao().getById(showcaseId)
            }
            if (showcase == null) {
                Toast.makeText(context, "Вітрину не знайдено. Оновіть список.", Toast.LENGTH_LONG).show()
                return
            }
            val photoType = getCapturePhotoType()
            if (photoType == null) {
                Toast.makeText(context, "Не визначено тип фото. Відкрийте список повторно з опції.", Toast.LENGTH_LONG).show()
                return
            }

            if (isReferenceSelection) {
                if (PhotoReferenceSelection.complete(context, dataJson, PhotoReferenceSelection.SHOWCASE, showcase.id)) {
                    onPhotoFlowStarted()
                }
                return
            }

            val wpDataDB = getCurrentWpDataOrLog("makePhoto") ?: run {
                Toast.makeText(context, "Відвідування не знайдено. Оновіть список.", Toast.LENGTH_LONG).show()
                return
            }
            val captureData = JsonParser.parseString(dataJson).asJsonObject
            val optionDbId = captureData.get("optionDbId")?.takeUnless { it.isJsonNull }?.asString
            val option = optionDbId?.let { OptionsRealm.getOptionById(it) }
                ?.let { RealmManager.INSTANCE.copyFromRealm(it) }
            if (optionDbId != null && option == null) {
                Toast.makeText(context, "Опцію не знайдено. Відкрийте список повторно з опції.", Toast.LENGTH_LONG).show()
                return
            }

            MakePhoto.example_id = captureData.get("exampleId")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
            MakePhoto.tovarId = captureData.get("tovarId")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
            MakePhoto.photoCustomerGroup = captureData.get("photoCustomerGroup")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
            MakePhoto().makePhotoForShowcase(activity, wpDataDB, option, photoType.toString(), showcase)
            onPhotoFlowStarted()
        } catch (e: Exception) {
            Globals.writeToMLOG("ERROR", "ShowcaseDBViewModel/makePhoto", "dataJson=${dataJson.previewForLog()}, error=$e")
            Toast.makeText(context, "Не вдалося розпочати виготовлення фото.", Toast.LENGTH_LONG).show()
        }
    }

    private fun getCapturePhotoType(): Int? = runCatching {
        JsonParser.parseString(dataJson).asJsonObject.get("photoType")
            ?.takeUnless { it.isJsonNull }?.asString?.toIntOrNull()?.takeIf { it >= 0 }
    }.getOrNull()

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        showShowcaseContextMenu(listOf(itemUI), context)
    }

    override fun onClickItems(
        items: List<DataItemUI>,
        context: Context,
        clickedItem: DataItemUI
    ) {
        showShowcaseContextMenu(items.withClickedFirst(clickedItem), context)
    }

    override fun onLongClickItem(itemUI: DataItemUI, context: Context) {
        showShowcaseContextMenu(listOf(itemUI), context)
    }

    override fun onLongClickItems(
        items: List<DataItemUI>,
        context: Context,
        clickedItem: DataItemUI
    ) {
        showShowcaseContextMenu(items.withClickedFirst(clickedItem), context)
    }

    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context, index: Int) {
        val photo = clickedDataItemUI.rawAs<StackPhotoDB>()
        if (photo == null || resolvePhotoDbForItem(photo, index) == null) {
            Toast.makeText(context, "Фото вітрини ще не завантажено.", Toast.LENGTH_LONG).show()
            return
        }

        super.onClickItemImage(clickedDataItemUI, context, index)
        if (!isPhotoCapture) return

        val photoDialog = dialog ?: return
        photoDialog.setClose {
            photoDialog.dismiss()
            if (dialog === photoDialog) dialog = null
            updateContent()
        }
        val selectCurrent: () -> Unit = action@{
            val currentPhoto = photoDialog.currentPhoto ?: return@action
            val currentItem = uiState.value.items.firstOrNull {
                it.rawAs<StackPhotoDB>()?.showcaseId == currentPhoto.showcaseId
            } ?: return@action

            makeShowcasePhoto(currentItem, context) {
                photoDialog.dismiss()
                if (dialog === photoDialog) dialog = null
            }
        }
        if (isReferenceSelection && PhotoReferenceSelection.gallery(dataJson)) {
            photoDialog.setGallery { selectCurrent() }
        } else {
            photoDialog.setCamera { selectCurrent() }
        }
    }

    override fun resolvePhotoDbForItem(obj: Any, index: Int): StackPhotoDB? {
        if (obj !is StackPhotoDB) {
            return super.resolvePhotoDbForItem(obj, index)
        }

        val photoId = obj.photoServerId?.trim()
            ?.takeIf { (it.toLongOrNull() ?: 0L) > 0L }
            ?: return null

        // UI placeholders use showcase IDs. Only real photo rows may reach the downloader.
        return runCatching {
            StackPhotoRealm.stackPhotoDBGetPhotoBySiteId2(photoId)
                ?.takeIf { it.id > 0 }
                ?.apply {
                    restoreShowcasePhotoCache(this)
                    showcaseId = obj.showcaseId
                    showcaseName = obj.showcaseName
                }
        }.onFailure { error ->
            logShowcaseDiagnosticOnce(
                key = "resolve_photo_$photoId",
                level = "ERROR",
                stage = "resolvePhotoDbForItem",
                message = "Cannot resolve original showcase photo. photoServerId=$photoId",
                error = error
            )
        }.getOrNull()
    }

    override fun onContextMenuAction(event: ContextMenuActionEvent) {
        when (event.actionId) {
            ContextMenuActionIds.SHOWCASE_VIEW_PHOTO -> {
                hideContextMenu()
                contextForPhotoAction?.get()?.let { context ->
                    if (isPhotoCapture) {
                        makeShowcasePhoto(event.payload.firstItem, context)
                    } else {
                        onClickItemImage(event.payload.firstItem, context)
                    }
                }
                contextForPhotoAction = null
            }

            ContextMenuActionIds.MARK -> {
                updateItemsSelect(
                    ids = event.payload.selectedItems.map { it.stableId },
                    checked = true
                )
            }

            ContextMenuActionIds.MARK_ALL -> {
                updateItemsSelect(
                    ids = uiState.value.items.map { it.stableId },
                    checked = true
                )
            }

            ContextMenuActionIds.UNMARK -> {
                updateItemsSelect(
                    ids = event.payload.selectedItems.map { it.stableId },
                    checked = false
                )
            }

            ContextMenuActionIds.UNMARK_ALL -> {
                updateItemsSelect(
                    ids = uiState.value.items.map { it.stableId },
                    checked = false
                )
            }

            ContextMenuActionIds.INVERT -> {
                val selectedIds = uiState.value.items
                    .filter { it.selected }
                    .map { it.stableId }

                val unselectedIds = uiState.value.items
                    .filter { !it.selected }
                    .map { it.stableId }

                if (selectedIds.isNotEmpty()) {
                    updateItemsSelect(
                        ids = selectedIds,
                        checked = false
                    )
                }

                if (unselectedIds.isNotEmpty()) {
                    updateItemsSelect(
                        ids = unselectedIds,
                        checked = true
                    )
                }
            }

            ContextMenuActionIds.COLLAPSE_LIST -> {
                collapseAllDecks()
            }

            ContextMenuActionIds.EXPAND_LIST -> {
                expandAllDecks()
            }

            ContextMenuActionIds.LIST_SETTINGS -> {
                openSortingDialog()
            }

            ContextMenuActionIds.SHOWCASE_NOT_ACTUAL -> {
                val context = contextForPhotoAction?.get()
                hideContextMenu()
                contextForPhotoAction = null
                if (context != null) {
                    showShowcaseNotActualDialog(event.payload.firstItem, context)
                } else {
                    Globals.writeToMLOG(
                        "ERROR",
                        "ShowcaseDBViewModel/onContextMenuAction",
                        "Cannot open showcase not actual dialog: context is null"
                    )
                }
            }

            ContextMenuActionIds.CLOSE -> {
                hideContextMenu()
                contextForPhotoAction = null
            }

            else -> super.onContextMenuAction(event)
        }
    }

    override fun onContextMenuDismissed() {
        contextForPhotoAction = null
    }

    private fun showShowcaseContextMenu(items: List<DataItemUI>, context: Context) {
        if (items.isEmpty()) return

        contextForPhotoAction = WeakReference(context)

        val payload = ContextMenuPayload(
            selectedItems = items
        )

        showContextMenu(
            ContextMenuUiState(
                payload = payload,
                header = buildShowcaseHeader(items),
                entries = buildShowcaseEntries(payload)
            )
        )
    }

    private fun buildShowcaseHeader(items: List<DataItemUI>): ContextMenuHeaderUi {
        val showcaseIds = items
            .mapNotNull { item -> item.showcaseIdText() }
            .distinct()

        val showcaseNames = items
            .mapNotNull { item -> item.showcaseNameText() }
            .distinct()

        val rows = buildList {
            add(
                ContextMenuHeaderRow(
                    label = "Iдентифiкатор",
                    value = showcaseIds.toHeaderValue(defaultValue = "0")
                )
            )
            add(
                ContextMenuHeaderRow(
                    label = "Назва",
                    value = showcaseNames.toHeaderValue(defaultValue = "Без назви")
                )
            )
        }

        return ContextMenuHeaderUi(
            visible = true,
            title = if (items.size > 1) {
                "Оберіть дію для групи з ${items.size} вітрин"
            } else {
                "Оберіть дію для вітрини"
            },
            rows = rows
        )
    }

    private fun buildShowcaseEntries(payload: ContextMenuPayload): List<ContextMenuEntry> {
        val isActiveGrouped = uiState.value.groupingFields.any { it.key?.isNotBlank() == true }

        return buildList {
            add(
                ContextMenuEntry.Action(
                    id = "showcase_view_photo",
                    actionId = ContextMenuActionIds.SHOWCASE_VIEW_PHOTO,
                    title = if (isPhotoCapture) "Виготовити фото" else "Відкрити",
                    leading = MenuLeading.DrawableIcon(
                        if (isPhotoCapture) android.R.drawable.ic_menu_camera else R.drawable.ic_eye
                    )
                )
            )

            add(
                ContextMenuEntry.Action(
                    id = "showcase_add",
                    actionId = "showcase_add",
                    title = "Додати",
                    leading = MenuLeading.Text("+"),
                    enabled = false
                )
            )

            add(
                ContextMenuEntry.Action(
                    id = "showcase_edit",
                    actionId = "showcase_edit",
                    title = "Змінити",
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_24),
                    enabled = false
                )
            )

            add(
                ContextMenuEntry.Action(
                    id = "showcase_delete",
                    actionId = "showcase_delete",
                    title = "Видалити",
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_delete),
                    enabled = false
                )
            )

            add(ContextMenuEntry.Divider("showcase_divider_open_selection"))

            add(
                ContextMenuEntry.Submenu(
                    id = "showcase_submenu_selection",
                    title = "Позначення элементiв",
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_multiple_select),
                    items = buildShowcaseSelectionEntries(payload),
                    presentation = SubmenuPresentation.INLINE_EXPAND,
                    expandedByDefault = false
                )
            )

            add(ContextMenuEntry.Divider("showcase_divider_selection_list"))

            if (!isActiveGrouped) {
                add(
                    ContextMenuPresets.ListSettings.toEntry(
                        id = "showcase_list_settings"
                    )
                )
            } else {
                add(
                    ContextMenuEntry.Submenu(
                        id = "showcase_submenu_list",
                        title = "Групування",
                        leading = MenuLeading.DrawableIcon(R.drawable.ic_sort_down),
                        items = buildShowcaseListEntries(),
                        presentation = SubmenuPresentation.INLINE_EXPAND,
                        expandedByDefault = false
                    )
                )
            }

            add(ContextMenuEntry.Divider("showcase_divider_list_actions"))

            add(
                ContextMenuEntry.Submenu(
                    id = "showcase_actions",
                    title = "Обрати дію",
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_21),
                    items = listOf(
                        ContextMenuEntry.Action(
                            id = "showcase_not_actual",
                            actionId = ContextMenuActionIds.SHOWCASE_NOT_ACTUAL,
                            title = "Позначити як не актуальну",
                            leading = MenuLeading.DrawableIcon(R.drawable.ic_hide)
                        ),
                        ContextMenuEntry.Divider("showcase_divider_selection"),
                        ContextMenuPresets.Close.toEntry(
                            id = "showcase_actions_close"
                        )
                    ),
                    presentation = SubmenuPresentation.OVERLAY,
                    expandedByDefault = false
                )
            )
            add(ContextMenuEntry.Divider("showcase_root_divider_close"))

            add(
                ContextMenuPresets.Close.toEntry(
                    id = "showcase_root_close"
                )
            )
        }
    }

    private fun buildShowcaseSelectionEntries(payload: ContextMenuPayload): List<ContextMenuEntry> {
        val items = payload.selectedItems
        val payloadIds = items.map { it.stableId }.toSet()
        val allItems = uiState.value.items

        val selectedInPayloadCount = allItems.count { it.stableId in payloadIds && it.selected }
        val unselectedInPayloadCount = allItems.count { it.stableId in payloadIds && !it.selected }
        val selectedGlobalCount = allItems.count { it.selected }
        val unselectedGlobalCount = allItems.count { !it.selected }
        val isMulti = items.size > 1

        return listOf(
            ContextMenuPresets.Mark.toEntry(
                id = "showcase_selection_mark",
                title = if (isMulti) {
                    "Позначити ($unselectedInPayloadCount)"
                } else {
                    "Позначити"
                },
                enabled = unselectedInPayloadCount > 0
            ),
            ContextMenuPresets.MarkAll.toEntry(
                id = "showcase_selection_mark_all",
                title = "Позначити усі ($unselectedGlobalCount)",
                enabled = unselectedGlobalCount > 0
            ),
            ContextMenuPresets.Unmark.toEntry(
                id = "showcase_selection_unmark",
                title = if (isMulti) {
                    "Зняти позначку ($selectedInPayloadCount)"
                } else {
                    "Зняти позначку"
                },
                enabled = selectedInPayloadCount > 0
            ),
            ContextMenuPresets.UnmarkAll.toEntry(
                id = "showcase_selection_unmark_all",
                title = "Зняти всі позначки ($selectedGlobalCount)",
                enabled = selectedGlobalCount > 0
            ),
            ContextMenuPresets.Invert.toEntry(
                id = "showcase_selection_invert",
                enabled = payloadIds.isNotEmpty()
            )
        )
    }

    private fun buildShowcaseListEntries(): List<ContextMenuEntry> {
        return listOf(
            ContextMenuPresets.CollapseList.toEntry(
                id = "showcase_list_collapse"
            ),
            ContextMenuPresets.ExpandList.toEntry(
                id = "showcase_list_expand"
            )
        )
    }

    private fun showShowcaseNotActualDialog(item: DataItemUI, context: Context) {
        cancelPending()
        emitEvent(
            MainEvent.ShowMessageDialog(
                MessageDialogData(
                    title = "Пометить как не актуальную",
                    subTitle = "Отмеченные как неактуальные витрины не будут использоваться (отображаться) системой. Поданная заявка будет обработана на протяжении нескольких дней.",
                    message = "Подать заявку на удаление текущей витрины из списка актуальных?",
                    status = DialogStatus.NORMAL,
                    positivText = "Да",
                    cancelText = "Нет",
                    showButton = true,
                    isCancelable = true,
                    onButtonOkClicked = {
                        showShowcaseNotActualReasonDialog(item, context)
                    }
                )
            )
        )
    }

    private fun showShowcaseNotActualReasonDialog(item: DataItemUI, context: Context) {
        val showcaseId = item.showcaseIdLongOrNull()
        val showcaseName = item.showcaseNameText().orEmpty()
        val dialog = DialogData(context)

        dialog.setTitle("Пометить как не актуальную")
        dialog.setText(
            "Введите причину по которой Вы считаете витрину ${showcaseLabel(showcaseId, showcaseName)} не актуальной"
        )
        dialog.setOperation(DialogData.Operations.TEXT, "", null, null)
        dialog.setOkNotClose("Сохранить") {
            val userComment = dialog.operationResult
                ?.trim()
                .orEmpty()

            if (userComment.length < SHOWCASE_NOT_ACTUAL_MIN_COMMENT_LENGTH) {
                Toast.makeText(
                    context,
                    "Введите причину минимум ${SHOWCASE_NOT_ACTUAL_MIN_COMMENT_LENGTH} символов",
                    Toast.LENGTH_LONG
                ).show()
                return@setOkNotClose
            }

            if (saveShowcaseNotActualRequest(item, userComment)) {
                dialog.dismiss()
                showShowcaseNotActualSuccessDialog(showcaseId)
            } else {
                Toast.makeText(
                    context,
                    "Не удалось сохранить заявку. Попробуйте еще раз.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        dialog.setCancel("Отмена") {
            dialog.dismiss()
        }
        dialog.setClose {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun saveShowcaseNotActualRequest(item: DataItemUI, userComment: String): Boolean {
        val wp = getCurrentWpDataForShowcaseAction()
        val showcaseId = item.showcaseIdLongOrNull()
        val showcaseName = item.showcaseNameText().orEmpty()
        val photoServerId = item.rawAs<StackPhotoDB>()?.photoServerId.orEmpty()

        if (wp == null || showcaseId == null) {
            Globals.writeToMLOG(
                "ERROR",
                "ShowcaseDBViewModel/saveShowcaseNotActualRequest",
                "Cannot create LogDB: wp=$wp, showcaseId=$showcaseId, photoServerId=$photoServerId"
            )
            return false
        }

        return runCatching {
            val comments = buildString {
                append("Витрина не актуальна.")
                append(" Заявка на удаление витрины из списка актуальных.")
                append(" Витрина: №").append(showcaseId)
                if (showcaseName.isNotBlank()) {
                    append(" ").append(showcaseName)
                }
                if (photoServerId.isNotBlank()) {
                    append(". Фото витрины: ").append(photoServerId)
                }
                append(". Визит: ").append(wp.code_dad2)
                append(" | Комментарий пользователя: ").append(userComment)
            }

            RealmManager.setRowToLog(
                listOf(
                    LogDB(
                        RealmManager.getLastIdLogDB() + 1,
                        System.currentTimeMillis() / 1000,
                        comments,
                        SHOWCASE_NOT_ACTUAL_LOG_THEME_ID,
                        wp.client_id,
                        wp.addr_id,
                        showcaseId,
                        Globals.getCurrentUserId()
                            .takeIf { it > 0 }
                            ?: wp.user_id,
                        null,
                        Globals.session,
                        wp.dt?.toString(),
                        wp.code_dad2.toString(),
                        wp.theme_id.toString()
                    )
                )
            )

            Globals.writeToMLOG(
                "INFO",
                "ShowcaseDBViewModel/saveShowcaseNotActualRequest",
                "Created LogDB theme=$SHOWCASE_NOT_ACTUAL_LOG_THEME_ID, showcaseId=$showcaseId, codeDad2=${wp.code_dad2}"
            )
            true
        }.onFailure { error ->
            Globals.writeToMLOG(
                "ERROR",
                "ShowcaseDBViewModel/saveShowcaseNotActualRequest",
                "Exception: $error, showcaseId=$showcaseId, codeDad2=${wp.code_dad2}"
            )
        }.getOrDefault(false)
    }

    private fun showShowcaseNotActualSuccessDialog(showcaseId: Long?) {
        cancelPending()
        emitEvent(
            MainEvent.ShowMessageDialog(
                MessageDialogData(
                    title = "Пометить как не актуальную",
                    message = "Заявка на исключение витрины №${showcaseId ?: 0} передана ее оператору и будет им рассмотрена в течении нескольких дней.",
                    status = DialogStatus.NORMAL,
                    positivText = "Ок",
                    cancelText = "Вiдмiнити"
                )
            )
        )
    }

    private fun showcaseLabel(showcaseId: Long?, showcaseName: String): String {
        return buildString {
            append("№").append(showcaseId ?: 0)
            if (showcaseName.isNotBlank()) {
                append(" ").append(showcaseName)
            }
        }
    }

    private fun getCurrentWpDataForShowcaseAction(): WpDataDB? {
        val codeDad2 = getCurrentCodeDad2ForShowcaseAction() ?: return null
        return RealmManager.getWorkPlanRowByCodeDad2Detached(codeDad2)
    }

    private fun taskReferenceContext(): PhotoReferenceContext? =
        if (isPhotoCapture && isReferenceSelection) PhotoReferenceContext.fromReferenceJson(dataJson) else null

    private fun getReferenceContext(stage: String): PhotoReferenceContext? =
        taskReferenceContext() ?: getCurrentWpDataOrLog(stage)?.let { PhotoReferenceContext.fromVisit(it) }

    private fun getCurrentWpDataOrLog(stage: String): WpDataDB? {
        val codeDad2 = getCurrentCodeDad2ForShowcaseAction()
        if (codeDad2 == null) {
            logShowcaseDiagnosticOnce(
                key = "${stage}_missing_dad2_${dataJson.previewForLog()}",
                level = "ERROR",
                stage = stage,
                message = "Cannot resolve codeDad2. contextUI=$contextUI, dataJson=${dataJson.previewForLog()}"
            )
            return null
        }

        val wpDataDB = RealmManager.getWorkPlanRowByCodeDad2Detached(codeDad2)
        if (wpDataDB == null) {
            logShowcaseDiagnosticOnce(
                key = "${stage}_missing_wp_$codeDad2",
                level = "ERROR",
                stage = stage,
                message = "WpDataDB not found. contextUI=$contextUI, codeDad2=$codeDad2"
            )
        }
        return wpDataDB
    }

    private fun getCurrentCodeDad2ForShowcaseAction(): Long? {
        return runCatching {
            val rawDataJson = dataJson?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            val root = JsonParser.parseString(rawDataJson)

            when {
                root.isJsonPrimitive -> root.asString.toLongOrNull()
                root.isJsonObject -> {
                    val json = root.asJsonObject
                    listOf("wpDataDBId", "codeDad2")
                        .firstNotNullOfOrNull { key ->
                            json.get(key)
                                ?.takeIf { !it.isJsonNull }
                                ?.asString
                                ?.toLongOrNull()
                        }
                }

                else -> null
            }
        }.getOrNull()
    }

    private fun getPlanogrammVizitShowcaseIdOrDefault(): Int {
        return runCatching {
            val rawDataJson = dataJson?.trim()?.takeIf { it.isNotEmpty() } ?: return 0
            val root = JsonParser.parseString(rawDataJson)
            if (!root.isJsonObject) return 0

            root.asJsonObject
                .get("planogrammVizitShowcaseId")
                ?.takeIf { !it.isJsonNull }
                ?.asString
                ?.toIntOrNull()
                ?: 0
        }.onFailure { error ->
            logShowcaseDiagnosticOnce(
                key = "planogramm_id_parse_${dataJson.previewForLog()}",
                level = "ERROR",
                stage = "getPlanogrammVizitShowcaseIdOrDefault",
                message = "Cannot parse planogrammVizitShowcaseId. dataJson=${dataJson.previewForLog()}",
                error = error
            )
        }.getOrDefault(0)
    }

    private fun getShowcaseDataList(wpDataDB: WpDataDB, stage: String): List<ShowcaseSDB> =
        getShowcaseDataList(PhotoReferenceContext.fromVisit(wpDataDB), stage)

    private fun getShowcaseDataList(reference: PhotoReferenceContext, stage: String): List<ShowcaseSDB> {
        val clientId = reference.clientId
            .trim()
            .takeIf { it.isNotEmpty() }

        if (clientId == null || reference.addressId <= 0) {
            logShowcaseDiagnosticOnce(
                key = "${stage}_invalid_wp_${reference.codeDad2}",
                level = "ERROR",
                stage = stage,
                message = "Cannot load showcases: invalid context. codeDad2=${reference.codeDad2}, clientId=$clientId, addrId=${reference.addressId}"
            )
            return emptyList()
        }

        val isCompletedCheck = usesCompletedCheckFilters
        val typesForLog = if (isCompletedCheck) "all" else SHOWCASE_TYPES.toString()

        return runCatching {
            val showcaseDao = RoomManager.SQL_DB.showcaseDao()
            if (isCompletedCheck) {
                // Same query selection as OptionControlPhotoShowcase, sections 2.1-2.2.
                val showcaseIds = AdditionalRequirementsRealm.getAdditionalRequirements(
                    clientId,
                    reference.addressId,
                    SHOWCASE_CONTROL_OPTION_ID
                ).map { it.showcaseTpId }

                if (showcaseIds.isNotEmpty()) {
                    showcaseDao.getByDoc(clientId, reference.addressId, showcaseIds)
                } else {
                    showcaseDao.getByDoc(clientId, reference.addressId)
                }
            } else {
                showcaseDao.getByDocTP(
                    clientId,
                    reference.addressId,
                    SHOWCASE_TYPES
                )
            }
        }.onSuccess { showcases ->
            if (showcases.isEmpty()) {
                logShowcaseDiagnosticOnce(
                    key = "${stage}_empty_showcases_${reference.codeDad2}_${clientId}_${reference.addressId}",
                    level = "INFO",
                    stage = stage,
                    message = "ShowcaseSDB list is empty. contextUI=$contextUI, codeDad2=${reference.codeDad2}, clientId=$clientId, addrId=${reference.addressId}, tpIds=$typesForLog"
                )
            }
        }.onFailure { error ->
            logShowcaseDiagnosticOnce(
                key = "${stage}_showcase_query_exception_${reference.codeDad2}",
                level = "ERROR",
                stage = stage,
                message = "ShowcaseSDB query failed. contextUI=$contextUI, codeDad2=${reference.codeDad2}, clientId=$clientId, addrId=${reference.addressId}",
                error = error
            )
        }.getOrDefault(emptyList())
    }

    private fun buildShowcasePhotos(
        showcases: List<ShowcaseSDB>,
        wpDataDB: WpDataDB
    ): List<StackPhotoDB> = buildShowcasePhotos(showcases, PhotoReferenceContext.fromVisit(wpDataDB))

    private fun buildShowcasePhotos(
        showcases: List<ShowcaseSDB>,
        reference: PhotoReferenceContext
    ): List<StackPhotoDB> {
        val photoIds = showcases.mapNotNull { it.photoId?.takeIf { id -> id > 0 }?.toString() }.distinct()
        val photos = loadShowcasePhotos(photoIds, "getItems/$contextUI", reference)
            .onEach { restoreShowcasePhotoCache(it) }
        return ShowcasePhotoRows.build(showcases, photos, reference)
    }

    private fun showcaseImages(photo: StackPhotoDB?): List<String> =
        listOf(photo?.photo_num?.takeIf { it.isNotBlank() } ?: R.mipmap.merchik.toString())

    private fun restoreShowcasePhotoCache(photo: StackPhotoDB) {
        if (photo.id <= 0 || photo.photo_size != "Full") return
        val photoId = photo.photoServerId?.takeIf { (it.toLongOrNull() ?: 0L) > 0L } ?: return
        val previousPath = photo.photo_num ?: return

        runCatching {
            val pictures = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: return
            val replacedFile = File(pictures, "ShowcaseOriginal/$photoId.jpg")
            if (File(previousPath).absolutePath != replacedFile.absolutePath) return

            // Repair only the cache written by the removed ShowcaseOriginal downloader.
            // Read bounds, not bitmaps; keep the largest surviving file as the preview.
            val fullFile = File(pictures, "UserPhotoFull-$photoId.jpg")
            val legacyFile = File(pictures, "Showcase/$photoId.jpg")
            val restoredFile = listOf(fullFile, legacyFile, replacedFile)
                .mapNotNull { file ->
                    if (!file.isFile) return@mapNotNull null
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.path, bounds)
                    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@mapNotNull null
                    file to bounds.outWidth.toLong() * bounds.outHeight
                }
                .maxByOrNull { it.second }
                ?.first ?: replacedFile
            val restoredSize = if (restoredFile == fullFile) "Full" else "Small"

            Realm.getDefaultInstance().use { realm ->
                realm.executeTransaction { transaction ->
                    val current = transaction.where(StackPhotoDB::class.java)
                        .equalTo("id", photo.id)
                        .equalTo("photoServerId", photoId)
                        .equalTo("photo_num", previousPath)
                        .equalTo("photo_size", "Full")
                        .findFirst() ?: return@executeTransaction
                    current.photo_num = restoredFile.absolutePath
                    // Small lets DialogFullPhoto use the standard authenticated PhotoDownload.
                    current.photo_size = restoredSize
                    photo.photo_num = current.photo_num
                    photo.photo_size = current.photo_size
                }
            }
            logShowcaseDiagnosticOnce(
                key = "restore_cache_${photo.id}",
                level = "INFO",
                stage = "restorePhotoCache",
                message = "photoServerId=$photoId, previousFile=$previousPath, file=${photo.photo_num}, size=${photo.photo_size}"
            )
        }.onFailure { error ->
            logShowcaseDiagnosticOnce(
                key = "restore_cache_error_${photo.id}",
                level = "ERROR",
                stage = "restorePhotoCache",
                message = "Cannot restore photo cache. photoServerId=$photoId",
                error = error
            )
        }
    }

    private fun loadShowcasePhotos(
        photoIds: Collection<String>,
        stage: String,
        reference: PhotoReferenceContext
    ): List<StackPhotoDB> {
        val cleanPhotoIds = photoIds
            .mapNotNull { it.trim().takeIf { id -> id.isNotEmpty() && id != "0" } }
            .distinct()

        if (cleanPhotoIds.isEmpty()) {
            if (photoIds.isNotEmpty()) {
                logShowcaseDiagnosticOnce(
                    key = "${stage}_photo_ids_cleaned_empty_${reference.codeDad2}",
                    level = "INFO",
                    stage = stage,
                    message = "Showcase photo ids became empty after cleanup. contextUI=$contextUI, codeDad2=${reference.codeDad2}, rawCount=${photoIds.size}"
                )
            }
            return emptyList()
        }

        return runCatching {
            RealmManager.INSTANCE.copyFromRealm(
                StackPhotoRealm.getByIds2(cleanPhotoIds.toTypedArray())
            )
        }.onSuccess { photos ->
            val loadedIds = photos
                .mapNotNull { it.photoServerId?.trim()?.takeIf { id -> id.isNotEmpty() } }
                .toSet()
            val missingIds = cleanPhotoIds.filterNot { it in loadedIds }

            if (missingIds.isNotEmpty()) {
                logShowcaseDiagnosticOnce(
                    key = "${stage}_missing_stack_photos_${reference.codeDad2}_${missingIds.size}",
                    level = "INFO",
                    stage = stage,
                    message = "ShowcaseSDB references photos not loaded in StackPhotoDB. contextUI=$contextUI, codeDad2=${reference.codeDad2}, clientId=${reference.clientId}, addrId=${reference.addressId}, expected=${cleanPhotoIds.size}, loaded=${photos.size}, missing=${missingIds.size}, missingIds=${missingIds.previewIds()}"
                )
            }
        }.onFailure { error ->
            logShowcaseDiagnosticOnce(
                key = "${stage}_stack_photo_query_exception_${reference.codeDad2}",
                level = "ERROR",
                stage = stage,
                message = "StackPhotoDB query failed. contextUI=$contextUI, codeDad2=${reference.codeDad2}, expectedPhotoIds=${cleanPhotoIds.previewIds()}",
                error = error
            )
        }.getOrDefault(emptyList())
    }

    private fun loadCompletedPhotos(
        codeDad2: Long,
        photoType: Int,
        stage: String
    ): List<StackPhotoDB> {
        return runCatching {
            RealmManager.INSTANCE.copyFromRealm(
                StackPhotoRealm.getPhotosByDAD2(
                    codeDad2,
                    photoType
                )
            )
        }.onFailure { error ->
            logShowcaseDiagnosticOnce(
                key = "${stage}_completed_photos_exception_${codeDad2}_$photoType",
                level = "ERROR",
                stage = stage,
                message = "Cannot load completed photos. contextUI=$contextUI, codeDad2=$codeDad2, photoType=$photoType",
                error = error
            )
        }.getOrDefault(emptyList())
    }

    private fun workedWithClientLessThanOptionalPeriod(wpDataDB: WpDataDB): Boolean {
        return runCatching {
            val dossier = RoomManager.SQL_DB
                .dossierSotrDao()
                .getData(null, SHOWCASE_DOSSIER_THEME_ID, wpDataDB.code_iza)
                .firstOrNull()
                ?: run {
                    logShowcaseDiagnosticOnce(
                        key = "dossier_missing_${wpDataDB.code_dad2}_${wpDataDB.code_iza}",
                        level = "INFO",
                        stage = "workedWithClientLessThanOptionalPeriod",
                        message = "DossierSotrSDB not found, allow create photo without showcase. codeDad2=${wpDataDB.code_dad2}, codeIza=${wpDataDB.code_iza}, themeId=$SHOWCASE_DOSSIER_THEME_ID"
                    )
                    return true
                }

            val workDateSec = wpDataDB.dt
                ?.time
                ?.div(1_000L)
                ?: run {
                    logShowcaseDiagnosticOnce(
                        key = "wp_dt_missing_${wpDataDB.code_dad2}",
                        level = "ERROR",
                        stage = "workedWithClientLessThanOptionalPeriod",
                        message = "WpDataDB.dt is empty, cannot calculate optional showcase period. codeDad2=${wpDataDB.code_dad2}"
                    )
                    return false
                }

            val clientWorkStartSec = dossier.priznak
                ?.takeIf { it > UNIX_1971_SEC }
                ?: workDateSec

            clientWorkStartSec > workDateSec - SHOWCASE_OPTIONAL_WORK_DAYS * SECONDS_IN_DAY
        }.onFailure { error ->
            logShowcaseDiagnosticOnce(
                key = "dossier_period_exception_${wpDataDB.code_dad2}",
                level = "ERROR",
                stage = "workedWithClientLessThanOptionalPeriod",
                message = "Cannot calculate optional showcase period, allow create photo without showcase. codeDad2=${wpDataDB.code_dad2}, codeIza=${wpDataDB.code_iza}",
                error = error
            )
        }.getOrDefault(true)
    }

    private fun DataItemUI.showcaseIdText(): String? {
        val photo = rawAs<StackPhotoDB>()
        return photo?.showcaseId
            ?.takeIf { it > 0 }
            ?.toString()
            ?: fieldDisplayValueOrNull("showcaseId")
            ?: fieldDisplayValueOrNull("showcase_id")
    }

    private fun DataItemUI.showcaseNameText(): String? {
        val photo = rawAs<StackPhotoDB>()
        return photo?.showcaseName
            ?.takeIf { it.isNotBlank() }
            ?: fieldDisplayValueOrNull("showcaseName")
            ?: fieldDisplayValueOrNull("showcase_name")
    }

    private fun DataItemUI.showcaseIdLongOrNull(): Long? {
        val photo = rawAs<StackPhotoDB>()
        return photo?.showcaseId
            ?.takeIf { it > 0 }
            ?.toLong()
            ?: photo?.showcase_id?.toLongOrNull()
            ?: fieldDisplayValueOrNull("showcaseId")?.toLongOrNull()
            ?: fieldDisplayValueOrNull("showcase_id")?.toLongOrNull()
    }

    private fun List<DataItemUI>.withClickedFirst(clickedItem: DataItemUI): List<DataItemUI> {
        if (isEmpty()) return listOf(clickedItem)
        return listOf(clickedItem) + filter { it.stableId != clickedItem.stableId }
    }

    private fun List<String>.toHeaderValue(defaultValue: String): String {
        val values = filter { it.isNotBlank() }
        return when {
            values.isEmpty() -> defaultValue
            values.size == 1 -> values.first()
            values.size <= 3 -> values.joinToString(", ")
            else -> values.take(3).joinToString(", ") + " ..."
        }
    }

    private fun DataItemUI.fieldDisplayValueOrNull(key: String): String? {
        return fields.firstOrNull { it.key.equals(key, ignoreCase = true) }
            ?.value
            ?.value
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: rawFields.firstOrNull { it.key.equals(key, ignoreCase = true) }
                ?.value
                ?.rawValue
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
    }

    private fun buildMainOptionFilter(wpDataDB: WpDataDB): ItemFilter? {
        val mainOptionId = wpDataDB.main_option_id
            ?.trim()
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
            ?: return null

        val showcaseDataList = getShowcaseDataList(wpDataDB, "buildMainOptionFilter")
        // Match OptionControlPhotoShowcase 2.2, including the visit-date cutoff.
        val dateFromNewLogic = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 13, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val useNewLogic = wpDataDB.dt?.let { !it.before(dateFromNewLogic) } == true
        val allowedMainOptionIds = if (useNewLogic) listOf(mainOptionId, 0) else listOf(mainOptionId)
        val hasMatchingShowcase = showcaseDataList.any { it.mainOptionId in allowedMainOptionIds }

        if (!hasMatchingShowcase) {
            val localMainOptions = showcaseDataList
                .mapNotNull { it.mainOptionId?.takeIf { optionId -> optionId > 0 } }
                .distinct()

            logShowcaseDiagnosticOnce(
                key = "main_option_filter_skip_${wpDataDB.code_dad2}_$mainOptionId",
                level = "INFO",
                stage = "buildMainOptionFilter",
                message = "Skip mainOption filter: local showcases do not contain allowed main options. contextUI=$contextUI, codeDad2=${wpDataDB.code_dad2}, wpMainOptionId=$mainOptionId, allowedMainOptionIds=$allowedMainOptionIds, showcaseCount=${showcaseDataList.size}, localMainOptions=${localMainOptions.map { it.toString() }.previewIds()}"
            )
            return null
        }

        val optionName = getMainOptionName(mainOptionId)

        return ItemFilter(
            "Основна опція",
            OptionsDB::class,
            OptionsDBViewModel::class,
            ModeUI.MULTI_SELECT,
            "Основна опція",
            "Оберіть основну опцію",
            "mainOption",
            "iD",
            allowedMainOptionIds.map { it.toString() },
            allowedMainOptionIds.map { if (it == 0) "Без основної опції" else optionName },
            true
        )
    }

    private fun getMainOptionName(mainOptionId: Int): String {
        return OptionsRealm.getOptionById(mainOptionId.toString())
            ?.let { RealmManager.INSTANCE.copyFromRealm(it) }
            ?.optionTxt
            ?.takeIf { it.isNotBlank() }
            ?: "Опція $mainOptionId"
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        if (isPhotoCapture) {
            val photoContext = context ?: return
            val selectedItem = itemsUI.singleOrNull()
            if (selectedItem == null) {
                Toast.makeText(photoContext, "Оберіть одну вітрину.", Toast.LENGTH_LONG).show()
                return
            }
            makeShowcasePhoto(selectedItem, photoContext)
            return
        }

        val stackPhotoDB = itemsUI
            .firstOrNull()
            ?.rawObj
            ?.firstOrNull { it is StackPhotoDB } as? StackPhotoDB

        if (stackPhotoDB == null) {
            logShowcaseDiagnosticOnce(
                key = "onSelectedItemsUI_empty_${contextUI}_${itemsUI.size}",
                level = "ERROR",
                stage = "onSelectedItemsUI",
                message = "Cannot apply showcase selection: StackPhotoDB not found. contextUI=$contextUI, itemsSize=${itemsUI.size}"
            )
            return
        }

        stackPhotoDB.let {
            when (contextUI) {
                ContextUI.SHOWCASE -> {
                    val dataHolder = VizitShowcaseDataHolder.getInstance()
                    dataHolder[planogrammId.value].showcaseId =
                        it.showcase_id?.toIntOrNull()
                            ?: it.showcaseId
                                .takeIf { showcaseId -> showcaseId > 0 }
                            ?: 0
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

    private fun logShowcaseDiagnosticOnce(
        key: String,
        level: String,
        stage: String,
        message: String,
        error: Throwable? = null
    ) {
        val normalizedKey = "$stage|$key"
        if (!loggedShowcaseDiagnostics.add(normalizedKey)) return

        val errorMessage = error?.let {
            ", error=${it::class.java.simpleName}: ${it.message}"
        }.orEmpty()

        Globals.writeToMLOG(
            level,
            "ShowcaseDBViewModel/$stage",
            "$message$errorMessage"
        )
    }

    private fun String?.previewForLog(limit: Int = 300): String {
        val value = this
            ?.replace('\n', ' ')
            ?.replace('\r', ' ')
            ?.trim()
            .orEmpty()

        return if (value.length <= limit) {
            value
        } else {
            value.take(limit) + "..."
        }
    }

    private fun Collection<String>.previewIds(limit: Int = 20): String {
        if (isEmpty()) return "[]"

        val result = take(limit).joinToString(
            prefix = "[",
            postfix = if (size > limit) ", ...]" else "]"
        )
        return result
    }

    private companion object {
        const val SHOWCASE_CONTROL_OPTION_ID = 160568
        const val SHOWCASE_NOT_ACTUAL_LOG_THEME_ID = 1403
        const val SHOWCASE_NOT_ACTUAL_MIN_COMMENT_LENGTH = 10
        const val SHOWCASE_DOSSIER_THEME_ID = 982L
        const val SHOWCASE_OPTIONAL_WORK_DAYS = 60L
        const val SECONDS_IN_DAY = 86_400L
        const val UNIX_1971_SEC = 31_536_000L
        val SHOWCASE_TYPES = listOf(0, 1, 2)
    }

}
