package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Activities.DetailedReportActivity.StoresMapFromMapsHost
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.features.indicator.LineSpinFadeLoaderIndicator
import ua.com.merchik.merchik.features.maps.domain.StoreCenter
import ua.com.merchik.merchik.features.maps.domain.isValidLatLon
import ua.com.merchik.merchik.features.maps.presentation.MapIntent
import ua.com.merchik.merchik.features.maps.presentation.main.StoresMap
import ua.com.merchik.merchik.features.maps.presentation.viewModels.MapFromMapsViewModel

@JvmOverloads
fun attachTARVisitMap(composeView: ComposeView, sourceDad2: Long, addressId: Int = 0) {
    composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    composeView.setContent {
        MerchikTheme {
            var visit by remember(sourceDad2) { mutableStateOf<WpDataDB?>(null) }
            var failed by remember(sourceDad2) { mutableStateOf(false) }
            var attempt by remember(sourceDad2) { mutableStateOf(0) }
            var fallbackAddress by remember(sourceDad2, addressId) { mutableStateOf<AddressSDB?>(null) }

            LaunchedEffect(sourceDad2, addressId, attempt) {
                failed = false
                visit = null
                fallbackAddress = null
                if (addressId > 0) {
                    try {
                        fallbackAddress = withContext(Dispatchers.IO) {
                            RoomManager.SQL_DB.addressDao().getById(addressId)
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        Globals.writeToMLOG("ERROR", "TARVisitMap/address", "addrId=$addressId, error=$error")
                    }
                }
                try {
                    visit = TARTovarDataLoader.loadVisit(sourceDad2)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    failed = true
                    Globals.writeToMLOG("ERROR", "TARVisitMap/load",
                        "sourceDad2=$sourceDad2, addrId=$addressId, addressFallback=${hasTarMapCoordinates(fallbackAddress)}, error=$error")
                }
            }

            val wpData = visit
            if (wpData != null) {
                StoresMapFromMapsHost(wpData)
            } else if (failed && hasTarMapCoordinates(fallbackAddress)) {
                val address = checkNotNull(fallbackAddress)
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) { TARAddressMap(address) }
                    Text("Вихідне відвідування недоступне. Показано адресу ЗіР.", modifier = Modifier.padding(8.dp))
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (failed) {
                        TextButton(onClick = { attempt++ }, modifier = Modifier.padding(16.dp)) {
                            Text("Вихідне відвідування недоступне, координати адреси відсутні. Повторити")
                        }
                    } else {
                        LineSpinFadeLoaderIndicator(
                            color = Color.Gray,
                            modifier = Modifier.size(36.dp),
                            radius = 12f, elementHeight = 5f, penThickness = 3f
                        )
                    }
                }
            }
        }
    }
}

internal fun hasTarMapCoordinates(address: AddressSDB?): Boolean =
    address != null && isValidLatLon(address.locationXd?.toDouble(), address.locationYd?.toDouble())

@Composable
private fun TARAddressMap(address: AddressSDB) {
    val mapVm: MapFromMapsViewModel = hiltViewModel(key = "tar-address-map-${address.id}")
    val camera = rememberCameraPositionState()
    LaunchedEffect(address.id, address.locationXd, address.locationYd) {
        // Address-only fallback: never invent a visit or show today's movements as historical ones.
        mapVm.process(tarAddressMapInput(address))
    }
    StoresMap(cameraPositionState = camera, vm = mapVm)
}

internal fun tarAddressMapInput(address: AddressSDB): MapIntent.SetPointsInput {
    require(hasTarMapCoordinates(address))
    return MapIntent.SetPointsInput(
        center = StoreCenter(LatLng(address.locationXd.toDouble(), address.locationYd.toDouble()), address.nm, address.id),
        points = emptyList(), userLat = null, userLon = null, distanceMeters = null
    )
}
