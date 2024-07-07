package ua.com.merchik.merchik.features.main

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
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
    val title: String = "",
    val items: List<ItemUI> = emptyList(),
    val settingsItems: List<SettingsItemUI> = emptyList(),
    val lastUpdate: Long = 0
)

data class Filters(
    val rangeDataByKey: RangeDate,
    val searchText: String
)

data class RangeDate(
    val key: String,
    val start: LocalDate,
    val end: LocalDate
)

abstract class MainViewModel(
    val repository: MainRepository,
    val nameUIRepository: NameUIRepository,
    protected val savedStateHandle: SavedStateHandle
): ViewModel() {

    open val title: String = "Довідник"
    abstract val contextUI: ContextUI
    abstract val table: KClass<out DataObjectUI>
    abstract fun getItems(): List<ItemUI>
    open fun getFilters(): Filters? = null
    open fun onClickItemImage(itemUI: ItemUI, activity: AppCompatActivity) {}

//    private val contextUI = ContextUI.MAIN
//    private val table = LogDB::class

//    private val table = PromoDB::class
//    private val table = ArticleDB::class
//    private val table = TovarDB::class
//    private val table = SiteObjectsDB::class
//    private val table = CustomerSDB::class
//    private val table = PlanogrammSDB::class
//    private val table = AddressSDB::class
//    private val table = UsersSDB::class

    private val _uiState = MutableStateFlow(StateUI())
    val uiState: StateFlow<StateUI>
        get() = _uiState.asStateFlow()

    init {
        updateContent()
    }

    fun getTranslateString(text: String, translateId: Long? = null) =
        nameUIRepository.getTranslateString(text, translateId)

    fun saveSettings() {
        viewModelScope.launch {
            repository.saveSettingsUI(table, uiState.value.settingsItems, contextUI)
        }
    }

    fun updateContent() {
        viewModelScope.launch {

            val items = getItems()

            val settingsItems = repository.getSettingsItemList(table, contextUI)

            _uiState.update {
                it.copy(
                    title = title,
                    items = items ?: emptyList(),
                    settingsItems = settingsItems,
                    lastUpdate = System.currentTimeMillis()
                )
            }
        }
    }

    fun onChangeItemIndex(item: SettingsItemUI, offset: Int) {
//        repository.getSettingsItemList(table, contextUI)
    }
}