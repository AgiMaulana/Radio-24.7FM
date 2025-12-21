package io.github.agimaulana.radio.core.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import io.github.agimaulana.radio.core.design.theme.LightRadioColors
import io.github.agimaulana.radio.core.design.theme.RadioColors

val LocalRadioColors = compositionLocalOf { LightRadioColors }

val LocalRadioTypography = compositionLocalOf { RadioTypography }

object RadioTheme {
    val colors: RadioColors
        @Composable
        get() = LocalRadioColors.current

    val typography: RadioTypography
        @Composable
        get() = LocalRadioTypography.current
}
