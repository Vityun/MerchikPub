package ua.com.merchik.merchik.dialogs.features.dialogLoading

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun AnimatedLoadingBar(viewModel: ProgressViewModel) {

    val animatedProgress by animateFloatAsState(
        targetValue = viewModel.progress.floatValue,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing), label = ""
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoadingBarWithPercentage(progress = animatedProgress)


//        Text(
//            text = viewModel.currentMessage.value,
//            style = MaterialTheme.typography.labelLarge,
//            modifier = Modifier
//                .padding(16.dp, 0.dp, 16.dp, 16.dp)
//                .align(Alignment.Start),
//            color = Color(0xCC1E201D)
//        )
    }
}
