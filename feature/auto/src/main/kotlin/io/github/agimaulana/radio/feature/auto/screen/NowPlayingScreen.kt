package io.github.agimaulana.radio.feature.auto.screen

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.PinStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.UnpinStationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NowPlayingScreen(
    carContext: CarContext,
    private val station: RadioStation,
    private val pinStationUseCase: PinStationUseCase,
    private val unpinStationUseCase: UnpinStationUseCase,
    private val playerController: StateFlow<RadioPlayerController?>,
) : Screen(carContext) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isPlaying = true
    private var isPinned = false

    init {
        scope.launch {
            playerController.filterNotNull().first().also { controller ->
                isPlaying = controller.isPlaying
                invalidate()
                controller.event.collect { event ->
                    when (event) {
                        is PlaybackEvent.PlayingChanged -> {
                            isPlaying = event.isPlaying
                            invalidate()
                        }
                        is PlaybackEvent.StateChanged,
                        is PlaybackEvent.MediaItemTransition -> Unit
                    }
                }
            }
        }
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                scope.cancel()
            }
        })
    }

    override fun onGetTemplate(): Template {
        val controller = playerController.value

        val playPauseAction = Action.Builder()
            .setIcon(CarIcon.APP_ICON) // In a real app, use play/pause icons
            .setTitle(if (isPlaying) "Pause" else "Play")
            .setOnClickListener {
                if (isPlaying) controller?.pause() else controller?.play()
            }
            .build()

        val stopAction = Action.Builder()
            .setTitle("Stop")
            .setOnClickListener { controller?.stop() }
            .build()

        val pinAction = Action.Builder()
            .setTitle(if (isPinned) "Unpin" else "Pin")
            .setOnClickListener {
                scope.launch {
                    if (isPinned) {
                        unpinStationUseCase.execute(station.stationUuid)
                        isPinned = false
                    } else {
                        pinStationUseCase.execute(station)
                        isPinned = true
                    }
                    invalidate()
                }
            }
            .build()

        val pane = Pane.Builder()
            .setImage(CarIcon.APP_ICON) // Should be station artwork
            .addRow(
                Row.Builder()
                    .setTitle(station.name)
                    .addText(station.tags.firstOrNull() ?: "")
                    .addText(if (isPlaying) "Playing" else "Paused")
                    .build()
            )
            .addAction(playPauseAction)
            .addAction(stopAction)
            .build()

        return PaneTemplate.Builder(pane)
            .setHeaderAction(Action.BACK)
            .setTitle("Now Playing")
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(pinAction)
                    .build()
            )
            .build()
    }
}
