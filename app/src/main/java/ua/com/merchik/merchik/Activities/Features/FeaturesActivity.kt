package ua.com.merchik.merchik.Activities.Features

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammJOINSDB
import ua.com.merchik.merchik.features.main.DBViewModels.AddressSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.LogMPDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.PlanogrammJOINSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.PlanogrammSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.UsersSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataDBViewModel
import ua.com.merchik.merchik.features.main.MainUI

@AndroidEntryPoint
class FeaturesActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MerchikTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainUI(viewModel = viewModel() as LogMPDBViewModel)
                }
            }
        }
    }
}


