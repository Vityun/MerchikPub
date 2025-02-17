@file:JvmName("ComposeFunctions")

package ua.com.merchik.merchik.Activities.Features.ui

import androidx.compose.ui.platform.ComposeView
import ua.com.merchik.merchik.Activities.DetailedReportActivity.CommentViewModel
import ua.com.merchik.merchik.Activities.DetailedReportActivity.OpinionAndCommentView
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.data.RealmModels.WpDataDB


fun setContent(
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