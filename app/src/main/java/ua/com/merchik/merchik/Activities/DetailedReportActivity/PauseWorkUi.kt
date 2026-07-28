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
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.Utils.TrustedTime
import ua.com.merchik.merchik.data.Database.Room.WPDataPauseSDB
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.DaoInterfaces.WPDataPauseDao
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import java.util.concurrent.ConcurrentHashMap

data class PauseWorkUiState(
    val codeDad2: Long,
    val wpDataId: Long,
    val startedAtMillis: Long
)

object PauseWorkStateHolder {
    private const val TAG = "PauseWorkStateHolder"

    private val states = ConcurrentHashMap<Long, PauseWorkUiState>()

    @Volatile
    private var activeCodeDad2: Long? = null

    @JvmStatic
    fun start(codeDad2: Long, wpDataId: Long): PauseWorkUiState {
        if (codeDad2 <= 0L) {
            Globals.writeToMLOG(
                "ERROR",
                "$TAG/start",
                "Invalid codeDad2=$codeDad2, wpDataId=$wpDataId"
            )
            return PauseWorkUiState(
                codeDad2 = codeDad2,
                wpDataId = wpDataId,
                startedAtMillis = System.currentTimeMillis()
            )
        }

        val activeRow = runCatching {
            dao()?.getActiveByCodeDad2Sync(codeDad2)
        }.onFailure {
            logError("start.getActiveByCodeDad2Sync", it)
        }.getOrNull()

        val state = if (activeRow != null) {
            activeRow.toUiState(wpDataId)
        } else {
            val nowSeconds = nextAvailableStartSeconds(codeDad2, currentTimeSeconds())
            val row = WPDataPauseSDB().apply {
                this.codeDad2 = codeDad2
                this.dtStart = nowSeconds
                this.dtEnd = 0L
                this.dtUpdateClient = nowSeconds
                this.uploadStatus = 1
            }

            runCatching {
                dao()?.insertSync(row)
            }.onFailure {
                logError("start.insertSync", it)
            }

            PauseWorkUiState(
                codeDad2 = codeDad2,
                wpDataId = wpDataId,
                startedAtMillis = row.dtStart * 1_000L
            )
        }

        states[codeDad2] = state
        activeCodeDad2 = codeDad2
        return state
    }

    @JvmStatic
    fun stop(codeDad2: Long) {
        if (codeDad2 <= 0L) return

        val nowSeconds = currentTimeSeconds()
        runCatching {
            dao()?.finishActivePauseSync(
                codeDad2,
                nowSeconds,
                nowSeconds
            )
        }.onFailure {
            logError("stop.finishActivePauseSync", it)
        }

        states.remove(codeDad2)
        if (activeCodeDad2 == codeDad2) {
            activeCodeDad2 = states.keys.firstOrNull()
        }
    }

    @JvmStatic
    operator fun get(codeDad2: Long): PauseWorkUiState? {
        val pauseDao = dao()
        if (pauseDao == null) {
            return states[codeDad2]
        }

        val state = try {
            pauseDao.getActiveByCodeDad2Sync(codeDad2)?.toUiState()
        } catch (error: Throwable) {
            logError("get.getActiveByCodeDad2Sync", error)
            return states[codeDad2]
        }

        if (state != null) {
            states[codeDad2] = state
            activeCodeDad2 = codeDad2
        } else {
            states.remove(codeDad2)
        }

        return state
    }

    @JvmStatic
    fun hasPauseFor(codeDad2: Long): Boolean {
        val pauseDao = dao() ?: return states.containsKey(codeDad2)

        return runCatching {
            pauseDao.getActiveByCodeDad2Sync(codeDad2) != null
        }.onFailure {
            logError("hasPauseFor.getActiveByCodeDad2Sync", it)
        }.getOrDefault(states.containsKey(codeDad2))
    }

    @JvmStatic
    fun hasActivePause(): Boolean = activePauseCount() > 0

    @JvmStatic
    fun activePauseCount(): Int {
        return runCatching {
            dao()?.getActivePauseVisitCountSync() ?: states.keys.size
        }.onFailure {
            logError("activePauseCount.getActivePauseVisitCountSync", it)
        }.getOrDefault(states.keys.size)
    }

    @JvmStatic
    fun getActivePausedCodeDad2List(): List<Long> {
        return runCatching {
            dao()?.getActivePauseCodeDad2ListSync()
        }.onFailure {
            logError("getActivePausedCodeDad2List", it)
        }.getOrNull()
            ?.filter { it > 0L }
            ?.distinct()
            ?: states.keys.filter { it > 0L }.toList()
    }

    @JvmStatic
    fun getActive(): PauseWorkUiState? {
        val pauseDao = dao()
        if (pauseDao != null) {
            val row = runCatching {
                pauseDao.getActivePausesSync().firstOrNull()
            }.onFailure {
                logError("getActive.getActivePausesSync", it)
            }.getOrElse {
                null
            }

            val state = row?.toUiState()
            if (state != null) {
                states[state.codeDad2] = state
                activeCodeDad2 = state.codeDad2
                return state
            }

            states.clear()
            activeCodeDad2 = null
            return null
        }

        val active = activeCodeDad2?.let { states[it] }
        if (active != null) return active
        return states.values.firstOrNull()
    }

    private fun WPDataPauseSDB.toUiState(fallbackWpDataId: Long = 0L): PauseWorkUiState {
        return PauseWorkUiState(
            codeDad2 = codeDad2,
            wpDataId = fallbackWpDataId.takeIf { it > 0L } ?: resolveWpDataId(codeDad2),
            startedAtMillis = dtStart * 1_000L
        )
    }

    private fun resolveWpDataId(codeDad2: Long): Long {
        return runCatching {
            RealmManager.getWorkPlanRowByCodeDad2(codeDad2)?.id ?: 0L
        }.onFailure {
            logError("resolveWpDataId", it)
        }.getOrDefault(0L)
    }

    private fun dao(): WPDataPauseDao? {
        return runCatching {
            RoomManager.SQL_DB?.wpDataPauseDao()
        }.onFailure {
            logError("dao", it)
        }.getOrNull()
    }

    private fun currentTimeSeconds(): Long = TrustedTime.nowServerSecOrLocalSec()

    private fun nextAvailableStartSeconds(codeDad2: Long, preferredSeconds: Long): Long {
        val pauseDao = dao() ?: return preferredSeconds
        var candidate = preferredSeconds

        repeat(5) {
            val existing = runCatching {
                pauseDao.getByIdSync(codeDad2, candidate)
            }.getOrNull()
            if (existing == null) {
                return candidate
            }
            candidate++
        }

        return candidate
    }

    private fun logError(place: String, throwable: Throwable) {
        Globals.writeToMLOG(
            "ERROR",
            "$TAG/$place",
            "Exception: $throwable"
        )
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
        mutableStateOf(currentTimerMillis())
    }

    LaunchedEffect(startedAtMillis) {
        while (true) {
            now = currentTimerMillis()
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

private fun currentTimerMillis(): Long {
    return TrustedTime.nowServerSecOrNull()?.let { it * 1_000L }
        ?: System.currentTimeMillis()
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
