package io.github.agimaulana.radio.feature.auto

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import io.github.agimaulana.radio.domain.api.usecase.GetPinnedStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.PinStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.UnpinStationUseCase
import io.github.agimaulana.radio.feature.auto.screen.BrowseScreen
import io.github.agimaulana.radio.feature.auto.screen.SearchScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AutoSession(
    private val getRadioStationsUseCase: GetRadioStationsUseCase,
    private val getPinnedStationsUseCase: GetPinnedStationsUseCase,
    private val pinStationUseCase: PinStationUseCase,
    private val unpinStationUseCase: UnpinStationUseCase,
    private val radioPlayerControllerFactory: RadioPlayerControllerFactory,
) : Session() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _playerController = MutableStateFlow<RadioPlayerController?>(null)
    val playerController: StateFlow<RadioPlayerController?> = _playerController

    override fun onCreateScreen(intent: Intent): Screen {
        scope.launch {
            _playerController.value = radioPlayerControllerFactory.get()
        }
        return BrowseScreen(
            carContext = carContext,
            getRadioStationsUseCase = getRadioStationsUseCase,
            getPinnedStationsUseCase = getPinnedStationsUseCase,
            pinStationUseCase = pinStationUseCase,
            unpinStationUseCase = unpinStationUseCase,
            playerController = playerController,
        )
    }

    override fun onNewIntent(intent: Intent) {
        // Voice command entry point — push search screen over current stack
        screenManager.pushForResult(
            SearchScreen(
                carContext = carContext,
                getRadioStationsUseCase = getRadioStationsUseCase,
                getPinnedStationsUseCase = getPinnedStationsUseCase,
                playerController = playerController,
            )
        ) { /* no result handling needed */ }
    }

    override fun onDestroy() {
        _playerController.value?.release()
        scope.cancel()
        super.onDestroy()
    }
}
