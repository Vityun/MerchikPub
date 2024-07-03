package ua.com.merchik.merchik.features.main

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.RealmObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.data.RealmModels.LogDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import javax.inject.Inject
import kotlin.reflect.KClass

data class StateUI(
    val title: String = "",
    val items: List<ItemUI> = emptyList(),
    val settingsItems: List<SettingsItemUI> = emptyList(),
    val lastUpdate: Long = 0
)

abstract class MainViewModel(
    val repository: MainRepository,
    val nameUIRepository: NameUIRepository,
    protected val savedStateHandle: SavedStateHandle
): ViewModel() {

    abstract val contextUI: ContextUI
    abstract val table: KClass<out DataObjectUI>
    abstract fun getItems(): List<ItemUI>

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
                    title = "Title",
                    items = items ?: emptyList(),
                    settingsItems = settingsItems,
                    lastUpdate = System.currentTimeMillis()
                )
            }
        }
    }
}