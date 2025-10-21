package ua.com.merchik.merchik.features.maps.presentation.main


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.maps.domain.parseDoubleSafe
import ua.com.merchik.merchik.features.maps.domain.stringByKey
import ua.com.merchik.merchik.features.maps.presentation.MainMapActionsBridge
import ua.com.merchik.merchik.features.maps.presentation.MapActionsBridge
import ua.com.merchik.merchik.features.maps.presentation.MapEffect
import ua.com.merchik.merchik.features.maps.presentation.MapIntent
import ua.com.merchik.merchik.features.maps.presentation.viewModels.BaseMapViewModel
import ua.com.merchik.merchik.features.maps.presentation.viewModels.MapFromMapsViewModel
import ua.com.merchik.merchik.features.maps.presentation.viewModels.MapFromWPdataViewModel


@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun MapsDialog(
    mainViewModel: MainViewModel,
    onDismiss: () -> Unit,
    contextUI: ContextUI,
    onOpenContextMenu: (WpDataDB, ContextUI) -> Unit
) {
    val uiState by mainViewModel.uiState.collectAsState()

    val highlightColor: Color = colorResource(id = R.color.ufmd_accept_t)

// Decide scenario once per input
    val hasLogCenter by remember(uiState.items) {
        mutableStateOf(
            uiState.items.firstOrNull {
                it.rawFields.stringByKey("log_addr_location_xd")?.parseDoubleSafe() != null &&
                        it.rawFields.stringByKey("log_addr_location_yd")?.parseDoubleSafe() != null
            } != null
        )
    }


    val vm: BaseMapViewModel =
        if (hasLogCenter) hiltViewModel<MapFromMapsViewModel>() else hiltViewModel<MapFromWPdataViewModel>()


// attach bridge сразу после получения VM
    LaunchedEffect(vm, onDismiss, contextUI, highlightColor) {
        vm.attachBridge(
            MainMapActionsBridge(
                mainVm = mainViewModel,
                onDismiss = onDismiss,
                contextUI = contextUI,
                highlightColor = highlightColor
            )
        )
    }
//    LaunchedEffect(vm, onDismiss, contextUI, highlightColor) {
//        vm.attachBridge(object : MapActionsBridge {
//            override val contextUI: ContextUI = contextUI
//            override val highlightColor: Color = highlightColor
//            override fun requestScrollToVisit(stableId: Long) =
//                mainViewModel.requestScrollToVisit(stableId)
//
//            override fun highlightByAddrId(addrId: String, color: Color) =
//                mainViewModel.highlightByAddrId(addrId, color)
//
//            override fun dismissHost() = onDismiss()
//        })
//    }

// pump inputs into VM (acts like controller/Coordinator)
    LaunchedEffect(
        uiState.items,
        uiState.filters,
        uiState.sortingFields,
        mainViewModel.rangeDataStart.value,
        mainViewModel.rangeDataEnd.value,
        uiState.filters?.searchText,
        Globals.CoordX,
        Globals.CoordY
    ) {
        vm.process(
            MapIntent.SetInput(
                items = uiState.items,
                filters = uiState.filters,
                sorting = uiState.sortingFields,
                rangeStartLocalDate = mainViewModel.rangeDataStart.value,
                rangeEndLocalDate = mainViewModel.rangeDataEnd.value,
                search = uiState.filters?.searchText,
                userLat = Globals.CoordX,
                userLon = Globals.CoordY
            )
        )
    }
    // Effects handling
    val effects = vm.effects
    val cameraController = rememberCameraPositionState()


    LaunchedEffect(Unit) {
        effects.collectLatest { e ->
            when (e) {
                is MapEffect.MoveCamera -> {
                    val latLngs = e.latLngs
                    try {
                        if (latLngs.size >= 2) {
                            val builder = LatLngBounds.builder()
                            latLngs.forEach { builder.include(it) }
                            cameraController.animate(CameraUpdateFactory.newLatLngBounds(builder.build(), e.padding))
                        } else if (latLngs.size == 1) {
                            cameraController.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLngs.first(),
                                    e.zoomIfSingle ?: 14f
                                )
                            )
                        }
                    } catch (_: Throwable) { /* ignore */
                    }
                }

                is MapEffect.OpenContextMenu -> onOpenContextMenu(e.wp, e.contextUI)
                is MapEffect.ShowConfirm -> {
// confirm dialog is drawn below from state; nothing to do here
                }
            }
        }
    }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(vertical = 40.dp)
                .background(Color.Transparent)
        ) {
            Row(modifier = Modifier.align(Alignment.End)) {
                ImageButton(
                    id = R.drawable.ic_settings,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                    onClick = { /* TODO: your WIP dialog */ }
                )
                ImageButton(
                    id = R.drawable.ic_letter_x,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                    onClick = onDismiss
                )
            }


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Text(text = "Карта", modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(8.dp))
                    Text(text = "## Опис можливих дій. Додасться поступово")
                    Spacer(Modifier.height(8.dp))


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    ) {
                        StoresMap(
                            cameraPositionState = cameraController,
                            vm = vm
                        )
                    }


                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
    // Confirm Dialog (bound to VM state)
    val state by vm.state.collectAsState()
    if (state.pendingWp != null) {
        val wp = state.pendingWp
        MessageDialog(
            title = "Перейти к посещенням",
            status = DialogStatus.NORMAL,
            subTitle = wp?.addr_txt,
            message = "Показать все работы по этому адресу за выбранный период?",
            onDismiss = { vm.process(MapIntent.DismissConfirm) },
            onConfirmAction = { vm.process(MapIntent.ConfirmJump) },
            onCancelAction = { vm.process(MapIntent.DismissConfirm) }
        )
    }
}