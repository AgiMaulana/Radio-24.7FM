package io.github.agimaulana.radio.feature.stationlist.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.feature.stationlist.R
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.Action
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StationContextMenu(
    showMenu: Boolean,
    station: Station?,
    onDismiss: () -> Unit,
    onAction: (Action) -> Unit,
    useBottomSheet: Boolean = true
) {
    if (station == null || !showMenu) return
    val containerColor = MaterialTheme.colorScheme.surface
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    if (useBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = containerColor,
            dragHandle = null
        ) {
            MenuContent(
                station = station,
                dividerColor = dividerColor,
                onDismiss = onDismiss,
                onAction = onAction
            )
        }
    } else {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = containerColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MenuContent(
                        station = station,
                        dividerColor = dividerColor,
                        onDismiss = onDismiss,
                        onAction = onAction
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuContent(
    station: Station,
    dividerColor: Color,
    onDismiss: () -> Unit,
    onAction: (Action) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Header(station)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = dividerColor
        )

        MenuItem(
            text = stringResource(id = R.string.menu_play_now),
            icon = painterResource(id = R.drawable.ic_play),
            onClick = {
                onAction(Action.Click(station))
                onDismiss()
            }
        )

        if (station.isPinned) {
            MenuItem(
                text = stringResource(id = R.string.menu_remove_pin),
                icon = painterResource(id = R.drawable.ic_star_filled),
                onClick = {
                onAction(Action.UnpinStation(station.serverUuid))
                onDismiss()
                },
                iconColor = RadioTheme.colors.primary
            )
        } else {
            MenuItem(
                text = stringResource(id = R.string.menu_pin_station),
                icon = painterResource(id = R.drawable.ic_star_filled),
                onClick = {
                    onAction(Action.PinStation(station))
                    onDismiss()
                }
            )
        }

        MenuItem(
            text = stringResource(id = R.string.menu_share),
            icon = painterResource(id = R.drawable.ic_share),
            onClick = {
                // TODO: Share action
                onDismiss()
            },
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Header(station: Station) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = station.imageUrl,
                placeholder = painterResource(id = R.drawable.station_default),
                error = painterResource(id = R.drawable.station_default)
            ),
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = station.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
            Text(
                text = station.genre,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun MenuItem(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    iconColor: Color = RadioTheme.colors.primary,
    textColor: Color = RadioTheme.colors.primary
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Preview
@Composable
private fun StationContextMenuPreview() {
    PreviewTheme {
        StationContextMenu(
            showMenu = true,
            station = Station(
                serverUuid = "1",
                name = "Most 105.8 FM Jakarta",
                genre = "90s",
                imageUrl = "",
                streamUrl = "",
                isBuffering = false,
                isPlaying = false,
                isPinned = false
            ),
            onDismiss = {},
            onAction = {},
            useBottomSheet = true
        )
    }
}

@Preview
@Composable
private fun StationContextMenuDialogPreview() {
    PreviewTheme {
        StationContextMenu(
            showMenu = true,
            station = Station(
                serverUuid = "1",
                name = "Most 105.8 FM Jakarta",
                genre = "90s",
                imageUrl = "",
                streamUrl = "",
                isBuffering = false,
                isPlaying = false,
                isPinned = true
            ),
            onDismiss = {},
            onAction = {},
            useBottomSheet = false
        )
    }
}
