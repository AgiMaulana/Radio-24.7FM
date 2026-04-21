package io.github.agimaulana.radio.feature.stationlist.player

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PlayerColors(
    val dominant: Color,
    val vibrant: Color,
    val darkMuted: Color,
)

private val fallbackColors = PlayerColors(
    dominant = Color(0xFF1C1A24),
    vibrant = Color(0xFF3a1040),
    darkMuted = Color(0xFF0e0c14),
)

suspend fun extractPlayerColors(
    imageUrl: String?,
    context: android.content.Context,
): PlayerColors {
    if (imageUrl.isNullOrBlank()) return fallbackColors

    return withContext(Dispatchers.IO) {
        try {
            val loader = context.imageLoader
            val request = ImageRequest.Builder(context).data(imageUrl).allowHardware(false).build()
            val result = loader.execute(request)

            val bitmap: Bitmap = ((result as? SuccessResult)?.drawable as? BitmapDrawable)
                ?.bitmap ?: return@withContext fallbackColors

            val scaled = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
            val palette = Palette.from(scaled).generate()

            PlayerColors(
                dominant = palette.getDominantColor(0xFF1C1A24.toInt()).toComposeColor().darken(0.55f),
                vibrant = palette.getVibrantColor(0xFF3a1040.toInt()).toComposeColor().darken(0.45f),
                darkMuted = palette.getDarkMutedColor(0xFF0e0c14.toInt()).toComposeColor().darken(0.65f),
            )
        } catch (e: Exception) {
            fallbackColors
        }
    }
}

private fun Int.toComposeColor() = Color(this)

private fun Color.darken(factor: Float): Color {
    return Color(
        red = red * factor,
        green = green * factor,
        blue = blue * factor,
        alpha = alpha,
    )
}

@Composable
fun DynamicPlayerBackground(
    colors: PlayerColors,
    modifier: Modifier = Modifier,
) {
    val animDuration = 600

    val animatedDominant by animateColorAsState(colors.dominant, tween(animDuration), label = "dominant")
    val animatedVibrant by animateColorAsState(colors.vibrant, tween(animDuration), label = "vibrant")
    val animatedDarkMuted by animateColorAsState(colors.darkMuted, tween(animDuration), label = "darkMuted")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to animatedDominant,
                        0.45f to animatedVibrant,
                        1.00f to animatedDarkMuted,
                    )
                )
            )
    )
}
