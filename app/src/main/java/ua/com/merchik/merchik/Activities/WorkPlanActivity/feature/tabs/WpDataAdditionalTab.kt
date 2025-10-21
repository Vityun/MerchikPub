package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.Activities.WorkPlanActivity.WPDataActivity
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.helpers.ScrollDataHolder
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI


@Composable
fun OtherComposeTab() {

    WPDataActivity.textLesson = 8930


    val viewModel: WpDataDBViewModel = hiltViewModel()
    val context = LocalContext.current
    viewModel.contextUI = ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER
    viewModel.modeUI = ModeUI.DEFAULT
    viewModel.typeWindow = "container"
    viewModel.subTitle = "Этот раздел предназначен для внештатных исполнителей. В нем отображаются работы которые может взять на исполнение любой пользователь. Для этого кликните по интересующему вас визиту и выберите из контекстного меню нужный вам"
    viewModel.context = context

    // Подписка на изменения ScrollDataHolder через DisposableEffect
    DisposableEffect(Unit) {
        val removeListener = ScrollDataHolder.instance().addOnIdsChangedListener { ids ->
            ids.forEach { id ->
                try {
                    val wpAddition = RoomManager.SQL_DB.wpDataAdditionalDao().getByIdSync(id)
                    wpAddition?.let {
                        val wpData = RealmManager.getWorkPlanRowByCodeDad2(it.codeDad2)
                        wpData?.let {wp ->
                            viewModel.requestFlyByStableId(wp.id)
                        }
                    }
                } catch (_: Throwable) { }
            }
        }

        onDispose {
            removeListener()
        }
    }

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
