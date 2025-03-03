package ua.com.merchik.merchik.Activities.DetailedReportActivity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CommentViewModel : DetailedReportViewModel() {

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment

    fun updateComment(newComment: String) {
        _comment.value = newComment
        if (newComment.length > 15) {
            _isSaved.value = true
        }
    }

    // надо ли сохранять изменения
    private val _isSaved = MutableStateFlow(false)
    var isSaved: StateFlow<Boolean> = _isSaved

    fun setSave(isSave: Boolean) {
        _isSaved.value = isSave
    }

}