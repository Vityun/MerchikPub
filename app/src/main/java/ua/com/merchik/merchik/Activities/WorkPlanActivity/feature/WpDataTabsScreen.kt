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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import ua.com.merchik.merchik.Activities.CronchikViewModel
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.helpers.ScrollDataHolder
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs.OtherComposeTab
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs.WpDataContentTab
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.BuildConfig
import ua.com.merchik.merchik.data.synchronization.StartupPolicy
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsLocalDefaults
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.ThemeDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.hasData
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder
import ua.com.merchik.merchik.dialogs.features.dialogLoading.LoadingDialog
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.componentsUI.CounterBadge
import ua.com.merchik.merchik.retrofit.GlobalErrors
import ua.com.merchik.merchik.retrofit.RetrofitBuilder
import ua.com.merchik.merchik.retrofit.CheckInternet.NetworkUtil


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
    val startedOffline = remember {
        activity.intent?.getBooleanExtra(StartupPolicy.EXTRA_OFFLINE_LOGIN, false) == true
    }
    fun isOfflineNow() = StartupPolicy.isOffline(
        startedOffline, NetworkUtil.isNetworkConnected(context),
        RetrofitBuilder.hasServerStatusUI(), RetrofitBuilder.getServerStatusUI()
    )
    var offline by remember { mutableStateOf(isOfflineNow()) }
    val canShowContent = dataIsReady
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var initialTabResolved by rememberSaveable { mutableStateOf(false) }

    val dossierFlow = remember {
        RoomManager.SQL_DB.dossierSotrDao().observeData(null, 949L, null)
    }

    val dossierSotrSDBList by dossierFlow.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    val hasAdditionalIncomeAccess = remember(dossierSotrSDBList) {
        dossierSotrSDBList.any { it.priznak == 1L }
    }

    var showAdditionalIncomeDeniedDialog by rememberSaveable { mutableStateOf(false) }

    val tabTitles = listOf(
        stringResource(R.string.title_0),
        "Доп.заробіток"
    )

    LaunchedEffect(canShowContent, hasAdditionalIncomeAccess) {
        if (!canShowContent || initialTabResolved) return@LaunchedEffect

        val hasFirstTabData = RealmManager.getAllWorkPlanWithOutRNO_LIST().isNotEmpty()
        val hasSecondTabData = RealmManager.getAllWorkPlanForRNO_LIST().isNotEmpty()

        selectedTabIndex = when {
            hasFirstTabData -> 0
            hasSecondTabData && hasAdditionalIncomeAccess -> 1
            else -> 0
        }

        initialTabResolved = true
    }

    LaunchedEffect(selectedTabIndex, hasAdditionalIncomeAccess, canShowContent) {
        if (selectedTabIndex == 1 && !hasAdditionalIncomeAccess && canShowContent) {
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

    val progressModel = remember { ProgressViewModel(1) }

    LaunchedEffect(Unit) {
        logStartupReadiness("start: offline=$offline")
        while (!dataIsReady) {
            val currentlyOffline = isOfflineNow()
            if (offline != currentlyOffline) {
                offline = currentlyOffline
                logStartupReadiness("offline=$offline")
            }
            if (isDataReadyCompat()) {
                dataIsReady = true
                logStartupReadiness("ready")
                cronchikViewModel.updateBadgeAdditionalIncome(5000f)
                break
            }

            // These accounts skip the plan/options exchange, but still need UI dictionaries.
            if ((Globals.getCurrentUserId() == 172906 || Globals.getCurrentUserId() == 19653)
                && SiteObjectsLocalDefaults.hasDownloadedObjects()
                && RoomManager.SQL_DB.themeDao().getCount() > 0
            ) {
                dataIsReady = true
                logStartupReadiness("ready_without_plan")
                break
            }

            delay(1000)
        }
    }

    val showLoading = StartupPolicy.shouldShowLoading(dataIsReady, offline)
    LaunchedEffect(showLoading) {
        if (showLoading) {
            progressModel.reset("Отримання даних вiд сервера")
            progressModel.onNextEvent("Отримання даних вiд сервера", 23_500)
        } else {
            progressModel.reset("")
        }
    }
    if (showLoading) {
        LoadingDialog(progressModel, canCancel = false, onDismiss = {})
    }
    // Do not create the plan ViewModel/MainUI while required dictionaries are missing.
    if (!canShowContent) {
        if (offline) {
            Text(
                text = "Початкове завантаження даних не завершено. Повторіть обмін, коли сервер буде доступний.",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = Color.DarkGray
            )
        }
        return
    }

    val viewModel: WpDataDBViewModel = hiltViewModel()
    val green = colorResource(id = R.color.selected_item)
    val distance by viewModel.offsetDistanceMeters.collectAsState()

    if (badgeCounts[1] == null || badgeCounts[1] == 0)
        cronchikViewModel.updateBadgeAdditionalIncome(distance)

    val pendingScrollHash = remember { mutableStateOf<Long?>(null) }
    val pendingFilterIds = remember { mutableStateOf<List<Long>?>(null) }
    val isScrolling = remember { mutableStateOf(false) }

    fun applyPlanBadgeFilter(targetIds: List<Long>) {
        if (targetIds.isEmpty() || isScrolling.value) return

        val currentItems = viewModel.uiState.value.items
        if (currentItems.isEmpty()) return

        val availableIds = currentItems.map { it.stableId }.toSet()
        val applicableIds = targetIds.filter { it in availableIds }
        val staleIds = targetIds.filterNot { it in availableIds }
        if (staleIds.isNotEmpty()) {
            ScrollDataHolder.instance().removeIds(staleIds)
        }
        if (applicableIds.isEmpty()) return

        isScrolling.value = true
        try {
            applicableIds.forEach {
                viewModel.highlightBId(it, green)
            }
            viewModel.selectOnlyItemsByStableIds(applicableIds)
            viewModel.updateContent()
        } finally {
            isScrolling.value = false
        }
    }

    val shouldApplyPlanFilterFromIntent =
        activity.intent?.getBooleanExtra("showWPDataWithFilters", false) == true

    LaunchedEffect(
        shouldApplyPlanFilterFromIntent,
        dataIsReady,
        badgeCounts.getOrNull(0) ?: 0
    ) {
        if (!shouldApplyPlanFilterFromIntent) return@LaunchedEffect

        val targetHash = ScrollDataHolder.instance().getAll()
        selectedTabIndex = 0
        initialTabResolved = true

        if (targetHash.isEmpty()) return@LaunchedEffect

        if (viewModel.uiState.value.items.isNotEmpty()) {
            applyPlanBadgeFilter(targetHash)
        } else {
            pendingFilterIds.value = targetHash
        }

        activity.intent?.removeExtra("showWPDataWithFilters")
    }

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
                                            if (index != 0) {
                                                selectTab(index)
                                                return@clickable
                                            }

                                            Toast.makeText(
                                                context,
                                                "Фільтри до плану робіт застосовані. Відібрано ${count ?: 0} візитів",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            val targetHash =
                                                if (index == 0) ScrollDataHolder.instance()
                                                    .getAll() else null

                                            if (targetHash == null) {
                                                selectTab(index)
                                                return@clickable
                                            }

                                            selectTab(index)

                                            if (selectedTabIndex == index && viewModel.uiState.value.items.isNotEmpty()) {
                                                applyPlanBadgeFilter(targetHash)
                                            } else {
                                                pendingFilterIds.value = targetHash
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

        LaunchedEffect(selectedTabIndex, pendingFilterIds.value, dataIsReady) {
            val pendingIds = pendingFilterIds.value ?: return@LaunchedEffect
            if (selectedTabIndex != 0) return@LaunchedEffect

            repeat(8) {
                if (viewModel.uiState.value.items.isNotEmpty()) {
                    applyPlanBadgeFilter(pendingIds)
                    pendingFilterIds.value = null
                    return@LaunchedEffect
                }
                delay(150)
            }

            applyPlanBadgeFilter(pendingIds)
            pendingFilterIds.value = null
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

        if (Globals.getCurrentUserId() == 255247) {
            WpDataContentTab(dataIsReady = canShowContent)
        } else {
            when (selectedTabIndex) {
                0 -> WpDataContentTab(dataIsReady = canShowContent)
                1 -> OtherComposeTab(dataIsReady = canShowContent && hasAdditionalIncomeAccess)
            }
        }
    }

    if (showAdditionalIncomeDeniedDialog) {
        viewModel.setBlockMapsForAdditionalWork()
        val user = RoomManager.SQL_DB.usersDao().getUserById(Globals.getCurrentUserId())

        user?.let {
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
 * Восстанавливаем отсутствующие флаги по данным из актуальных хранилищ.
 * Сетевые ошибки не сбрасывают ранее сохранённую готовность.
 */
fun isDataReadyCompat(): Boolean {
    val siteAvailable = SiteObjectsLocalDefaults.hasDownloadedObjects()
    val themeAvailable = RoomManager.SQL_DB.themeDao().getCount() > 0
    if (!siteAvailable || !themeAvailable) return false
    val state = RoomManager.SQL_DB.initStateDao().mergeLocalReadiness(
        wp = RealmManager.INSTANCE.hasData<WpDataDB>(),
        site = siteAvailable,
        options = RealmManager.INSTANCE.hasData<OptionsDB>(),
        theme = themeAvailable
    )
    return StartupPolicy.areRequiredTablesReady(
        state.wpLoaded, state.siteLoaded, state.optionsLoaded, state.themeLoaded,
        siteAvailable, themeAvailable
    )
}


fun checkRealmReady(): Boolean {
    val hasWp = RealmManager.INSTANCE.hasData<WpDataDB>()
    val hasStObj = SiteObjectsLocalDefaults.hasDownloadedObjects()
    val hasOption = RealmManager.INSTANCE.hasData<OptionsDB>()
    val hasThema = RoomManager.SQL_DB.themeDao().getCount() > 0

    return hasWp && hasStObj && hasOption && hasThema
}

fun checkRealmReadyII(): Boolean {
    val initDao = RoomManager.SQL_DB.initStateDao()
    val state = initDao.getState()

    return state != null && StartupPolicy.areRequiredTablesReady(
        state.wpLoaded, state.siteLoaded, state.optionsLoaded, state.themeLoaded,
        SiteObjectsLocalDefaults.hasDownloadedObjects(),
        RoomManager.SQL_DB.themeDao().getCount() > 0
    )
}

private fun logStartupReadiness(reason: String) {
    try {
        Globals.writeToMLOG(
            "INFO", "StartupReadiness",
            "$reason, version=${BuildConfig.VERSION_NAME}, user=${Globals.getCurrentUserId()}, " +
                    "flags=${RoomManager.SQL_DB.initStateDao().getState()}, " +
                    "wp=${RealmManager.INSTANCE.where(WpDataDB::class.java).count()}, " +
                    "site=${RealmManager.INSTANCE.where(SiteObjectsDB::class.java).count()}, " +
                    "siteDownloaded=${SiteObjectsLocalDefaults.hasDownloadedObjects()}, " +
                    "options=${RealmManager.INSTANCE.where(OptionsDB::class.java).count()}, " +
                    "themeRoom=${RoomManager.SQL_DB.themeDao().getCount()}, " +
                    "themeRealm=${RealmManager.INSTANCE.where(ThemeDB::class.java).count()}"
        )
    } catch (e: Exception) {
        Log.e("StartupReadiness", "Cannot read startup diagnostics", e)
    }
}
