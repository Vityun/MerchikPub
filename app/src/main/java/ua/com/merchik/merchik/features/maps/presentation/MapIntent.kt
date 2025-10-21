package ua.com.merchik.merchik.features.maps.presentation

import com.google.android.gms.maps.model.LatLng
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.SortingField
import ua.com.merchik.merchik.features.maps.domain.PointUi
import ua.com.merchik.merchik.features.maps.domain.StoreCenter
import java.time.LocalDate

sealed interface MapIntent {
    data object Init : MapIntent
    data class SetInput(
        val items: List<DataItemUI>,
        val filters: Filters?,
        val sorting: List<SortingField?>,
        val rangeStartLocalDate: LocalDate?, // поменяли тип на LocalDate?
        val rangeEndLocalDate: LocalDate?,
        val search: String?,
        val userLat: Double?,
        val userLon: Double?
    ) : MapIntent



    data class MarkerClicked(val point: PointUi) : MapIntent
    data object ConfirmJump : MapIntent
    data object DismissConfirm : MapIntent
}


sealed interface MapEffect {
    data class OpenContextMenu(val wp: WpDataDB, val contextUI: ContextUI) : MapEffect
    data class ShowConfirm(val wp: WpDataDB, val stableId: Long?) : MapEffect
    data class MoveCamera(val latLngs: List<LatLng>, val padding: Int, val zoomIfSingle: Float?) : MapEffect
}


data class MapState(
    val isLoading: Boolean = false,
    val filtered: List<DataItemUI> = emptyList(),
    val center: StoreCenter? = null,
    val pointsUi: List<PointUi> = emptyList(),
    val userLat: Double? = null,
    val userLon: Double? = null,
    val pendingWp: WpDataDB? = null,
    val pendingStableId: Long? = null
)


object MapReducer {
    fun reduce(state: MapState, intent: MapIntent): MapState = when (intent) {
        MapIntent.Init -> state.copy(isLoading = true)
        is MapIntent.SetInput -> state.copy(isLoading = true, userLat = intent.userLat, userLon = intent.userLon)
        is MapIntent.MarkerClicked -> state
        MapIntent.ConfirmJump -> state.copy(pendingWp = null, pendingStableId = null)
        MapIntent.DismissConfirm -> state.copy(pendingWp = null, pendingStableId = null)
    }
}