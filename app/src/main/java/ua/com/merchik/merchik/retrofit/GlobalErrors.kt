package ua.com.merchik.merchik.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow


object GlobalErrors {
    private val _messages = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 64)
    val messages: SharedFlow<String> = _messages
    fun emit(msg: String) { _messages.tryEmit(msg) }
}

object GlobalErrorsLive {
    // LiveData, которое удобно наблюдать из Java
    val messages: LiveData<String> = GlobalErrors.messages.asLiveData(Dispatchers.Main)
}