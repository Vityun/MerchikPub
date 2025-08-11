package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI


@Composable
fun OtherComposeTab() {
    val viewModel: WpDataDBViewModel = hiltViewModel()
    val context = LocalContext.current
    viewModel.contextUI = ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER
    viewModel.modeUI = ModeUI.DEFAULT
    viewModel.typeWindow = "container"
    viewModel.subTitle = "subTitle"
    viewModel.context = context

    LaunchedEffect(Unit) {
        viewModel.updateContent()
    }

    MerchikTheme {
        MainUI(
            context = context,
            modifier = Modifier,
            viewModel = viewModel
        )
    }
}
