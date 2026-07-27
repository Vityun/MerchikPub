package ua.com.merchik.merchik.Activities.DetailedReportActivity

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.ViewCompositionStrategy
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import java.util.concurrent.ConcurrentHashMap

data class PauseWorkUiState(
    val codeDad2: Long,
    val wpDataId: Long,
    val startedAtMillis: Long
)

object PauseWorkStateHolder {
    private val states = ConcurrentHashMap<Long, PauseWorkUiState>()

    @Volatile
    private var activeCodeDad2: Long? = null

    @JvmStatic
    fun start(codeDad2: Long, wpDataId: Long): PauseWorkUiState {
        val state = states[codeDad2] ?: PauseWorkUiState(
            codeDad2 = codeDad2,
            wpDataId = wpDataId,
            startedAtMillis = System.currentTimeMillis()
        )
        states[codeDad2] = state
        activeCodeDad2 = codeDad2
        return state
    }

    @JvmStatic
    fun stop(codeDad2: Long) {
        states.remove(codeDad2)
        if (activeCodeDad2 == codeDad2) {
            activeCodeDad2 = states.keys.firstOrNull()
        }
    }

    @JvmStatic
    operator fun get(codeDad2: Long): PauseWorkUiState? = states[codeDad2]

    @JvmStatic
    fun hasPauseFor(codeDad2: Long): Boolean = states.containsKey(codeDad2)

    @JvmStatic
    fun hasActivePause(): Boolean = states.isNotEmpty()

    @JvmStatic
    fun getActive(): PauseWorkUiState? {
        val active = activeCodeDad2?.let { states[it] }
        return active ?: states.values.firstOrNull()
    }
}

class PauseWorkComposeHost(
    private val composeView: ComposeView,
    private val onContinue: Runnable,
    private val onHome: Runnable
) {
    private var pendingState: PauseWorkUiState? = null
    private var renderedState: PauseWorkUiState? = null

    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            pendingState?.let { renderNow(it) }
        }

        override fun onViewDetachedFromWindow(v: View) {
            composeView.disposeComposition()
            renderedState = null
        }
    }

    init {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindow
        )
        composeView.addOnAttachStateChangeListener(attachListener)
    }

    fun show(state: PauseWorkUiState) {
        pendingState = state
        composeView.visibility = View.VISIBLE
        composeView.bringToFront()
        if (!composeView.isAttachedToWindow) {
            return
        }
        renderNow(state)
    }

    fun hide() {
        pendingState = null
        renderedState = null
        composeView.visibility = View.GONE
        composeView.disposeComposition()
    }

    private fun renderNow(state: PauseWorkUiState) {
        if (renderedState == state) return

        renderedState = state
        composeView.setContent {
            PauseWorkOverlay(
                state = state,
                onContinue = { onContinue.run() },
                onHome = { onHome.run() }
            )
        }
    }
}

@Composable
private fun PauseWorkOverlay(
    state: PauseWorkUiState,
    onContinue: () -> Unit,
    onHome: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.align(Alignment.End)) {
                ImageButton(
                    id = R.drawable.ic_home_pause_work,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                    onClick = onHome
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Работа на паузе",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2F3033),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                PauseWorkTimer(startedAtMillis = state.startedAtMillis)

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onContinue,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF41C85A),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = "ПРОДОЛЖИТЬ РАБОТУ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PauseWorkTimer(startedAtMillis: Long) {
    var now by remember(startedAtMillis) {
        mutableStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(startedAtMillis) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFEDEDED)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatPauseDuration(now - startedAtMillis),
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF34363A),
            textAlign = TextAlign.Center
        )
    }
}

private fun formatPauseDuration(durationMillis: Long): String {
    val secondsTotal = (durationMillis.coerceAtLeast(0L) / 1_000).toInt()
    val hours = secondsTotal / 3_600
    val minutes = (secondsTotal % 3_600) / 60
    val seconds = secondsTotal % 60

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
