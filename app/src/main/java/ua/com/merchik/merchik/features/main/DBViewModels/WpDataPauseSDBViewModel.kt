package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.data.Database.Room.WPDataPauseSDB
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class WpDataPauseSDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = WPDataPauseSDB::class

    override fun getDefaultHideUserFields(): List<String>? {
        return "code_dad2, dt_update_client, uploadStatus".split(",")
    }

    override fun updateFilters() {
        val codeDad2 = getCodeDad2FromDataJson() ?: return
        val codeDad2Text = codeDad2.toString()
        val prev = uiState.value.filters ?: Filters()

        updateFilters(
            prev.copy(
                items = listOf(
                    ItemFilter(
                        title = "code_dad2",
                        clazz = WPDataPauseSDB::class,
                        clazzViewModel = WpDataPauseSDBViewModel::class,
                        modeUI = ModeUI.MULTI_SELECT,
                        titleContext = "Dad2",
                        subTitleContext = "Паузы текущего визита",
                        leftField = "code_dad2",
                        rightField = "code_dad2",
                        rightValuesRaw = listOf(codeDad2Text),
                        rightValuesUI = listOf(codeDad2Text),
                        enabled = false
                    )
                ),
                rangeDataByKey = null
            )
        )
    }

    override suspend fun getItems(): List<DataItemUI> {
        val codeDad2 = getCodeDad2FromDataJson()
        val data = if (codeDad2 != null) {
            RoomManager.SQL_DB.wpDataPauseDao().getAllByDad2(codeDad2)
        } else {
            RoomManager.SQL_DB.wpDataPauseDao().getAll()
        }
        return repository.toItemUIList(WPDataPauseSDB::class, data, contextUI, null)
    }

    private fun getCodeDad2FromDataJson(): Long? {
        val raw = dataJson?.takeIf { it.isNotBlank() } ?: return null
        return runCatching {
            val element = JsonParser.parseString(raw)
            when {
                element.isJsonPrimitive -> element.asLongOrNull()
                element.isJsonObject -> element.asJsonObject.codeDad2OrNull()
                else -> null
            }
        }.getOrNull()?.takeIf { it > 0L }
    }

    private fun JsonObject.codeDad2OrNull(): Long? {
        return longOrNull("codeDad2")
            ?: longOrNull("code_dad2")
            ?: get("nameValuePairs")
                ?.takeIf { it.isJsonObject }
                ?.asJsonObject
                ?.let { nested ->
                    nested.longOrNull("codeDad2") ?: nested.longOrNull("code_dad2")
                }
    }

    private fun JsonObject.longOrNull(key: String): Long? {
        return get(key)?.asLongOrNull()
    }

    private fun JsonElement.asLongOrNull(): Long? {
        if (isJsonNull) return null
        return runCatching { asLong }.getOrNull()
    }
}
