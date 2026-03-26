package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import ua.com.merchik.merchik.Activities.CronchikViewModel
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.helpers.ScrollDataHolder
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs.OtherComposeTab
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs.WpDataContentTab
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.InitStateEntity
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.hasData
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.features.LoadingDialogWithPercent
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder
import ua.com.merchik.merchik.dialogs.features.dialogLoading.DialogDismissedListener
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.Main.GroupingField
import ua.com.merchik.merchik.features.main.componentsUI.CounterBadge
import ua.com.merchik.merchik.retrofit.GlobalErrors


@Composable
fun WpDataTabsScreen() {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    val cronchikViewModel =
        ViewModelProvider(activity).get<CronchikViewModel>(CronchikViewModel::class.java)

    val selectedColor = Color(ContextCompat.getColor(context, R.color.main_form))
    val tabBarBackground = Color(0xFFB1B1B1)
    val textSelectedColor = Color.DarkGray
    val textUnselectedColor = Color.Gray

    var dataIsReady by remember { mutableStateOf(isDataReadyCompat()) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var initialTabResolved by rememberSaveable { mutableStateOf(false) }

    val dossierSotrSDBList = remember {
        RoomManager.SQL_DB.dossierSotrDao().getData(null, 949L, null).orEmpty()
    }

    val hasAdditionalIncomeAccess = remember(dossierSotrSDBList) {
        dossierSotrSDBList.any { it.priznak == 1L }
    }

    var showAdditionalIncomeDeniedDialog by rememberSaveable { mutableStateOf(false) }

    val tabTitles = listOf(
        stringResource(R.string.title_0),
        "Доп.заробіток"
    )

    LaunchedEffect(dataIsReady, hasAdditionalIncomeAccess) {
        if (!dataIsReady || initialTabResolved) return@LaunchedEffect

        val hasFirstTabData = RealmManager.getAllWorkPlanWithOutRNO().isNotEmpty()
        val hasSecondTabData = RealmManager.getAllWorkPlanForRNO().isNotEmpty()

        selectedTabIndex = when {
            hasFirstTabData -> 0
            hasSecondTabData && hasAdditionalIncomeAccess -> 1
            else -> 0
        }

        initialTabResolved = true
    }

    LaunchedEffect(selectedTabIndex, hasAdditionalIncomeAccess) {
        if (selectedTabIndex == 1 && !hasAdditionalIncomeAccess) {
            showAdditionalIncomeDeniedDialog = true
        }
    }

    val selectTab: (Int) -> Unit = { index ->
        selectedTabIndex = index
    }

    val rememberRemoveListener = remember {
        var remove: (() -> Unit)? = null
        remove = ScrollDataHolder.instance().addOnIdsChangedListener { list ->
            cronchikViewModel.updateBadge(0, list.size)
        }
        remove
    }

    DisposableEffect(Unit) {
        onDispose { rememberRemoveListener?.invoke() }
    }

    val badgeCounts = cronchikViewModel.badgeCounts

    var isLoading by remember { mutableStateOf(false) }
    val progressModel = remember { ProgressViewModel(1) }

    LaunchedEffect(Unit) {
        while (!dataIsReady) {
            if (isDataReadyCompat()) {
                delay(800)
                dataIsReady = true
                progressModel.onCompleted()
                cronchikViewModel.updateBadgeAdditionalIncome(5000f)
                break
            }

            if (Globals.userId == 172906 || Globals.userId == 19653) {
                dataIsReady = true
                progressModel.onCompleted()
                break
            }

            delay(1000)
        }
    }

    LaunchedEffect(isLoading, dataIsReady) {
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

    val viewModel: WpDataDBViewModel = hiltViewModel()
    val green = colorResource(id = R.color.selected_item)
    val distance by viewModel.offsetDistanceMeters.collectAsState()

    if (badgeCounts[1] == null || badgeCounts[1] == 0)
        cronchikViewModel.updateBadgeAdditionalIncome(distance)

    val pendingScrollHash = remember { mutableStateOf<Long?>(null) }
    val isScrolling = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {

        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = tabBarBackground,
            indicator = {},
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectTab(index) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (selectedTabIndex == index) selectedColor else Color.Transparent),
                    text = {
                        Box(
                            contentAlignment = Alignment.TopEnd,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) textSelectedColor else textUnselectedColor,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clickable { selectTab(index) }
                            )

                            val count = badgeCounts.getOrNull(index)
                            if (count != null && count > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 14.dp, y = (-9).dp)
                                ) {
                                    CounterBadge(
                                        count = count,
                                        background = if (index == 0) green else Color.Red,
                                        borderAndTextColor = if (index == 0) Color.Black else Color.White,
                                        modifier = Modifier.clickable {
                                            Toast.makeText(
                                                context,
                                                "Фільтри до плану робіт застосовані. Відібрано ${count ?: 0} візитів",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            val targetHash =
                                                if (index == 0) ScrollDataHolder.instance().getAll() else null

                                            if (targetHash == null) {
                                                selectTab(index)
                                                return@clickable
                                            }

                                            if (selectedTabIndex == index) {
                                                if (!isScrolling.value) {
                                                    isScrolling.value = true
                                                    if (targetHash.isNotEmpty()) {
                                                        targetHash.forEach {
                                                            viewModel.highlightBId(it, green)
                                                        }
                                                        viewModel.selectOnlyItemsByStableIds(targetHash)
                                                        viewModel.updateContent()
                                                    }
                                                    isScrolling.value = false
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        LaunchedEffect(selectedTabIndex, pendingScrollHash.value) {
            val pending = pendingScrollHash.value ?: return@LaunchedEffect
            if (isScrolling.value) return@LaunchedEffect

            val maxAttempts = 8
            val delayMs = 150L

            repeat(maxAttempts) {
                try {
                    isScrolling.value = true

                    viewModel.requestScrollToVisit(pending)
                    viewModel.highlightBId(pending, green)

                    pendingScrollHash.value = null

                    delay(400)
                    isScrolling.value = false
                    return@LaunchedEffect
                } catch (_: Throwable) {
                    isScrolling.value = false
                    delay(delayMs)
                }
            }

            pendingScrollHash.value = null
            isScrolling.value = false
        }

        if (Globals.userId == 255247) {
            WpDataContentTab(dataIsReady = dataIsReady)
        } else {
            when (selectedTabIndex) {
                0 -> WpDataContentTab(dataIsReady = dataIsReady)
                1 -> OtherComposeTab(dataIsReady = dataIsReady)
            }
        }
    }

    if (showAdditionalIncomeDeniedDialog) {
        viewModel.setBlockMapsForAdditionalWork()
        val user = RoomManager.SQL_DB.usersDao().getUserById(Globals.userId)

        MessageDialog(
            title = "Додатковий заробіток",
            status = DialogStatus.NORMAL,
            subTitle = "Базовий мерчендайзинг",
            message = "Прием заявок от ${user.fio} запрещен. Обратитесь за помощью к <a href=\"app://click\">супервайзеру</a> или в <a href=\"app://click\">службу поддержки</a>",
            onDismiss = {
                showAdditionalIncomeDeniedDialog = false
                selectedTabIndex = 0
            },
            onTextLinkClick = {
                Globals.telephoneCall(context, "+380674491265")
            },
            okButtonName = "Ok",
            onConfirmAction = {
                showAdditionalIncomeDeniedDialog = false
                selectedTabIndex = 0
            },
            onDialogClosed = {
                showAdditionalIncomeDeniedDialog = false
                selectedTabIndex = 0

            }
        )
    }

    GlobalErrorMsg()
    RequestNotificationsPermissionPersistent()
}

@Composable
fun RequestNotificationsPermissionPersistent() {
    if (VERSION.SDK_INT >= 33) {
        val ctx = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            Log.d("FCM", "POST_NOTIFICATIONS granted=$granted")
        }

        // Проверяем каждый раз при рендере
        if (ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Всегда будет вызывать диалог, пока пользователь не даст "Разрешить"
            SideEffect {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun GlobalErrorMsg() {
    val ctx = LocalContext.current as Activity
    val appPackageName = ctx.packageName
    var isShow by remember { mutableStateOf(true) }
    val versionName = if (Build.VERSION.SDK_INT >= 33) {
        ctx.packageManager.getPackageInfo(
            ctx.packageName,
            PackageManager.PackageInfoFlags.of(0)
        ).versionName
    } else {
        @Suppress("DEPRECATION")
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
    }
    LaunchedEffect(isShow) {
        GlobalErrors.messages.collectLatest { msg ->
            if (isShow) {
                isShow = false
                MessageDialogBuilder(ctx)
                    .setTitle("Необхідне оновлення додатку")
                    .setSubTitle("Відповідь від сервера")
                    .setStatus(DialogStatus.ERROR)
                    .setMessage(msg)
                    .setOnCancelAction("Оновити") {
                        try {
                            // Сначала пробуем открыть Play Market
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=$appPackageName")
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ctx.startActivity(intent)
                        } catch (e: android.content.ActivityNotFoundException) {
                            // Если Play Market не установлен, открываем в браузере
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ctx.startActivity(intent)
                        }
                        isShow = true
                    }
                    .show()

            }
        }
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
            themeLoaded = true,
            customerLoaded = true
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
