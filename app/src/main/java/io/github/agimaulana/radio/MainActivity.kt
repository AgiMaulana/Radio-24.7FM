@file:Suppress("FunctionNaming")

package io.github.agimaulana.radio

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.agimaulana.radio.core.design.theme.RadioTheme
import io.github.agimaulana.radio.feature.stationlist.StationListRoute

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
