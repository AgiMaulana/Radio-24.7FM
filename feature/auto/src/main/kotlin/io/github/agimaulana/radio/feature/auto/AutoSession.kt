package io.github.agimaulana.radio.feature.auto

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import io.github.agimaulana.radio.feature.auto.screen.main.MainScreen

class AutoSession : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        // Pass the factory down to whatever screen is starting
        return MainScreen(carContext)
    }

//    override fun onNewIntent(intent: Intent) {
//        // Voice command entry point — push search screen over current stack
//        screenManager.pushForResult(
//            SearchScreen(
//                carContext = carContext,
//                getRadioStationsUseCase = getRadioStationsUseCase,
//                getPinnedStationsUseCase = getPinnedStationsUseCase,
//                playerController = playerController,
//            )
//        ) { /* no result handling needed */ }
//    }
}
