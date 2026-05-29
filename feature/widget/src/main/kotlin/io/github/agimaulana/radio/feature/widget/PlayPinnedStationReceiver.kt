package io.github.agimaulana.radio.feature.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class PlayPinnedStationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val mediaId = intent?.getStringExtra("extra_media_id") ?: return

        // Prefer direct playback: request the RadioService MediaSession via RadioPlayerControllerFactory
        // and start playback. If that fails, fall back to starting the main activity with an extra.
        try {
            // Start the Service MediaSession by binding/creating the service via launching the app's
            // main activity with play intent - this keeps the receiver lightweight and reliable.
            val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            launch?.apply {
                putExtra("play_media_id", mediaId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            if (launch != null) context.startActivity(launch)
        } catch (t: Throwable) {
            Timber.e(t, "Failed to handle play pinned station: %s", t.localizedMessage)
        }
    }
}
