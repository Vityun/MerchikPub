package ua.com.merchik.merchik.Activities.DetailedReportActivity

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
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
    val activity = context as ComponentActivity

    val viewModel: TovarDBViewModel = hiltViewModel()
    viewModel.dataJson = Gson().toJson(
        JSONObject()
            .put("codeDad2", wpData.code_dad2.toString())
            .put("clientId", wpData.client_id))
    viewModel.contextUI = ContextUI.TOVAR_FROM_TOVAR_TABS
    viewModel.modeUI = ModeUI.DEFAULT
    viewModel.typeWindow = "container"
    viewModel.subTitle = ""
    viewModel.context = context
    viewModel.subTitle = "тут будет текст"
    viewModel.updateContent()

    MerchikTheme {
        MainUI(
            context = context,
            modifier = Modifier,
            viewModel = viewModel
        )
    }
}