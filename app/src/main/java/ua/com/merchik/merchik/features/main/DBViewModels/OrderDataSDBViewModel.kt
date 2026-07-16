package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Database.Room.OrderDataSDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class OrderDataSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = OrderDataSDB::class

//    override fun getDefaultHideUserFields(): List<String>? {
//        return "id, order_type_id, dt_create, dt_smeta_ymd, smeta_id, time_start, time_end, price_act, invoice_id, price_paid, order_status".split(
//            ","
//        )
//    }

    override fun getDefaultSortUserFields(): List<String>? {
        return "order_id".split(",")
    }

    override suspend fun getItems(): List<DataItemUI> {
        val selectedId = CustomAditionalOrderSelectionHolder.selectedId
        val filterValues = FilteringDialogDataHolder.instance()
            .filters
            ?.items
            ?.firstOrNull { it.clazz == table }
            ?.rightValuesRaw

        return repository.getAllRoom(OrderDataSDB::class, contextUI, null)
            .map { item ->
                val order = item.rawObj.firstOrNull { it is OrderDataSDB } as? OrderDataSDB
                val orderId = order?.id?.takeIf { it.isNotBlank() } ?: order?.orderId
                item.copy(
                    selected = selectedId?.let { it == order?.id || it == order?.orderId } == true ||
                            orderId?.let { filterValues?.contains(it) } == true
                )
            }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        val selectedOrder = itemsUI.firstOrNull()
            ?.rawObj
            ?.firstOrNull { it is OrderDataSDB } as? OrderDataSDB

        CustomAditionalOrderSelectionHolder.set(
            id = selectedOrder?.id?.takeIf { it.isNotBlank() } ?: selectedOrder?.orderId,
            name = selectedOrder?.toCustomAditionalTitle()
        )

        when (contextUI) {

            ContextUI.DEFAULT -> {
                FilteringDialogDataHolder.instance().filters.apply {
                    this?.let { filters ->
                        filters.items = filters.items.map { itemFilter ->
                            if (itemFilter.clazz == table) {
                                val rightValuesRaw = mutableListOf<String>()
                                val rightValuesUI = mutableListOf<String>()
                                itemsUI.forEach {
                                    (it.rawObj.firstOrNull() as? OrderDataSDB)?.let {
                                        rightValuesRaw.add(it.id)
                                        rightValuesUI.add(it.toCustomAditionalTitle())
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
            else -> {
                Log.e("OrderDataSDBViewModel", "onSelectedItemsUI -> empty")
            }
        }
    }

    private fun OrderDataSDB.toCustomAditionalTitle(): String {
        val number = (orderId ?: id)
            .takeIf { it.isNotBlank() }
            ?.let { "№$it" }
        val description = orderStatusTxt
            ?.takeIf { it.isNotBlank() }
            ?: orderType?.takeIf { it.isNotBlank() }

        return listOfNotNull(number, description)
            .joinToString(" - ")
            .ifBlank { "(нет данных)" }
    }
}

data class CustomAditionalOrderSelection(
    val id: String?,
    val name: String?
)

object CustomAditionalOrderSelectionHolder {
    var selectedId: String? = null
        private set
    var selectedName: String? = null
        private set

    fun set(id: String?, name: String?) {
        selectedId = id
        selectedName = name
    }

    fun consume(): CustomAditionalOrderSelection? {
        if (selectedId == null && selectedName == null) return null
        val result = CustomAditionalOrderSelection(selectedId, selectedName)
        clear()
        return result
    }

    fun clear() {
        selectedId = null
        selectedName = null
    }
}
