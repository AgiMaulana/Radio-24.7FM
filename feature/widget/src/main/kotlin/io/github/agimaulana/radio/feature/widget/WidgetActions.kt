package io.github.agimaulana.radio.feature.widget

import android.content.Context
import dagger.hilt.android.EntryPointAccessors
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val StationUuidKey = ActionParameters.Key<String>("station_uuid")

class PlayStationAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val stationUuid = parameters[StationUuidKey] ?: return
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val getRadioStationUseCase = entryPoint.getRadioStationUseCase()
        val radioPlayerControllerFactory = entryPoint.radioPlayerControllerFactory()

        val station = getRadioStationUseCase.execute(stationUuid)
        withContext(Dispatchers.Main) {
            val controller = radioPlayerControllerFactory.get()
            controller.startPlayback(listOf(station.toRadioMediaItem()))
        }
    }
}

class TogglePlaybackAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val radioPlayerControllerFactory = entryPoint.radioPlayerControllerFactory()
        withContext(Dispatchers.Main) {
            val controller = radioPlayerControllerFactory.get()
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }
}

fun RadioStation.toRadioMediaItem() = RadioMediaItem(
    mediaId = stationUuid,
    streamUrl = url,
    radioMetadata = RadioMediaItem.RadioMetadata(
        stationName = name,
        genre = tags.getOrNull(0).orEmpty(),
        imageUrl = imageUrl
    )
)
