package ua.com.merchik.merchik.features.main.DBViewModels

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB
import ua.com.merchik.merchik.data.RealmModels.LogDB
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.tables.ImagesTypeListRealm
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class ImagesTypeListDBViewModel @Inject constructor(
    application: Application,
    repository: MainRepository,
    nameUIRepository: NameUIRepository,
    savedStateHandle: SavedStateHandle
) : MainViewModel(application, repository, nameUIRepository, savedStateHandle) {

    override val table: KClass<out DataObjectUI>
        get() = ImagesTypeListDB::class

    override fun getDefaultHideUserFields(): List<String> = listOf("column_name", "id")

    override fun updateFilters() {
        val excludedIds = getExcludedImageTypeIds()
        val imageTypesById = if (excludedIds.isEmpty()) {
            emptyMap()
        } else {
            ImagesTypeListRealm.getAll().associateBy { it.id }
        }
        filters = Filters(
            items = if (excludedIds.isEmpty()) {
                emptyList()
            } else {
                listOf(
                    ItemFilter(
                        title = "Виключити типи фото",
                        clazz = table,
                        modeUI = ModeUI.MULTI_SELECT,
                        titleContext = "Типи фото",
                        subTitleContext = "",
                        leftField = "id",
                        rightField = "id",
                        rightValuesRaw = excludedIds.map { it.toString() },
                        rightValuesUI = excludedIds.map { id ->
                            "($id) ${imageTypesById[id]?.nm.orEmpty()}"
                        },
                        enabled = false,
                        excludeMode = true
                    )
                )
            }
        )
    }

    override suspend fun getItems(): List<DataItemUI> {
        val showId = isIdVisibleInSettings()
        return repository.getAllRealm(ImagesTypeListDB::class, contextUI, null)
            .map {
                val imageType = it.rawObj.firstOrNull { raw -> raw is ImagesTypeListDB } as? ImagesTypeListDB
                val selected = FilteringDialogDataHolder.instance()
                    .filters
                    ?.items
                    ?.firstOrNull { it.clazz == table }
                    ?.rightValuesRaw
                    ?.contains(imageType?.id?.toString())

                val displayFields = it.fields
                    .filterNot { field -> field.key.equals("id", ignoreCase = true) }
                    .map { field ->
                        if (field.key.equals("nm", ignoreCase = true)) {
                            val displayName = imageType?.let { type ->
                                val name = type.nm.orEmpty()
                                if (showId) type.id?.let { id -> "($id) $name" } ?: name else name
                            }.orEmpty()
                            field.copy(
                                field = field.field.copy(value = ""),
                                value = field.value.copy(value = displayName)
                            )
                        } else {
                            field
                        }
                    }

                it.copy(fields = displayFields, selected = selected == true)
            }
    }

    private fun isIdVisibleInSettings(): Boolean {
        val hiddenFields = repository.getSettingsUI(table.java, contextUI, settingsVisitId)?.hideFields
            ?: getDefaultHideUserFields().orEmpty()
        return hiddenFields.none { it.trim().equals("id", ignoreCase = true) }
    }

    private fun getExcludedImageTypeIds(): Set<Int> {
        val ids = runCatching { JSONObject(dataJson ?: "{}").optJSONArray("excludedTypeIds") }
            .getOrNull() ?: return emptySet()
        return buildSet {
            for (index in 0 until ids.length()) {
                ids.optInt(index, -1).takeIf { it >= 0 }?.let(::add)
            }
        }
    }

    override fun onSelectedItemsUI(itemsUI: List<DataItemUI>) {
        FilteringDialogDataHolder.instance().filters.apply {
            this?.let {filters ->
                filters.items = filters.items.map { itemFilter ->
                    if (itemFilter.clazz == table) {
                        val rightValuesRaw = mutableListOf<String>()
                        val rightValuesUI = mutableListOf<String>()
                        itemsUI.forEach {
                            (it.rawObj.firstOrNull() as? ImagesTypeListDB)?.let {
                                rightValuesRaw.add(it.id.toString())
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
}
