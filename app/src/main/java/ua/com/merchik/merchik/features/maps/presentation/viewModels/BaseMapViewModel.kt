package ua.com.merchik.merchik.features.maps.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.features.maps.domain.PointUi
import ua.com.merchik.merchik.features.maps.domain.isValidLatLon
import ua.com.merchik.merchik.features.maps.domain.usecases.BuildMapPointsUC
import ua.com.merchik.merchik.features.maps.domain.usecases.FilterAndSortItemsUC
import ua.com.merchik.merchik.features.maps.domain.usecases.MakePointUiUC
import ua.com.merchik.merchik.features.maps.presentation.*


abstract class BaseMapViewModel(
    private val filterUC: FilterAndSortItemsUC,
    private val buildUC: BuildMapPointsUC,
    private val makeUiUC: MakePointUiUC,
    protected val radiusMeters: Double
) : ViewModel() {


    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state


    private val _effects = MutableSharedFlow<MapEffect>(extraBufferCapacity = 32)
    val effects: SharedFlow<MapEffect> = _effects

    private var bridge: MapActionsBridge? = null
    fun attachBridge(bridge: MapActionsBridge) { this.bridge = bridge }

    fun process(intent: MapIntent) {
        _state.update { MapReducer.reduce(it, intent) }
        when (intent) {
            is MapIntent.Init -> Unit
            is MapIntent.SetInput -> onSetInput(intent)
            is MapIntent.MarkerClicked -> onMarkerClicked(intent.point)
            MapIntent.ConfirmJump -> onConfirm()
            MapIntent.DismissConfirm -> Unit
        }
    }


    private fun onSetInput(i: MapIntent.SetInput) = viewModelScope.launch(Dispatchers.Default) {
        val filtered = filterUC(i.items, i.filters, i.sorting, i.rangeStartLocalDate, i.rangeEndLocalDate, i.search).items
        val (center, points, badges) = buildUC(filtered)
        val pointsUi = makeUiUC(center, points, badges, radiusMeters)


        _state.update { it.copy(isLoading = false, filtered = filtered, center = center, pointsUi = pointsUi, userLat = i.userLat, userLon = i.userLon) }


        val latLngs = buildList {
            center?.let { add(it.pos) }
            addAll(points.map { com.google.android.gms.maps.model.LatLng(it.lat, it.lon) })
            val uLat = i.userLat; val uLon = i.userLon
            if (isValidLatLon(uLat, uLon)) add(com.google.android.gms.maps.model.LatLng(uLat!!, uLon!!))
        }
        _effects.tryEmit(MapEffect.MoveCamera(latLngs, padding = 80, zoomIfSingle = 14f))
    }


    private fun onMarkerClicked(p: PointUi) {
        val wp = p.point.wp ?: return
        val b = bridge
        if (p.count == 1 && wp.user_id != 14041 && b != null) {
            _effects.tryEmit(MapEffect.OpenContextMenu(wp, b.contextUI))
        } else {
            _state.update { it.copy(pendingWp = wp, pendingStableId = p.point.dataItemsUI?.stableId) }
            _effects.tryEmit(MapEffect.ShowConfirm(wp, p.point.dataItemsUI?.stableId))
        }
    }


    private fun onConfirm() {
        val b = bridge ?: return
        val st = _state.value
        st.pendingStableId?.let { b.requestScrollToVisit(it) }
        st.pendingWp?.let { b.highlightByAddrId(it.addr_id.toString(), b.highlightColor) }
        b.dismissHost()
        _state.update { it.copy(pendingWp = null, pendingStableId = null) }
    }
}