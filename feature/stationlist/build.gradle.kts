plugins {
    id(libs.plugins.boilerplate.android.feature.get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.feature.stationlist"
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":core:shared-test"))
    implementation(project(":domain:api"))
    implementation(libs.androidx.media3.ui.compose.material3)
}