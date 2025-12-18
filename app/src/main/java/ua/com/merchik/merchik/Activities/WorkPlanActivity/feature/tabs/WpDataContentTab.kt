package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.Activities.WorkPlanActivity.WPDataActivity
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.WpDataTabsDialogViewModel
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.isDataReadyCompat
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI


const val PHONE_TAG = "phone_tag"


@Composable
fun WpDataContentTab(dataIsReady: Boolean) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    WPDataActivity.textLesson = 8718

    // ✅ локальный "бейдж"
    var localReady by remember(dataIsReady) { mutableStateOf(dataIsReady) }

    var dataIsReady by remember { mutableStateOf(isDataReadyCompat()) }

    if (localReady) {
        val viewModel: WpDataDBViewModel = hiltViewModel()
        val dialogViewModel: WpDataTabsDialogViewModel = viewModel()

//        var emptyScenario by rememberSaveable { mutableStateOf(false) }
        val user = remember {
            RoomManager.SQL_DB.usersDao().getUserById(Globals.userId)
        }
        val isEmptyScenario =
            user.clientId == "92106" &&
                    (user?.reportCount == 0)

        LaunchedEffect(dataIsReady) {
            if (isEmptyScenario) {
                dialogViewModel.startRegistrationIfNeeded(
                    activity = activity,
                    isEmptyScenario = localReady,
                    user = user
                )
            }
        }


        viewModel.contextUI = ContextUI.WP_DATA_IN_CONTAINER
        viewModel.modeUI = ModeUI.DEFAULT
        viewModel.typeWindow = "container"
        viewModel.subTitle = ""
        viewModel.context = context

        LaunchedEffect(dataIsReady) {
            delay(500)
            if (localReady) viewModel.updateContent()
        }



        MerchikTheme {
            MainUI(
                context = context,
                modifier = Modifier,
                viewModel = viewModel
            )
        }
    }
}
