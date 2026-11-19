@file:JvmName("TarCommentsContent")

package ua.com.merchik.merchik.features.main.tar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.features.main.DBViewModels.TarCommentsViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI

fun setContentTarComments(view: ComposeView, viewModel: TarCommentsViewModel) {
    view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    view.setContent {
        MerchikTheme {
            val loading by viewModel.loading.collectAsState()
            val error by viewModel.error.collectAsState()
            Column {
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                error?.let { message ->
                    TextButton(onClick = viewModel::updateContent) { Text("$message. Повторити") }
                }
                MainUI(Modifier.weight(1f), viewModel, LocalContext.current)
            }
        }
    }
}

@Composable
fun TarAddCommentItem(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp), color = Color.White,
        contentColor = Color.DarkGray, shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_plus), contentDescription = null,
                modifier = Modifier.size(24.dp))
            Text(stringResource(R.string.tar_add_comment), Modifier.padding(start = 12.dp))
        }
    }
}
