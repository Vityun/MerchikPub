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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.features.main.DBViewModels.LogMPDBViewModel
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
                                val dataJson = bundle.getString("dataJson")
                                when (Class.forName(it).kotlin) {
                                    LogMPDBViewModel::class -> {
                                        val viewModel = viewModel() as LogMPDBViewModel
                                        viewModel.dataJson = dataJson
                                        MainUI(viewModel = viewModel, this)
                                    }
                                    else -> { finish() }
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


