package ua.com.merchik.merchik.Activities.PhotoLogActivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.Activities.WorkPlanActivity.WPDataActivity
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.features.main.DBViewModels.JournalPhotoSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.SMSPlanSDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI

@Composable
fun JournalPhotoScreen() {

    val viewModel: JournalPhotoSDBViewModel = hiltViewModel()
    val context = LocalContext.current
    viewModel.contextUI = ContextUI.JOURNAL_PHOTO_DEFAULT
    viewModel.modeUI = ModeUI.DEFAULT
    viewModel.typeWindow = "container"
    viewModel.subTitle = null
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
