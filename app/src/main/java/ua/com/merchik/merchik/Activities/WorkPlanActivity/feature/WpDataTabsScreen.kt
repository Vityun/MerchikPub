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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.collectLatest
import ua.com.merchik.merchik.Activities.CronchikViewModel
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs.AdditionalContentTab
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs.OtherComposeTab
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs.WpDataContentTab
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
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

    val tabTitles = listOf(
        stringResource(R.string.title_0),
        "Доп.Заработок",
        "Заявки"
    )

//    cronchikViewModel.updateBadge(1, 10)
    cronchikViewModel.updateBadgeAdditionalIncome()
    // Кол-во уведомлений на вкладках. null или 0 — не отображаем.
    val badgeCounts = remember { cronchikViewModel.badgeCounts }

    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = tabBarBackground,
            indicator = {}, // отключаем дефолтный индикатор
            divider = {}             // <-- отключаем линию
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
                        ) {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) textSelectedColor else textUnselectedColor,
                                modifier = Modifier.align(Alignment.Center)
                            )
                            val count = badgeCounts.getOrNull(index)
                            if (count != null && count > 0) {
                                CounterBadge(
                                    count = count,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 14.dp, y = (-9).dp)
                                )
//                                Box(
//                                    modifier = Modifier
//                                        .align(Alignment.TopEnd)
//                                        .offset(x = 14.dp, y = (-9).dp)
//                                        .background(Color.Red, CircleShape)
//                                        .border(
//                                            width = 1.dp,
//                                            color = Color.White,
//                                            shape = CircleShape
//                                        )
//                                        .padding(horizontal = 5.dp, vertical = 2.dp)
//                                ) {
//                                    Text(
//                                        text = if (count > 9) "9+" else count.toString(),
//                                        color = Color.White,
//                                        fontSize = 11.sp,
//                                    )
//                                }
                            }
                        }
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> WpDataContentTab()
            1 -> OtherComposeTab()
            2 -> AdditionalContentTab()
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
