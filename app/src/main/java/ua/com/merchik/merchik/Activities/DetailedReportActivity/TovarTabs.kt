package ua.com.merchik.merchik.Activities.DetailedReportActivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.gson.Gson
import org.json.JSONObject
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.features.main.DBViewModels.TovarDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI


@Composable
fun TovarTabs(wpData: WpDataDB) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: TovarDBViewModel = hiltViewModel()
    val codeDad2 = wpData.code_dad2
    val clientId = wpData.client_id

    viewModel.dataJson = remember(codeDad2, clientId) {
        Gson().toJson(
            JSONObject()
                .put("codeDad2", codeDad2.toString())
                .put("clientId", clientId)
        )
    }
    viewModel.contextUI = ContextUI.TOVAR_FROM_TOVAR_TABS
    viewModel.modeUI = ModeUI.FILTER_SELECT
    viewModel.typeWindow = "container"
    viewModel.context = context

    DisposableEffect(viewModel, lifecycleOwner, codeDad2, clientId) {
        viewModel.prepareVisitContent()

        // ViewPager2 resumes only the selected fragment, not its preloaded neighbours.
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshReportPrepareOnTabResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    MerchikTheme {
        MainUI(
            context = context,
            modifier = Modifier,
            viewModel = viewModel
        )
    }
}
