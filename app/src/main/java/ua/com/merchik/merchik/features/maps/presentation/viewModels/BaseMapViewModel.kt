package ua.com.merchik.merchik.features.maps.presentation.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.types.geo.Distance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.addrIdOrNull
import ua.com.merchik.merchik.features.maps.domain.PointUi
import ua.com.merchik.merchik.features.maps.domain.haversine
import ua.com.merchik.merchik.features.maps.domain.isValidLatLon
import ua.com.merchik.merchik.features.maps.domain.usecases.BuildMapPointsUC
import ua.com.merchik.merchik.features.maps.domain.usecases.FilterAndSortItemsUC
import ua.com.merchik.merchik.features.maps.domain.usecases.MakePointUiUC
import ua.com.merchik.merchik.features.maps.presentation.*


abstract class BaseMapViewModel(
    private val filterUC: FilterAndSortItemsUC,
    private val buildUC: BuildMapPointsUC,
    private val makeUiUC: MakePointUiUC
) : ViewModel() {


    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state

    // ❗️Без буфера и без реплея, чтобы не тянуть старые эффекты
    private val _effects = MutableSharedFlow<MapEffect>(
        replay = 0,
        extraBufferCapacity = 0
    )
    val effects: SharedFlow<MapEffect> = _effects

    private var bridge: MapActionsBridge? = null
    fun attachBridge(bridge: MapActionsBridge) {
        this.bridge = bridge
    }

    private var didAutoCenter = false
    private var currentSessionId: Long = 0L     // 👈 текущая сессия


    fun process(intent: MapIntent) {
        _state.update { MapReducer.reduce(it, intent) }
        when (intent) {
            is MapIntent.Init -> {
                didAutoCenter = false
                currentSessionId = intent.sessionId   // 👈 запомнили сессию
            }

            is MapIntent.SetInput -> onSetInput(intent)
            is MapIntent.MarkerClicked -> onMarkerClicked(intent.point)
            MapIntent.ConfirmJump -> onConfirm()
            MapIntent.DismissConfirm -> Unit
//            is MapIntent.SetCircleRadius -> {}
        }
    }


    private fun onSetInput(i: MapIntent.SetInput) = viewModelScope.launch(Dispatchers.Default) {
        val filtered =
            filterUC(i.items, i.filters, i.sorting, i.grouping, i.rangeStartLocalDate, i.rangeEndLocalDate, i.search).items
        val (center, points, badges) = buildUC(filtered)
        val effectiveDistance = i.distanceMeters?.toDouble() ?: 5_000.0
        val pointsUi = makeUiUC(center, points, badges, effectiveDistance)
        Log.d("MAP_DEBUG", "distance=${i.distanceMeters}, pointsUi.size=${pointsUi.size}")

        _state.update {
            it.copy(
                isLoading = false,
                filtered = filtered,
                center = center,
                pointsUi = pointsUi,
                userLat = i.userLat,
                userLon = i.userLon
            )
        }

        // если FromWPdata (center == null) — считаем радиус от userLocation
        val baseline: Double? = run {
            val uLat = i.userLat
            val uLon = i.userLon
            if (center == null && isValidLatLon(uLat, uLon) && points.isNotEmpty()) {
                val maxDist = points.maxOf { p -> haversine(uLat!!, uLon!!, p.lat, p.lon) }
                maxDist + 50.0       // + 50 м
            } else null
        }

        _state.update { st ->
            val newAuto = if (center != null) null else baseline
            val newCustom = i.distanceMeters?.toDouble()
            val eff = effectiveRadius(newAuto, newCustom)
            st.copy(
                autoBaselineRadiusMeters = newAuto,
                customRadiusMeters = newCustom,
                circleRadiusMeters = eff
            )
        }

        val latLngs = buildList {
            center?.let { add(it.pos) }
            addAll(points.map { com.google.android.gms.maps.model.LatLng(it.lat, it.lon) })
            val uLat = i.userLat;
            val uLon = i.userLon
            if (isValidLatLon(uLat, uLon)) add(com.google.android.gms.maps.model.LatLng(uLat!!, uLon!!))
        }
        // авто-фит ТОЛЬКО если разрешено и ещё не делали
        if (i.autoCenterOnSetInput && !didAutoCenter && latLngs.isNotEmpty()) {
            _effects.emit(MapEffect.MoveCamera(currentSessionId, latLngs, padding = 80, zoomIfSingle = 14f))
            didAutoCenter = true
        }

    }


    private fun onMarkerClicked(p: PointUi) {
        val wp = p.point.wp ?: return
        val b = bridge
        if (wp.user_id != 14041 ) {
            if (p.count == 1) {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            pendingWp = wp,
                            pendingStableId = p.point.dataItemsUI?.stableId
                        )
                    }
                    _effects.emit(MapEffect.OpenContextMenu(wp, ContextUI.WP_DATA_IN_CONTAINER))
                }
            } else {
//            _effects.tryEmit(MapEffect.ShowConfirm(wp, p.point.dataItemsUI?.stableId))
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            pendingWp = wp,
                            pendingStableId = p.point.dataItemsUI?.stableId
                        )
                    }
                    _effects.emit(
                        MapEffect.OpenContextMenu(
                            wp,
                            ContextUI.WP_DATA_IN_CONTAINER_MULT
                        )
                    )
                }
            }
        } else {
//            if (p.count == 1) {
//                viewModelScope.launch {
//                    _state.update {
//                        it.copy(
//                            pendingWp = wp,
//                            pendingStableId = p.point.dataItemsUI?.stableId
//                        )
//                    }
//                    _effects.emit(MapEffect.OpenContextMenu(wp, ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER))
//                }
//            } else {
//            _effects.tryEmit(MapEffect.ShowConfirm(wp, p.point.dataItemsUI?.stableId))
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            pendingWp = wp,
                            pendingStableId = p.point.dataItemsUI?.stableId
                        )
                    }
                    _effects.emit(
                        MapEffect.OpenContextMenu(
                            wp,
                            ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER_MULT
                        )
                    )
//                }
            }
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