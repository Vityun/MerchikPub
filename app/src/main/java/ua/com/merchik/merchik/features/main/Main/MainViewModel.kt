package ua.com.merchik.merchik.features.main.Main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.features.main.DBViewModels.UsersSDBViewModel
import java.time.LocalDate
import java.util.UUID
import kotlin.reflect.KClass

data class StateUI(
    val title: String? = null,
    val subTitle: String? = null,
    val idResImage: Int? = null,
    val itemsHeader: List<DataItemUI> = emptyList(),
    val items: List<DataItemUI> = emptyList(),
    val itemsFooter: List<DataItemUI> = emptyList(),
    val settingsItems: List<SettingsItemUI> = emptyList(),
    var sortingFields: List<SortingField> = emptyList(),
    val filters: Filters? = null,
    val lastUpdate: Long = 0
)

data class Filters(
    val title: String = "Фільтри",
    val subTitle: String? = null,
    val rangeDataByKey: RangeDate? = null,
    val searchText: String,
    var items: List<ItemFilter>? = null
)

data class ItemFilter(
    val title: String,
    val clazz: KClass<out DataObjectUI>,
    val clazzViewModel: KClass<out MainViewModel>,
    val modeUI: ModeUI,
    val titleContext: String,
    val subTitleContext: String,
    val leftField: String,
    val rightField: String,
    val rightValuesRaw: List<String>,
    val rightValuesUI: List<String>,
    val enabled: Boolean,
) {
    fun onSelect(activity: Activity) {
        val intent = Intent(activity, FeaturesActivity::class.java)
        val bundle = Bundle()
        bundle.putString("viewModel", clazzViewModel.java.canonicalName)
        bundle.putString("modeUI", modeUI.toString())
        bundle.putString("title", titleContext)
        bundle.putString("subTitle", subTitleContext)
        intent.putExtras(bundle)
        ActivityCompat.startActivityForResult(
            activity,
            intent,
            DetailedReportActivity.NEED_UPDATE_UI_REQUEST,
            null
        )
    }
}

data class RangeDate(
    val key: String? = null,
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    val enabled: Boolean
)

data class SortingField(
    val key: String? = null,
    val title: String? = null,
    var order: Int? = null
)

data class SettingsUI(
    val hideFields: List<String>? = null,
    val sortFields: List<SortingField>? = null
)

abstract class MainViewModel(
    val repository: MainRepository,
    val nameUIRepository: NameUIRepository,
    protected val savedStateHandle: SavedStateHandle
): ViewModel() {

    var context: Context? = null
    var dataJson: String? = null
    var title: String? = null
    var subTitle: String? = null
    var idResImage: Int? = null
    var modeUI: ModeUI = ModeUI.DEFAULT
    var contextUI: ContextUI = ContextUI.DEFAULT

    abstract val table: KClass<out DataObjectUI>

    open fun getItemsHeader(): List<DataItemUI> = emptyList()

    open fun getItemsFooter(): List<DataItemUI> = emptyList()

    open fun updateFilters() {}

    abstract fun getItems(): List<DataItemUI>
    open fun onClickItem(itemUI: DataItemUI, context: Context) {}

    open fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {}

    var filters: Filters? = null

    private val _uiState = MutableStateFlow(StateUI())
    val uiState: StateFlow<StateUI>
        get() = _uiState.asStateFlow()

    fun getTranslateString(text: String, translateId: Long? = null) =
        nameUIRepository.getTranslateString(text, translateId)

    fun saveSettings() {
        viewModelScope.launch {
            repository.saveSettingsUI(
                table,
                SettingsUI(
                    hideFields = uiState.value.settingsItems.filter { !it.isEnabled }.map { it.key },
                    sortFields = uiState.value.sortingFields.filter { it.key != null }.map { it.copy(title = null) }
                ),
                contextUI)
        }
    }

    fun updateSorting(newSortingField: SortingField?, position: Int) {
        viewModelScope.launch {
            val newSortingFields = mutableListOf<SortingField>()
            newSortingFields.addAll(_uiState.value.sortingFields)
            newSortingField?.let {
                if (position < newSortingFields.size) newSortingFields[position] = it
                else newSortingFields.add(position, it)
            } ?: run {
                if (position < newSortingFields.size) newSortingFields[position] = SortingField()
            }
            _uiState.update {
                it.copy(
                    sortingFields = newSortingFields
                )
            }
        }
    }

    fun updateContent() {
        viewModelScope.launch {

            val settingsItems = repository.getSettingsItemList(table, contextUI)
            val sortingFields = repository.getSortingFields(table, contextUI)
            updateFilters()

            _uiState.update {
                it.copy(
                    title = title,
                    subTitle = subTitle,
                    idResImage = idResImage,
                    itemsHeader = getItemsHeader(),
                    items = getItems(),
                    itemsFooter = getItemsFooter(),
                    settingsItems = settingsItems,
                    sortingFields = sortingFields,
                    filters = filters,
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
                    filters = filters,
                    lastUpdate = System.currentTimeMillis()
                )
            }
        }
    }

    fun updateItemSelect(checked: Boolean, itemUI: DataItemUI){
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    itemsHeader = it.itemsHeader.map { oldItemUI ->
                        oldItemUI.copy(selected =
                        if (itemUI === oldItemUI) checked
                        else if (modeUI == ModeUI.ONE_SELECT) false else oldItemUI.selected)
                    },
                    items = it.items.map { oldItemUI ->
                        oldItemUI.copy(selected =
                        if (itemUI === oldItemUI) checked
                        else if (modeUI == ModeUI.ONE_SELECT) false else oldItemUI.selected)
                    },
                    itemsFooter = it.itemsFooter.map { oldItemUI ->
                        oldItemUI.copy(selected =
                        if (itemUI === oldItemUI) checked
                        else if (modeUI == ModeUI.ONE_SELECT) false else oldItemUI.selected)
                    },
                )
            }
        }
    }

    fun onChangeItemIndex(item: SettingsItemUI, offset: Int) {
//        repository.getSettingsItemList(table, contextUI)
    }
}