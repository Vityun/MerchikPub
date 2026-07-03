package ua.com.merchik.merchik.dialogs.features.dialogLoading

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.R


@Composable
fun AnimatedLoadingBar(viewModel: ProgressViewModel) {

    val animatedProgress by animateFloatAsState(
        targetValue = viewModel.progress.floatValue,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing), label = ""
    )
    val showCompletionAnimation = viewModel.showCompletionAnimation
    val completionComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.completed)
    )
    val completionProgress by animateLottieCompositionAsState(
        composition = completionComposition,
        isPlaying = showCompletionAnimation,
        iterations = 1
    )

    LaunchedEffect(showCompletionAnimation, completionProgress) {
        if (showCompletionAnimation && completionProgress >= 0.99f) {
            viewModel.onCompletionAnimationFinished()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoadingBarWithPercentage(
            progress = animatedProgress,
            completionContent = if (showCompletionAnimation) {
                {
                    LottieAnimation(
                        modifier = Modifier.size(32.dp),
                        composition = completionComposition,
                        progress = { completionProgress }
                    )
                }
            } else {
                null
            }
        )


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
