package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
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
class AddressSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = AddressSDB::class

    override suspend fun getItems(): List<DataItemUI> {
        val selectedId = CustomAditionalAddressSelectionHolder.selectedId
        val filterValues = FilteringDialogDataHolder.instance()
            .filters
            ?.items
            ?.firstOrNull { it.clazz == table }
            ?.rightValuesRaw

        return repository.getAllRoom(AddressSDB::class, contextUI, null)
            .map { item ->
                val address = item.rawObj.firstOrNull { it is AddressSDB } as? AddressSDB
                val addressId = address?.id?.toString()
                item.copy(
                    selected = selectedId?.let { it == addressId } == true ||
                            addressId?.let { filterValues?.contains(it) } == true
                )
            }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        val selectedAddress = itemsUI.firstOrNull()
            ?.rawObj
            ?.firstOrNull { it is AddressSDB } as? AddressSDB

        CustomAditionalAddressSelectionHolder.set(
            id = selectedAddress?.id?.toString(),
            name = selectedAddress?.toCustomAditionalTitle()
        )

        when (contextUI) {
            ContextUI.DEFAULT -> {
                FilteringDialogDataHolder.instance().filters?.let { filters ->
                    filters.items = filters.items.map { itemFilter ->
                        if (itemFilter.clazz == table) {
                            val rightValuesRaw = mutableListOf<String>()
                            val rightValuesUI = mutableListOf<String>()

                            itemsUI.forEach { item ->
                                (item.rawObj.firstOrNull() as? AddressSDB)?.let { address ->
                                    address.id?.toString()?.let { rightValuesRaw.add(it) }
                                    rightValuesUI.add(address.toCustomAditionalTitle())
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
            else -> {
                Log.e("AddressSDBViewModel", "onSelectedItemsUI -> empty")
            }
        }
    }

    private fun AddressSDB.toCustomAditionalTitle(): String {
        return nm
            ?.takeIf { it.isNotBlank() }
            ?: id?.toString()?.takeIf { it.isNotBlank() }?.let { "#$it" }
            ?: "(нет данных)"
    }
}

data class CustomAditionalAddressSelection(
    val id: String?,
    val name: String?
)

object CustomAditionalAddressSelectionHolder {
    var selectedId: String? = null
        private set
    var selectedName: String? = null
        private set
    var mapSelectionEnabled: Boolean = false
        private set
    var mapSubtitle: String? = null
        private set
    var mapDistanceMeters: Float = 2_000f
        private set

    fun set(id: String?, name: String?) {
        selectedId = id
        selectedName = name
    }

    fun configureMapSelection(
        subtitle: String?,
        distanceMeters: Float = 2_000f
    ) {
        mapSelectionEnabled = true
        mapSubtitle = subtitle
        mapDistanceMeters = distanceMeters
    }

    fun consume(): CustomAditionalAddressSelection? {
        if (selectedId == null && selectedName == null) {
            clear()
            return null
        }
        val result = CustomAditionalAddressSelection(selectedId, selectedName)
        clear()
        return result
    }

    fun clear() {
        selectedId = null
        selectedName = null
        mapSelectionEnabled = false
        mapSubtitle = null
        mapDistanceMeters = 2_000f
    }
}
