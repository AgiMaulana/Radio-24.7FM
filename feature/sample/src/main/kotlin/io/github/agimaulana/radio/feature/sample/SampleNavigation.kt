package io.github.agimaulana.radio.feature.sample

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

private val ROUTE = "sample"

fun NavGraphBuilder.sampleScreen() {
    composable(
        route = ROUTE
    ) {
        SampleRoute()
    }
}

fun NavController.navigateToSampleScreen() {
    navigate(ROUTE)
}
