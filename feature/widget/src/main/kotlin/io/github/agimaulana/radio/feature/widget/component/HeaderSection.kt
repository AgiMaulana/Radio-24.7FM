package io.github.agimaulana.radio.feature.widget.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.GlanceComposable

@Composable
@GlanceComposable
fun HeaderSection(modifier: GlanceModifier = GlanceModifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "24.7 FM",
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(
            text = "★ Pinned",
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 12.sp
            )
        )
    }
}

@Preview(widthDp = 220, heightDp = 40)
@Composable
fun HeaderSectionPreview() {
    GlancePreview {
        HeaderSection()
    }
}
