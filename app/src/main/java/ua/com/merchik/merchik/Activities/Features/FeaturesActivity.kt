package ua.com.merchik.merchik.Activities.Features

import android.os.Bundle
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
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.DBViewModels.AdditionalRequirementsDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.CustomerSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.ImagesTypeListDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.LogMPDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.ReportPrepareDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.StackPhotoDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.ThemeDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.TovarDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.TradeMarkDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.UsersSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.VacancySDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI

@AndroidEntryPoint
class FeaturesActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MerchikTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .clip(RoundedCornerShape(8.dp)),
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
                                    WpDataDBViewModel::class -> viewModel() as WpDataDBViewModel
                                    ImagesTypeListDBViewModel::class -> viewModel() as ImagesTypeListDBViewModel
                                    ReportPrepareDBViewModel::class -> viewModel() as ReportPrepareDBViewModel
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
                                    viewModel.subTitle = bundle.getString("subTitle")
                                    viewModel.idResImage = if (bundle.getInt("idResImage") == 0) null else bundle.getInt("idResImage")
                                    viewModel.context = LocalContext.current
                                    viewModel.updateContent()
                                    MainUI(viewModel = viewModel, LocalContext.current)
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
}


