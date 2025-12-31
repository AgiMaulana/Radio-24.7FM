package io.github.agimaulana.radio

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.agimaulana.radio.core.design.theme.RadioTheme
import io.github.agimaulana.radio.feature.sample.navigateToSampleScreen
import io.github.agimaulana.radio.feature.sample.sampleScreen
import io.github.agimaulana.radio.feature.stationlist.StationListRoute
import io.github.agimaulana.radio.infrastructure.response.RadioStationResponse

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            RadioTheme {
                StationListRoute()
            }
        }
    }
}

@Composable
fun RootNavigation() {
    val rootNavController = rememberNavController()

    NavHost(navController = rootNavController, startDestination = "main_tabs") {
        composable("main_tabs") {
            MainScreen(
                onNavigateToSample = {
                    rootNavController.navigateToSampleScreen()
                }
            )
        }

        sampleScreen()
    }
}