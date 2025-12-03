package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs

import android.app.Activity
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.res.colorResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.Activities.CronchikViewModel
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.Activities.WorkPlanActivity.WPDataActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.InitStateEntity
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.hasData
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
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
    var dataIsReady by remember { mutableStateOf(isDataReadyCompat()) }

    // Ref to ProgressViewModel (для отмены позже)
    val progressModel = remember { ProgressViewModel(1) }

    WPDataActivity.textLesson = 8718

    val activity = context as ComponentActivity
    val cronchikViewModel =
        ViewModelProvider(activity).get<CronchikViewModel>(CronchikViewModel::class.java)

    // ✅ Проверка каждую 1–3 секунды
    LaunchedEffect(Unit) {
        while (!dataIsReady) {
            if (isDataReadyCompat()) {
                dataIsReady = true
                progressModel.onCompleted()
                cronchikViewModel.updateBadgeAdditionalIncome()
                break
            }
            if (Globals.userId == 172906) // исключение для Шевченко
                dataIsReady = true
            if (Globals.userId == 19653)
                dataIsReady = true
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
            progressModel.onNextEvent("Отримання даних вiд сервера", 23_500)
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

        val green = colorResource(id = R.color.ufmd_accept_t)

    }
}

/**
 * Совместимая проверка готовности:
 * 1) Если Room-флаги уже говорят "всё загружено" → true
 * 2) Иначе проверяем старую логику Realm.
 *    Если там всё ок → считаем готово и ДОзаполняем Room-флаги, чтобы потом
 *    уже всегда идти по новой схеме.
 */
fun isDataReadyCompat(): Boolean {
    // 1. Сначала пробуем новый путь (через Room-флаги)
    if (checkRealmReadyII()) return true

    // 2. Старый путь: все таблицы Realm существуют и не пустые
    if (checkRealmReady()) {
        // Миграция: проставим флаги в Room, чтобы в следующий раз
        // уже не опираться на Realm-состояние.
        val initDao = RoomManager.SQL_DB.initStateDao()
        val current = initDao.getState()

        val updated = (current ?: InitStateEntity(id = 1)).copy(
            wpLoaded = true,
            siteLoaded = true,
            optionsLoaded = true,
            themeLoaded = true
        )
        initDao.saveState(updated)

        return true
    }

    return false
}


fun checkRealmReady(): Boolean {
    val hasWp = RealmManager.INSTANCE.hasData<WpDataDB>()
    val hasStObj = RealmManager.INSTANCE.hasData<SiteObjectsDB>()
    val hasOption = RealmManager.INSTANCE.hasData<OptionsDB>()
    val hasThema = RealmManager.INSTANCE.hasData<ThemeDB>()

    return hasWp && hasStObj && hasOption && hasThema
}

fun checkRealmReadyII(): Boolean {
    val initDao = RoomManager.SQL_DB.initStateDao()
    val state = initDao.getState()

    return state?.wpLoaded == true && state.siteLoaded && state.optionsLoaded && state.themeLoaded
}
