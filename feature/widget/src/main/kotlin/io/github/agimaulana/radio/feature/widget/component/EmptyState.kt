package io.github.agimaulana.radio.feature.widget.component

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.GlanceComposable

@Composable
@GlanceComposable
fun EmptyState(modifier: GlanceModifier = GlanceModifier) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .clickable(actionStartActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("radio247fm://stations"))
            )),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pin a station to see it here",
            style = TextStyle(color = GlanceTheme.colors.onBackground)
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(text = "★", style = TextStyle(color = GlanceTheme.colors.primary))
    }
}

@Preview(widthDp = 220, heightDp = 110)
@Composable
fun EmptyStatePreview() {
    GlancePreview {
        EmptyState()
    }
}
