package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.hasData
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.dialogs.features.LoadingDialogWithPercent
import ua.com.merchik.merchik.dialogs.features.dialogLoading.DialogDismissedListener
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenuState



@Composable
fun WpDataContentTab() {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var dataIsReady by remember { mutableStateOf(checkRealmReady()) }

    // Ref to ProgressViewModel (для отмены позже)
    val progressModel = remember { ProgressViewModel(1) }

    // ✅ Проверка каждую 1–3 секунды
    LaunchedEffect(Unit) {
        while (!dataIsReady) {
            if (checkRealmReady()) {
                dataIsReady = true
                progressModel.onCompleted()
                break
            }
            delay(1000)
        }
    }

    // ✅ Показываем диалог, если данных нет и диалог ещё не показан
    LaunchedEffect(key1 = isLoading, key2 = dataIsReady) {
        if (!dataIsReady && !isLoading) {
            isLoading = true
            val dialog = LoadingDialogWithPercent(context as Activity, progressModel)

            dialog.setOnDismissListener(object : DialogDismissedListener {
                override fun onDialogDismissed() {
                    isLoading = false
                }
            })

            dialog.show()
            progressModel.onNextEvent("Отримання даних із сервера", 16_500)
        }
    }

    // ✅ Отображение UI
    if (dataIsReady) {
        val viewModel: WpDataDBViewModel = hiltViewModel()

        viewModel.contextUI = ContextUI.WP_DATA_IN_CONTAINER
        viewModel.modeUI = ModeUI.DEFAULT
        viewModel.typeWindow = "container"
        viewModel.subTitle = ""
        viewModel.context = context

        LaunchedEffect(Unit) {
            viewModel.updateContent()
        }

        MerchikTheme {
            MainUI(
                context = context,
                modifier = Modifier,
                viewModel = viewModel
            )
        }
    }
}



fun checkRealmReady(): Boolean {
    val hasWp = RealmManager.INSTANCE.hasData<WpDataDB>()
    val hasStObj = RealmManager.INSTANCE.hasData<SiteObjectsDB>()
    val hasOption = RealmManager.INSTANCE.hasData<OptionsDB>()
    val hasThema = RealmManager.INSTANCE.hasData<ThemeDB>()

    return hasWp && hasStObj && hasOption && hasThema
}
