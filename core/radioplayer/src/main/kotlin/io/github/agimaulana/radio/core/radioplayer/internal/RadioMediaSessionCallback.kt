package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession

internal class RadioMediaSessionCallback : MediaSession.Callback {
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)

        @OptIn(UnstableApi::class)
        val customPlayerCommands = connectionResult.availablePlayerCommands.buildUpon()
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
            .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .build()

        return MediaSession.ConnectionResult.accept(
            connectionResult.availableSessionCommands,
            customPlayerCommands
        )
    }
}
