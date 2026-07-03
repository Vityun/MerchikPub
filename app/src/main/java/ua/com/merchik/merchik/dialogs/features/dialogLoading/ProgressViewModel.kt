package ua.com.merchik.merchik.dialogs.features.dialogLoading

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ProgressViewModel(expectedEvents: Int) : ViewModel() {

    var progress = mutableFloatStateOf(0f)
        private set

    val currentMessage = mutableStateOf("")

    var isCompleted by mutableStateOf(false)
        private set

    var showCompletionAnimation by mutableStateOf(false)
        private set

    private val stepPercentage = if (expectedEvents <= 0) 1f else 1f / expectedEvents
    private var currentStepIndex = 0
    private var currentTargetProgress = 0f

    private var currentAnimationJob: Job? = null

    fun reset(message: String) {
        currentAnimationJob?.cancel()
        progress.floatValue = 0f
        currentMessage.value = message
        isCompleted = false
        showCompletionAnimation = false
        currentStepIndex = 0
        currentTargetProgress = 0f
    }

    fun setMessage(message: String?) {
        if (!message.isNullOrBlank()) {
            currentMessage.value = message
        }
    }

    @JvmOverloads
    fun setProgressPercent(progressPercent: Float, message: String? = null, durationMillis: Long = 300L) {
        if (showCompletionAnimation) {
            return
        }
        if (isCompleted) {
            isCompleted = false
        }
        setMessage(message)
        val targetProgress = (progressPercent / 100f).coerceIn(0f, 1f)
        currentAnimationJob?.cancel()
        currentAnimationJob = viewModelScope.launch {
            animateProgress(from = progress.floatValue, to = targetProgress, durationMillis)
        }
    }

    @JvmOverloads
    fun completeAndHide(durationMillis: Long = 300L, progressHoldMillis: Long = 500L) {
        if (showCompletionAnimation || isCompleted) {
            return
        }
        currentAnimationJob?.cancel()
        currentAnimationJob = viewModelScope.launch {
            if (progress.floatValue < 1f) {
                animateProgress(from = progress.floatValue, to = 1f, durationMillis)
            } else {
                progress.floatValue = 1f
            }
            delay(progressHoldMillis)
            showCompletionAnimation = true
        }
    }

    fun hideNow() {
        currentAnimationJob?.cancel()
        showCompletionAnimation = false
        viewModelScope.launch {
            isCompleted = true
        }
    }

    fun onNextEvent(message: String, durationMillis: Long = 2000L) {
        if (isCompleted || showCompletionAnimation) return

        // Вычисляем цель для следующего шага
        val nextTargetProgress = ((currentStepIndex + 1) * stepPercentage).coerceAtMost(0.99f)

        // Обновляем сообщение
        currentMessage.value = message

        // Останавливаем текущую анимацию, если она идёт
        currentAnimationJob?.cancel()

        // Запускаем новую анимацию
        currentAnimationJob = viewModelScope.launch {
            animateProgress(from = progress.floatValue, to = nextTargetProgress, durationMillis)
        }

        // Обновляем индекс и текущую цель
        currentStepIndex++
        currentTargetProgress = nextTargetProgress

    }

    fun onNextEvent(message: String) {
        if (isCompleted || showCompletionAnimation) return

        // Вычисляем цель для следующего шага
        val nextTargetProgress = ((currentStepIndex + 1) * stepPercentage).coerceAtMost(0.99f)

        // Обновляем сообщение
        currentMessage.value = message

        // Останавливаем текущую анимацию, если она идёт
        currentAnimationJob?.cancel()

        // Запускаем новую анимацию
        currentAnimationJob = viewModelScope.launch {
            animateProgress(from = progress.floatValue, to = nextTargetProgress, 2000)
        }

        // Обновляем индекс и текущую цель
        currentStepIndex++
        currentTargetProgress = nextTargetProgress

    }

    private suspend fun animateProgress(from: Float, to: Float, durationMillis: Long) {
        if (durationMillis <= 0L) {
            progress.floatValue = to
            return
        }

        val startTime = System.currentTimeMillis()
        val endTime = startTime + durationMillis

        while (System.currentTimeMillis() < endTime) {
            val elapsedTime = System.currentTimeMillis() - startTime
            val fraction = (elapsedTime / durationMillis.toFloat()).coerceAtMost(1f)

            // Линейная интерполяция прогресса
            progress.floatValue = from + (to - from) * fraction

            delay(16L) // 60 FPS
        }

        // Устанавливаем конечное значение
        progress.floatValue = to
    }

    fun onCompleted() {
        if (isCompleted || showCompletionAnimation) return
        currentMessage.value = "Виконано"
        completeAndHide()
    }

    fun onCanceled() {
        if (isCompleted) return
        showCompletionAnimation = false
        currentMessage.value = "Вiдмiнено"
        viewModelScope.launch {
            delay(500)
            isCompleted = true
        }
    }

    fun onCompletedNoAnim() {
        if (isCompleted) return
        currentMessage.value = "Виконано"
        viewModelScope.launch {
            progress.floatValue = 1f
            showCompletionAnimation = false
            isCompleted = true
        }
    }

    fun onCanceledNoAnim() {
        if (isCompleted) return
        showCompletionAnimation = false
        currentMessage.value = "Вiдмiнено"
        viewModelScope.launch {
            isCompleted = true
        }
    }

    fun onCompletionAnimationFinished() {
        if (isCompleted) return
        showCompletionAnimation = false
        isCompleted = true
    }
}
