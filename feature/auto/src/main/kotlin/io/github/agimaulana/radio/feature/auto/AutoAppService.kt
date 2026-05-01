package io.github.agimaulana.radio.feature.auto

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import dagger.hilt.android.AndroidEntryPoint
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import io.github.agimaulana.radio.domain.api.usecase.GetPinnedStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.PinStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.UnpinStationUseCase
import javax.inject.Inject

@AndroidEntryPoint
class AutoAppService : CarAppService() {

    @Inject lateinit var getRadioStationsUseCase: GetRadioStationsUseCase
    @Inject lateinit var getPinnedStationsUseCase: GetPinnedStationsUseCase
    @Inject lateinit var pinStationUseCase: PinStationUseCase
    @Inject lateinit var unpinStationUseCase: UnpinStationUseCase
    @Inject lateinit var radioPlayerControllerFactory: RadioPlayerControllerFactory

    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = AutoSession(
        getRadioStationsUseCase = getRadioStationsUseCase,
        getPinnedStationsUseCase = getPinnedStationsUseCase,
        pinStationUseCase = pinStationUseCase,
        unpinStationUseCase = unpinStationUseCase,
        radioPlayerControllerFactory = radioPlayerControllerFactory,
    )
}
