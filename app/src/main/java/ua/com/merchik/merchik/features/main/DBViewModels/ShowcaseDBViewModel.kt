package ua.com.merchik.merchik.features.main.DBViewModels

import MessageDialogData
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
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
import java.lang.ref.WeakReference
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

            val mainOptionFilter = if (contextUI == ContextUI.SHOWCASE_COMPLETED_CHECK) {
                buildMainOptionFilter(wpDataDB)
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
            Log.e("!!!!!", "err: ${e.message}")
        }
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
                        photo.mainOption = showcase?.mainOptionId ?: 0
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
                            photo.statusShowcase = showcase?.status ?: 0
                            photo.mainOption = showcase?.mainOptionId ?: 0

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

    override fun shouldOpenContextMenuOnCardClick(): Boolean = true

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

    override fun onContextMenuAction(event: ContextMenuActionEvent) {
        when (event.actionId) {
            ContextMenuActionIds.SHOWCASE_VIEW_PHOTO -> {
                hideContextMenu()
                contextForPhotoAction?.get()?.let { context ->
                    onClickItemImage(event.payload.firstItem, context)
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
                    title = "Відкрити",
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_eye)
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
        val gson = Gson()
        Log.d("ShowcaseDBViewModel", "WpDataDB: ${gson.toJson(wpDataDB)}")
        val mainOptionId = wpDataDB.main_option_id
            ?.trim()
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
            ?: return null

        Log.d("ShowcaseDBViewModel", "WpDataDB mainOptionId: $mainOptionId")

        val showcaseTypes = listOf(0, 1, 2)
        val hasMatchingShowcase = RoomManager.SQL_DB
            .showcaseDao()
            .getByDocTP(
                wpDataDB.client_id,
                wpDataDB.addr_id,
                showcaseTypes
            )
            .any { it.mainOptionId == mainOptionId }

//        if (!hasMatchingShowcase) {
//            return null
//        }

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
            mutableListOf(mainOptionId.toString()),
            mutableListOf(optionName),
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

    private companion object {
        const val SHOWCASE_NOT_ACTUAL_LOG_THEME_ID = 1403
        const val SHOWCASE_NOT_ACTUAL_MIN_COMMENT_LENGTH = 10
    }

}
