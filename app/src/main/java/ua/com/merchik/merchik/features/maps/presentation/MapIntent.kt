package ua.com.merchik.merchik.features.maps.presentation

import com.google.android.gms.maps.model.LatLng
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.GroupingField
import ua.com.merchik.merchik.features.main.Main.SortingField
import ua.com.merchik.merchik.features.maps.domain.PointUi
import ua.com.merchik.merchik.features.maps.domain.StoreCenter
import java.time.LocalDate

sealed interface MapIntent {
    data class Init(val sessionId: Long) : MapIntent   // 👈 добавили id сессии
    data class SetInput(
        val items: List<DataItemUI>,
        val filters: Filters?,
        val sorting: List<SortingField?>,
        val grouping: List<GroupingField?>,
        val rangeStartLocalDate: LocalDate?, // поменяли тип на LocalDate?
        val rangeEndLocalDate: LocalDate?,
        val search: String?,
        val userLat: Double?,
        val userLon: Double?,
        val autoCenterOnSetInput: Boolean = true
    ) : MapIntent

    data class MarkerClicked(val point: PointUi) : MapIntent
    data object ConfirmJump : MapIntent
    data object DismissConfirm : MapIntent
    data class SetCircleRadius(val meters: Double?) : MapIntent

}


sealed interface MapEffect {
    data class OpenContextMenu(val wp: WpDataDB, val contextUI: ContextUI) : MapEffect
    data class ShowConfirm(val wp: WpDataDB, val stableId: Long?) : MapEffect
    data class MoveCamera(
        val sessionId: Long,                  // 👈 добавили
        val latLngs: List<LatLng>,
        val padding: Int,
        val zoomIfSingle: Float?
    ) : MapEffect
}

data class MapState(
    val isLoading: Boolean = false,
    val filtered: List<DataItemUI> = emptyList(),
    val center: StoreCenter? = null,
    val pointsUi: List<PointUi> = emptyList(),
    val userLat: Double? = null,
    val userLon: Double? = null,
    val pendingWp: WpDataDB? = null,
    val pendingStableId: Long? = null,
    val activePoint: PointUi? = null,             // активная точка null или id
    val autoBaselineRadiusMeters: Double? = null, // авто «самая дальняя + 50м»
    val customRadiusMeters: Double? = null,       // из меню; может быть null (нет сужения)
    val circleRadiusMeters: Double? = null        // эффективный (рисуем по нему)
)


object MapReducer {
    fun reduce(state: MapState, intent: MapIntent): MapState = when (intent) {
        is MapIntent.Init -> state.copy(
            isLoading = true,
            // НИЧЕГО больше не меняем — sessionId хранится во VM, не в state
        )

        is MapIntent.SetInput -> state.copy(isLoading = true, userLat = intent.userLat, userLon = intent.userLon)
        is MapIntent.MarkerClicked -> state.copy(activePoint = intent.point)
        MapIntent.ConfirmJump -> state  // <-- НИЧЕГО НЕ ДЕЛАЕМ!
        MapIntent.DismissConfirm -> state.copy(pendingWp = null, pendingStableId = null)
        is MapIntent.SetCircleRadius -> {
            val newCustom = intent.meters
            val eff = effectiveRadius(state.autoBaselineRadiusMeters, newCustom)
            state.copy(customRadiusMeters = newCustom, circleRadiusMeters = eff)
        }

    }
}


internal fun effectiveRadius(auto: Double?, custom: Double?): Double? =
    when {
        auto == null && custom == null -> null
        auto == null -> custom
        custom == null -> auto
        else -> minOf(auto, custom) // пользователь только уменьшает
    }