package ua.com.merchik.merchik.Activities.DetailedReportActivity



// ComposeHosts.kt


import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.google.maps.android.compose.rememberCameraPositionState
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.MainViewModelImpl
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.maps.presentation.MainMapActionsBridge
import ua.com.merchik.merchik.features.maps.presentation.MapIntent
import ua.com.merchik.merchik.features.maps.presentation.main.MapsDialog
import ua.com.merchik.merchik.features.maps.presentation.main.StoresMap
import ua.com.merchik.merchik.features.maps.presentation.viewModels.BaseMapViewModel
import ua.com.merchik.merchik.features.maps.presentation.viewModels.MapFromMapsViewModel
import androidx.compose.runtime.collectAsState

@Composable
private fun StoresMapFromMapsHost(wpData: WpDataDB) {
    val mainVm: MainViewModel = hiltViewModel<MainViewModelImpl>()
    mainVm.dataJson = Gson().toJson(wpData)
    val mapVm: MapFromMapsViewModel = hiltViewModel()

    val camera = rememberCameraPositionState()

    val highlightColor = colorResource(id = R.color.selected_item)
    val contextUI = mainVm.contextUI

    var showMapsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { mainVm.updateContent() }

    LaunchedEffect(mapVm, mainVm, highlightColor, contextUI) {
        mapVm.attachBridge(
            MainMapActionsBridge(
                mainVm = mainVm,
                onDismiss = { },
                contextUI = contextUI,
                highlightColor = highlightColor
            )
        )
    }

    val uiState by mainVm.uiState.collectAsState()
    val rangeStart by mainVm.rangeDataStart.collectAsState()
    val rangeEnd by mainVm.rangeDataEnd.collectAsState()
    val distance by mainVm.offsetDistanceMeters.collectAsState()

    LaunchedEffect(
        uiState.items,
        uiState.filters,
        uiState.sortingFields,
        uiState.groupingFields,
        rangeStart,
        rangeEnd,
        uiState.filters?.searchText,
        Globals.CoordX,
        Globals.CoordY,
        mapVm,
        distance
    ) {
        mapVm.process(
            MapIntent.SetInput(
                items = uiState.items,
                filters = uiState.filters,
                sorting = uiState.sortingFields,
                grouping = uiState.groupingFields,
                rangeStartLocalDate = rangeStart,
                rangeEndLocalDate = rangeEnd,
                search = uiState.filters?.searchText,
                userLat = Globals.CoordX,
                userLon = Globals.CoordY,
                distanceMeters = distance,
                autoCenterOnSetInput = false
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        StoresMap(
            cameraPositionState = camera,
            vm = mapVm as BaseMapViewModel
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            showMapsDialog = true
                        },
                        onPress = {
                            showMapsDialog = true
                        }
                    )
                }
        )
    }

//    if (showMapsDialog) {
//        MapsDialog(
//            mainViewModel = mainVm,
//            onDismiss = { showMapsDialog = false },
//            onOpenContextMenu = { wp, ctxUI, _ ->
//                mainVm.openContextMenu(wp, ctxUI)
//            }
//        )
//    }
}

/** Расширение для удобного вызова из Java. */
@JvmOverloads
fun attachStoresMapFromMaps(
    composeView: ComposeView,
    wpData: WpDataDB
) {

    composeView.setViewCompositionStrategy(
        ViewCompositionStrategy.DisposeOnDetachedFromWindow
    )
    composeView.setContent {
        StoresMapFromMapsHost(wpData)
    }
}