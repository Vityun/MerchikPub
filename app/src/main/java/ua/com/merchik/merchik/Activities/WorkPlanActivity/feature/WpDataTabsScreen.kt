package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.util.Log
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

    // VM для диалогов
//    val dialogViewModel: WpDataTabsDialogViewModel = viewModel()

    // ✅ общий флаг готовности
    var dataIsReady by remember { mutableStateOf(isDataReadyCompat()) }

    // ✅ выбранный таб
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    // ✅ чтобы “авто-выбор при старте” сработал ровно один раз
    var initialTabResolved by rememberSaveable { mutableStateOf(false) }


    LaunchedEffect(dataIsReady) {
        if (!dataIsReady || initialTabResolved) return@LaunchedEffect

        val hasFirstTabData =
            RealmManager.getAllWorkPlanWithOutRNO().isNotEmpty()

        val hasSecondTabData =
            RealmManager.getAllWorkPlanForRNO().isNotEmpty()

        selectedTabIndex = when {
            hasFirstTabData -> 0
            hasSecondTabData -> 1
            else -> 0
        }

        initialTabResolved = true
    }

    val dossierSotrSDBList =
        RoomManager.SQL_DB.dossierSotrDao().getData(null, 982L, null)
    val hasPriznak1 =
        dossierSotrSDBList?.any { it.priznak == 1L } == false

    val tabTitles =
//        if (Globals.userId == 176053 || Globals.userId == 255212 || Globals.userId == 241562
//            || Globals.userId == 130647 || Globals.userId == 249929)
        if (hasPriznak1)
        {
            listOf(
                stringResource(R.string.title_0),
                "Доп.заробіток",
            )
        } else {
            listOf(
                stringResource(R.string.title_0)
            )
        }

    // Подпишемся на изменения ids (минимальные правки, без StateFlow)
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


    // Кол-во уведомлений на вкладках. null или 0 — не отображаем.
//    val badgeCounts = remember { cronchikViewModel.badgeCounts }
    val badgeCounts = cronchikViewModel.badgeCounts

    if (badgeCounts[1] == null || badgeCounts[1] == 0)
        cronchikViewModel.updateBadgeAdditionalIncome()


    // -----------------------------
    // ✅ ОБЩАЯ ЛОГИКА ГОТОВНОСТИ ДАННЫХ
    // -----------------------------
    var isLoading by remember { mutableStateOf(false) }
    val progressModel = remember { ProgressViewModel(1) }

    // ✅ Проверка каждую ~1 сек
    LaunchedEffect(Unit) {
        while (!dataIsReady) {
            if (isDataReadyCompat()) {
                delay(800)
                dataIsReady = true
                progressModel.onCompleted()
                cronchikViewModel.updateBadgeAdditionalIncome()
                break
            }

            // исключения
            if (Globals.userId == 172906 || Globals.userId == 19653) {
                dataIsReady = true
                progressModel.onCompleted()
                break
            }

            delay(1000)
        }
    }

    // ✅ Показ общего лоадинг-диалога
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

    // -----------------------------
    // ✅ BADGE + SCROLL LOGIC (как было)
    // -----------------------------
    Log.e("WpDataTabsScreen", "ScrollDataHolder.instance().getIds() -")
    val ids = ScrollDataHolder.instance().getIds()
    Log.e("WpDataTabsScreen", "ScrollDataHolder.instance().getIds() +")

    val viewModel: WpDataDBViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val green = colorResource(id = R.color.selected_item)

    val badgeTargets: List<Long?> = remember(ids) {
        List(tabTitles.size) { idx -> ids.getOrNull(idx) }
    }

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
                    onClick = { selectedTabIndex = index },
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
                                    .clickable { selectedTabIndex = index }
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
//                                            val targetHash = badgeTargets.getOrNull(index)
                                            val targetHash = if (index == 0) ScrollDataHolder.instance().getNext() else null

                                            if (targetHash == null) {
                                                selectedTabIndex = index
                                                return@clickable
                                            }

                                            if (selectedTabIndex == index) {
                                                if (!isScrolling.value) {
                                                    isScrolling.value = true
                                                    if (targetHash > 0) {
                                                        viewModel.requestScrollToVisit(targetHash)
                                                        viewModel.highlightBId(targetHash, green)

                                                        val items = uiState.items
                                                        val indexInData =
                                                            items.indexOfFirst { it.stableId == targetHash }
                                                        if (indexInData > 0) {

                                                            val item = items[indexInData]
                                                            // достаём WpData из rawObj (если он там есть)
                                                            val wpData: WpDataDB? = item.rawObj
                                                                .firstNotNullOfOrNull { (it as? WpDataDB) }

                                                            wpData?.let {
                                                                val currentFilter = uiState.filters
                                                                if (currentFilter != null) {
                                                                    viewModel.updateFilters(
                                                                        currentFilter.copy(
                                                                            searchText = it.addr_txt
                                                                        )
                                                                    )
                                                                    val groupingFieldAdr =
                                                                        GroupingField(
                                                                            key = "addr_txt",
                                                                            title = viewModel.getTranslateString(
                                                                                "Адреса",
                                                                                1101
                                                                            ),
                                                                            priority = 1,
                                                                            collapsedByDefault = false
                                                                        )

                                                                    val groupingFieldDate =
                                                                        GroupingField(
                                                                            key = "dt",
                                                                            title = viewModel.getTranslateString(
                                                                                "Дата",
                                                                                1100
                                                                            ),
                                                                            priority = 1,
                                                                            collapsedByDefault = false
                                                                        )

                                                                    viewModel.updateGrouping(
                                                                        groupingFieldAdr,
                                                                        0
                                                                    )
                                                                    viewModel.updateGrouping(
                                                                        groupingFieldDate,
                                                                        1
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                    isScrolling.value = false
                                                }
                                            } else {
                                                pendingScrollHash.value = targetHash
                                                selectedTabIndex = index
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

        // При смене выбранного таба, если есть pendingScrollHash — пытаемся проскроллить
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

        // Контент выбранной вкладки
//        if (Globals.userId == 176053 || Globals.userId == 2)

        if (Globals.userId == 255247)
            WpDataContentTab(dataIsReady = dataIsReady)
        else

            when (selectedTabIndex) {
                0 -> WpDataContentTab(dataIsReady = dataIsReady)
                1 -> OtherComposeTab(dataIsReady = dataIsReady)
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
