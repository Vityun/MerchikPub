package ua.com.merchik.merchik.Activities.DetailedReportActivity

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.database.realm.RealmManager

class CommentViewModel() : ViewModel() {

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment

    fun updateComment(newComment: String) {
        _comment.value = newComment
        if (newComment.length > 15) {
            _isSaved.value = true
//            _isSavedDialogShow.value = true
        }
    }

    // надо ли сохранять изменения
    private val _isSaved = MutableStateFlow(false)
    var isSaved: StateFlow<Boolean> = _isSaved

    fun setSave(isSave: Boolean) {
        _isSaved.value = isSave
    }

//    private val _isSavedDialogShow = MutableStateFlow(false)
//    var isSavedDialogShow: StateFlow<Boolean> = _isSavedDialogShow
//
//    fun setSavedDialogShow(isSaveDialog: Boolean) {
//        _isSavedDialogShow.value = isSaveDialog
//    }

//    var showDialog by mutableStateOf(false)

//    fun saveComment() {
//        // Ваша логика сохранения
//        // Например:
//        // Repository.saveComment(themeId, comment)
//        isSaved = true
//        showDialog = false
//
//    }
}