package ua.com.merchik.merchik.features.main.componentsUI



import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.StateUI
@Composable
fun BottomSummaryRow(
    dataItemsUI: List<DataItemUI>,
    viewModel: MainViewModel
) {
    Row {
        Tooltip(
            text = viewModel.getTranslateString(
                stringResource(
                    id = R.string.total_number_selected,
                    dataItemsUI.size
                )
            )
        ) {
            Text(
                text = "\u2211 ${dataItemsUI.size}",
                fontSize = 16.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
            )
        }

        Tooltip(
            text = viewModel.getTranslateString(
                stringResource(
                    id = R.string.total_number_selected,
                    0
                )
            )
        ) {
            Text(
                text = "⚲ ${0}",
                fontSize = 16.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
            )
        }

        Tooltip(
            text = viewModel.getTranslateString(
                stringResource(
                    id = R.string.total_number_selected,
                    0
                )
            )
        ) {
            Text(
                text = "\u2207 ${0}",
                fontSize = 16.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (viewModel.modeUI == ModeUI.ONE_SELECT || viewModel.modeUI == ModeUI.MULTI_SELECT) {
            val selectedCount = dataItemsUI.count { it.selected }
            Tooltip(
                text = viewModel.getTranslateString(
                    stringResource(
                        id = R.string.total_number_marked,
                        selectedCount
                    )
                )
            ) {
                Text(
                    text = "\u2713 $selectedCount",
                    fontSize = 16.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
                )
            }
        }
    }
}
