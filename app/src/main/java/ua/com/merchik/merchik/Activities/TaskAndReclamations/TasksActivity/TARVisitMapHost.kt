package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.CancellationException
import ua.com.merchik.merchik.Activities.DetailedReportActivity.StoresMapFromMapsHost
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dialogs.features.indicator.LineSpinFadeLoaderIndicator

fun attachTARVisitMap(composeView: ComposeView, sourceDad2: Long) {
    composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    composeView.setContent {
        MerchikTheme {
            var visit by remember(sourceDad2) { mutableStateOf<WpDataDB?>(null) }
            var failed by remember(sourceDad2) { mutableStateOf(false) }
            var attempt by remember(sourceDad2) { mutableStateOf(0) }

            LaunchedEffect(sourceDad2, attempt) {
                failed = false
                try {
                    visit = TARTovarDataLoader.loadVisit(sourceDad2)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    failed = true
                    Globals.writeToMLOG("ERROR", "TARVisitMap/load", "sourceDad2=$sourceDad2, error=$error")
                }
            }

            val wpData = visit
            if (wpData != null) {
                StoresMapFromMapsHost(wpData)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (failed) {
                        TextButton(onClick = { attempt++ }, modifier = Modifier.padding(16.dp)) {
                            Text("Не вдалося завантажити карту. Повторити")
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
