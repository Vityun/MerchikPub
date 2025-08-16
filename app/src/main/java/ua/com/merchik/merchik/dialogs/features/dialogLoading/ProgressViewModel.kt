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

    private val stepPercentage = 1f / expectedEvents
    private var currentStepIndex = 0
    private var currentTargetProgress = 0f

    private var currentAnimationJob: Job? = null

    fun onNextEvent(message: String, durationMillis: Long = 2000L) {
        if (isCompleted) return

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
        if (isCompleted) return

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
        if (isCompleted) return
        currentMessage.value = "Виконано"
        viewModelScope.launch {
            progress.floatValue = 1f
            delay(1000)
            isCompleted = true
        }
    }

    fun onCanceled() {
        if (isCompleted) return
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
            isCompleted = true
        }
    }

    fun onCanceledNoAnim() {
        if (isCompleted) return
        currentMessage.value = "Вiдмiнено"
        viewModelScope.launch {
            isCompleted = true
        }
    }
}
