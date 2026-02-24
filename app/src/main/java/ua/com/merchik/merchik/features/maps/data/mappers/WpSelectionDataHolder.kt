package ua.com.merchik.merchik.features.maps.data.mappers


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ua.com.merchik.merchik.data.RealmModels.WpDataDB

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class WpSelectionDataHolder private constructor() {

    var selected: List<WpDataDB> by mutableStateOf(emptyList())
        private set

    var version: Long by mutableLongStateOf(0L)
        private set

    companion object {
        @Volatile private var instance: WpSelectionDataHolder? = null
        fun instance(): WpSelectionDataHolder =
            instance ?: synchronized(this) {
                instance ?: WpSelectionDataHolder().also { instance = it }
            }
    }

    fun init() {
        selected = emptyList()
        // version НЕ сбрасывай, иначе LaunchedEffect может не отработать как ожидаешь
    }

    fun set(items: List<WpDataDB>) {
        selected = items
        version += 1 // ✅ только тут
    }

    fun consumePendingSelected(): List<WpDataDB> {
        val res = selected
        selected = emptyList()
        return res
    }
}
