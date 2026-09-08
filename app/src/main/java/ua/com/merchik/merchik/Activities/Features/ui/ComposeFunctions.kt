@file:JvmName("ComposeFunctions")

package ua.com.merchik.merchik.Activities.Features.ui

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import ua.com.merchik.merchik.features.main.DBViewModels.OptionsDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI
import ua.com.merchik.merchik.Activities.DetailedReportActivity.CommentViewModel
import ua.com.merchik.merchik.Activities.DetailedReportActivity.OpinionAndCommentView
import ua.com.merchik.merchik.Activities.DetailedReportActivity.TovarTabs
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.Activities.PhotoLogActivity.JournalPhotoScreen
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.WpDataTabsScreen
import ua.com.merchik.merchik.data.RealmModels.WpDataDB


fun setContentOpinion(
    composeView: ComposeView,
    wpDataDB: WpDataDB,
    viewModel: CommentViewModel
) {
    composeView.setContent {
        MerchikTheme {
            OpinionAndCommentView(wpDataDB = wpDataDB, viewModel = viewModel)
        }
    }
}


fun setContentTabsWpData(
    composeView: ComposeView
    ) {
    composeView.setContent {
        WpDataTabsScreen()

    }
}


fun setContentTovarData(
    composeView: ComposeView,
    wpDataDB: WpDataDB
) {
    composeView.setContent {
        TovarTabs(wpDataDB)

    }
}

fun setContentOptionsData(
    composeView: ComposeView,
    viewModel: OptionsDBViewModel
) {
    composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    composeView.setContent {
        MerchikTheme {
            MainUI(Modifier, viewModel, LocalContext.current)
        }
    }
}


fun setContentJournalPhoto(
    composeView: ComposeView
) {
    composeView.setContent {
        JournalPhotoScreen()
    }
}
