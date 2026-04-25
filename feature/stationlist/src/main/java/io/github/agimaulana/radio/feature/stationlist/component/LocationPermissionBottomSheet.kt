package io.github.agimaulana.radio.feature.stationlist.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.feature.stationlist.R
import kotlinx.coroutines.launch

private val BottomSheetBackground = Color(0xFF1C1A24)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPermissionBottomSheet(
    onAllowClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.PartiallyExpanded },
    ),
) {
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = BottomSheetBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
            )
        },
        modifier = modifier,
    ) {
        PermissionBottomSheetContent(
            onAllowClick = {
                scope.launch {
                    sheetState.hide()
                    onAllowClick()
                }
            },
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
            },
        )
    }
}

@Composable
private fun PermissionBottomSheetContent(
    onAllowClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        PermissionHeader()
        Spacer(modifier = Modifier.height(24.dp))
        PermissionDescription()
        Spacer(modifier = Modifier.height(24.dp))
        PermissionInfoItems()
        Spacer(modifier = Modifier.height(32.dp))
        PermissionActionButtons(onAllowClick, onDismissRequest)
    }
}

@Composable
private fun PermissionHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_perm_location),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = R.string.location_perm_title),
            style = RadioTheme.typography.stationTitle.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 28.sp,
            ),
        )
    }
}

@Composable
private fun PermissionDescription() {
    Text(
        text = stringResource(id = R.string.location_perm_description),
        style = RadioTheme.typography.stationTitle.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = 0.7f),
            lineHeight = 22.sp,
        ),
    )
}

@Composable
private fun PermissionInfoItems() {
    Column {
        PermissionInfoItem(
            iconResId = R.drawable.ic_perm_radio,
            title = stringResource(id = R.string.location_perm_local_stations_title),
            description = stringResource(id = R.string.location_perm_local_stations_desc),
        )
        Spacer(modifier = Modifier.height(12.dp))
        PermissionInfoItem(
            iconResId = R.drawable.ic_perm_lock,
            title = stringResource(id = R.string.location_perm_private_title),
            description = stringResource(id = R.string.location_perm_private_desc),
        )
        Spacer(modifier = Modifier.height(12.dp))
        PermissionInfoItem(
            iconResId = R.drawable.ic_perm_settings,
            title = stringResource(id = R.string.location_perm_revokable_title),
            description = stringResource(id = R.string.location_perm_revokable_desc),
        )
    }
}

@Composable
private fun PermissionActionButtons(
    onAllowClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Button(
        onClick = onAllowClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RadioTheme.colors.primary,
            contentColor = Color.Black,
        ),
    ) {
        Text(
            text = stringResource(id = R.string.location_perm_allow_button),
            style = RadioTheme.typography.stationTitle.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(
        onClick = onDismissRequest,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Text(
            text = stringResource(id = R.string.location_perm_not_now_button),
            style = RadioTheme.typography.stationTitle.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.7f),
            ),
        )
    }
}

@Composable
private fun PermissionInfoItem(
    iconResId: Int,
    title: String,
    description: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = RadioTheme.colors.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                        append(title)
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.5f),
                        ),
                    ) {
                        append(" • ")
                        append(description)
                    }
                },
                style = RadioTheme.typography.stationTitle.copy(fontSize = 14.sp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun LocationPermissionBottomSheetPreview() {
    PreviewTheme {
        LocationPermissionBottomSheet(
            onAllowClick = {},
            onDismissRequest = {},
        )
    }
}
