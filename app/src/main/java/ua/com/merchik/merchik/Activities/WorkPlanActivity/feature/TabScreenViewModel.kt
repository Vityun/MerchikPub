package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import jakarta.inject.Inject

class TabScreenViewModel @Inject constructor(): ViewModel() {

    private val _badgeCounts = mutableStateListOf<Int?>(null, null)
    val badgeCounts: List<Int?> get() = _badgeCounts

    fun updateBadge(index: Int, count: Int?) {
        if (index in _badgeCounts.indices) {
            _badgeCounts[index] = count
        }
    }

    fun clearBadge(index: Int) {
        updateBadge(index, null)
    }

}