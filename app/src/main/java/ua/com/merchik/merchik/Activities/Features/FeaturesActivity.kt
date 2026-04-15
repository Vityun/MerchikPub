package ua.com.merchik.merchik.Activities.Features

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.MakePhoto.MakePhoto
import ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.LaunchOrigin
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.main.DBViewModels.AdditionalRequirementsDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.CustomerSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.ImagesTypeListDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.JournalPhotoSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.LogMPDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.OpinionSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.PlanogrammVizitShowcaseViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.ReportPrepareDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.SMSPlanSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.SamplePhotoSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.ShowcaseDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.StackPhotoDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.ThemeDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.TovarDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.TradeMarkDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.UsersSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.VacancySDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI
import ua.com.merchik.merchik.toolbar_menus
import java.io.File

@AndroidEntryPoint
class FeaturesActivity : AppCompatActivity() {
    private lateinit var launchOrigin: LaunchOrigin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchOrigin = intent?.readLaunchOriginOrNull() ?: LaunchOrigin(0,0,0,0)

        val normalizedLaunchOrigin = launchOrigin.takeIf {
            it.x != 0 || it.y != 0 || it.width != 0 || it.height != 0
        }

        if (normalizedLaunchOrigin != null) {
            overridePendingTransition(0, 0)
        }
        setContent {
            MerchikTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = Color.Transparent,
                ) {
                    val user: UsersSDB = RoomManager.SQL_DB.usersDao().getUserById(Globals.userId)
                    val animationTime = when {
                        user.reportDate05 == null -> 2650
                        user.reportDate20 == null -> 1550
                        else -> 850
                    }

                    FeaturesLaunchAnimationContainer(
                        origin = normalizedLaunchOrigin,
                        durationMillis = animationTime
                    ) {
                        intent?.let { intent ->
                            intent.extras?.let { bundle ->
                                bundle.getString("viewModel")?.let {
                                    when (Class.forName(it).kotlin) {
                                        LogMPDBViewModel::class -> viewModel() as LogMPDBViewModel
                                        AdditionalRequirementsDBViewModel::class -> viewModel() as AdditionalRequirementsDBViewModel
                                        TovarDBViewModel::class -> viewModel() as TovarDBViewModel
                                        TradeMarkDBViewModel::class -> viewModel() as TradeMarkDBViewModel
                                        ThemeDBViewModel::class -> viewModel() as ThemeDBViewModel
                                        StackPhotoDBViewModel::class -> viewModel() as StackPhotoDBViewModel
                                        CustomerSDBViewModel::class -> viewModel() as CustomerSDBViewModel
                                        UsersSDBViewModel::class -> viewModel() as UsersSDBViewModel
                                        VacancySDBViewModel::class -> viewModel() as VacancySDBViewModel
                                        SamplePhotoSDBViewModel::class -> viewModel() as SamplePhotoSDBViewModel
                                        WpDataDBViewModel::class -> viewModel() as WpDataDBViewModel
                                        ImagesTypeListDBViewModel::class -> viewModel() as ImagesTypeListDBViewModel
                                        ReportPrepareDBViewModel::class -> viewModel() as ReportPrepareDBViewModel
                                        OpinionSDBViewModel::class -> viewModel() as OpinionSDBViewModel
                                        PlanogrammVizitShowcaseViewModel::class -> viewModel() as PlanogrammVizitShowcaseViewModel
                                        ShowcaseDBViewModel::class -> viewModel() as ShowcaseDBViewModel
                                        SMSPlanSDBViewModel::class -> viewModel() as SMSPlanSDBViewModel
                                        JournalPhotoSDBViewModel::class -> viewModel() as JournalPhotoSDBViewModel
                                        else -> null
                                    }?.let { viewModel ->
                                        viewModel.dataJson = bundle.getString("dataJson")
                                        viewModel.contextUI =
                                            try {
                                                ContextUI.valueOf(
                                                    bundle.getString("contextUI") ?: ""
                                                )
                                            } catch (e: Exception) {
                                                ContextUI.DEFAULT
                                            }
                                        viewModel.modeUI =
                                            try {
                                                ModeUI.valueOf(bundle.getString("modeUI") ?: "")
                                            } catch (e: Exception) {
                                                ModeUI.DEFAULT
                                            }
                                        viewModel.title = bundle.getString("title")
                                        viewModel.typeWindow = bundle.getString("typeWindow") ?: ""
                                        viewModel.subTitle = bundle.getString("subTitle")
                                        viewModel.idResImage =
                                            if (bundle.getInt("idResImage") == 0) null else bundle.getInt(
                                                "idResImage"
                                            )
                                        viewModel.context = LocalContext.current
                                        viewModel.updateContent()
                                        MainUI(
                                            modifier = Modifier.then(
                                                when (bundle.getString("typeWindow")?.lowercase()) {
                                                    "full" -> Modifier
                                                    "container" -> Modifier
                                                    else -> Modifier
                                                        .padding(20.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                }
                                            ),
                                            viewModel = viewModel,
                                            LocalContext.current
                                        )
                                    } ?: {
                                        finish()
                                    }
                                }
                            }
                        } ?: run {
                            finish()
                        }
                    }
                }
            }
            RequestNotificationsPermissionPersistent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MakePhoto.PICK_GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            try {
//                    int photoType = data.getIntExtra("photo_type", 4); // Получаем тип фотографии из Intent
                val photoType = MakePhotoFromGalery.photoType // Получаем тип фотографии из Intent


                val uri = data.data
                val file = File(
                    Globals.FileUtils.getRealPathFromUri(
                        applicationContext, uri
                    )
                )
                val stackPhotoDB = DetailedReportActivity.savePhoto(
                    file,
                    MakePhotoFromGalery.MakePhotoFromGaleryWpDataDB,
                    photoType,
                    MakePhotoFromGalery.tovarId,
                    applicationContext
                )

                if (stackPhotoDB != null) {
                    // Сохраняем результат что фото сохранено
                    val resultIntent = Intent()
                    resultIntent.putExtra(
                        "photo_saved",
                        true
                    ) // Передайте информацию о сохраненном фото
                    setResult(RESULT_OK, resultIntent)
                }
            } catch (e: java.lang.Exception) {
                Globals.writeToMLOG(
                    "INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST",
                    "Exception e: $e"
                )
            }
        } else if (requestCode == MakePhoto.CAMERA_REQUEST_TAKE_PHOTO_TEST && resultCode == RESULT_OK) {
            DetailedReportActivity.savePhoto(Globals(), this)
        } else if (requestCode == MakePhoto.CAMERA_REQUEST_TAKE_PHOTO_TEST && resultCode == RESULT_CANCELED) {
            StackPhotoRealm.deleteByPhotoNum(MakePhoto.photoNum)
        }
    }

//    override fun finish() {
//        super.finish()
//        if (intent?.readLaunchOriginOrNull() != null) {
//            overridePendingTransition(0, 0)
//        }
//    }
}


@Composable
fun RequestNotificationsPermissionPersistent() {
    if (Build.VERSION.SDK_INT >= 33) {
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
private fun FeaturesLaunchAnimationContainer(
    origin: LaunchOrigin?,
    durationMillis: Int = 2500,
    content: @Composable () -> Unit
) {
    val slowThenFastEasing = remember {
        CubicBezierEasing(
            0.85f, 0f,
            1f, 1f
        )
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current
        val fullWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
        val fullHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)

        val initialScaleX = if (origin != null) {
            (origin.width / fullWidthPx).coerceIn(0.05f, 1f)
        } else 1f

        val initialScaleY = if (origin != null) {
            (origin.height / fullHeightPx).coerceIn(0.05f, 1f)
        } else 1f

        val initialTranslationX = if (origin != null) origin.x.toFloat() else 0f
        val initialTranslationY = if (origin != null) (origin.y + 150).toFloat() else 0f
        val initialAlpha = if (origin != null) 0.55f else 1f

        val scaleX = remember(origin) { Animatable(initialScaleX) }
        val scaleY = remember(origin) { Animatable(initialScaleY) }
        val translationX = remember(origin) { Animatable(initialTranslationX) }
        val translationY = remember(origin) { Animatable(initialTranslationY) }
        val alpha = remember(origin) { Animatable(initialAlpha) }

        LaunchedEffect(origin, fullWidthPx, fullHeightPx) {
            if (origin != null) {
                scaleX.snapTo(initialScaleX)
                scaleY.snapTo(initialScaleY)
                translationX.snapTo(initialTranslationX)
                translationY.snapTo(initialTranslationY)
                alpha.snapTo(initialAlpha)

                coroutineScope {
                    launch {
                        scaleX.animateTo(
                            1f,
                            animationSpec = tween(
                                durationMillis = durationMillis,
                                easing = slowThenFastEasing
                            )
                        )
                    }
                    launch {
                        scaleY.animateTo(
                            1f,
                            animationSpec = tween(
                                durationMillis = durationMillis,
                                easing = slowThenFastEasing
                            )
                        )
                    }
                    launch {
                        translationX.animateTo(
                            0f,
                            animationSpec = tween(
                                durationMillis = durationMillis,
                                easing = slowThenFastEasing
                            )
                        )
                    }
                    launch {
                        translationY.animateTo(
                            0f,
                            animationSpec = tween(
                                durationMillis = durationMillis,
                                easing = slowThenFastEasing
                            )
                        )
                    }
                    launch {
                        alpha.animateTo(
                            1f,
                            animationSpec = tween(
                                durationMillis = (durationMillis * 0.55f).toInt(),
                                easing = LinearEasing
                            )
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.scaleX = scaleX.value
                    this.scaleY = scaleY.value
                    this.translationX = translationX.value
                    this.translationY = translationY.value
                    this.alpha = alpha.value
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) {
            content()
        }
    }
}

private fun Intent.readLaunchOriginOrNull(): LaunchOrigin? {
    val left = getIntExtra("launch_origin_left", -1)
    val top = getIntExtra("launch_origin_top", -1)
    val width = getIntExtra("launch_origin_width", -1)
    val height = getIntExtra("launch_origin_height", -1)

    return if (left >= 0 && top >= 0 && width > 0 && height > 0) {
        LaunchOrigin(left, top, width, height)
    } else {
        null
    }
}