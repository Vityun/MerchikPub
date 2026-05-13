package ua.com.merchik.merchik.features.main.DBViewModels

import MessageDialogData
import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites
import ua.com.merchik.merchik.Activities.DetailedReportActivity.RecycleViewDRAdapterTovar
import ua.com.merchik.merchik.Activities.DetailedReportActivity.RecycleViewDRAdapterTovar.ViewHolder.getArticle
import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.Globals.OptionControlName
import ua.com.merchik.merchik.Options.Options
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.ServerExchange.Exchange
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading
import ua.com.merchik.merchik.Utils.MySimpleExpandableListAdapter
import ua.com.merchik.merchik.ViewHolders.Clicks
import ua.com.merchik.merchik.ViewHolders.Clicks.clickVoid
import ua.com.merchik.merchik.ViewHolders.TovarTabsAddNewHolder
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.PhotoDescriptionText
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB
import ua.com.merchik.merchik.data.RealmModels.ErrorDB
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportHint
import ua.com.merchik.merchik.data.TovarOptions
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.HiddenMenuMode
import ua.com.merchik.merchik.dataLayer.LaunchOrigin
import ua.com.merchik.merchik.dataLayer.MainEvent
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.getDefaultImageCommentValueModifier
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
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.dataLayer.model.addOrReplaceField
import ua.com.merchik.merchik.dataLayer.model.buildOptionCodeField
import ua.com.merchik.merchik.dataLayer.model.rawAs
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm
import ua.com.merchik.merchik.database.realm.tables.PPADBRealm
import ua.com.merchik.merchik.database.realm.tables.PromoRealm
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.AdditionalRequirementsAdapter
import ua.com.merchik.merchik.dialogs.DialogData
import ua.com.merchik.merchik.dialogs.DialogData.DialogClickListener
import ua.com.merchik.merchik.dialogs.DialogFilter.Click
import ua.com.merchik.merchik.dialogs.DialogPhotoTovar
import ua.com.merchik.merchik.dialogs.features.LoadingDialogWithPercent
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.InlineChoiceUi
import ua.com.merchik.merchik.features.main.Main.InlineEditorKind
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.ProductCodeEditorMode
import ua.com.merchik.merchik.features.main.Main.ProductCodeEditorRowUi
import ua.com.merchik.merchik.features.main.Main.ProductCodeEditorState
import ua.com.merchik.merchik.features.main.Main.launchFeaturesActivity
import ua.com.merchik.merchik.features.main.componentsUI.TovarPhotoDialogUiState
import ua.com.merchik.merchik.features.main.componentsUI.TovarPhotoQuality
import ua.com.merchik.merchik.retrofit.RetrofitBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.Optional
import java.util.function.Consumer
import javax.inject.Inject
import kotlin.reflect.KClass


private const val FIELD_OPTION_CODE = "option_code"

enum class AkciyaPresence {
    HAS,
    NONE,
    UNSET
}

@HiltViewModel
class TovarDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    private data class ProductCodeSessionCache(
        val codeDad2: String,
        val optionsList2: MutableList<OptionsDB>?,
        val allTovarOptions: List<TovarOptions>,
        val reportPrepareByTovarId: MutableMap<String, ReportPrepareDB?> = mutableMapOf(),
        val rowsByItemAndMode: MutableMap<Pair<Long, ProductCodeEditorMode>, List<ProductCodeEditorRowUi>> = mutableMapOf()
    )

    private var productCodeSessionCache: ProductCodeSessionCache? = null

    override val table: KClass<out DataObjectUI>
        get() = TovarDB::class

    private fun getOrCreateProductCodeSessionCache(): ProductCodeSessionCache {
        val codeDad2 = getCodeDad2String()

        val current = productCodeSessionCache
        if (current != null && current.codeDad2 == codeDad2) {
            return current
        }

        val newCache = ProductCodeSessionCache(
            codeDad2 = codeDad2,
            optionsList2 = RealmManager.getTovarOptionInReportPrepare(codeDad2, null),
            allTovarOptions = Options.getTovarOptins()
        )
        productCodeSessionCache = newCache
        return newCache
    }

    private fun afterTovarRequisitesSaved(tovars: List<TovarDB>) {
        if (tovars.isEmpty()) return

        val savedIds = tovars
            .map { it.getiD() }
            .filter { it.isNotBlank() }
            .toSet()

        val affectedItems = getAllCurrentItems().filter { item ->
            val tovarId = item.rawAs<TovarDB>()?.getiD()
            tovarId in savedIds
        }

        affectedItems.forEach { item ->
            val tovar = item.rawAs<TovarDB>() ?: return@forEach

            invalidateProductCodeItemCache(
                stableId = item.stableId,
                tovarId = tovar.getiD()
            )

            refreshAllProductCodeRowsForItem(item.stableId)
            refreshOptionCodeForItem(item.stableId)
        }
    }

    private fun refreshAllProductCodeRowsForItem(itemId: Long) {
        val current = productCodeEditorState.value
        if (!current.expanded) return

        val rows = current.rowsByItemId[itemId].orEmpty()
        if (rows.isEmpty()) return

        val tovar = findTovarByStableId(itemId) ?: return
        val codeDad2 = getCodeDad2String()
        val rp = RealmManager.getTovarReportPrepare(codeDad2, tovar.getiD())

        val refreshedRows = rows.map { row ->
            when (row.option.getOptionControlName()) {
                OptionControlName.AKCIYA_ID -> {
                    row.copy(
                        value = getCurrentData(row.option, rp = rp).orEmpty(),
                        value2 = rp?.getAkciya().orEmpty()
                    )
                }

                OptionControlName.ERROR_ID -> {
                    row.copy(
                        value = getCurrentData(row.option, rp = rp).orEmpty(),
                        value2 = rp?.getErrorComment().orEmpty()
                    )
                }

                else -> {
                    row.copy(
                        value = getCurrentData(row.option, rp = rp).orEmpty()
                    )
                }
            }
        }

        val updatedMap = current.rowsByItemId.toMutableMap()
        updatedMap[itemId] = refreshedRows

        setProductCodeEditor(
            current.copy(rowsByItemId = updatedMap)
        )
    }

    private fun getCachedReportPrepare(
        codeDad2: String,
        tovarId: String
    ): ReportPrepareDB? {
        val cache = getOrCreateProductCodeSessionCache()
        return cache.reportPrepareByTovarId.getOrPut(tovarId) {
            RealmManager.getTovarReportPrepare(codeDad2, tovarId)
        }
    }

    private fun invalidateProductCodeItemCache(
        stableId: Long,
        tovarId: String?
    ) {
        productCodeSessionCache?.rowsByItemAndMode?.keys
            ?.filter { it.first == stableId }
            ?.toList()
            ?.forEach { key ->
                productCodeSessionCache?.rowsByItemAndMode?.remove(key)
            }

        if (!tovarId.isNullOrBlank()) {
            productCodeSessionCache?.reportPrepareByTovarId?.remove(tovarId)
        }
    }

    private fun clearProductCodeSessionCache() {
        productCodeSessionCache = null
    }

    override fun onClickProductCode(
        itemUI: DataItemUI,
        fieldValue: FieldValue,
        action: ClickTextAction,
        context: Context
    ) {
        super.onClickProductCode(itemUI, fieldValue, action, context)
        if (contextUI == ContextUI.TOVAR_FROM_TOVAR_TABS) {
            openProductCodeEditor(
                itemUI = itemUI,
                fieldValue = fieldValue,
                action = action,
                mode = ProductCodeEditorMode.REQUIRED
            )
        }
    }

    override fun onLongClickProductCode(
        itemUI: DataItemUI,
        fieldValue: FieldValue,
        action: ClickTextAction,
        context: Context
    ) {

        if (contextUI == ContextUI.TOVAR_FROM_TOVAR_TABS) {
            openProductCodeEditor(
                itemUI = itemUI,
                fieldValue = fieldValue,
                action = action,
                mode = ProductCodeEditorMode.ALL
            )
        }
    }

    override fun getFieldsForCommentsImage(): List<String>? {
        return when (contextUI) {
            ContextUI.TOVAR_FROM_TOVAR_TABS -> ("nm" +
//                    ", manufacturerId" +
                    "").split(",").map { it.trim() }

            else -> null
        }
    }

    override fun persistInlineRowValue(
        itemId: Long,
        row: ProductCodeEditorRowUi,
        newValue: String,
        newValue2: String?
    ): Boolean {
        return try {
            val tovar = findTovarByStableId(itemId) ?: return false
            val codeDad2 = getCodeDad2String()
            val wpDataDB = RealmManager.getWorkPlanRowByCodeDad2(codeDad2.toLong()) ?: return false
            val rp = getCachedReportPrepare(codeDad2, tovar.getiD())

            operetionSaveRPToDB(
                tpl = row.option,
                rp = rp,
                data = newValue,
                data2 = newValue2 ?: row.value2,
                tovarId = tovar.getiD(),
                wpDataDB = wpDataDB
            )

            invalidateProductCodeItemCache(
                stableId = itemId,
                tovarId = tovar.getiD()
            )

            true
        } catch (e: Exception) {
            Globals.writeToMLOG(
                "ERROR",
                "TovarDBViewModel.persistInlineRowValue",
                "Exception: $e"
            )
            false
        }
    }

    override fun getDefaultSortUserFields(): List<String>? {
        return "group_id".split(",")
//        return "nm, manufacturer_id, weight, option_code, barcode".split(",")
    }

    override fun getDefaultGroupUserFields(): List<String> {
        return listOf(
                "group_id",        // 1-й уровень группировки
//                "addr_txt",  // 2-й уровень (если включишь вторую группировку)
                //            "client_txt" // 3-й уровень (опционально)
            )
    }

    override fun getDefaultHideUserFields(): List<String>? {
        return "column_name, expire_period, group_header".split(",")
    }


    override fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context) {
//        super.onClickItemImage(clickedDataItemUI, context)
        Log.e("!!!!!!!!!", "++++")
        if (contextUI == ContextUI.TOVAR_FROM_TOVAR_TABS) {
            val tovar = clickedDataItemUI.rawAs<TovarDB>()
            if (tovar != null) {
                openTovarPhotoDialog(tovar)
                return
            }
        } else {
            val tovar =
                (clickedDataItemUI.rawObj.firstOrNull { it is TovarDB } as? TovarDB)
            tovar?.let {
                val tovId = tovar.getiD().toInt()

                val stackPhotoDB =
                    RealmManager.getTovarPhotoByIdAndType(tovId, tovar.photoId, 18, true)

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
    }

    override fun updateFilters() {
//        val codeDad2 =
//            Gson().fromJson(dataJson, JSONObject::class.java).getString("codeDad2").toLong()
//        val data = RealmManager.getTovarListFromReportPrepareByDad2Copy(codeDad2)
//        val filterTovarDB = ItemFilter(
//            "Доп. фильтр",
//            TovarDB::class,
//            TovarDBViewModel::class,
//            ModeUI.MULTI_SELECT,
//            "Товар",
//            "subTitle",
//            "iD",
//            "iD",
//            data.map { it.getiD() },
//            data.map { it.nm },
//            false
//        )

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

//        if (contextUI == ContextUI.TOVAR_FROM_TOVAR_TABS_ADD_NEW)
//            filters = Filters(
//                rangeDataByKey = null,
//                searchText = "",
//                items = mutableListOf(
//                    filterCustomerSDB
//                )
//            )
//        else
            filters = Filters(
                rangeDataByKey = null,
                searchText = "",
                items = mutableListOf(
//                    filterTovarDB,
                    filterCustomerSDB
                )
            )

    }


    override suspend fun getItems(): List<DataItemUI> {
        return try {
            if (contextUI == ContextUI.TOVAR_FROM_TOVAR_TABS_ADD_NEW ||
                contextUI == ContextUI.TOVAR_FROM_TOVAR_TABS_ADD_ALL) {
                val clientId = Gson().fromJson(dataJson, JSONObject::class.java)
                    .getString("clientId")

                val codeDad2 = Gson().fromJson(dataJson, JSONObject::class.java)
                    .getString("codeDad2")
                    .toLong()

                val baseTovars = RealmManager.getTovarListFromReportPrepareByDad2Copy(codeDad2)
                val baseIds = baseTovars
                    .mapNotNull { it.getiD() }
                    .toSet()

                val allClientTovars = RealmManager.INSTANCE
                    .copyFromRealm(RealmManager.getTovarListByCustomer(clientId))

                val tovarList = allClientTovars
                    .filterNot { it.getiD() in baseIds }

                val baseItems = repository.toItemUIList(
                    TovarDB::class,
                    tovarList,
                    contextUI,
                    18
                )

                return baseItems.map {
                    if (contextUI == ContextUI.TOVAR_FROM_TOVAR_TABS_ADD_NEW) {
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
                    } else {
                        it.copy(selected = true)
                    }
                }
            } else {
                applyPendingAddedTovarsIfNeeded()
                val codeDad2 = Gson().fromJson(dataJson, JSONObject::class.java)
                    .getString("codeDad2")
                    .toLong()

                val wpDataDB = RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                val cache = getOrCreateProductCodeSessionCache()
                val baseTovars = RealmManager.getTovarListFromReportPrepareByDad2Copy(codeDad2)
                val tovarList = mergeBaseAndManualTovars(baseTovars)
                val manuallyAddedIds = getManuallyAddedIds()
//            val tovarList = RealmManager.getTovarListFromReportPrepareByDad2Copy(codeDad2)

                val baseItems = repository.toItemUIList(
                    TovarDB::class,
                    tovarList,
                    contextUI,
                    18
                )

                val optionsList = cache.optionsList2 ?: mutableListOf()
                val deletePromoOption = false

                baseItems
                    .map { item ->
                        when (contextUI) {

                            ContextUI.TOVAR_FROM_TOVAR_TABS -> {
                                val tovar = item.rawObj.firstOrNull { it is TovarDB } as? TovarDB
                                    ?: return@map item

                                val tovarId = tovar.getiD()
                                val reportPrepare = getCachedReportPrepare(cache.codeDad2, tovarId) ?:
                                createNewRPRow(tovarId = tovarId,
                                    wpDataDB = wpDataDB)

                                val optionString =
                                    Options().getOptionString(
                                        optionsList,
                                        reportPrepare,
                                        deletePromoOption
                                    )



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

                            else -> item
                        }
                    }
                    .map { itemUI ->
                        val tovar = itemUI.rawAs<TovarDB>() ?: return@map itemUI

                        val rp = ReportPrepareRealm.getReportPrepareByTov(
                            codeDad2.toString(),
                            tovar.getiD()
                        )

                        val commentField = buildTovarBalanceImageCommentField(rp)

                        itemUI.addOrReplaceImageCommentField(commentField)
                    }
                    .map { item ->
                        when (contextUI) {
                            ContextUI.TOVAR_FROM_TOVAR_TABS -> {
                                val tovarId =
                                    (item.rawObj.firstOrNull { raw -> raw is TovarDB } as? TovarDB)
                                        ?.getiD()
                                        .orEmpty()

                                if (tovarId in manuallyAddedIds) {
                                    item.copy(selected = true)
                                } else {
                                    item
                                }
                            }

                            else -> item
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
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        showServiceContextMenu(itemsUI)

        when (contextUI) {
            ContextUI.TOVAR_FROM_TOVAR_TABS_ADD_NEW,
            ContextUI.TOVAR_FROM_TOVAR_TABS_ADD_ALL -> {
//                FilteringDialogDataHolder.instance().filters?.let { filters ->
//                    filters.items = filters.items.map { itemFilter ->
//                        if (itemFilter.clazz == table) {
//                            val rightValuesRaw = mutableListOf<String>()
//                            val rightValuesUI = mutableListOf<String>()
//
//                            itemsUI.forEach { item ->
//                                (item.rawObj.firstOrNull() as? TovarDB)?.let { tovar ->
//                                    rightValuesRaw.add(tovar.getiD())
//                                    rightValuesUI.add(tovar.nm)
//                                }
//                            }
//
//                            itemFilter.copy(
//                                rightValuesRaw = rightValuesRaw,
//                                rightValuesUI = rightValuesUI
//                            )
//                        } else {
//                            itemFilter
//                        }
//                    }
//                }
                val selectedIds = itemsUI.mapNotNull { item ->
                    (item.rawObj.firstOrNull() as? TovarDB)?.getiD()
                }

                TovarTabsAddNewHolder.set(selectedIds)
                return
            }

            ContextUI.TOVAR_FROM_ACHIEVEMENT -> {
                (itemsUI.firstOrNull()?.rawObj?.firstOrNull { it is TovarDB } as? TovarDB)?.let {
                    AchievementDataHolder.instance().tovarId = it.getiD().toInt()
                    AchievementDataHolder.instance().tovarName = it.nm
                }
                return
            }

            ContextUI.DEFAULT -> {
                FilteringDialogDataHolder.instance().filters?.let { filters ->
                    filters.items = filters.items.map { itemFilter ->
                        if (itemFilter.clazz == table) {
                            val rightValuesRaw = mutableListOf<String>()
                            val rightValuesUI = mutableListOf<String>()

                            itemsUI.forEach { item ->
                                (item.rawObj.firstOrNull() as? TovarDB)?.let { tovar ->
                                    rightValuesRaw.add(tovar.getiD())
                                    rightValuesUI.add(tovar.nm)
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
                return
            }

            else -> return
        }
    }

    override fun onClickImageComment(
        itemUI: DataItemUI,
        fieldValue: FieldValue,
        context: Context
    ) {
        if (contextUI != ContextUI.TOVAR_FROM_TOVAR_TABS) return
        Log.e("!!!!!!!!!!!!!!!","++++++++++++++++++++++++++")
        if (!fieldValue.key.equals("tovar_image_balance_comment", ignoreCase = true)) {
            return
        }

        val tovar = itemUI.rawAs<TovarDB>() ?: return

        showTovarBalanceDialog(
            context = context,
            tovar = tovar
        )
    }

    private fun showTovarBalanceDialog(
        context: Context,
        tovar: TovarDB
    ) {
        try {
            val codeDad2 = getCodeDad2String()
            val wpDataDB = RealmManager.getWorkPlanRowByCodeDad2(codeDad2.toLong()) ?: return

            val addressId = wpDataDB.addr_id
            val clientId = wpDataDB.client_id

            val rp = getCachedReportPrepare(
                codeDad2 = codeDad2,
                tovarId = tovar.getiD()
            )

            val finalBalanceData = rp
                ?.getOborotvedNum()
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: "0"

            val finalBalanceDate = formatBalanceDateForDialog(rp)

            val isOldBalance = isBalanceOld(rp)

            val oborotVed = buildOborotVedText(
                tovar = tovar,
                addressId = addressId
            )

            val stringBuilder = SpannableStringBuilder()

            val addressName = runCatching {
                RoomManager.SQL_DB.addressDao().getById(addressId)?.nm
            }.getOrNull().orEmpty()

            val clientName = runCatching {
                RoomManager.SQL_DB.customerDao().getById(clientId)?.nm
            }.getOrNull().orEmpty()

            appendHtml(stringBuilder, "<b>Адрес: </b>$addressName<br>")
            appendHtml(stringBuilder, "<b>Клиент: </b>$clientName<br>")

            appendHtml(stringBuilder, "<b>Код товара: </b>${tovar.getiD()}<br>")
            appendHtml(stringBuilder, "<b>Товар: </b>${tovar.getNm()}<br>")
            appendHtml(stringBuilder, "<b>Штрихкод: </b>${tovar.getBarcode()}<br>")

            val article = getArticle(tovar, 0)
                ?.takeIf { it.isNotBlank() }
                ?: "(нет данных)"

            appendHtml(stringBuilder, "<b>Артикул: </b>$article<br>")

            if (isOldBalance) {
                appendHtml(
                    stringBuilder,
                    "<strong>Дата остатков: </strong><font color='#e6e6e6'>$finalBalanceDate</font><br>"
                )
                appendHtml(
                    stringBuilder,
                    "<b>Остаток: </b><font color='#e6e6e6'>Устарел</font><br><br>"
                )
                appendHtml(
                    stringBuilder,
                    "Данные об остатке <font color='#e6e6e6'>устарели</font>"
                )
            } else {
                appendHtml(
                    stringBuilder,
                    "<strong>Дата остатков: </strong><font color='#00A800'>$finalBalanceDate</font><br>"
                )
                appendHtml(
                    stringBuilder,
                    "<b>Остаток: </b><font color='#00A800'>$finalBalanceData</font> <b>шт</b><br><br>"
                )
                appendHtml(
                    stringBuilder,
                    "Данные об остатке <b><font color='#00A800'>актуальны</font></b>"
                )
            }

            stringBuilder.append("\n\n\n")
            stringBuilder.append(oborotVed)

            val dialog = DialogData(context)
            dialog.setTitle("Остатки товара в ТТ")
            dialog.setText(stringBuilder)
            dialog.show()

        } catch (e: Exception) {
            Globals.writeToMLOG(
                "ERROR",
                "TovarDBViewModel.showTovarBalanceDialog",
                "Exception e: $e"
            )

            Globals.alertDialogMsg(
                context,
                DialogStatus.ERROR,
                "Увага",
                "Не вдалося відкрити залишки товару.\n\nПомилка: $e"
            )
        }
    }

    override fun onInlineRowPersisted(itemId: Long, rowId: String) {
        refreshEditedInlineRow(itemId, rowId)
        refreshOptionCodeForItem(itemId)
    }

    private fun refreshOptionCodeForItem(itemId: Long) {
        val originalItem = getAllCurrentItems().firstOrNull { it.stableId == itemId } ?: return
        val tovar = originalItem.rawAs<TovarDB>() ?: return

        val codeDad2 = getCodeDad2String()

        val reportPrepare = RealmManager.getTovarReportPrepare(
            codeDad2,
            tovar.getiD()
        )

        val optionsList = RealmManager.getTovarOptionInReportPrepare(
            codeDad2,
            ""
        )

        val deletePromoOption = false

        var updatedItem = originalItem

        if (reportPrepare != null) {
            val optionString = Options().getOptionString(
                optionsList,
                reportPrepare,
                deletePromoOption
            )

            if (optionString.isNotBlank()) {
                val optionField = buildOptionCodeField(
                    optionHtmlOrText = optionString,
                    key = FIELD_OPTION_CODE,
                    title = "Шифр",
                    actionId = "open_option_code"
                )

                updatedItem = updatedItem.addOrReplaceField(optionField)
            }

            val commentField = buildTovarBalanceImageCommentField(reportPrepare)
            updatedItem = updatedItem.addOrReplaceImageCommentField(commentField)
        }

        replaceCurrentItemByStableId(updatedItem)
    }

    private fun refreshEditedInlineRow(
        itemId: Long,
        rowId: String
    ) {
        val current = productCodeEditorState.value
        val rows = current.rowsByItemId[itemId].orEmpty()
        val existingRow = rows.firstOrNull { it.rowId == rowId } ?: return

        val tovar = findTovarByStableId(itemId) ?: return
        val codeDad2 = getCodeDad2String()
        val rp = RealmManager.getTovarReportPrepare(codeDad2, tovar.getiD())

        val refreshedRow = when (existingRow.option.getOptionControlName()) {
            OptionControlName.AKCIYA_ID -> {
                existingRow.copy(
                    value = getCurrentData(existingRow.option, rp = rp).orEmpty(),
                    value2 = rp?.getAkciya().orEmpty()
                )
            }

            OptionControlName.ERROR_ID -> {
                existingRow.copy(
                    value = getCurrentData(existingRow.option, rp = rp).orEmpty(),
                    value2 = rp?.getErrorComment().orEmpty()
                )
            }

            else -> {
                existingRow.copy(
                    value = getCurrentData(existingRow.option, rp = rp).orEmpty()
                )
            }
        }

        val updatedMap = current.rowsByItemId.toMutableMap()
        updatedMap[itemId] = rows.map { row ->
            if (row.rowId == rowId) refreshedRow else row
        }

        setProductCodeEditor(
            current.copy(rowsByItemId = updatedMap)
        )
    }

    override fun onClickAdditionalContent() {
        if (contextUI == ContextUI.TOVAR_FROM_TOVAR_TABS)
            showAdditionalContentMenu()
    }

    private fun openProductCodeEditor(
        itemUI: DataItemUI,
        fieldValue: FieldValue,
        action: ClickTextAction,
        mode: ProductCodeEditorMode
    ) {
        val current = productCodeEditorState.value

        val sameSourceClicked =
            current.expanded &&
                    current.sourceItemId == itemUI.stableId &&
                    current.mode == mode

        if (sameSourceClicked) {
            setProductCodeEditor(ProductCodeEditorState())
            return
        }

        val allItems = buildList {
            addAll(uiState.value.itemsHeader)
            addAll(uiState.value.items)
            addAll(uiState.value.itemsFooter)
        }

        val rowsByItemId = allItems.associate { item ->
            item.stableId to buildProductCodeRowsForItem(
                item = item,
                clickedField = fieldValue,
                action = action,
                mode = mode
            )
        }

        setProductCodeEditor(
            ProductCodeEditorState(
                expanded = true,
                sourceItemId = itemUI.stableId,
                mode = mode,
                rowsByItemId = rowsByItemId
            )
        )
    }

    private val hiddenMenuMode: HiddenMenuMode = HiddenMenuMode.INLINE

    private fun hiddenPresentation(): SubmenuPresentation =
        when (hiddenMenuMode) {
            HiddenMenuMode.INLINE -> SubmenuPresentation.INLINE_EXPAND
            HiddenMenuMode.OVERLAY -> SubmenuPresentation.OVERLAY
            HiddenMenuMode.REPLACE -> SubmenuPresentation.REPLACE
        }

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

    private fun getRequiredSupportedInlineOptions(
        optionsList2: MutableList<OptionsDB>?,
        finalDeletePromoOption: Boolean,
        tovarOptions: List<TovarOptions>
    ): List<TovarOptions> {
        val required = Options().getRequiredOptionsTPL(optionsList2, finalDeletePromoOption)
        val supportedIds = getAllSupportedInlineOptions(tovarOptions)
            .mapNotNull { it.getOptionControlName() }
            .toSet()

        return required.filter { it.getOptionControlName() in supportedIds }
    }

//    private fun buildServiceEntries(): List<ContextMenuEntry> {
//        val tovarOptions = Options.getTovarOptins()
//
//        return tovarEditorSpecs.mapNotNull { spec ->
//            val tpl = tovarOptions.getOrNull(spec.optionIndex) ?: return@mapNotNull null
//
//            ContextMenuEntry.Action(
//                id = spec.menuId,
//                actionId = spec.actionId,
//                title = spec.title,
//                leading = MenuLeading.BadgeText(spec.badge)
//            )
//        }
//    }

    private fun buildServiceEntries(payload: ContextMenuPayload): List<ContextMenuEntry> {
        val items = payload.selectedItems
        val selectedItems = items.filter { it.selected }
        val selectedCount = selectedItems.size
        val hasSelectedInClickedCard = selectedCount > 0

        return when (contextUI) {
            ContextUI.TOVAR_FROM_TOVAR_TABS -> listOf(
                ContextMenuEntry.Action(
                    id = "tovar_price",
                    actionId = ContextMenuActionIds.TOVAR_PRICE,
                    title = "Цена товара",
                    leading = MenuLeading.BadgeText("Ц")
                ),
                ContextMenuEntry.Action(
                    id = "tovar_face",
                    actionId = ContextMenuActionIds.TOVAR_FACE,
                    title = "Кол. фейсов",
                    leading = MenuLeading.BadgeText("Ф")
                ),
                ContextMenuEntry.Action(
                    id = "tovar_expire_left",
                    actionId = ContextMenuActionIds.TOVAR_EXPIRE_LEFT,
                    title = "Возврат",
                    leading = MenuLeading.BadgeText("В")
                ),
                ContextMenuEntry.Action(
                    id = "tovar_amount",
                    actionId = ContextMenuActionIds.TOVAR_AMOUNT,
                    title = "Кол. на витрине",
                    leading = MenuLeading.BadgeText("К")
                ),
                ContextMenuEntry.Action(
                    id = "tovar_up",
                    actionId = ContextMenuActionIds.TOVAR_UP,
                    title = "Поднято товара",
                    leading = MenuLeading.BadgeText("П")
                ),
                ContextMenuEntry.Action(
                    id = "tovar_dt_expire",
                    actionId = ContextMenuActionIds.TOVAR_DT_EXPIRE,
                    title = "Дата ок. ср. год",
                    leading = MenuLeading.BadgeText("Д")
                ),
                ContextMenuEntry.Action(
                    id = "tovar_oborotved_num",
                    actionId = ContextMenuActionIds.TOVAR_OBOROTVED_NUM,
                    title = "Остаток по учёту",
                    leading = MenuLeading.BadgeText("О")
                ),
                ContextMenuEntry.Action(
                    id = "tovar_error_id",
                    actionId = ContextMenuActionIds.TOVAR_ERROR_ID,
                    title = "Ошибка товара",
                    leading = MenuLeading.BadgeText("Ш")
                ),
                ContextMenuEntry.Action(
                    id = "tovar_akciya_id",
                    actionId = ContextMenuActionIds.TOVAR_AKCIYA_ID,
                    title = "Акция",
                    leading = MenuLeading.BadgeText("А")
                ),
                ContextMenuEntry.Action(
                    id = "tovar_akciya",
                    actionId = ContextMenuActionIds.TOVAR_AKCIYA,
                    title = "Наличие акции",
                    leading = MenuLeading.BadgeText("Н")
                ),
                ContextMenuEntry.Action(
                    id = "tovar_notes",
                    actionId = ContextMenuActionIds.TOVAR_NOTES,
                    title = "Примечание к товару",
                    leading = MenuLeading.BadgeText("П")
                )
            )


            ContextUI.TOVAR_FROM_TOVAR_TABS_ADD_ALL,
            ContextUI.TOVAR_FROM_TOVAR_TABS_ADD_NEW -> listOf(
                ContextMenuPresets.AdditionalWorkAdd.toEntry(
                    id = "service_apply_request",
                    title = "Добавить ($selectedCount)",
                    enabled = hasSelectedInClickedCard
                )
            )

            else -> emptyList()
        }
    }

    override fun onContextMenuAction(event: ContextMenuActionEvent) {
        when (event.actionId) {
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

            ContextMenuActionIds.INVERT -> invertSelection()

            ContextMenuActionIds.COLLAPSE_LIST -> collapseAllDecks()
            ContextMenuActionIds.EXPAND_LIST -> expandAllDecks()
            ContextMenuActionIds.OPEN_SMS_PLAN -> openSmsPlanDirectory()
            ContextMenuActionIds.LIST_SETTINGS -> openSortingDialog()
            ContextMenuActionIds.CLOSE -> hideContextMenu()

            ContextMenuActionIds.ADDITIONAL_ADD -> {
                val selectedItems = event.payload.selectedItems.filter { it.selected }

                val itemsToAdd = if (selectedItems.isNotEmpty()) {
                    selectedItems
                } else {
                    event.payload.selectedItems
                }

                val selectedIds = itemsToAdd.mapNotNull { item ->
                    item.rawAs<TovarDB>()?.getiD()
                }

                TovarTabsAddNewHolder.set(selectedIds)
                hideContextMenu()

                // Здесь нужен твой текущий механизм закрытия FeaturesActivity/возврата назад.
                // Например, если у тебя есть событие закрытия экрана:
                // emitEvent(MainEvent.CloseWithResult)
            }

            ContextMenuActionIds.ADDITIONAL_CONTENT_DELETE_EXTRA -> {
                hideContextMenu()
                clearManualTovars()
            }

            ContextMenuActionIds.ADDITIONAL_CONTENT_ADD_PPA -> {
                hideContextMenu()
                requestAddPpaTovars()
            }

            ContextMenuActionIds.ADDITIONAL_CONTENT_ADD_ALL -> {
                hideContextMenu()
                requestAddAllTovar()

            }

            ContextMenuActionIds.ADDITIONAL_CONTENT_ADD_ONE -> {
                hideContextMenu()
                requestAddOneTovar()
            }

            else -> handleTovarDialogAction(event)
        }
    }

    override fun onClickItem(itemUI: DataItemUI, context: Context) {
        super.onClickItem(itemUI, context)

        if (contextUI != ContextUI.TOVAR_FROM_TOVAR_TABS) return

        try {
            val tovar = itemUI.rawAs<TovarDB>() ?: return

            val codeDad2 = Gson()
                .fromJson(dataJson, JSONObject::class.java)
                .getString("codeDad2")
                .toLong()

            val codeDad2String = codeDad2.toString()

            val wpDataDB = RealmManager.getWorkPlanRowByCodeDad2(codeDad2)
                ?: return

            val tovarId = tovar.getiD()

            val adList: List<AdditionalRequirementsDB> = runCatching {
                AdditionalRequirementsRealm.getData3(
                    wpDataDB,
                    AdditionalRequirementsRealm.AdditionalRequirementsModENUM.DEFAULT,
                    null,
                    null,
                    0
                )
            }.getOrNull().orEmpty()

            adList.firstOrNull { requirement ->
                requirement.getTovarId() == tovarId &&
                        requirement.getOptionId().isNullOrEmpty()
            }?.let { requirement ->
                val dialogMsg = DialogData(context)
                dialogMsg.setTitle("Додаткова вимога до товару.")
                dialogMsg.setText(requirement.getNm())
                dialogMsg.setClose(DialogClickListener {
                    dialogMsg.dismiss()
                })
                dialogMsg.show()
            }

            val optionsList2 = RealmManager.getTovarOptionInReportPrepare(
                codeDad2String,
                null
            )

            if (optionsList2.isNullOrEmpty()) {
                val dialog = DialogData(context)
                dialog.setTitle("Внимание!")
                dialog.setText(
                    "Для данного товара не определены реквизиты обязательные для заполнения. " +
                            "Для принудительного вызова списка реквизитов выполните длинный клик по товару."
                )
                dialog.setClose(DialogClickListener {
                    dialog.dismiss()
                })
                dialog.show()

                showTovarAdditionalRequirement(tovar, adList)?.let { requirement ->
                    showTovarAdditionalRequirementDialog(
                        context = context,
                        tovar = tovar,
                        additionalRequirementsDB = requirement
                    )
                }

                return
            }

            val options = Options()
            val finalDeletePromoOption = false

            val requiredOptions = options.getRequiredOptionsTPL(
                optionsList2,
                finalDeletePromoOption
            )

            Log.e(
                "DRAdapterTovar",
                "Кол-во. обязательных опций: ${requiredOptions.size}"
            )

            val firstRequiredOption = requiredOptions.firstOrNull { option ->
                option.getOptionControlName() != OptionControlName.AKCIYA
            }

            if (firstRequiredOption == null) {
                val dialog = DialogData(context)
                dialog.setTitle("Внимание!")
                dialog.setText(
                    "Для данного товара не определены реквизиты обязательные для заполнения. " +
                            "Для принудительного вызова списка реквизитов выполните длинный клик по товару."
                )
                dialog.setClose(DialogClickListener {
                    dialog.dismiss()
                })
                dialog.show()

                showTovarAdditionalRequirement(tovar, adList)?.let { requirement ->
                    showTovarAdditionalRequirementDialog(
                        context = context,
                        tovar = tovar,
                        additionalRequirementsDB = requirement
                    )
                }

                return
            }

            val reportPrepare = RealmManager.getTovarReportPrepare(
                codeDad2String,
                tovarId
            ) ?: createNewRPRow(
                tovarId = tovarId,
                wpDataDB = wpDataDB
            )

            showDialogForItems(
                tovars = listOf(tovar),
                tpl = firstRequiredOption,
                wpDataDB = wpDataDB,
                finalBalanceData1 = reportPrepare
                    ?.getOborotvedNum()
                    ?.toString()
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: "0",
                finalBalanceDate1 = formatBalanceDateForDialog(reportPrepare),
                clickType = true,
                tovOptTplList = requiredOptions.toMutableList()
            )

            showTovarAdditionalRequirement(tovar, adList)?.let { requirement ->
                showTovarAdditionalRequirementDialog(
                    context = context,
                    tovar = tovar,
                    additionalRequirementsDB = requirement
                )
            }

        } catch (e: Exception) {
            Globals.writeToMLOG(
                "ERROR",
                "TovarDBViewModel.onClickItem",
                "Exception e: $e"
            )

            Globals.alertDialogMsg(
                context,
                DialogStatus.ERROR,
                "Увага",
                "Не удалось открыть Опцию. Если ошибка повторяется - обратитесь к своему руководителю.\n\nОшибка: $e"
            )
        }
    }

    override fun onProductCodeTakePhoto(itemUI: DataItemUI, context: Context) {
        try {
            val tovar = itemUI.rawAs<TovarDB>() ?: return

            val codeDad2 = Gson()
                .fromJson(dataJson, JSONObject::class.java)
                .getString("codeDad2")
                .toLong()

            val wpDataDB = RealmManager.getWorkPlanRowByCodeDad2(codeDad2) ?: return
            val reportPrepare = RealmManager.getTovarReportPrepare(
                codeDad2.toString(),
                tovar.getiD()
            ) ?: return

            TovarRequisites(tovar, reportPrepare)
                .createDialog(context, wpDataDB, null, Clicks.clickVoid { })
                .show()

        } catch (e: Exception) {
            Globals.writeToMLOG(
                "ERROR",
                "MainViewModel.onProductCodeTakePhoto",
                "Exception e: $e"
            )

            Globals.alertDialogMsg(
                context,
                DialogStatus.ERROR,
                "Увага",
                "Не вдалося відкрити діалог фото залишків.\n\nПомилка: $e"
            )
        }
    }

    private fun buildPromotionalTovIds(
        wpDataDB: WpDataDB,
        data: List<AdditionalRequirementsDB>
    ): MutableList<Int> {
        val result = mutableListOf<Int>()

        val docDt = wpDataDB.dt?.time?.div(1000) ?: 0L
        val docDtMinus2 = Clock.getDatePeriodLong(docDt, -2)

        data.forEach { item ->
            val tovarId = item.getTovarId()
                ?.takeIf { it.isNotBlank() && it != "0" }
                ?.toIntOrNull()
                ?: return@forEach

            val startDt = item.dtStart?.time?.div(1000) ?: 0L
            val endDt = item.dtEnd?.time?.div(1000) ?: 0L

            val active = (startDt > 0L && endDt > 0L && docDtMinus2 < endDt) ||
                    (startDt > 0L && endDt == 0L)

            if (active) {
                result.add(tovarId)
            }
        }

        return result
    }

    private val manuallyAddedTovars = mutableListOf<TovarDB>()

    private fun getManuallyAddedIds(): Set<String> {
        return manuallyAddedTovars
            .mapNotNull { it.getiD() }
            .toSet()
    }

    private fun mergeManualTovars(
        newItems: List<TovarDB>,
        refresh: Boolean = true
    ) {
        if (newItems.isEmpty()) return

        val merged = LinkedHashMap<String, TovarDB>()

        manuallyAddedTovars.forEach { existing ->
            merged[existing.getiD()] = existing
        }

        newItems.forEach { incoming ->
            incoming.timeColor = "FAF7BB"
            merged[incoming.getiD()] = incoming
        }

        manuallyAddedTovars.clear()
        manuallyAddedTovars.addAll(merged.values)

        if (refresh) {
            updateContent()
        }
    }

    private fun clearManualTovars() {
        if (manuallyAddedTovars.isEmpty()) return
        manuallyAddedTovars.clear()
        updateContent()
    }

    private fun invertSelection() {
        val selectedIds = mutableListOf<Long>()
        val unselectedIds = mutableListOf<Long>()

        uiState.value.items.forEach { item ->
            if (item.selected) {
                selectedIds.add(item.stableId)
            } else {
                unselectedIds.add(item.stableId)
            }
        }

        if (selectedIds.isNotEmpty()) {
            updateItemsSelect(ids = selectedIds, checked = false)
        }

        if (unselectedIds.isNotEmpty()) {
            updateItemsSelect(ids = unselectedIds, checked = true)
        }
    }

    private fun handleTovarDialogAction(event: ContextMenuActionEvent) {
        if (!isTovarDialogAction(event.actionId)) return

        val selectedTovars = extractTovars(event.payload.selectedItems)
        if (selectedTovars.isEmpty()) return

        val codeDad2 = getCodeDad2String().toLong()
        val wpDataDB = RealmManager.getWorkPlanRowByCodeDad2(codeDad2) ?: return
        val tovarOptions = getOrCreateProductCodeSessionCache().allTovarOptions

        val tpl = resolveTovarOption(event.actionId, tovarOptions) ?: return

        showDialogForItems(
            tovars = selectedTovars,
            tpl = tpl,
            wpDataDB = wpDataDB,
            finalBalanceData1 = "0",
            finalBalanceDate1 = "?",
            clickType = false,
            tovOptTplList = tovarOptions.toMutableList()
        )
    }

    private fun mergeBaseAndManualTovars(base: List<TovarDB>): List<TovarDB> {
        val merged = LinkedHashMap<String, TovarDB>()

        base.forEach { merged[it.getiD()] = it }
        manuallyAddedTovars.forEach { merged[it.getiD()] = it }

        return merged.values.toList()
    }

    private fun resolveTovarOption(
        actionId: String,
        tovarOptions: List<TovarOptions>
    ): TovarOptions? {
        val spec = tovarEditorSpecs.firstOrNull { it.actionId == actionId } ?: return null
        return tovarOptions.getOrNull(spec.optionIndex)
    }


    private fun extractTovars(items: List<DataItemUI>): List<TovarDB> {
        return items.mapNotNull { it.rawAs<TovarDB>() }
    }

    private fun isTovarDialogAction(actionId: String): Boolean {
        return tovarEditorSpecs.any { it.actionId == actionId }
    }

    private data class TovarPhotoRequest(
        val tovarId: String,
        val tovarIdInt: Int,
        val photoId: String?,
        val barcode: String,
        val article: String,
        val detachedTovar: TovarDB
    )

    private data class LocalTovarPhoto(
        val path: String,
        val quality: TovarPhotoQuality
    )

    private fun openTovarPhotoDialog(tovar: TovarDB) {
        val request = buildTovarPhotoRequest(tovar) ?: return
        val loadToken = System.nanoTime()

        val localPhoto = findLocalTovarPhoto(request)

        showTovarPhotoDialog(
            TovarPhotoDialogUiState(
                tovarId = request.tovarId,
                photoId = request.photoId,
                imagePath = localPhoto?.path,
                imageQuality = localPhoto?.quality ?: TovarPhotoQuality.NONE,
                barcode = request.barcode,
                article = request.article,
                isLoadingFull = true,
                imageVersion = System.currentTimeMillis(),
                loadToken = loadToken
            )
        )

        loadFullTovarPhotoInBackground(
            request = request,
            loadToken = loadToken
        )
    }

    private fun buildTovarPhotoRequest(tovar: TovarDB): TovarPhotoRequest? {
        val tovarId = tovar.getiD()
        val tovarIdInt = tovarId.toIntOrNull() ?: return null

        val detached = try {
            RealmManager.INSTANCE.copyFromRealm(tovar)
        } catch (e: Exception) {
            tovar
        }

        return TovarPhotoRequest(
            tovarId = tovarId,
            tovarIdInt = tovarIdInt,
            photoId = tovar.photoId,
            barcode = tovar.getBarcode().orEmpty(),
            article = getArticle(tovar, 0).orEmpty(),
            detachedTovar = detached
        )
    }
    private fun findLocalTovarPhoto(
        request: TovarPhotoRequest
    ): LocalTovarPhoto? {
        val fullPath = findValidTovarPhotoPath(
            tovId = request.tovarIdInt,
            photoId = request.photoId,
            full = true
        )

        if (!fullPath.isNullOrBlank()) {
            return LocalTovarPhoto(
                path = fullPath,
                quality = TovarPhotoQuality.FULL
            )
        }

        val previewPath = findValidTovarPhotoPath(
            tovId = request.tovarIdInt,
            photoId = request.photoId,
            full = false
        )

        if (!previewPath.isNullOrBlank()) {
            return LocalTovarPhoto(
                path = previewPath,
                quality = TovarPhotoQuality.PREVIEW
            )
        }

        return null
    }

    private fun findValidTovarPhotoPath(
        tovId: Int,
        photoId: String?,
        full: Boolean
    ): String? {
        val photo = RealmManager.getTovarPhotoByIdAndType(
            tovId,
            photoId,
            18,
            full
        ) ?: return null

        if (photo.getObject_id() != tovId) return null

        val path = photo.getPhoto_num()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        return path
    }

    private fun findValidTovarPhoto(
        tovId: Int,
        photoId: String?,
        full: Boolean
    ): StackPhotoDB? {
        val photo = RealmManager.getTovarPhotoByIdAndType(
            tovId,
            photoId,
            18,
            full
        ) ?: return null

        if (photo.getObject_id() != tovId) return null
        if (photo.getPhoto_num().isNullOrBlank()) return null

        return photo
    }

    private fun loadFullTovarPhotoInBackground(
        request: TovarPhotoRequest,
        loadToken: Long
    ) {
        Exchange().getTovarImg(
            listOf(request.detachedTovar),
            "full",
            object : Globals.OperationResult {
                override fun onSuccess() {
                    viewModelScope.launch(Dispatchers.Main) {
                        val fullPath = waitForFullTovarPhotoPathOnMain(
                            request = request,
                            attempts = 12,
                            delayMs = 250L
                        )

                        updateTovarPhotoDialog { current ->
                            if (current.tovarId != request.tovarId || current.loadToken != loadToken) {
                                current
                            } else if (!fullPath.isNullOrBlank()) {
                                current.copy(
                                    imagePath = fullPath,
                                    imageQuality = TovarPhotoQuality.FULL,
                                    isLoadingFull = false,
                                    imageVersion = System.currentTimeMillis()
                                )
                            } else {
                                current.copy(
                                    isLoadingFull = false,
                                    imageVersion = System.currentTimeMillis()
                                )
                            }
                        }
                    }
                }

                override fun onFailure(error: String?) {
                    viewModelScope.launch(Dispatchers.Main) {
                        updateTovarPhotoDialog { current ->
                            if (current.tovarId != request.tovarId || current.loadToken != loadToken) {
                                current
                            } else {
                                current.copy(isLoadingFull = false)
                            }
                        }
                    }
                }
            }
        )
    }

    private suspend fun waitForFullTovarPhotoPathOnMain(
        request: TovarPhotoRequest,
        attempts: Int,
        delayMs: Long
    ): String? {
        repeat(attempts) {
            val path = findValidTovarPhotoPath(
                tovId = request.tovarIdInt,
                photoId = request.photoId,
                full = true
            )

            if (!path.isNullOrBlank()) {
                return path
            }

            delay(delayMs)
        }

        return null
    }

    private suspend fun waitForFullTovarPhoto(
        tovar: TovarDB,
        attempts: Int,
        delayMs: Long
    ): StackPhotoDB? {
        val tovId = tovar.getiD().toIntOrNull() ?: return null

        repeat(attempts) {
            val fullPhoto = findValidTovarPhoto(
                tovId = tovId,
                photoId = tovar.photoId,
                full = true
            )

            if (fullPhoto != null) {
                return fullPhoto
            }

            delay(delayMs)
        }

        return null
    }

    private data class TovarEditorSpec(
        val actionId: String,
        val menuId: String,
        val title: String,
        val badge: String,
        val optionIndex: Int
    )

    private val tovarEditorSpecs = listOf(
        TovarEditorSpec(
            actionId = ContextMenuActionIds.TOVAR_PRICE,
            menuId = "tovar_price",
            title = "Цена товара",
            badge = "Ц",
            optionIndex = 0
        ),
        TovarEditorSpec(
            actionId = ContextMenuActionIds.TOVAR_FACE,
            menuId = "tovar_face",
            title = "Кол. фейсов",
            badge = "Ф",
            optionIndex = 1
        ),
        TovarEditorSpec(
            actionId = ContextMenuActionIds.TOVAR_EXPIRE_LEFT,
            menuId = "tovar_expire_left",
            title = "Возврат",
            badge = "В",
            optionIndex = 2
        ),
        TovarEditorSpec(
            actionId = ContextMenuActionIds.TOVAR_AMOUNT,
            menuId = "tovar_amount",
            title = "Кол. на витрине",
            badge = "К",
            optionIndex = 3
        ),
        TovarEditorSpec(
            actionId = ContextMenuActionIds.TOVAR_UP,
            menuId = "tovar_up",
            title = "Поднято товара",
            badge = "П",
            optionIndex = 4
        ),
        TovarEditorSpec(
            actionId = ContextMenuActionIds.TOVAR_DT_EXPIRE,
            menuId = "tovar_dt_expire",
            title = "Дата ок. ср. год",
            badge = "Д",
            optionIndex = 5
        ),
        TovarEditorSpec(
            actionId = ContextMenuActionIds.TOVAR_OBOROTVED_NUM,
            menuId = "tovar_oborotved_num",
            title = "Остаток по учёту",
            badge = "О",
            optionIndex = 6
        ),
        TovarEditorSpec(
            actionId = ContextMenuActionIds.TOVAR_ERROR_ID,
            menuId = "tovar_error_id",
            title = "Ошибка товара",
            badge = "Ш",
            optionIndex = 7
        ),
        TovarEditorSpec(
            actionId = ContextMenuActionIds.TOVAR_AKCIYA_ID,
            menuId = "tovar_akciya_id",
            title = "Акция",
            badge = "А",
            optionIndex = 8
        ),
        TovarEditorSpec(
            actionId = ContextMenuActionIds.TOVAR_AKCIYA,
            menuId = "tovar_akciya",
            title = "Наличие акции",
            badge = "Н",
            optionIndex = 9
        ),
        TovarEditorSpec(
            actionId = ContextMenuActionIds.TOVAR_NOTES,
            menuId = "tovar_notes",
            title = "Примечание к товару",
            badge = "П",
            optionIndex = 10
        )
    )

    private fun getAllSupportedInlineOptions(
        tovarOptions: List<TovarOptions>
    ): List<TovarOptions> {
        return tovarEditorSpecs.mapNotNull { spec ->
            tovarOptions.getOrNull(spec.optionIndex)
        }
    }

    private fun showDialogForItems(
        tovars: List<TovarDB>,
        tpl: TovarOptions,
        wpDataDB: WpDataDB,
        finalBalanceData1: String?,
        finalBalanceDate1: String?,
        clickType: Boolean,
        tovOptTplList: MutableList<TovarOptions>
    ) {
        if (tovars.isEmpty()) return

        try {
            val previewTovar = tovars.first()
            val cd2 = wpDataDB.code_dad2.toString()
            val clientId = wpDataDB.client_id // а не code_dad2
            val previewRp =
                RealmManager.getTovarReportPrepare(cd2, previewTovar.getiD()) ?: ReportPrepareDB()

            val dialog = DialogData(context)
            dialog.setTitle("")
            dialog.setText("")
            dialog.setClose {
                closeDialogRule(dialog) {
//                    updateContent()
                    dialog.dismiss()
                }
            }
            dialog.setLesson(context, true, 802)
            dialog.setVideoLesson(context, true, 803, null, null)
            dialog.setImage(true, getPhotoFromDB(previewTovar))
            dialog.setAdditionalText(
                setPhotoInfo(
                    reportPrepareDB = previewRp,
                    tpl = tpl,
                    tovar = previewTovar,
                    finalBalanceData1 = finalBalanceData1,
                    finalBalanceDate1 = finalBalanceDate1
                )
            )

            dialog.tovarOptions = tpl
            dialog.reportPrepareDB = previewRp

            when (tpl.getOptionControlName()) {
                OptionControlName.AKCIYA_ID -> {
                    dialog.setOperationSpinnerData(setMapData(tpl.getOptionControlName()))
                    dialog.setOperationSpinner2Data(setMapData(OptionControlName.AKCIYA))

                    val promoDB = PromoRealm.getPromoDBById(previewRp.getAkciyaId())
                    dialog.setOperationTextData(
                        if (promoDB != null) promoDB.getNm() else previewRp.getAkciyaId()
                    )

                    val map = hashMapOf<Int?, String?>(
                        2 to "Акция отсутствует",
                        1 to "Есть акция"
                    )
                    val akciya = map[previewRp.getAkciya().toIntOrNull()]
                    dialog.setOperationTextData2(akciya)
                }

                else -> Unit
            }

            if (tpl.getOptionControlName() == OptionControlName.ERROR_ID) {
                var groupPos: String? = null
                val contains135591 = tovOptTplList.any { it.getOptionId().contains(135591) }
                val contains157241 = tovOptTplList.any { it.getOptionId().contains(157241) }

                if (contains135591) groupPos = "22"
                if (contains157241) groupPos = "13"

                dialog.setExpandableListView(
                    createExpandableAdapter(dialog.context, groupPos),
                    DialogClickListener {
                        if (!dialog.getOperationResult().isNullOrBlank()) {
                            saveDialogResultForItems(
                                tovars = tovars,
                                tpl = tpl,
                                dialog = dialog,
                                wpDataDB = wpDataDB
                            )
                        }
                    }
                )
            } else {
                dialog.setOperation(
                    operationType(tpl),
                    getCommonCurrentData(
                        tpl = tpl,
                        cd2 = cd2,
                        tovars = tovars
                    ),
                    setMapData(tpl.getOptionControlName()),
                    DialogClickListener {
                        if (operationType(tpl) == DialogData.Operations.Date) {
                            val value = dialog.getOperationResult()
                            if (!value.isNullOrBlank()) {
                                val hasInvalidDate = tovars.any { tovar ->
                                    val tovExpirationDate = (tovar.expirePeriod * 86400).toLong()
                                    val dtCurrentWPData = wpDataDB.getDt().time / 1000
                                    val dtUserSetToTovar = Clock.dateConvertToLong(value) / 1000
                                    val resDays = dtCurrentWPData + tovExpirationDate
                                    tovar.expirePeriod != 0 && dtUserSetToTovar > resDays
                                }

                                if (OptionsRealm.getOptionControl(
                                        cd2,
                                        "165276"
                                    ) != null && hasInvalidDate
                                ) {
                                    val dialogBadData = DialogData(dialog.context)
                                    dialogBadData.setTitle("Зауваження до Дати")
                                    dialogBadData.setText("Для частини товарів дата некоректна. Відмовитись від її збереження?")
                                    dialogBadData.setOk("Так", DialogClickListener {
                                        dialogBadData.dismiss()
                                        Toast.makeText(
                                            context,
                                            "Дата не збережена!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    })
                                    dialogBadData.setCancel("Ні", DialogClickListener {
                                        dialogBadData.dismiss()
                                        saveDialogResultForItems(
                                            tovars = tovars,
                                            tpl = tpl,
                                            dialog = dialog,
                                            wpDataDB = wpDataDB
                                        )
                                    })
                                    dialogBadData.setClose(DialogClickListener { dialogBadData.dismiss() })
                                    dialogBadData.show()
                                    return@DialogClickListener
                                }
                            }
                        }

                        if (!dialog.getOperationResult().isNullOrBlank()) {
                            saveDialogResultForItems(
                                tovars = tovars,
                                tpl = tpl,
                                dialog = dialog,
                                wpDataDB = wpDataDB
                            )
//                            updateContent()
                        } else {
                            Toast.makeText(
                                dialog.context,
                                "Внесите корректно данные",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            }

            dialog.setCancel("Пропустить", DialogClickListener {
                closeDialogRule(dialog, clickVoid { dialog.dismiss() })
            })

            if (tpl.getOptionControlName() != OptionControlName.AKCIYA_ID &&
                tpl.getOptionControlName() != OptionControlName.AKCIYA
            ) {
                val mod = "report_prepare"
                val act = "get_param_stats"

                RetrofitBuilder.getRetrofitInterface()
                    .GET_REPORT_HINT(mod, act, previewTovar.getiD(), cd2, clientId)
                    .enqueue(object : Callback<ReportHint?> {
                        override fun onResponse(
                            call: Call<ReportHint?>,
                            response: Response<ReportHint?>
                        ) = Unit

                        override fun onFailure(call: Call<ReportHint?>, t: Throwable) = Unit
                    })
            }

            dialog.show()
        } catch (e: Exception) {
            Log.d("showDialogForItems", "Exception: $e")
        }
    }

    private fun showAdditionalContentMenu() {
        val seedItem = buildAdditionalContentSeedItem()
        if (seedItem == null) {
            Toast.makeText(
                context,
                "Немає даних для відкриття меню",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val payload = ContextMenuPayload(
            selectedItems = listOf(seedItem)
        )

        showContextMenu(
            ContextMenuUiState(
                payload = payload,
                header = ContextMenuHeaderUi(
                    visible = false,
                    title = "",
                    rows = emptyList()
                ),
                entries = buildAdditionalContentEntries()
            )
        )
    }

    private fun saveDialogResultForItems(
        tovars: List<TovarDB>,
        tpl: TovarOptions,
        dialog: DialogData,
        wpDataDB: WpDataDB
    ) {
        val value1 = dialog.getOperationResult()
        val value2 = dialog.getOperationResult2()
        val cd2 = wpDataDB.code_dad2.toString()

        tovars.forEach { tovar ->
            val rp = RealmManager.getTovarReportPrepare(cd2, tovar.getiD())

            operetionSaveRPToDB(
                tpl = tpl,
                rp = rp,
                data = value1,
                data2 = value2,
                tovarId = tovar.getiD(),
                wpDataDB = wpDataDB
            )
        }

        afterTovarRequisitesSaved(tovars)

        Toast.makeText(
            context,
            if (tovars.size == 1) {
                "Внесено: $value1"
            } else {
                "Внесено для ${tovars.size} товарів"
            },
            Toast.LENGTH_LONG
        ).show()
    }

    private fun getCommonCurrentData(
        tpl: TovarOptions,
        cd2: String,
        tovars: List<TovarDB>
    ): String {
        val distinctValues = tovars
            .map { tovar ->
                val rp = getCachedReportPrepare(cd2, tovar.getiD())
                getCurrentData(tpl, rp).orEmpty()
            }
            .distinct()

        return if (distinctValues.size == 1) distinctValues.first() else ""
    }

    fun showTovarAdditionalRequirementDialog(
        context: Context?,
        tovar: TovarDB,
        additionalRequirementsDB: AdditionalRequirementsDB
    ) {
        val tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(tovar.getManufacturerId())
        AdditionalRequirementsAdapter().click(context, additionalRequirementsDB, tovar, tradeMarkDB)
    }

    private fun showTovarAdditionalRequirement(
        tovar: TovarDB,
        adList: List<AdditionalRequirementsDB>
    ): AdditionalRequirementsDB? {
        val res = arrayOf<AdditionalRequirementsDB?>(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val result = adList.stream()
                .filter { obj: AdditionalRequirementsDB? -> obj!!.getTovarId() == tovar.getiD() }
                .findFirst()
            result!!.ifPresent(Consumer { currentAR: AdditionalRequirementsDB? ->
                var currentAR = currentAR
                currentAR = result.get()
                // если опция контроля не указана
                if (currentAR.getOptionId() != null && currentAR.getOptionId() == "0") {
                    res[0] = currentAR
                } else {
                    println()
                    res[0] = null
                }
            })
        }
        return res[0]
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

    private fun buildAdditionalContentEntries(): List<ContextMenuEntry> {
        return listOf(
//            ContextMenuEntry.Action(
//                id = "additional_content_delete_extra",
//                actionId = ContextMenuActionIds.ADDITIONAL_CONTENT_DELETE_EXTRA,
//                title = "Видалити зайві товари",
//                leading = MenuLeading.DrawableIcon(R.drawable.ic_letter_x)
//            ),
            ContextMenuEntry.Action(
                id = "additional_content_add_ppa",
                actionId = ContextMenuActionIds.ADDITIONAL_CONTENT_ADD_PPA,
                title = "Додати товари з ППА",
                leading = MenuLeading.DrawableIcon(R.drawable.ic_add)
            ),
            ContextMenuEntry.Action(
                id = "additional_content_add_all",
                actionId = ContextMenuActionIds.ADDITIONAL_CONTENT_ADD_ALL,
                title = "Додати усі товари замовника",
                leading = MenuLeading.DrawableIcon(R.drawable.ic_add)
            ),
            ContextMenuEntry.Action(
                id = "additional_content_add_one",
                actionId = ContextMenuActionIds.ADDITIONAL_CONTENT_ADD_ONE,
                title = "Додати товари",
                leading = MenuLeading.DrawableIcon(R.drawable.ic_add)
            ),
            ContextMenuEntry.Divider("additional_content_divider"),
            ContextMenuPresets.Close.toEntry(
                id = "additional_content_close"
            )
        )
    }

    private fun closeDialogRule(dialog: DialogData, click: clickVoid) {
        if (dialog.tovarOptions.getOptionControlName() == OptionControlName.AKCIYA || dialog.tovarOptions.getOptionControlName() == OptionControlName.AKCIYA_ID) {
            if ((dialog.getOperationResult() == null && dialog.getOperationResult2() != null) ||
                (dialog.getOperationResult() != null && dialog.getOperationResult2() == null) ||
                ((dialog.getOperationResult() != null && (dialog.getOperationResult() == "" || dialog.getOperationResult() == "0")) &&
                        (dialog.getOperationResult2() != null && (dialog.getOperationResult2() != "" && dialog.getOperationResult2() != "0"))
                        ) ||
                ((dialog.getOperationResult2() != null && (dialog.getOperationResult2() == "" || dialog.getOperationResult2() == "0")) &&
                        (dialog.getOperationResult() != null && (dialog.getOperationResult() != "" && dialog.getOperationResult() != "0"))
                        )
            ) {
                Toast.makeText(
                    dialog.context,
                    "Внесіть, будь-ласка, обидва реквізити!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                click.click()
            }
        } else {
            click.click()
        }
    }

    fun getPhotoFromDB(tovar: TovarDB): File? {
        val id = tovar.getiD().toInt()

        val stackPhotoDB = RealmManager.getTovarPhotoByIdAndType(id, tovar.photoId, 18, false)
        if (stackPhotoDB != null) {
            if (stackPhotoDB.getObject_id() == id) {
                if (stackPhotoDB.getPhoto_num() != null && stackPhotoDB.getPhoto_num() != "") {
                    val file = File(stackPhotoDB.getPhoto_num())
                    return file
                }
            }
        }
        return null
    }

    private fun setPhotoInfo(
        reportPrepareDB: ReportPrepareDB,
        tpl: TovarOptions,
        tovar: TovarDB,
        finalBalanceData1: String?,
        finalBalanceDate1: String?
    ): PhotoDescriptionText {
        val res = PhotoDescriptionText()

        try {
            val weightString = String.format(
                "%s, %s",
                tovar.getWeight(),
                tovar.getBarcode()
            ) // составление строк веса и штрихкода для того что б выводить в одно поле
            Log.e("КОСТЫЛИ", "tpl.getOptionLong(): " + tpl.getOptionLong())

            var title = tpl.getOptionLong()

            if (DetailedReportActivity.rpThemeId == 1178) {
                if (tpl.getOptionId().contains(578) || tpl.getOptionId().contains(1465)) {
                    title = "Кол-во выкуп. товара"
                }

                if (tpl.getOptionId().contains(579)) {
                    title = "Цена выкуп. товара"
                }
            }

            if (DetailedReportActivity.rpThemeId == 33) {
                if (tpl.getOptionId().contains(587)) {
                    title = "Кол-во заказанного товара"
                }
            }

            res.row1Text = title
            res.row1TextValue = ""
            res.row2TextValue = tovar.getNm()
            res.row3TextValue = weightString
            Log.e(
                "ПРОИЗВОДИТЕЛЬ",
                if ("2ШТО ТУТ?:" + RealmManager.getNmById(tovar.getManufacturerId()) != null) RealmManager.getNmById(
                    tovar.getManufacturerId()
                ).getNm() else ""
            )

            res.row4TextValue =
                if (RealmManager.getNmById(tovar.getManufacturerId()) != null) RealmManager.getNmById(
                    tovar.getManufacturerId()
                ).getNm() else ""

            res.row5Text = "Ост.:"
            res.row5TextValue = finalBalanceData1 + " шт на " + finalBalanceDate1

            if (reportPrepareDB.facesPlan != null && reportPrepareDB.facesPlan > 0) {
                res.row6Text = "План фейс.:"
                res.row6TextValue = "" + reportPrepareDB.facesPlan
            }
        } catch (e: java.lang.Exception) {
            Globals.writeToMLOG(
                "ERROR",
                "RecycleViewDRAdapterTovar.setPhotoInfo",
                "Exception e: " + e
            )
        }
        return res
    }

    private fun setMapData(optionControlName: OptionControlName): MutableMap<Int?, String?>? {
        val map: MutableMap<Int?, String?> = java.util.HashMap<Int?, String?>()
        when (optionControlName) {
            OptionControlName.ERROR_ID -> {
//                val errorDbList = RealmManager.getAllErrorDb()
//                var i = 0
//                while (i < errorDbList.size) {
//                    if (errorDbList.get(i)!!.getNm() != null && errorDbList.get(i)!!
//                            .getNm() != ""
//                    ) {
//                        map.put(errorDbList.get(i)!!.getID().toInt(), errorDbList.get(i)!!.getNm())
//                    }
//                    i++
//                }
                map.put(0, "Выберите ошибку товара")
                return map
            }

            OptionControlName.AKCIYA_ID -> {
                val promoDbList = RealmManager.getAllPromoDb()
                var i = 0
                while (i < promoDbList.size) {
                    if (promoDbList.get(i)!!.getNm() != null && promoDbList.get(i)!!
                            .getNm() != ""
                    ) {
                        map.put(promoDbList.get(i)!!.getID().toInt(), promoDbList.get(i)!!.getNm())
                    }
                    i++
                }

                map.put(0, "Оберіть тип акції")

                return map
            }

            OptionControlName.AKCIYA -> {
                map.put(2, "Акция отсутствует")
                map.put(1, "Есть акция")

                map.put(0, "Оберіть наявність акції")

                return map
            }

            else -> return null
        }
    }

    private fun createExpandableAdapter(
        context: Context?,
        groupPos: String?
    ): MySimpleExpandableListAdapter {
        var map: MutableMap<String?, String?>?
        val groupDataList = ArrayList<MutableMap<String?, String?>>()

        // список атрибутов групп для чтения
        val groupFrom: Array<String> = arrayOf("groupName")
        // список ID view-элементов, в которые будет помещены атрибуты групп
        val groupTo: IntArray = intArrayOf(android.R.id.text1)

        // список атрибутов элементов для чтения
        val childFrom: Array<String> = arrayOf("itemName")
        // список ID view-элементов, в которые будет помещены атрибуты
        // элементов
        val childTo: IntArray? = intArrayOf(android.R.id.text1)

        // создаем общую коллекцию для коллекций элементов
        val сhildDataList = ArrayList<ArrayList<MutableMap<String?, String?>?>?>()
        // создаем коллекцию элементов для первой группы
        var сhildDataItemList = ArrayList<MutableMap<String?, String?>?>()

        // Получение данных с БД
        val errorDbList = RealmManager.getAllErrorDb()
        val errorGroupsDB: RealmResults<ErrorDB> =
            errorDbList.where().equalTo("parentId", "0").findAll()

        for (group in errorGroupsDB) {
            map = java.util.HashMap<String?, String?>()
            map.put("groupName", group.getNm())
            map.put("groupId", group.getID())

            groupDataList.add(map)

            val errorItemsDB: RealmResults<ErrorDB>? =
                errorDbList.where().equalTo("parentId", group.getID()).findAll()
            if (errorItemsDB != null && errorItemsDB.size > 0) {
                сhildDataItemList = ArrayList<MutableMap<String?, String?>?>()
                for (item in errorItemsDB) {
                    map = java.util.HashMap<String?, String?>()
                    map.put("itemName", "* " + item.getNm())
                    сhildDataItemList.add(map)
                }
                сhildDataList.add(сhildDataItemList)
            } else {
                сhildDataItemList = ArrayList<MutableMap<String?, String?>?>()
                map = java.util.HashMap<String?, String?>()
                map.put("itemName", "* " + group.getNm())
                сhildDataItemList.add(map)
                сhildDataList.add(сhildDataItemList)
            }
        }

        val adapter = MySimpleExpandableListAdapter(
            context, groupDataList,
            android.R.layout.simple_expandable_list_item_1, groupFrom,
            groupTo, сhildDataList, android.R.layout.simple_list_item_1,
            childFrom, childTo
        )


        // Проверка наличия группы с идентификатором 22
        var groupPosition = -1
        for (i in groupDataList.indices) {
            val groupData = groupDataList.get(i)
            val groupId =
                groupData.get("groupId") // Здесь нужно использовать правильный ключ для идентификатора группы
            if (groupId != null && groupId == groupPos) {
                groupPosition = i
                break
            }
        }
        adapter.group = groupPosition

        return adapter
    }

    private fun operetionSaveRPToDB(
        tpl: TovarOptions,
        rp: ReportPrepareDB?,
        data: String?,
        data2: String?,
        tovarId: String,
        wpDataDB: WpDataDB
    ) {
        var rp = rp
        if (rp == null) {
            rp = createNewRPRow(tovarId, wpDataDB)
        }

        if (data == null || data.isEmpty()) {
            Toast.makeText(context, "Для сохранения - внесите данные", Toast.LENGTH_SHORT).show()
            return
        }

        val table = rp
        when (tpl.getOptionControlName()) {
            OptionControlName.PRICE -> {
                Log.e("SAVE_TO_REPORT_OPT", "PRICE: " + data)
                RealmManager.INSTANCE.executeTransaction(Realm.Transaction { realm: Realm? ->
                    table!!.setPrice(data)
                    table.setUploadStatus(1)
                    table.setDtChange(System.currentTimeMillis() / 1000)
                    RealmManager.setReportPrepareRow(table)
                })
            }

            OptionControlName.FACE -> {
                Log.e("SAVE_TO_REPORT_OPT", "FACE: " + data)
                RealmManager.INSTANCE.executeTransaction(Realm.Transaction { realm: Realm? ->
                    table!!.setFace(data)
                    table.setUploadStatus(1)
                    table.setDtChange(System.currentTimeMillis() / 1000)
                    RealmManager.setReportPrepareRow(table)
                })
            }

            OptionControlName.EXPIRE_LEFT -> {
                Log.e("SAVE_TO_REPORT_OPT", "EXPIRE_LEFT: " + data)
                RealmManager.INSTANCE.executeTransaction(Realm.Transaction { realm: Realm? ->
                    table!!.setExpireLeft(data)
                    table.setUploadStatus(1)
                    table.setDtChange(System.currentTimeMillis() / 1000)
                    RealmManager.setReportPrepareRow(table)
                })
            }

            OptionControlName.AMOUNT -> {
                Log.e("SAVE_TO_REPORT_OPT", "AMOUNT: " + data)
                RealmManager.INSTANCE.executeTransaction(Realm.Transaction { realm: Realm? ->
                    table!!.setAmount(data.toInt())
                    table.setUploadStatus(1)
                    table.setDtChange(System.currentTimeMillis() / 1000)
                    RealmManager.setReportPrepareRow(table)
                })
            }

            OptionControlName.OBOROTVED_NUM -> {
                Log.e("SAVE_TO_REPORT_OPT", "OBOROTVED_NUM: " + data)
                RealmManager.INSTANCE.executeTransaction(Realm.Transaction { realm: Realm? ->
                    table!!.setOborotvedNum(data)
                    table.setUploadStatus(1)
                    table.setDtChange(System.currentTimeMillis() / 1000)
                    RealmManager.setReportPrepareRow(table)
                })
            }

            OptionControlName.UP -> {
                Log.e("SAVE_TO_REPORT_OPT", "UP: " + data)
                val curent = System.currentTimeMillis() / 1000
                val millis = System.currentTimeMillis()
                val seconds = millis / 1000
                Log.d("TIME_CHECK", "Millis: " + millis + ", Seconds: " + seconds)
                Log.e("SAVE_TO_REPORT_OPT", "TIME: " + curent)
                RealmManager.INSTANCE.executeTransaction(Realm.Transaction { realm: Realm? ->
                    table!!.setUp(data)
                    table.setUploadStatus(1)
                    table.setDtChange(System.currentTimeMillis() / 1000)
                    RealmManager.setReportPrepareRow(table)
                })
            }

            OptionControlName.DT_EXPIRE -> {
                Log.e("SAVE_TO_REPORT_OPT", "DT_EXPIRE: " + data)
                RealmManager.INSTANCE.executeTransaction(Realm.Transaction { realm: Realm? ->
                    table!!.setDtExpire(data)
                    table.setUploadStatus(1)
                    table.setDtChange(System.currentTimeMillis() / 1000)
                    RealmManager.setReportPrepareRow(table)
                })
            }

            OptionControlName.ERROR_ID -> {
                Log.e("SAVE_TO_REPORT_OPT", "ERROR_ID: " + data)
                Log.e("SAVE_TO_REPORT_OPT", "ERROR_COMMENT: " + data2)
                RealmManager.INSTANCE.executeTransaction(Realm.Transaction { realm: Realm? ->
                    table!!.setErrorId(data)
                    table.setErrorComment(data2)
                    table.setNotes(data2)
                    table.setUploadStatus(1)
                    table.setDtChange(System.currentTimeMillis() / 1000)
                    RealmManager.setReportPrepareRow(table)
                })
            }

            OptionControlName.AKCIYA_ID -> {
                Log.e("SAVE_TO_REPORT_OPT", "AKCIYA_ID: " + data)
                Log.e("SAVE_TO_REPORT_OPT", "AKCIYA_ID_А: " + data2)
                RealmManager.INSTANCE.executeTransaction(Realm.Transaction { realm: Realm? ->
                    table!!.setAkciyaId(data)
                    if (data2 != null && !data2.isEmpty()) {
                        table.setAkciya(data2)
                    }
                    table.setUploadStatus(1)
                    table.setDtChange(System.currentTimeMillis() / 1000)
                    RealmManager.setReportPrepareRow(table)
                })
            }

            OptionControlName.NOTES -> {
                Log.e("SAVE_TO_REPORT_OPT", "NOTES: " + data)
                RealmManager.INSTANCE.executeTransaction(Realm.Transaction { realm: Realm? ->
                    table!!.setNotes(data)
                    table.setUploadStatus(1)
                    table.setDtChange(System.currentTimeMillis() / 1000)
                    RealmManager.setReportPrepareRow(table)
                })
            }

            else -> {}
        }
    }

    private fun createNewRPRow(tovarId: String, wpDataDB: WpDataDB): ReportPrepareDB {
        val rp = ReportPrepareDB()

        var id = RealmManager.reportPrepareGetLastId()
        id = id + 1

        rp.setID(id)
        rp.setDt(System.currentTimeMillis().toString())
        rp.setDtReport(System.currentTimeMillis().toString())
        rp.setKli(wpDataDB.client_id)
        rp.setTovarId(tovarId)
        rp.setAddrId(wpDataDB.addr_id.toString())
        rp.setPrice("")
        rp.setFace("")
        rp.setAmount(0)
        rp.setDtExpire("")
        rp.setExpireLeft("")
        rp.setNotes("")
        rp.setUp("0")
        rp.setAkciya("")
        rp.setAkciyaId("")
        rp.setOborotvedNum("")
        rp.setErrorId("")
        rp.setErrorComment("")
        rp.setCodeDad2(wpDataDB.code_dad2.toString())

        // TODO сохранение в БД новой строки что б потом работать с ней в getCurrentData()
        RealmManager.INSTANCE.beginTransaction()
        RealmManager.INSTANCE.copyToRealmOrUpdate<ReportPrepareDB?>(rp)
        RealmManager.INSTANCE.commitTransaction()
        return rp
    }

    private fun operationType(tpl: TovarOptions): DialogData.Operations {
        when (tpl.getOrderField()) {
            ("price"), ("face"), ("expire_left"), ("amount"), ("oborotved_num"), ("up") -> return DialogData.Operations.Number

            ("dt_expire") -> return DialogData.Operations.Date


            ("akciya_id") -> //                case ("akciya"):
                return DialogData.Operations.DoubleSpinner

            ("error_id") -> return DialogData.Operations.EditTextAndSpinner

            ("notes") -> return DialogData.Operations.Text

            else -> return DialogData.Operations.Text
        }
    }

    private fun getCurrentData(
        tpl: TovarOptions,
        rp: ReportPrepareDB?
    ): String? {
        val table = rp ?: return null

        return when (tpl.getOptionControlName()) {
            OptionControlName.PRICE -> table.getPrice()
            OptionControlName.FACE -> table.getFace()
            OptionControlName.EXPIRE_LEFT -> table.getExpireLeft()
            OptionControlName.AMOUNT -> table.getAmount().toString()
            OptionControlName.OBOROTVED_NUM -> table.getOborotvedNum()
            OptionControlName.UP -> table.getUp()
            OptionControlName.DT_EXPIRE -> table.getDtExpire()
            OptionControlName.ERROR_ID -> table.getErrorId()
            OptionControlName.AKCIYA_ID -> table.getAkciyaId()
            OptionControlName.AKCIYA -> table.getAkciya()
            OptionControlName.NOTES -> table.getNotes()
            else -> null
        }
    }

    private fun buildProductCodeRowsForItem(
        item: DataItemUI,
        clickedField: FieldValue,
        action: ClickTextAction,
        mode: ProductCodeEditorMode
    ): List<ProductCodeEditorRowUi> {
        val tovar = item.rawAs<TovarDB>() ?: return emptyList()

        val stableId = item.stableId
        val tovarId = tovar.getiD()
        val cache = getOrCreateProductCodeSessionCache()

        val cacheKey = stableId to mode
        cache.rowsByItemAndMode[cacheKey]?.let { return it }

        val codeDad2 = cache.codeDad2
        val optionsList2 = cache.optionsList2
        val finalDeletePromoOption = false

        val templates: List<TovarOptions> = when (mode) {
            ProductCodeEditorMode.REQUIRED -> {
                getRequiredSupportedInlineOptions(
                    optionsList2 = optionsList2,
                    finalDeletePromoOption = finalDeletePromoOption,
                    tovarOptions = cache.allTovarOptions
                )
            }

            ProductCodeEditorMode.ALL -> {
                getAllSupportedInlineOptions(cache.allTovarOptions)
            }
        }

        val rp = getCachedReportPrepare(codeDad2, tovarId)

        val rows = templates
            .filter { it.getOptionControlName() != OptionControlName.AKCIYA }
            .map { tpl ->
                val kind = when (operationType(tpl)) {
                    DialogData.Operations.Number -> InlineEditorKind.NUMBER
                    DialogData.Operations.Text -> InlineEditorKind.TEXT
                    DialogData.Operations.Date -> InlineEditorKind.DATE
                    DialogData.Operations.DoubleSpinner -> InlineEditorKind.DOUBLE_SELECT
                    DialogData.Operations.EditTextAndSpinner -> InlineEditorKind.TEXT_AND_SELECT
                    else -> InlineEditorKind.TEXT
                }

                val currentValue = getCurrentData(tpl, rp).orEmpty()

                val choices = setMapData(tpl.getOptionControlName())
                    ?.map { (key, value) ->
                        InlineChoiceUi(
                            id = key?.toString().orEmpty(),
                            title = value.orEmpty()
                        )
                    }
                    .orEmpty()

                val secondChoices =
                    if (tpl.getOptionControlName() == OptionControlName.AKCIYA_ID) {
                        setMapData(OptionControlName.AKCIYA)
                            ?.map { (key, value) ->
                                InlineChoiceUi(
                                    id = key?.toString().orEmpty(),
                                    title = value.orEmpty()
                                )
                            }
                            .orEmpty()
                    } else {
                        emptyList()
                    }

                val value2 = when (tpl.getOptionControlName()) {
                    OptionControlName.AKCIYA_ID -> rp?.getAkciya().orEmpty()
                    OptionControlName.ERROR_ID -> rp?.getErrorComment().orEmpty()
                    else -> ""
                }

                ProductCodeEditorRowUi(
                    rowId = tpl.getOrderField().orEmpty(),
                    title = tpl.getOptionLong().orEmpty(),
                    kind = kind,
                    option = tpl,
                    value = currentValue,
                    value2 = value2,
                    choices = choices,
                    choices2 = secondChoices
                )
            }

        cache.rowsByItemAndMode[cacheKey] = rows
        return rows
    }

    private fun getCurrentWpData(): WpDataDB? {
        return RealmManager.getWorkPlanRowByCodeDad2(getCodeDad2String().toLong())
    }

    private fun getCurrentCustomer(): CustomerSDB? {
        val wp = getCurrentWpData() ?: return null
        return try {
            RoomManager.SQL_DB.customerDao().getById(wp.client_id)
        } catch (_: Exception) {
            null
        }
    }

    private fun shouldShowAddTovarWarning(customer: CustomerSDB?): Boolean {
        return customer != null &&
                customer.ppaAuto == 1 &&
                customer.id != "9382" &&
                customer.id != "32246"
    }

    private fun runWithAddTovarWarningIfNeeded(onContinue: () -> Unit) {
        val customer = getCurrentCustomer()

        if (!shouldShowAddTovarWarning(customer)) {
            onContinue()
            return
        }

        emitEvent(
            MainEvent.ShowMessageDialog(
                MessageDialogData(
                    title = "Товари",
                    subTitle = "Не можна виконати",
                    message = "По даному замовнику КАТЕГОРИЧНО ЗАБОРОНЕНО додавати товари!<br><br>Відмовитися від додавання товарів?",
                    status = DialogStatus.ALERT,
                    positivText = "Так",
                    cancelText = "Ні",
                    isCancelable = true,
                    onButtonOkClicked = {
                        // ничего не делаем, пользователь отказался
                    },
                    onButtonCancelClicked = {
                        onContinue()
                    }
                )
            )
        )
    }

    private fun requestAddPpaTovars() {
        runWithAddTovarWarningIfNeeded {
            val wp = getCurrentWpData() ?: return@runWithAddTovarWarningIfNeeded

            val ppaTovars = PPADBRealm.getTovarListByPPA(
                wp.client_id,
                wp.addr_id,
                null
            )

            if (ppaTovars.isEmpty()) {
                emitEvent(
                    MainEvent.ShowMessageDialog(
                        MessageDialogData(
                            title = "Товари",
                            subTitle = "Додати товари з ППА",
                            message = "Для поточного візиту товари з ППА не знайдені.",
                            status = DialogStatus.NORMAL
                        )
                    )
                )
                return@runWithAddTovarWarningIfNeeded
            }

            val currentIds = getAllCurrentItems()
                .mapNotNull { it.rawAs<TovarDB>()?.getiD() }
                .toSet()

            val newPpaTovars = ppaTovars.filter { it.getiD() !in currentIds }
            val newPpaTovars2 = emptyList<String>()

            if (newPpaTovars2.isEmpty()) {
                emitEvent(
                    MainEvent.ShowMessageDialog(
                        MessageDialogData(
                            title = "Товари",
                            subTitle = "Додати товари з ППА",
                            message = "Немає чого додавати — усі товари з ППА вже актуальні.",
                            status = DialogStatus.NORMAL
                        )
                    )
                )
                return@runWithAddTovarWarningIfNeeded
            }

            mergeManualTovars(newPpaTovars)

            emitEvent(
                MainEvent.ShowMessageDialog(
                    MessageDialogData(
                        title = "Товари",
                        subTitle = "Товари додані",
                        message = "Додано товари з ППА (${newPpaTovars.size}).",
                        status = DialogStatus.NORMAL
                    )
                )
            )
        }
    }

    private fun requestAddAllCustomerTovars() {
//        if (!Globals.onlineStatus) {
//            emitEvent(
//                MainEvent.ShowMessageDialog(
//                    MessageDialogData(
//                        subTitle = "Відсутнє інтернет з'єднання",
//                        message = "Знайдіть місце з кращим інтернет-з'єднанням і повторіть спробу.<br>Нові товари не можуть бути додані без інтернету.",
//                        status = DialogStatus.ERROR
//                    )
//                )
//            )
//            return
//        }

        val wp = getCurrentWpData() ?: return

        val currentCustomerTovars = RealmManager.INSTANCE.copyFromRealm(
            RealmManager.getTovarListByCustomer(wp.client_id)
        )

        val progress = ProgressViewModel(1)
        val loadingDialog = LoadingDialogWithPercent(context as Activity, progress)
        loadingDialog.show()
        progress.onNextEvent("Завантажую усі товари цього клієнта", 3000)

        val dataList = listOf(wp)

        TablesLoadingUnloading().downloadTovarTableWhithResult(dataList, object : Click {
            override fun <T> onSuccess(data: T) {
                progress.onCompleted()

                @Suppress("UNCHECKED_CAST")
                val serverTovars = data as? List<TovarDB> ?: emptyList()

                if (serverTovars.isEmpty()) {
                    emitEvent(
                        MainEvent.ShowMessageDialog(
                            MessageDialogData(
                                title = "Товари",
                                subTitle = "Додати усі товари замовника",
                                message = "На сервері немає нових товарів.",
                                status = DialogStatus.NORMAL
                            )
                        )
                    )
                    return
                }

                val currentIds = currentCustomerTovars.map { it.getiD() }.toSet()
                val newFromServer = serverTovars.filter { it.getiD() !in currentIds }

                if (newFromServer.isEmpty()) {
                    emitEvent(
                        MainEvent.ShowMessageDialog(
                            MessageDialogData(
                                title = "Товари",
                                subTitle = "Додати усі товари замовника",
                                message = "Усі доступні товари вже відображені, на сервері немає нових товарів.",
                                status = DialogStatus.NORMAL
                            )
                        )
                    )
                    return
                }

                emitEvent(
                    MainEvent.ShowMessageDialog(
                        MessageDialogData(
                            title = "Товари",
                            subTitle = "Додати усі товари замовника",
                            message = "Завантажити ${newFromServer.size} товарів з сервера і додати у поточний звіт?",
                            status = DialogStatus.NORMAL,
                            positivText = "Так",
                            cancelText = "Ні",
                            isCancelable = true,
                            onButtonOkClicked = {
                                mergeManualTovars(newFromServer)

                                emitEvent(
                                    MainEvent.ShowMessageDialog(
                                        MessageDialogData(
                                            title = "Товари",
                                            subTitle = "Товари додані",
                                            message = "${newFromServer.size} товарів додані до звіту.<br><br>Збережені будуть тільки ті товари, по яких ви внесете зміни у реквізити.",
                                            status = DialogStatus.NORMAL
                                        )
                                    )
                                )
                            }
                        )
                    )
                )
            }

            override fun onFailure(error: String) {
                progress.onCanceled()

                emitEvent(
                    MainEvent.ShowMessageDialog(
                        MessageDialogData(
                            subTitle = "Сталася помилка",
                            message = error,
                            status = DialogStatus.ERROR
                        )
                    )
                )
            }
        })
    }

    private fun requestAddAllTovar() {
        runWithAddTovarWarningIfNeeded {
            openOneTovarDialogAllUFMD()
        }
    }
    private fun requestAddOneTovar() {
        runWithAddTovarWarningIfNeeded {
            openOneTovarDialogUFMD()
        }
    }

    private fun openOneTovarDialogAllUFMD() {
        val wpDataDB = getCurrentWpData()
        wpDataDB?.let { wpData ->
            launcher?.let {
                launchFeaturesActivity(
                    launcher = it,
                    context = context!!,
                    viewModelClass = TovarDBViewModel::class,
                    dataJson = Gson().toJson(
                        JSONObject()
                            .put("codeDad2", wpData.code_dad2.toString())
                            .put("clientId", wpData.client_id)
                    ),
                    modeUI = ModeUI.MULTI_SELECT,
                    contextUI = ContextUI.TOVAR_FROM_TOVAR_TABS_ADD_ALL,
                    title = "Товари",
                    subTitle = "Отметьте те товары которые хотите добавить отчет",
                )
            }
        }
    }

    private fun openOneTovarDialogUFMD() {
        val wpDataDB = getCurrentWpData()
        wpDataDB?.let { wpData ->
            launcher?.let {
                launchFeaturesActivity(
                    launcher = it,
                    context = context!!,
                    viewModelClass = TovarDBViewModel::class,
                    dataJson = Gson().toJson(
                        JSONObject()
                            .put("codeDad2", wpData.code_dad2.toString())
                            .put("clientId", wpData.client_id)
                    ),
                    modeUI = ModeUI.MULTI_SELECT,
                    contextUI = ContextUI.TOVAR_FROM_TOVAR_TABS_ADD_NEW,
                    title = "Товари",
                    subTitle = "Отметьте те товары которые хотите добавить отчет",
                )
            }
        }
    }

    private fun openOneTovarDialog() {
        val ctx = context ?: return
        val wp = getCurrentWpData() ?: return

        val dialog = DialogData(ctx)
        dialog.setTitle("Оберіть Товар")
        dialog.setText("")

        val adapter = RecycleViewDRAdapterTovar(
            ctx,
            RealmManager.INSTANCE.copyFromRealm(
                RealmManager.getTovarListByCustomer(wp.client_id)
            ),
            wp,
            RecycleViewDRAdapterTovar.OpenType.DIALOG
        )

        adapter.elementClick(object : Clicks.click {
            override fun <T> click(data: T) {
                val tov = data as? TovarDB ?: return
                Toast.makeText(ctx, "Додано товар: ${tov.nm}", Toast.LENGTH_SHORT).show()
                mergeManualTovars(listOf(tov))
                dialog.dismiss()
            }
        })

        adapter.getFilter().filter(dialog.getEditTextSearchText())

        dialog.setEditTextSearch(adapter)
        dialog.setRecycler(
            adapter,
            LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        )
        dialog.setClose(dialog::dismiss)
        dialog.show()
    }

    private fun buildAdditionalContentSeedItem(): DataItemUI? {
        getAllCurrentItems().firstOrNull()?.let { return it }

        val wp = RealmManager.getWorkPlanRowByCodeDad2(getCodeDad2String().toLong())
            ?: return null

        return DataItemUI(
            rawObj = listOf(wp),
            rawFields = emptyList(),
            fields = emptyList(),
            selected = false,
            stableId = -wp.code_dad2
        )
    }

    private fun applyPendingAddedTovarsIfNeeded() {
        if (contextUI != ContextUI.TOVAR_FROM_TOVAR_TABS) return

        val pendingIds = TovarTabsAddNewHolder.consume()
        if (pendingIds.isEmpty()) return

        val clientId = Gson().fromJson(dataJson, JSONObject::class.java)
            .getString("clientId")

        val allClientTovars = RealmManager.INSTANCE
            .copyFromRealm(RealmManager.getTovarListByCustomer(clientId))

        val selectedTovars = allClientTovars.filter { it.getiD() in pendingIds }

        mergeManualTovars(
            newItems = selectedTovars,
            refresh = false
        )
    }

    private fun buildTovarBalanceImageCommentField(
        rp: ReportPrepareDB?
    ): FieldValue? {
        val text = buildTovarBalanceText(rp) ?: return null

        return FieldValue(
            key = "tovar_image_balance_comment",
            field = TextField(
                rawValue = "tovar_image_balance_comment",
                value = "Залишок:"
            ),
            value = TextField(
                rawValue = text,
                value = text,
                modifierValue = getDefaultImageCommentValueModifier()
            )
        )
    }

    private fun buildTovarBalanceText(rp: ReportPrepareDB?): String? {
        if (rp == null) return null

        val balanceData = rp.getOborotvedNum()
            ?.toString()
            ?.trim()
            .orEmpty()

        val balanceDate = rp.oborotved_num_date
            ?.trim()
            ?.toLongOrNull()
            ?.takeIf { it > 0L }
            ?.let { seconds ->
                SimpleDateFormat("dd-MM", Locale.getDefault())
                    .format(Date(seconds * 1000L))
            }
            .orEmpty()

        if (balanceData.isBlank() && balanceDate.isBlank()) {
            return null
        }

        return "Ост: ${balanceData.ifBlank { "-" }} / ${balanceDate.ifBlank { "-" }}"
    }

    private fun DataItemUI.addOrReplaceImageCommentField(
        field: FieldValue?
    ): DataItemUI {
        if (field == null) return this

        return copy(
            rawFields = rawFields
                .filterNot { it.key.equals(field.key, ignoreCase = true) } + field,
            fields = fields
                .filterNot { it.key.equals(field.key, ignoreCase = true) } + field
        )
    }

    private fun buildOborotVedText(
        tovar: TovarDB,
        addressId: Int
    ): SpannableStringBuilder {
        val oborotVed = SpannableStringBuilder()

        try {
            val tovarId = tovar.getiD().toIntOrNull() ?: return oborotVed

            val data = RoomManager.SQL_DB.oborotVedDao().getOborotData(
                Clock.today,
                Clock.today_7,
                tovarId,
                addressId
            )

            if (data.isNullOrEmpty()) {
                oborotVed.append("Даних по оборотній відомості немає")
                return oborotVed
            }

            oborotVed.append("_______________Приход")
            oborotVed.append("__|___Расход\n")

            var kolPostSum = 0
            var kolProdSum = 0

            data.forEach { item ->
                appendHtml(
                    oborotVed,
                    "(${item.dat})___${item.kolPost}________|"
                )
                appendHtml(
                    oborotVed,
                    "___${item.kolProd}<br>"
                )

                kolPostSum += item.kolPost
                kolProdSum += item.kolProd
            }

            appendHtml(
                oborotVed,
                "<b>ИТОГ: _________</b>${kolPostSum}________|___"
            )
            appendHtml(
                oborotVed,
                "$kolProdSum<br>"
            )

            val last = data.lastOrNull()
            if (last != null) {
                appendHtml(
                    oborotVed,
                    "<b>Кон. Ост. </b>(${last.dat}): ${last.kolOst}<br>"
                )
            }

        } catch (e: Exception) {
            Log.e("OBOROT_VED", "Exception e: $e")
            Globals.writeToMLOG(
                "ERROR",
                "TovarDBViewModel.buildOborotVedText",
                "Exception e: $e"
            )
        }

        return oborotVed
    }

    private fun formatBalanceDateForDialog(
        rp: ReportPrepareDB?
    ): String {
        val seconds = rp
            ?.oborotved_num_date
            ?.trim()
            ?.toLongOrNull()
            ?: return "(нет данных)"

        if (seconds <= 0L) return "(нет данных)"

        return SimpleDateFormat("dd-MM", Locale.getDefault())
            .format(Date(seconds * 1000L))
    }

    private fun isBalanceOld(
        rp: ReportPrepareDB?
    ): Boolean {
        val seconds = rp
            ?.oborotved_num_date
            ?.trim()
            ?.toLongOrNull()
            ?: return true

        if (seconds <= 0L) return true

        val balanceMillis = seconds * 1000L
        val ageMillis = System.currentTimeMillis() - balanceMillis
        val sevenDaysMillis = 7L * 24L * 60L * 60L * 1000L

        return ageMillis > sevenDaysMillis
    }

    private fun appendHtml(
        builder: SpannableStringBuilder,
        html: String
    ) {
        builder.append(
            HtmlCompat.fromHtml(
                html,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )
    }
}


