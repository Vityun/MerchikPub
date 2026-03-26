package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import ua.com.merchik.merchik.Activities.CronchikViewModel
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.Activities.WorkPlanActivity.WPDataActivity
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.helpers.ScrollDataHolder
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.isDataReadyCompat
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogCloseReason
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI


@Composable
fun OtherComposeTab(dataIsReady: Boolean) {
    WPDataActivity.textLesson = 8930

    val viewModel: WpDataDBViewModel = hiltViewModel()
    val context = LocalContext.current
    val activity = context as ComponentActivity

    viewModel.contextUI = ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER
    viewModel.modeUI = ModeUI.MULTI_SELECT
    viewModel.typeWindow = "container"
    viewModel.subTitle = viewModel.getTranslateString(
        "Этот раздел предназначен для внештатных исполнителей. В нем отображаются работы которые может взять на исполнение любой пользователь. Для этого кликните по интересующему вас визиту и выберите из контекстного меню нужный вам",
        9070
    )
    viewModel.context = context

    // ✅ локальный "бейдж"
    var localReady by remember(dataIsReady) { mutableStateOf(dataIsReady) }
    var dataIsReady by remember { mutableStateOf(isDataReadyCompat()) }

    // Подписка на изменения ScrollDataHolder
    DisposableEffect(Unit) {
        val removeListener = ScrollDataHolder.instance().addOnIdsChangedListener { ids ->
            ids.forEach { id ->
                try {
                    val wpAddition = RoomManager.SQL_DB.wpDataAdditionalDao().getByIdSync(id)
                    wpAddition?.let {
                        val wpData = RealmManager.getWorkPlanRowByCodeDad2(it.codeDad2)
                        wpData?.let { wp ->
                            viewModel.requestFlyByStableId(wp.id)
                        }
                    }
                } catch (_: Throwable) {
                }
            }
        }
        onDispose { removeListener() }
    }

    LaunchedEffect(dataIsReady) {
        if (localReady) viewModel.updateContent()
    }

    if (localReady) {
        MerchikTheme {
            MainUI(
                context = context,
                modifier = Modifier,
                viewModel = viewModel
            )
        }
    }

//    val dossierSotrSDB = RoomManager.SQL_DB.dossierSotrDao().getData(null, 949L, null)
//    val user = RoomManager.SQL_DB.usersDao().getUserById(Globals.userId)
//    var di by remember { mutableStateOf(true) }
//    if (di)
//        MessageDialog(
//            title = "Додатковий заробіток",
//            status = DialogStatus.NORMAL,
//            subTitle = "Базовий мерчендайзинг",
//            message = "Прием заявок от ${user.fio} запрещен. Обратитесь за помощью к супервайзеру или в службу поддержки по телефону +380674491265",     // ← тело подсказки по сценарию
//            onDismiss = { di = false },
//            onConfirmAction = {
//                di = false
//            },
//            onDialogClosed = { reason ->
//                Toast.makeText(context, "dfvdgfb", Toast.LENGTH_LONG).show()
//                when (reason) {
//                    DialogCloseReason.DISMISS -> {
//                        Log.d("MessageDialog", "Закрыт по back/outside")
//                    }
//
//                    DialogCloseReason.CLOSE_BUTTON -> {
//                        Log.d("MessageDialog", "Закрыт по крестику")
//                    }
//
//                    DialogCloseReason.CONFIRM -> {
//                        Log.d("MessageDialog", "Закрыт после подтверждения")
//                    }
//
//                    DialogCloseReason.CANCEL -> {
//                        Log.d("MessageDialog", "Закрыт после отмены")
//                    }
//                }
//            }
//        )


}

