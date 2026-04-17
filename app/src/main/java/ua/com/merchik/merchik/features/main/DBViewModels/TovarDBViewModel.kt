package ua.com.merchik.merchik.features.main.DBViewModels

import CardItemsData
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import ua.com.merchik.merchik.Activities.DetailedReportActivity.RecycleViewDRAdapterTovar.ViewHolder.getArticle
import ua.com.merchik.merchik.Options.Options
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.HiddenMenuMode
import ua.com.merchik.merchik.dataLayer.LaunchOrigin
import ua.com.merchik.merchik.dataLayer.MainEvent
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.addrIdOrNull
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.model.ClickTextAction
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
import ua.com.merchik.merchik.dataLayer.model.SubmenuPresentation
import ua.com.merchik.merchik.dataLayer.model.addOrReplaceField
import ua.com.merchik.merchik.dataLayer.model.buildOptionCodeField
import ua.com.merchik.merchik.dataLayer.model.rawAs
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.dialogs.DialogPhotoTovar
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.maps.data.mappers.WpSelectionDataHolder
import javax.inject.Inject
import kotlin.reflect.KClass


private const val FIELD_OPTION_CODE = "option_code"

@HiltViewModel
class TovarDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = TovarDB::class


    override fun onClickProductCode(
        itemUI: DataItemUI,
        fieldValue: FieldValue,
        action: ClickTextAction,
        context: Context
    ) {
        super.onClickProductCode(itemUI, fieldValue, action, context)
        Log.e("!!!!!!!!!!!!","++++++++++++++++++++")
    }
    override fun getFieldsForCommentsImage(): List<String>? {
        return when (contextUI) {
            ContextUI.TOVAR_FROM_TOVAR_TABS -> ("nm, barcode" +
//                    ", manufacturerId" +
                    "").split(",").map { it.trim() }

            else -> null
        }
    }

    override fun getDefaultHideFieldsForCards(): List<String> {
        return ("manufacturer, ID").split(",").map { it.trim() }
    }

//    override fun onClickItem(itemUI: DataItemUI, context: Context) {
//        super.onClickItem(itemUI, context)
//        viewModelScope.launch {
//            Log.e("!!!!!","onClickItem+++++++++")
//            _events.emit(
//                MainEvent.ShowCardItemsDialog(
//                    cardItemsData = CardItemsData(
//                        dateItemUI = itemUI,
//                        title = "Товари"
//                    )
//                )
//            )
//
//        }
//
//    }

    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context) {
//        super.onClickItemImage(clickedDataItemUI, context)
        Log.e("!!!!!!!!!", "++++")
        val tovar =
            (clickedDataItemUI.rawObj.firstOrNull { it is TovarDB } as? TovarDB)
        tovar?.let {
            val tovId = tovar.getiD().toInt()

            val stackPhotoDB = RealmManager.getTovarPhotoByIdAndType(tovId, tovar.photoId, 18, true)

            if (stackPhotoDB != null)
                if (stackPhotoDB.getPhoto_num() != null && stackPhotoDB.getPhoto_num() != "") {
                    Log.e(
                        "ФОТО_ТОВАРОВ",
                        "displayFullSizeTovarPhotoDialog: " + Uri.parse(stackPhotoDB.getPhoto_num())
                    )

                    val dialogPhotoTovar = DialogPhotoTovar(context)

                    dialogPhotoTovar.setPhotoTovar(Uri.parse(stackPhotoDB.getPhoto_num()))

                    val sb = StringBuilder()
                    sb.append("Штрихкод: ").append(tovar.barcode).append("\n")
                    sb.append("Артикул: ").append(getArticle(tovar, 0))

                    dialogPhotoTovar.setPhotoBarcode(tovar.barcode)
                    dialogPhotoTovar.setTextInfo(sb)

                    dialogPhotoTovar.setClose { dialogPhotoTovar.dismiss() }
                    dialogPhotoTovar.show()

                    Log.e("ФОТО_ТОВАРОВ", "Вроде отобразил диалог")
                } else
                    super.onClickItemImage(clickedDataItemUI, context)
        }
    }

    override fun updateFilters() {
        val codeDad2 =
            Gson().fromJson(dataJson, JSONObject::class.java).getString("codeDad2").toLong()
        val data = RealmManager.getTovarListFromReportPrepareByDad2Copy(codeDad2)
        val filterTovarDB = ItemFilter(
            "Доп. фильтр",
            TovarDB::class,
            TovarDBViewModel::class,
            ModeUI.MULTI_SELECT,
            "Товар",
            "subTitle",
            "iD",
            "iD",
            data.map { it.getiD() },
            data.map { it.nm },
            false
        )

        val clientId = Gson().fromJson(dataJson, JSONObject::class.java).getString("clientId")
        val client = CustomerRealm.getCustomerById(clientId)
        val filterCustomerSDB = ItemFilter(
            "Клиент",
            CustomerSDB::class,
            CustomerSDBViewModel::class,
            ModeUI.MULTI_SELECT,
            "title",
            "subTitle",
            "client_id",
            "id",
            mutableListOf(client.id),
            mutableListOf(client.nm),
            false
        )

        filters = Filters(
            rangeDataByKey = null,
            searchText = "",
            items = mutableListOf(
                filterTovarDB,
                filterCustomerSDB
            )
        )

    }

//    override suspend fun getItems(): List<DataItemUI> {
//        return try {
//            val codeDad2 =
//                Gson().fromJson(dataJson, JSONObject::class.java).getString("codeDad2").toLong()
//            val data = RealmManager.getTovarListFromReportPrepareByDad2Copy(codeDad2)
//            repository.toItemUIList(TovarDB::class, data, contextUI, 18)
//                .map {
//                    when (contextUI) {
//                        ContextUI.TOVAR_FROM_ACHIEVEMENT -> {
//                            val selected =
//                                (it.rawObj.firstOrNull { it is TovarDB } as? TovarDB)?.getiD()
//                                    ?.toIntOrNull() == AchievementDataHolder.instance().tovarId
//                            it.copy(selected = selected)
//                        }
//
//                        ContextUI.DEFAULT -> {
//                            val selected = FilteringDialogDataHolder.instance()
//                                .filters
//                                ?.items
//                                ?.firstOrNull { it.clazz == table }
//                                ?.rightValuesRaw
//                                ?.contains((it.rawObj.firstOrNull { it is TovarDB } as? TovarDB)?.getiD()
//                                    .toString())
//                            it.copy(selected = selected == true)
//                        }
//
//                        else -> {
//                            it
//                        }
//                    }
//                }
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }

    override suspend fun getItems(): List<DataItemUI> {
        return try {
            val codeDad2 =
                Gson().fromJson(dataJson, JSONObject::class.java)
                    .getString("codeDad2")
                    .toLong()

            val tovarList = RealmManager.getTovarListFromReportPrepareByDad2Copy(codeDad2)

            // Базовые item'ы как и раньше
            val baseItems = repository.toItemUIList(
                TovarDB::class,
                tovarList,
                contextUI,
                18
            )

            // optionsList у тебя сейчас выбирается только по dad2, без tovarId
            // значит грузим один раз
            val optionsList = RealmManager.getTovarOptionInReportPrepare(
                codeDad2.toString(),
                ""
            )

            val deletePromoOption = false // подставь своё реальное значение

            baseItems
                .map { item ->
                    val tovar = item.rawObj.firstOrNull { it is TovarDB } as? TovarDB
                        ?: return@map item

                    val tovarId = tovar.getiD()

                    val reportPrepare = RealmManager.getTovarReportPrepare(
                        codeDad2.toString(),
                        tovarId
                    )

                    val optionString = if (reportPrepare != null) {
                        Options().getOptionString(
                            optionsList,
                            reportPrepare,
                            deletePromoOption
                        )
                    } else {
                        ""
                    }

                    if (optionString.isBlank()) {
                        item
                    } else {
                        val optionField = buildOptionCodeField(
                            optionHtmlOrText = optionString,
                            key = FIELD_OPTION_CODE,
                            title = "Шифр",
                            actionId = "open_option_code"
                        )

                        item.addOrReplaceField(optionField)
                    }
                }
                .map {
                    when (contextUI) {
                        ContextUI.TOVAR_FROM_ACHIEVEMENT -> {
                            val selected =
                                (it.rawObj.firstOrNull { raw -> raw is TovarDB } as? TovarDB)
                                    ?.getiD()
                                    ?.toIntOrNull() == AchievementDataHolder.instance().tovarId
                            it.copy(selected = selected)
                        }

                        ContextUI.DEFAULT -> {
                            val selected = FilteringDialogDataHolder.instance()
                                .filters
                                ?.items
                                ?.firstOrNull { filterItem -> filterItem.clazz == table }
                                ?.rightValuesRaw
                                ?.contains(
                                    (it.rawObj.firstOrNull { raw -> raw is TovarDB } as? TovarDB)
                                        ?.getiD()
                                        .toString()
                                )
                            it.copy(selected = selected == true)
                        }

                        else -> it
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        when (contextUI) {
            ContextUI.TOVAR_FROM_ACHIEVEMENT -> {
                (itemsUI.first().rawObj.firstOrNull { it is TovarDB } as? TovarDB)?.let {
                    AchievementDataHolder.instance().tovarId = it.getiD().toInt()
                    AchievementDataHolder.instance().tovarName = it.nm
                }
            }

            ContextUI.DEFAULT -> {
                FilteringDialogDataHolder.instance().filters.apply {
                    this?.let { filters ->
                        filters.items = filters.items.map { itemFilter ->
                            if (itemFilter.clazz == table) {
                                val rightValuesRaw = mutableListOf<String>()
                                val rightValuesUI = mutableListOf<String>()
                                itemsUI.forEach {
                                    (it.rawObj.firstOrNull() as? TovarDB)?.let {
                                        rightValuesRaw.add(it.getiD())
                                        rightValuesUI.add(it.nm)
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

            else -> {}
        }
        showServiceContextMenu(itemsUI)
    }


    private val hiddenMenuMode: HiddenMenuMode = HiddenMenuMode.INLINE

    private fun hiddenPresentation(): SubmenuPresentation =
        when (hiddenMenuMode) {
            HiddenMenuMode.INLINE -> SubmenuPresentation.INLINE_EXPAND
            HiddenMenuMode.OVERLAY -> SubmenuPresentation.OVERLAY
            HiddenMenuMode.REPLACE -> SubmenuPresentation.REPLACE
        }


//    private fun buildHeader(items: List<DataItemUI>): ContextMenuHeaderUi {
//        val item = items.first()
//
//        val wp = item.rawAs<WpDataDB>()
//        val theme = item.rawAs<ThemeDB>()
//
//        val clientText = item.fieldValueOrNull("client_txt")
//            ?: wp?.client_txt
//            ?: theme?.nm
//            ?: ""
//
//        val addressText = item.fieldValueOrNull("addr_txt")
//            ?: wp?.addr_txt
//            ?: ""
//
//        val rows = buildList {
//            if (clientText.isNotBlank()) {
//                add(
//                    ContextMenuHeaderRow(
//                        label = if (items.size > 1) "з ${items.size} елементiв" else "Клієнт",
//                        value = if (items.size > 1) "" else clientText
//                    )
//                )
//            }
//
//            if (addressText.isNotBlank()) {
//                add(
//                    ContextMenuHeaderRow(
//                        label = "За адресою",
//                        value = addressText
//                    )
//                )
//            }
//        }
//
//        return ContextMenuHeaderUi(
//            visible = rows.isNotEmpty(),
//            title = if (items.size > 1) {
//                "Виберіть дію для групи з ${items.size} вiзитiв"
//            } else {
//                "Виберіть дію для вiзиту"
//            },
//            rows = rows
//        )
//    }

    private fun buildHeader(items: List<DataItemUI>): ContextMenuHeaderUi {
        val groupingFields = uiState.value.groupingFields
            .filter { !it.key.isNullOrBlank() }
            .sortedBy { it.priority }

        val sortingFields = uiState.value.sortingFields
            .filter { !it.key.isNullOrBlank() }

        val groupingRows = buildList {
            groupingFields.forEach { groupingField ->
                val key = groupingField.key ?: return@forEach
                val sortingTitle = sortingFields
                    .firstOrNull { it.key == key }
                    ?.title

                var label = groupingField.title?.takeIf { it.isNotBlank() } ?: key
                if (label == "dt")
                    label = "дата"
                if (label == "addr_txt")
                    label = "адреса"
//                val label = sortingTitle?.takeIf { it.isNotBlank() }
//                    ?: groupingField.title?.takeIf { it.isNotBlank() }
//                    ?: "key"

                val values = items
                    .mapNotNull { it.fieldDisplayValueOrNull(key) }
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()


                if (values.isNotEmpty()) {
                    val value = when {
                        values.size == 1 -> values.first()
                        values.size <= 3 -> values.joinToString(", ")
                        else -> values.take(3).joinToString(", ") + " …"
                    }
                    add(
                        ContextMenuHeaderRow(
                            label = "",
                            value = value
                        )
                    )
                }
            }
        }

        val rows = groupingRows.ifEmpty {
            val item = items.first()

            val wp = item.rawAs<WpDataDB>()
            val theme = item.rawAs<ThemeDB>()

            val clientText = item.fieldDisplayValueOrNull("client_txt")
                ?: wp?.client_txt
                ?: theme?.nm
                ?: ""

            val addressText = item.fieldDisplayValueOrNull("addr_txt")
                ?: wp?.addr_txt
                ?: ""

            buildList {
                if (clientText.isNotBlank()) {
                    add(
                        ContextMenuHeaderRow(
                            label = if (items.size > 1) "з ${items.size} елементiв" else "Клієнт",
                            value = if (items.size > 1) "" else clientText
                        )
                    )
                }

                if (addressText.isNotBlank()) {
                    add(
                        ContextMenuHeaderRow(
                            label = "За адресою",
                            value = addressText
                        )
                    )
                }
            }
        }

        return ContextMenuHeaderUi(
            visible = true,
            title = if (items.size > 1) {
                "Виберіть дію для групи з ${items.size} товарiв"
            } else {
                "Виберіть дію для товару"
            },
            rows = rows
        )
    }


    private fun buildEntries(payload: ContextMenuPayload): List<ContextMenuEntry> {
        val isActiveGrouped = uiState.value.groupingFields.isNotEmpty()

        val items = payload.selectedItems
        val first = items.first()

        val payloadIds = items.map { it.stableId }.toSet()
        val itemsTemp = uiState.value.items

        val allItems =
            filterAndSortDataItems(
                items = itemsTemp,
                filters = uiState.value.filters,
                sortingFields = uiState.value.sortingFields,
                groupingFields = uiState.value.groupingFields,
                rangeStart = rangeDataStart.value,
                rangeEnd = rangeDataEnd.value,
                searchText = uiState.value.filters?.searchText
            ).items


        val selectedInPayload = allItems.filter { it.stableId in payloadIds && it.selected }
        val unselectedInPayload = allItems.filter { it.stableId in payloadIds && !it.selected }

        val selectedGlobal = allItems.filter { it.selected }
        val unselectedGlobal = allItems.filter { !it.selected }

        val selectedInPayloadCount = selectedInPayload.size
        val unselectedInPayloadCount = unselectedInPayload.size

        val selectedGlobalCount = selectedGlobal.size
        val unselectedGlobalCount = unselectedGlobal.size

        val wp = first.rawAs<WpDataDB>()
        val isMulti = items.size > 1

        val canEdit = !isMulti && wp != null

        val markTitle = if (isMulti) {
            "Позначити ($unselectedInPayloadCount)"
        } else {
            "Позначити"
        }

        val unmarkTitle = if (isMulti) {
            "Зняти позначку ($selectedInPayloadCount)"
        } else {
            "Зняти позначку"
        }

        val markAllTitle = "Позначити усі ($unselectedGlobalCount)"
        val unmarkAllTitle = "Зняти всi позначки ($selectedGlobalCount)"

        val selectionEntries = listOf(
            ContextMenuPresets.Mark.toEntry(
                id = "selection_mark",
                title = markTitle,
                enabled = unselectedInPayloadCount > 0
            ),
            ContextMenuPresets.MarkAll.toEntry(
                id = "selection_mark_all",
                title = markAllTitle,
                enabled = unselectedGlobalCount > 0
            ),
            ContextMenuPresets.Unmark.toEntry(
                id = "selection_unmark",
                title = unmarkTitle,
                enabled = selectedInPayloadCount > 0
            ),
            ContextMenuPresets.UnmarkAll.toEntry(
                id = "selection_unmark_all",
                title = unmarkAllTitle,
                enabled = selectedGlobalCount > 0
            ),
            ContextMenuPresets.Invert.toEntry(
                id = "selection_invert",
                enabled = payloadIds.isNotEmpty()
            )
        )

        val serviceEntries = buildServiceEntries(payload)

        val listEntries = listOf(
            ContextMenuPresets.CollapseList.toEntry(
                id = "list_collapse"
            ),
            ContextMenuPresets.ExpandList.toEntry(
                id = "list_expand"
            )
        )

        return buildList {
            add(
                ContextMenuPresets.Edit.toEntry(
                    id = "root_edit",
                    title = "Змiнити/Вiдкрити",
                    enabled = canEdit
                )
            )

            add(ContextMenuEntry.Divider("divider_main_1"))

            add(
                ContextMenuEntry.Submenu(
                    id = "submenu_selection",
                    title = "Позначення элементiв",
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_multiple_select),
                    items = selectionEntries,
                    presentation = hiddenPresentation(),
                    expandedByDefault = false
                )
            )

            add(ContextMenuEntry.Divider("divider_main_3"))

            if (!isActiveGrouped) {
                add(
                    ContextMenuPresets.ListSettings.toEntry(
                        id = "list_settings"
                    )
                )
            } else {
                add(
                    ContextMenuEntry.Submenu(
                        id = "submenu_list",
                        title = "Групування",
                        leading = MenuLeading.DrawableIcon(R.drawable.ic_sort_down),
                        items = listEntries,
                        presentation = hiddenPresentation(),
                        expandedByDefault = false
                    )
                )
            }

            add(ContextMenuEntry.Divider("divider_main_2"))

            if (serviceEntries.isNotEmpty()) {
                add(
                    ContextMenuEntry.Submenu(
                        id = "submenu_service",
                        title = "Обрати дію",
                        leading = MenuLeading.DrawableIcon(R.drawable.ic_21),
                        items = buildServiceMenuEntries(payload),
                        presentation = SubmenuPresentation.OVERLAY,
                        expandedByDefault = false
                    )
                )
            } else {
                add(
                    ContextMenuPresets.AdditionalWorkAdd.toEntry()
                )
            }

            add(ContextMenuEntry.Divider("divider_main_4"))

            add(
                ContextMenuPresets.Close.toEntry(
                    id = "root_close"
                )
            )
        }
    }

    private fun buildServiceMenuEntries(payload: ContextMenuPayload): List<ContextMenuEntry> {
        val serviceEntries = buildServiceEntries(payload)

        if (serviceEntries.isEmpty()) {
            return listOf(
                ContextMenuPresets.Close.toEntry(
                    id = "service_root_close"
                )
            )
        }

        return buildList {
            addAll(serviceEntries)
            add(ContextMenuEntry.Divider("service_root_divider_close"))
            add(
                ContextMenuPresets.Close.toEntry(
                    id = "service_root_close"
                )
            )
        }
    }

    private fun buildServiceEntries(payload: ContextMenuPayload): List<ContextMenuEntry> {
        val items = payload.selectedItems
        val selectedItems = items.filter { it.selected }
        val selectedCount = selectedItems.size
        val hasSelectedInClickedCard = selectedCount > 0

        return when (contextUI) {
            ContextUI.WP_DATA_IN_CONTAINER -> listOf(
                ContextMenuPresets.RejectWork.toEntry(
                    id = "service_reject_work",
                    title = "Відмовитися від роботи ($selectedCount)",
                    enabled = hasSelectedInClickedCard,
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_hide)
                ),
                ContextMenuPresets.RejectClient.toEntry(
                    id = "service_reject_client",
                    title = "Відмовитися від клієнта",
                    enabled = hasSelectedInClickedCard,
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_hide)
                ),
                ContextMenuPresets.RejectAddress.toEntry(
                    id = "service_reject_address",
                    title = "Відмовитися від адреси",
                    enabled = hasSelectedInClickedCard,
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_hide)
                ),
                ContextMenuPresets.AskMoreMoney.toEntry(
                    id = "service_ask_more_money",
                    title = "Запросити більшу оплату",
                    enabled = hasSelectedInClickedCard,
                    leading = MenuLeading.DrawableIcon(R.drawable.ic_money)
                )
            )

            ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER -> listOf(
                ContextMenuPresets.AdditionalWorkAdd.toEntry(
                    id = "service_apply_request",
                    title = "Подати заявку ($selectedCount)",
                    enabled = hasSelectedInClickedCard
                ),
                ContextMenuPresets.AdditionalWorkDellAddress.toEntry(
                    id = "service_hide_address",
                    title = "Не показувати адресу",
                    enabled = hasSelectedInClickedCard
                ),
                ContextMenuPresets.AdditionalWorkDellClient.toEntry(
                    id = "service_hide_client",
                    title = "Не показувати клієнта",
                    enabled = hasSelectedInClickedCard
                ),
                ContextMenuEntry.Divider("service_divider_2"),
                ContextMenuPresets.OpenSmsPlan.toEntry(
                    id = "service_sms_plan"
                )
            )

            else -> emptyList()
        }
    }

    override fun onContextMenuAction(event: ContextMenuActionEvent) {
        when (event.actionId) {
            ContextMenuActionIds.EDIT -> {
                val wp = event.payload.firstItem.rawAs<WpDataDB>() ?: return
                openDetailedReport(wp.id)
            }

            ContextMenuActionIds.OPEN_ORDER -> {
                val wp = event.payload.firstItem.rawAs<WpDataDB>() ?: return
                openDetailedReport(wp.id)
            }

            ContextMenuActionIds.OPEN_UFMD_SELECTOR -> {
                openUFMDSelector(
                    addressId = event.payload.firstItem.addrIdOrNull(),
                    origin = event.payload.origin
                )
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
//                val targetIds = event.payload.items.map { it.stableId }.toSet()

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

            ContextMenuActionIds.OPEN_SMS_PLAN -> {
                openSmsPlanDirectory()
            }

            ContextMenuActionIds.LIST_SETTINGS -> {
                openSortingDialog()
            }

            ContextMenuActionIds.CLOSE -> {
                hideContextMenu()
            }

            ContextMenuActionIds.ADDITIONAL_ADD -> {
                openAdditionalWorkDialog()
            }
        }
    }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        super.onClickItem(itemUI, context)

        showItemContextMenu(
            items = listOf(itemUI)
        )
    }

    override fun onLongClickItem(itemUI: DataItemUI, context: Context) {
        showItemContextMenu(
            items = listOf(itemUI)
        )
    }

    override fun onLongClickItems(
        items: List<DataItemUI>,
        context: Context,
        clickedItem: DataItemUI
    ) {
        showItemContextMenu(
            items = items
        )
    }

    private fun showItemContextMenu(
        items: List<DataItemUI>,
        origin: LaunchOrigin? = null,
        deckId: String? = null
    ) {
        if (items.isEmpty()) return

        val payload = ContextMenuPayload(
            selectedItems = items,
            deckId = deckId,
            origin = origin
        )

        showContextMenu(
            ContextMenuUiState(
                payload = payload,
                header = buildHeader(items),
                entries = buildEntries(payload)
            )
        )
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

    private fun showServiceContextMenu(
        items: List<DataItemUI>,
        origin: LaunchOrigin? = null,
        deckId: String? = null
    ) {
        if (items.isEmpty()) return

        val payload = ContextMenuPayload(
            selectedItems = items,
            deckId = deckId,
            origin = origin
        )

        showContextMenu(
            ContextMenuUiState(
                payload = payload,
                header = buildHeader(items),
                entries = buildServiceMenuEntries(payload)
            )
        )
    }

}


