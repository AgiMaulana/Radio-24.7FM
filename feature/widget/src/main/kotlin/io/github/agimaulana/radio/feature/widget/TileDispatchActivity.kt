package io.github.agimaulana.radio.feature.widget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import timber.log.Timber

class TileDispatchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val mediaId = intent?.getStringExtra("extra_media_id")
            if (!mediaId.isNullOrEmpty()) {
                val broadcast = Intent("io.github.agimaulana.radio.action.PLAY_PINNED").apply {
                    `package` = packageName
                    putExtra("extra_media_id", mediaId)
                }
                sendBroadcast(broadcast)
            }
        } catch (t: Throwable) {
            Timber.e(t, "TileDispatchActivity failed")
        }
        finish()
    }
}
