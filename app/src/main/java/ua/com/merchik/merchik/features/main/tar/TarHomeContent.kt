@file:JvmName("TarHomeContent")

package ua.com.merchik.merchik.features.main.tar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.features.main.DBViewModels.TarHomeViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI
import ua.com.merchik.merchik.features.main.componentsUI.SectionToolbarTitle

fun setContentTarHome(view: ComposeView, viewModel: TarHomeViewModel) {
    view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    view.setContent {
        MerchikTheme {
            val loading by viewModel.loading.collectAsState()
            val error by viewModel.error.collectAsState()
            val heading by viewModel.heading.collectAsState()
            Column(Modifier.fillMaxSize()) {
                SectionToolbarTitle(heading.title, heading.activeCount.toLong(),
                    Modifier.background(Color(0xFFB1B1B1)))
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                error?.let { message ->
                    TextButton(onClick = viewModel::updateContent) { Text("$message. Повторити") }
                }
                MainUI(Modifier.weight(1f), viewModel, LocalContext.current)
            }
        }
    }
}
