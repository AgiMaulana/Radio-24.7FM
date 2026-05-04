package io.github.agimaulana.radio.feature.stationlist.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController.CastState
import io.github.agimaulana.radio.feature.stationlist.R

@Composable
fun CastButton(
    castState: CastState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (castState) {
        CastState.NO_DEVICES, CastState.NOT_CONNECTED -> Color(0xFFFF69B4).copy(alpha = 0.4f)
        CastState.CONNECTING -> Color(0xFFFFD700).copy(alpha = 0.4f)
        CastState.CONNECTED -> Color(0xFFFF69B4).copy(alpha = 0.4f)
    }

    val iconTint = when (castState) {
        CastState.NO_DEVICES, CastState.NOT_CONNECTED -> Color(0xFFFF69B4)
        CastState.CONNECTING -> Color(0xFFFFD700)
        CastState.CONNECTED -> Color(0xFFFF69B4)
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_cast),
            contentDescription = "Cast",
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        if (castState == CastState.CONNECTED) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .background(Color(0xFF00FF00), CircleShape)
                    .border(1.dp, Color.Black, CircleShape)
            )
        }
    }
}
