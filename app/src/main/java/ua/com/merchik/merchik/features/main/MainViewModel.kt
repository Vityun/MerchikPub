package ua.com.merchik.merchik.features.main

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import java.time.LocalDate
import kotlin.reflect.KClass

data class StateUI(
    val title: String? = null,
    val subTitle: String? = null,
    val idResImage: Int? = null,
    val items: List<ItemUI> = emptyList(),
    val settingsItems: List<SettingsItemUI> = emptyList(),
    val filters: Filters? = null,
    val lastUpdate: Long = 0
)

data class Filters(
    val rangeDataByKey: RangeDate? = null,
    val searchText: String
)

data class RangeDate(
    val key: String? = null,
    val start: LocalDate? = null,
    val end: LocalDate? = null
)

abstract class MainViewModel(
    val repository: MainRepository,
    val nameUIRepository: NameUIRepository,
    protected val savedStateHandle: SavedStateHandle
): ViewModel() {

    var dataJson: String? = null
    var title: String? = null
    var subTitle: String? = null
    var idResImage: Int? = null

    abstract val contextUI: ContextUI
    abstract val table: KClass<out DataObjectUI>
    abstract fun getItems(): List<ItemUI>
    open var filters: Filters? = null
    open fun onClickItem(itemUI: ItemUI, context: Context) {}
    open fun onSelectedItemsUI(itemsUI: List<ItemUI>) {}

    private val _uiState = MutableStateFlow(StateUI())
    val uiState: StateFlow<StateUI>
        get() = _uiState.asStateFlow()

    fun getTranslateString(text: String, translateId: Long? = null) =
        nameUIRepository.getTranslateString(text, translateId)

    fun saveSettings() {
        viewModelScope.launch {
            repository.saveSettingsUI(table, uiState.value.settingsItems, contextUI)
        }
    }

    fun updateContent() {
        viewModelScope.launch {

            val settingsItems = repository.getSettingsItemList(table, contextUI)

            _uiState.update {
                it.copy(
                    title = title,
                    subTitle = subTitle,
                    idResImage = idResImage,
                    items = getItems(),
                    settingsItems = settingsItems,
                    lastUpdate = System.currentTimeMillis()
                )
            }
        }
    }

    fun updateFilters(filters: Filters){
        this.filters = filters
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    filters = filters
                )
            }
        }
    }

    fun updateItemSelect(checked: Boolean, itemUI: ItemUI){
        viewModelScope.launch {
            _uiState.update {
                it.copy(items = it.items.map { oldItemUI ->
                    oldItemUI.copy(selected =
                    if (itemUI === oldItemUI) checked
                    else if (contextUI == ContextUI.ONE_SELECT) false else oldItemUI.selected)
                })
            }
        }
    }

    fun onChangeItemIndex(item: SettingsItemUI, offset: Int) {
//        repository.getSettingsItemList(table, contextUI)
    }
}