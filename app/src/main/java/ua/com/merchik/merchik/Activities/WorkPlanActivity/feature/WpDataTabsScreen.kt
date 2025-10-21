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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import ua.com.merchik.merchik.Activities.CronchikViewModel
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.helpers.ScrollDataHolder
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs.AdditionalContentTab
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs.OtherComposeTab
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs.WpDataContentTab
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
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

    val tabTitles = if (RealmManager.getAllWorkPlanForRNO().isNullOrEmpty()) listOf(
        stringResource(R.string.title_0)
    ) else listOf(
        stringResource(R.string.title_0),
        "Доп.Заработок",
//        "Заявки"
    )
    // Подпишемся на изменения ids (минимальные правки, без StateFlow)
    val rememberRemoveListener = remember {
        // создаём listener один раз и вернём функцию удаления
        var remove: (() -> Unit)? = null
        remove = ScrollDataHolder.instance().addOnIdsChangedListener { list ->
            // вызываем обновление бейджа в ViewModel
            cronchikViewModel.updateBadge(0, list.size)
        }
        remove
    }

    DisposableEffect(Unit) {
        onDispose {
            rememberRemoveListener.invoke()
        }
    }
//    cronchikViewModel.updateBadge(0, ScrollDataHolder.instance().getIds().size)
    cronchikViewModel.updateBadgeAdditionalIncome()
    // Кол-во уведомлений на вкладках. null или 0 — не отображаем.
    val badgeCounts = remember { cronchikViewModel.badgeCounts }

    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }

    val ids = ScrollDataHolder.instance().getIds()
//    val idNext = ScrollDataHolder.instance().getNext()
//    Log.e("!!!!!!","list: $ids, idNext: $idNext")
//    val ids = mutableListOf(4060380514L,4060380514L)
    val viewModel: WpDataDBViewModel = hiltViewModel()
    val green = colorResource(id = R.color.ufmd_accept_t)

    // Альтернативно: если getIds возвращает List<String> или иной формат — преобразуйте
    val badgeTargets: List<Long?> = remember(ids) {
        // пытаемся взять id для каждого таба, иначе null
        List(maxOf(tabTitles.size, ids.size)) { idx -> ids.getOrNull(idx) }
            .take(tabTitles.size)
    }
    // pendingScrollHash: хэш, который надо проскроллить после переключения таба
    val pendingScrollHash = remember { mutableStateOf<Long?>(null) }
    val isScrolling = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = tabBarBackground,
            indicator = {}, // отключаем дефолтный индикатор
            divider = {}    // <-- отключаем линию
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (selectedTabIndex == index) selectedColor else Color.Transparent
                        ),
                    text = {
                        Box(
                            contentAlignment = Alignment.TopEnd,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            // Сам заголовок таба — кликабелен для переключения
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) textSelectedColor else textUnselectedColor,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clickable {
                                        selectedTabIndex = index
                                    }
                            )

                            // бейдж (если есть)
                            val count = badgeCounts.getOrNull(index)
                            if (count != null && count > 0) {
                                // оборачиваем CounterBadge в Box, чтобы ловить клик только по бейджу
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 14.dp, y = (-9).dp)
                                ) {
                                    CounterBadge(
                                        count = count,
                                        modifier = Modifier
                                            .clickable {
                                                val targetHash = badgeTargets.getOrNull(index)
                                                if (targetHash == null) {
                                                    // нет цели — просто переключаем вкладку
                                                    selectedTabIndex = index
                                                    return@clickable
                                                }

                                                // Если таб уже выбран — скроллим сразу
                                                if (selectedTabIndex == index) {
                                                    if (!isScrolling.value) {
                                                        isScrolling.value = true
                                                        viewModel.requestScrollToVisit(targetHash)
                                                        viewModel.highlightBId(targetHash, green)
                                                        // сброс флага через delay (задача UI, не критично)
//                                                        LaunchedEffect(targetHash) {
//                                                            delay(800)
                                                            isScrolling.value = false
//                                                        }
                                                    }
                                                } else {
                                                    // Сохраняем pending hash и переключаемся — LaunchedEffect ниже выполнит скролл
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
            val pending = pendingScrollHash.value
            if (pending == null) return@LaunchedEffect

            // если уже скроллится — ждём и затем выходим
            if (isScrolling.value) return@LaunchedEffect

            // Попробуем несколько раз выполнить скролл — даём контенту время отрисоваться
            val maxAttempts = 8
            val delayMs = 150L
            var succeeded = false

            repeat(maxAttempts) { attempt ->
                try {
                    // помечаем что идёт скролл
                    isScrolling.value = true

                    // Вызов твоего метода; он обычно эмитит событие и не бросает исключение
                    viewModel.requestScrollToVisit(pending)
                    viewModel.highlightBId(pending, green)

                    // считаем как успешный — очищаем pending
                    pendingScrollHash.value = null
                    succeeded = true

                    // даём немного времени, чтобы скролл/анимация начались
                    delay(400)
                    isScrolling.value = false
                    return@LaunchedEffect
                } catch (t: Throwable) {
                    // если что-то упало — ждем и пробуем снова
                    isScrolling.value = false
                    delay(delayMs)
                }
            }

            if (!succeeded) {
                // не удалось — снимаем pending, чтобы не зацикливаться
                pendingScrollHash.value = null
                isScrolling.value = false
            }
        }

        // Контент выбранной вкладки
        when (selectedTabIndex) {
            0 -> WpDataContentTab()
            1 -> OtherComposeTab()
//            2 -> AdditionalContentTab()
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
