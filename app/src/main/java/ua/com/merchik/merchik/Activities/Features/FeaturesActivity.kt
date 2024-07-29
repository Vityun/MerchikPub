package ua.com.merchik.merchik.Activities.Features

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
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
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB
import ua.com.merchik.merchik.data.RealmModels.LogMPDB
import ua.com.merchik.merchik.features.main.DBViewModels.AdditionalRequirementsDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.CustomerSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.LogMPDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.TovarDBViewModel
import ua.com.merchik.merchik.features.main.MainUI
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

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
                                    CustomerSDBViewModel::class -> viewModel() as CustomerSDBViewModel
                                    else -> null
                                }?.let { viewModel ->
                                    viewModel.dataJson = bundle.getString("dataJson")
                                    viewModel.title = bundle.getString("title")
                                    viewModel.subTitle = bundle.getString("subTitle")
                                    viewModel.idResImage = if (bundle.getInt("idResImage") == 0) null else bundle.getInt("idResImage")
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


