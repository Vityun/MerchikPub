package ua.com.merchik.merchik.Activities.Features

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.MakePhoto.MakePhoto
import ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.features.main.DBViewModels.AdditionalRequirementsDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.CustomerSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.ImagesTypeListDBViewModel
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
class FeaturesActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MerchikTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = Color.Transparent,
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
                                    else -> null
                                }?.let { viewModel ->
                                    viewModel.dataJson = bundle.getString("dataJson")
                                    viewModel.contextUI =
                                        try {
                                            ContextUI.valueOf(bundle.getString("contextUI") ?: "")
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
                                    viewModel.idResImage = if (bundle.getInt("idResImage") == 0) null else bundle.getInt("idResImage")
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
}


