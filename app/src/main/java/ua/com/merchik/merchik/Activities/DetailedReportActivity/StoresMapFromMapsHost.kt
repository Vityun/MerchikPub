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
import ua.com.merchik.merchik.features.main.DBViewModels.LogMPDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.maps.presentation.MainMapActionsBridge
import ua.com.merchik.merchik.features.maps.presentation.MapIntent
import ua.com.merchik.merchik.features.maps.presentation.main.MapsDialog
import ua.com.merchik.merchik.features.maps.presentation.main.StoresMap
import ua.com.merchik.merchik.features.maps.presentation.viewModels.BaseMapViewModel
import ua.com.merchik.merchik.features.maps.presentation.viewModels.MapFromMapsViewModel

@Composable
private fun StoresMapFromMapsHost(wpData: WpDataDB) {
    // 1) Вьюмодели
    val mainVm: MainViewModel = hiltViewModel<MainViewModelImpl>()
    mainVm.dataJson = Gson().toJson(wpData)
    val mapVm: MapFromMapsViewModel = hiltViewModel()

    // 2) Камера
    val camera = rememberCameraPositionState()

    // 3) Цвет/контекст
    val highlightColor = colorResource(id = R.color.ufmd_accept_t)
    val contextUI = mainVm.contextUI

    // 4) Локальный стейт диалога
    var showMapsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { mainVm.updateContent() }

    // 5) Мост
    LaunchedEffect(mapVm, mainVm, highlightColor, contextUI) {
        mapVm.attachBridge(
            MainMapActionsBridge(
                mainVm = mainVm,
                onDismiss = { /* ничего, диалог здесь отдельный */ },
                contextUI = contextUI,
                highlightColor = highlightColor
            )
        )
    }

    // 6) Подкидываем входные данные
    val uiState by mainVm.uiState.collectAsState()
    LaunchedEffect(
        uiState.items,
        uiState.filters,
        uiState.sortingFields,
        mainVm.rangeDataStart.value,
        mainVm.rangeDataEnd.value,
        uiState.filters?.searchText,
        Globals.CoordX,
        Globals.CoordY,
        mapVm
    ) {
        mapVm.process(
            MapIntent.SetInput(
                items = uiState.items,
                filters = uiState.filters,
                sorting = uiState.sortingFields,
                grouping = uiState.groupingFields,
                rangeStartLocalDate = mainVm.rangeDataStart.value,
                rangeEndLocalDate = mainVm.rangeDataEnd.value,
                search = uiState.filters?.searchText,
                userLat = Globals.CoordX,
                userLon = Globals.CoordY,
                autoCenterOnSetInput = false
            )
        )
    }

    // 7) Карта + прозрачный клик-оверлей
    Box(modifier = Modifier.fillMaxSize()) {
        StoresMap(
            cameraPositionState = camera,
            vm = mapVm as BaseMapViewModel
        )

        // Прозрачный слой поверх карты: любой тап => открыть диалог
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            showMapsDialog = true
                        },
                        onPress = {
                            // тоже можем открыть по любому "press"
                            showMapsDialog = true
                        }
                    )
                }
        )
    }

    // 8) Собственно MapsDialog, когда нужно
    if (showMapsDialog) {
        MapsDialog(
            mainViewModel = mainVm,
            onDismiss = { showMapsDialog = false },
            contextUI = contextUI,
            onOpenContextMenu = { wp, ctxUI ->
                mainVm.openContextMenu(wp, ctxUI)
            }
        )
    }
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