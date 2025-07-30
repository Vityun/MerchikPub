@file:JvmName("ComposeFunctions")

package ua.com.merchik.merchik.Activities.Features.ui

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ua.com.merchik.merchik.Activities.DetailedReportActivity.CommentViewModel
import ua.com.merchik.merchik.Activities.DetailedReportActivity.OpinionAndCommentView
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI


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

fun setContentWpData(
    context: Context,
    composeView: ComposeView,
) {
    composeView.setContent {
        val viewModel: WpDataDBViewModel = hiltViewModel() // ✅ получаем через Hilt
        viewModel.contextUI =
            ContextUI.WP_DATA_IN_CONTAINER

        viewModel.modeUI =
                ModeUI.DEFAULT

//        viewModel.title = "title"
        viewModel.typeWindow = "container"
        viewModel.subTitle = "subTitle"
        viewModel.context = LocalContext.current

        MerchikTheme {
            MainUI(
                context = context,
                modifier = Modifier,
                viewModel = viewModel)
        }
        viewModel.updateContent()
    }
}